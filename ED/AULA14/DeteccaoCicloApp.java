import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Aula 14 — Hands-on: MESMA DETECÇÃO DE CICLO com dois conjuntos, em vez de três cores.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>"visitando" é o caminho atual da recursão; "visitado" é o que já foi fechado;</li>
 *   <li>nas duas perguntas a resposta é <b>false</b>: este grafo não tem ciclo;</li>
 *   <li>acrescente <code>grafo.add("List", "App");</code> antes das perguntas,
 *       execute de novo e as duas respostas viram <b>true</b>;</li>
 *   <li>compare com DetectorDependencias.java: mesma ideia, escrita de outro jeito.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class DeteccaoCicloApp {
    static class Grafo {

        private Map<String, List<String>> adj = new HashMap<>();

        private void criarVertice(String v){
            adj.putIfAbsent(v, new ArrayList<>());
        }
        public void add(String vertice, String... vizinhos){
            criarVertice(vertice);
            for (String v : vizinhos){
                criarVertice(v);
                adj.get(vertice).add(v);
            }
        }

        public boolean checarCiclo(String origem){
            // "visitando" guarda os vértices no caminho recursivo atual (pilha da DFS);
            // "visitado" guarda os vértices já totalmente explorados, para não revisitá-los.
            Set<String> visitando = new HashSet<>();
            Set<String> visitado = new HashSet<>();
            return checarCiclo(origem, visitando, visitado);
        }

        private boolean checarCiclo(String atual, Set<String> visitando, Set<String> visitado){
            visitando.add(atual);
            if (adj.get(atual) != null){
                for (String vizinho : adj.get(atual)){
                    if (visitando.contains(vizinho)){
                        return true;
                    }
                    if (!visitado.contains(vizinho) && checarCiclo(vizinho, visitando, visitado)){
                        return true;
                    }
                }
            }
            visitando.remove(atual);
            visitado.add(atual);
            return false;
        }

    }

    static public void main(String[] args){
        Grafo grafo = new Grafo();

        grafo.add("App", "SQL", "Utils", "Networks");
        grafo.add("SQL", "Oracle", "PostgreSQL", "Networks");
        grafo.add("Utils", "Java", "List");
        grafo.add("Service", "List", "Build");
        grafo.add("Build", "Utils");
        grafo.add("Web", "SQL", "Service");
        System.out.println("Tem ciclo? " + grafo.checarCiclo("Service"));
        System.out.println("Tem ciclo? " + grafo.checarCiclo("App"));





    }
}
