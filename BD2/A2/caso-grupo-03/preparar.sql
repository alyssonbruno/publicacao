-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 03: Entrega Já Tocantins (logística)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   remetente         20 mil empresas que enviam encomendas
--   entrega           400 mil entregas (postadas de 01/01/2025 a 30/09/2026)
--   evento_rastreio   1,2 milhão de eventos de rastreio (3 por entrega)
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g03-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g03-pg psql -U aluno -d banco -f /a2/preparar.sql
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
DROP TABLE IF EXISTS evento_rastreio, entrega, remetente CASCADE;

-- 3. As tabelas.
CREATE TABLE remetente (
    id            integer      PRIMARY KEY,
    razao_social  varchar(60)  NOT NULL,
    cnpj          char(14)     NOT NULL UNIQUE,
    cidade        varchar(25)  NOT NULL
);

CREATE TABLE entrega (
    id               integer       PRIMARY KEY,
    codigo_rastreio  varchar(13)   NOT NULL,   -- ex.: TO123456789BR
    remetente_id     integer       NOT NULL,
    entregador_id    integer       NOT NULL,   -- matrícula do entregador (1 a 500)
    cidade_destino   varchar(25)   NOT NULL,   -- doze cidades do Tocantins
    cep_destino      varchar(8)    NOT NULL,
    endereco         varchar(80)   NOT NULL,
    data_postagem    date          NOT NULL,   -- 01/01/2025 a 30/09/2026
    prazo            date          NOT NULL,   -- data prometida ao cliente
    entregue_em      date,                     -- vazio se ainda não entregue
    situacao         varchar(20)   NOT NULL,
    peso_kg          numeric(6,2)  NOT NULL,
    valor_frete      numeric(8,2)  NOT NULL
);

CREATE TABLE evento_rastreio (
    id           integer      PRIMARY KEY,
    entrega_id   integer      NOT NULL,
    data_hora    timestamp    NOT NULL,
    tipo_evento  varchar(20)  NOT NULL,
    local        varchar(25)  NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2603) \g /dev/null

INSERT INTO remetente (id, razao_social, cnpj, cidade)
SELECT g,
       (ARRAY['Comercial','Distribuidora','Atacadão','Loja','Farmácia','Papelaria',
              'Eletro','Casa'])[1 + floor(r1 * 8)::int]
       || ' ' ||
       (ARRAY['Cerrado','Tocantins','Araguaia','Jalapão','Capim Dourado','Serra',
              'Buriti','Ipê'])[1 + floor(r2 * 8)::int]
       || ' ' || g,
       (10000000000000 + g::bigint * 7919)::text,
       CASE WHEN r3 < 0.50 THEN 'Palmas'
            WHEN r3 < 0.75 THEN 'Araguaína'
            ELSE 'Gurupi'
       END
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 20000) AS g) AS sorteio
 ORDER BY g;

-- As entregas entram em ordem de postagem. Dez grandes remetentes
-- (ids 1 a 10) respondem por 30% das entregas; os outros 19.990
-- dividem o resto.
INSERT INTO entrega (id, codigo_rastreio, remetente_id, entregador_id,
                     cidade_destino, cep_destino, endereco, data_postagem,
                     prazo, entregue_em, situacao, peso_kg, valor_frete)
