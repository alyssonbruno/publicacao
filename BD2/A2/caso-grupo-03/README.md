# Caso do Grupo 03 — Entrega Já Tocantins (logística)

> Avaliação A2 de Banco de Dados II — 2026/2 — Prof. Me. Alysson Martins Bruno.
> **Estudo de caso hipotético:** a empresa, as pessoas e os números são fictícios.

## 1. A empresa

A **Entrega Já Tocantins** é uma transportadora de encomendas que atende lojas e
distribuidoras e entrega em doze cidades do Tocantins. Cada encomenda tem um
código de rastreio e passa por três eventos: postada, em trânsito e saiu para
entrega. Dez grandes clientes respondem por quase um terço das entregas; os
outros vinte mil remetentes são lojas pequenas.

Desde janeiro de 2025 foram 400 mil entregas e mais de um milhão de eventos de
rastreio. O banco recebeu, ao longo do tempo, alguns índices criados por um
antigo analista, sem nenhum registro do motivo. O seu grupo foi contratado para
descobrir o que está lento, consertar o que der e explicar o que não vale a pena
consertar.

## 2. Do que a transportadora reclama

- "O cliente digita o código de rastreio no site e espera."
- "O portal das lojas, que mostra os eventos das entregas, está lento."
- "O nosso maior cliente reclama que a fatura dele demora para abrir — e a
  consulta até usa índice."
- "O relatório de atrasos do mês é lento."
- "O painel de produtividade do RH roda a mesma consulta para cada um dos 500
  entregadores e demora para montar."
- "A diretoria quer o relatório de entregas por cidade mais rápido."

## 3. O banco de dados

| Tabela | Linhas | O que guarda |
|---|---:|---|
| `remetente` | 20 mil | as empresas que enviam encomendas |
| `entrega` | 400 mil | as entregas, postadas de 01/01/2025 a 30/09/2026 |
| `evento_rastreio` | 1,2 milhão | três eventos por entrega |

```sql
CREATE TABLE remetente (
    id            integer      PRIMARY KEY,
    razao_social  varchar(60)  NOT NULL,
    cnpj          char(14)     NOT NULL UNIQUE,
    cidade        varchar(25)  NOT NULL
);

CREATE TABLE entrega (
    id               integer       PRIMARY KEY,
    codigo_rastreio  varchar(13)   NOT NULL,   -- ex.: TO123456789BR
    remetente_id     integer       NOT NULL REFERENCES remetente,
    entregador_id    integer       NOT NULL,   -- matrícula do entregador (1 a 500)
    cidade_destino   varchar(25)   NOT NULL,   -- doze cidades do Tocantins
    cep_destino      varchar(8)    NOT NULL,
    endereco         varchar(80)   NOT NULL,
    data_postagem    date          NOT NULL,
    prazo            date          NOT NULL,   -- data prometida ao cliente
    entregue_em      date,                     -- vazio se ainda não entregue
    situacao         varchar(20)   NOT NULL,   -- ENTREGUE, EM_ROTA, AGUARDANDO_RETIRADA,
                                               -- DEVOLVIDA, EXTRAVIADA
    peso_kg          numeric(6,2)  NOT NULL,
    valor_frete      numeric(8,2)  NOT NULL
);

CREATE TABLE evento_rastreio (
    id           integer      PRIMARY KEY,
    entrega_id   integer      NOT NULL REFERENCES entrega,
    data_hora    timestamp    NOT NULL,
    tipo_evento  varchar(20)  NOT NULL,
    local        varchar(25)  NOT NULL
);
```

**Índices que já existem:** as chaves primárias, o índice único de
`remetente.cnpj` e dois índices criados pelo antigo analista:

```sql
CREATE INDEX idx_entrega_remetente           ON entrega (remetente_id);
CREATE INDEX idx_entrega_postagem_entregador ON entrega (data_postagem, entregador_id);
```

As entregas são gravadas no dia da postagem, então ficam na tabela em ordem de
data. Para entender como os valores se distribuem (quantas entregas há em cada
situação, quantas tem cada remetente), consulte os próprios dados — isso faz
parte do diagnóstico.

## 4. As seis consultas lentas

Estão no arquivo `consultas.sql`, exatamente como o sistema as envia. Os tempos
"hoje" foram medidos no ambiente da A2 (iximiuz Labs) e servem só de
referência: na sua máquina podem ser outros.

### Q1. Diretoria: entregas concluídas por cidade e tempo médio, em dias

