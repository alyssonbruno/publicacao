-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 04: Crédito Popular Araguaia (fintech)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   correntista   50 mil correntistas
--   proposta      400 mil propostas de crédito (01/01/2025 a 30/09/2026)
--   parcela       cerca de 770 mil parcelas das propostas aprovadas
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g04-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g04-pg psql -U aluno -d banco -f /a2/preparar.sql
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
DROP TABLE IF EXISTS parcela, proposta, correntista CASCADE;

-- 3. As tabelas.
CREATE TABLE correntista (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE proposta (
    id                integer        PRIMARY KEY,
    contrato          varchar(12)    NOT NULL,   -- agência + número: 0457-0123456
    correntista_id    integer        NOT NULL,
    avalista_id       integer,                   -- quem garante a dívida (15%)
    produto           varchar(12)    NOT NULL,   -- CONSIGNADO, PESSOAL, MICROCREDITO, CARTAO
    valor_solicitado  numeric(10,2)  NOT NULL,
    data_proposta     timestamp      NOT NULL,   -- 01/01/2025 a 30/09/2026
    situacao          varchar(15)    NOT NULL,
    score             smallint       NOT NULL    -- 0 a 1000
);

CREATE TABLE parcela (
    id           integer       PRIMARY KEY,
    proposta_id  integer       NOT NULL,
    numero       smallint      NOT NULL,
    vencimento   date          NOT NULL,
    valor        numeric(9,2)  NOT NULL,
    pago_em      date                        -- vazio: ainda não paga
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2604) \g /dev/null

INSERT INTO correntista (id, nome, cpf, cidade)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       (20000000000 + g::bigint * 104729)::text,
       CASE WHEN r3 < 0.40 THEN 'Palmas'
            WHEN r3 < 0.60 THEN 'Araguaína'
            WHEN r3 < 0.72 THEN 'Gurupi'
            WHEN r3 < 0.82 THEN 'Porto Nacional'
            WHEN r3 < 0.90 THEN 'Paraíso do Tocantins'
            WHEN r3 < 0.97 THEN 'Colinas do Tocantins'
            ELSE 'Dianópolis'
       END
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 50000) AS g) AS sorteio
 ORDER BY g;

-- As propostas entram em ordem de data. O contrato começa pelo código
-- da agência (500 agências: 0100, 0103, 0106, ..., 1597).
INSERT INTO proposta (id, contrato, correntista_id, avalista_id, produto,
                      valor_solicitado, data_proposta, situacao, score)
SELECT g,
       lpad((100 + floor(r_ag * 500)::int * 3)::text, 4, '0') || '-' || lpad(g::text, 7, '0'),
       1 + floor(r_cor * 50000)::int,
       CASE WHEN r_aval < 0.15 THEN 1 + floor(r_aval / 0.15 * 50000)::int END,
       CASE WHEN r_prod < 0.30 THEN 'CONSIGNADO'             -- 30%
            WHEN r_prod < 0.65 THEN 'PESSOAL'                -- 35%
            WHEN r_prod < 0.85 THEN 'MICROCREDITO'           -- 20%
            ELSE 'CARTAO'                                    -- 15%
       END,
       round((300 + r_valor * 29700)::numeric, 2),
       timestamp '2025-01-01 08:00'
         + ((g - 1) * 638 / 400000) * interval '1 day'
         + floor(r_hora * 720) * interval '1 minute',
       CASE WHEN r_sit < 0.550 THEN 'APROVADA'               -- 55,0%
            WHEN r_sit < 0.880 THEN 'RECUSADA'               -- 33,0%
            WHEN r_sit < 0.998 THEN 'CANCELADA'              -- 11,8%
            ELSE 'ANALISE_MANUAL'                            --  0,2%
       END,
       floor(r_score * 1001)::int
  FROM (SELECT g, random() AS r_ag, random() AS r_cor, random() AS r_aval,
               random() AS r_prod, random() AS r_valor, random() AS r_hora,
               random() AS r_sit, random() AS r_score
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

-- Parcelas: só das propostas aprovadas, de 1 a 6 por proposta, uma a
-- cada 30 dias. As que vencem até 30/09/2026 estão pagas, menos cerca
-- de 9%, que estão em atraso. As que vencem depois ainda não venceram.
INSERT INTO parcela (id, proposta_id, numero, vencimento, valor, pago_em)
SELECT p.id * 10 + k,
       p.id,
       k,
       p.data_proposta::date + 30 * k,
       round(p.valor_solicitado * 1.15 / (1 + p.id % 6), 2),
       CASE WHEN p.data_proposta::date + 30 * k > date '2026-09-30' THEN NULL
            WHEN (p.id * 7 + k * 13) % 100 < 9 THEN NULL
            ELSE p.data_proposta::date + 30 * k - (p.id % 5)
       END
  FROM proposta AS p
 CROSS JOIN LATERAL generate_series(1, 1 + p.id % 6) AS k
 WHERE p.situacao = 'APROVADA'
 ORDER BY p.id, k;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida). Não há nenhum índice auxiliar: só as chaves primárias
--    e o CPF, que é único.
ALTER TABLE proposta
    ADD CONSTRAINT proposta_correntista_fk FOREIGN KEY (correntista_id) REFERENCES correntista (id),
    ADD CONSTRAINT proposta_avalista_fk    FOREIGN KEY (avalista_id)    REFERENCES correntista (id);
ALTER TABLE parcela
    ADD CONSTRAINT parcela_proposta_fk FOREIGN KEY (proposta_id) REFERENCES proposta (id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) correntista, proposta, parcela;

\echo
\echo 'Caso do Grupo 04 pronto (Crédito Popular Araguaia). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('correntista', 'proposta', 'parcela')
 ORDER BY reltuples DESC;
