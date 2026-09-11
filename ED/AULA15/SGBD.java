import java.util.List;

/**
 * TAD de um banco de dados chave-valor didatico.
 *
 * <p>Este contrato especifica o que o banco faz, sem expor como ele
 * persiste ou organiza internamente os dados.</p>
 *
 * <p><strong>Invariante:</strong> para toda chave valida, no maximo um valor
 * atual esta associado a ela no estado corrente do banco.</p>
 */
public interface SGBD extends AutoCloseable {

    /**
     * Insere ou atualiza o valor associado a uma chave.
     *
     * @param chave chave nao nula e nao vazia
     * @param valor valor nao nulo
     * @throws NullPointerException se chave ou valor forem nulos
     * @throws IllegalArgumentException se a chave for vazia
     * Post: get(chave) devolve valor.
     */
    void put(String chave, String valor);

    /**
     * Consulta o valor associado a uma chave.
     *
     * @param chave chave nao nula e nao vazia
     * @return valor associado ou null se nao existir
     * @throws NullPointerException se chave for nula
     * @throws IllegalArgumentException se a chave for vazia
     */
    String get(String chave);

    /**
     * Remove a chave do banco.
     *
     * @param chave chave nao nula e nao vazia
     * @throws NullPointerException se chave for nula
     * @throws IllegalArgumentException se a chave for vazia
     * Post: get(chave) devolve null.
     */
    void delete(String chave);

    /**
     * Lista as chaves em ordem crescente.
     *
     * @return lista ordenada das chaves atuais
     */
    List<String> listarChavesEmOrdem();

    /**
     * Informa a quantidade de chaves atualmente armazenadas.
     *
     * @return numero de pares chave-valor presentes
     */
    int tamanho();

    /**
     * Fecha o banco e libera recursos de persistencia.
     */
    void fechar();

    @Override
    default void close() {
        fechar();
    }
}
