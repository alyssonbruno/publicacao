-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Aula 07 - Transações, ACID e confirmação em duas fases (2PC)
-- Arquivo: preparar.sql
--
-- Deixa o laboratório no ponto de partida, sempre igual:
--
--   banco           tabela `conta` com as quatro contas da cooperativa
--   agencia_palmas  tabela `conta` só com as contas de Palmas
--   agencia_gurupi  tabela `conta` só com as contas de Gurupi
--
-- É executado sozinho na primeira vez que o PostgreSQL sobe (o
-- compose.yaml o monta em /docker-entrypoint-initdb.d) e pode ser
-- executado de novo a qualquer momento, pelo terminal:
--
--   podman exec -i bd2-aula07-pg psql -U aluno -d banco < preparar.sql
--
-- Pode rodar quantas vezes quiser: ele encerra as sessões abertas,
-- desfaz transações preparadas esquecidas e recria as tabelas.
-- É por isso que cada atividade do laboratório é independente.
-- =============================================================

\set ON_ERROR_STOP on
\set QUIET on
SET client_min_messages TO warning;

-- 1. Os bancos das duas agências (usados na atividade de 2PC).
SELECT 'CREATE DATABASE agencia_palmas'
 WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'agencia_palmas') \gexec
SELECT 'CREATE DATABASE agencia_gurupi'
 WHERE NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'agencia_gurupi') \gexec

-- 2. Encerra as sessões que ficaram abertas em outro terminal ou no
--    pgAdmin. Uma transação esquecida seguraria as tabelas e o DROP
--    abaixo ficaria esperando para sempre.
\o /dev/null
SELECT pg_terminate_backend(pid, 5000)
  FROM pg_stat_activity
 WHERE datname IN ('banco', 'agencia_palmas', 'agencia_gurupi')
   AND pid <> pg_backend_pid()
   AND backend_type = 'client backend';
\o

-- 3. Banco da cooperativa: as quatro contas.
\c banco
SET client_min_messages TO warning;
SELECT format('ROLLBACK PREPARED %L', gid)
  FROM pg_prepared_xacts
 WHERE database = current_database() \gexec

DROP TABLE IF EXISTS conta;

CREATE TABLE conta (
    id      integer       PRIMARY KEY,
    titular varchar(40)   NOT NULL,
    cidade  varchar(30)   NOT NULL,
    saldo   numeric(10,2) NOT NULL CHECK (saldo >= 0)
);

INSERT INTO conta (id, titular, cidade, saldo) VALUES
    (1, 'Ana',   'Palmas',         1000.00),
    (2, 'Bruno', 'Palmas',          500.00),
    (3, 'Carla', 'Gurupi',          200.00),
    (4, 'Davi',  'Gurupi',           50.00);

-- 4. Agência de Palmas: um servidor "separado" com as contas de Palmas.
\c agencia_palmas
SET client_min_messages TO warning;
SELECT format('ROLLBACK PREPARED %L', gid)
  FROM pg_prepared_xacts
 WHERE database = current_database() \gexec

DROP TABLE IF EXISTS conta;

CREATE TABLE conta (
    id      integer       PRIMARY KEY,
    titular varchar(40)   NOT NULL,
    saldo   numeric(10,2) NOT NULL CHECK (saldo >= 0)
);

INSERT INTO conta (id, titular, saldo) VALUES
    (1, 'Ana',   1000.00),
    (2, 'Bruno',  500.00);

-- 5. Agência de Gurupi: o outro servidor, com as contas de Gurupi.
\c agencia_gurupi
SET client_min_messages TO warning;
SELECT format('ROLLBACK PREPARED %L', gid)
  FROM pg_prepared_xacts
 WHERE database = current_database() \gexec

DROP TABLE IF EXISTS conta;

CREATE TABLE conta (
    id      integer       PRIMARY KEY,
    titular varchar(40)   NOT NULL,
    saldo   numeric(10,2) NOT NULL CHECK (saldo >= 0)
);

INSERT INTO conta (id, titular, saldo) VALUES
    (3, 'Carla', 200.00),
    (4, 'Davi',   50.00);

\c banco
\echo
\echo 'Laboratório da Aula 07 pronto. Contas no banco "banco":'
SELECT id, titular, cidade, saldo FROM conta ORDER BY id;
