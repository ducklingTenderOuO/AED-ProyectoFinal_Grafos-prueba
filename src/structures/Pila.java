package structures;

import java.util.Arrays;

public class Pila<T> {
    private T[] pila;
    private int tope;
    private static final int CAPACIDAD_INICIAL = 100;
    private static final int MINIMO_VALOR = -1;

    @SuppressWarnings("unchecked")
    public Pila() {
        this(CAPACIDAD_INICIAL);
    }

    @SuppressWarnings("unchecked")
    public Pila(int capacidad) {
        this.pila = (T[]) new Object[Math.max(capacidad, 10)];
        this.tope = MINIMO_VALOR;
    }

    public void push(T dato) {
        if (pila_llena()) {
            redimensionar();
        }
        tope++;
        pila[tope] = dato;
    }

    public T pop() {
        if (pila_vacia()) {
            return null;
        }
        T dato = pila[tope];
        pila[tope] = null;
        tope--;
        return dato;
    }

    public boolean pila_vacia() {
        return tope == MINIMO_VALOR;
    }

    public boolean estaVacia() {
        return pila_vacia();
    }

    public boolean pila_llena() {
        return tope == pila.length - 1;
    }

    public T cima() {
        if (pila_vacia()) {
            return null;
        }
        return pila[tope];
    }

    public int getTope() {
        return tope;
    }

    public int getTamaño() {
        return tope + 1;
    }

    public int getCapacidad() {
        return pila.length;
    }

    @SuppressWarnings("unchecked")
    private void redimensionar() {
        int nuevaCap = pila.length * 2;
        pila = Arrays.copyOf(pila, nuevaCap);
    }
}
