-- ============================================================
-- Banco de Dados II - Laboratório 01 - Carga de Dados
-- ============================================================

-- ESTADOS
INSERT INTO estado (nome, sigla) VALUES
    ('Tocantins', 'TO'),
    ('Goiás', 'GO'),
    ('Maranhão', 'MA'),
    ('Pará', 'PA'),
    ('São Paulo', 'SP');

-- CIDADES
INSERT INTO cidade (nome, estado_id) VALUES
    ('Palmas', 1),
    ('Paraíso do Tocantins', 1),
    ('Gurupi', 1),
    ('Goiânia', 2),
    ('Imperatriz', 3),
    ('Marabá', 4),
    ('São Paulo', 5),
    -- Araguaína recebe cliente, mas nenhum funcionário. Junto com Gurupi
    -- (que tem funcionário e nenhum cliente), garante que UNION, INTERSECT
    -- e EXCEPT produzam resultados distintos nas questões da Parte 3.
    ('Araguaína', 1);

-- DEPARTAMENTOS
INSERT INTO departamento (nome, andar, orcamento) VALUES
    ('Tecnologia da Informação', 3, 150000.00),
    ('Recursos Humanos', 2, 80000.00),
    ('Comercial', 1, 120000.00),
    ('Financeiro', 2, 95000.00),
    ('Marketing', 1, 70000.00),
    ('Logística', 0, 60000.00);

-- FUNCIONÁRIOS (alguns sem departamento, para prática de JOINs)
INSERT INTO funcionario (nome, cpf, email, salario, data_admissao, departamento_id, gerente_id, cidade_id) VALUES
    ('Ana Paula Souza',    '111.111.111-11', 'ana.souza@empresa.com',     12000.00, '2020-03-15', 1, NULL, 1),
    ('Bruno Costa Lima',   '222.222.222-22', 'bruno.lima@empresa.com',     8500.00, '2021-06-01', 1, 1, 1),
    ('Carlos Eduardo Silva','333.333.333-33', 'carlos.silva@empresa.com',  7200.00, '2022-01-10', 1, 1, 2),
    ('Diana Ferreira',     '444.444.444-44', 'diana.ferreira@empresa.com', 9800.00, '2019-11-20', 2, NULL, 1),
    ('Eduardo Martins',    '555.555.555-55', 'eduardo.martins@empresa.com',6500.00, '2023-03-05', 2, 4, 3),
    ('Fernanda Oliveira',  '666.666.666-66', 'fernanda.oliveira@empresa.com',11000.00,'2020-08-12',3, NULL, 4),
    ('Gabriel Santos',     '777.777.777-77', 'gabriel.santos@empresa.com', 7800.00, '2021-09-30', 3, 6, 1),
    ('Helena Rodrigues',   '888.888.888-88', 'helena.rodrigues@empresa.com',8200.00, '2022-04-18', 4, NULL, 5),
    ('Igor Nascimento',    '999.999.999-99', 'igor.nascimento@empresa.com',6000.00, '2023-07-22', 4, 8, 1),
    ('Julia Almeida',      '101.010.101-01', 'julia.almeida@empresa.com', 5800.00, '2024-01-08', NULL, NULL, 6),
    ('Kleber Moreira',     '202.020.202-02', 'kleber.moreira@empresa.com',7500.00, '2022-11-14', 3, 6, 7),
    ('Larissa Pereira',    '303.030.303-03', 'larissa.pereira@empresa.com',13500.00, '2021-02-28', 1, 1, 1),
    ('Marcos Vieira',      '404.040.404-04', 'marcos.vieira@empresa.com', 6800.00, '2023-05-10', NULL, NULL, 2);

-- PROJETOS
INSERT INTO projeto (nome, descricao, data_inicio, data_fim, orcamento, departamento_id) VALUES
    ('Sistema ERP', 'Desenvolvimento do sistema ERP corporativo', '2024-01-15', '2025-12-31', 500000.00, 1),
    ('Migração Cloud', 'Migração da infraestrutura para a nuvem', '2024-06-01', '2025-06-30', 200000.00, 1),
    ('Portal do Cliente', 'Desenvolvimento do portal web para clientes', '2025-01-01', NULL, 150000.00, 1),
    ('Campanha Verão', 'Campanha de marketing para o verão 2026', '2025-10-01', '2026-03-31', 80000.00, 5),
    ('Reestruturação Logística', 'Otimização dos processos logísticos', '2025-03-01', '2025-12-31', 120000.00, 6),
    ('Recrutamento Tech', 'Programa de recrutamento de desenvolvedores', '2025-06-01', '2026-06-30', 50000.00, 2);

