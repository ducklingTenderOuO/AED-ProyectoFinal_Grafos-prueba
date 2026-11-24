package model;

import structures.ListaSimple;
import structures.Nodo;

import java.util.*;

public class Grafo {
    private boolean dirigido;

    private final List<Integer> vertices;

    private final Map<Integer, ListaSimple<Arista>> listasAdyacencia;

    private double[][] matrizAdyacencia;
    private boolean matrizDesactualizada;

    public Grafo(boolean dirigido) {
        this.dirigido = dirigido;
        this.vertices = new ArrayList<>();
        this.listasAdyacencia = new LinkedHashMap<>();
        this.matrizAdyacencia = new double[0][0];
        this.matrizDesactualizada = true;
    }

    public boolean isDirigido() {
        return dirigido;
    }

    public void setDirigido(boolean dirigido) {
        if (this.dirigido != dirigido) {
            this.dirigido = dirigido;
            reconstruirPorCambioDireccion();
        }
    }

    public synchronized boolean agregarVertice(int id) {
        if (!listasAdyacencia.containsKey(id)) {
            vertices.add(id);

            Collections.sort(vertices);
            listasAdyacencia.put(id, new ListaSimple<>());
            matrizDesactualizada = true;
            return true;
        }
        return false;
    }

    public synchronized boolean eliminarVertice(int id) {
        if (!listasAdyacencia.containsKey(id)) {
            return false;
        }

        for (Integer v : vertices) {
            if (v != id) {
                ListaSimple<Arista> lista = listasAdyacencia.get(v);
                Nodo<Arista> act = lista.getInicio();
                while (act != null) {
                    Arista a = act.getInfo();
                    if (a.getDestino() == id) {
                        lista.eliminar(a);
                    }
                    act = act.getSig();
                }
            }
        }

        listasAdyacencia.remove(id);
        vertices.remove(Integer.valueOf(id));
        matrizDesactualizada = true;
        return true;
    }

    public synchronized boolean agregarArista(int origen, int destino, double peso) {

        if (origen == destino) {
            throw new IllegalArgumentException("No se permiten lazos/conexiones consigo mismo (" + origen + " -> " + destino + ")");
        }
        if (peso < 0) {
            throw new IllegalArgumentException("El peso debe ser no negativo: " + peso);
        }

        agregarVertice(origen);
        agregarVertice(destino);

        ListaSimple<Arista> listaOrigen = listasAdyacencia.get(origen);

        Nodo<Arista> act = listaOrigen.getInicio();
        while (act != null) {
            if (act.getInfo().getDestino() == destino) {

                act.getInfo().setPeso(peso);
                if (!dirigido) {
                    actualizarAristaInversa(destino, origen, peso);
                }
                matrizDesactualizada = true;
                return true;
            }
            act = act.getSig();
        }

        listaOrigen.insertaFin(new Arista(origen, destino, peso));

        if (!dirigido) {
            ListaSimple<Arista> listaDestino = listasAdyacencia.get(destino);
            boolean existeInversa = false;
            Nodo<Arista> actDest = listaDestino.getInicio();
            while (actDest != null) {
                if (actDest.getInfo().getDestino() == origen) {
                    actDest.getInfo().setPeso(peso);
                    existeInversa = true;
                    break;
                }
                actDest = actDest.getSig();
            }
            if (!existeInversa) {
                listaDestino.insertaFin(new Arista(destino, origen, peso));
            }
        }

        matrizDesactualizada = true;
        return true;
    }

    private void actualizarAristaInversa(int origen, int destino, double peso) {
        ListaSimple<Arista> lista = listasAdyacencia.get(origen);
        if (lista != null) {
            Nodo<Arista> act = lista.getInicio();
            while (act != null) {
                if (act.getInfo().getDestino() == destino) {
                    act.getInfo().setPeso(peso);
                    return;
                }
                act = act.getSig();
            }
            lista.insertaFin(new Arista(origen, destino, peso));
        }
    }