Cerca de **10 vezes por dia**, e precisa mostrar as entregas **até aquele
momento**. Hoje: cerca de **28 ms**.

```sql
SELECT cidade_destino,
       count(*)                                  AS entregues,
       round(avg(entregue_em - data_postagem), 1) AS dias_em_media
  FROM entrega
 WHERE situacao = 'ENTREGUE'
 GROUP BY cidade_destino
 ORDER BY entregues DESC;
```

### Q2. Site: o cliente digita o código de rastreio

Cerca de **50 mil vezes por dia**. Hoje: cerca de **14 ms**.

```sql
SELECT id, codigo_rastreio, cidade_destino, data_postagem, prazo,
       entregue_em, situacao
  FROM entrega
 WHERE codigo_rastreio = 'TO491138101BR';
```

### Q3. Financeiro: fatura mensal do maior cliente

O próprio cliente abre o painel de faturamento cerca de **200 vezes por dia**.
Hoje: cerca de **13 ms** e quase **5.800 páginas** lidas.

```sql
SELECT date_trunc('month', data_postagem)::date AS mes,
       count(*)                                 AS entregas,
       sum(valor_frete)                         AS frete
  FROM entrega
 WHERE remetente_id = 3
 GROUP BY 1
 ORDER BY 1;
```

### Q4. Portal do remetente: os eventos de rastreio das entregas de uma loja

Cerca de **2 mil vezes por dia**. Hoje: cerca de **39 ms**.

```sql
SELECT e.codigo_rastreio, ev.data_hora, ev.tipo_evento, ev.local
  FROM entrega e
  JOIN evento_rastreio ev ON ev.entrega_id = e.id
 WHERE e.remetente_id = 4321
 ORDER BY e.codigo_rastreio, ev.data_hora;
```

### Q5. Qualidade: entregas de abril de 2026 que chegaram depois do prazo, por cidade

Cerca de **10 vezes por dia**. Hoje: cerca de **26 ms**.

```sql
SELECT cidade_destino, count(*) AS atrasadas
  FROM entrega
 WHERE to_char(data_postagem, 'YYYY-MM') = '2026-04'
   AND entregue_em > prazo
 GROUP BY cidade_destino
 ORDER BY atrasadas DESC;
```

### Q6. Recursos humanos: produtividade mensal de um entregador em 2026

O painel roda esta consulta **para cada um dos 500 entregadores** sempre que é
aberto, umas 40 vezes por dia (20 mil execuções). Hoje, cada execução leva cerca
de **2 ms** e lê cerca de **400 páginas** para devolver nove linhas.

```sql
SELECT date_trunc('month', data_postagem)::date AS mes,
       count(*)                                 AS entregas
  FROM entrega
 WHERE entregador_id = 57
   AND data_postagem BETWEEN DATE '2026-01-01' AND DATE '2026-09-30'
 GROUP BY 1
 ORDER BY 1;
```

## 5. O que a transportadora espera

| Consulta | Meta (no ambiente da A2) |
|---|---|
| Q1 | mais rápida — ou a demonstração, com medições, de que não vale a pena |
| Q2 | menos de 1 ms |
| Q3 | menos de 5 ms e menos de 500 páginas lidas |
| Q4 | menos de 1 ms |
| Q5 | menos de 5 ms, com o mesmo resultado |
| Q6 | menos de 0,5 ms e menos de 20 páginas lidas |

E, para cada mudança, a transportadora quer saber **quanto ela custa**: espaço
em disco, efeito nas gravações (são centenas de entregas e milhares de eventos
por dia) e o trabalho de manutenção. Os dois índices antigos podem ser
mantidos, trocados ou apagados — desde que o grupo meça e justifique. Uma
consulta reescrita precisa devolver **exatamente as mesmas linhas** que a
original.

## 6. Como montar o ambiente

**No iximiuz Labs** (ambiente da A2: <https://labs.iximiuz.com/playgrounds/BD2-A2-af942709>):

```bash
cd ~/a2/caso-grupo-03
podman compose up -d
until podman exec bd2-a2-g03-pg pg_isready -h localhost -q; do sleep 2; done
podman exec -i bd2-a2-g03-pg psql -U aluno -d banco < preparar.sql
podman exec -it bd2-a2-g03-pg psql -U aluno -d banco
```

**No Podman Desktop**, baixe os quatro arquivos para uma pasta e rode os mesmos
comandos dentro dela:

- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-03/README.md>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-03/compose.yaml>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-03/preparar.sql>
- <https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/A2/caso-grupo-03/consultas.sql>

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
