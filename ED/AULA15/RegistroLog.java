import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Uma linha do arquivo de log: a operação, a chave e o valor.
 *
 * <p>Formato da linha: {@code OPERACAO|chave|valor}. Exemplos:</p>
 *
 * <pre>
 * PUT|produto:001|Lápis preto\|1.20\|300
 * DEL|produto:003|
 * </pre>
 *
 * <p>Como a barra vertical separa os campos, ela é "escapada" dentro da chave e
 * do valor. As trocas feitas na gravação são: {@code \} vira {@code \\},
 * {@code |} vira {@code \|}, quebra de linha vira {@code \n} e retorno de carro
 * vira {@code \r}. A leitura desfaz as trocas. Assim, qualquer texto pode ser
 * chave ou valor sem quebrar o arquivo.</p>
 */
public final class RegistroLog {
    private final TipoOperacao operacao;
    private final String chave;
    private final String valor;

    public RegistroLog(TipoOperacao operacao, String chave, String valor) {
        this.operacao = Objects.requireNonNull(operacao, "operação não pode ser nula");
        this.chave = Objects.requireNonNull(chave, "chave não pode ser nula");
        this.valor = valor == null ? "" : valor;
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

    /** Transforma o registro numa linha de texto, sem a quebra de linha final. */
    public String paraLinha() {
        return operacao.name() + "|" + escapar(chave) + "|" + escapar(valor);
    }

    /**
     * Lê uma linha do log e devolve o registro correspondente.
     *
     * @throws IllegalArgumentException se a linha não tiver os três campos
     *         ou se a operação for desconhecida
     */
    public static RegistroLog deLinha(String linha) {
        List<String> campos = separarCampos(linha);
        if (campos.size() != 3) {
            throw new IllegalArgumentException("linha de log inválida: " + linha);
        }
        TipoOperacao operacao = TipoOperacao.valueOf(campos.get(0));
        return new RegistroLog(operacao, campos.get(1), campos.get(2));
    }

    private static String escapar(String texto) {
        StringBuilder saida = new StringBuilder(texto.length() + 8);
        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            switch (c) {
                case '\\' -> saida.append("\\\\");
                case '|' -> saida.append("\\|");
                case '\n' -> saida.append("\\n");
                case '\r' -> saida.append("\\r");
                default -> saida.append(c);
            }
        }
        return saida.toString();
    }

    /** Divide a linha nas barras que não estão escapadas, desfazendo os escapes. */
    private static List<String> separarCampos(String linha) {
        List<String> campos = new ArrayList<>(3);
        StringBuilder atual = new StringBuilder();
        for (int i = 0; i < linha.length(); i++) {
            char c = linha.charAt(i);
            if (c == '\\' && i + 1 < linha.length()) {
                char seguinte = linha.charAt(++i);
                switch (seguinte) {
                    case 'n' -> atual.append('\n');
                    case 'r' -> atual.append('\r');
                    default -> atual.append(seguinte);   // \\ e \|
                }
            } else if (c == '|') {
                campos.add(atual.toString());
                atual.setLength(0);
            } else {
                atual.append(c);
            }
        }
        campos.add(atual.toString());
        return campos;
    }

    @Override
    public String toString() {
        return paraLinha();
    }
}
