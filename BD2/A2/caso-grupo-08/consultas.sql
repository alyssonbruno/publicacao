-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 08: Bilhete Cerrado (transporte coletivo)
-- Arquivo: consultas.sql
--
-- As seis consultas de que o consórcio reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g08-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Ouvidoria: reclamações de motorista que não parou no ponto.
SELECT id, linha_id, registrada_em, canal, texto
  FROM reclamacao
 WHERE texto ILIKE '%não parou no ponto%';

-- Q2. Painel do centro de controle: as 50 validações mais recentes da
--     linha 3, atualizado a cada 30 segundos.
SELECT id, cartao_id, veiculo_id, data_hora, tarifa
  FROM validacao
 WHERE linha_id = 3
 ORDER BY data_hora DESC
 LIMIT 50;

-- Q3. Financeiro: receita mensal da linha 1, a maior linha troncal.
SELECT date_trunc('month', data_hora)::date AS mes,
       count(*)                             AS passagens,
       sum(tarifa)                          AS receita
  FROM validacao
 WHERE linha_id = 1
 GROUP BY 1
 ORDER BY 1;

-- Q4. Segurança: validações marcadas como suspeitas (possível cartão
--     clonado) desde julho, com o tipo do cartão.
SELECT v.id, v.data_hora, v.linha_id, c.numero, c.tipo
  FROM validacao v
  JOIN cartao c ON c.id = v.cartao_id
 WHERE v.situacao = 'SUSPEITA'
   AND v.data_hora >= TIMESTAMP '2026-07-01'
 ORDER BY v.data_hora;

-- Q5. Operação: passagens e receita por linha no dia 14/09/2026, no
--     horário de Palmas. Como o validador grava em UTC, o relatório
--     desconta três horas antes de comparar.
SELECT linha_id, count(*) AS passagens, sum(tarifa) AS receita
  FROM validacao
 WHERE data_hora - INTERVAL '3 hours' >= TIMESTAMP '2026-09-14'
   AND data_hora - INTERVAL '3 hours' <  TIMESTAMP '2026-09-15'
 GROUP BY linha_id
 ORDER BY receita DESC;

-- Q6. Fiscalização: tudo o que passou pelo ônibus 217 ou pelas mãos do
--     motorista 512 (investigação de fraude no cobrador eletrônico).
SELECT id, cartao_id, linha_id, veiculo_id, motorista_id, data_hora
  FROM validacao
 WHERE veiculo_id = 217 OR motorista_id = 512
 ORDER BY data_hora;
