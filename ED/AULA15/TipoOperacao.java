/**
 * Tipos de operação gravados no log.
 *
 * <p>{@code PUT} salva ou atualiza um valor; {@code DEL} remove uma chave.
 * Um grupo pode acrescentar tipos novos, desde que explique no relatório como
 * a reconstrução trata cada um.</p>
 */
public enum TipoOperacao {
    PUT,
    DEL
}
