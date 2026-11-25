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

}
