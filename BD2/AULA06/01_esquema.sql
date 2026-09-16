-- =============================================================
-- Banco de Dados II — UNITINS | Prof. Alysson Martins Bruno
-- Aula 06 — Gatilhos, cursores, controle e exceções
-- Arquivo: 01_esquema.sql
--
-- Recria o esquema da loja usado desde a Aula 05 e acrescenta
-- o que a Aula 06 precisa:
--
--   - coluna  cliente.email       (UNIQUE) — para estudar a
--             exceção unique_violation;
--   - coluna  pedido.total        — valor derivado mantido por
--             gatilho, em vez de calculado a cada consulta;
--   - tabela  auditoria_produto   — trilha de auditoria gravada
--             por gatilho.
--
-- Executar no banco "loja":
--   podman exec -i bd2-pg psql -U postgres -d loja < 01_esquema.sql
--
-- O script pode ser executado quantas vezes for preciso: ele
-- apaga as tabelas antes de criá-las de novo.
-- =============================================================

DROP TABLE IF EXISTS auditoria_produto CASCADE;
DROP TABLE IF EXISTS item_pedido CASCADE;
DROP TABLE IF EXISTS pedido CASCADE;
DROP TABLE IF EXISTS produto CASCADE;
DROP TABLE IF EXISTS cliente CASCADE;

-- -------------------------------------------------------------
-- Tabelas da loja
-- -------------------------------------------------------------
CREATE TABLE cliente (
    id     serial PRIMARY KEY,
    nome   text NOT NULL,
    cidade text NOT NULL,
    email  text UNIQUE                      -- novo na Aula 06
);

CREATE TABLE produto (
    id      serial PRIMARY KEY,
    nome    text NOT NULL,
    preco   numeric(10,2) NOT NULL CHECK (preco > 0),
    estoque integer NOT NULL DEFAULT 0 CHECK (estoque >= 0)
);

CREATE TABLE pedido (
    id          serial PRIMARY KEY,
    cliente_id  integer NOT NULL REFERENCES cliente (id),
    data_pedido date NOT NULL DEFAULT CURRENT_DATE,
    situacao    text NOT NULL DEFAULT 'aberto',
    total       numeric(10,2) NOT NULL DEFAULT 0   -- novo na Aula 06
);

CREATE TABLE item_pedido (
    pedido_id      integer NOT NULL REFERENCES pedido (id) ON DELETE CASCADE,
    produto_id     integer NOT NULL REFERENCES produto (id),
    quantidade     integer NOT NULL CHECK (quantidade > 0),
    preco_unitario numeric(10,2) NOT NULL CHECK (preco_unitario > 0),
    PRIMARY KEY (pedido_id, produto_id)
);

-- -------------------------------------------------------------
-- Trilha de auditoria: quem mudou o quê, quando e de que valor
-- para que valor. Quem grava aqui é um gatilho, nunca a aplicação.
-- -------------------------------------------------------------
CREATE TABLE auditoria_produto (
    id           bigserial PRIMARY KEY,
    produto_id   integer NOT NULL,
    operacao     text NOT NULL,
    campo        text NOT NULL,
    valor_antigo text,
    valor_novo   text,
    usuario      text NOT NULL DEFAULT current_user,
    momento      timestamptz NOT NULL DEFAULT now()
);

-- -------------------------------------------------------------
-- Dados de exemplo
-- -------------------------------------------------------------
INSERT INTO cliente (nome, cidade, email) VALUES
    ('Ana Souza',  'Palmas',               'ana@exemplo.com'),
    ('Bruno Lima', 'Porto Nacional',       'bruno@exemplo.com'),
    ('Carla Dias', 'Paraíso do Tocantins', 'carla@exemplo.com');

INSERT INTO produto (nome, preco, estoque) VALUES
    ('Teclado',   120.00, 10),
    ('Mouse',      80.00, 15),
    ('Monitor',   900.00,  4),
    ('Cabo HDMI',  35.00, 25);

INSERT INTO pedido (cliente_id) VALUES (1), (1), (2);

INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES
    (1, 1, 2, 120.00),
    (1, 4, 1,  35.00),
    (2, 3, 1, 900.00),
    (3, 2, 3,  80.00);

-- A coluna pedido.total ainda está zerada de propósito: ela passa
-- a ser preenchida sozinha depois que o gatilho da Parte 5 existir.
