#!/usr/bin/env python3
"""Gera as ilustrações dos materiais dos concursos a partir dos prompts.

Baseado no `build/ilustrar.py` do repositório da obra (another-isekai): os mesmos geradores
(Gemini e OpenAI), a mesma separação entre recusa, falha e cota, o mesmo manifesto com o hash
do prompt. Aqui, cada ilustração mora ao lado do texto que a usa:

    CONCURSOS/<c>/<d>/<t>/imagens/abertura.prompt.md   o prompt (um bloco ```text)
    CONCURSOS/<c>/<d>/<t>/imagens/abertura.webp        a imagem, gerada aqui
    CONCURSOS/<c>/<d>/<t>/imagens/geradas.json         o hash do prompt de cada imagem

O texto da aula cita a imagem (`![descrição](imagens/abertura.webp)`); enquanto ela não
existe, o validador do sistema a dá como pendente e o painel não a mostra.

O que ele decide sozinho:

1. QUEM PRECISA: o prompt sem imagem ao lado e o prompt que mudou depois de a imagem ser
   gerada (o hash em `geradas.json`). Recusa do filtro fica anotada e não se tenta de novo até
   o prompt mudar. Imagem posta à mão é respeitada.
2. O ESTILO: o bloco de `.github/ilustracoes/estilo.md` vai antes de todo prompt, para as
   aulas terem a mesma cara. Mudar o estilo não regera nada sozinho: use `--refazer`.
3. O GERADOR: `--gerador=`, ou o primeiro cuja chave esteja no ambiente (GEMINI_API_KEY ou
   OPENAI_API_KEY). A chave não é impressa em lugar nenhum.
4. QUANDO PARAR: 429 e 5xx esperam e tentam de novo, menos a cota esgotada, que encerra a
   rodada (o que vem depois falharia igual).

A imagem sai em WebP, com até 1600 px de largura e menos de 900 kB (o sistema recusa imagem
acima de 1 MB), recortada na proporção do prompt (linha `Proporção: 21:9`; padrão 16:9).

Uso:
    python3 .github/ilustracoes/ilustrar.py --simular       # só lista; não gasta nada
    python3 .github/ilustracoes/ilustrar.py                 # tudo o que falta ou mudou
    python3 .github/ilustracoes/ilustrar.py --pasta=CONCURSOS/cau-to-2026-assistente-administrativo
    python3 .github/ilustracoes/ilustrar.py --refazer --maximo=3 --gerador=openai

Opções:
    --pasta=CAMINHO      onde procurar os prompts (padrão CONCURSOS)
    --maximo=N           teto de imagens nesta rodada (padrão 10; 0 = sem teto)
    --gerador=NOME       gemini | openai (padrão: o primeiro com chave)
    --modelo=NOME        o modelo do gerador (padrões em GERADORES)
    --refazer            gera de novo mesmo quem já tem imagem atual
    --simular            mostra o plano e sai; não precisa de chave

Saída: as imagens, os `geradas.json` atualizados a cada acerto (uma queda no meio não perde o
que já foi feito) e o relatório, no resumo do job (dentro do Actions) ou no terminal.
Código 1 quando alguma chamada falhou, foi recusada, ou a cota acabou.
"""

import base64
import datetime
import hashlib
import io
import json
import os
import re
import sys
import time
import urllib.error
import urllib.request
from dataclasses import dataclass
from pathlib import Path

RAIZ = Path(__file__).resolve().parents[2]
ESTILO = Path(__file__).resolve().parent / "estilo.md"
SUFIXO = ".prompt.md"
MANIFESTO = "geradas.json"
LARGURA_MAXIMA = 1600
BYTES_MAXIMOS = 900_000
PROPORCOES = ("16:9", "21:9", "4:3", "3:2", "1:1")

GERADORES = {
    "gemini": {
        "variavel": "GEMINI_API_KEY",
        "modelo": "gemini-2.5-flash-image",
        "url": os.environ.get(
            "ILUSTRAR_URL_GEMINI", "https://generativelanguage.googleapis.com/v1beta/models/"
        ),
    },
    "openai": {
        "variavel": "OPENAI_API_KEY",
        "modelo": "gpt-image-1",
        "url": os.environ.get(
            "ILUSTRAR_URL_OPENAI", "https://api.openai.com/v1/images/generations"
        ),
    },
}
TENTATIVAS = 4  # 429 e 5xx: espera 5, 10 e 20 s e tenta de novo
ESPERA_MAXIMA = 60  # espera pedida acima disto é cota, não fila
TEMPO_LIMITE = 300  # segundos por chamada


