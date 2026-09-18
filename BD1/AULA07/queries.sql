-- =====================================================================
-- Banco de Dados I — Aula 07
-- Clínica Patas do Cerrado — DML em uma única tabela: tutor
--
-- ONDE COLAR: no painel de comandos do OneCompiler MySQL
--             (https://onecompiler.com/mysql), com o init.sql desta
--             pasta já colado no painel da estrutura.
--
-- COMO USAR: o site executa este painel inteiro, de cima para baixo, a
-- cada clique em Run, e mostra a saída de cada comando. Para acompanhar
-- passo a passo, deixe apenas um bloco sem comentário por vez: duas
-- barras no começo da linha (--) fazem o site ignorá-la.
--
-- LEMBRE: a cada Run o banco é recriado. Nada do que você inseriu na
-- execução anterior continua lá.
-- =====================================================================

-- ---------------------------------------------------------------------
-- PASSO 1 — A estrutura está de pé?
-- ---------------------------------------------------------------------
SHOW TABLES;
DESCRIBE tutor;

-- ---------------------------------------------------------------------
-- PASSO 2 — A tabela existe, mas está vazia
-- Saída esperada: Empty set
-- ---------------------------------------------------------------------
SELECT * FROM tutor;

-- ---------------------------------------------------------------------
-- PASSO 3 — INSERT de uma linha
-- A coluna id não é informada: o AUTO_INCREMENT a preenche sozinho.
-- Saída esperada: Query OK, 1 row affected
-- ---------------------------------------------------------------------
INSERT INTO tutor (nome, telefone, email)
VALUES ('Marina Alves de Sousa', '63 9 9111-1111', 'marina.sousa@exemplo.com');

SELECT * FROM tutor;

-- ---------------------------------------------------------------------
-- PASSO 4 — INSERT de várias linhas em um único comando
-- NULL sem aspas significa "não informado". 'NULL' com aspas seria o
-- texto N-U-L-L, que é outra coisa.
-- Saída esperada: Query OK, 4 rows affected
-- ---------------------------------------------------------------------
INSERT INTO tutor (nome, telefone, email) VALUES
  ('João Batista Ferreira',    '63 9 9222-2222', NULL),
  ('Sebastião Rodrigues Lima', '63 9 9333-3333', 'sebastiao.lima@exemplo.com'),
  ('Aparecida Gomes da Silva', '63 9 9444-4444', 'aparecida.gomes@exemplo.com'),
  ('Raimundo Nonato Barros',   '63 9 9555-5555', NULL);

SELECT * FROM tutor;

-- ---------------------------------------------------------------------
-- PASSO 5 — SELECT: escolher colunas, filtrar, ordenar e limitar
-- ---------------------------------------------------------------------

-- 5.1 Todas as colunas de todas as linhas
SELECT * FROM tutor;

-- 5.2 Só as colunas que interessam (projeção)
SELECT nome, telefone FROM tutor;

-- 5.3 Apelido de coluna: muda o cabeçalho, não o dado
SELECT nome AS tutor, telefone AS contato FROM tutor;

-- 5.4 Filtro por igualdade — 1 linha
SELECT * FROM tutor WHERE id = 3;

-- 5.5 Filtro por ausência de valor — 2 linhas
SELECT nome, email FROM tutor WHERE email IS NULL;

-- 5.6 Filtro por parte do texto — 1 linha (Aparecida Gomes da Silva)
SELECT id, nome FROM tutor WHERE nome LIKE '%Silva%';

-- 5.7 Ordenação alfabética
SELECT id, nome FROM tutor ORDER BY nome;

-- 5.8 Os dois últimos cadastrados
SELECT id, nome FROM tutor ORDER BY id DESC LIMIT 2;

-- ---------------------------------------------------------------------
-- PASSO 6 — UPDATE: corrigir o que já está lá
-- O hábito profissional: primeiro o SELECT com o mesmo WHERE, para ver
-- quais linhas serão atingidas; só então o UPDATE.
-- ---------------------------------------------------------------------

-- 6.1 Conferir antes
SELECT * FROM tutor WHERE id = 2;

-- 6.2 Corrigir o telefone de uma linha
-- Saída esperada: Query OK, 1 row affected
UPDATE tutor
   SET telefone = '63 9 8888-0000'
 WHERE id = 2;

-- 6.3 Conferir depois
SELECT * FROM tutor WHERE id = 2;

-- 6.4 Preencher um e-mail que faltava
UPDATE tutor
   SET email = 'joao.ferreira@exemplo.com'
 WHERE id = 2;

SELECT id, nome, email FROM tutor WHERE id = 2;

-- ---------------------------------------------------------------------
-- PASSO 7 — DELETE: remover linhas
-- A linha sai da tabela; a tabela continua existindo.
-- ---------------------------------------------------------------------

-- 7.1 Ver exatamente quem vai sair
SELECT * FROM tutor WHERE nome = 'Raimundo Nonato Barros';

-- 7.2 Remover
-- Saída esperada: Query OK, 1 row affected
DELETE FROM tutor
 WHERE nome = 'Raimundo Nonato Barros';

-- 7.3 Conferir: sobraram 4 linhas
SELECT * FROM tutor;

-- ---------------------------------------------------------------------
-- EXPERIMENTOS (descomente um de cada vez e observe a mensagem)
-- ---------------------------------------------------------------------

-- Falta uma coluna obrigatória:
-- INSERT INTO tutor (nome) VALUES ('Tutor sem telefone');

-- E-mail repetido, proibido pela restrição uq_tutor_email:
-- INSERT INTO tutor (nome, telefone, email)
-- VALUES ('Outra pessoa', '63 9 9000-0000', 'marina.sousa@exemplo.com');

-- UPDATE sem WHERE: muda a tabela inteira (repare no número de linhas)
-- UPDATE tutor SET telefone = '63 9 0000-0000';

-- DELETE sem WHERE: esvazia a tabela
-- DELETE FROM tutor;
