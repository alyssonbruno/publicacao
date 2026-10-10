# Caso do Grupo 04 — Crédito Popular Araguaia (fintech)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

A **Crédito Popular Araguaia** é uma financeira digital que oferece crédito
consignado, crédito pessoal, microcrédito e cartão. As propostas chegam pelo
aplicativo e pelas 500 agências parceiras; parte delas exige um **avalista**,
que garante a dívida de outra pessoa. As propostas aprovadas viram parcelas
mensais.

De janeiro de 2025 a setembro de 2026 foram 400 mil propostas e cerca de 770 mil
parcelas. O banco foi montado por uma equipe que já saiu da empresa e não tem
nenhum índice além das chaves primárias. O seu grupo foi contratado para
descobrir o que está lento, consertar o que der e explicar o que não vale a pena
consertar.

## 2. Do que a financeira reclama

- "O atendimento procura tudo o que uma pessoa assinou — como titular ou como
  avalista — e a tela demora."
- "A lista de cobrança de uma cidade, com as parcelas em atraso de cada
  proposta, leva mais de um segundo."
- "A agência digita o código dela e o sistema lista os contratos devagar."
- "O painel do gerente, com as propostas de consignado mais recentes, está lento."
- "O relatório das 100 propostas com mais atraso demora e o servidor grava
  arquivos temporários enquanto ele roda."
- "A fila da mesa de crédito demora para abrir."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `correntista` | 50 mil | os clientes |
| `proposta` | 400 mil | as propostas de crédito, de 01/01/2025 a 30/09/2026 |
| `parcela` | cerca de 770 mil | as parcelas das propostas aprovadas |

```sql
CREATE TABLE correntista (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE proposta (
    id                integer        PRIMARY KEY,
    contrato          varchar(12)    NOT NULL,   -- agência + número: 0457-0123456
    correntista_id    integer        NOT NULL REFERENCES correntista,
    avalista_id       integer        REFERENCES correntista,  -- quando há avalista
    produto           varchar(12)    NOT NULL,   -- CONSIGNADO, PESSOAL, MICROCREDITO, CARTAO
    valor_solicitado  numeric(10,2)  NOT NULL,
    data_proposta     timestamp      NOT NULL,
    situacao          varchar(15)    NOT NULL,   -- APROVADA, RECUSADA, CANCELADA,
                                                 -- ANALISE_MANUAL
    score             smallint       NOT NULL    -- 0 a 1000
);

CREATE TABLE parcela (
    id           integer       PRIMARY KEY,
    proposta_id  integer       NOT NULL REFERENCES proposta,
    numero       smallint      NOT NULL,
    vencimento   date          NOT NULL,
    valor        numeric(9,2)  NOT NULL,
    pago_em      date                        -- vazio: ainda não paga
);
```

**Índices que já existem:** só as chaves primárias e o índice único de
`correntista.cpf`.

As propostas são gravadas no momento em que chegam, então ficam na tabela em
ordem de data. Para entender como os valores se distribuem (quantas propostas há
em cada situação, quantas parcelas estão em atraso), consulte os próprios dados —
isso faz parte do diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Atendimento: tudo o que uma pessoa assinou, como titular ou como avalista

Cerca de **mil vezes por dia**. Hoje: cerca de **12 ms**.

```sql
SELECT id, contrato, correntista_id, avalista_id, produto, situacao,
       data_proposta
  FROM proposta
 WHERE correntista_id = 27182 OR avalista_id = 27182
 ORDER BY data_proposta DESC;
```

### Q2. Cobrança: propostas aprovadas de uma cidade numa semana, com as parcelas em atraso

Cerca de **30 vezes por dia**. Hoje: cerca de **1,3 segundo**.