-- ALOCAÇÕES EM PROJETOS (nem todos os funcionários estão alocados)
INSERT INTO alocacao_projeto (funcionario_id, projeto_id, papel, horas_semanais, data_entrada) VALUES
    (1, 1, 'Gerente de Projeto', 20, '2024-01-15'),
    (2, 1, 'Desenvolvedor Sênior', 40, '2024-01-15'),
    (3, 1, 'Desenvolvedor Pleno', 40, '2024-02-01'),
    (12, 1, 'Analista de Sistemas', 30, '2024-03-01'),
    (1, 2, 'Líder Técnico', 20, '2024-06-01'),
    (2, 2, 'Arquiteto Cloud', 30, '2024-06-01'),
    (3, 3, 'Desenvolvedor Full-Stack', 40, '2025-01-01'),
    (12, 3, 'Analista de Requisitos', 20, '2025-01-01'),
    (7, 4, 'Coordenador', 30, '2025-10-01'),
    (4, 6, 'Gestora de RH', 20, '2025-06-01'),
    (5, 6, 'Recrutador', 40, '2025-06-01');

-- CATEGORIAS DE PRODUTOS
INSERT INTO categoria (nome) VALUES
    ('Eletrônicos'),
    ('Informática'),
    ('Escritório'),
    ('Móveis'),
    ('Telefonia'),
    ('Acessórios');

-- PRODUTOS (alguns sem categoria, para prática de JOINs)
INSERT INTO produto (nome, descricao, preco, estoque, categoria_id) VALUES
    ('Notebook Dell Inspiron', 'Notebook 15.6" Intel i7 16GB RAM', 4500.00, 25, 2),
    ('Monitor LG 27"', 'Monitor IPS Full HD', 1200.00, 40, 2),
    ('Teclado Mecânico', 'Teclado mecânico RGB switches blue', 350.00, 100, 2),
    ('Mouse Wireless', 'Mouse sem fio ergonômico', 120.00, 150, 2),
    ('Cadeira Gamer', 'Cadeira ergonômica com apoio lombar', 1800.00, 15, 4),
    ('Mesa Escritório', 'Mesa em L com gavetas', 950.00, 20, 4),
    ('Smartphone Samsung', 'Galaxy S24 Ultra 256GB', 6200.00, 30, 5),
    ('Fone Bluetooth', 'Fone over-ear com cancelamento de ruído', 450.00, 60, 1),
    ('Webcam Full HD', 'Webcam 1080p com microfone', 280.00, 80, 2),
    ('Caixa de Som', 'Caixa de som portátil Bluetooth', 320.00, 45, 1),
    ('Cabo HDMI 2m', 'Cabo HDMI 2.1 alta velocidade', 45.00, 200, 6),
    ('Hub USB-C', 'Hub 7 em 1 com HDMI e ethernet', 180.00, 70, 6),
    ('Papel A4 500fls', 'Resma de papel A4 75g', 28.00, 300, 3),
    ('Caneta Esferográfica', 'Caixa com 50 canetas azuis', 35.00, 500, 3),
    ('Impressora Laser', 'Impressora laser monocromática', 1100.00, 10, 2),
    ('Produto Sem Categoria', 'Produto de teste sem categoria', 99.00, 5, NULL);

-- CLIENTES (alguns sem cidade, para prática de JOINs)
INSERT INTO cliente (nome, email, telefone, cidade_id, data_cadastro) VALUES
    ('Tech Solutions Ltda', 'contato@techsolutions.com', '(63) 3333-1111', 1, '2024-01-10'),
    ('Comércio Digital ME', 'vendas@comerciodigital.com', '(63) 3333-2222', 1, '2024-02-15'),
    ('Escritório Moderno', 'adm@escritoriomoderno.com', '(62) 4444-3333', 4, '2024-03-20'),
    ('StartUp Inova', 'hello@startupinova.com', '(63) 3333-4444', 2, '2024-05-01'),
    ('Escola Futuro', 'secretaria@escolafuturo.com', '(63) 3333-5555', 8, '2024-06-12'),
    ('Construtora Horizonte', 'compras@horizonte.com', '(99) 5555-6666', 5, '2024-07-30'),
    ('Hospital Vida', 'compras@hospitalvida.com', '(94) 6666-7777', 6, '2024-08-15'),
    ('Loja Conecta', 'pedidos@conecta.com', '(11) 7777-8888', 7, '2024-09-01'),
    ('Maria das Graças', 'maria.gracas@email.com', '(63) 9999-0001', 1, '2025-01-05'),
    ('João Pedro Alves', 'joao.alves@email.com', '(63) 9999-0002', NULL, '2025-02-10');

