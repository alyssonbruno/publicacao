# Ilustrações sob demanda

O workflow `.github/workflows/ilustracoes.yml` gera, com IA, as ilustrações das aulas dos
concursos a partir de prompts escritos ao lado do texto. É o mesmo desenho do workflow de
ilustrações do repositório da obra (another-isekai): os mesmos geradores, o mesmo manifesto com
o hash do prompt e a mesma regra de só rodar quando alguém pede.

## Onde fica cada coisa

```text
CONCURSOS/<concurso>/<disciplina>/<tópico>/
├── texto.md                    cita ![descrição](imagens/abertura.webp)
└── imagens/
    ├── abertura.prompt.md      o prompt: um bloco ```text e, se quiser, "Proporção: 21:9"
    ├── abertura.webp           a imagem, gerada pelo workflow
    ├── geradas.json            o hash do prompt de cada imagem gerada
    └── tres-erros.svg          figura desenhada (SVG), sem prompt
```

- O estilo comum a todas as ilustrações está em [`estilo.md`](estilo.md) e vai antes de todo
  prompt.
- **IA só para ilustração sem texto** (a abertura da aula, uma cena). Gráfico, tabela, esquema
  ou qualquer coisa com palavras e números é **SVG feito à mão**: o gerador erra números e letras.
- Enquanto a imagem não é gerada, o validador do sistema a dá como pendente e o painel não a
  mostra.

## Como rodar

1. Configure uma chave em *Settings → Secrets and variables → Actions*: `GEMINI_API_KEY`
   (Gemini, o padrão) e/ou `OPENAI_API_KEY`. A variável `IMAGENS_GERADOR` (opcional) escolhe
   `gemini` ou `openai` quando as duas existem.
2. Em *Actions → Ilustrações sob demanda → Run workflow*, escolha **o branch do pull request**
   que trouxe os prompts. As imagens entram como um commit nesse branch e são revisadas junto.
   Rodado no `main`, as imagens vão para um branch novo (`ilustracoes/<número>`), e o resumo do
   job traz o link para abrir o pull request.
3. Campos: a pasta (padrão `CONCURSOS`), o teto de imagens (padrão 10), `refazer` e o gerador.
   Vazio quer dizer gerar o que falta ou mudou.

Para ver o que seria gerado sem gastar nada:

```bash
python3 .github/ilustracoes/ilustrar.py --simular
```

## O que sai

WebP de até 1600 px de largura e menos de 900 kB (o sistema recusa imagem acima de 1 MB),
recortado na proporção do prompt (padrão 16:9; 21:9 para as aberturas). Sem Git LFS: o servidor
do sistema lê uma cópia simples deste repositório, e as imagens são poucas e pequenas.

## Quando der errado

| sintoma | causa provável | conserto |
| --- | --- | --- |
| aviso de chave ausente | nenhum secret de gerador | configure `GEMINI_API_KEY` ou `OPENAI_API_KEY` |
| "a cota do gerador acabou" | cota do dia ou faturamento | aguarde, ative o faturamento ou troque o gerador |
| recusada pelo gerador | filtro de conteúdo | mude o prompt; a recusa fica anotada até ele mudar |
| imagens geradas e push falhou | concorrência no branch | baixe o artefato `ilustracoes-<n>` (30 dias) |
| mudou o estilo e nada aconteceu | esperado: o hash é só do prompt | rode com `refazer` |
