-- =============================================================
-- Banco de Dados II - UNITINS | Prof. Alysson Martins Bruno
-- Avaliação A2 (2026/2) - Caso do Grupo 06: Prefeitura de Serra Azul do Tocantins
-- Arquivo: consultas.sql
--
-- As seis consultas de que a prefeitura reclama, exatamente como o
-- sistema as envia ao banco. NÃO as altere neste arquivo: ele é o
-- "antes". As versões reescritas pelo grupo vão em outro arquivo.
--
-- Para medir uma consulta, entre no banco:
--
--   podman exec -it bd2-a2-g06-pg psql -U aluno -d banco
--
-- e escreva EXPLAIN (ANALYZE, BUFFERS) antes dela. Rode cada medição
-- pelo menos duas vezes e anote a segunda: a primeira costuma ser
-- mais lenta, porque as páginas ainda não estão na memória.
-- =============================================================

-- Q1. Portal do cidadão: a tramitação de um protocolo, pelo número.
SELECT p.numero, p.servico, p.situacao, a.data_hora, a.setor, a.descricao
  FROM protocolo p
  JOIN andamento a ON a.protocolo_id = p.id
 WHERE p.numero = '2026/271828'
 ORDER BY a.data_hora;

-- Q2. Gabinete do prefeito: os 5 protocolos concluídos que mais
--     demoraram, em cada secretaria.
SELECT secretaria, numero, servico, duracao, posicao
  FROM (SELECT secretaria, numero, servico,
               concluido_em - aberto_em AS duracao,
               rank() OVER (PARTITION BY secretaria
                            ORDER BY concluido_em - aberto_em DESC) AS posicao
          FROM protocolo
         WHERE concluido_em IS NOT NULL) AS t
 WHERE posicao <= 5
 ORDER BY secretaria, posicao;

-- Q3. Painel do servidor: protocolos que ele registrou ou pelos quais
--     é o responsável.
SELECT id, numero, servico, situacao, aberto_em, aberto_por, responsavel_id
  FROM protocolo
 WHERE aberto_por = 56 OR responsavel_id = 56
 ORDER BY aberto_em DESC;

-- Q4. Secretaria de Cultura: quanto cada serviço arrecadou em taxas.
SELECT servico, count(*) AS pedidos, sum(taxa) AS arrecadado
  FROM protocolo
 WHERE secretaria = 'CULTURA'
 GROUP BY servico
 ORDER BY servico;

-- Q5. Tela inicial do atendimento: os 50 protocolos abertos mais
--     recentemente, com o nome do cidadão.
SELECT p.numero, p.aberto_em, p.servico, c.nome AS cidadao
  FROM protocolo p
  JOIN cidadao c ON c.id = p.cidadao_id
 ORDER BY p.aberto_em DESC
 LIMIT 50;

-- Q6. Meio Ambiente: pedidos de poda de árvore ainda não concluídos,
--     por bairro.
SELECT bairro, count(*) AS pedidos_pendentes
  FROM protocolo
 WHERE servico = 'PODA_ARVORE'
   AND situacao <> 'CONCLUIDO'
 GROUP BY bairro
 ORDER BY pedidos_pendentes DESC;
