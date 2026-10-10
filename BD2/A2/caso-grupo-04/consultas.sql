-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 04: Crédito Popular Araguaia (fintech)
-- Arquivo: consultas.sql
--
-- As seis consultas de que a financeira reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g04-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Atendimento: tudo o que uma pessoa assinou, como titular ou como
--     avalista (quem garante a dívida de outra pessoa).
SELECT id, contrato, correntista_id, avalista_id, produto, situacao,
       data_proposta
  FROM proposta
 WHERE correntista_id = 27182 OR avalista_id = 27182
 ORDER BY data_proposta DESC;

-- Q2. Cobrança: propostas aprovadas na primeira semana de março de 2026
--     para clientes de Dianópolis, com o número de parcelas em atraso.
SELECT p.contrato, c.nome, p.valor_solicitado,
       (SELECT count(*)
          FROM parcela pa
         WHERE pa.proposta_id = p.id
           AND pa.pago_em IS NULL
           AND pa.vencimento < DATE '2026-10-01') AS parcelas_em_atraso
  FROM proposta p
  JOIN correntista c ON c.id = p.correntista_id
 WHERE c.cidade = 'Dianópolis'
   AND p.situacao = 'APROVADA'
   AND p.data_proposta >= TIMESTAMP '2026-03-02'
   AND p.data_proposta <  TIMESTAMP '2026-03-09'
 ORDER BY parcelas_em_atraso DESC, p.contrato;

-- Q3. Agência 0457: todos os contratos da agência. O código do contrato
--     começa pelo código da agência.
SELECT id, contrato, produto, valor_solicitado, situacao
  FROM proposta
 WHERE contrato LIKE '0457-%';

-- Q4. Painel do gerente: as 30 propostas de consignado mais recentes.
SELECT id, contrato, valor_solicitado, data_proposta, situacao
  FROM proposta
 WHERE produto = 'CONSIGNADO'
 ORDER BY data_proposta DESC
 LIMIT 30;

-- Q5. Cobrança: as 100 propostas com maior valor em atraso.
SELECT proposta_id,
       count(*) FILTER (WHERE pago_em IS NULL
                          AND vencimento < DATE '2026-10-01') AS parcelas_atrasadas,
       sum(valor) FILTER (WHERE pago_em IS NULL
                            AND vencimento < DATE '2026-10-01') AS valor_atrasado
  FROM parcela
 GROUP BY proposta_id
 ORDER BY valor_atrasado DESC NULLS LAST
 LIMIT 100;

-- Q6. Mesa de crédito: fila das propostas em análise manual, da mais
--     antiga para a mais recente.
SELECT id, contrato, valor_solicitado, score, data_proposta
  FROM proposta
 WHERE situacao = 'ANALISE_MANUAL'
 ORDER BY data_proposta;
