import java.util.List;

/**
 * TAD do SGBD chave-valor da A2: diz <em>o que</em> o banco faz, sem dizer
 * <em>como</em> ele guarda os dados.
 *
 * <p>Este é o <strong>núcleo comum</strong>, igual para os sete grupos. Cada grupo
 * cria a própria interface, que estende esta e acrescenta as operações do seu
 * caso. Exemplo:</p>
 *
 * <pre>
 * public interface SGBDBiblioteca extends SGBD {
 *     List&lt;String&gt; listarPorTitulo();
 * }
 * </pre>
 *
 * <p><strong>Invariantes</strong> (valem depois de qualquer operação):</p>
 * <ol>
 *   <li>cada chave tem no máximo um valor atual;</li>
 *   <li>toda alteração ({@code put} ou {@code delete} que remove algo) já está
 *       gravada no log quando o método termina;</li>
 *   <li>fechar e reabrir o banco sobre o mesmo arquivo de log devolve o mesmo
 *       estado de antes de fechar.</li>
 * </ol>
 *
 * <p><strong>Regras de chave e valor</strong>, válidas em todas as operações: a
 * chave não pode ser nula nem vazia (só espaços conta como vazia); o valor não
 * pode ser nulo. Depois de {@link #fechar()}, qualquer operação lança
 * {@link IllegalStateException}.</p>
 */
public interface SGBD extends AutoCloseable {

    /**
     * Salva o valor da chave. Se a chave já existe, atualiza o valor.
     *
     * <p>Pré: chave válida e valor não nulo.<br>
     * Pós: {@code get(chave)} devolve {@code valor}, e a operação já está no log.</p>
     *
     * @param chave chave não nula e não vazia
     * @param valor valor não nulo (pode ser texto vazio)
     * @throws NullPointerException se a chave ou o valor forem nulos
     * @throws IllegalArgumentException se a chave for vazia
     */
    void put(String chave, String valor);

    /**
     * Recupera o valor atual da chave.
     *
     * @param chave chave não nula e não vazia
     * @return o valor atual, ou {@code null} se a chave não existe
     */
    String get(String chave);

    /**
     * Remove a chave e o seu valor.
     *
     * <p>Pós: {@code get(chave)} devolve {@code null}. Se a chave não existia,
     * nada muda e nada é gravado no log.</p>
     *
     * @param chave chave não nula e não vazia
     * @return {@code true} se a chave existia e foi removida
     */
    boolean delete(String chave);

    /**
     * Diz se a chave existe no banco.
     *
     * @param chave chave não nula e não vazia
     * @return {@code true} se há um valor atual para a chave
     */
    default boolean contem(String chave) {
        return get(chave) != null;
    }

    /**
     * Quantidade de chaves guardadas agora.
     *
     * @return número de pares chave-valor
     */
    int tamanho();

    /**
     * Lista as chaves atuais em ordem crescente (a ordem de {@link String#compareTo}).
     *
     * <p>O custo depende da estrutura: numa árvore de busca basta o percurso em
     * ordem; numa tabela hash é preciso ordenar as chaves antes.</p>
     *
     * @return nova lista com as chaves em ordem crescente
     */
    List<String> listarChavesEmOrdem();

    /**
     * Fecha o banco e libera o arquivo de log. Chamar duas vezes não dá erro.
     */
    void fechar();

    /** Permite usar o banco em {@code try (SGBD banco = ...) { ... }}. */
    @Override
    default void close() {
        fechar();
    }
}
