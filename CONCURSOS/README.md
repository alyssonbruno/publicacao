# Concursos

Materiais de estudo e enunciados de simulados do sistema de apoio a concursos, um diretório por
concurso (`<concurso>/`). Diferente das pastas das disciplinas, estes arquivos **não** vêm do
`tools/publicar.sh` das aulas: são escritos pelos agentes do repositório privado `concursos` e
revisados por pull request (ADR-0004 de lá).

- `<concurso>/diagnostico.yaml`: o simulado de diagnóstico, só com os enunciados.
- `<concurso>/<disciplina>/<tópico>/`: materiais (`materiais.yaml` e textos `.md`) e simulados de
  treino (`simulado-NN.yaml`).

Os gabaritos ficam no repositório privado: o diagnóstico só mede o que o aluno sabe se ele não
vir as respostas antes.
