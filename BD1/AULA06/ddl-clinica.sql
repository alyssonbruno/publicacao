-- =====================================================================
-- Banco de Dados I — Aula 06
-- Clínica Patas do Cerrado — script DDL completo
-- Modelo lógico construído na Aula 05
-- SGBD: MySQL 8.4 (imagem mysql:8.4)
--
-- Como executar dentro do cliente MySQL:
--   SOURCE /caminho/ddl-clinica.sql;
-- Ou copie e cole bloco a bloco, na ordem em que aparecem.
-- =====================================================================

-- ---------------------------------------------------------------------
-- 1. Banco de dados
-- ---------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS clinica_veterinaria
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE clinica_veterinaria;

-- ---------------------------------------------------------------------
-- 2. TUTOR — não depende de ninguém, por isso vem primeiro
--    Regras: nome e telefone obrigatórios; e-mail, quando informado,
--    não pode se repetir.
-- ---------------------------------------------------------------------
CREATE TABLE tutor (
  id        BIGINT UNSIGNED AUTO_INCREMENT,
  nome      VARCHAR(100)    NOT NULL,
  telefone  VARCHAR(20)     NOT NULL,
  email     VARCHAR(150),
  CONSTRAINT pk_tutor      PRIMARY KEY (id),
  CONSTRAINT uq_tutor_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- 3. VETERINÁRIO — também não depende de ninguém
--    Regras: nome e CRMV obrigatórios; o CRMV identifica o profissional
--    e não pode se repetir.
-- ---------------------------------------------------------------------
CREATE TABLE veterinario (
  id             BIGINT UNSIGNED AUTO_INCREMENT,
  nome           VARCHAR(100)    NOT NULL,
  crmv           VARCHAR(20)     NOT NULL,
  especialidade  VARCHAR(80),
  CONSTRAINT pk_veterinario      PRIMARY KEY (id),
  CONSTRAINT uq_veterinario_crmv UNIQUE (crmv)
);

-- ---------------------------------------------------------------------
-- 4. PROCEDIMENTO — catálogo de serviços da clínica
--    Regras: nome único; valor de referência nunca negativo.
-- ---------------------------------------------------------------------
CREATE TABLE procedimento (
  id                BIGINT UNSIGNED AUTO_INCREMENT,
  nome              VARCHAR(100)    NOT NULL,
  descricao         VARCHAR(255),
  valor_referencia  DECIMAL(10,2)   NOT NULL,
  CONSTRAINT pk_procedimento      PRIMARY KEY (id),
  CONSTRAINT uq_procedimento_nome UNIQUE (nome),
  CONSTRAINT ck_procedimento_valor CHECK (valor_referencia >= 0)
);

-- ---------------------------------------------------------------------
-- 5. ANIMAL — depende de TUTOR (relacionamento 1:N)
--    Regras: todo animal pertence a um tutor existente; nome e espécie
--    obrigatórios; raça e data de nascimento podem faltar.
-- ---------------------------------------------------------------------
CREATE TABLE animal (
  id               BIGINT UNSIGNED AUTO_INCREMENT,
  tutor_id         BIGINT UNSIGNED NOT NULL,
  nome             VARCHAR(80)     NOT NULL,
  especie          VARCHAR(40)     NOT NULL,
  raca             VARCHAR(60),
  data_nascimento  DATE,
  CONSTRAINT pk_animal PRIMARY KEY (id),
  CONSTRAINT fk_animal_tutor FOREIGN KEY (tutor_id)
    REFERENCES tutor (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE
);

-- ---------------------------------------------------------------------
-- 6. CONSULTA — depende de ANIMAL e de VETERINÁRIO
--    Regras: toda consulta pertence a um animal e a um veterinário;
--    data/hora e motivo obrigatórios; o mesmo veterinário não pode ter
--    duas consultas no mesmo horário.
-- ---------------------------------------------------------------------
CREATE TABLE consulta (
  id              BIGINT UNSIGNED AUTO_INCREMENT,
  animal_id       BIGINT UNSIGNED NOT NULL,
  veterinario_id  BIGINT UNSIGNED NOT NULL,
  data_hora       DATETIME        NOT NULL,
  motivo          VARCHAR(255)    NOT NULL,
  observacoes     TEXT,
  CONSTRAINT pk_consulta PRIMARY KEY (id),
  CONSTRAINT fk_consulta_animal FOREIGN KEY (animal_id)
    REFERENCES animal (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT fk_consulta_veterinario FOREIGN KEY (veterinario_id)
    REFERENCES veterinario (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT uq_consulta_agenda UNIQUE (veterinario_id, data_hora)
);

-- ---------------------------------------------------------------------
-- 7. CONSULTA_PROCEDIMENTO — tabela associativa do N:M
--    Depende de CONSULTA e de PROCEDIMENTO, por isso vem por último.
--    Regras: um procedimento aparece uma única vez em cada consulta;
--    a repetição é registrada em quantidade; quantidade maior que zero;
--    valor praticado nunca negativo.
-- ---------------------------------------------------------------------
CREATE TABLE consulta_procedimento (
  id               BIGINT UNSIGNED  AUTO_INCREMENT,
  consulta_id      BIGINT UNSIGNED  NOT NULL,
  procedimento_id  BIGINT UNSIGNED  NOT NULL,
  quantidade       SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  valor_praticado  DECIMAL(10,2)    NOT NULL,
  CONSTRAINT pk_consulta_procedimento PRIMARY KEY (id),
  CONSTRAINT fk_cp_consulta FOREIGN KEY (consulta_id)
    REFERENCES consulta (id)
    ON DELETE CASCADE
    ON UPDATE CASCADE,
  CONSTRAINT fk_cp_procedimento FOREIGN KEY (procedimento_id)
    REFERENCES procedimento (id)
    ON DELETE RESTRICT
    ON UPDATE CASCADE,
  CONSTRAINT uq_consulta_procedimento UNIQUE (consulta_id, procedimento_id),
  CONSTRAINT ck_cp_quantidade CHECK (quantidade > 0),
  CONSTRAINT ck_cp_valor      CHECK (valor_praticado >= 0)
);

-- ---------------------------------------------------------------------
-- 8. Conferência
-- ---------------------------------------------------------------------
SHOW TABLES;

DESCRIBE tutor;
DESCRIBE animal;
DESCRIBE veterinario;
DESCRIBE consulta;
DESCRIBE procedimento;
DESCRIBE consulta_procedimento;

-- Restrições registradas no banco:
SELECT table_name, constraint_name, constraint_type
  FROM information_schema.table_constraints
 WHERE table_schema = 'clinica_veterinaria'
 ORDER BY table_name, constraint_type;
