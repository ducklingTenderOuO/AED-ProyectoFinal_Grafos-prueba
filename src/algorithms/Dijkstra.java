package algorithms;

import model.Arista;
import model.Grafo;
import structures.ColaPrioridadMin;
import structures.ListaSimple;
import structures.Nodo;

import java.util.*;

public class Dijkstra {

    public static ResultadoDijkstra encontrarRuta(Grafo grafo, int origen, int destino) {
        if (!grafo.contieneVertice(origen) || !grafo.contieneVertice(destino)) {
            return new ResultadoDijkstra(origen, destino, Collections.emptyList(), -1, false);
        }

        if (origen == destino) {
            return new ResultadoDijkstra(origen, destino, Collections.singletonList(origen), 0.0, true);
        }

        Map<Integer, Double> distancias = new HashMap<>();
        Map<Integer, Integer> previo = new HashMap<>();
        Set<Integer> visitados = new HashSet<>();

        for (Integer v : grafo.getVertices()) {
            distancias.put(v, Double.POSITIVE_INFINITY);
        }
        distancias.put(origen, 0.0);

        ColaPrioridadMin<Integer> pq = new ColaPrioridadMin<>(grafo.getOrden() * 2 + 10);
        pq.insertarDato(origen, 0.0);

        while (!pq.estaVacia()) {
            Integer actual = pq.eliminarDato();
            if (actual == null) break;

            if (actual == destino) {

                break;
            }

            if (visitados.contains(actual)) {
                continue;
            }
            visitados.add(actual);

            double distActual = distancias.get(actual);
            ListaSimple<Arista> adyacentes = grafo.obtenerAdyacentes(actual);
            if (adyacentes != null) {
                Nodo<Arista> nodo = adyacentes.getInicio();
                while (nodo != null) {
                    Arista arista = nodo.getInfo();
                    int vecino = arista.getDestino();
                    double peso = arista.getPeso();

                    if (!visitados.contains(vecino)) {
                        double nuevaDist = distActual + peso;
                        if (nuevaDist < distancias.getOrDefault(vecino, Double.POSITIVE_INFINITY)) {
                            distancias.put(vecino, nuevaDist);
                            previo.put(vecino, actual);
                            pq.insertarDato(vecino, nuevaDist);
                        }
                    }
                    nodo = nodo.getSig();
                }
            }
        }

        if (!previo.containsKey(destino) && origen != destino) {
            return new ResultadoDijkstra(origen, destino, Collections.emptyList(), -1, false);
        }

        LinkedList<Integer> ruta = new LinkedList<>();
        Integer curr = destino;
        while (curr != null) {
            ruta.addFirst(curr);
            curr = previo.get(curr);
        }

        double costoTotal = distancias.get(destino);
        return new ResultadoDijkstra(origen, destino, ruta, costoTotal, true);
    }
}
