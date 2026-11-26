package benchmark;

import algorithms.BFS;
import algorithms.DFS;
import algorithms.Dijkstra;
import algorithms.MetodosOrdenamiento;
import io.GestorCSV;
import model.Arista;
import model.Grafo;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class BenchmarkService {

    public static class MetricaBenchmark {
        public String tamañoNombre;
        public int vertices;
        public int aristas;
        public double tiempoImportacionMs;
        public double tiempoConstruccionMs;
        public double tiempoBFSMs;
        public double tiempoDFSMs;
        public double tiempoDijkstraMs;
        public double tiempoQuickSortMs;
        public double tiempoShellSortMs;

        public MetricaBenchmark(String tamañoNombre, int vertices, int aristas) {
            this.tamañoNombre = tamañoNombre;
            this.vertices = vertices;
            this.aristas = aristas;
        }
    }

    public static List<MetricaBenchmark> ejecutarBenchmarkCompleto(File carpetaDatasets) {
        List<MetricaBenchmark> resultados = new ArrayList<>();

        int[] tamaños = {20, 100, 500, 1000};
        String[] nombres = {"Pequeño (20)", "Mediano (100)", "Grande (500)", "Muy Grande (1000)"};
        int[] extras = {25, 200, 1500, 4000};

        for (int i = 0; i < tamaños.length; i++) {
            int n = tamaños[i];
            String nombre = nombres[i];
            int ext = extras[i];

            File csvFile = new File(carpetaDatasets, "temp_bench_" + n + ".csv");
            Grafo grafoBase = GeneradorGrafos.generar(n, false, ext);
            GestorCSV.exportarCSV(csvFile, grafoBase);

            MetricaBenchmark metrica = new MetricaBenchmark(nombre, n, grafoBase.getTamaño());

            Grafo gImportado = new Grafo(false);
            long t0 = System.nanoTime();
            GestorCSV.importarCSV(csvFile, gImportado);
            long t1 = System.nanoTime();
            metrica.tiempoImportacionMs = (t1 - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            Grafo gConstruido = GeneradorGrafos.generar(n, false, ext);
            t1 = System.nanoTime();
            metrica.tiempoConstruccionMs = (t1 - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            BFS.ejecutar(gImportado, 1);
            t1 = System.nanoTime();
            metrica.tiempoBFSMs = (t1 - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            DFS.ejecutar(gImportado, 1);
            t1 = System.nanoTime();
            metrica.tiempoDFSMs = (t1 - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            Dijkstra.encontrarRuta(gImportado, 1, n);
            t1 = System.nanoTime();
            metrica.tiempoDijkstraMs = (t1 - t0) / 1_000_000.0;

            List<Arista> listaAristas = gImportado.getTodasLasAristas();
            Arista[] arrQuick = listaAristas.toArray(new Arista[0]);
            Arista[] arrShell = listaAristas.toArray(new Arista[0]);
            Comparator<Arista> compPeso = Comparator.comparingDouble(Arista::getPeso);

            t0 = System.nanoTime();
            MetodosOrdenamiento.quickSortAristas(arrQuick, compPeso);
            t1 = System.nanoTime();
            metrica.tiempoQuickSortMs = (t1 - t0) / 1_000_000.0;

            t0 = System.nanoTime();
            MetodosOrdenamiento.shellSortAristas(arrShell, compPeso);
            t1 = System.nanoTime();
            metrica.tiempoShellSortMs = (t1 - t0) / 1_000_000.0;

            resultados.add(metrica);

            if (csvFile.exists()) csvFile.delete();
        }

        return resultados;
    }
}
