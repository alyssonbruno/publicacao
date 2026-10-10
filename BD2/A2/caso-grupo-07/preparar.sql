-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 07: Protege Tocantins Seguros (seguro de automóveis)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   segurado             50 mil segurados
--   veiculo              60 mil veículos
--   sinistro             400 mil sinistros (01/01/2025 a 30/09/2026)
--   andamento_sinistro   1 milhão de andamentos dos sinistros
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g07-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g07-pg psql -U aluno -d banco -f /a2/preparar.sql
--
-- Pode rodar quantas vezes quiser: ele encerra as sessões abertas,
-- desfaz os parâmetros alterados com ALTER SYSTEM, apaga as tabelas
-- (e com elas todos os índices e visões que o grupo criou) e as
-- recria do zero. Os dados são sorteados com semente fixa (setseed):
-- no PostgreSQL 17, toda execução produz exatamente as mesmas linhas.
--
-- Empresa, pessoas, placas e números são FICTÍCIOS.
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
DROP TABLE IF EXISTS andamento_sinistro, sinistro, veiculo, segurado CASCADE;

-- 3. As tabelas.
CREATE TABLE segurado (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE veiculo (
    id           integer      PRIMARY KEY,
    segurado_id  integer      NOT NULL,
    placa        char(7)      NOT NULL UNIQUE,   -- padrão Mercosul: QKT1A23
    modelo       varchar(30)  NOT NULL,
    ano          smallint     NOT NULL
);

CREATE TABLE sinistro (
    id               integer        PRIMARY KEY,
    boletim          varchar(11)    NOT NULL,   -- boletim de ocorrência: BO123456789
    veiculo_id       integer        NOT NULL,
    placa            varchar(7)     NOT NULL,   -- copiada do veículo na abertura
    segurado_id      integer        NOT NULL,
    data_ocorrencia  date           NOT NULL,   -- 01/01/2025 a 30/09/2026
    cidade           varchar(25)    NOT NULL,
    tipo             varchar(12)    NOT NULL,
    situacao         varchar(12)    NOT NULL,
    valor_estimado   numeric(10,2)  NOT NULL,
    valor_pago       numeric(10,2)  NOT NULL    -- 0 se não indenizado
);

CREATE TABLE andamento_sinistro (
    id           integer      PRIMARY KEY,
    sinistro_id  integer      NOT NULL,
    data_hora    timestamp    NOT NULL,
    etapa        varchar(25)  NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2607) \g /dev/null

INSERT INTO segurado (id, nome, cpf, cidade)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       (40000000000 + g::bigint * 104729)::text,
       CASE WHEN r3 < 0.45 THEN 'Palmas'
            WHEN r3 < 0.65 THEN 'Araguaína'
            WHEN r3 < 0.77 THEN 'Gurupi'
            WHEN r3 < 0.87 THEN 'Porto Nacional'
            ELSE 'Paraíso do Tocantins'
       END
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 50000) AS g) AS sorteio
 ORDER BY g;

-- A placa e o dono de cada veículo saem de fórmulas fixas, para que o
-- sinistro possa copiá-los sem consultar a tabela de veículos.
INSERT INTO veiculo (id, segurado_id, placa, modelo, ano)
SELECT g,
       1 + (g * 7919) % 50000,
       'Q' || chr(65 + g % 26) || chr(65 + (g / 26) % 26) || ((g / 676) % 10)::text
           || chr(65 + (g / 6760) % 26) || lpad(((g * 37) % 100)::text, 2, '0'),
       (ARRAY['Onix','HB20','Gol','Strada','Hilux','Corolla','Kwid','Toro',
              'Saveiro','Compass'])[1 + floor(r1 * 10)::int],
       2008 + floor(r2 * 19)::int
  FROM (SELECT g, random() AS r1, random() AS r2
          FROM generate_series(1, 60000) AS g) AS sorteio
 ORDER BY g;

-- Os sinistros entram em ordem de data de ocorrência.
INSERT INTO sinistro (id, boletim, veiculo_id, placa, segurado_id,
                      data_ocorrencia, cidade, tipo, situacao,
                      valor_estimado, valor_pago)
