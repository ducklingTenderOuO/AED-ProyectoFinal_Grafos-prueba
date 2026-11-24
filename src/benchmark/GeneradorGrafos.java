package benchmark;

import io.GestorCSV;
import model.Grafo;

import java.io.File;
import java.util.Random;

public class GeneradorGrafos {

    public static Grafo generar(int n, boolean dirigido, int aristasExtra) {
        Grafo g = new Grafo(dirigido);
        if (n <= 0) return g;

        for (int i = 1; i <= n; i++) {
            g.agregarVertice(i);
        }

        if (n == 1) return g;

        Random rand = new Random(42 + n);

        for (int i = 1; i < n; i++) {
            double peso = 10 + rand.nextInt(90);
            g.agregarArista(i, i + 1, peso);
        }

        int agregadas = 0;
        int intentos = 0;
        int maxIntentos = aristasExtra * 10;
        while (agregadas < aristasExtra && intentos < maxIntentos) {
            intentos++;
            int u = 1 + rand.nextInt(n);
            int v = 1 + rand.nextInt(n);
            if (u != v && !g.contieneArista(u, v)) {
                double peso = 5 + rand.nextInt(195);
                g.agregarArista(u, v, peso);
                agregadas++;
            }
        }

        return g;
    }

    public static void generarDatasetsIniciales(File carpetaDestino) {
        if (!carpetaDestino.exists()) {
            carpetaDestino.mkdirs();
        }

        Grafo g20 = generar(20, false, 25);
        GestorCSV.exportarCSV(new File(carpetaDestino, "grafo_pequeno_20.csv"), g20);

        Grafo g100 = generar(100, false, 200);
        GestorCSV.exportarCSV(new File(carpetaDestino, "grafo_mediano_100.csv"), g100);

        Grafo g500 = generar(500, false, 1500);
        GestorCSV.exportarCSV(new File(carpetaDestino, "grafo_grande_500.csv"), g500);

        Grafo g1000 = generar(1000, false, 4000);
        GestorCSV.exportarCSV(new File(carpetaDestino, "grafo_muy_grande_1000.csv"), g1000);
    }
}
