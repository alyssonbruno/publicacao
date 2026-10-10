-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 01: Clínica Vida Cerrado
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   paciente        40 mil pacientes
--   medico          240 médicos
--   atendimento     400 mil atendimentos (01/01/2025 a 30/09/2026)
--   exame_pedido    600 mil exames pedidos nos atendimentos
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g01-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g01-pg psql -U aluno -d banco -f /a2/preparar.sql
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
DROP TABLE IF EXISTS exame_pedido, atendimento, medico, paciente CASCADE;

-- 3. As tabelas.
CREATE TABLE paciente (
    id               integer      PRIMARY KEY,
    nome             varchar(60)  NOT NULL,
    cpf              char(11)     NOT NULL UNIQUE,
    data_nascimento  date         NOT NULL,
    cidade           varchar(25)  NOT NULL
);

CREATE TABLE medico (
    id             integer      PRIMARY KEY,
    nome           varchar(60)  NOT NULL,
    especialidade  varchar(30)  NOT NULL
);

CREATE TABLE atendimento (
    id              integer       PRIMARY KEY,
    protocolo       varchar(9)    NOT NULL,   -- impresso na guia, ex.: CV0123456
    paciente_id     integer       NOT NULL,
    responsavel_id  integer,                  -- acompanhante (menor de idade, idoso)
    medico_id       integer       NOT NULL,
    unidade         varchar(25)   NOT NULL,   -- seis unidades
    data_hora       timestamp     NOT NULL,   -- 01/01/2025 a 30/09/2026
    tipo            varchar(10)   NOT NULL,   -- CONSULTA, RETORNO, URGENCIA
    situacao        varchar(10)   NOT NULL,   -- REALIZADO, FALTOU, CANCELADO
    valor           numeric(8,2)  NOT NULL
);

CREATE TABLE exame_pedido (
    id              integer      PRIMARY KEY,
    atendimento_id  integer      NOT NULL,
    exame           varchar(30)  NOT NULL,
    urgente         boolean      NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2601) \g /dev/null

INSERT INTO paciente (id, nome, cpf, data_nascimento, cidade)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       (10000000000 + g::bigint * 104729)::text,
       date '1940-01-01' + floor(r3 * 30000)::int,
       CASE WHEN r4 < 0.45 THEN 'Palmas'
            WHEN r4 < 0.65 THEN 'Araguaína'
            WHEN r4 < 0.77 THEN 'Gurupi'
            WHEN r4 < 0.87 THEN 'Porto Nacional'
            ELSE 'Paraíso do Tocantins'
       END
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3, random() AS r4
          FROM generate_series(1, 40000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO medico (id, nome, especialidade)
SELECT g,
       'Dr(a). ' ||
       (ARRAY['Adriana','Bruno','Camila','Diego','Elisa','Fábio','Helena','Igor',
              'Jéssica','Leonardo','Mariana','Rafael'])[1 + floor(r1 * 12)::int]
       || ' ' ||
       (ARRAY['Araújo','Batista','Cardoso','Dias','Freitas','Moraes','Nunes',
              'Rocha','Teixeira','Vieira'])[1 + floor(r2 * 10)::int],
       (ARRAY['Clínica geral','Pediatria','Ginecologia','Cardiologia','Ortopedia',
              'Dermatologia','Endocrinologia','Oftalmologia'])[1 + floor(r3 * 8)::int]
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 240) AS g) AS sorteio
 ORDER BY g;

-- Os atendimentos entram em ordem de data, como num sistema real:
-- cerca de 627 por dia, das 7h às 18h.
INSERT INTO atendimento (id, protocolo, paciente_id, responsavel_id, medico_id,
                         unidade, data_hora, tipo, situacao, valor)
SELECT g,
       'CV' || lpad(((g::bigint * 104729) % 1000003)::text, 7, '0'),
       1 + floor(r_pac * 40000)::int,
       CASE WHEN r_resp < 0.12 THEN 1 + floor(r_resp / 0.12 * 40000)::int END,
       1 + floor(r_med * 240)::int,
       CASE WHEN r_uni < 0.30 THEN 'Palmas Centro'           -- 30%
            WHEN r_uni < 0.50 THEN 'Palmas Sul'              -- 20%
            WHEN r_uni < 0.68 THEN 'Araguaína'               -- 18%
            WHEN r_uni < 0.80 THEN 'Gurupi'                  -- 12%
            WHEN r_uni < 0.90 THEN 'Porto Nacional'          -- 10%
            ELSE 'Paraíso do Tocantins'                      -- 10%
       END,
       timestamp '2025-01-01 07:00'
         + ((g - 1) * 638 / 400000) * interval '1 day'
         + floor(r_hora * 660) * interval '1 minute',
       CASE WHEN r_tipo < 0.70 THEN 'CONSULTA'                -- 70%
            WHEN r_tipo < 0.90 THEN 'RETORNO'                 -- 20%
            ELSE 'URGENCIA'                                   -- 10%
       END,
       CASE WHEN r_sit < 0.80 THEN 'REALIZADO'                -- 80%
            WHEN r_sit < 0.90 THEN 'FALTOU'                   -- 10%
            ELSE 'CANCELADO'                                  -- 10%
       END,
       round((80 + r_valor * 920)::numeric, 2)
  FROM (SELECT g, random() AS r_pac, random() AS r_resp, random() AS r_med,
               random() AS r_uni, random() AS r_hora, random() AS r_tipo,
               random() AS r_sit, random() AS r_valor
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

-- Os exames são gravados depois, pelo laboratório: ficam espalhados
-- pela tabela, sem acompanhar a ordem dos atendimentos.
INSERT INTO exame_pedido (id, atendimento_id, exame, urgente)
SELECT g,
       1 + floor(r_at * 400000)::int,
       (ARRAY['Hemograma','Glicemia','Colesterol total','Urina tipo 1',
              'Raio X de tórax','Eletrocardiograma','TSH','Creatinina',
              'Ultrassom abdominal','Vitamina D'])[1 + floor(r_ex * 10)::int],
       r_urg < 0.10
  FROM (SELECT g, random() AS r_at, random() AS r_ex, random() AS r_urg
          FROM generate_series(1, 600000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e o único índice auxiliar que o sistema já tinha.
ALTER TABLE atendimento
    ADD CONSTRAINT atendimento_paciente_fk    FOREIGN KEY (paciente_id)    REFERENCES paciente (id),
    ADD CONSTRAINT atendimento_responsavel_fk FOREIGN KEY (responsavel_id) REFERENCES paciente (id),
    ADD CONSTRAINT atendimento_medico_fk      FOREIGN KEY (medico_id)      REFERENCES medico (id);
ALTER TABLE exame_pedido
    ADD CONSTRAINT exame_pedido_atendimento_fk FOREIGN KEY (atendimento_id) REFERENCES atendimento (id);

CREATE INDEX idx_atendimento_paciente ON atendimento (paciente_id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) paciente, medico, atendimento, exame_pedido;

\echo
\echo 'Caso do Grupo 01 pronto (Clínica Vida Cerrado). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('paciente', 'medico', 'atendimento', 'exame_pedido')
 ORDER BY reltuples DESC;
