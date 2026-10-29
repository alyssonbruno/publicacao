import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aula 14 — Hands-on: GRAFO em lista de adjacência, BFS, DFS e componentes.
 *
 * <p>Como usar: no OneCompiler (https://onecompiler.com/java) roda como está:
 * apague o exemplo da tela, cole este arquivo inteiro e clique em <b>Run</b>.
 * No JDoodle (https://www.jdoodle.com/online-java-compiler-ide), deixe a
 * versão mais nova e troque <b>public class GrafosApp</b> por
 * <b>public class MyClass</b>; depois, clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>A rede do exemplo: oito equipamentos de rede (A a H) e os cabos entre
 * eles. A rede é um grafo NÃO direcionado: o cabo A-B serve nos dois sentidos.
 * G e H estão num prédio que ainda não foi ligado ao resto.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>a lista de adjacência guarda 14 vizinhos (2 por cabo); a matriz, 64 posições;</li>
 *   <li>a BFS tira os vértices da fila na ordem A, B, C, D, E, F: primeiro os
 *       vizinhos de A, depois os vizinhos dos vizinhos;</li>
 *   <li>o caminho com menos saltos de A até F é A, B, D, E, F (4 saltos), e não há
 *       caminho de A até G;</li>
 *   <li>a DFS visita A, B, D, C, E, F: desce até não ter para onde ir e volta;
 *       a versão com pilha visita na mesma ordem;</li>
 *   <li>a rede tem duas partes (componentes): A a F, e G com H.</li>
 * </ol>
 *
 * <p>Para mudar (Passo 4 do roteiro do laboratório): logo acima do main, troque
 * <code>caboNovoCE = false</code> por <code>true</code> e execute de novo.</p>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class GrafosApp {

    /** Grafo guardado em lista de adjacência: cada vértice aponta para a lista dos vizinhos. */
    static class Grafo {

        // LinkedHashMap guarda os vértices na ordem em que foram criados:
        // assim a saída é sempre a mesma, em qualquer máquina.
        private final Map<String, List<String>> adj = new LinkedHashMap<>();
        private final boolean direcionado;
        private int arestas = 0;

        Grafo(boolean direcionado) {
            this.direcionado = direcionado;
        }

        void adicionarVertice(String v) {
            adj.putIfAbsent(v, new ArrayList<>());
        }

        void adicionarAresta(String a, String b) {
            adicionarVertice(a);
            adicionarVertice(b);
            adj.get(a).add(b);
            if (!direcionado) {
                adj.get(b).add(a); // não direcionado: o cabo serve nos dois sentidos
            }
            arestas++;
        }

        List<String> vizinhos(String v) {
            return adj.getOrDefault(v, List.of());
        }

        int numVertices() {
            return adj.size();
        }

        int numArestas() {
            return arestas;
        }

        void imprimirLista() {
            int entradas = 0;
            for (Map.Entry<String, List<String>> e : adj.entrySet()) {
                System.out.println(e.getKey() + ": " + String.join(", ", e.getValue()));
                entradas += e.getValue().size();
            }
            System.out.println("vizinhos guardados na lista: " + entradas);
        }

        void imprimirMatriz() {
            // A matriz precisa de um número (índice) para cada vértice.
            List<String> nomes = new ArrayList<>(adj.keySet());
            Map<String, Integer> indice = new HashMap<>();
            for (int i = 0; i < nomes.size(); i++) {
                indice.put(nomes.get(i), i);
            }
            int[][] m = new int[nomes.size()][nomes.size()];
            for (String v : nomes) {
                for (String w : adj.get(v)) {
                    m[indice.get(v)][indice.get(w)] = 1;
                }
            }
            System.out.println("   " + String.join(" ", nomes));
            for (int i = 0; i < nomes.size(); i++) {
                StringBuilder linha = new StringBuilder(nomes.get(i) + "  ");
                for (int j = 0; j < nomes.size(); j++) {
                    linha.append(m[i][j]).append(j < nomes.size() - 1 ? " " : "");
                }
                System.out.println(linha);
            }
            System.out.println("posições da matriz: " + nomes.size() * nomes.size());
        }

        /** BFS: usa uma FILA; mostra cada passo. Devolve a ordem em que os vértices saem da fila. */
        List<String> bfs(String origem, boolean mostrarPassos) {
            List<String> ordem = new ArrayList<>();
            Set<String> visitados = new HashSet<>();
            Deque<String> fila = new ArrayDeque<>();

            visitados.add(origem);   // marca ao ENTRAR na fila
            fila.addLast(origem);
            if (mostrarPassos) {
                System.out.println("início: fila = " + fila);
            }

            while (!fila.isEmpty()) {
                String atual = fila.pollFirst();   // tira da FRENTE
                ordem.add(atual);
                List<String> relato = new ArrayList<>();
                for (String vizinho : vizinhos(atual)) {
                    if (!visitados.contains(vizinho)) {
                        visitados.add(vizinho);
                        fila.addLast(vizinho);         // põe no FIM
                        relato.add(vizinho + " (novo)");
                    } else {
                        relato.add(vizinho + " (já visto)");
                    }
                }
                if (mostrarPassos) {
                    System.out.println("tira " + atual + " | vizinhos: " + String.join(", ", relato)
                            + " | fila = " + fila);
                }
            }
            return ordem;
        }

        /**
         * BFS que anota, para cada vértice, quem o descobriu (o "pai") e a distância
         * em saltos. É o que permite remontar o caminho com menos saltos.
         */
        Map<String, String> bfsComPais(String origem, Map<String, Integer> distancia) {
            Map<String, String> pai = new HashMap<>();
            Deque<String> fila = new ArrayDeque<>();
            distancia.put(origem, 0);
            fila.addLast(origem);
            while (!fila.isEmpty()) {
                String atual = fila.pollFirst();
                for (String vizinho : vizinhos(atual)) {
                    if (!distancia.containsKey(vizinho)) {      // ainda não descoberto
                        distancia.put(vizinho, distancia.get(atual) + 1);
                        pai.put(vizinho, atual);
                        fila.addLast(vizinho);
                    }
                }
            }
            return pai;
        }

        /** Caminho com menos saltos: segue os pais de trás para frente. Lista vazia se não há caminho. */
        List<String> caminhoMaisCurto(String origem, String destino) {
            Map<String, Integer> distancia = new HashMap<>();
            Map<String, String> pai = bfsComPais(origem, distancia);
            List<String> caminho = new ArrayList<>();
            if (!distancia.containsKey(destino)) {
                return caminho; // destino não foi alcançado
            }
            for (String v = destino; v != null; v = pai.get(v)) {
                caminho.add(0, v); // põe na frente: o caminho sai de trás para frente
            }
            return caminho;
        }

        /** DFS recursiva: a pilha de chamadas do Java guarda o caminho atual. */
        List<String> dfs(String origem, boolean mostrarPassos) {
            List<String> ordem = new ArrayList<>();
            dfsVisitar(origem, new HashSet<>(), ordem, 0, mostrarPassos);
            return ordem;
        }

        private void dfsVisitar(String atual, Set<String> visitados, List<String> ordem,
                                int profundidade, boolean mostrarPassos) {
            visitados.add(atual);
            ordem.add(atual);
            String recuo = "  ".repeat(profundidade);
            if (mostrarPassos) {
                System.out.println(recuo + "entra em " + atual);
            }
            for (String vizinho : vizinhos(atual)) {
                if (!visitados.contains(vizinho)) {
                    dfsVisitar(vizinho, visitados, ordem, profundidade + 1, mostrarPassos);
                }
            }
            if (mostrarPassos) {
                System.out.println(recuo + "volta de " + atual);
            }
        }

        /** DFS com uma PILHA explícita (Aula 06), sem recursão. */
        List<String> dfsComPilha(String origem) {
            List<String> ordem = new ArrayList<>();
            Set<String> visitados = new HashSet<>();
            Deque<String> pilha = new ArrayDeque<>();
            pilha.push(origem);
            while (!pilha.isEmpty()) {
                String atual = pilha.pop();
                if (visitados.contains(atual)) {
                    continue; // já visitado por outro caminho
                }
                visitados.add(atual);
                ordem.add(atual);
                List<String> viz = vizinhos(atual);
                // empilha de trás para frente, para o primeiro vizinho sair primeiro
                for (int i = viz.size() - 1; i >= 0; i--) {
                    if (!visitados.contains(viz.get(i))) {
                        pilha.push(viz.get(i));
                    }
                }
            }
            return ordem;
        }

        /**
         * Componentes de um grafo NÃO direcionado: uma BFS a partir de cada vértice
         * que ainda não foi alcançado. Cada BFS devolve uma parte da rede.
         */
        List<List<String>> componentes() {
            List<List<String>> partes = new ArrayList<>();
            Set<String> alcancados = new HashSet<>();
            for (String v : adj.keySet()) {
                if (!alcancados.contains(v)) {
                    List<String> parte = bfs(v, false);
                    alcancados.addAll(parte);
                    partes.add(parte);
                }
            }
            return partes;
        }
    }

    // Passo 4 do laboratório: troque para true para ligar um cabo novo entre C e E.
    static boolean caboNovoCE = false;

    public static void main(String[] args) {
        Grafo rede = new Grafo(false); // false = não direcionado
        rede.adicionarAresta("A", "B");
        rede.adicionarAresta("A", "C");
        rede.adicionarAresta("B", "D");
        rede.adicionarAresta("C", "D");
        rede.adicionarAresta("D", "E");
        rede.adicionarAresta("E", "F");
        rede.adicionarAresta("G", "H"); // prédio novo, ainda sem ligação com o resto
        if (caboNovoCE) {
            rede.adicionarAresta("C", "E");
        }

        System.out.println("=== 1. A rede: " + rede.numVertices() + " vértices e "
                + rede.numArestas() + " arestas (lista de adjacência)");
        rede.imprimirLista();

        System.out.println();
        System.out.println("=== 2. A mesma rede como matriz de adjacência (1 = há cabo)");
        rede.imprimirMatriz();

        System.out.println();
        System.out.println("=== 3. BFS a partir de A, passo a passo (fila)");
        List<String> ordemBfs = rede.bfs("A", true);
        System.out.println("ordem da BFS: " + ordemBfs);

        System.out.println();
        System.out.println("=== 4. Saltos a partir de A e quem descobriu cada vértice");
        Map<String, Integer> distancia = new HashMap<>();
        Map<String, String> pai = rede.bfsComPais("A", distancia);
        for (String v : List.of("A", "B", "C", "D", "E", "F", "G", "H")) {
            if (distancia.containsKey(v)) {
                String quem = pai.containsKey(v) ? "descoberto por " + pai.get(v) : "é a origem";
                System.out.println(v + ": " + distancia.get(v) + " salto(s), " + quem);
            } else {
                System.out.println(v + ": sem caminho a partir de A");
            }
        }
        List<String> caminhoF = rede.caminhoMaisCurto("A", "F");
        System.out.println("caminho com menos saltos de A até F: " + caminhoF
                + " (" + (caminhoF.size() - 1) + " saltos)");
        System.out.println("caminho de A até G: " + rede.caminhoMaisCurto("A", "G")
                + " (lista vazia: não há caminho)");

        System.out.println();
        System.out.println("=== 5. DFS a partir de A (recursiva: entra e volta)");
        List<String> ordemDfs = rede.dfs("A", true);
        System.out.println("ordem da DFS recursiva: " + ordemDfs);
        System.out.println("ordem da DFS com pilha: " + rede.dfsComPilha("A"));

        System.out.println();
        System.out.println("=== 6. Componentes: as partes da rede ligadas entre si");
        List<List<String>> partes = rede.componentes();
        for (int i = 0; i < partes.size(); i++) {
            System.out.println("componente " + (i + 1) + ": " + partes.get(i));
        }
    }
}
