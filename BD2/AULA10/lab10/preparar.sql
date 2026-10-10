-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Aula 10 - Índices, seletividade e diagnóstico de consultas
-- Arquivo: preparar.sql
--
-- Deixa o laboratório no ponto de partida, sempre igual:
--
--   movimento   500 mil movimentações da Cooperativa Cerrado
--               (dados fictícios), só com a chave primária
--
-- Execute no início de TODA atividade, pelo terminal:
--
--   podman exec -i bd2-aula10-pg psql -U aluno -d banco < preparar.sql
--
-- Pode rodar quantas vezes quiser: ele encerra as sessões abertas,
-- apaga a tabela (e com ela todos os índices que você criou) e a
-- recria do zero. É por isso que cada atividade é independente.
--
-- Os dados são gerados por sorteio, mas com semente fixa
-- (setseed): no PostgreSQL 17, toda execução produz exatamente as
-- mesmas 500 mil linhas.
-- =============================================================

\set ON_ERROR_STOP on
\set QUIET on
SET client_min_messages TO warning;

-- 1. Encerra as sessões que ficaram abertas em outro terminal. Uma
--    transação esquecida seguraria a tabela e o DROP abaixo ficaria
--    esperando para sempre.
\o /dev/null
SELECT pg_terminate_backend(pid, 5000)
  FROM pg_stat_activity
 WHERE datname = current_database()
   AND pid <> pg_backend_pid()
   AND backend_type = 'client backend';
\o

-- 2. Apaga o que as atividades criam. Apagar a tabela apaga também
--    todos os índices dela.
DROP TABLE IF EXISTS carga_sem_indice;
DROP TABLE IF EXISTS carga_com_indices;
DROP TABLE IF EXISTS movimento;

-- 3. A tabela de movimentações (PIX, cartão, boleto, saque e TED).
CREATE TABLE movimento (
    id         integer       PRIMARY KEY,
    protocolo  varchar(9)    NOT NULL,   -- código único, ex.: PX0007919
    conta_id   integer       NOT NULL,   -- 20 mil contas
    data_mov   date          NOT NULL,   -- 01/10/2024 a 30/09/2026
    tipo       varchar(8)    NOT NULL,   -- PIX, CARTAO, BOLETO, SAQUE, TED
    cidade     varchar(25)   NOT NULL,   -- oito cidades do Tocantins
    valor      numeric(10,2) NOT NULL    -- de R$ 10,00 a R$ 5.000,00
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
--    - as linhas entram em ordem de data, como num sistema real, em
--      que cada movimentação é gravada no dia em que acontece;
--    - o protocolo é único e não segue a ordem das linhas;
--    - conta, tipo, cidade e valor são sorteados, com as proporções
--      indicadas nos comentários.
SELECT setseed(0.2026) \g /dev/null

INSERT INTO movimento (id, protocolo, conta_id, data_mov, tipo, cidade, valor)
SELECT g,
       'PX' || lpad(((g::bigint * 7919) % 1000003)::text, 7, '0'),
       1 + floor(r_conta * 20000)::int,
       date '2024-10-01' + ((g - 1) * 730 / 500000),
       CASE WHEN r_tipo < 0.60 THEN 'PIX'       -- 60%
            WHEN r_tipo < 0.80 THEN 'CARTAO'    -- 20%
            WHEN r_tipo < 0.92 THEN 'BOLETO'    -- 12%
            WHEN r_tipo < 0.98 THEN 'SAQUE'     --  6%
            ELSE 'TED'                          --  2%
       END,
       CASE WHEN r_cidade < 0.40 THEN 'Palmas'                  -- 40%
            WHEN r_cidade < 0.60 THEN 'Araguaína'               -- 20%
            WHEN r_cidade < 0.72 THEN 'Gurupi'                  -- 12%
            WHEN r_cidade < 0.82 THEN 'Porto Nacional'          -- 10%
            WHEN r_cidade < 0.90 THEN 'Paraíso do Tocantins'    --  8%
            WHEN r_cidade < 0.95 THEN 'Colinas do Tocantins'    --  5%
            WHEN r_cidade < 0.98 THEN 'Guaraí'                  --  3%
            ELSE 'Dianópolis'                                   --  2%
       END,
       round((10 + r_valor * 4990)::numeric, 2)
  FROM (SELECT g,
               random() AS r_conta,
               random() AS r_tipo,
               random() AS r_cidade,
               random() AS r_valor
          FROM generate_series(1, 500000) AS g) AS sorteio
 ORDER BY g;

-- 5. Estatísticas atualizadas: o planejador decide com base nelas.
--    O VACUUM também marca as páginas como "todas visíveis", o que
--    permite a varredura só no índice (Index Only Scan).
VACUUM (ANALYZE) movimento;

\echo
\echo 'Laboratório da Aula 10 pronto. Tabela movimento:'
SELECT count(*)                                   AS linhas,
       pg_size_pretty(pg_relation_size('movimento')) AS tamanho,
       min(data_mov)                               AS primeira_data,
       max(data_mov)                               AS ultima_data
  FROM movimento;
