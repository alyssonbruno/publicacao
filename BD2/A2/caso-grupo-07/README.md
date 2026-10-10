# Caso do Grupo 07 — Protege Tocantins Seguros (seguro de automóveis)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas, as placas e os números
> são fictícios.

## 1. A empresa

A **Protege Tocantins Seguros** vende seguro de automóveis no estado. Quando um
segurado tem um problema — batida, roubo, vidro quebrado, alagamento —, abre um
**sinistro**, que passa por vistoria, conferência de documentos e parecer até ser
indenizado ou negado. Cada passo é um **andamento**.

De janeiro de 2025 a setembro de 2026 foram 400 mil sinistros e 1 milhão de
andamentos. Quando o sinistro é aberto, o sistema copia a placa do veículo para
dentro do sinistro. O banco nunca foi revisado. O seu grupo foi contratado para
descobrir o que está lento, consertar o que der e explicar o que não vale a pena
consertar.

## 2. Do que a seguradora reclama

- "A auditoria externa percorre a lista de sinistros página por página, e cada
  página demora mais que a anterior."
- "A diretoria quer o relatório de indenizações por tipo de sinistro mais rápido."
- "A central de atendimento procura o sinistro pelo boletim de ocorrência e espera."
- "A regulação lista os alagamentos de uma cidade com o último andamento de cada
  um e leva quase dois segundos."
- "O antifraude investiga lotes de placas pelo começo da placa e a busca é lenta."
- "No portal, o segurado abre os andamentos dos seus sinistros e a página demora."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `segurado` | 50 mil | os segurados |
| `veiculo` | 60 mil | os veículos segurados |
| `sinistro` | 400 mil | os sinistros, de 01/01/2025 a 30/09/2026 |
| `andamento_sinistro` | 1 milhão | os andamentos de cada sinistro |

```sql
CREATE TABLE segurado (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    cidade  varchar(25)  NOT NULL
);

CREATE TABLE veiculo (
    id           integer      PRIMARY KEY,
    segurado_id  integer      NOT NULL REFERENCES segurado,
    placa        char(7)      NOT NULL UNIQUE,   -- padrão Mercosul: QKT1A23
    modelo       varchar(30)  NOT NULL,
    ano          smallint     NOT NULL
);

CREATE TABLE sinistro (
    id               integer        PRIMARY KEY,
    boletim          varchar(11)    NOT NULL,   -- boletim de ocorrência: BO123456789
    veiculo_id       integer        NOT NULL REFERENCES veiculo,
    placa            varchar(7)     NOT NULL,   -- copiada do veículo na abertura
    segurado_id      integer        NOT NULL REFERENCES segurado,
    data_ocorrencia  date           NOT NULL,
    cidade           varchar(25)    NOT NULL,
    tipo             varchar(12)    NOT NULL,   -- COLISAO, VIDROS, ROUBO, FURTO,
                                                -- ALAGAMENTO, INCENDIO, OUTROS
    situacao         varchar(12)    NOT NULL,   -- INDENIZADO, NEGADO, EM_ANALISE,
                                                -- EM_VISTORIA
    valor_estimado   numeric(10,2)  NOT NULL,
    valor_pago       numeric(10,2)  NOT NULL    -- 0 se não indenizado
);

CREATE TABLE andamento_sinistro (
    id           integer      PRIMARY KEY,
    sinistro_id  integer      NOT NULL REFERENCES sinistro,
    data_hora    timestamp    NOT NULL,
    etapa        varchar(25)  NOT NULL
);
```

**Índices que já existem:** as chaves primárias, os índices únicos de
`segurado.cpf` e `veiculo.placa` e `idx_sinistro_segurado`, em
`sinistro (segurado_id)`. Nenhum outro.

Os sinistros são gravados na data em que acontecem, então ficam na tabela em
ordem de data. Para entender como os valores se distribuem (quantos sinistros há
de cada tipo e em cada situação), consulte os próprios dados — isso faz parte do
diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Auditoria externa: sinistros por data, de 25 em 25 (esta é a página 10.001)

A auditoria percorre as 16 mil páginas, uma depois da outra, **toda semana**.
Hoje, a página 10.001 leva cerca de **83 ms**, e o servidor grava arquivos
temporários.

