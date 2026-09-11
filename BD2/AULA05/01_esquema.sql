-- =============================================================
-- Banco de Dados II — UNITINS | Prof. Alysson Martins Bruno
-- Aula 05 — Funções e procedimentos armazenados em PL/pgSQL
-- Arquivo: 01_esquema.sql
--
-- Cria o esquema mínimo da loja e insere dados de exemplo.
-- Executar no banco "loja":
--   podman exec -i bd2-pg psql -U postgres -d loja < 01_esquema.sql
-- =============================================================

-- Ordem inversa das dependências
DROP TABLE IF EXISTS item_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS cliente;

CREATE TABLE cliente (
    id     serial PRIMARY KEY,
    nome   text NOT NULL,
    cidade text NOT NULL
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
    situacao    text NOT NULL DEFAULT 'aberto'
);

CREATE TABLE item_pedido (
    pedido_id      integer NOT NULL REFERENCES pedido (id),
    produto_id     integer NOT NULL REFERENCES produto (id),
    quantidade     integer NOT NULL CHECK (quantidade > 0),
    preco_unitario numeric(10,2) NOT NULL CHECK (preco_unitario > 0),
    PRIMARY KEY (pedido_id, produto_id)
);

-- -------------------------------------------------------------
-- Dados de exemplo
-- -------------------------------------------------------------

INSERT INTO cliente (nome, cidade) VALUES
    ('Ana Souza',   'Palmas'),
    ('Bruno Lima',  'Porto Nacional'),
    ('Carla Dias',  'Paraíso do Tocantins');

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

-- Conferência rápida:
--   pedido 1 = 2 × 120,00 + 1 × 35,00 = 275,00
--   pedido 2 = 1 × 900,00             = 900,00
--   pedido 3 = 3 ×  80,00             = 240,00
