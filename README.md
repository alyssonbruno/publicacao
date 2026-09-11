# Publicação

Arquivos de apoio das disciplinas de TI do Prof. **Alysson M. Bruno** (UNITINS),
publicados para os alunos.

Este repositório é **público de propósito**: é a origem que o
[iximiuz Labs](https://labs.iximiuz.com) lê para montar os ambientes de
laboratório e o endereço dos downloads postados no Google Classroom.

## O que fica aqui

Só o que o aluno precisa receber: scripts de laboratório, esquemas de banco,
dados de exemplo e arquivos de configuração citados em aula.

Gabaritos, provas, respostas de exercício e material de planejamento **não**
entram aqui — ficam no repositório privado da disciplina.

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

## Como os arquivos chegam aqui

Eles não são editados neste repositório. A cópia de trabalho fica no
repositório privado das aulas, e a publicação é feita pelo script
`tools/publicar.sh` de lá, que copia apenas o que está declarado em
`tools/publicacao.lista`.

Editar um arquivo diretamente aqui faz a cópia divergir da original e a
alteração será desfeita na próxima publicação.

## Licença

Material didático de uso livre para fins educacionais, com atribuição.
