# Caso do Grupo 06 — Prefeitura de Serra Azul do Tocantins

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** o município, as pessoas e os números são fictícios.

## 1. A prefeitura

**Serra Azul do Tocantins** é um município fictício que atende o cidadão por um
sistema de **protocolos**: tapa-buraco, segunda via do IPTU, alvarás, poda de
árvore, matrícula escolar, iluminação pública, ouvidoria. Cada protocolo é
aberto por um servidor, fica sob a responsabilidade de outro servidor e passa
por andamentos (protocolo geral, setor técnico, gabinete) até ser concluído.

De janeiro de 2025 a setembro de 2026 foram 400 mil protocolos e 1,2 milhão de
andamentos. O sistema foi contratado por licitação e o banco nunca foi revisado.
O seu grupo foi chamado para descobrir o que está lento, consertar o que der e
explicar o que não vale a pena consertar.

## 2. Do que a prefeitura reclama

- "No portal do cidadão, a tramitação de um protocolo demora para aparecer."
- "O gabinete do prefeito quer o ranking dos protocolos mais demorados de cada
  secretaria, e ele é lento — o servidor até grava arquivos temporários."
- "O painel do servidor, com o que ele registrou e o que está com ele, demora."
- "O relatório de arrecadação da Secretaria de Cultura usa índice e mesmo assim
  é lento."
- "A tela inicial do atendimento, com os últimos protocolos, demora a cada
  atualização."
- "O Meio Ambiente lista os pedidos de poda pendentes por bairro. Já existe um
  índice com a coluna `servico`, mas a consulta não fica rápida."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `cidadao` | 50 mil | os cidadãos cadastrados |
| `servidor` | 2 mil | os servidores municipais |
| `protocolo` | 400 mil | os protocolos, abertos de 01/01/2025 a 30/09/2026 |
| `andamento` | 1,2 milhão | três andamentos por protocolo |

```sql
CREATE TABLE cidadao (
    id      integer      PRIMARY KEY,
    nome    varchar(60)  NOT NULL,
    cpf     char(11)     NOT NULL UNIQUE,
    bairro  varchar(30)  NOT NULL
);

CREATE TABLE servidor (
    id          integer      PRIMARY KEY,
    nome        varchar(60)  NOT NULL,
    secretaria  varchar(20)  NOT NULL
);

CREATE TABLE protocolo (
    id              integer       PRIMARY KEY,
    numero          varchar(11)   NOT NULL UNIQUE,  -- ano/número: 2026/271828
    cidadao_id      integer       NOT NULL REFERENCES cidadao,
    servico         varchar(25)   NOT NULL,         -- 18 serviços, ex.: PODA_ARVORE
    secretaria      varchar(20)   NOT NULL,         -- 8 secretarias; vem do serviço
    bairro          varchar(30)   NOT NULL,
    aberto_em       timestamp     NOT NULL,
    concluido_em    timestamp,                      -- vazio se ainda não concluído
    situacao        varchar(15)   NOT NULL,         -- CONCLUIDO, EM_ANDAMENTO, ABERTO,
                                                    -- INDEFERIDO, CANCELADO
    aberto_por      integer       NOT NULL REFERENCES servidor,
    responsavel_id  integer       NOT NULL REFERENCES servidor,
    taxa            numeric(8,2)  NOT NULL          -- taxa paga (0 se gratuito)
);

CREATE TABLE andamento (
    id            integer      PRIMARY KEY,
    protocolo_id  integer      NOT NULL REFERENCES protocolo,
    data_hora     timestamp    NOT NULL,
    setor         varchar(30)  NOT NULL,
    descricao     varchar(80)  NOT NULL
);
```

**Índices que já existem:** as chaves primárias, os índices únicos de
`protocolo.numero` e `cidadao.cpf` e um índice criado para a tela de consulta
por secretaria:

```sql
CREATE INDEX idx_protocolo_secretaria_servico ON protocolo (secretaria, servico);
```

Os protocolos são gravados no momento em que são abertos, então ficam na tabela
em ordem de data. Cada serviço pertence a uma única secretaria. Para entender
como os valores se distribuem (quantos protocolos há por serviço, por situação),
consulte os próprios dados — isso faz parte do diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Portal do cidadão: a tramitação de um protocolo, pelo número

