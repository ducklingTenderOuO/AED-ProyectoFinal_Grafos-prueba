package algorithms;

import model.Arista;

import java.util.Comparator;

@SuppressWarnings({"rawtypes", "unchecked"})
public class MetodosOrdenamiento {

    public static void shellSort(Comparable[] a) {
        if (a == null || a.length < 2) return;
        int n = a.length;
        int inter = n + 1;
        while (inter > 1) {
            inter = inter / 2;
            boolean band = true;
            while (band) {
                band = false;
                int i = 0;
                while ((i + inter) < n) {
                    if (a[i].compareTo(a[i + inter]) > 0) {
                        Comparable aux = a[i];
                        a[i] = a[i + inter];
                        a[i + inter] = aux;
                        band = true;
                    }
                    i++;
                }
            }
        }
    }

    public static void shellSortAristas(Arista[] a, Comparator<Arista> comparador) {
        if (a == null || a.length < 2) return;
        int n = a.length;
        int inter = n + 1;
        while (inter > 1) {
            inter = inter / 2;
            boolean band = true;
            while (band) {
                band = false;
                int i = 0;
                while ((i + inter) < n) {
                    if (comparador.compare(a[i], a[i + inter]) > 0) {
                        Arista aux = a[i];
                        a[i] = a[i + inter];
                        a[i + inter] = aux;
                        band = true;
                    }
                    i++;
                }
            }
        }
    }

    public static void quickSort(Comparable[] a) {
        if (a == null || a.length < 2) return;
        quickSortRecursivo(a, 0, a.length - 1);
    }

    private static void quickSortRecursivo(Comparable[] a, int ini, int fin) {
        int izq = ini;
        int der = fin;
        int pos = ini;
        boolean band = true;

        while (band) {
            band = false;
            while (a[pos].compareTo(a[der]) <= 0 && pos != der) {
                der = der - 1;
            }
            if (pos != der) {
                Comparable aux = a[pos];
                a[pos] = a[der];
                a[der] = aux;
                pos = der;

                while (a[pos].compareTo(a[izq]) >= 0 && pos != izq) {
                    izq = izq + 1;
                }
                if (pos != izq) {
                    band = true;
                    aux = a[pos];
                    a[pos] = a[izq];
                    a[izq] = aux;
                    pos = izq;
                }
            }
        }

        if ((pos - 1) > ini) {
            quickSortRecursivo(a, ini, pos - 1);
        }
        if (fin > (pos + 1)) {
            quickSortRecursivo(a, pos + 1, fin);
        }
    }

    public static void quickSortAristas(Arista[] a, Comparator<Arista> comparador) {
        if (a == null || a.length < 2) return;
        quickSortAristasRecursivo(a, 0, a.length - 1, comparador);
    }

    private static void quickSortAristasRecursivo(Arista[] a, int ini, int fin, Comparator<Arista> comp) {
        int izq = ini;
        int der = fin;
        int pos = ini;
        boolean band = true;

        while (band) {
            band = false;
            while (comp.compare(a[pos], a[der]) <= 0 && pos != der) {
                der = der - 1;
            }
            if (pos != der) {
                Arista aux = a[pos];
                a[pos] = a[der];
                a[der] = aux;
                pos = der;

                while (comp.compare(a[pos], a[izq]) >= 0 && pos != izq) {
                    izq = izq + 1;
                }
                if (pos != izq) {
                    band = true;
                    aux = a[pos];
                    a[pos] = a[izq];
                    a[izq] = aux;
                    pos = izq;
                }
            }
        }

        if ((pos - 1) > ini) {
            quickSortAristasRecursivo(a, ini, pos - 1, comp);
        }
        if (fin > (pos + 1)) {
            quickSortAristasRecursivo(a, pos + 1, fin, comp);
        }
    }
}