```sql
SELECT id, boletim, data_ocorrencia, tipo, situacao, valor_estimado
  FROM sinistro
 ORDER BY data_ocorrencia, id
 LIMIT 25 OFFSET 250000;
```

### Q2. Diretoria: valor indenizado por tipo de sinistro

**Uma vez por mês**, no fechamento. Hoje: cerca de **27 ms**.

```sql
SELECT tipo, count(*) AS sinistros, sum(valor_pago) AS total_pago
  FROM sinistro
 WHERE situacao = 'INDENIZADO'
 GROUP BY tipo
 ORDER BY total_pago DESC;
```

### Q3. Central de atendimento: o sinistro pelo número do boletim de ocorrência

Cerca de **2 mil vezes por dia**. Hoje: cerca de **14 ms**.

```sql
SELECT id, boletim, placa, data_ocorrencia, tipo, situacao
  FROM sinistro
 WHERE boletim = 'BO152605918';
```

### Q4. Regulação: alagamentos em Araguaína em janeiro de 2026, com o último andamento

Cerca de **50 vezes por dia** (depois de cada chuva forte, muito mais). Hoje:
cerca de **1,8 segundo**.

```sql
SELECT s.id, s.boletim, s.data_ocorrencia, s.valor_estimado,
       (SELECT max(a.data_hora)
          FROM andamento_sinistro a
         WHERE a.sinistro_id = s.id) AS ultimo_andamento
  FROM sinistro s
 WHERE s.tipo = 'ALAGAMENTO'
   AND s.cidade = 'Araguaína'
   AND s.data_ocorrencia BETWEEN DATE '2026-01-01' AND DATE '2026-01-31'
 ORDER BY s.data_ocorrencia;
```

### Q5. Antifraude: sinistros de placas que começam por QKT

Cerca de **30 vezes por dia**. Hoje: cerca de **13 ms**.

```sql
SELECT id, boletim, placa, data_ocorrencia, tipo, situacao
  FROM sinistro
 WHERE placa LIKE 'QKT%';
```

### Q6. Portal do segurado: os andamentos de todos os sinistros de um segurado

Cerca de **3 mil vezes por dia**. Hoje: cerca de **33 ms**.

```sql
SELECT s.boletim, s.tipo, a.data_hora, a.etapa
  FROM sinistro s
  JOIN andamento_sinistro a ON a.sinistro_id = s.id
 WHERE s.segurado_id = 4321
 ORDER BY s.boletim, a.data_hora;
```

## 5. O que a seguradora espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 1 ms, **qualquer que seja a página** |
| Q2 | mais rápida — ou a demonstração, com medições, de que não vale a pena |
| Q3 | menos de 1 ms |
| Q4 | menos de 20 ms, com o mesmo resultado |
| Q5 | menos de 3 ms |
| Q6 | menos de 1 ms |

E, para cada mudança, a seguradora quer saber **quanto ela custa**: espaço em
disco, efeito nas gravações (sinistros e andamentos chegam o dia todo) e o
trabalho de manutenção. Uma consulta reescrita precisa devolver **exatamente as
mesmas linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-07
podman compose up -d
until podman exec bd2-a2-g07-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g07-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g07-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-07/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-07/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-07/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-07/consultas.sql>

No PowerShell do Windows, troque o `< preparar.sql` por `-f /a2/preparar.sql`.

**Conexão** (psql ou DBeaver): servidor `localhost`, porta `5432`, usuário
`aluno`, senha `aluno`, banco `banco`.

O `preparar.sql` leva poucos segundos e pode ser rodado quantas vezes quiser:
ele apaga os índices e as visões que vocês criaram e recria os dados, sempre
iguais. **Rode-o no começo de cada sessão e antes de medir cada mudança** — uma
mudança pode melhorar mais de uma consulta, e só a base limpa mostra o "antes"
verdadeiro de cada uma.

## 7. Por onde começar

1. Rode cada consulta uma vez, sem `EXPLAIN`, e veja o que ela devolve.
2. Meça cada uma com `EXPLAIN (ANALYZE, BUFFERS)`, duas vezes, e anote a segunda.
3. Para cada plano, pergunte: qual nó gasta mais tempo? Quantas vezes ele roda
   (`loops`)? Quantas linhas ele lê e quantas devolve? Aparece `Disk:`?
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
