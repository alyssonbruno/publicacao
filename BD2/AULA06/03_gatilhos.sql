-- =============================================================
-- Banco de Dados II — UNITINS | Prof. Alysson Martins Bruno
-- Aula 06 — Gatilhos, cursores, controle e exceções
-- Arquivo: 03_gatilhos.sql
--
-- Parte 4 da aula: os gatilhos (triggers).
--
-- Cada gatilho é feito de DUAS peças:
--   1. uma FUNÇÃO GATILHO — não recebe parâmetros e devolve o
--      tipo especial "trigger";
--   2. o comando CREATE TRIGGER — amarra essa função a uma
--      tabela, a um evento e a um momento.
--
-- Executar no banco "loja", depois de 01_esquema.sql e
-- 02_rotinas.sql:
--   podman exec -i bd2-pg psql -U postgres -d loja < 03_gatilhos.sql
--
-- Convenção de nomes da disciplina:
--   fn_tg_  função gatilho        tg_  gatilho
-- =============================================================

-- Os gatilhos são apagados junto com a tabela; estes DROP servem
-- para o caso de você rodar o arquivo mais de uma vez sem recriar
-- o esquema.
DROP TRIGGER IF EXISTS tg_cliente_normalizar      ON cliente;
DROP TRIGGER IF EXISTS tg_produto_limitar_reajuste ON produto;
DROP TRIGGER IF EXISTS tg_produto_auditar          ON produto;
DROP TRIGGER IF EXISTS tg_produto_resumo           ON produto;
DROP TRIGGER IF EXISTS tg_item_total               ON item_pedido;


-- =============================================================
-- 1) BEFORE ... FOR EACH ROW — corrigir o dado antes de gravar
--
-- Só um gatilho BEFORE ROW consegue alterar o que será gravado:
-- ele mexe no registro NEW e o devolve. Devolver NULL, aqui,
-- cancelaria a gravação daquela linha.
-- =============================================================
CREATE OR REPLACE FUNCTION fn_tg_normalizar_cliente()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.nome   := btrim(NEW.nome);
    NEW.cidade := btrim(NEW.cidade);
    NEW.email  := lower(btrim(NEW.email));

    IF NEW.nome = '' THEN
        RAISE EXCEPTION 'O nome do cliente não pode ser vazio.'
            USING HINT = 'Informe o nome completo do cliente.';
    END IF;

    RETURN NEW;      -- segue a gravação, já com os dados corrigidos
END;
$$;

CREATE TRIGGER tg_cliente_normalizar
    BEFORE INSERT OR UPDATE ON cliente
    FOR EACH ROW
    EXECUTE FUNCTION fn_tg_normalizar_cliente();


-- =============================================================
-- 2) BEFORE ... WHEN — a regra que a restrição CHECK não alcança
--
-- Uma CHECK enxerga apenas a linha nova, isolada. Para comparar
-- o valor antigo com o novo é preciso um gatilho, porque só ele
-- recebe OLD e NEW ao mesmo tempo.
--
-- A cláusula WHEN evita chamar a função quando o preço não mudou.
-- =============================================================
CREATE OR REPLACE FUNCTION fn_tg_limitar_reajuste()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_variacao numeric;
BEGIN
    v_variacao := round((NEW.preco - OLD.preco) / OLD.preco * 100, 2);

    IF v_variacao > 50 THEN
        RAISE EXCEPTION
            'Reajuste de % por cento no produto % excede o limite de 50 por cento.',
            v_variacao, OLD.nome
            USING HINT = 'Reajustes maiores exigem autorização da diretoria.';
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER tg_produto_limitar_reajuste
    BEFORE UPDATE ON produto
    FOR EACH ROW
    WHEN (NEW.preco IS DISTINCT FROM OLD.preco)
    EXECUTE FUNCTION fn_tg_limitar_reajuste();