class Recusa(Exception):
    """O gerador respondeu, mas não quis desenhar: filtro de conteúdo. O conserto é no prompt."""


class Falha(Exception):
    """A chamada não deu certo depois de todas as tentativas."""


class Cota(Exception):
    """A cota do gerador acabou: encerra a rodada."""


@dataclass
class Alvo:
    prompt: Path  # .../imagens/abertura.prompt.md
    nome: str  # abertura
    texto: str  # o bloco do prompt
    proporcao: str

    @property
    def pasta(self) -> Path:
        return self.prompt.parent

    @property
    def imagem(self) -> Path:
        return self.pasta / f"{self.nome}.webp"

    @property
    def rotulo(self) -> str:
        return str(self.pasta.relative_to(RAIZ) / self.nome)

    @property
    def hash(self) -> str:
        conteudo = f"{self.proporcao}\n{self.texto}".encode()
        return "sha256:" + hashlib.sha256(conteudo).hexdigest()[:16]


# ------------------------------------------------------------------ os prompts


def _bloco(texto: str) -> str:
    """O primeiro bloco cercado (```text ... ```) do arquivo: o prompt."""
    achado = re.search(r"^```[^\n]*\n(.*?)\n```", texto, re.DOTALL | re.MULTILINE)
    return " ".join(achado.group(1).split()) if achado else ""


def _proporcao(texto: str) -> str:
    achado = re.search(r"^Propor[çc][ãa]o:\s*(\d+:\d+)\s*$", texto, re.MULTILINE | re.IGNORECASE)
    return achado.group(1) if achado else "16:9"


def alvos(pasta: Path) -> tuple[list[Alvo], list[tuple[Path, str]]]:
    """Os prompts da pasta, em ordem, e os que têm problema (sem bloco, proporção estranha)."""
    lista, problemas = [], []
    for arquivo in sorted(pasta.rglob(f"imagens/*{SUFIXO}")):
        conteudo = arquivo.read_text(encoding="utf-8")
        texto, proporcao = _bloco(conteudo), _proporcao(conteudo)
        if not texto:
            problemas.append((arquivo, "sem o bloco ```text com o prompt"))
        elif proporcao not in PROPORCOES:
            problemas.append((arquivo, f"proporção {proporcao}: use {', '.join(PROPORCOES)}"))
        else:
            lista.append(Alvo(arquivo, arquivo.name[: -len(SUFIXO)], texto, proporcao))
    return lista, problemas


def ler_manifesto(pasta: Path) -> dict:
    arquivo = pasta / MANIFESTO
    return json.loads(arquivo.read_text(encoding="utf-8")) if arquivo.exists() else {}


def gravar_manifesto(pasta: Path, manifesto: dict) -> None:
    if manifesto:
        ordenado = {chave: manifesto[chave] for chave in sorted(manifesto)}
        (pasta / MANIFESTO).write_text(
            json.dumps(ordenado, ensure_ascii=False, indent=1) + "\n", encoding="utf-8"
        )


def planejar(lista: list[Alvo], refazer: bool) -> tuple[list[tuple[Alvo, str]], list[str]]:
    """O que gerar (com o motivo) e os avisos do que fica de fora até o prompt mudar."""
    plano, avisos = [], []
    for alvo in lista:
        manifesto = ler_manifesto(alvo.pasta)
        entrada = manifesto.get(alvo.nome, {})
        if refazer:
            plano.append((alvo, "refazer"))
        elif not alvo.imagem.exists():
            if entrada.get("recusa") and entrada.get("prompt") == alvo.hash:
                avisos.append(f"{alvo.rotulo}: recusada pelo gerador; mude o prompt")
            else:
                plano.append((alvo, "falta"))
        elif not entrada:
            # Imagem posta à mão: fica, e o hash entra para uma mudança futura ser vista.
            manifesto[alvo.nome] = {"prompt": alvo.hash, "modelo": "à mão"}
            gravar_manifesto(alvo.pasta, manifesto)
        elif entrada.get("prompt") != alvo.hash:
            plano.append((alvo, "mudou"))
    return plano, avisos


