-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 06: Prefeitura de Serra Azul do Tocantins
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   cidadao     50 mil cidadãos cadastrados
--   servidor    2 mil servidores municipais
--   protocolo   400 mil protocolos de atendimento (01/01/2025 a 30/09/2026)
--   andamento   1,2 milhão de andamentos (3 por protocolo)
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g06-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g06-pg psql -U aluno -d banco -f /a2/preparar.sql
--
-- Pode rodar quantas vezes quiser: ele encerra as sessões abertas,
-- desfaz os parâmetros alterados com ALTER SYSTEM, apaga as tabelas
-- (e com elas todos os índices e visões que o grupo criou) e as
-- recria do zero. Os dados são sorteados com semente fixa (setseed):
-- no PostgreSQL 17, toda execução produz exatamente as mesmas linhas.
--
-- Município, pessoas e números são FICTÍCIOS.
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
DROP TABLE IF EXISTS andamento, protocolo, servidor, cidadao CASCADE;

-- 3. As tabelas.
CREATE TABLE cidadao (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    bairro  varchar(30)  NOT NULL
);

CREATE TABLE servidor (
    id          integer      PRIMARY KEY,
    nome        varchar(60)  NOT NULL,
    secretaria  varchar(20)  NOT NULL
);

CREATE TABLE protocolo (
    id              integer       PRIMARY KEY,
    numero          varchar(11)   NOT NULL UNIQUE,  -- ano/número: 2026/271828
    cidadao_id      integer       NOT NULL,
    servico         varchar(25)   NOT NULL,         -- 18 serviços
    secretaria      varchar(20)   NOT NULL,         -- 8 secretarias
    bairro          varchar(30)   NOT NULL,
    aberto_em       timestamp     NOT NULL,         -- 01/01/2025 a 30/09/2026
    concluido_em    timestamp,                      -- vazio se ainda não concluído
    situacao        varchar(15)   NOT NULL,
    aberto_por      integer       NOT NULL,         -- servidor que registrou
    responsavel_id  integer       NOT NULL,         -- servidor que atende
    taxa            numeric(8,2)  NOT NULL          -- taxa paga (0 se gratuito)
);

CREATE TABLE andamento (
    id            integer      PRIMARY KEY,
    protocolo_id  integer      NOT NULL,
    data_hora     timestamp    NOT NULL,
    setor         varchar(30)  NOT NULL,
    descricao     varchar(80)  NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2606) \g /dev/null

INSERT INTO cidadao (id, nome, cpf, bairro)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       (30000000000 + g::bigint * 104729)::text,
       'Setor ' || (ARRAY['Central','Norte','Sul','Leste','Oeste','Aeroporto',
                          'Universitário','Industrial','Bela Vista','Morada do Sol'])[1 + floor(r3 * 10)::int]
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 50000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO servidor (id, nome, secretaria)
SELECT g,
       'Servidor(a) ' || g,
       (ARRAY['OBRAS','MEIO_AMBIENTE','FAZENDA','SAUDE','EDUCACAO',
              'SERVICOS_URBANOS','CULTURA','OUVIDORIA'])[1 + (g - 1) % 8]
  FROM generate_series(1, 2000) AS g;

-- Os protocolos entram em ordem de abertura. O sorteio do serviço segue
-- a procura real de cada um (de 0,5% a 15%); a secretaria vem do serviço.
INSERT INTO protocolo (id, numero, cidadao_id, servico, secretaria, bairro,
                       aberto_em, concluido_em, situacao, aberto_por,
                       responsavel_id, taxa)
