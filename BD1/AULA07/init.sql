-- =====================================================================
-- Banco de Dados I — Aula 07
-- Clínica Patas do Cerrado — estrutura do banco (DDL)
--
-- É o mesmo script escrito na Aula 06, com duas diferenças:
--   * não traz CREATE DATABASE nem USE, porque o site já abre com um
--     banco pronto e selecionado;
--   * não traz SHOW TABLES nem DESCRIBE, que ficam no painel de comandos.
--
-- ONDE COLAR: no painel "init.sql" do OneCompiler MySQL
--             (https://onecompiler.com/mysql).
-- Esse painel é executado ANTES dos seus comandos, a cada clique em Run.
--
-- SE VOCÊ USA O PODMAN (arquivo compose.yaml desta pasta), rode antes,
-- dentro do cliente MySQL, as duas linhas abaixo — no site elas NÃO são
-- necessárias e devem continuar comentadas:
--
--   CREATE DATABASE IF NOT EXISTS clinica_veterinaria
--     CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
--   USE clinica_veterinaria;
-- =====================================================================

-- ---------------------------------------------------------------------
-- TUTOR — a tabela desta aula. Não depende de nenhuma outra, por isso é
-- criada primeiro e é nela que faremos INSERT, SELECT, UPDATE e DELETE.
-- Regras: nome e telefone obrigatórios; e-mail opcional, mas, quando
-- informado, não pode se repetir.
-- ---------------------------------------------------------------------
CREATE TABLE tutor (
  id        BIGINT UNSIGNED AUTO_INCREMENT,
  nome      VARCHAR(100)    NOT NULL,
  telefone  VARCHAR(20)     NOT NULL,
  email     VARCHAR(150),
  CONSTRAINT pk_tutor       PRIMARY KEY (id),
  CONSTRAINT uq_tutor_email UNIQUE (email)
);

-- ---------------------------------------------------------------------
-- VETERINÁRIO — também não depende de ninguém.
-- Regras: nome e CRMV obrigatórios; o CRMV identifica o profissional e
-- não pode se repetir.
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
-- PROCEDIMENTO — catálogo de serviços da clínica.
-- Regras: nome único; valor de referência nunca negativo.
-- ---------------------------------------------------------------------
CREATE TABLE procedimento (
  id                BIGINT UNSIGNED AUTO_INCREMENT,
  nome              VARCHAR(100)    NOT NULL,
  descricao         VARCHAR(255),
  valor_referencia  DECIMAL(10,2)   NOT NULL,
  CONSTRAINT pk_procedimento       PRIMARY KEY (id),
  CONSTRAINT uq_procedimento_nome  UNIQUE (nome),
  CONSTRAINT ck_procedimento_valor CHECK (valor_referencia >= 0)
);

-- ---------------------------------------------------------------------
-- ANIMAL — depende de TUTOR (relacionamento 1:N).
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
-- CONSULTA — depende de ANIMAL e de VETERINÁRIO.
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
-- CONSULTA_PROCEDIMENTO — tabela associativa do N:M, criada por último.
-- ---------------------------------------------------------------------
CREATE TABLE consulta_procedimento (
  id               BIGINT UNSIGNED   AUTO_INCREMENT,
  consulta_id      BIGINT UNSIGNED   NOT NULL,
  procedimento_id  BIGINT UNSIGNED   NOT NULL,
  quantidade       SMALLINT UNSIGNED NOT NULL DEFAULT 1,
  valor_praticado  DECIMAL(10,2)     NOT NULL,
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