# ------------------------------------------------------------------ os geradores


def _uma_linha(texto: object) -> str:
    return " ".join(str(texto).split())[:240]


def _espera_pedida(erro: object) -> float | None:
    """Os segundos de `RetryInfo.retryDelay` (limite por minuto); sem ele, é a cota do dia."""
    for detalhe in (erro.get("details") or []) if isinstance(erro, dict) else []:
        if isinstance(detalhe, dict) and "RetryInfo" in str(detalhe.get("@type", "")):
            achado = re.match(r"(\d+(?:\.\d+)?)s", str(detalhe.get("retryDelay", "")))
            if achado:
                return float(achado.group(1))
    return None


def _post(url: str, corpo: dict, cabecalhos: dict) -> dict:
    """POST em JSON com nova tentativa em 429 e 5xx. Nada aqui imprime os cabeçalhos (a chave)."""
    dados = json.dumps(corpo).encode()
    ultimo = None
    for tentativa in range(TENTATIVAS):
        pedido = urllib.request.Request(url, data=dados, method="POST")
        for nome, valor in cabecalhos.items():
            pedido.add_header(nome, valor)
        pedido.add_header("Content-Type", "application/json")
        try:
            with urllib.request.urlopen(pedido, timeout=TEMPO_LIMITE) as resposta:
                return json.loads(resposta.read().decode())
        except urllib.error.HTTPError as erro_http:
            texto = erro_http.read().decode("utf-8", "replace")
            try:
                erro = json.loads(texto).get("error") or {}
            except ValueError:
                erro = {}
            codigo = erro.get("code") if isinstance(erro, dict) else None
            mensagem = erro.get("message") if isinstance(erro, dict) else None
            if str(codigo) in ("moderation_blocked", "content_policy_violation"):
                raise Recusa(mensagem or codigo) from None
            ultimo = f"HTTP {erro_http.code} — {_uma_linha(mensagem or texto)}"
            if erro_http.code not in (429, 500, 502, 503, 504):
                raise Falha(ultimo) from None
            espera = 5 * 2**tentativa
            if erro_http.code == 429:
                pedida = _espera_pedida(erro)
                if pedida is None or pedida > ESPERA_MAXIMA:
                    raise Cota(ultimo) from None
                espera = pedida
        except (urllib.error.URLError, TimeoutError, OSError) as erro_rede:
            ultimo = f"rede — {_uma_linha(erro_rede)}"
            espera = 5 * 2**tentativa
        if tentativa + 1 < TENTATIVAS:
            time.sleep(espera)
    raise Falha(ultimo or "sem resposta")


def gemini(prompt: str, modelo: str, chave: str, proporcao: str) -> bytes:
    corpo = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {
            "responseModalities": ["TEXT", "IMAGE"],
            "imageConfig": {"aspectRatio": proporcao},
        },
    }
    resposta = _post(
        GERADORES["gemini"]["url"] + modelo + ":generateContent", corpo, {"x-goog-api-key": chave}
    )
    candidatos = resposta.get("candidates") or []
    if not candidatos:
        bloqueio = (resposta.get("promptFeedback") or {}).get("blockReason")
        raise Recusa(f"prompt bloqueado: {bloqueio or 'sem candidato'}")
    textos = []
    for parte in (candidatos[0].get("content") or {}).get("parts") or []:
        embutido = parte.get("inlineData") or parte.get("inline_data")
        if embutido and embutido.get("data"):
            return base64.b64decode(embutido["data"])
        if parte.get("text"):
            textos.append(parte["text"].strip())
    motivo = candidatos[0].get("finishReason") or "sem imagem na resposta"
    raise Recusa(motivo + (" — " + " ".join(textos)[:200] if textos else ""))


