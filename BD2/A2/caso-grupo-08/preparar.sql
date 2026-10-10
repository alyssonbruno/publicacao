-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 08: Bilhete Cerrado (transporte coletivo)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   cartao       80 mil cartões de passagem
--   linha        60 linhas de ônibus
--   validacao    400 mil passagens validadas na catraca (01/01 a 30/09/2026)
--   reclamacao   200 mil reclamações de passageiros (mesmo período)
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g08-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g08-pg psql -U aluno -d banco -f /a2/preparar.sql
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
DROP TABLE IF EXISTS reclamacao, validacao, linha, cartao CASCADE;

-- 3. As tabelas.
CREATE TABLE cartao (
    id       integer      PRIMARY KEY,
    numero   char(16)     NOT NULL UNIQUE,   -- impresso no cartão
    tipo     varchar(10)  NOT NULL,          -- COMUM, ESTUDANTE, IDOSO, PCD
    titular  varchar(60)  NOT NULL
);

CREATE TABLE linha (
    id      integer      PRIMARY KEY,
    codigo  varchar(5)   NOT NULL UNIQUE,    -- ex.: L023
    nome    varchar(60)  NOT NULL
);

-- ATENÇÃO: o validador grava data_hora no horário UNIVERSAL (UTC), três
-- horas à frente do horário de Palmas. Uma passagem paga às 7h da manhã
-- aparece como 10h.
CREATE TABLE validacao (
    id            integer       PRIMARY KEY,
    cartao_id     integer       NOT NULL,
    linha_id      integer       NOT NULL,
    veiculo_id    integer       NOT NULL,   -- número do ônibus (1 a 800)
    motorista_id  integer       NOT NULL,   -- matrícula do motorista (1 a 3.000)
    data_hora     timestamp     NOT NULL,   -- em UTC: 01/01/2026 a 30/09/2026
    tarifa        numeric(5,2)  NOT NULL,   -- 4,50 inteira, 2,25 meia, 0 gratuidade
    situacao      varchar(10)   NOT NULL    -- OK ou SUSPEITA (possível cartão clonado)
);

CREATE TABLE reclamacao (
    id             integer      PRIMARY KEY,
    linha_id       integer      NOT NULL,
    registrada_em  timestamp    NOT NULL,
    canal          varchar(10)  NOT NULL,   -- APLICATIVO, TELEFONE, SITE
    texto          text         NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2608) \g /dev/null

INSERT INTO cartao (id, numero, tipo, titular)
SELECT g,
       '6037' || lpad(((g::bigint * 104729) % 1000000000007)::text, 12, '0'),
       CASE WHEN r1 < 0.70 THEN 'COMUM'
            WHEN r1 < 0.90 THEN 'ESTUDANTE'
            WHEN r1 < 0.98 THEN 'IDOSO'
            ELSE 'PCD'
       END,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r2 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r3 * 16)::int]
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 80000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO linha (id, codigo, nome)
SELECT g,
       'L' || lpad(g::text, 3, '0'),
       'Linha ' || lpad(g::text, 3, '0') || ' - '
       || (ARRAY['Centro','Taquaralto','Aureny','Plano Diretor Norte','Plano Diretor Sul',
                 'Universidade','Rodoviária','Aeroporto','Praia','Distrito Industrial'])[1 + (g - 1) % 10]
  FROM generate_series(1, 60) AS g;

-- As validações entram em ordem de horário. As cinco linhas troncais
-- (1 a 5) levam 6% das passagens cada uma; as outras 55 dividem o resto.
INSERT INTO validacao (id, cartao_id, linha_id, veiculo_id, motorista_id,
                       data_hora, tarifa, situacao)
SELECT g,
       1 + floor(r_car * 80000)::int,
       CASE WHEN r_lin < 0.30 THEN 1 + floor(r_lin / 0.06)::int
            ELSE 6 + floor((r_lin - 0.30) / 0.70 * 55)::int
       END,
       1 + floor(r_vei * 800)::int,
       1 + floor(r_mot * 3000)::int,
       timestamp '2026-01-01 08:00'
         + ((g - 1) * 273 / 400000) * interval '1 day'
         + floor(r_hora * 1080) * interval '1 minute',
       CASE WHEN r_tar < 0.70 THEN 4.50
            WHEN r_tar < 0.90 THEN 2.25
            ELSE 0
       END,
       CASE WHEN r_sit < 0.998 THEN 'OK' ELSE 'SUSPEITA' END    -- 0,2% suspeitas
  FROM (SELECT g, random() AS r_car, random() AS r_lin, random() AS r_vei,
               random() AS r_mot, random() AS r_hora, random() AS r_tar,
               random() AS r_sit
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO reclamacao (id, linha_id, registrada_em, canal, texto)
SELECT g,
       1 + floor(r_lin * 60)::int,
       timestamp '2026-01-01 09:00'
         + ((g - 1) * 273 / 200000) * interval '1 day'
         + floor(r_hora * 840) * interval '1 minute',
       (ARRAY['APLICATIVO','TELEFONE','SITE'])[1 + floor(r_can * 3)::int],
       CASE WHEN r_par < 0.005 THEN 'O motorista não parou no ponto e eu perdi a hora. '
            ELSE ''
       END
       || (ARRAY['Ônibus lotado no horário de pico.','O ar-condicionado estava desligado.',
                 'Atraso de mais de 30 minutos.','O validador não leu o cartão.',
                 'Ônibus sujo.','O aplicativo mostrou o horário errado.',
                 'Motorista educado e atencioso, parabéns.','Ponto de ônibus sem cobertura.',
                 'A linha mudou o trajeto sem aviso.','Cobrança em dobro no cartão.'])[1 + floor(r_t1 * 10)::int]
  FROM (SELECT g, random() AS r_lin, random() AS r_hora, random() AS r_can,
               random() AS r_par, random() AS r_t1
          FROM generate_series(1, 200000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e o índice auxiliar que o sistema já tinha.
ALTER TABLE validacao
    ADD CONSTRAINT validacao_cartao_fk FOREIGN KEY (cartao_id) REFERENCES cartao (id),
    ADD CONSTRAINT validacao_linha_fk  FOREIGN KEY (linha_id)  REFERENCES linha (id);
ALTER TABLE reclamacao
    ADD CONSTRAINT reclamacao_linha_fk FOREIGN KEY (linha_id) REFERENCES linha (id);

CREATE INDEX idx_validacao_linha ON validacao (linha_id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) cartao, linha, validacao, reclamacao;

\echo
\echo 'Caso do Grupo 08 pronto (Bilhete Cerrado). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('cartao', 'linha', 'validacao', 'reclamacao')
 ORDER BY reltuples DESC;