    public synchronized boolean eliminarArista(int origen, int destino) {
        ListaSimple<Arista> lista = listasAdyacencia.get(origen);
        if (lista == null) return false;

        boolean eliminada = false;
        Nodo<Arista> act = lista.getInicio();
        while (act != null) {
            if (act.getInfo().getDestino() == destino) {
                lista.eliminar(act.getInfo());
                eliminada = true;
                break;
            }
            act = act.getSig();
        }

        if (!dirigido) {
            ListaSimple<Arista> listaInversa = listasAdyacencia.get(destino);
            if (listaInversa != null) {
                Nodo<Arista> actInv = listaInversa.getInicio();
                while (actInv != null) {
                    if (actInv.getInfo().getDestino() == origen) {
                        listaInversa.eliminar(actInv.getInfo());
                        break;
                    }
                    actInv = actInv.getSig();
                }
            }
        }

        if (eliminada) {
            matrizDesactualizada = true;
        }
        return eliminada;
    }

    public synchronized void limpiar() {
        vertices.clear();
        listasAdyacencia.clear();
        matrizAdyacencia = new double[0][0];
        matrizDesactualizada = true;
    }

    public boolean contieneVertice(int id) {
        return listasAdyacencia.containsKey(id);
    }

    public boolean contieneArista(int origen, int destino) {
        ListaSimple<Arista> lista = listasAdyacencia.get(origen);
        if (lista == null) return false;
        Nodo<Arista> act = lista.getInicio();
        while (act != null) {
            if (act.getInfo().getDestino() == destino) return true;
            act = act.getSig();
        }
        return false;
    }

    public ListaSimple<Arista> obtenerAdyacentes(int id) {
        return listasAdyacencia.get(id);
    }

    public List<Integer> getVertices() {
        return Collections.unmodifiableList(vertices);
    }

    public List<Arista> getTodasLasAristas() {
        List<Arista> lista = new ArrayList<>();
        Set<String> visitadas = new HashSet<>();

        for (Integer v : vertices) {
            ListaSimple<Arista> ady = listasAdyacencia.get(v);
            if (ady != null) {
                Nodo<Arista> act = ady.getInicio();
                while (act != null) {
                    Arista a = act.getInfo();
                    if (dirigido) {
                        lista.add(a);
                    } else {

                        int u = Math.min(a.getOrigen(), a.getDestino());
                        int w = Math.max(a.getOrigen(), a.getDestino());
                        String clave = u + "-" + w;
                        if (!visitadas.contains(clave)) {
                            visitadas.add(clave);
                            lista.add(a);
                        }
                    }
                    act = act.getSig();
                }
            }
        }
        return lista;
    }

    public synchronized double[][] getMatrizAdyacencia() {
        if (matrizDesactualizada) {
            actualizarMatriz();
        }
        return matrizAdyacencia;
    }

    private void actualizarMatriz() {
        int n = vertices.size();
        matrizAdyacencia = new double[n][n];

        Map<Integer, Integer> indiceMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            indiceMap.put(vertices.get(i), i);
        }

        for (int i = 0; i < n; i++) {
            int u = vertices.get(i);
            ListaSimple<Arista> lista = listasAdyacencia.get(u);
            if (lista != null) {
                Nodo<Arista> act = lista.getInicio();
                while (act != null) {
                    Arista a = act.getInfo();
                    Integer idxDest = indiceMap.get(a.getDestino());
                    if (idxDest != null) {
                        matrizAdyacencia[i][idxDest] = a.getPeso();
                    }
                    act = act.getSig();
                }
            }
        }
        matrizDesactualizada = false;
    }

    private void reconstruirPorCambioDireccion() {
        List<Arista> aristasActuales = getTodasLasAristas();
        List<Integer> verticesActuales = new ArrayList<>(vertices);
        limpiar();

        for (Integer v : verticesActuales) {
            agregarVertice(v);
        }
        for (Arista a : aristasActuales) {
            agregarArista(a.getOrigen(), a.getDestino(), a.getPeso());
        }
    }

    public int getOrden() {
        return vertices.size();
    }

    public int getTamaño() {
        return getTodasLasAristas().size();
    }

    public int getGrado(int vertice) {
        ListaSimple<Arista> lista = listasAdyacencia.get(vertice);
        return (lista != null) ? lista.getTamaño() : 0;
    }
}
