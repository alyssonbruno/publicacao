-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 05: AVA Cerrado Digital (educação a distância)
-- Arquivo: consultas.sql
--
-- As seis consultas de que a plataforma reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g05-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Coordenação: mensagens de agosto de 2026, por curso.
SELECT curso_id, count(*) AS mensagens
  FROM mensagem_forum
 WHERE date_trunc('month', postado_em) = TIMESTAMP '2026-08-01'
 GROUP BY curso_id
 ORDER BY mensagens DESC;

-- Q2. Tutoria: histórico de mensagens de um aluno.
SELECT id, curso_id, postado_em, situacao, left(texto, 60) AS trecho
  FROM mensagem_forum
 WHERE aluno_id = 4321
 ORDER BY postado_em DESC;

-- Q3. Direção: mensagens publicadas pelo celular, por curso.
SELECT curso_id, count(*) AS mensagens_pelo_celular
  FROM mensagem_forum
 WHERE situacao = 'PUBLICADA'
   AND dispositivo = 'CELULAR'
 GROUP BY curso_id
 ORDER BY curso_id;

-- Q4. Secretaria: mensagens que falam de boleto vencido.
SELECT id, aluno_id, postado_em, texto
  FROM mensagem_forum
 WHERE texto ILIKE '%boleto vencido%';

-- Q5. Mural do curso 1, que mostra 20 mensagens por página, da mais
--     nova para a mais antiga. Esta é a página 1.501.
SELECT id, aluno_id, postado_em, left(texto, 60) AS trecho
  FROM mensagem_forum
 WHERE curso_id = 1
 ORDER BY postado_em DESC, id DESC
 LIMIT 20 OFFSET 30000;

-- Q6. Moderação: quantas mensagens denunciadas há em cada curso.
SELECT curso_id, count(*) AS denunciadas
  FROM mensagem_forum
 WHERE situacao = 'DENUNCIADA'
 GROUP BY curso_id
 ORDER BY denunciadas DESC;
