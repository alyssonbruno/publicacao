import java.util.Scanner;

/**
 * Questão Prática 2 — Lista de tarefas com lista encadeada simples.
 *
 * <p>Sistema de to-do list em console que demonstra inserção no início
 * (tarefas urgentes), inserção no fim (tarefas normais) e remoção do
 * início (conclusão da próxima tarefa).</p>
 *
 * <p>No JDoodle, cole este arquivo inteiro e ative o Interactive Mode.</p>
 *
 * @author Alysson M. Bruno
 * @version 1.0
 */
public class TodoApp {

    // ----------------------------------------------------------
    // Nó da lista (não genérico: armazena String diretamente)
    // ----------------------------------------------------------
    static class No {
        String tarefa;
        No proximo;

        No(String tarefa) {
            this.tarefa = tarefa;
        }
    }

    // ----------------------------------------------------------
    // Lista de tarefas baseada em lista encadeada simples
    // ----------------------------------------------------------
    static class ListaTarefas {
        No cabeca;
        No cauda;
        int tamanho;

        /** Insere tarefa urgente no início — O(1). */
        void inserirUrgente(String tarefa) {
            No novo      = new No(tarefa);
            novo.proximo = cabeca;
            cabeca       = novo;
            if (cauda == null) cauda = novo;
            tamanho++;
        }

        /** Insere tarefa normal no fim — O(1). */
        void inserirNormal(String tarefa) {
            No novo = new No(tarefa);
            if (cauda == null) {
                cabeca = cauda = novo;
            } else {
                cauda.proximo = novo;
                cauda         = novo;
            }
            tamanho++;
        }

        /**
         * Remove e retorna a próxima tarefa (cabeca da lista) — O(1).
         *
         * @return descrição da tarefa, ou {@code null} se a lista estiver vazia
         */
        String concluirProxima() {
            if (cabeca == null) return null;
            String tarefa = cabeca.tarefa;
            cabeca        = cabeca.proximo;
            if (cabeca == null) cauda = null;
            tamanho--;
            return tarefa;
        }

        /** Lista todas as tarefas em ordem de prioridade. */
        void listar() {
            if (cabeca == null) {
                System.out.println("  (nenhuma tarefa pendente)");
                return;
            }
            No atual = cabeca;
            int i    = 1;
            while (atual != null) {
                System.out.println("  " + i++ + ". " + atual.tarefa);
                atual = atual.proximo;
            }
        }
    }

    // ----------------------------------------------------------
    // Interface de console
    // ----------------------------------------------------------
    public static void main(String[] args) {
        Scanner sc     = new Scanner(System.in);
        ListaTarefas l = new ListaTarefas();

        while (true) {
            System.out.println("\n=== Lista de Tarefas ===");
            System.out.println("1. Adicionar tarefa urgente (início da lista)");
            System.out.println("2. Adicionar tarefa normal  (fim da lista)");
            System.out.println("3. Concluir próxima tarefa  (remover do início)");
            System.out.println("4. Listar todas as tarefas");
            System.out.println("5. Sair");
            System.out.print("Opção: ");

            String entrada = sc.nextLine().trim();
            if (entrada.isEmpty()) continue;

            int opcao;
            try {
                opcao = Integer.parseInt(entrada);
            } catch (NumberFormatException e) {
                System.out.println("Opção inválida — digite um número de 1 a 5.");
                continue;
            }

            switch (opcao) {
                case 1 -> {
                    System.out.print("Descrição da tarefa urgente: ");
                    String t = sc.nextLine().trim();
                    if (t.isEmpty()) { System.out.println("Descrição não pode ser vazia."); break; }
                    l.inserirUrgente(t);
                    System.out.println("✔ Tarefa urgente adicionada ao início.");
                }
                case 2 -> {
                    System.out.print("Descrição da tarefa normal: ");
                    String t = sc.nextLine().trim();
                    if (t.isEmpty()) { System.out.println("Descrição não pode ser vazia."); break; }
                    l.inserirNormal(t);
                    System.out.println("✔ Tarefa adicionada ao fim.");
                }
                case 3 -> {
                    String t = l.concluirProxima();
                    if (t == null) System.out.println("Nenhuma tarefa pendente.");
                    else           System.out.println("✔ Concluída: \"" + t + "\"");
                }
                case 4 -> {
                    System.out.println("--- Tarefas pendentes (" + l.tamanho + ") ---");
                    l.listar();
                }
                case 5 -> {
                    System.out.println("Tarefas ainda pendentes: " + l.tamanho);
                    System.out.println("Encerrando. Até a próxima aula!");
                    return;
                }
                default -> System.out.println("Opção inválida — escolha entre 1 e 5.");
            }
        }
    }
}
