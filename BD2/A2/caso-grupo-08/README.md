# Caso do Grupo 08 — Bilhete Cerrado (transporte coletivo)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

O **Bilhete Cerrado** é o consórcio fictício que administra a bilhetagem
eletrônica de um sistema de ônibus urbano com 60 linhas. Cada vez que um
passageiro passa o cartão na catraca, o validador grava uma **validação**: o
cartão, a linha, o ônibus, o motorista, o horário e a tarifa. O consórcio também
recebe **reclamações** dos passageiros pelo aplicativo, pelo telefone e pelo site.

**Atenção:** o validador grava o horário no **padrão universal (UTC)**, três horas
à frente do horário de Palmas. Uma passagem paga às 7h da manhã aparece como 10h.

De janeiro a setembro de 2026 foram 400 mil validações e 200 mil reclamações. O
seu grupo foi contratado para descobrir o que está lento, consertar o que der e
explicar o que não vale a pena consertar.

## 2. Do que o consórcio reclama

- "A ouvidoria procura as reclamações de motorista que não parou no ponto e a
  busca demora."
- "O painel do centro de controle, com as últimas validações de uma linha, está
  lento — e ele se atualiza sozinho o tempo todo."
- "O relatório de receita mensal da maior linha usa índice e mesmo assim é lento."
- "A segurança lista as validações suspeitas (possível cartão clonado) e espera."
- "O relatório de passagens do dia, no horário de Palmas, é lento."
- "A fiscalização investiga uma fraude: tudo o que passou por um ônibus ou pelas
  mãos de um motorista. A consulta demora."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `cartao` | 80 mil | os cartões de passagem |
| `linha` | 60 | as linhas de ônibus |
| `validacao` | 400 mil | as passagens validadas na catraca, de 01/01 a 30/09/2026 |
| `reclamacao` | 200 mil | as reclamações dos passageiros, no mesmo período |

```sql
CREATE TABLE cartao (
    id       integer      PRIMARY KEY,
    numero   char(16)     NOT NULL UNIQUE,   -- impresso no cartão
    tipo     varchar(10)  NOT NULL,          -- COMUM, ESTUDANTE, IDOSO, PCD
    titular  varchar(60)  NOT NULL
);

CREATE TABLE linha (
    id      integer      PRIMARY KEY,
    codigo  varchar(5)   NOT NULL UNIQUE,    -- ex.: L023
    nome    varchar(60)  NOT NULL
);

CREATE TABLE validacao (
    id            integer       PRIMARY KEY,
    cartao_id     integer       NOT NULL REFERENCES cartao,
    linha_id      integer       NOT NULL REFERENCES linha,
    veiculo_id    integer       NOT NULL,   -- número do ônibus (1 a 800)
    motorista_id  integer       NOT NULL,   -- matrícula do motorista (1 a 3.000)
    data_hora     timestamp     NOT NULL,   -- em UTC (três horas à frente de Palmas)
    tarifa        numeric(5,2)  NOT NULL,   -- 4,50 inteira, 2,25 meia, 0 gratuidade
    situacao      varchar(10)   NOT NULL    -- OK ou SUSPEITA
);

CREATE TABLE reclamacao (
    id             integer      PRIMARY KEY,
    linha_id       integer      NOT NULL REFERENCES linha,
    registrada_em  timestamp    NOT NULL,
    canal          varchar(10)  NOT NULL,   -- APLICATIVO, TELEFONE, SITE
    texto          text         NOT NULL
);
```

**Índices que já existem:** as chaves primárias, os índices únicos de
`cartao.numero` e `linha.codigo` e `idx_validacao_linha`, em
`validacao (linha_id)`. Nenhum outro.

As validações são gravadas no momento da passagem, então ficam na tabela em
ordem de horário. As cinco linhas troncais (1 a 5) são bem maiores que as outras.
Para entender como os valores se distribuem (quantas validações há por linha, em
cada situação), consulte os próprios dados — isso faz parte do diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Ouvidoria: reclamações de motorista que não parou no ponto

Cerca de **30 vezes por dia**. Hoje: cerca de **78 ms**.

```sql
SELECT id, linha_id, registrada_em, canal, texto
  FROM reclamacao
 WHERE texto ILIKE '%não parou no ponto%';
```