SELECT g,
       to_char(aberto_em, 'YYYY') || '/' || lpad(g::text, 6, '0'),
       cidadao_id,
       servico,
       CASE WHEN servico IN ('TAPA_BURACO','CALCADA','ALVARA_CONSTRUCAO') THEN 'OBRAS'
            WHEN servico IN ('PODA_ARVORE','COLETA_ENTULHO','LICENCA_AMBIENTAL') THEN 'MEIO_AMBIENTE'
            WHEN servico IN ('IPTU_SEGUNDA_VIA','CERTIDAO_NEGATIVA','REVISAO_IPTU') THEN 'FAZENDA'
            WHEN servico IN ('AGENDAMENTO_CONSULTA','VIGILANCIA_SANITARIA') THEN 'SAUDE'
            WHEN servico IN ('MATRICULA_ESCOLAR','TRANSPORTE_ESCOLAR') THEN 'EDUCACAO'
            WHEN servico IN ('ILUMINACAO_PUBLICA','LIMPEZA_TERRENO') THEN 'SERVICOS_URBANOS'
            WHEN servico IN ('USO_ESPACO_PUBLICO','ALVARA_EVENTO') THEN 'CULTURA'
            ELSE 'OUVIDORIA'
       END,
       bairro,
       aberto_em,
       CASE WHEN situacao IN ('CONCLUIDO','INDEFERIDO')
            THEN aberto_em + floor(r_dur * r_dur * 90 * 24 * 60) * interval '1 minute'
       END,
       situacao,
       aberto_por,
       responsavel_id,
       CASE servico
            WHEN 'ALVARA_CONSTRUCAO'  THEN round((150 + r_taxa * 750)::numeric, 2)
            WHEN 'LICENCA_AMBIENTAL'  THEN round((200 + r_taxa * 1000)::numeric, 2)
            WHEN 'CERTIDAO_NEGATIVA'  THEN 15.00
            WHEN 'USO_ESPACO_PUBLICO' THEN round((80 + r_taxa * 320)::numeric, 2)
            WHEN 'ALVARA_EVENTO'      THEN round((120 + r_taxa * 480)::numeric, 2)
            ELSE 0
       END
  FROM (SELECT g,
               1 + floor(r_cid * 50000)::int AS cidadao_id,
               CASE WHEN r_serv < 0.10  THEN 'TAPA_BURACO'            -- 10%
                    WHEN r_serv < 0.14  THEN 'CALCADA'                --  4%
                    WHEN r_serv < 0.19  THEN 'ALVARA_CONSTRUCAO'      --  5%
                    WHEN r_serv < 0.195 THEN 'PODA_ARVORE'            --  0,5%
                    WHEN r_serv < 0.27  THEN 'COLETA_ENTULHO'         --  7,5%
                    WHEN r_serv < 0.29  THEN 'LICENCA_AMBIENTAL'      --  2%
                    WHEN r_serv < 0.44  THEN 'IPTU_SEGUNDA_VIA'       -- 15%
                    WHEN r_serv < 0.52  THEN 'CERTIDAO_NEGATIVA'      --  8%
                    WHEN r_serv < 0.55  THEN 'REVISAO_IPTU'           --  3%
                    WHEN r_serv < 0.67  THEN 'AGENDAMENTO_CONSULTA'   -- 12%
                    WHEN r_serv < 0.70  THEN 'VIGILANCIA_SANITARIA'   --  3%
                    WHEN r_serv < 0.76  THEN 'MATRICULA_ESCOLAR'      --  6%
                    WHEN r_serv < 0.79  THEN 'TRANSPORTE_ESCOLAR'     --  3%
                    WHEN r_serv < 0.88  THEN 'ILUMINACAO_PUBLICA'     --  9%
                    WHEN r_serv < 0.92  THEN 'LIMPEZA_TERRENO'        --  4%
                    WHEN r_serv < 0.935 THEN 'USO_ESPACO_PUBLICO'     --  1,5%
                    WHEN r_serv < 0.95  THEN 'ALVARA_EVENTO'          --  1,5%
                    ELSE 'OUVIDORIA'                                  --  5%
               END AS servico,
               'Setor ' || (ARRAY['Central','Norte','Sul','Leste','Oeste','Aeroporto',
                                  'Universitário','Industrial','Bela Vista',
                                  'Morada do Sol'])[1 + floor(r_bai * 10)::int] AS bairro,
               timestamp '2025-01-01 07:00'
                 + ((g - 1) * 638 / 400000) * interval '1 day'
                 + floor(r_hora * 660) * interval '1 minute' AS aberto_em,
               CASE WHEN r_sit < 0.80 THEN 'CONCLUIDO'               -- 80%
                    WHEN r_sit < 0.88 THEN 'EM_ANDAMENTO'            --  8%
                    WHEN r_sit < 0.95 THEN 'ABERTO'                  --  7%
                    WHEN r_sit < 0.99 THEN 'INDEFERIDO'              --  4%
                    ELSE 'CANCELADO'                                 --  1%
               END AS situacao,
               1 + floor(r_aber * 2000)::int AS aberto_por,
               1 + floor(r_resp * 2000)::int AS responsavel_id,
               r_dur, r_taxa
          FROM (SELECT g, random() AS r_cid, random() AS r_serv, random() AS r_bai,
                       random() AS r_hora, random() AS r_sit, random() AS r_aber,
                       random() AS r_resp, random() AS r_dur, random() AS r_taxa
                  FROM generate_series(1, 400000) AS g) AS s1) AS sorteio
 ORDER BY g;

-- Três andamentos por protocolo, gravados à medida que ele tramita.
INSERT INTO andamento (id, protocolo_id, data_hora, setor, descricao)
SELECT g,
       1 + (g - 1) / 3,
       timestamp '2025-01-01 08:00'
         + ((((g - 1) / 3) * 638 / 400000) + (g - 1) % 3) * interval '1 day'
         + floor(r_hora * 540) * interval '1 minute',
       (ARRAY['Protocolo geral','Setor técnico','Gabinete'])[1 + (g - 1) % 3],
       (ARRAY['Recebido e encaminhado','Em análise pelo setor técnico',
              'Aguardando vistoria','Despacho do secretário',
              'Resposta enviada ao cidadão'])[1 + floor(r_desc * 5)::int]
  FROM (SELECT g, random() AS r_hora, random() AS r_desc
          FROM generate_series(1, 1200000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida) e o índice auxiliar que o sistema já tinha, criado para
--    a tela de consulta por secretaria e serviço.
ALTER TABLE protocolo
    ADD CONSTRAINT protocolo_cidadao_fk     FOREIGN KEY (cidadao_id)     REFERENCES cidadao (id),
    ADD CONSTRAINT protocolo_aberto_por_fk  FOREIGN KEY (aberto_por)     REFERENCES servidor (id),
    ADD CONSTRAINT protocolo_responsavel_fk FOREIGN KEY (responsavel_id) REFERENCES servidor (id);
ALTER TABLE andamento
    ADD CONSTRAINT andamento_protocolo_fk FOREIGN KEY (protocolo_id) REFERENCES protocolo (id);

CREATE INDEX idx_protocolo_secretaria_servico ON protocolo (secretaria, servico);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) cidadao, servidor, protocolo, andamento;

\echo
\echo 'Caso do Grupo 06 pronto (Prefeitura de Serra Azul do Tocantins). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('cidadao', 'servidor', 'protocolo', 'andamento')
 ORDER BY reltuples DESC;
