package structures;

import java.util.Arrays;

public class ColaSimple<T> {
    private Object[] cola;
    private int inicio;
    private int fin;
    private int max;
    private int tamaño;

    public ColaSimple(int max) {
        this.max = Math.max(max, 10);
        this.cola = new Object[this.max + 1];
        this.inicio = 0;
        this.fin = 0;
        this.tamaño = 0;
    }

    public ColaSimple() {
        this(100);
    }

    public void insertarDato(T dato) {
        if (fin >= max) {

            redimensionar();
        }
        fin = fin + 1;
        cola[fin] = dato;
        if (fin == 1 || inicio == 0) {
            inicio = 1;
        }
        tamaño++;
    }

    @SuppressWarnings("unchecked")
    public T eliminarDato() {
        if (inicio != 0 && tamaño > 0) {
            T dato = (T) cola[inicio];
            cola[inicio] = null;
            if (inicio == fin) {
                inicio = 0;
                fin = 0;
            } else {
                inicio = inicio + 1;
            }
            tamaño--;
            return dato;
        }
        return null;
    }

    public boolean estaVacia() {
        return tamaño == 0;
    }

    public boolean estaLlena() {
        return tamaño == max;
    }

    public int getTamaño() {
        return tamaño;
    }

    @SuppressWarnings("unchecked")
    public T frente() {
        if (!estaVacia()) {
            return (T) cola[inicio];
        }
        return null;
    }

    private void redimensionar() {
        int nuevoMax = max * 2;
        Object[] nueva = new Object[nuevoMax + 1];
        int idx = 1;
        for (int i = inicio; i <= fin; i++) {
            nueva[idx++] = cola[i];
        }
        this.cola = nueva;
        this.inicio = 1;
        this.fin = idx - 1;
        this.max = nuevoMax;
    }
}
