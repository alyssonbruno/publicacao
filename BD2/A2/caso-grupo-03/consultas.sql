-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 03: Entrega Já Tocantins (logística)
-- Arquivo: consultas.sql
--
-- As seis consultas de que a transportadora reclama, exatamente como
-- o sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g03-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Diretoria: entregas concluídas por cidade e tempo médio, em dias.
SELECT cidade_destino,
       count(*)                                  AS entregues,
       round(avg(entregue_em - data_postagem), 1) AS dias_em_media
  FROM entrega
 WHERE situacao = 'ENTREGUE'
 GROUP BY cidade_destino
 ORDER BY entregues DESC;

-- Q2. Site: o cliente digita o código de rastreio.
SELECT id, codigo_rastreio, cidade_destino, data_postagem, prazo,
       entregue_em, situacao
  FROM entrega
 WHERE codigo_rastreio = 'TO491138101BR';

-- Q3. Financeiro: fatura mensal do maior cliente da transportadora.
SELECT date_trunc('month', data_postagem)::date AS mes,
       count(*)                                 AS entregas,
       sum(valor_frete)                         AS frete
  FROM entrega
 WHERE remetente_id = 3
 GROUP BY 1
 ORDER BY 1;

-- Q4. Portal do remetente: os eventos de rastreio de todas as entregas
--     de uma pequena loja.
SELECT e.codigo_rastreio, ev.data_hora, ev.tipo_evento, ev.local
  FROM entrega e
  JOIN evento_rastreio ev ON ev.entrega_id = e.id
 WHERE e.remetente_id = 4321
 ORDER BY e.codigo_rastreio, ev.data_hora;

-- Q5. Qualidade: entregas de abril de 2026 que chegaram depois do prazo,
--     por cidade.
SELECT cidade_destino, count(*) AS atrasadas
  FROM entrega
 WHERE to_char(data_postagem, 'YYYY-MM') = '2026-04'
   AND entregue_em > prazo
 GROUP BY cidade_destino
 ORDER BY atrasadas DESC;

-- Q6. Recursos humanos: produtividade mensal de um entregador em 2026.
--     O painel de RH roda esta consulta para cada um dos 500 entregadores.
SELECT date_trunc('month', data_postagem)::date AS mes,
       count(*)                                 AS entregas
  FROM entrega
 WHERE entregador_id = 57
   AND data_postagem BETWEEN DATE '2026-01-01' AND DATE '2026-09-30'
 GROUP BY 1
 ORDER BY 1;
