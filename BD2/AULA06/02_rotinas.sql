-- =============================================================
-- Banco de Dados II — UNITINS | Prof. Alysson Martins Bruno
-- Aula 06 — Gatilhos, cursores, controle e exceções
-- Arquivo: 02_rotinas.sql
--
-- Rotinas que exercitam as três primeiras partes da aula:
--   Parte 1  estruturas de controle (IF, CASE, FOR, WHILE, LOOP)
--   Parte 2  tratamento de exceções (EXCEPTION, RAISE)
--   Parte 3  cursores (implícito e explícito)
--
-- Executar no banco "loja", depois de 01_esquema.sql:
--   podman exec -i bd2-pg psql -U postgres -d loja < 02_rotinas.sql
--
-- Use este arquivo para CONFERIR o seu trabalho depois de
-- escrever cada rotina. Rodar tudo de uma vez cria as rotinas
-- prontas e tira da prática justamente o que ela treina.
--
-- Convenção de nomes da disciplina:
--   p_  parâmetro     v_  variável      c_  cursor
--   fn_ função        sp_ procedimento  tg_ função gatilho
-- =============================================================


-- =============================================================
-- PARTE 1 — ESTRUTURAS DE CONTROLE
-- =============================================================

-- -------------------------------------------------------------
-- 1.1) IF / ELSIF / ELSE — uma decisão por faixa de valor.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_faixa_frete(p_total numeric)
RETURNS numeric
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    v_frete numeric;
BEGIN
    IF p_total >= 500 THEN
        v_frete := 0;
    ELSIF p_total >= 200 THEN
        v_frete := 15.00;
    ELSE
        v_frete := 30.00;
    END IF;

    RETURN v_frete;
END;
$$;

-- -------------------------------------------------------------
-- 1.2) CASE — o mesmo tipo de decisão, escrito como tabela.
--      Note o ELSE: sem ele, um valor fora das opções provoca
--      o erro "case not found".
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_situacao_estoque(p_estoque integer)
RETURNS text
LANGUAGE plpgsql
IMMUTABLE
AS $$
BEGIN
    RETURN CASE
               WHEN p_estoque = 0           THEN 'esgotado'
               WHEN p_estoque <= 5          THEN 'crítico'
               WHEN p_estoque <= 20         THEN 'normal'
               ELSE                              'folgado'
           END;
END;
$$;

-- -------------------------------------------------------------
-- 1.3) WHILE — repete enquanto a condição for verdadeira.
--      Devolve em quantas parcelas o total cabe, respeitando um
--      valor mínimo e o teto de 12 vezes.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_parcelas(
    p_total         numeric,
    p_valor_minimo  numeric DEFAULT 100
)
RETURNS integer
LANGUAGE plpgsql
IMMUTABLE
AS $$
DECLARE
    v_n integer := 12;
BEGIN
    WHILE v_n > 1 AND p_total / v_n < p_valor_minimo LOOP
        v_n := v_n - 1;
    END LOOP;

    RETURN v_n;
END;
$$;

-- -------------------------------------------------------------
-- 1.4) FOR com contador — repete um número conhecido de vezes.
--      RAISE NOTICE escreve na tela sem interromper a rotina.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_mostrar_parcelas(
    p_total numeric,
    p_vezes integer
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_valor numeric := round(p_total / p_vezes, 2);
BEGIN
    FOR v_i IN 1..p_vezes LOOP
        RAISE NOTICE 'Parcela %/% .......... R$ %', v_i, p_vezes, v_valor;
    END LOOP;
END;
$$;


-- =============================================================
-- PARTE 2 — TRATAMENTO DE EXCEÇÕES
-- =============================================================

-- -------------------------------------------------------------
-- 2.1) Capturar um erro previsto e devolver um valor de reserva.
--      Sem o bloco EXCEPTION, a divisão por zero abortaria o
--      comando inteiro.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_dividir_seguro(
    p_dividendo numeric,
    p_divisor   numeric
)
RETURNS numeric
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN round(p_dividendo / p_divisor, 2);
EXCEPTION
    WHEN division_by_zero THEN
        RAISE NOTICE 'Divisor igual a zero; devolvendo NULL.';
        RETURN NULL;
END;
$$;

-- -------------------------------------------------------------
-- 2.2) Traduzir o erro técnico do servidor para uma mensagem
--      que a pessoa do balcão entenda.
--      SQLSTATE guarda o código do erro; SQLERRM, o texto.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_cadastrar_cliente(
    p_nome   text,
    p_cidade text,
    p_email  text
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO cliente (nome, cidade, email)
    VALUES (p_nome, p_cidade, p_email);

    RAISE NOTICE 'Cliente % cadastrado.', p_nome;
EXCEPTION
    WHEN unique_violation THEN
        RAISE NOTICE 'O e-mail % já pertence a outro cliente.', p_email;
        RAISE NOTICE 'Código do erro: % — %', SQLSTATE, SQLERRM;
END;
$$;

-- -------------------------------------------------------------
-- 2.3) Emitir o próprio erro, com mensagem e dica.
--      A rotina para antes do UPDATE: nada é alterado.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_baixar_estoque(
    p_produto_id integer,
    p_quantidade integer
)
LANGUAGE plpgsql
AS $$
DECLARE
    v_estoque integer;
