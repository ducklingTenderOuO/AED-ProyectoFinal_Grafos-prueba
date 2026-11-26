package structures;

import java.util.Arrays;

public class ColaPrioridadMin<T> {
    private Object[] datos;
    private double[] prioridades;
    private int fin;
    private int max;

    public ColaPrioridadMin(int max) {
        this.max = Math.max(max, 10);
        this.datos = new Object[this.max];
        this.prioridades = new double[this.max];
        this.fin = 0;
    }

    public ColaPrioridadMin() {
        this(100);
    }

    private int padre(int i) { return (i - 1) / 2; }
    private int hijoIzq(int i) { return 2 * i + 1; }
    private int hijoDer(int i) { return 2 * i + 2; }

    private void subir(int i) {
        while (i > 0 && prioridades[i] < prioridades[padre(i)]) {
            intercambiar(i, padre(i));
            i = padre(i);
        }
    }

    private void bajar(int i) {
        int menor = i;
        int izq = hijoIzq(i);
        int der = hijoDer(i);

        if (izq < fin && prioridades[izq] < prioridades[menor]) menor = izq;
        if (der < fin && prioridades[der] < prioridades[menor]) menor = der;

        if (menor != i) {
            intercambiar(i, menor);
            bajar(menor);
        }
    }

    private void intercambiar(int a, int b) {
        Object auxDato = datos[a];
        datos[a] = datos[b];
        datos[b] = auxDato;

        double auxPri = prioridades[a];
        prioridades[a] = prioridades[b];
        prioridades[b] = auxPri;
    }

    public void insertarDato(T dato, double prioridad) {
        if (estaLlena()) {
            redimensionar();
        }
        datos[fin] = dato;
        prioridades[fin] = prioridad;
        subir(fin);
        fin = fin + 1;
    }

    @SuppressWarnings("unchecked")
    public T eliminarDato() {
        if (estaVacia()) {
            return null;
        }
        T dato = (T) datos[0];
        datos[0] = datos[fin - 1];
        prioridades[0] = prioridades[fin - 1];
        datos[fin - 1] = null;
        fin = fin - 1;
        if (fin > 0) {
            bajar(0);
        }
        return dato;
    }

    public boolean estaVacia() {
        return fin == 0;
    }

    public boolean estaLlena() {
        return fin == max;
    }

    public int getTamaño() {
        return fin;
    }

    @SuppressWarnings("unchecked")
    public T frente() {
        if (!estaVacia()) {
            return (T) datos[0];
        }
        return null;
    }

    public double prioridadFrente() {
        if (!estaVacia()) {
            return prioridades[0];
        }
        return Double.MAX_VALUE;
    }

    private void redimensionar() {
        int nuevoMax = max * 2;
        datos = Arrays.copyOf(datos, nuevoMax);
        prioridades = Arrays.copyOf(prioridades, nuevoMax);
        this.max = nuevoMax;
    }
}