SELECT g,
       'TO' || lpad(((g::bigint * 15485863) % 1000000007)::text, 9, '0') || 'BR',
       CASE WHEN r_rem < 0.30 THEN 1 + floor(r_rem / 0.03)::int
            ELSE 11 + floor(r_rem2 * 19990)::int
       END,
       1 + floor(r_ent * 500)::int,
       CASE WHEN r_cid < 0.35 THEN 'Palmas'                   -- 35%
            WHEN r_cid < 0.53 THEN 'Araguaína'                -- 18%
            WHEN r_cid < 0.63 THEN 'Gurupi'                   -- 10%
            WHEN r_cid < 0.71 THEN 'Porto Nacional'           --  8%
            WHEN r_cid < 0.77 THEN 'Paraíso do Tocantins'     --  6%
            WHEN r_cid < 0.82 THEN 'Colinas do Tocantins'     --  5%
            WHEN r_cid < 0.86 THEN 'Guaraí'                   --  4%
            WHEN r_cid < 0.89 THEN 'Tocantinópolis'           --  3%
            WHEN r_cid < 0.92 THEN 'Miracema do Tocantins'    --  3%
            WHEN r_cid < 0.95 THEN 'Formoso do Araguaia'      --  3%
            WHEN r_cid < 0.98 THEN 'Augustinópolis'           --  3%
            ELSE 'Dianópolis'                                 --  2%
       END,
       '77' || lpad(floor(r_cep * 1000000)::text, 6, '0'),
       (ARRAY['Rua','Avenida','Quadra','Alameda'])[1 + floor(r_end * 4)::int]
         || ' ' || (1 + floor(r_end * 900)::int) || ', número '
         || (1 + floor(r_peso * 2000)::int),
       data_postagem,
       data_postagem + 2 + floor(r_prazo * 7)::int,
       CASE WHEN r_sit < 0.82
            THEN least(data_postagem + 1 + floor(r_dias * 10)::int, date '2026-09-30')
       END,
       CASE WHEN r_sit < 0.82 THEN 'ENTREGUE'                 -- 82%
            WHEN r_sit < 0.89 THEN 'EM_ROTA'                  --  7%
            WHEN r_sit < 0.94 THEN 'AGUARDANDO_RETIRADA'      --  5%
            WHEN r_sit < 0.99 THEN 'DEVOLVIDA'                --  5%
            ELSE 'EXTRAVIADA'                                 --  1%
       END,
       round((0.1 + r_peso * 29.9)::numeric, 2),
       round((12 + r_peso * 150 + r_frete * 20)::numeric, 2)
  FROM (SELECT g,
               date '2025-01-01' + ((g - 1) * 638 / 400000) AS data_postagem,
               random() AS r_rem, random() AS r_rem2, random() AS r_cid,
               random() AS r_cep, random() AS r_end, random() AS r_prazo,
               random() AS r_sit, random() AS r_dias, random() AS r_peso,
               random() AS r_frete, random() AS r_ent
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

-- Três eventos por entrega, gravados à medida que a encomenda anda.
INSERT INTO evento_rastreio (id, entrega_id, data_hora, tipo_evento, local)
SELECT g,
       1 + (g - 1) / 3,
       timestamp '2025-01-01 08:00'
         + ((((g - 1) / 3) * 638 / 400000) + (g - 1) % 3) * interval '1 day'
         + floor(r_hora * 600) * interval '1 minute',
       (ARRAY['POSTADO','EM_TRANSITO','SAIU_PARA_ENTREGA'])[1 + (g - 1) % 3],
       (ARRAY['Palmas','Araguaína','Gurupi','Porto Nacional',
              'Paraíso do Tocantins'])[1 + floor(r_local * 5)::int]
  FROM (SELECT g, random() AS r_hora, random() AS r_local
          FROM generate_series(1, 1200000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e os índices auxiliares que o sistema já tinha.
ALTER TABLE entrega
    ADD CONSTRAINT entrega_remetente_fk FOREIGN KEY (remetente_id) REFERENCES remetente (id);
ALTER TABLE evento_rastreio
    ADD CONSTRAINT evento_rastreio_entrega_fk FOREIGN KEY (entrega_id) REFERENCES entrega (id);

CREATE INDEX idx_entrega_remetente           ON entrega (remetente_id);
CREATE INDEX idx_entrega_postagem_entregador ON entrega (data_postagem, entregador_id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) remetente, entrega, evento_rastreio;

\echo
\echo 'Caso do Grupo 03 pronto (Entrega Já Tocantins). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('remetente', 'entrega', 'evento_rastreio')
 ORDER BY reltuples DESC;
