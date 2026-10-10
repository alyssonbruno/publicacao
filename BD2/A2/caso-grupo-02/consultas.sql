-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 02: Mercado Norte (loja virtual)
-- Arquivo: consultas.sql
--
-- As seis consultas de que a loja reclama, exatamente como o sistema
-- as envia ao banco. NÃO as altere neste arquivo: ele é o "antes".
-- As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g02-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Integração com o sistema de contabilidade: a exportação lê os
--     pedidos de 20 em 20. Esta é a página 15.001.
SELECT id, numero, data_pedido, valor_total
  FROM pedido
 ORDER BY id
 LIMIT 20 OFFSET 300000;

-- Q2. Atendimento (SAC): pedidos em que o cliente registrou embalagem
--     violada.
SELECT id, numero, data_pedido, observacao
  FROM pedido
 WHERE observacao ILIKE '%embalagem violada%';

-- Q3. Tela "Meu pedido": os itens de um pedido, pelo número que o
--     cliente digita.
SELECT p.numero, p.data_pedido, pr.nome AS produto,
       i.quantidade, i.preco_unitario
  FROM pedido p
  JOIN item_pedido i ON i.pedido_id = p.id
  JOIN produto pr    ON pr.id = i.produto_id
 WHERE p.numero = 'MN0599476';

-- Q4. Marketing: quantos clientes diferentes compraram em cada mês.
SELECT date_trunc('month', data_pedido) AS mes,
       count(*)                         AS pedidos,
       count(DISTINCT cliente_id)       AS clientes
  FROM pedido
 GROUP BY 1
 ORDER BY 1;

-- Q5. Jurídico: fila dos pedidos em disputa, do mais antigo para o
--     mais recente.
SELECT id, numero, data_pedido, valor_total
  FROM pedido
 WHERE situacao = 'EM_DISPUTA'
 ORDER BY data_pedido;

-- Q6. Logística: pedidos de uma faixa de CEP, para montar a rota da
--     transportadora regional.
SELECT id, numero, cep_entrega, situacao
  FROM pedido
 WHERE cep_entrega LIKE '77160%';
