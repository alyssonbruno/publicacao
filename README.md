# Publicação

Arquivos de apoio das disciplinas de TI do Prof. **Alysson M. Bruno** (UNITINS) e
do grupo de estudos para concursos públicos, publicados para os alunos.

Este repositório é **público de propósito**: é a origem que o
[iximiuz Labs](https://labs.iximiuz.com) lê para montar os ambientes de
laboratório e o endereço dos downloads postados no Google Classroom.

## O que fica aqui

Só o que o aluno precisa receber:

- **disciplinas** (`BD1/`, `BD2/`, `ED/`...): scripts de laboratório, esquemas de
  banco, dados de exemplo e arquivos de configuração citados em aula;
- **concursos** (`CONCURSOS/`): materiais de estudo e enunciados dos simulados de
  cada concurso, lidos pelo sistema de estudos do grupo.

Gabaritos, provas, respostas de exercício e material de planejamento **não**
entram aqui — ficam no repositório privado de origem.

## Estrutura de Dados — como usar

Os arquivos `.java` de cada aula são **autossuficientes**: cada um reúne todas as
classes de que precisa e não declara `package`. Para executar:

1. abra <https://www.jdoodle.com/online-java-compiler-ide>;
2. escolha a linguagem **Java** e a versão **JDK 25**;
3. apague o exemplo que aparece na tela;
4. cole **um arquivo inteiro**;
5. clique em **Execute**.

Funciona no computador e também no navegador do celular. O `README.md` de cada
aula diz o que cada programa demonstra e traz os desafios.

A Aula 07 tem também uma página que executa os algoritmos de ordenação **passo a
passo**, mostrando a linha do Java e o vetor mudando ao mesmo tempo:

<https://alyssonbruno.github.io/publicacao/ED/AULA07/visualizador-ed-aula07.html>

## Organização

```
<DISCIPLINA>/<AULA>/<arquivo>
```

Por exemplo, o esquema do laboratório da Aula 05 de Banco de Dados II:

```
BD2/AULA05/01_esquema.sql
```

O endereço direto (`raw`) desse arquivo é:

```
https://raw.githubusercontent.com/alyssonbruno/publicacao/main/BD2/AULA05/01_esquema.sql
```

O endereço `raw` serve para **baixar** um arquivo. Ele entrega tudo como texto
puro, então uma página `.html` aberta por ele aparece como código-fonte. Para
abrir uma página como página, use o endereço do GitHub Pages:

```
https://alyssonbruno.github.io/publicacao/ED/AULA07/visualizador-ed-aula07.html
```

Nos concursos, a organização é por concurso, disciplina e tópico:

```
CONCURSOS/<concurso>/diagnostico.yaml
CONCURSOS/<concurso>/<disciplina>/<tópico>/materiais.yaml
```

## Como os arquivos chegam aqui

Cada parte tem a sua origem:

- **Disciplinas:** a cópia de trabalho fica no repositório privado das aulas, e a
  publicação é feita pelo script `tools/publicar.sh` de lá, que copia apenas o
  que está declarado em `tools/publicacao.lista`. Editar um desses arquivos
  diretamente aqui faz a cópia divergir da original, e a alteração será desfeita
  na próxima publicação.
- **Concursos:** a pasta `CONCURSOS/` é escrita pelos agentes do sistema de
  estudos, cujo repositório privado guarda os gabaritos, e muda só por pull
  request. O `publicar.sh` não a toca. Detalhes em
  [`CONCURSOS/README.md`](CONCURSOS/README.md).

Este `README.md` também vem das aulas (`tools/publicacao-README.md`): para
mudá-lo, mude lá e publique.

## Licença

Material didático de uso livre para fins educacionais, com atribuição.
