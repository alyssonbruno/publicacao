# Caso do Grupo 01 — Clínica Vida Cerrado

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

A **Clínica Vida Cerrado** é uma rede de seis unidades de atendimento médico no
Tocantins: Palmas Centro, Palmas Sul, Araguaína, Gurupi, Porto Nacional e
Paraíso do Tocantins. O sistema de agendamento foi feito há anos por uma empresa
que já fechou, e o banco de dados nunca passou por uma revisão.

Com quase dois anos de atendimentos guardados (de janeiro de 2025 a setembro de
2026), as telas começaram a demorar. A direção contratou o seu grupo para
descobrir o que está lento, consertar o que der e explicar o que não vale a pena
consertar.

## 2. Do que a clínica reclama

- "Na recepção, a gente digita o protocolo da guia e a tela fica pensando."
- "A agenda do dia demora para abrir, principalmente às 7h, quando todo mundo
  chega."
- "Para ver o histórico de uma pessoa — como paciente ou como acompanhante de
  outro paciente — o prontuário trava."
- "A auditoria reclama da lista dos atendimentos mais caros."
- "O relatório de faturamento da diretoria é lento."
- "O médico abre a lista dos atendimentos com a quantidade de exames pedidos e
  dá tempo de tomar um café."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `paciente` | 40 mil | os pacientes cadastrados |
| `medico` | 240 | os médicos da rede |
| `atendimento` | 400 mil | cada consulta, retorno ou urgência, de 01/01/2025 a 30/09/2026 |
| `exame_pedido` | 600 mil | os exames pedidos em cada atendimento |

```sql
CREATE TABLE paciente (
    id               integer      PRIMARY KEY,
    nome             varchar(60)  NOT NULL,
    cpf              char(11)     NOT NULL UNIQUE,
    data_nascimento  date         NOT NULL,
    cidade           varchar(25)  NOT NULL
);

CREATE TABLE medico (
    id             integer      PRIMARY KEY,
    nome           varchar(60)  NOT NULL,
    especialidade  varchar(30)  NOT NULL
);

CREATE TABLE atendimento (
    id              integer       PRIMARY KEY,
    protocolo       varchar(9)    NOT NULL,   -- impresso na guia, ex.: CV0123456
    paciente_id     integer       NOT NULL REFERENCES paciente,
    responsavel_id  integer       REFERENCES paciente,  -- acompanhante, quando há
    medico_id       integer       NOT NULL REFERENCES medico,
    unidade         varchar(25)   NOT NULL,   -- seis unidades
    data_hora       timestamp     NOT NULL,
    tipo            varchar(10)   NOT NULL,   -- CONSULTA, RETORNO, URGENCIA
    situacao        varchar(10)   NOT NULL,   -- REALIZADO, FALTOU, CANCELADO
    valor           numeric(8,2)  NOT NULL
);

CREATE TABLE exame_pedido (
    id              integer      PRIMARY KEY,
    atendimento_id  integer      NOT NULL REFERENCES atendimento,
    exame           varchar(30)  NOT NULL,
    urgente         boolean      NOT NULL
);
```

**Índices que já existem:** as chaves primárias, o índice único de
`paciente.cpf` e `idx_atendimento_paciente`, em `atendimento (paciente_id)`.
Nenhum outro.

Os atendimentos são gravados no dia em que acontecem, então ficam na tabela em
ordem de data. Para entender como os valores se distribuem (quantos atendimentos
há em cada situação, por exemplo), consulte os próprios dados — isso faz parte do
diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Recepção: localizar o atendimento pelo protocolo da guia

Cerca de **3.000 vezes por dia**. Hoje: cerca de **13 ms**.

```sql
SELECT id, protocolo, paciente_id, medico_id, unidade, data_hora, situacao
  FROM atendimento
 WHERE protocolo = 'CV0189208';
```

### Q2. Recepção: agenda do dia de uma unidade

Cerca de **600 vezes por dia**. Hoje: cerca de **16 ms**.

```sql
SELECT a.data_hora, p.nome AS paciente, m.nome AS medico, a.tipo, a.situacao
  FROM atendimento a
  JOIN paciente p ON p.id = a.paciente_id
  JOIN medico m   ON m.id = a.medico_id
 WHERE date(a.data_hora) = DATE '2026-03-16'
   AND a.unidade = 'Palmas Centro'
 ORDER BY a.data_hora;
```

### Q3. Prontuário: tudo o que envolve uma pessoa, como paciente ou como acompanhante

Cerca de **400 vezes por dia**. Hoje: cerca de **13 ms**.

```sql
SELECT id, data_hora, unidade, paciente_id, responsavel_id, situacao
  FROM atendimento
 WHERE paciente_id = 31415 OR responsavel_id = 31415
 ORDER BY data_hora DESC;
```

### Q4. Auditoria: os 20 atendimentos de maior valor de 2026

Cerca de **20 vezes por dia**. Hoje: cerca de **21 ms**.

```sql
SELECT id, protocolo, data_hora, unidade, valor
  FROM atendimento
 WHERE data_hora >= TIMESTAMP '2026-01-01'
 ORDER BY valor DESC
 LIMIT 20;
```

### Q5. Diretoria: faturamento dos atendimentos realizados, por unidade

**Uma vez por dia**, às 8h. Hoje: cerca de **31 ms**. A diretoria aceita ver os
números fechados no dia anterior.

```sql
SELECT unidade, count(*) AS atendimentos, sum(valor) AS faturamento
  FROM atendimento
 WHERE situacao = 'REALIZADO'
 GROUP BY unidade
 ORDER BY faturamento DESC;
```

### Q6. Médico: atendimentos da quinzena, com a quantidade de exames pedidos

Cerca de **300 vezes por dia** (cada médico abre a tela várias vezes). Hoje:
cerca de **430 ms**.

```sql
SELECT a.id, a.data_hora, p.nome AS paciente,
       (SELECT count(*)
          FROM exame_pedido e
         WHERE e.atendimento_id = a.id) AS exames
  FROM atendimento a
  JOIN paciente p ON p.id = a.paciente_id
 WHERE a.medico_id = 17
   AND a.data_hora >= TIMESTAMP '2026-03-01'
   AND a.data_hora <  TIMESTAMP '2026-03-16'
 ORDER BY a.data_hora;
```

## 5. O que a clínica espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 1 ms |
| Q2 | menos de 2 ms, com o mesmo resultado |
| Q3 | menos de 1 ms |
| Q4 | menos de 1 ms |
| Q5 | menos de 5 ms — ou a demonstração, com medições, de que não vale a pena |
| Q6 | menos de 20 ms |

E, para cada mudança, a clínica quer saber **quanto ela custa**: espaço em disco,
efeito nas gravações (a recepção grava atendimentos o dia todo e o laboratório
grava exames o dia todo) e o trabalho de manutenção. Uma consulta reescrita
precisa devolver **exatamente as mesmas linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-01
podman compose up -d
until podman exec bd2-a2-g01-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g01-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g01-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-01/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-01/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-01/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-01/consultas.sql>

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
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