SELECT g,
       'BO' || lpad(((g::bigint * 7919) % 1000000007)::text, 9, '0'),
       v,
       'Q' || chr(65 + v % 26) || chr(65 + (v / 26) % 26) || ((v / 676) % 10)::text
           || chr(65 + (v / 6760) % 26) || lpad(((v * 37) % 100)::text, 2, '0'),
       1 + (v * 7919) % 50000,
       date '2025-01-01' + ((g - 1) * 638 / 400000),
       CASE WHEN r_cid < 0.40 THEN 'Palmas'                   -- 40%
            WHEN r_cid < 0.60 THEN 'Araguaína'                -- 20%
            WHEN r_cid < 0.72 THEN 'Gurupi'                   -- 12%
            WHEN r_cid < 0.82 THEN 'Porto Nacional'           -- 10%
            WHEN r_cid < 0.90 THEN 'Paraíso do Tocantins'     --  8%
            ELSE 'Colinas do Tocantins'                       -- 10%
       END,
       CASE WHEN r_tipo < 0.55 THEN 'COLISAO'                 -- 55%
            WHEN r_tipo < 0.75 THEN 'VIDROS'                  -- 20%
            WHEN r_tipo < 0.83 THEN 'ROUBO'                   --  8%
            WHEN r_tipo < 0.90 THEN 'FURTO'                   --  7%
            WHEN r_tipo < 0.92 THEN 'ALAGAMENTO'              --  2%
            WHEN r_tipo < 0.93 THEN 'INCENDIO'                --  1%
            ELSE 'OUTROS'                                     --  7%
       END,
       situacao,
       round((500 + r_valor * 49500)::numeric, 2),
       CASE WHEN situacao = 'INDENIZADO'
            THEN round(((500 + r_valor * 49500) * (0.6 + r_pago * 0.4))::numeric, 2)
            ELSE 0
       END
  FROM (SELECT g,
               1 + floor(r_vei * 60000)::int AS v,
               r_cid, r_tipo, r_valor, r_pago,
               CASE WHEN r_sit < 0.70 THEN 'INDENIZADO'       -- 70%
                    WHEN r_sit < 0.85 THEN 'NEGADO'           -- 15%
                    WHEN r_sit < 0.95 THEN 'EM_ANALISE'       -- 10%
                    ELSE 'EM_VISTORIA'                        --  5%
               END AS situacao
          FROM (SELECT g, random() AS r_vei, random() AS r_cid, random() AS r_tipo,
                       random() AS r_sit, random() AS r_valor, random() AS r_pago
                  FROM generate_series(1, 400000) AS g) AS s1) AS sorteio
 ORDER BY g;

-- Andamentos: dois ou três por sinistro, gravados à medida que o
-- processo anda.
INSERT INTO andamento_sinistro (id, sinistro_id, data_hora, etapa)
SELECT g,
       1 + (g - 1) * 2 / 5,
       timestamp '2025-01-01 08:00'
         + (((((g - 1) * 2 / 5) * 638 / 400000)) + (g - 1) % 5) * interval '1 day'
         + floor(r_hora * 600) * interval '1 minute',
       (ARRAY['Aviso recebido','Vistoria agendada','Vistoria realizada',
              'Documentação conferida','Parecer emitido'])[1 + floor(r_etapa * 5)::int]
  FROM (SELECT g, random() AS r_hora, random() AS r_etapa
          FROM generate_series(1, 1000000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e o índice auxiliar que o sistema já tinha.
ALTER TABLE veiculo
    ADD CONSTRAINT veiculo_segurado_fk FOREIGN KEY (segurado_id) REFERENCES segurado (id);
ALTER TABLE sinistro
    ADD CONSTRAINT sinistro_veiculo_fk  FOREIGN KEY (veiculo_id)  REFERENCES veiculo (id),
    ADD CONSTRAINT sinistro_segurado_fk FOREIGN KEY (segurado_id) REFERENCES segurado (id);
ALTER TABLE andamento_sinistro
    ADD CONSTRAINT andamento_sinistro_fk FOREIGN KEY (sinistro_id) REFERENCES sinistro (id);

CREATE INDEX idx_sinistro_segurado ON sinistro (segurado_id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) segurado, veiculo, sinistro, andamento_sinistro;

\echo
\echo 'Caso do Grupo 07 pronto (Protege Tocantins Seguros). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('segurado', 'veiculo', 'sinistro', 'andamento_sinistro')
 ORDER BY reltuples DESC;
