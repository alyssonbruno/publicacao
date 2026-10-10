# Caso do Grupo 02 — Mercado Norte (loja virtual)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

A **Mercado Norte** é uma loja virtual do Tocantins que vende eletrônicos,
roupas, utensílios de casa, papelaria e artigos esportivos. Em menos de dois anos
(de janeiro de 2025 a setembro de 2026) a loja passou de 400 mil pedidos. O banco
de dados foi montado às pressas, no lançamento, e ninguém mexeu nele desde então.

O seu grupo foi contratado para descobrir o que está lento, consertar o que der
e explicar o que não vale a pena consertar.

## 2. Do que a loja reclama

- "A exportação para a contabilidade, que lê os pedidos de 20 em 20, começa
  rápida e vai ficando cada vez mais lenta."
- "O SAC procura as observações em que o cliente reclamou de embalagem violada e
  a busca demora."
- "A tela 'Meu pedido', que o cliente mais usa, está lenta."
- "O relatório do marketing com os clientes distintos por mês demora e o
  servidor grava arquivos temporários enquanto ele roda."
- "A fila do jurídico, com os pedidos em disputa, demora para abrir."
- "A transportadora pede os pedidos de uma faixa de CEP. Já existe um índice
  no CEP e mesmo assim a busca é lenta."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `cliente` | 50 mil | os clientes cadastrados |
| `produto` | 8 mil | o catálogo |
| `pedido` | 400 mil | os pedidos, de 01/01/2025 a 30/09/2026 |
| `item_pedido` | 1 milhão | os itens de cada pedido (dois ou três por pedido) |

```sql
CREATE TABLE cliente (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    email   varchar(80)  NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE produto (
    id         integer       PRIMARY KEY,
    sku        varchar(10)   NOT NULL UNIQUE,
    nome       varchar(60)   NOT NULL,
    categoria  varchar(20)   NOT NULL,
    preco      numeric(8,2)  NOT NULL
);

CREATE TABLE pedido (
    id           integer        PRIMARY KEY,
    numero       varchar(9)     NOT NULL UNIQUE,  -- o cliente vê: MN0123456
    cliente_id   integer        NOT NULL REFERENCES cliente,
    data_pedido  timestamp      NOT NULL,
    situacao     varchar(20)    NOT NULL,         -- ENTREGUE, CANCELADO, ENVIADO, PAGO,
                                                  -- AGUARDANDO_PAGAMENTO, EM_DISPUTA
    cep_entrega  varchar(8)     NOT NULL,         -- só os dígitos: 77015202
    valor_total  numeric(10,2)  NOT NULL,
    observacao   text                             -- recado do cliente ao entregador
);

CREATE TABLE item_pedido (
    id              integer       PRIMARY KEY,
    pedido_id       integer       NOT NULL REFERENCES pedido,
    produto_id      integer       NOT NULL REFERENCES produto,
    quantidade      smallint      NOT NULL,
    preco_unitario  numeric(8,2)  NOT NULL
);
```

**Índices que já existem:** as chaves primárias, os índices únicos de
`pedido.numero`, `cliente.email` e `produto.sku`, e `idx_pedido_cep`, em
`pedido (cep_entrega)`. Nenhum outro.

Os pedidos são gravados no momento da compra, então ficam na tabela em ordem de
data. Para entender como os valores se distribuem (quantos pedidos há em cada
situação, por exemplo), consulte os próprios dados — isso faz parte do
diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Exportação para a contabilidade, de 20 em 20 pedidos (esta é a página 15.001)

A exportação roda **toda noite** e pede as 20 mil páginas, uma depois da outra.
Hoje, a página 15.001 leva cerca de **38 ms**.

```sql
SELECT id, numero, data_pedido, valor_total
  FROM pedido
 ORDER BY id
 LIMIT 20 OFFSET 300000;
```

### Q2. SAC: pedidos com reclamação de embalagem violada

Cerca de **50 vezes por dia**. Hoje: cerca de **29 ms**.

```sql
SELECT id, numero, data_pedido, observacao
  FROM pedido
 WHERE observacao ILIKE '%embalagem violada%';
```

### Q3. Tela "Meu pedido": os itens de um pedido, pelo número

Cerca de **20 mil vezes por dia**. Hoje: cerca de **45 ms**.

```sql
SELECT p.numero, p.data_pedido, pr.nome AS produto,
       i.quantidade, i.preco_unitario
  FROM pedido p
  JOIN item_pedido i ON i.pedido_id = p.id
  JOIN produto pr    ON pr.id = i.produto_id
 WHERE p.numero = 'MN0599476';
```

### Q4. Marketing: quantos clientes diferentes compraram em cada mês

**Duas vezes por dia.** Hoje: cerca de **180 ms**. Os meses já fechados não
mudam mais, e o marketing aceita que o mês corrente apareça com até um dia de
atraso.

```sql
SELECT date_trunc('month', data_pedido) AS mes,
       count(*)                         AS pedidos,
       count(DISTINCT cliente_id)       AS clientes
  FROM pedido
 GROUP BY 1
 ORDER BY 1;
```

### Q5. Jurídico: fila dos pedidos em disputa, do mais antigo para o mais recente

Cerca de **200 vezes por dia**. Hoje: cerca de **12 ms**.

```sql
SELECT id, numero, data_pedido, valor_total
  FROM pedido
 WHERE situacao = 'EM_DISPUTA'
 ORDER BY data_pedido;
```

### Q6. Logística: pedidos de uma faixa de CEP

Cerca de **100 vezes por dia**. Hoje: cerca de **14 ms**.

```sql
SELECT id, numero, cep_entrega, situacao
  FROM pedido
 WHERE cep_entrega LIKE '77160%';
```

## 5. O que a loja espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 1 ms, **qualquer que seja a página** |
| Q2 | menos de 10 ms |
| Q3 | menos de 1 ms |
| Q4 | menos de 20 ms e sem gravar arquivos temporários — ou a demonstração, com medições, de que não vale a pena |
| Q5 | menos de 5 ms |
| Q6 | menos de 5 ms |

E, para cada mudança, a loja quer saber **quanto ela custa**: espaço em disco,
efeito nas gravações (são milhares de pedidos e itens por dia) e o trabalho de
manutenção. Uma consulta reescrita precisa devolver **exatamente as mesmas
linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-02
podman compose up -d
until podman exec bd2-a2-g02-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g02-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g02-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-02/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-02/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-02/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-02/consultas.sql>

No PowerShell do Windows, troque o `< preparar.sql` por `-f /a2/preparar.sql`.

**Conexão** (psql ou DBeaver): servidor `localhost`, porta `5432`, usuário
`aluno`, senha `aluno`, banco `banco`.

O `preparar.sql` leva poucos segundos e pode ser rodado quantas vezes quiser:
ele apaga os índices e as visões que vocês criaram e recria os dados, sempre
iguais. **Rode-o no começo de cada sessão e antes de medir cada mudança.**

## 7. Por onde começar

1. Rode cada consulta uma vez, sem `EXPLAIN`, e veja o que ela devolve.
2. Meça cada uma com `EXPLAIN (ANALYZE, BUFFERS)`, duas vezes, e anote a segunda.
3. Para cada plano, pergunte: qual nó gasta mais tempo? Quantas linhas ele lê e
   quantas devolve? Há algum índice que deveria estar sendo usado e não está?
   Aparece `Disk:` ou `external merge`?
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
