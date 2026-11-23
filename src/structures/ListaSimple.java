package structures;

public class ListaSimple<T> {
    private Nodo<T> inicio;
    private int tamaño;

    public ListaSimple() {
        this.inicio = null;
        this.tamaño = 0;
    }

    public Nodo<T> getInicio() {
        return inicio;
    }

    public int getTamaño() {
        return tamaño;
    }

    public boolean estaVacia() {
        return inicio == null;
    }

    public void insertaInicio(T dato) {
        Nodo<T> n = new Nodo<>(dato);
        n.setSig(inicio);
        inicio = n;
        tamaño++;
    }

    public void insertaFin(T dato) {
        Nodo<T> n = new Nodo<>(dato);
        if (inicio == null) {
            inicio = n;
        } else {
            Nodo<T> r = inicio;
            while (r.getSig() != null) {
                r = r.getSig();
            }
            r.setSig(n);
        }
        tamaño++;
    }

    public T eliminaInicio() {
        if (inicio == null) {
            return null;
        }
        T datoEliminado = inicio.getInfo();
        inicio = inicio.getSig();
        tamaño--;
        return datoEliminado;
    }

    public T eliminaFin() {
        if (inicio == null) {
            return null;
        }
        T datoEliminado;
        if (inicio.getSig() == null) {
            datoEliminado = inicio.getInfo();
            inicio = null;
        } else {
            Nodo<T> r = inicio;
            Nodo<T> a = r;
            while (r.getSig() != null) {
                a = r;
                r = r.getSig();
            }
            datoEliminado = r.getInfo();
            a.setSig(null);
        }
        tamaño--;
        return datoEliminado;
    }

    public boolean eliminar(T dato) {
        if (inicio == null) return false;
        if (inicio.getInfo().equals(dato)) {
            inicio = inicio.getSig();
            tamaño--;
            return true;
        }
        Nodo<T> ant = inicio;
        Nodo<T> act = inicio.getSig();
        while (act != null) {
            if (act.getInfo().equals(dato)) {
                ant.setSig(act.getSig());
                tamaño--;
                return true;
            }
            ant = act;
            act = act.getSig();
        }
        return false;
    }

    public boolean contiene(T dato) {
        Nodo<T> r = inicio;
        while (r != null) {
            if (r.getInfo().equals(dato)) {
                return true;
            }
            r = r.getSig();
        }
        return false;
    }

    public void limpiar() {
        inicio = null;
        tamaño = 0;
    }

    public String recorrer() {
        if (inicio == null) return "Lista vacia";
        StringBuilder sb = new StringBuilder();
        Nodo<T> r = inicio;
        while (r != null) {
            sb.append(r.getInfo());
            if (r.getSig() != null) sb.append(" -> ");
            r = r.getSig();
        }
        return sb.toString();
    }
}
