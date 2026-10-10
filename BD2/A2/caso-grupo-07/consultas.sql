-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 07: Protege Tocantins Seguros (seguro de automóveis)
-- Arquivo: consultas.sql
--
-- As seis consultas de que a seguradora reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g07-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Auditoria externa: a tela lista os sinistros por data, de 25 em
--     25. Esta é a página 10.001.
SELECT id, boletim, data_ocorrencia, tipo, situacao, valor_estimado
  FROM sinistro
 ORDER BY data_ocorrencia, id
 LIMIT 25 OFFSET 250000;

-- Q2. Diretoria: valor indenizado por tipo de sinistro.
SELECT tipo, count(*) AS sinistros, sum(valor_pago) AS total_pago
  FROM sinistro
 WHERE situacao = 'INDENIZADO'
 GROUP BY tipo
 ORDER BY total_pago DESC;

-- Q3. Central de atendimento: localizar o sinistro pelo número do
--     boletim de ocorrência.
SELECT id, boletim, placa, data_ocorrencia, tipo, situacao
  FROM sinistro
 WHERE boletim = 'BO152605918';

-- Q4. Regulação: alagamentos em Araguaína em janeiro de 2026, com a
--     data do último andamento de cada um.
SELECT s.id, s.boletim, s.data_ocorrencia, s.valor_estimado,
       (SELECT max(a.data_hora)
          FROM andamento_sinistro a
         WHERE a.sinistro_id = s.id) AS ultimo_andamento
  FROM sinistro s
 WHERE s.tipo = 'ALAGAMENTO'
   AND s.cidade = 'Araguaína'
   AND s.data_ocorrencia BETWEEN DATE '2026-01-01' AND DATE '2026-01-31'
 ORDER BY s.data_ocorrencia;

-- Q5. Antifraude: sinistros de placas que começam por QKT (lote de
--     placas sob investigação).
SELECT id, boletim, placa, data_ocorrencia, tipo, situacao
  FROM sinistro
 WHERE placa LIKE 'QKT%';

-- Q6. Portal do segurado: os andamentos de todos os sinistros de um
--     segurado.
SELECT s.boletim, s.tipo, a.data_hora, a.etapa
  FROM sinistro s
  JOIN andamento_sinistro a ON a.sinistro_id = s.id
 WHERE s.segurado_id = 4321
 ORDER BY s.boletim, a.data_hora;
