package gui;

import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import model.Arista;
import model.Grafo;
import structures.ListaSimple;
import structures.Nodo;

import java.util.*;

public class GrafoCanvas extends Pane {
    private static final int MAX_NODOS_DIBUJABLES = 50;
    private static final double RADIO_NODO = 18.0;

    private final Canvas canvas;
    private Grafo grafo;

    private final Map<Integer, Point2D> posiciones;
    private final Set<Integer> nodosResaltados;
    private final List<Integer> rutaDijkstra;
    private Color colorResaltado = Color.web("#10b981");

    public GrafoCanvas() {
        this.canvas = new Canvas(800, 600);
        this.posiciones = new HashMap<>();
        this.nodosResaltados = new LinkedHashSet<>();
        this.rutaDijkstra = new ArrayList<>();

        getChildren().add(canvas);

        widthProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setWidth(newVal.doubleValue());
            recalcularPosiciones();
            dibujar();
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            canvas.setHeight(newVal.doubleValue());
            recalcularPosiciones();
            dibujar();
        });
    }

    public void setGrafo(Grafo grafo) {
        this.grafo = grafo;
        this.nodosResaltados.clear();
        this.rutaDijkstra.clear();
        recalcularPosiciones();
        dibujar();
    }

    public void resaltarRutaDijkstra(List<Integer> ruta) {
        this.rutaDijkstra.clear();
        this.nodosResaltados.clear();
        if (ruta != null) {
            this.rutaDijkstra.addAll(ruta);
            this.nodosResaltados.addAll(ruta);
        }
        dibujar();
    }

    public void resaltarNodosBFS(Collection<Integer> nodos) {
        this.nodosResaltados.clear();
        this.rutaDijkstra.clear();
        this.colorResaltado = Color.web("#10b981");
        if (nodos != null) {
            this.nodosResaltados.addAll(nodos);
        }
        dibujar();
    }

    public void resaltarNodosDFS(Collection<Integer> nodos) {
        this.nodosResaltados.clear();
        this.rutaDijkstra.clear();
        this.colorResaltado = Color.web("#ea580c");
        if (nodos != null) {
            this.nodosResaltados.addAll(nodos);
        }
        dibujar();
    }

    public void limpiarResaltados() {
        this.nodosResaltados.clear();
        this.rutaDijkstra.clear();
        dibujar();
    }

    public void recalcularPosiciones() {
        posiciones.clear();
        if (grafo == null) return;

        List<Integer> verts = grafo.getVertices();
        int total = Math.min(verts.size(), MAX_NODOS_DIBUJABLES);
        if (total == 0) return;

        double w = Math.max(canvas.getWidth(), 300);
        double h = Math.max(canvas.getHeight(), 300);
        double centroX = w / 2.0;
        double centroY = h / 2.0;
        double radioDistribucion = Math.min(centroX, centroY) - 50.0;
        if (radioDistribucion < 40) radioDistribucion = 40;

        for (int i = 0; i < total; i++) {
            double angulo = 2 * Math.PI * i / total - (Math.PI / 2);
            double x = centroX + radioDistribucion * Math.cos(angulo);
            double y = centroY + radioDistribucion * Math.sin(angulo);
            posiciones.put(verts.get(i), new Point2D(x, y));
        }
    }

    public void dibujar() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double w = canvas.getWidth();
        double h = canvas.getHeight();

        gc.setFill(Color.web("#121212"));
        gc.fillRect(0, 0, w, h);

        if (grafo == null || grafo.getOrden() == 0) {
            gc.setFill(Color.web("#71717a"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 15));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Grafo vacío. Carga un archivo CSV o crea conexiones en el panel derecho.", w / 2, h / 2);
            return;
        }

        int totalNodos = grafo.getOrden();
        boolean excedeLimite = totalNodos > MAX_NODOS_DIBUJABLES;

        if (excedeLimite) {
            gc.setFill(Color.web("#27272a"));
            gc.fillRect(10, 10, w - 20, 32);
            gc.setStroke(Color.web("#f59e0b"));
            gc.setLineWidth(1.0);
            gc.strokeRect(10, 10, w - 20, 32);

            gc.setFill(Color.web("#fbbf24"));
            gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText("Aviso: El grafo contiene " + totalNodos + " vértices. Por rendimiento se muestran los primeros 50 en pantalla.", w / 2, 30);
        }

        Set<String> aristasEnRuta = new HashSet<>();
        for (int i = 0; i < rutaDijkstra.size() - 1; i++) {
            int u = rutaDijkstra.get(i);
            int v = rutaDijkstra.get(i + 1);
            aristasEnRuta.add(u + "->" + v);
            if (!grafo.isDirigido()) {
                aristasEnRuta.add(v + "->" + u);
            }
        }

        for (Map.Entry<Integer, Point2D> entry : posiciones.entrySet()) {
            int u = entry.getKey();
            Point2D pU = entry.getValue();
            ListaSimple<Arista> ady = grafo.obtenerAdyacentes(u);
            if (ady == null) continue;

            Nodo<Arista> act = ady.getInicio();
            while (act != null) {
                Arista arista = act.getInfo();
                int v = arista.getDestino();

                if (posiciones.containsKey(v)) {

                    if (!grafo.isDirigido() && u > v) {
                        act = act.getSig();
                        continue;
                    }

                    Point2D pV = posiciones.get(v);
                    boolean esRutaOptima = aristasEnRuta.contains(u + "->" + v);

                    boolean esBidireccional = grafo.isDirigido() && grafo.contieneArista(v, u);

                    dibujarArista(gc, pU, pV, arista.getPeso(), grafo.isDirigido(), esRutaOptima, esBidireccional);
                }
                act = act.getSig();
            }
        }

        for (Map.Entry<Integer, Point2D> entry : posiciones.entrySet()) {
            int id = entry.getKey();
            Point2D pos = entry.getValue();
            boolean resaltado = nodosResaltados.contains(id);
            boolean esRutaDijkstra = rutaDijkstra.contains(id);

            dibujarVertice(gc, id, pos.getX(), pos.getY(), resaltado, esRutaDijkstra);
        }
    }

    private void dibujarArista(GraphicsContext gc, Point2D p1, Point2D p2, double peso, boolean dirigida, boolean resaltada, boolean curvada) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double dist = Math.hypot(dx, dy);
        if (dist == 0) return;

        double ux = dx / dist;
        double uy = dy / dist;

        if (resaltada) {
            gc.setStroke(Color.web("#f43f5e"));
            gc.setLineWidth(3.5);
        } else {
            gc.setStroke(Color.web("#71717a"));
            gc.setLineWidth(1.8);
        }

        double startX = p1.getX() + ux * RADIO_NODO;
        double startY = p1.getY() + uy * RADIO_NODO;
        double endX = p2.getX() - ux * RADIO_NODO;
        double endY = p2.getY() - uy * RADIO_NODO;

        double etiquetaX;
        double etiquetaY;

        if (curvada) {

            double nx = -uy;
            double ny = ux;
            double curvatura = 28.0;

            double cx = (startX + endX) / 2.0 + nx * curvatura;
            double cy = (startY + endY) / 2.0 + ny * curvatura;

            gc.beginPath();
            gc.moveTo(startX, startY);
            gc.quadraticCurveTo(cx, cy, endX, endY);
            gc.stroke();

            double tDx = endX - cx;
            double tDy = endY - cy;
            double tAngle = Math.atan2(tDy, tDx);

            dibujarFlecha(gc, endX, endY, tAngle, resaltada);

            etiquetaX = cx;
            etiquetaY = cy;
        } else {

            gc.strokeLine(startX, startY, endX, endY);

            if (dirigida) {
                double arrowAngle = Math.atan2(dy, dx);
                dibujarFlecha(gc, endX, endY, arrowAngle, resaltada);
            }

            etiquetaX = (startX + endX) / 2.0;
            etiquetaY = (startY + endY) / 2.0;
        }

        String textoPeso = (peso == (long) peso) ? String.format("%d", (long) peso) : String.format("%.1f", peso);
        gc.setFill(Color.web("#27272a"));
        gc.fillRoundRect(etiquetaX - 14, etiquetaY - 9, 28, 18, 6, 6);
        gc.setStroke(resaltada ? Color.web("#f43f5e") : Color.web("#52525b"));
        gc.setLineWidth(1.0);
        gc.strokeRoundRect(etiquetaX - 14, etiquetaY - 9, 28, 18, 6, 6);

        gc.setFill(resaltada ? Color.web("#fb7185") : Color.web("#f4f4f5"));
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(textoPeso, etiquetaX, etiquetaY + 4);
    }

    private void dibujarFlecha(GraphicsContext gc, double x, double y, double angulo, boolean resaltada) {
        double arrowSize = 10.0;
        double ax1 = x - arrowSize * Math.cos(angulo - Math.PI / 6);
        double ay1 = y - arrowSize * Math.sin(angulo - Math.PI / 6);
        double ax2 = x - arrowSize * Math.cos(angulo + Math.PI / 6);
        double ay2 = y - arrowSize * Math.sin(angulo + Math.PI / 6);

        gc.setFill(resaltada ? Color.web("#f43f5e") : Color.web("#a1a1aa"));
        gc.fillPolygon(new double[]{x, ax1, ax2}, new double[]{y, ay1, ay2}, 3);
    }

    private void dibujarVertice(GraphicsContext gc, int id, double x, double y, boolean resaltado, boolean esRutaDijkstra) {

        gc.setFill(Color.web("#000000", 0.35));
        gc.fillOval(x - RADIO_NODO + 2, y - RADIO_NODO + 2, RADIO_NODO * 2, RADIO_NODO * 2);

        if (esRutaDijkstra) {
            gc.setFill(Color.web("#f43f5e"));
            gc.setStroke(Color.web("#fff1f2"));
        } else if (resaltado) {
            gc.setFill(colorResaltado);
            gc.setStroke(Color.web("#ffffff"));
        } else {
            gc.setFill(Color.web("#2563eb"));
            gc.setStroke(Color.web("#60a5fa"));
        }

        gc.setLineWidth(2.2);
        gc.fillOval(x - RADIO_NODO, y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);
        gc.strokeOval(x - RADIO_NODO, y - RADIO_NODO, RADIO_NODO * 2, RADIO_NODO * 2);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Segoe UI", FontWeight.BOLD, 12));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(id), x, y + 4);
    }
}
