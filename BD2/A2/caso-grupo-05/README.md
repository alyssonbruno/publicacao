# Caso do Grupo 05 — AVA Cerrado Digital (educação a distância)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

O **AVA Cerrado Digital** é o ambiente virtual de aprendizagem de uma faculdade
fictícia de educação a distância, com 40 turmas e 40 mil alunos espalhados por
polos do Tocantins. O coração da plataforma é o **fórum**: cada turma tem um
mural em que alunos e tutores trocam mensagens — a maioria escrita pelo celular.

De janeiro de 2025 a setembro de 2026 foram 400 mil mensagens. O banco não tem
nenhum índice além das chaves primárias. O seu grupo foi contratado para
descobrir o que está lento, consertar o que der e explicar o que não vale a pena
consertar.

## 2. Do que a plataforma reclama

- "O relatório mensal da coordenação, com as mensagens por curso, está lento."
- "O tutor abre o histórico de um aluno e espera."
- "A direção quer o relatório das mensagens enviadas pelo celular mais rápido."
- "A secretaria procura as mensagens que falam de boleto vencido e a busca demora."
- "Quanto mais para trás o aluno vai no mural da turma, mais lenta fica cada
  página."
- "O painel da moderação, com as denúncias por curso, demora para abrir."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `aluno` | 40 mil | os alunos |
| `curso` | 40 | as turmas |
| `mensagem_forum` | 400 mil | as mensagens dos fóruns, de 01/01/2025 a 30/09/2026 |

```sql
CREATE TABLE aluno (
    id     integer      PRIMARY KEY,
    nome   varchar(60)  NOT NULL,
    ra     char(9)      NOT NULL UNIQUE,   -- registro acadêmico
    polo   varchar(25)  NOT NULL
);

CREATE TABLE curso (
    id    integer      PRIMARY KEY,
    nome  varchar(60)  NOT NULL
);

CREATE TABLE mensagem_forum (
    id           integer      PRIMARY KEY,
    curso_id     integer      NOT NULL REFERENCES curso,
    aluno_id     integer      NOT NULL REFERENCES aluno,
    postado_em   timestamp    NOT NULL,
    situacao     varchar(15)  NOT NULL,   -- PUBLICADA, OCULTA, EM_MODERACAO, DENUNCIADA
    dispositivo  varchar(10)  NOT NULL,   -- CELULAR, COMPUTADOR, TABLET
    texto        text         NOT NULL
);
```

**Índices que já existem:** só as chaves primárias e o índice único de
`aluno.ra`.

As mensagens são gravadas no momento em que são postadas, então ficam na tabela
em ordem de data. Algumas turmas são bem maiores que outras. Para entender como
os valores se distribuem (quantas mensagens há em cada situação, em cada curso,
por dispositivo), consulte os próprios dados — isso faz parte do diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Coordenação: mensagens de agosto de 2026, por curso

Cerca de **50 vezes por dia**. Hoje: cerca de **17 ms**.

```sql
SELECT curso_id, count(*) AS mensagens
  FROM mensagem_forum
 WHERE date_trunc('month', postado_em) = TIMESTAMP '2026-08-01'
 GROUP BY curso_id
 ORDER BY mensagens DESC;
```

### Q2. Tutoria: histórico de mensagens de um aluno

Cerca de **3 mil vezes por dia**. Hoje: cerca de **12 ms**.

```sql
SELECT id, curso_id, postado_em, situacao, left(texto, 60) AS trecho
  FROM mensagem_forum
 WHERE aluno_id = 4321
 ORDER BY postado_em DESC;
```

### Q3. Direção: mensagens publicadas pelo celular, por curso

**Uma vez por semana**, na reunião da direção; os números podem ser os do dia
anterior. Hoje: cerca de **25 ms**.

```sql
SELECT curso_id, count(*) AS mensagens_pelo_celular
  FROM mensagem_forum
 WHERE situacao = 'PUBLICADA'
   AND dispositivo = 'CELULAR'
 GROUP BY curso_id
 ORDER BY curso_id;
```

### Q4. Secretaria: mensagens que falam de boleto vencido

Cerca de **20 vezes por dia**. Hoje: cerca de **68 ms**.

```sql
SELECT id, aluno_id, postado_em, texto
  FROM mensagem_forum
 WHERE texto ILIKE '%boleto vencido%';
```

### Q5. Mural do curso 1, de 20 em 20 mensagens, da mais nova para a mais antiga (esta é a página 1.501)

O mural é a tela mais usada: cerca de **10 mil páginas por dia**. Hoje, a página
1.501 leva cerca de **26 ms**.

```sql
SELECT id, aluno_id, postado_em, left(texto, 60) AS trecho
  FROM mensagem_forum
 WHERE curso_id = 1
 ORDER BY postado_em DESC, id DESC
 LIMIT 20 OFFSET 30000;
```

### Q6. Moderação: quantas mensagens denunciadas há em cada curso

O painel se atualiza sozinho: cerca de **mil vezes por dia**. Hoje: cerca de
**13 ms**.

```sql
SELECT curso_id, count(*) AS denunciadas
  FROM mensagem_forum
 WHERE situacao = 'DENUNCIADA'
 GROUP BY curso_id
 ORDER BY denunciadas DESC;
```

## 5. O que a plataforma espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 8 ms, com o mesmo resultado |
| Q2 | menos de 1 ms |
| Q3 | mais rápida — ou a demonstração, com medições, de que não vale a pena |
| Q4 | menos de 10 ms |
| Q5 | menos de 1 ms, **qualquer que seja a página** |
| Q6 | menos de 1 ms |

E, para cada mudança, a plataforma quer saber **quanto ela custa**: espaço em
disco, efeito nas gravações (o fórum recebe mensagens o dia inteiro) e o
trabalho de manutenção. Uma consulta reescrita precisa devolver **exatamente as
mesmas linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-05
podman compose up -d
until podman exec bd2-a2-g05-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g05-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g05-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-05/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-05/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-05/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-05/consultas.sql>

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
