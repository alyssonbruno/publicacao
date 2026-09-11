-- =============================================================
-- Banco de Dados II — UNITINS | Prof. Alysson Martins Bruno
-- Aula 05 — Funções e procedimentos armazenados em PL/pgSQL
-- Arquivo: 02_rotinas.sql
--
-- Contém apenas as definições das rotinas construídas no
-- laboratório. As chamadas de teste estão no roteiro do
-- laboratório e no texto-base.
--
-- Executar no banco "loja":
--   podman exec -i bd2-pg psql -U postgres -d loja < 02_rotinas.sql
--
-- Convenção de nomes usada na disciplina:
--   p_  parâmetro recebido pela rotina
--   v_  variável declarada dentro da rotina
--   fn_ função        sp_ procedimento
-- =============================================================

-- -------------------------------------------------------------
-- 1) Função em LANGUAGE sql: o corpo é uma única consulta.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_desconto_sql(
    p_preco      numeric,
    p_percentual numeric
)
RETURNS numeric
LANGUAGE sql
IMMUTABLE
AS $$
    SELECT round(p_preco - p_preco * p_percentual / 100, 2);
$$;

-- -------------------------------------------------------------
-- 2) A mesma regra em PL/pgSQL, com variável e valor-padrão.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_desconto(
    p_preco      numeric,
    p_percentual numeric DEFAULT 10
)
RETURNS numeric
LANGUAGE plpgsql
AS $$
DECLARE
    v_valor numeric;
BEGIN
    v_valor := p_preco - p_preco * p_percentual / 100;
    RETURN round(v_valor, 2);
END;
$$;

-- -------------------------------------------------------------
-- 3) Função que consulta o banco e devolve um único valor.
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_total_pedido(
    p_pedido_id integer
)
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

-- -------------------------------------------------------------
-- 4) Função que devolve uma tabela (várias linhas e colunas).
--    Atenção: os nomes de RETURNS TABLE viram variáveis; por
--    isso todas as colunas do SELECT são qualificadas com "p.".
-- -------------------------------------------------------------
CREATE OR REPLACE FUNCTION fn_pedidos_do_cliente(
    p_cliente_id integer
)
RETURNS TABLE (
    pedido_id    integer,
    data_pedido  date,
    valor_total  numeric
)
LANGUAGE plpgsql
AS $$
BEGIN
    RETURN QUERY
        SELECT p.id, p.data_pedido, fn_total_pedido(p.id)
          FROM pedido p
         WHERE p.cliente_id = p_cliente_id
         ORDER BY p.id;
END;
$$;

-- -------------------------------------------------------------
-- 5) Procedimento: executa uma ação e confirma a transação.
--    RAISE EXCEPTION será estudado em detalhe na Aula 06.
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
    SELECT estoque
      INTO v_estoque
      FROM produto
     WHERE id = p_produto_id;

    IF v_estoque IS NULL THEN
        RAISE EXCEPTION 'Produto % não encontrado.', p_produto_id;
    END IF;

    IF v_estoque < p_quantidade THEN
        RAISE EXCEPTION 'Estoque insuficiente: há % unidades e foram pedidas %.',
            v_estoque, p_quantidade;
    END IF;

    UPDATE produto
       SET estoque = estoque - p_quantidade
     WHERE id = p_produto_id;

    COMMIT;
END;
$$;

-- -------------------------------------------------------------
-- 6) Desafio: procedimento que devolve valor por parâmetro INOUT.
-- -------------------------------------------------------------
CREATE OR REPLACE PROCEDURE sp_abrir_pedido(
    p_cliente_id     integer,
    INOUT p_pedido_id integer
)
LANGUAGE plpgsql
AS $$
BEGIN
    INSERT INTO pedido (cliente_id)
    VALUES (p_cliente_id)
    RETURNING id INTO p_pedido_id;
END;
$$;
