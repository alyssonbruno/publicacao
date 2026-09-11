/**
 * Aula 7 — Hands-on: BUBBLE SORT sobre uma LISTA ENCADEADA.
 *
 * <p>Como usar no JDoodle (https://www.jdoodle.com/online-java-compiler-ide):
 * escolha a linguagem <b>Java</b> e o <b>JDK 25</b>, apague o exemplo da tela,
 * cole este arquivo inteiro e clique em <b>Execute</b>.
 * Não é preciso digitar nada: o programa roda sozinho.</p>
 *
 * <p>O que observar na saída:</p>
 * <ol>
 *   <li>aqui o Bubble Sort não roda sobre um vetor, e sim sobre a lista da Aula 05;</li>
 *   <li>a cada passagem completa a lista é impressa — acompanhe as trocas;</li>
 *   <li>a última passagem não troca nada: é assim que o algoritmo sabe que terminou;</li>
 *   <li>no fim, os elementos são retirados da cabeça da lista, já em ordem.</li>
 * </ol>
 *
 * @author Prof. Alysson M. Bruno
 * @version 1.0
 */
public class BubbleSortListaApp {
    static public void main (String[] args){
        BubbleSort<Integer> fila = new BubbleSort<Integer>();
        
        fila.inserir(10);
        fila.inserir(9);
        fila.inserir(8);
        fila.inserir(1);
        fila.inserir(2);
        fila.inserir(13);
        fila.inserir(4);
        fila.inserir(5);
        fila.inserir(3);
        
        fila.ordenar();

        while(true){
            Integer dado = fila.retirar();
            if(dado == null) break;
            System.out.println("Dado: " + dado);
        }
    }
}

/**
 * BubbleSort Interface -- Implementação didática
 * 
 * Esta é uma implementação de uma Fila
 * @author Alysson Bruno alysson.mb@unitins.br
 */

interface BubbleSortInterface<T> {
    /**
     * insere o elemento no final da pilha
     */
    public void inserir(T dado);

    /**
     * retira o elemento do final da pilha
     */
    public T retirar();
    public void ordenar();
}

@SuppressWarnings("unchecked")
class BubbleSort<T extends Comparable<T>> implements BubbleSortInterface<T> {
    class Node {
        T dado;
        Node proximo;
        
        public Node(T dado){
            this.dado = dado;
        }

        public T getDado(){
            return dado;
        }

        public Node getProximo(){
            return proximo;
        }

        public void setProximo(Node prox){
            this.proximo = prox;
        }
        
    }
    Node cabeca;
    Node cauda;
    int tamanho = 0;

    @Override
    public void inserir(T dado) {
        if (cauda == null){
            cauda = new Node(dado);
            cabeca = cauda;
        }
        else {
            cauda.setProximo(new Node(dado));
            this.cauda = cauda.getProximo();
        }
        tamanho += 1;
    }

    @Override
    
    public T retirar(){
        if(cabeca != null){
            Object dadoRetirado;
            dadoRetirado = cabeca.getDado();
            cabeca = cabeca.getProximo();
            tamanho -= 1;
            return (T) dadoRetirado;            
        }
        return null;
    }

    private Boolean _ordenar(Node anterior, Node primeiro, Node segundo){
        if (primeiro == null || segundo == null){
            return false;
        } 
        Comparable dadoDoPrimeiro = primeiro.getDado();
        Comparable dadoDoSegundo = segundo.getDado();
        if(dadoDoPrimeiro.compareTo(dadoDoSegundo)>0){
            Node temp = segundo.getProximo();
            if(anterior != null) {
                anterior.setProximo(segundo);
            }
            else if (primeiro == cabeca) {
                cabeca = segundo;
            }
            segundo.setProximo(primeiro);
            primeiro.setProximo(temp);
            return true;
        }
        return false;
    }

    @Override
    public void ordenar(){
        Boolean ordenou = true;
        while (ordenou){
            ordenou = false;
            Node atual = cabeca;
            Node anterior = null;
            while(atual != null){
                if(atual.getProximo() != null){
                    ordenou = _ordenar(anterior, atual, atual.getProximo()) || ordenou;
                }
                anterior = atual;
                atual = atual.getProximo();
            }
            this.print();
        }

    }

    public void print(){
        Node atual = cabeca;
        System.out.print("[");
        while(atual != null){
            System.out.print( atual.getProximo() != null ? atual.getDado()+"," : atual.getDado());
            atual = atual.getProximo();
        }
        System.out.printf("]\n");
    }

}
