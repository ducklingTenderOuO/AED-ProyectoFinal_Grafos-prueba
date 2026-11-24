package algorithms;

import model.Arista;
import model.Grafo;
import structures.ColaSimple;
import structures.ListaSimple;
import structures.Nodo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BFS {

    public static List<Integer> ejecutar(Grafo grafo, int inicio) {
        List<Integer> ordenVisita = new ArrayList<>();
        if (!grafo.contieneVertice(inicio)) {
            return ordenVisita;
        }

        Set<Integer> visitados = new HashSet<>();
        ColaSimple<Integer> cola = new ColaSimple<>(grafo.getOrden() + 10);

        visitados.add(inicio);
        cola.insertarDato(inicio);

        while (!cola.estaVacia()) {
            Integer actual = cola.eliminarDato();
            if (actual == null) break;
            ordenVisita.add(actual);

            ListaSimple<Arista> adyacentes = grafo.obtenerAdyacentes(actual);
            if (adyacentes != null) {
                Nodo<Arista> nodo = adyacentes.getInicio();
                while (nodo != null) {
                    int vecino = nodo.getInfo().getDestino();
                    if (!visitados.contains(vecino)) {
                        visitados.add(vecino);
                        cola.insertarDato(vecino);
                    }
                    nodo = nodo.getSig();
                }
            }
        }

        return ordenVisita;
    }
}
