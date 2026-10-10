-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 01: Clínica Vida Cerrado
-- Arquivo: consultas.sql
--
-- As seis consultas de que a clínica reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g01-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Recepção: localizar o atendimento pelo protocolo impresso na guia.
SELECT id, protocolo, paciente_id, medico_id, unidade, data_hora, situacao
  FROM atendimento
 WHERE protocolo = 'CV0189208';

-- Q2. Recepção: agenda do dia de uma unidade.
SELECT a.data_hora, p.nome AS paciente, m.nome AS medico, a.tipo, a.situacao
  FROM atendimento a
  JOIN paciente p ON p.id = a.paciente_id
  JOIN medico m   ON m.id = a.medico_id
 WHERE date(a.data_hora) = DATE '2026-03-16'
   AND a.unidade = 'Palmas Centro'
 ORDER BY a.data_hora;

-- Q3. Prontuário: tudo o que envolve uma pessoa, como paciente ou como
--     acompanhante (responsável) de outro paciente.
SELECT id, data_hora, unidade, paciente_id, responsavel_id, situacao
  FROM atendimento
 WHERE paciente_id = 31415 OR responsavel_id = 31415
 ORDER BY data_hora DESC;

-- Q4. Auditoria: os 20 atendimentos de maior valor de 2026.
SELECT id, protocolo, data_hora, unidade, valor
  FROM atendimento
 WHERE data_hora >= TIMESTAMP '2026-01-01'
 ORDER BY valor DESC
 LIMIT 20;

-- Q5. Diretoria: faturamento dos atendimentos realizados, por unidade.
SELECT unidade, count(*) AS atendimentos, sum(valor) AS faturamento
  FROM atendimento
 WHERE situacao = 'REALIZADO'
 GROUP BY unidade
 ORDER BY faturamento DESC;

-- Q6. Médico: atendimentos da primeira quinzena de março de 2026, com a
--     quantidade de exames pedidos em cada um.
SELECT a.id, a.data_hora, p.nome AS paciente,
       (SELECT count(*)
          FROM exame_pedido e
         WHERE e.atendimento_id = a.id) AS exames
  FROM atendimento a
  JOIN paciente p ON p.id = a.paciente_id
 WHERE a.medico_id = 17
   AND a.data_hora >= TIMESTAMP '2026-03-01'
   AND a.data_hora <  TIMESTAMP '2026-03-16'
 ORDER BY a.data_hora;
