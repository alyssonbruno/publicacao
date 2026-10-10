-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 02: Mercado Norte (loja virtual)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   cliente       50 mil clientes
--   produto       8 mil produtos
--   pedido        400 mil pedidos (01/01/2025 a 30/09/2026)
--   item_pedido   1 milhão de itens dos pedidos
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g02-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g02-pg psql -U aluno -d banco -f /a2/preparar.sql
--
-- Pode rodar quantas vezes quiser: ele encerra as sessões abertas,
-- desfaz os parâmetros alterados com ALTER SYSTEM, apaga as tabelas
-- (e com elas todos os índices e visões que o grupo criou) e as
-- recria do zero. Os dados são sorteados com semente fixa (setseed):
-- no PostgreSQL 17, toda execução produz exatamente as mesmas linhas.
--
-- Empresa, pessoas e números são FICTÍCIOS.
-- =============================================================

\set ON_ERROR_STOP on
\set QUIET on
SET client_min_messages TO warning;

-- 1. Encerra as sessões abertas em outros terminais (uma transação
--    esquecida seguraria as tabelas e o DROP ficaria esperando) e
--    desfaz os ajustes feitos com ALTER SYSTEM em sessões anteriores.
\o /dev/null
SELECT pg_terminate_backend(pid, 5000)
  FROM pg_stat_activity
 WHERE datname = current_database()
   AND pid <> pg_backend_pid()
   AND backend_type = 'client backend';
ALTER SYSTEM RESET ALL;
SELECT pg_reload_conf();
\o

-- 2. Apaga as tabelas do caso. O CASCADE leva junto as visões e as
--    visões materializadas que o grupo tenha criado sobre elas.
DROP TABLE IF EXISTS item_pedido, pedido, produto, cliente CASCADE;

-- 3. As tabelas.
CREATE TABLE cliente (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    email   varchar(80)  NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE produto (
    id         integer       PRIMARY KEY,
    sku        varchar(10)   NOT NULL UNIQUE,
    nome       varchar(60)   NOT NULL,
    categoria  varchar(20)   NOT NULL,
    preco      numeric(8,2)  NOT NULL
);

CREATE TABLE pedido (
    id           integer        PRIMARY KEY,
    numero       varchar(9)     NOT NULL UNIQUE,  -- o cliente vê: MN0123456
    cliente_id   integer        NOT NULL,
    data_pedido  timestamp      NOT NULL,         -- 01/01/2025 a 30/09/2026
    situacao     varchar(20)    NOT NULL,
    cep_entrega  varchar(8)     NOT NULL,         -- só os dígitos: 77015202
    valor_total  numeric(10,2)  NOT NULL,
    observacao   text                             -- recado do cliente ao entregador
);

CREATE TABLE item_pedido (
    id              integer       PRIMARY KEY,
    pedido_id       integer       NOT NULL,
    produto_id      integer       NOT NULL,
    quantidade      smallint      NOT NULL,
    preco_unitario  numeric(8,2)  NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2602) \g /dev/null

INSERT INTO cliente (id, nome, email, cidade)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       'cliente' || g || '@exemplo.com.br',
       CASE WHEN r3 < 0.45 THEN 'Palmas'
            WHEN r3 < 0.65 THEN 'Araguaína'
            WHEN r3 < 0.77 THEN 'Gurupi'
            WHEN r3 < 0.87 THEN 'Porto Nacional'
            ELSE 'Paraíso do Tocantins'
       END
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 50000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO produto (id, sku, nome, categoria, preco)
SELECT g,
       'SKU' || lpad(g::text, 6, '0'),
       (ARRAY['Fone','Carregador','Capinha','Camiseta','Tênis','Panela','Garrafa',
              'Mochila','Livro','Caderno','Lâmpada','Ventilador'])[1 + floor(r1 * 12)::int]
       || ' modelo ' || g,
       (ARRAY['Eletrônicos','Moda','Casa','Papelaria','Esporte'])[1 + floor(r2 * 5)::int],
       round((5 + r3 * 495)::numeric, 2)
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 8000) AS g) AS sorteio
 ORDER BY g;

-- Os pedidos entram em ordem de data, como num sistema real.
INSERT INTO pedido (id, numero, cliente_id, data_pedido, situacao,
                    cep_entrega, valor_total, observacao)
SELECT g,
       'MN' || lpad(((g::bigint * 7919) % 1000003)::text, 7, '0'),
       1 + floor(r_cli * 50000)::int,
       timestamp '2025-01-01'
         + ((g - 1) * 638 / 400000) * interval '1 day'
         + floor(r_hora * 1440) * interval '1 minute',
       CASE WHEN r_sit < 0.720 THEN 'ENTREGUE'               -- 72,0%
            WHEN r_sit < 0.810 THEN 'CANCELADO'              --  9,0%
            WHEN r_sit < 0.890 THEN 'ENVIADO'                --  8,0%
            WHEN r_sit < 0.950 THEN 'PAGO'                   --  6,0%
            WHEN r_sit < 0.9985 THEN 'AGUARDANDO_PAGAMENTO'  --  4,85%
            ELSE 'EM_DISPUTA'                                --  0,15%
       END,
       '77' || lpad(floor(r_cep1 * 1000)::text, 3, '0')
            || lpad(floor(r_cep2 * 1000)::text, 3, '0'),
       round((20 + r_valor * 980)::numeric, 2),
       CASE WHEN r_obs < 0.004 THEN 'Embalagem violada na entrega, conferir o conteúdo'
            WHEN r_obs < 0.350 THEN
                 (ARRAY['Entregar na portaria','Deixar com o vizinho da frente',
                        'Ligar antes de entregar','Embalagem para presente, por favor',
                        'Portão azul, casa dos fundos','Entregar depois das 18h',
                        'Não tocar a campainha, tem bebê dormindo',
                        'Nota fiscal no nome da empresa',
                        'Apartamento no 3º andar, sem elevador',
                        'Entregar no comércio ao lado'])[1 + floor(r_obs2 * 10)::int]
       END
  FROM (SELECT g, random() AS r_cli, random() AS r_hora, random() AS r_sit,
               random() AS r_cep1, random() AS r_cep2, random() AS r_valor,
               random() AS r_obs, random() AS r_obs2
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

-- Itens: dois ou três por pedido, gravados junto com o pedido.
INSERT INTO item_pedido (id, pedido_id, produto_id, quantidade, preco_unitario)
SELECT g,
       1 + (g - 1) * 2 / 5,
       1 + floor(r_prod * 8000)::int,
       1 + floor(r_qtd * 4)::int,
       round((5 + r_preco * 495)::numeric, 2)
  FROM (SELECT g, random() AS r_prod, random() AS r_qtd, random() AS r_preco
          FROM generate_series(1, 1000000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e o único índice auxiliar que o sistema já tinha.
ALTER TABLE pedido
    ADD CONSTRAINT pedido_cliente_fk FOREIGN KEY (cliente_id) REFERENCES cliente (id);
ALTER TABLE item_pedido
    ADD CONSTRAINT item_pedido_pedido_fk  FOREIGN KEY (pedido_id)  REFERENCES pedido (id),
    ADD CONSTRAINT item_pedido_produto_fk FOREIGN KEY (produto_id) REFERENCES produto (id);

CREATE INDEX idx_pedido_cep ON pedido (cep_entrega);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) cliente, produto, pedido, item_pedido;

\echo
\echo 'Caso do Grupo 02 pronto (Mercado Norte). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('cliente', 'produto', 'pedido', 'item_pedido')
 ORDER BY reltuples DESC;
