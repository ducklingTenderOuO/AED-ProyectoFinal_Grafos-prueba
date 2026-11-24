package algorithms;

import model.Arista;
import model.Grafo;
import structures.ListaSimple;
import structures.Nodo;
import structures.Pila;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DFS {

    public static List<Integer> ejecutar(Grafo grafo, int inicio) {
        List<Integer> ordenVisita = new ArrayList<>();
        if (!grafo.contieneVertice(inicio)) {
            return ordenVisita;
        }

        Set<Integer> visitados = new HashSet<>();
        Pila<Integer> pila = new Pila<>(grafo.getOrden() + 10);

        pila.push(inicio);

        while (!pila.estaVacia()) {
            Integer actual = pila.pop();
            if (actual == null) break;

            if (!visitados.contains(actual)) {
                visitados.add(actual);
                ordenVisita.add(actual);

                ListaSimple<Arista> adyacentes = grafo.obtenerAdyacentes(actual);
                if (adyacentes != null) {

                    List<Integer> vecinos = new ArrayList<>();
                    Nodo<Arista> nodo = adyacentes.getInicio();
                    while (nodo != null) {
                        vecinos.add(nodo.getInfo().getDestino());
                        nodo = nodo.getSig();
                    }

                    for (int i = vecinos.size() - 1; i >= 0; i--) {
                        int vecino = vecinos.get(i);
                        if (!visitados.contains(vecino)) {
                            pila.push(vecino);
                        }
                    }
                }
            }
        }

        return ordenVisita;
    }
}