### Q2. Centro de controle: as 50 validações mais recentes da linha 3

O painel se atualiza a cada 30 segundos em dez telas: cerca de **30 mil vezes por
dia**. Hoje: cerca de **11 ms**.

```sql
SELECT id, cartao_id, veiculo_id, data_hora, tarifa
  FROM validacao
 WHERE linha_id = 3
 ORDER BY data_hora DESC
 LIMIT 50;
```

### Q3. Financeiro: receita mensal da linha 1, a maior linha troncal

Cerca de **50 vezes por dia**. Hoje: cerca de **13 ms** e cerca de **3.700
páginas** lidas.

```sql
SELECT date_trunc('month', data_hora)::date AS mes,
       count(*)                             AS passagens,
       sum(tarifa)                          AS receita
  FROM validacao
 WHERE linha_id = 1
 GROUP BY 1
 ORDER BY 1;
```

### Q4. Segurança: validações suspeitas desde julho, com o tipo do cartão

Cerca de **300 vezes por dia**. Hoje: cerca de **12 ms**.

```sql
SELECT v.id, v.data_hora, v.linha_id, c.numero, c.tipo
  FROM validacao v
  JOIN cartao c ON c.id = v.cartao_id
 WHERE v.situacao = 'SUSPEITA'
   AND v.data_hora >= TIMESTAMP '2026-07-01'
 ORDER BY v.data_hora;
```

### Q5. Operação: passagens e receita por linha no dia 14/09/2026, no horário de Palmas

Como o validador grava em UTC, o relatório desconta três horas antes de comparar.
Cerca de **100 vezes por dia**. Hoje: cerca de **12 ms**.

```sql
SELECT linha_id, count(*) AS passagens, sum(tarifa) AS receita
  FROM validacao
 WHERE data_hora - INTERVAL '3 hours' >= TIMESTAMP '2026-09-14'
   AND data_hora - INTERVAL '3 hours' <  TIMESTAMP '2026-09-15'
 GROUP BY linha_id
 ORDER BY receita DESC;
```

### Q6. Fiscalização: tudo o que passou pelo ônibus 217 ou pelo motorista 512

Cerca de **20 vezes por dia**. Hoje: cerca de **11 ms**.

```sql
SELECT id, cartao_id, linha_id, veiculo_id, motorista_id, data_hora
  FROM validacao
 WHERE veiculo_id = 217 OR motorista_id = 512
 ORDER BY data_hora;
```

## 5. O que o consórcio espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 10 ms |
| Q2 | menos de 1 ms |
| Q3 | menos de 8 ms e menos de 300 páginas lidas |
| Q4 | menos de 3 ms |
| Q5 | menos de 2 ms, com o mesmo resultado |
| Q6 | menos de 3 ms |

O índice antigo pode ser mantido, trocado ou apagado — desde que o grupo meça o
efeito em todas as consultas que o usam. Para cada mudança, o consórcio quer saber
**quanto ela custa**: espaço em disco, efeito nas gravações (cada passagem é uma
gravação: são milhares por dia) e o trabalho de manutenção. Uma consulta
reescrita precisa devolver **exatamente as mesmas linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-08
podman compose up -d
until podman exec bd2-a2-g08-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g08-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g08-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-08/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-08/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-08/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-08/consultas.sql>

No PowerShell do Windows, troque o `< preparar.sql` por `-f /a2/preparar.sql`.

**Conexão** (psql ou DBeaver): servidor `localhost`, porta `5432`, usuário
`aluno`, senha `aluno`, banco `banco`.

O `preparar.sql` leva poucos segundos e pode ser rodado quantas vezes quiser:
ele apaga os índices e as visões que vocês criaram e recria os dados, sempre
iguais. **Rode-o no começo de cada sessão e antes de medir cada mudança.**

## 7. Por onde começar

1. Rode cada consulta uma vez, sem `EXPLAIN`, e veja o que ela devolve.
2. Meça cada uma com `EXPLAIN (ANALYZE, BUFFERS)`, duas vezes, e anote a segunda.
3. Para cada plano, pergunte: qual nó gasta mais tempo? Quantas linhas e quantas
   páginas ele lê para devolver quantas linhas? O índice usado é o mais adequado?
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