def openai(prompt: str, modelo: str, chave: str, proporcao: str) -> bytes:
    largura, altura = (int(x) for x in proporcao.split(":"))
    tamanho = "1536x1024" if largura > altura else "1024x1536" if altura > largura else "1024x1024"
    corpo = {"model": modelo, "prompt": prompt, "n": 1, "size": tamanho}
    if modelo.startswith("gpt-image"):
        corpo["quality"] = "medium"
        corpo["output_format"] = "png"
    resposta = _post(GERADORES["openai"]["url"], corpo, {"Authorization": "Bearer " + chave})
    dados = resposta.get("data") or []
    if not dados or not dados[0].get("b64_json"):
        raise Recusa("resposta sem imagem")
    return base64.b64decode(dados[0]["b64_json"])


# ------------------------------------------------------------------ gravar


def salvar(dados: bytes, alvo: Alvo) -> int:
    """Recorta na proporção, reduz até 1600 px e grava em WebP abaixo de 900 kB."""
    from PIL import Image

    imagem = Image.open(io.BytesIO(dados)).convert("RGB")
    largura, altura = (int(x) for x in alvo.proporcao.split(":"))
    alvo_razao = largura / altura
    if abs(imagem.width / imagem.height - alvo_razao) > 0.01:
        if imagem.width / imagem.height > alvo_razao:
            nova = round(imagem.height * alvo_razao)
            esquerda = (imagem.width - nova) // 2
            imagem = imagem.crop((esquerda, 0, esquerda + nova, imagem.height))
        else:
            nova = round(imagem.width / alvo_razao)
            topo = (imagem.height - nova) // 2
            imagem = imagem.crop((0, topo, imagem.width, topo + nova))
    if imagem.width > LARGURA_MAXIMA:
        imagem = imagem.resize(
            (LARGURA_MAXIMA, round(imagem.height * LARGURA_MAXIMA / imagem.width)),
            Image.Resampling.LANCZOS,
        )
    for qualidade in (82, 74, 66, 58, 50):
        saida = io.BytesIO()
        imagem.save(saida, "WEBP", quality=qualidade, method=6)
        if saida.tell() <= BYTES_MAXIMOS:
            break
    provisorio = alvo.imagem.with_suffix(".parcial")
    provisorio.write_bytes(saida.getvalue())
    provisorio.replace(alvo.imagem)
    return saida.tell()


# ------------------------------------------------------------------ main


def _opcoes(argv: list[str]) -> dict | None:
    opcoes = {
        "pasta": "CONCURSOS",
        "maximo": "10",
        "gerador": "",
        "modelo": "",
        "refazer": False,
        "simular": False,
    }
    for argumento in argv:
        if argumento in ("-h", "--help"):
            print(__doc__)
            return None
        nome, _, valor = argumento[2:].partition("=")
        if not argumento.startswith("--") or nome not in opcoes:
            sys.exit(f"opção desconhecida: {argumento} (veja --help)")
        if isinstance(opcoes[nome], bool):
            opcoes[nome] = True
        else:
            opcoes[nome] = valor
    if opcoes["gerador"] and opcoes["gerador"] not in GERADORES:
        sys.exit(f"--gerador= aceita {' | '.join(GERADORES)}")
    return opcoes


def relatorio(linhas: list[str]) -> None:
    """O relatório da rodada, no resumo do job (no Actions) ou no terminal."""
    texto = "\n".join(linhas) + "\n"
    if not os.environ.get("GITHUB_STEP_SUMMARY"):
        print(texto)
    else:
        with open(os.environ["GITHUB_STEP_SUMMARY"], "a", encoding="utf-8") as resumo:
            resumo.write(texto)


