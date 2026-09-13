-- =====================================================================
-- Banco de Dados I — Aula 06
-- Conferência do banco da clínica Patas do Cerrado
--
-- Execute DEPOIS de criar as suas tabelas:
--   SOURCE ~/lab06/conferir-clinica.sql;
--
-- O script não altera nada: apenas lê a estrutura e mostra o que
-- encontrou, para você comparar com o modelo lógico da Aula 05.
-- =====================================================================

USE clinica_veterinaria;

-- ---------------------------------------------------------------------
-- 1. As seis tabelas existem?
--    A coluna `situacao` mostra ">>> FALTA" no que ainda não foi criado.
-- ---------------------------------------------------------------------
SELECT '1. TABELAS ESPERADAS' AS conferencia;

SELECT e.tabela,
       IF(t.TABLE_NAME IS NULL, '>>> FALTA', 'ok') AS situacao
  FROM (          SELECT 'tutor'                 AS tabela
        UNION ALL SELECT 'veterinario'
        UNION ALL SELECT 'procedimento'
        UNION ALL SELECT 'animal'
        UNION ALL SELECT 'consulta'
        UNION ALL SELECT 'consulta_procedimento') AS e
  LEFT JOIN information_schema.TABLES t
         ON t.TABLE_SCHEMA = DATABASE()
        AND t.TABLE_NAME   = e.tabela
 ORDER BY e.tabela;

-- ---------------------------------------------------------------------
-- 2. As colunas de cada tabela
--    Confira o tipo e, principalmente, a coluna `aceita_nulo`:
--    só deve responder YES o que o modelo definiu como opcional.
-- ---------------------------------------------------------------------
SELECT '2. COLUNAS E OBRIGATORIEDADE' AS conferencia;

SELECT TABLE_NAME   AS tabela,
       COLUMN_NAME  AS coluna,
       COLUMN_TYPE  AS tipo,
       IS_NULLABLE  AS aceita_nulo,
       COLUMN_KEY   AS chave,
       EXTRA        AS extra
  FROM information_schema.COLUMNS
 WHERE TABLE_SCHEMA = DATABASE()
 ORDER BY TABLE_NAME, ORDINAL_POSITION;

-- ---------------------------------------------------------------------
-- 3. As restrições registradas
--    As chaves primárias aparecem sempre com o nome PRIMARY:
--    o MySQL ignora o nome escrito em CONSTRAINT pk_...
-- ---------------------------------------------------------------------
SELECT '3. RESTRICOES POR TABELA' AS conferencia;

SELECT TABLE_NAME      AS tabela,
       CONSTRAINT_TYPE AS tipo,
       CONSTRAINT_NAME AS nome
  FROM information_schema.TABLE_CONSTRAINTS
 WHERE TABLE_SCHEMA = DATABASE()
 ORDER BY TABLE_NAME, CONSTRAINT_TYPE, CONSTRAINT_NAME;

-- ---------------------------------------------------------------------
-- 4. As chaves estrangeiras e o que elas fazem
--    Esperado: cinco chaves estrangeiras, todas com ao_apagar RESTRICT,
--    menos fk_cp_consulta, que usa CASCADE de propósito.
-- ---------------------------------------------------------------------
SELECT '4. CHAVES ESTRANGEIRAS' AS conferencia;

SELECT k.CONSTRAINT_NAME       AS fk,
       k.TABLE_NAME            AS tabela_filha,
       k.COLUMN_NAME           AS coluna,
       k.REFERENCED_TABLE_NAME AS tabela_pai,
       r.DELETE_RULE           AS ao_apagar,
       r.UPDATE_RULE           AS ao_atualizar
  FROM information_schema.KEY_COLUMN_USAGE k
  JOIN information_schema.REFERENTIAL_CONSTRAINTS r
    ON r.CONSTRAINT_SCHEMA = k.CONSTRAINT_SCHEMA
   AND r.CONSTRAINT_NAME   = k.CONSTRAINT_NAME
 WHERE k.TABLE_SCHEMA = DATABASE()
   AND k.REFERENCED_TABLE_NAME IS NOT NULL
 ORDER BY k.TABLE_NAME, k.CONSTRAINT_NAME;

-- ---------------------------------------------------------------------
-- 5. As regras de verificação
-- ---------------------------------------------------------------------
SELECT '5. RESTRICOES CHECK' AS conferencia;

SELECT CONSTRAINT_NAME AS nome,
       CHECK_CLAUSE    AS regra
  FROM information_schema.CHECK_CONSTRAINTS
 WHERE CONSTRAINT_SCHEMA = DATABASE()
 ORDER BY CONSTRAINT_NAME;