BEGIN
    SELECT estoque INTO v_estoque
      FROM produto
     WHERE id = p_produto_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Produto % não encontrado.', p_produto_id
            USING HINT = 'Confira a lista com SELECT id, nome FROM produto;';
    END IF;

    IF v_estoque < p_quantidade THEN
        RAISE EXCEPTION 'Estoque insuficiente: há % unidades e foram pedidas %.',
              v_estoque, p_quantidade
            USING HINT = 'Reponha o estoque ou reduza a quantidade.';
    END IF;

    UPDATE produto
       SET estoque = estoque - p_quantidade
     WHERE id = p_produto_id;

    RAISE NOTICE 'Baixa de % unidade(s) registrada no produto %.',
          p_quantidade, p_produto_id;
END;
$$;


-- =============================================================
-- PARTE 3 — CURSORES
-- =============================================================

-- -------------------------------------------------------------
-- 3.1) Cursor implícito — o FOR ... IN SELECT abre, percorre e
--      fecha o cursor sozinho. É a forma preferida no dia a dia.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_reajustar_precos(p_percentual numeric)
LANGUAGE plpgsql
AS $$
DECLARE
    v_linha record;
    v_novo  numeric;
    v_conta integer := 0;
BEGIN
    FOR v_linha IN
        SELECT id, nome, preco FROM produto ORDER BY id
    LOOP
        v_novo := round(v_linha.preco * (1 + p_percentual / 100), 2);

        UPDATE produto SET preco = v_novo WHERE id = v_linha.id;

        RAISE NOTICE '% : % -> %', v_linha.nome, v_linha.preco, v_novo;
        v_conta := v_conta + 1;
    END LOOP;

    RAISE NOTICE '% produto(s) reajustado(s).', v_conta;
END;
$$;

-- -------------------------------------------------------------
-- 3.2) Cursor explícito — as quatro etapas visíveis:
--      DECLARE, OPEN, FETCH (em laço) e CLOSE.
--      O cursor recebe parâmetro, como uma função.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_listar_estoque_baixo(p_limite integer DEFAULT 5)
LANGUAGE plpgsql
AS $$
DECLARE
    c_produto CURSOR (p_lim integer) FOR
        SELECT id, nome, estoque
          FROM produto
         WHERE estoque <= p_lim
         ORDER BY estoque, id;

    v_linha record;
    v_conta integer := 0;
BEGIN
    OPEN c_produto(p_limite);

    LOOP
        FETCH c_produto INTO v_linha;
        EXIT WHEN NOT FOUND;          -- acabaram as linhas

        RAISE NOTICE 'Produto % (%) com % unidade(s) — situação: %',
              v_linha.id, v_linha.nome, v_linha.estoque,
              fn_situacao_estoque(v_linha.estoque);

        v_conta := v_conta + 1;
    END LOOP;

    CLOSE c_produto;

    IF v_conta = 0 THEN
        RAISE NOTICE 'Nenhum produto com estoque igual ou abaixo de %.', p_limite;
    END IF;
END;
$$;

-- -------------------------------------------------------------
-- 3.3) O mesmo resultado SEM cursor, em um único comando.
--      Compare: quando o SQL de conjunto resolve, ele é mais
--      curto, mais rápido e mais fácil de manter.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_estoque_baixo(p_limite integer DEFAULT 5)
RETURNS TABLE (
    produto_id integer,
    nome       text,
    estoque    integer,
    situacao   text
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT pr.id, pr.nome, pr.estoque, fn_situacao_estoque(pr.estoque)
          FROM produto pr
         WHERE pr.estoque <= p_limite
         ORDER BY pr.estoque, pr.id;
END;
$$;

-- -------------------------------------------------------------
-- 3.4) Função auxiliar da Aula 05, reaproveitada pelo gatilho
--      da Parte 5: soma os itens de um pedido.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_total_pedido(p_pedido_id integer)
RETURNS numeric
LANGUAGE plpgsql
AS $$
DECLARE
    v_total numeric;
BEGIN
    SELECT COALESCE(SUM(quantidade * preco_unitario), 0)
      INTO v_total
      FROM item_pedido
     WHERE pedido_id = p_pedido_id;

    RETURN v_total;
END;
$$;