Cerca de **5 mil vezes por dia**. Hoje: cerca de **40 ms**.

```sql
SELECT p.numero, p.servico, p.situacao, a.data_hora, a.setor, a.descricao
  FROM protocolo p
  JOIN andamento a ON a.protocolo_id = p.id
 WHERE p.numero = '2026/271828'
 ORDER BY a.data_hora;
```

### Q2. Gabinete do prefeito: os 5 protocolos concluídos que mais demoraram, em cada secretaria

Cerca de **20 vezes por dia**; dados de até **uma hora atrás** são aceitáveis.
Hoje: cerca de **310 ms**, gravando arquivos temporários.

```sql
SELECT secretaria, numero, servico, duracao, posicao
  FROM (SELECT secretaria, numero, servico,
               concluido_em - aberto_em AS duracao,
               rank() OVER (PARTITION BY secretaria
                            ORDER BY concluido_em - aberto_em DESC) AS posicao
          FROM protocolo
         WHERE concluido_em IS NOT NULL) AS t
 WHERE posicao <= 5
 ORDER BY secretaria, posicao;
```

### Q3. Painel do servidor: protocolos que ele registrou ou pelos quais é responsável

Cerca de **2 mil vezes por dia**. Hoje: cerca de **15 ms**.

```sql
SELECT id, numero, servico, situacao, aberto_em, aberto_por, responsavel_id
  FROM protocolo
 WHERE aberto_por = 56 OR responsavel_id = 56
 ORDER BY aberto_em DESC;
```

### Q4. Secretaria de Cultura: quanto cada serviço arrecadou em taxas

Cerca de **100 vezes por dia**. Hoje: cerca de **9 ms** e cerca de **5.600
páginas** lidas.

```sql
SELECT servico, count(*) AS pedidos, sum(taxa) AS arrecadado
  FROM protocolo
 WHERE secretaria = 'CULTURA'
 GROUP BY servico
 ORDER BY servico;
```

### Q5. Tela inicial do atendimento: os 50 protocolos abertos mais recentemente

A tela se atualiza a cada 30 segundos em 40 guichês: cerca de **40 mil vezes por
dia**. Hoje: cerca de **57 ms**.

```sql
SELECT p.numero, p.aberto_em, p.servico, c.nome AS cidadao
  FROM protocolo p
  JOIN cidadao c ON c.id = p.cidadao_id
 ORDER BY p.aberto_em DESC
 LIMIT 50;
```

### Q6. Meio Ambiente: pedidos de poda de árvore ainda não concluídos, por bairro

Cerca de **200 vezes por dia**. Hoje: cerca de **4 ms** e cerca de **2.100
páginas** lidas para devolver dez linhas.

```sql
SELECT bairro, count(*) AS pedidos_pendentes
  FROM protocolo
 WHERE servico = 'PODA_ARVORE'
   AND situacao <> 'CONCLUIDO'
 GROUP BY bairro
 ORDER BY pedidos_pendentes DESC;
```

## 5. O que a prefeitura espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | menos de 1 ms |
| Q2 | menos de 50 ms e sem gravar arquivos temporários, com o mesmo resultado |
| Q3 | menos de 2 ms |
| Q4 | menos de 3 ms e menos de 200 páginas lidas |
| Q5 | menos de 2 ms |
| Q6 | menos de 0,5 ms e menos de 50 páginas lidas |

Ajustar parâmetros do PostgreSQL é permitido, desde que o grupo **teste primeiro
na sessão** (`SET`) e discuta o risco de mudar o servidor inteiro. O índice
antigo pode ser mantido, trocado ou apagado — desde que o grupo meça o efeito em
todas as consultas que o usam. Para cada mudança, a prefeitura quer saber
**quanto ela custa**: espaço em disco, efeito nas gravações e o trabalho de
manutenção. Uma consulta reescrita precisa devolver **exatamente as mesmas
linhas** que a original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-06
podman compose up -d
until podman exec bd2-a2-g06-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g06-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g06-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-06/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-06/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-06/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-06/consultas.sql>

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
3. Para cada plano, pergunte: qual nó gasta mais tempo? Quantas páginas cada nó
   lê (inclusive os nós de índice) para devolver quantas linhas? Aparece
   `Disk:` ou `external merge`?
4. Escreva uma hipótese por consulta e teste **uma mudança de cada vez**.
