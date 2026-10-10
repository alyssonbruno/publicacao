-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 05: AVA Cerrado Digital (educação a distância)
-- Arquivo: preparar.sql
--
-- Deixa o banco do caso no ponto de partida, sempre igual:
--
--   aluno            40 mil alunos
--   curso            40 cursos
--   mensagem_forum   400 mil mensagens nos fóruns (01/01/2025 a 30/09/2026)
--
-- Execute no início de TODA sessão de trabalho, pelo terminal:
--
--   podman exec -i bd2-a2-g05-pg psql -U aluno -d banco < preparar.sql
--
-- No PowerShell do Windows, onde o `<` não funciona:
--
--   podman exec bd2-a2-g05-pg psql -U aluno -d banco -f /a2/preparar.sql
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
DROP TABLE IF EXISTS mensagem_forum, curso, aluno CASCADE;

-- 3. As tabelas.
CREATE TABLE aluno (
    id     integer      PRIMARY KEY,
    nome   varchar(60)  NOT NULL,
    ra     char(9)      NOT NULL UNIQUE,   -- registro acadêmico
    polo   varchar(25)  NOT NULL
);

CREATE TABLE curso (
    id    integer      PRIMARY KEY,
    nome  varchar(60)  NOT NULL
);

CREATE TABLE mensagem_forum (
    id           integer      PRIMARY KEY,
    curso_id     integer      NOT NULL,
    aluno_id     integer      NOT NULL,
    postado_em   timestamp    NOT NULL,   -- 01/01/2025 a 30/09/2026
    situacao     varchar(15)  NOT NULL,   -- PUBLICADA, OCULTA, EM_MODERACAO, DENUNCIADA
    dispositivo  varchar(10)  NOT NULL,   -- CELULAR, COMPUTADOR, TABLET
    texto        text         NOT NULL
);

-- 4. Os dados. A semente fixa torna o sorteio repetível.
SELECT setseed(0.2605) \g /dev/null

INSERT INTO aluno (id, nome, ra, polo)
SELECT g,
       (ARRAY['Ana','Antônio','Beatriz','Carlos','Daniela','Eduardo','Fernanda',
              'Francisco','Gabriela','João','Juliana','José','Larissa','Lucas',
              'Maria','Mateus','Patrícia','Pedro','Raimunda','Vitória'])[1 + floor(r1 * 20)::int]
       || ' ' ||
       (ARRAY['Almeida','Alves','Barbosa','Carvalho','Costa','Ferreira','Gomes',
              'Lima','Martins','Oliveira','Pereira','Ribeiro','Rodrigues','Santos',
              'Silva','Sousa'])[1 + floor(r2 * 16)::int],
       '2025' || lpad(g::text, 5, '0'),
       (ARRAY['Palmas','Araguaína','Gurupi','Porto Nacional','Dianópolis',
              'Augustinópolis','Arraias','Guaraí'])[1 + floor(r3 * 8)::int]
  FROM (SELECT g, random() AS r1, random() AS r2, random() AS r3
          FROM generate_series(1, 40000) AS g) AS sorteio
 ORDER BY g;

INSERT INTO curso (id, nome)
SELECT g,
       (ARRAY['Administração','Pedagogia','Ciências Contábeis','Gestão Pública',
              'Sistemas para Internet','Letras','Matemática','Logística'])[1 + (g - 1) % 8]
       || ' - turma ' || (1 + (g - 1) / 8)
  FROM generate_series(1, 40) AS g;

-- As mensagens entram em ordem de data. Os três primeiros cursos são
-- os maiores (15%, 10% e 8% das mensagens); os outros 37 dividem o resto.
INSERT INTO mensagem_forum (id, curso_id, aluno_id, postado_em, situacao,
                            dispositivo, texto)
SELECT g,
       CASE WHEN r_cur < 0.15 THEN 1
            WHEN r_cur < 0.25 THEN 2
            WHEN r_cur < 0.33 THEN 3
            ELSE 4 + floor((r_cur - 0.33) / 0.67 * 37)::int
       END,
       1 + floor(r_alu * 40000)::int,
       timestamp '2025-01-01'
         + ((g - 1) * 638 / 400000) * interval '1 day'
         + floor(r_hora * 1440) * interval '1 minute',
       CASE WHEN r_sit < 0.930 THEN 'PUBLICADA'              -- 93,0%
            WHEN r_sit < 0.970 THEN 'OCULTA'                 --  4,0%
            WHEN r_sit < 0.995 THEN 'EM_MODERACAO'           --  2,5%
            ELSE 'DENUNCIADA'                                --  0,5%
       END,
       CASE WHEN r_dis < 0.82 THEN 'CELULAR'                 -- 82%
            WHEN r_dis < 0.97 THEN 'COMPUTADOR'              -- 15%
            ELSE 'TABLET'                                    --  3%
       END,
       CASE WHEN r_bol < 0.004 THEN 'Professor, o boleto vencido não abre no aplicativo. '
            ELSE ''
       END
       || (ARRAY['Bom dia, turma!','Tenho uma dúvida sobre a atividade da semana.',
                 'Alguém conseguiu abrir o material da unidade 2?',
                 'Obrigado pela explicação, professor.',
                 'Qual é o prazo da avaliação?','Concordo com o colega acima.',
                 'Segue o link do meu trabalho.','A videoaula travou no minuto 12.',
                 'Não consegui enviar o arquivo pelo celular.',
                 'Quando sai a nota da prova?'])[1 + floor(r_t1 * 10)::int]
       || ' '
       || (ARRAY['Abraços.','Até mais.','Fico no aguardo.','Valeu!',
                 'Desde já agradeço.','Boa semana a todos.'])[1 + floor(r_t2 * 6)::int]
  FROM (SELECT g, random() AS r_cur, random() AS r_alu, random() AS r_hora,
               random() AS r_sit, random() AS r_dis, random() AS r_bol,
               random() AS r_t1, random() AS r_t2
          FROM generate_series(1, 400000) AS g) AS sorteio
 ORDER BY g;

-- 5. Chaves estrangeiras (criadas depois da carga, que fica mais
--    rápida). Não há nenhum índice auxiliar: só as chaves primárias
--    e o RA, que é único.
ALTER TABLE mensagem_forum
    ADD CONSTRAINT mensagem_forum_curso_fk FOREIGN KEY (curso_id) REFERENCES curso (id),
    ADD CONSTRAINT mensagem_forum_aluno_fk FOREIGN KEY (aluno_id) REFERENCES aluno (id);

-- 6. Estatísticas atualizadas e mapa de visibilidade em dia.
VACUUM (ANALYZE) aluno, curso, mensagem_forum;

\echo
\echo 'Caso do Grupo 05 pronto (AVA Cerrado Digital). Tabelas:'
SELECT relname                              AS tabela,
       reltuples::bigint                    AS linhas,
       pg_size_pretty(pg_table_size(oid))   AS tamanho
  FROM pg_class
 WHERE relname IN ('aluno', 'curso', 'mensagem_forum')
 ORDER BY reltuples DESC;