def main(argv: list[str]) -> int:
    opcoes = _opcoes(argv)
    if opcoes is None:
        return 0
    pasta = (RAIZ / opcoes["pasta"]).resolve()
    if not pasta.is_relative_to(RAIZ) or not pasta.is_dir():
        sys.exit(f"--pasta={opcoes['pasta']}: não é uma pasta deste repositório")
    estilo = _bloco(ESTILO.read_text(encoding="utf-8")) if ESTILO.exists() else ""
    lista, problemas = alvos(pasta)
    plano, avisos = planejar(lista, bool(opcoes["refazer"]))
    maximo = int(opcoes["maximo"] or 0)
    restantes = max(0, len(plano) - maximo) if maximo else 0
    if maximo:
        plano = plano[:maximo]

    gerador = opcoes["gerador"] or next(
        (nome for nome, g in GERADORES.items() if os.environ.get(g["variavel"])), ""
    )
    modelo = opcoes["modelo"] or (GERADORES[gerador]["modelo"] if gerador else "")
    linhas = [f"# Ilustrações — {datetime.date.today().isoformat()}", ""]
    linhas += [f"Gerador **{gerador or '—'}**, modelo `{modelo or '—'}`.", ""]
    for alvo, motivo in plano:
        print(f"  {alvo.rotulo}  ({motivo}, {alvo.proporcao})")
    for arquivo, problema in problemas:
        avisos.append(f"{arquivo.relative_to(RAIZ)}: {problema}")
    for aviso in avisos:
        print(f"  ⚠ {aviso}")
    if restantes:
        print(f"  … e mais {restantes} na próxima rodada (teto --maximo)")

    if opcoes["simular"] or not plano:
        linhas += [f"- {alvo.rotulo} ({motivo})" for alvo, motivo in plano] or [
            "Nada a gerar: todo prompt tem a sua imagem, e nenhum mudou."
        ]
        linhas += ["", *(f"- ⚠ {aviso}" for aviso in avisos)]
        relatorio(linhas)
        return 1 if problemas else 0
    if not gerador:
        sys.exit(
            "nenhuma chave no ambiente: defina GEMINI_API_KEY ou OPENAI_API_KEY "
            "(no GitHub, em Settings → Secrets; veja .github/ilustracoes/README.md)"
        )
    chave = os.environ[GERADORES[gerador]["variavel"]]

    feitas, erradas, cota = [], [], None
    for indice, (alvo, motivo) in enumerate(plano):
        prompt = f"{estilo}\n\n{alvo.texto}" if estilo else alvo.texto
        print(f"  {alvo.rotulo} …", flush=True)
        manifesto = ler_manifesto(alvo.pasta)
        try:
            gerar = gemini if gerador == "gemini" else openai
            tamanho = salvar(gerar(prompt, modelo, chave, alvo.proporcao), alvo)
            manifesto[alvo.nome] = {
                "arquivo": alvo.imagem.name,
                "prompt": alvo.hash,
                "modelo": modelo,
                "data": datetime.date.today().isoformat(),
            }
            feitas.append(f"| {alvo.rotulo} | {motivo} | {tamanho // 1000} kB |")
            print(f"    → {alvo.imagem.name} ({tamanho // 1000} kB)")
        except Recusa as erro:
            manifesto[alvo.nome] = {
                "prompt": alvo.hash,
                "modelo": modelo,
                "data": datetime.date.today().isoformat(),
                "recusa": _uma_linha(erro)[:200],
            }
            erradas.append(f"| {alvo.rotulo} | recusada: mude o prompt | {_uma_linha(erro)} |")
        except Falha as erro:
            erradas.append(f"| {alvo.rotulo} | falhou | {_uma_linha(erro)} |")
        except Cota as erro:
            cota = f"{_uma_linha(erro)} — {len(plano) - indice} não tentada(s)"
            break
        gravar_manifesto(alvo.pasta, manifesto)
        if indice + 1 < len(plano):
            time.sleep(3)

    if feitas:
        linhas += [
            f"## Geradas ({len(feitas)})",
            "",
            "| imagem | por quê | tamanho |",
            "| --- | --- | --- |",
            *feitas,
            "",
        ]
    if erradas:
        linhas += [
            f"## ❌ Com problema ({len(erradas)})",
            "",
            "| imagem | o quê | detalhe |",
            "| --- | --- | --- |",
            *erradas,
            "",
        ]
    if cota:
        linhas += ["## ❌ A cota do gerador acabou", "", cota, ""]
    if restantes:
        linhas += [f"⏳ {restantes} ficaram para a próxima rodada (teto `maximo`).", ""]
    linhas += [f"- ⚠ {aviso}" for aviso in avisos]
    relatorio(linhas)
    print(
        f"ilustrar — {len(feitas)} gerada(s), {len(erradas)} com problema"
        + (", e a cota acabou" if cota else "")
    )
    return 1 if (erradas or cota or problemas) else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
