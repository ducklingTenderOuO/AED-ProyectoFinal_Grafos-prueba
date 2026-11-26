import algorithms.*;
import benchmark.BenchmarkService;
import benchmark.GeneradorGrafos;
import io.GestorCSV;
import io.ResultadoImportacion;
import model.Arista;
import model.Grafo;

import java.io.File;
import java.util.Comparator;
import java.util.List;

public class TestConsola {
    public static void main(String[] args) {
        System.out.println("==================================================");
        System.out.println("  INICIANDO PRUEBAS UNITARIAS Y DE RENDIMIENTO   ");
        System.out.println("==================================================");

        Grafo g = new Grafo(false);
        g.agregarVertice(1);
        g.agregarVertice(2);
        g.agregarVertice(3);
        g.agregarVertice(4);
        g.agregarArista(1, 2, 10.0);
        g.agregarArista(2, 3, 20.0);
        g.agregarArista(3, 4, 30.0);
        g.agregarArista(1, 4, 100.0);

        System.out.println("[OK] Grafo creado. Orden: " + g.getOrden() + ", Tamaño: " + g.getTamaño());
        double[][] mat = g.getMatrizAdyacencia();
        System.out.println("[OK] Matriz de adyacencia generada (" + mat.length + "x" + mat.length + ")");

        List<Integer> ordenBFS = BFS.ejecutar(g, 1);
        System.out.println("[OK] BFS desde 1: " + ordenBFS);

        List<Integer> ordenDFS = DFS.ejecutar(g, 1);
        System.out.println("[OK] DFS desde 1: " + ordenDFS);

        ResultadoDijkstra resDij = Dijkstra.encontrarRuta(g, 1, 4);
        System.out.println("[OK] Dijkstra 1 -> 4: " + resDij.getRuta() + " (Costo: " + resDij.getCostoTotal() + ")");
        if (resDij.getCostoTotal() != 60.0) {
            throw new RuntimeException("Dijkstra falló: se esperaba costo 60.0 pero dio " + resDij.getCostoTotal());
        }

        List<Arista> aristas = g.getTodasLasAristas();
        Arista[] arrQ = aristas.toArray(new Arista[0]);
        Arista[] arrS = aristas.toArray(new Arista[0]);
        Comparator<Arista> comp = Comparator.comparingDouble(Arista::getPeso);

        MetodosOrdenamiento.quickSortAristas(arrQ, comp);
        MetodosOrdenamiento.shellSortAristas(arrS, comp);
        System.out.println("[OK] Aristas ordenadas por QuickSort: " + arrQ[0].getPeso() + " <= " + arrQ[arrQ.length - 1].getPeso());
        System.out.println("[OK] Aristas ordenadas por ShellSort: " + arrS[0].getPeso() + " <= " + arrS[arrS.length - 1].getPeso());

        File dirDatasets = new File("datasets");
        GeneradorGrafos.generarDatasetsIniciales(dirDatasets);
        System.out.println("[OK] Datasets iniciales creados en carpeta 'datasets/'");

        System.out.println("\n--- EJECUTANDO BENCHMARK (20, 100, 500, 1000 nodos) ---");
        List<BenchmarkService.MetricaBenchmark> metricas = BenchmarkService.ejecutarBenchmarkCompleto(dirDatasets);
        for (BenchmarkService.MetricaBenchmark m : metricas) {
            System.out.printf("%-20s | V: %4d | E: %5d | Import: %6.2f ms | Build: %6.2f ms | BFS: %6.3f ms | DFS: %6.3f ms | Dij: %6.3f ms | Quick: %6.4f ms | Shell: %6.4f ms%n",
                    m.tamañoNombre, m.vertices, m.aristas, m.tiempoImportacionMs, m.tiempoConstruccionMs, m.tiempoBFSMs, m.tiempoDFSMs, m.tiempoDijkstraMs, m.tiempoQuickSortMs, m.tiempoShellSortMs);
        }

        System.out.println("\n==================================================");
        System.out.println("   TODAS LAS PRUEBAS COMPLETADAS CON ÉXITO!       ");
        System.out.println("==================================================");
    }
}