-- =============================================================
-- 3) AFTER ... FOR EACH ROW — registrar o que já aconteceu
--
-- A trilha de auditoria só faz sentido depois que a gravação deu
-- certo: por isso AFTER. O retorno de um gatilho AFTER é
-- ignorado pelo servidor; por convenção, devolvemos NULL.
--
-- TG_OP informa qual comando disparou o gatilho. Em INSERT não
-- existe OLD; em DELETE não existe NEW. Ler o registro errado é
-- o engano mais comum nesta parte da matéria.
-- =============================================================
CREATE OR REPLACE FUNCTION fn_tg_auditar_produto()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF TG_OP = 'INSERT' THEN
        INSERT INTO auditoria_produto
               (produto_id, operacao, campo, valor_antigo, valor_novo)
        VALUES (NEW.id, TG_OP, 'registro', NULL,
                format('%s | R$ %s | %s un.', NEW.nome, NEW.preco, NEW.estoque));

    ELSIF TG_OP = 'UPDATE' THEN
        IF NEW.preco IS DISTINCT FROM OLD.preco THEN
            INSERT INTO auditoria_produto
                   (produto_id, operacao, campo, valor_antigo, valor_novo)
            VALUES (NEW.id, TG_OP, 'preco', OLD.preco::text, NEW.preco::text);
        END IF;

        IF NEW.estoque IS DISTINCT FROM OLD.estoque THEN
            INSERT INTO auditoria_produto
                   (produto_id, operacao, campo, valor_antigo, valor_novo)
            VALUES (NEW.id, TG_OP, 'estoque', OLD.estoque::text, NEW.estoque::text);
        END IF;

    ELSIF TG_OP = 'DELETE' THEN
        INSERT INTO auditoria_produto
               (produto_id, operacao, campo, valor_antigo, valor_novo)
        VALUES (OLD.id, TG_OP, 'registro',
                format('%s | R$ %s | %s un.', OLD.nome, OLD.preco, OLD.estoque),
                NULL);
    END IF;

    RETURN NULL;     -- gatilho AFTER: o retorno não é aproveitado
END;
$$;

CREATE TRIGGER tg_produto_auditar
    AFTER INSERT OR UPDATE OR DELETE ON produto
    FOR EACH ROW
    EXECUTE FUNCTION fn_tg_auditar_produto();


-- =============================================================
-- 4) AFTER ... FOR EACH STATEMENT — uma vez por comando
--
-- O gatilho de linha roda uma vez para cada linha afetada; o de
-- comando roda uma única vez, mesmo que o UPDATE altere mil
-- linhas. Em um gatilho de comando não existem OLD nem NEW.
-- =============================================================
CREATE OR REPLACE FUNCTION fn_tg_resumo_produto()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_quantos integer;
BEGIN
    SELECT count(*) INTO v_quantos FROM produto;

    RAISE NOTICE 'Comando % concluído em %; a tabela tem % produto(s).',
          TG_OP, TG_TABLE_NAME, v_quantos;

    RETURN NULL;
END;
$$;

CREATE TRIGGER tg_produto_resumo
    AFTER UPDATE ON produto
    FOR EACH STATEMENT
    EXECUTE FUNCTION fn_tg_resumo_produto();


-- =============================================================
-- 5) Gatilho que mantém um valor derivado
--
-- pedido.total deixa de ser recalculado a cada consulta: passa a
-- ser atualizado sozinho sempre que os itens do pedido mudam.
--
-- Repare no cuidado com o DELETE (só existe OLD) e no caso raro
-- do UPDATE que muda o item de pedido: dois pedidos precisam ser
-- recalculados.
-- =============================================================
CREATE OR REPLACE FUNCTION fn_tg_total_pedido()
RETURNS trigger
LANGUAGE plpgsql
AS $$
DECLARE
    v_pedido integer;
BEGIN
    IF TG_OP = 'DELETE' THEN
        v_pedido := OLD.pedido_id;
    ELSE
        v_pedido := NEW.pedido_id;
    END IF;

    UPDATE pedido
       SET total = fn_total_pedido(v_pedido)
     WHERE id = v_pedido;

    IF TG_OP = 'UPDATE' AND OLD.pedido_id IS DISTINCT FROM NEW.pedido_id THEN
        UPDATE pedido
           SET total = fn_total_pedido(OLD.pedido_id)
         WHERE id = OLD.pedido_id;
    END IF;

    RETURN NULL;
END;
$$;

CREATE TRIGGER tg_item_total
    AFTER INSERT OR UPDATE OR DELETE ON item_pedido
    FOR EACH ROW
    EXECUTE FUNCTION fn_tg_total_pedido();


-- =============================================================
-- 6) O gatilho vale do momento em que existe PARA A FRENTE
--
-- Os pedidos gravados antes da criação do gatilho continuam com
-- total zerado. Quem corrige o passado é este comando, executado
-- uma única vez.
-- =============================================================
UPDATE pedido p SET total = fn_total_pedido(p.id);
