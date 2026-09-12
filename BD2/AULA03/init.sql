-- ============================================================
-- Banco de Dados II - Laboratório 01
-- Tema: SELECT com JOINs Avançados
-- Professor: Alysson M. Bruno - UNITINS
-- ============================================================

-- Limpa tabelas caso existam (ordem inversa por causa das FKs)
DROP TABLE IF EXISTS item_pedido;
DROP TABLE IF EXISTS pedido;
DROP TABLE IF EXISTS produto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS alocacao_projeto;
DROP TABLE IF EXISTS projeto;
DROP TABLE IF EXISTS funcionario;
DROP TABLE IF EXISTS departamento;
DROP TABLE IF EXISTS cliente;
DROP TABLE IF EXISTS cidade;
DROP TABLE IF EXISTS estado;

-- ============================================================
-- TABELAS DE LOCALIZAÇÃO
-- ============================================================

CREATE TABLE estado (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL,
    sigla CHAR(2) NOT NULL UNIQUE
);

CREATE TABLE cidade (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    estado_id INT NOT NULL REFERENCES estado(id)
);

-- ============================================================
-- TABELAS DE RH
-- ============================================================

CREATE TABLE departamento (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    andar INT,
    orcamento DECIMAL(12,2) DEFAULT 0
);

CREATE TABLE funcionario (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    cpf VARCHAR(14) UNIQUE NOT NULL,
    email VARCHAR(100),
    salario DECIMAL(10,2) NOT NULL CHECK (salario > 0),
    data_admissao DATE NOT NULL DEFAULT CURRENT_DATE,
    departamento_id INT REFERENCES departamento(id),
    gerente_id INT REFERENCES funcionario(id),
    cidade_id INT REFERENCES cidade(id)
);

CREATE TABLE projeto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    data_inicio DATE NOT NULL,
    data_fim DATE,
    orcamento DECIMAL(12,2),
    departamento_id INT REFERENCES departamento(id)
);

CREATE TABLE alocacao_projeto (
    funcionario_id INT NOT NULL REFERENCES funcionario(id),
    projeto_id INT NOT NULL REFERENCES projeto(id),
    papel VARCHAR(50) NOT NULL,
    horas_semanais INT DEFAULT 40,
    data_entrada DATE NOT NULL DEFAULT CURRENT_DATE,
    PRIMARY KEY (funcionario_id, projeto_id)
);

-- ============================================================
-- TABELAS DE VENDAS
-- ============================================================

CREATE TABLE cliente (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE,
    telefone VARCHAR(20),
    cidade_id INT REFERENCES cidade(id),
    data_cadastro DATE DEFAULT CURRENT_DATE
);

CREATE TABLE categoria (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE produto (
    id SERIAL PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    descricao TEXT,
    preco DECIMAL(10,2) NOT NULL CHECK (preco > 0),
    estoque INT DEFAULT 0,
    categoria_id INT REFERENCES categoria(id)
);

CREATE TABLE pedido (
    id SERIAL PRIMARY KEY,
    cliente_id INT REFERENCES cliente(id),
    funcionario_id INT REFERENCES funcionario(id),
    data_pedido DATE DEFAULT CURRENT_DATE,
    status VARCHAR(20) DEFAULT 'pendente'
        CHECK (status IN ('pendente', 'processando', 'enviado', 'entregue', 'cancelado'))
);

CREATE TABLE item_pedido (
    pedido_id INT NOT NULL REFERENCES pedido(id),
    produto_id INT NOT NULL REFERENCES produto(id),
    quantidade INT NOT NULL CHECK (quantidade > 0),
    preco_unitario DECIMAL(10,2) NOT NULL CHECK (preco_unitario > 0),
    PRIMARY KEY (pedido_id, produto_id)
);