```sql
SELECT p.contrato, c.nome, p.valor_solicitado,
       (SELECT count(*)
          FROM parcela pa
         WHERE pa.proposta_id = p.id
           AND pa.pago_em IS NULL
           AND pa.vencimento < DATE '2026-10-01') AS parcelas_em_atraso
  FROM proposta p
  JOIN correntista c ON c.id = p.correntista_id
 WHERE c.cidade = 'Dianópolis'
   AND p.situacao = 'APROVADA'
   AND p.data_proposta >= TIMESTAMP '2026-03-02'
   AND p.data_proposta <  TIMESTAMP '2026-03-09'
 ORDER BY parcelas_em_atraso DESC, p.contrato;
```

### Q3. Agência 0457: todos os contratos da agência

O código do contrato começa pelo código da agência. Cerca de **300 vezes por
dia**. Hoje: cerca de **13 ms**.

```sql
SELECT id, contrato, produto, valor_solicitado, situacao
  FROM proposta
 WHERE contrato LIKE '0457-%';
```

### Q4. Painel do gerente: as 30 propostas de consignado mais recentes

O painel se atualiza sozinho: cerca de **5 mil vezes por dia**. Hoje: cerca de
**19 ms**.

```sql
SELECT id, contrato, valor_solicitado, data_proposta, situacao
  FROM proposta
 WHERE produto = 'CONSIGNADO'
 ORDER BY data_proposta DESC
 LIMIT 30;
```

### Q5. Cobrança: as 100 propostas com maior valor em atraso

Cerca de **4 vezes por dia**. Hoje: cerca de **280 ms**, gravando arquivos
temporários.

```sql
SELECT proposta_id,
       count(*) FILTER (WHERE pago_em IS NULL
                          AND vencimento < DATE '2026-10-01') AS parcelas_atrasadas,
       sum(valor) FILTER (WHERE pago_em IS NULL
                            AND vencimento < DATE '2026-10-01') AS valor_atrasado
  FROM parcela
 GROUP BY proposta_id
 ORDER BY valor_atrasado DESC NULLS LAST
 LIMIT 100;
```

### Q6. Mesa de crédito: fila das propostas em análise manual

Cerca de **500 vezes por dia**. Hoje: cerca de **13 ms**.

```sql
SELECT id, contrato, valor_solicitado, score, data_proposta
  FROM proposta
 WHERE situacao = 'ANALISE_MANUAL'
 ORDER BY data_proposta;
```

## 5. O que a financeira espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 1 ms |
| Q2 | menos de 50 ms, com o mesmo resultado |
| Q3 | menos de 5 ms |
| Q4 | menos de 1 ms |
| Q5 | menos de 100 ms e sem gravar arquivos temporários, com as mesmas 100 propostas |
| Q6 | menos de 5 ms |

Ajustar parâmetros do PostgreSQL é permitido, desde que o grupo **teste primeiro
na sessão** (`SET`) e discuta o risco de mudar o servidor inteiro. Para cada
mudança, a financeira quer saber **quanto ela custa**: espaço em disco, efeito
nas gravações (propostas e pagamentos chegam o dia todo) e o trabalho de
manutenção. Uma consulta reescrita precisa devolver **exatamente as mesmas
linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-04
podman compose up -d
until podman exec bd2-a2-g04-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g04-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g04-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-04/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-04/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-04/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-04/consultas.sql>

No PowerShell do Windows, troque o `< preparar.sql` por `-f /a2/preparar.sql`.

**Conexão** (psql ou DBeaver): servidor `localhost`, porta `5432`, usuário
`aluno`, senha `aluno`, banco `banco`.

O `preparar.sql` leva poucos segundos e pode ser rodado quantas vezes quiser:
ele apaga os índices e as visões que vocês criaram, desfaz parâmetros alterados
com `ALTER SYSTEM` e recria os dados, sempre iguais. **Rode-o no começo de cada
sessão e antes de medir cada mudança.**

## 7. Por onde começar

1. Rode cada consulta uma vez, sem `EXPLAIN`, e veja o que ela devolve.
2. Meça cada uma com `EXPLAIN (ANALYZE, BUFFERS)`, duas vezes, e anote a segunda.
3. Para cada plano, pergunte: qual nó gasta mais tempo? Quantas vezes ele roda
   (`loops`)? Quantas linhas ele lê e quantas devolve? Aparece `Disk Usage`?
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
