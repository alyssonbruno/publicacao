/**
 * Representa uma linha do arquivo de log.
 */
public class RegistroLog {
    private final TipoOperacao operacao;
    private final String chave;
    private final String valor;

    public RegistroLog(TipoOperacao operacao, String chave, String valor) {
        this.operacao = operacao;
        this.chave = chave;
        this.valor = valor;
    }

    public TipoOperacao getOperacao() {
        return operacao;
    }

    public String getChave() {
        return chave;
    }

    public String getValor() {
        return valor;
    }

    public String paraLinha() {
        String valorSeguro = valor == null ? "" : valor.replace("\n", "\\n").replace("|", "\\|");
        return operacao + "|" + chave + "|" + valorSeguro;
    }

    public static RegistroLog deLinha(String linha) {
        String[] partes = linha.split("(?<!\\\\)\\|", 3);
        if (partes.length < 2) {
            throw new IllegalArgumentException("Linha de log invalida: " + linha);
        }

        TipoOperacao operacao = TipoOperacao.valueOf(partes[0]);
        String chave = partes[1];
        String valor = partes.length == 3 ? partes[2].replace("\\|", "|").replace("\\n", "\n") : "";

        return new RegistroLog(operacao, chave, valor);
    }

    @Override
    public String toString() {
        return paraLinha();
    }
}