-- PEDIDOS
INSERT INTO pedido (cliente_id, funcionario_id, data_pedido, status) VALUES
    (1, 6,  '2025-01-15', 'entregue'),
    (1, 7,  '2025-02-20', 'entregue'),
    (2, 6,  '2025-03-10', 'entregue'),
    (3, 7,  '2025-04-05', 'enviado'),
    (4, 11, '2025-05-12', 'entregue'),
    (5, 6,  '2025-06-18', 'processando'),
    (1, 7,  '2025-07-22', 'entregue'),
    (6, 6,  '2025-08-30', 'pendente'),
    (7, 11, '2025-09-14', 'entregue'),
    (8, 7,  '2025-10-25', 'cancelado'),
    (2, 6,  '2025-11-08', 'enviado'),
    (9, 7,  '2025-12-01', 'pendente'),
    (1, 6,  '2026-01-10', 'processando'),
    (3, 11, '2026-02-05', 'pendente');

-- ITENS DOS PEDIDOS
INSERT INTO item_pedido (pedido_id, produto_id, quantidade, preco_unitario) VALUES
    -- Pedido 1: Tech Solutions compra notebooks e monitores
    (1, 1, 5, 4500.00),
    (1, 2, 5, 1200.00),
    (1, 3, 5, 350.00),
    -- Pedido 2: Tech Solutions compra acessórios
    (2, 4, 10, 120.00),
    (2, 11, 10, 45.00),
    (2, 12, 5, 180.00),
    -- Pedido 3: Comércio Digital
    (3, 1, 2, 4500.00),
    (3, 9, 3, 280.00),
    -- Pedido 4: Escritório Moderno
    (4, 5, 4, 1800.00),
    (4, 6, 4, 950.00),
    (4, 13, 20, 28.00),
    (4, 14, 10, 35.00),
    -- Pedido 5: StartUp Inova
    (5, 1, 3, 4500.00),
    (5, 2, 3, 1200.00),
    (5, 8, 3, 450.00),
    -- Pedido 6: Escola Futuro
    (6, 13, 50, 28.00),
    (6, 14, 30, 35.00),
    (6, 15, 2, 1100.00),
    -- Pedido 7: Tech Solutions mais compras
    (7, 7, 2, 6200.00),
    (7, 8, 2, 450.00),
    -- Pedido 8: Construtora Horizonte
    (8, 6, 10, 950.00),
    (8, 5, 5, 1800.00),
    -- Pedido 9: Hospital Vida
    (9, 1, 10, 4500.00),
    (9, 2, 10, 1200.00),
    (9, 15, 5, 1100.00),
    -- Pedido 10: Loja Conecta (cancelado - mas os itens existem)
    (10, 7, 20, 6200.00),
    (10, 8, 20, 450.00),
    -- Pedido 11: Comércio Digital
    (11, 3, 15, 350.00),
    (11, 4, 15, 120.00),
    -- Pedido 12: Maria das Graças
    (12, 4, 1, 120.00),
    (12, 8, 1, 450.00),
    -- Pedido 13: Tech Solutions
    (13, 9, 5, 280.00),
    (13, 12, 5, 180.00),
    -- Pedido 14: Escritório Moderno
    (14, 13, 30, 28.00),
    (14, 14, 20, 35.00);

-- Obs.: O cliente "João Pedro Alves" (id=10) não possui pedidos.
-- Obs.: O produto "Produto Sem Categoria" (id=16) e "Caixa de Som" (id=10)
--       não aparecem em nenhum pedido.
-- Obs.: Os departamentos "Marketing" (id=5) e "Logística" (id=6) não
--       possuem funcionários.
-- Obs.: Os funcionários Julia Almeida (id=10) e Marcos Vieira (id=13)
--       não possuem departamento.
-- Obs.: A cidade "Araguaína" (id=8) possui cliente e nenhum funcionário;
--       "Gurupi" (id=3) possui funcionário e nenhum cliente. Sem essa
--       assimetria, UNION e INTERSECT retornariam o mesmo conjunto.
