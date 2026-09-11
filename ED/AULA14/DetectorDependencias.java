import java.util.*;

/**
 * Aula 14 — Hands-on: DETECÇÃO DE CICLO em grafo de dependências (DFS com três cores).
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>cada módulo é um vértice e cada dependência é uma aresta com direção;</li>
 *   <li>BRANCO é o que ainda não foi visitado, CINZA está na pilha de chamadas, PRETO já foi fechado;</li>
 *   <li>achar um vizinho CINZA significa que a busca voltou ao próprio caminho — há ciclo;</li>
 *   <li>o primeiro teste não tem ciclo; o segundo fecha ModuloA → ModuloB → ModuloC → ModuloA.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class DetectorDependencias {

    // Enum para representar os estados (cores) de cada nó durante a DFS
    private enum Estado {
        NAO_VISITADO,  // Branco: Ainda não foi explorado
        VISITANDO,     // Cinza: Está na pilha de recursão atual (se achá-lo de novo, há ciclo!)
        VISITADO       // Preto: Totalmente explorado, sem ciclos a partir dele
    }

    // O Grafo é representado por uma Lista de Adjacência: Map<Modulo, List<Modulo>>
    private final Map<String, List<String>> grafo = new HashMap<>();

    // Método para adicionar um módulo e suas dependências (arestas direcionadas)
    public void adicionarDependencia(String modulo, String... dependencias) {
        grafo.putIfAbsent(modulo, new ArrayList<>());
        for (String dep : dependencias) {
            grafo.putIfAbsent(dep, new ArrayList<>()); // Garante que o nó destino exista
            grafo.get(modulo).add(dep);
        }
    }

    // Método principal que varre todo o grafo para detectar ciclos
    public boolean possuiCiclo() {
        Map<String, Estado> estados = new HashMap<>();
        
        // Inicializa todos os módulos como NÃO_VISITADOS
        for (String modulo : grafo.keySet()) {
            estados.put(modulo, Estado.NAO_VISITADO);
        }

        // Executa a DFS a partir de cada nó (necessário para grafos desconexos)
        for (String modulo : grafo.keySet()) {
            if (estados.get(modulo) == Estado.NAO_VISITADO) {
                if (detectarCicloDFS(modulo, estados)) {
                    return true; 
                }
            }
        }
        return false;
    }

    // Algoritmo DFS com coloração de nós
    private boolean detectarCicloDFS(String atual, Map<String, Estado> estados) {
        // Entrando no nó: marca como VISITANDO (na pilha)
        estados.put(atual, Estado.VISITANDO);

        // Explora todos os vizinhos (dependências)
        for (String vizinho : grafo.get(atual)) {
            Estado estadoVizinho = estados.get(vizinho);

            // Cenário A: Se o vizinho já está sendo visitado na linha de execução atual, há um ciclo!
            if (estadoVizinho == Estado.VISITANDO) {
                System.out.println("⚠️ Ciclo detectado envolvendo a conexão: " + atual + " -> " + vizinho);
                return true; 
            }

            // Cenário B: Se não foi visitado, continua a busca em profundidade
            if (estadoVizinho == Estado.NAO_VISITADO) {
                if (detectarCicloDFS(vizinho, estados)) {
                    return true;
                }
            }
        }

        // Saindo do nó: marca como totalmente VISITADO (fora da pilha)
        estados.put(atual, Estado.VISITADO);
        return false;
    }

    // Testando o cenário em aula
    public static void main(String[] args) {
        System.out.println("--- Teste 1: Cenário Seguro (Sem Ciclos) ---");
        DetectorDependencias sistemaSeguro = new DetectorDependencias();
        sistemaSeguro.adicionarDependencia("App", "Services", "Utils");
        sistemaSeguro.adicionarDependencia("Services", "Database");
        sistemaSeguro.adicionarDependencia("Database", "Utils"); // Caminhos alternativos são permitidos, desde que não voltem
        System.out.println("Possui ciclo? " + sistemaSeguro.possuiCiclo()); // Deve retornar false

        System.out.println("\n--- Teste 2: Cenário com Erro (Com Ciclos) ---");
        DetectorDependencias sistemaCiclico = new DetectorDependencias();
        sistemaCiclico.adicionarDependencia("ModuloA", "ModuloB");
        sistemaCiclico.adicionarDependencia("ModuloB", "ModuloC");
        sistemaCiclico.adicionarDependencia("ModuloC", "ModuloA"); // ModuloC aponta de volta para ModuloA!
        System.out.println("Possui ciclo? " + sistemaCiclico.possuiCiclo()); // Deve retornar true
    }
}
