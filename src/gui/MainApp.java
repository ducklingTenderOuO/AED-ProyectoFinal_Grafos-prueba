package gui;

import algorithms.*;
import benchmark.BenchmarkService;
import io.GestorCSV;
import io.ResultadoImportacion;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import model.Arista;
import model.Grafo;
import structures.ListaSimple;
import structures.Nodo;

import java.io.File;
import java.util.*;

public class MainApp extends Application {

    private final Grafo grafo = new Grafo(false);
    private final GrafoCanvas grafoCanvas = new GrafoCanvas();

    private final Label lblOrden = new Label("0");
    private final Label lblTamaño = new Label("0");
    private final Label lblTipo = new Label("No Dirigido");

    private final ComboBox<Integer> cbInicioBFS = new ComboBox<>();
    private final ComboBox<Integer> cbInicioDFS = new ComboBox<>();
    private final ComboBox<Integer> cbOrigenDijkstra = new ComboBox<>();
    private final ComboBox<Integer> cbDestinoDijkstra = new ComboBox<>();
    private final TextArea txtSalidaAlgoritmos = new TextArea();

    private final ObservableList<Arista> listaAristasOrdenadas = FXCollections.observableArrayList();
    private final TableView<Arista> tablaOrdenamiento = new TableView<>();
    private final Label lblTiempoOrdenamiento = new Label("Tiempo: 0 ns");

    private final TableView<ObservableList<String>> tablaMatriz = new TableView<>();
    private final TextArea txtListasAdyacencia = new TextArea();

    private final TableView<BenchmarkService.MetricaBenchmark> tablaBenchmark = new TableView<>();
    private LineChart<String, Number> chartAlgoritmos;
    private BarChart<String, Number> chartOrdenamiento;
    private final ProgressBar barraBenchmark = new ProgressBar(0);
    private final Label lblEstadoBenchmark = new Label("Listo para pruebas empíricas");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane raiz = new BorderPane();
        raiz.setTop(construirEncabezado(primaryStage));

        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        Tab tabPrincipal = new Tab("1. Visualización y Control del Grafo", construirTabPrincipal(primaryStage));
        Tab tabRepresentaciones = new Tab("2. Listas y Matriz de Adyacencia", construirTabRepresentaciones());
        Tab tabBenchmark = new Tab("3. Análisis Empírico y Gráficas", construirTabBenchmark());

        tabPane.getTabs().addAll(tabPrincipal, tabRepresentaciones, tabBenchmark);
        raiz.setCenter(tabPane);

        Scene escena = new Scene(raiz, 1380, 820);
        try {
            escena.getStylesheets().add(getClass().getResource("estilos.css").toExternalForm());
        } catch (Exception ignored) {}

        primaryStage.setTitle("AED - Proyecto Final");
        primaryStage.setScene(escena);
        primaryStage.setMaximized(true);
        primaryStage.show();

        actualizarTodo();
    }

    private HBox construirEncabezado(Stage stage) {
        HBox barra = new HBox(12);
        barra.getStyleClass().add("panel-encabezado");
        barra.setAlignment(Pos.CENTER_LEFT);

        Button btnImportar = new Button("Importar CSV");
        btnImportar.setOnAction(e -> accionImportarCSV(stage));

        Button btnExportar = new Button("Guardar CSV");
        btnExportar.getStyleClass().add("button-secundario");
        btnExportar.setOnAction(e -> accionExportarCSV(stage));

        Button btnLimpiar = new Button("Limpiar Grafo");
        btnLimpiar.getStyleClass().add("button-peligro");
        btnLimpiar.setOnAction(e -> {
            grafo.limpiar();
            txtSalidaAlgoritmos.setText("Grafo limpiado.");
            actualizarTodo();
        });

        CheckBox chkDirigido = new CheckBox("Grafo Dirigido");
        chkDirigido.setStyle("-fx-text-fill: #FFFFFF; -fx-font-weight: bold;");
        chkDirigido.setSelected(grafo.isDirigido());
        chkDirigido.setOnAction(e -> {
            grafo.setDirigido(chkDirigido.isSelected());
            lblTipo.setText(grafo.isDirigido() ? "Dirigido" : "No Dirigido");
            actualizarTodo();
        });

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox panelBadges = new HBox(10);
        panelBadges.setAlignment(Pos.CENTER_RIGHT);
        panelBadges.getChildren().addAll(
                crearBadge("Vértices: ", lblOrden),
                crearBadge("Aristas: ", lblTamaño),
                crearBadge("Tipo: ", lblTipo)
        );

        barra.getChildren().addAll(btnImportar, btnExportar, btnLimpiar, chkDirigido, spacer, panelBadges);
        return barra;
    }

    private HBox crearBadge(String etiqueta, Label valor) {
        HBox box = new HBox(4);
        box.getStyleClass().add("badge-info");
        box.setAlignment(Pos.CENTER);
        Label l = new Label(etiqueta);
        l.setStyle("-fx-text-fill: #94a3b8;");
        valor.setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
        box.getChildren().addAll(l, valor);
        return box;
    }

    private SplitPane construirTabPrincipal(Stage stage) {
        SplitPane split = new SplitPane();

        VBox panelIzquierdo = new VBox(8);
        panelIzquierdo.setPadding(new Insets(10));
        HBox.setHgrow(grafoCanvas, Priority.ALWAYS);
        VBox.setVgrow(grafoCanvas, Priority.ALWAYS);
        panelIzquierdo.getChildren().add(grafoCanvas);

        VBox contenedorDerecho = new VBox(12);
        contenedorDerecho.setPadding(new Insets(14));
        contenedorDerecho.setPrefWidth(430);

        VBox cardAristas = new VBox(8);
        cardAristas.getStyleClass().add("tarjeta");
        Label lblTArista = new Label("1. GESTIÓN DE CONEXIONES (ARISTAS)");
        lblTArista.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 13px;");

        HBox filaCampos = new HBox(8);
        filaCampos.setAlignment(Pos.CENTER_LEFT);
        TextField txtOrigen = new TextField();
        txtOrigen.setPromptText("Origen (int)");
        txtOrigen.setPrefWidth(100);
        TextField txtDestino = new TextField();
        txtDestino.setPromptText("Destino (int)");
        txtDestino.setPrefWidth(100);
        TextField txtPeso = new TextField();
        txtPeso.setPromptText("Peso (>=0)");
        txtPeso.setPrefWidth(110);
        filaCampos.getChildren().addAll(txtOrigen, txtDestino, txtPeso);

        HBox filaBotonesA = new HBox(8);
        Button btnConectar = new Button("Conectar / Modificar");
        btnConectar.setOnAction(e -> {
            try {
                int u = Integer.parseInt(txtOrigen.getText().trim());
                int v = Integer.parseInt(txtDestino.getText().trim());
                double w = Double.parseDouble(txtPeso.getText().trim());

                if (u == v) {
                    mostrarAlerta(Alert.AlertType.ERROR, "No se permiten lazos: un vértice no puede conectarse consigo mismo.");
                    return;
                }
                if (w < 0) {
                    mostrarAlerta(Alert.AlertType.ERROR, "El peso no puede ser negativo.");
                    return;
                }

                grafo.agregarArista(u, v, w);
                txtSalidaAlgoritmos.setText("Conexión registrada: " + u + " -> " + v + " (peso=" + w + ")");
                txtOrigen.clear();
                txtDestino.clear();
                txtPeso.clear();
                actualizarTodo();
            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Origen y Destino deben ser números enteros, y Peso debe ser numérico.");
            }
        });

        Button btnEliminarArista = new Button("Desconectar");
        btnEliminarArista.getStyleClass().add("button-secundario");
        btnEliminarArista.setOnAction(e -> {
            try {
                int u = Integer.parseInt(txtOrigen.getText().trim());
                int v = Integer.parseInt(txtDestino.getText().trim());
                if (grafo.eliminarArista(u, v)) {
                    txtSalidaAlgoritmos.setText("Arista " + u + " - " + v + " eliminada.");
                    actualizarTodo();
                } else {
                    mostrarAlerta(Alert.AlertType.WARNING, "No existe conexión entre " + u + " y " + v);
                }
            } catch (NumberFormatException ex) {
                mostrarAlerta(Alert.AlertType.ERROR, "Ingresa los vértices Origen y Destino enteros a desconectar.");
            }
        });
        filaBotonesA.getChildren().addAll(btnConectar, btnEliminarArista);
        cardAristas.getChildren().addAll(lblTArista, filaCampos, filaBotonesA);

        VBox cardAlgoritmos = new VBox(8);
        cardAlgoritmos.getStyleClass().add("tarjeta");
        Label lblTAlg = new Label("2. BÚSQUEDAS (BFS / DFS) Y RUTA ÓPTIMA (DIJKSTRA)");
        lblTAlg.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 13px;");

        HBox filaBFS = new HBox(8);
        filaBFS.setAlignment(Pos.CENTER_LEFT);
        cbInicioBFS.setPrefWidth(85);
        Button btnBFS = new Button("BFS (Anchura)");
        btnBFS.getStyleClass().add("button-exito");
        btnBFS.setOnAction(e -> {
            Integer ini = cbInicioBFS.getValue();
            if (ini == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un vértice inicial para BFS.");
                return;
            }
            long t0 = System.nanoTime();
            List<Integer> orden = BFS.ejecutar(grafo, ini);
            long t1 = System.nanoTime();
            txtSalidaAlgoritmos.setText("BFS desde Vértice " + ini + " (" + orden.size() + " nodos visitados):\n" + orden + "\nTiempo: " + (t1 - t0) + " ns (" + String.format("%.3f", (t1 - t0) / 1_000_000.0) + " ms)");
            grafoCanvas.resaltarNodosBFS(orden);
        });
        filaBFS.getChildren().addAll(new Label("Inicio BFS:"), cbInicioBFS, btnBFS);

        HBox filaDFS = new HBox(8);
        filaDFS.setAlignment(Pos.CENTER_LEFT);
        cbInicioDFS.setPrefWidth(85);
        Button btnDFS = new Button("DFS (Profundidad)");
        btnDFS.getStyleClass().add("button-naranja");
        btnDFS.setOnAction(e -> {
            Integer ini = cbInicioDFS.getValue();
            if (ini == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona un vértice inicial para DFS.");
                return;
            }
            long t0 = System.nanoTime();
            List<Integer> orden = DFS.ejecutar(grafo, ini);
            long t1 = System.nanoTime();
            txtSalidaAlgoritmos.setText("DFS desde Vértice " + ini + " (" + orden.size() + " nodos visitados):\n" + orden + "\nTiempo: " + (t1 - t0) + " ns (" + String.format("%.3f", (t1 - t0) / 1_000_000.0) + " ms)");
            grafoCanvas.resaltarNodosDFS(orden);
        });
        filaDFS.getChildren().addAll(new Label("Inicio DFS:"), cbInicioDFS, btnDFS);

        HBox filaDijkstra = new HBox(8);
        filaDijkstra.setAlignment(Pos.CENTER_LEFT);
        cbOrigenDijkstra.setPrefWidth(75);
        cbDestinoDijkstra.setPrefWidth(75);
        Button btnDijkstra = new Button("Dijkstra");
        btnDijkstra.setStyle("-fx-background-color: #f43f5e; -fx-text-fill: white; -fx-font-weight: bold;");
        btnDijkstra.setOnAction(e -> {
            Integer orig = cbOrigenDijkstra.getValue();
            Integer dest = cbDestinoDijkstra.getValue();
            if (orig == null || dest == null) {
                mostrarAlerta(Alert.AlertType.WARNING, "Selecciona vértice origen y destino para Dijkstra.");
                return;
            }
            long t0 = System.nanoTime();
            ResultadoDijkstra res = Dijkstra.encontrarRuta(grafo, orig, dest);
            long t1 = System.nanoTime();

            if (res.isAlcanzable()) {
                txtSalidaAlgoritmos.setText("RUTA ÓPTIMA DIJKSTRA:\n" +
                        "Secuencia de vértices: " + res.getRuta() + "\n" +
                        "Costo total: " + res.getCostoTotal() + "\n" +
                        "Tiempo: " + (t1 - t0) + " ns (" + String.format("%.3f", (t1 - t0) / 1_000_000.0) + " ms)");
                grafoCanvas.resaltarRutaDijkstra(res.getRuta());
            } else {
                txtSalidaAlgoritmos.setText("No existe ruta entre " + orig + " y " + dest + ".");
                grafoCanvas.limpiarResaltados();
            }
        });
        filaDijkstra.getChildren().addAll(new Label("De:"), cbOrigenDijkstra, new Label("A:"), cbDestinoDijkstra, btnDijkstra);

        Button btnLimpiarResaltado = new Button("Limpiar Resaltado en Canvas");
        btnLimpiarResaltado.getStyleClass().add("button-secundario");
        btnLimpiarResaltado.setOnAction(e -> grafoCanvas.limpiarResaltados());

        txtSalidaAlgoritmos.setEditable(false);
        txtSalidaAlgoritmos.setPrefRowCount(4);
        txtSalidaAlgoritmos.setWrapText(true);

        cardAlgoritmos.getChildren().addAll(lblTAlg, filaBFS, filaDFS, filaDijkstra, btnLimpiarResaltado, txtSalidaAlgoritmos);

        VBox cardOrdenamientos = new VBox(8);
        cardOrdenamientos.getStyleClass().add("tarjeta");
        Label lblTOrd = new Label("3. ORDENAMIENTO DE CONEXIONES");
        lblTOrd.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 13px;");

        HBox filaCtrlOrd = new HBox(8);
        filaCtrlOrd.setAlignment(Pos.CENTER_LEFT);
        ComboBox<String> cbCriterio = new ComboBox<>();
        cbCriterio.getItems().addAll("Por Peso", "Por Vértice Origen");
        cbCriterio.setValue("Por Peso");
        cbCriterio.setPrefWidth(130);

        Button btnQuickSort = new Button("QuickSort");
        Button btnShellSort = new Button("ShellSort");
        filaCtrlOrd.getChildren().addAll(cbCriterio, btnQuickSort, btnShellSort);

        configurarTablaOrdenamiento();
        tablaOrdenamiento.setPrefHeight(180);

        btnQuickSort.setOnAction(e -> {
            Arista[] arr = listaAristasOrdenadas.toArray(new Arista[0]);
            boolean porPeso = cbCriterio.getValue().contains("Peso");
            Comparator<Arista> comp = porPeso ? Comparator.comparingDouble(Arista::getPeso) : Comparator.comparingInt(Arista::getOrigen);

            long t0 = System.nanoTime();
            MetodosOrdenamiento.quickSortAristas(arr, comp);
            long t1 = System.nanoTime();

            listaAristasOrdenadas.setAll(arr);
            lblTiempoOrdenamiento.setText("QuickSort: " + (t1 - t0) + " ns (" + String.format("%.4f", (t1 - t0) / 1_000_000.0) + " ms)");
        });

        btnShellSort.setOnAction(e -> {
            Arista[] arr = listaAristasOrdenadas.toArray(new Arista[0]);
            boolean porPeso = cbCriterio.getValue().contains("Peso");
            Comparator<Arista> comp = porPeso ? Comparator.comparingDouble(Arista::getPeso) : Comparator.comparingInt(Arista::getOrigen);

            long t0 = System.nanoTime();
            MetodosOrdenamiento.shellSortAristas(arr, comp);
            long t1 = System.nanoTime();

            listaAristasOrdenadas.setAll(arr);
            lblTiempoOrdenamiento.setText("ShellSort: " + (t1 - t0) + " ns (" + String.format("%.4f", (t1 - t0) / 1_000_000.0) + " ms)");
        });

        cardOrdenamientos.getChildren().addAll(lblTOrd, filaCtrlOrd, lblTiempoOrdenamiento, tablaOrdenamiento);

        contenedorDerecho.getChildren().addAll(cardAristas, cardAlgoritmos, cardOrdenamientos);

        ScrollPane scrollDerecho = new ScrollPane(contenedorDerecho);
        scrollDerecho.setFitToWidth(true);

        split.getItems().addAll(panelIzquierdo, scrollDerecho);
        split.setDividerPositions(0.65);
        return split;
    }

    private void configurarTablaOrdenamiento() {
        TableColumn<Arista, Number> colOrigen = new TableColumn<>("Origen");
        colOrigen.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getOrigen()));
        colOrigen.setPrefWidth(90);
        colOrigen.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<Arista, Number> colDestino = new TableColumn<>("Destino");
        colDestino.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().getDestino()));
        colDestino.setPrefWidth(90);
        colDestino.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<Arista, Number> colPeso = new TableColumn<>("Peso");
        colPeso.setCellValueFactory(d -> new SimpleDoubleProperty(d.getValue().getPeso()));
        colPeso.setPrefWidth(120);
        colPeso.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        tablaOrdenamiento.getColumns().setAll(colOrigen, colDestino, colPeso);
        tablaOrdenamiento.setItems(listaAristasOrdenadas);
    }

    private SplitPane construirTabRepresentaciones() {
        SplitPane split = new SplitPane();

        VBox panelMatriz = new VBox(10);
        panelMatriz.setPadding(new Insets(15));
        Label lblMat = new Label("MATRIZ DE ADYACENCIA");
        lblMat.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 14px;");
        Label lblSubMat = new Label("Filas = Origen, Columnas = Destino. Celdas con '-' indican que no existe conexión.");
        lblSubMat.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        VBox.setVgrow(tablaMatriz, Priority.ALWAYS);
        panelMatriz.getChildren().addAll(lblMat, lblSubMat, tablaMatriz);

        VBox panelListas = new VBox(10);
        panelListas.setPadding(new Insets(15));
        Label lblLis = new Label("LISTAS DE ADYACENCIA");
        lblLis.setStyle("-fx-font-weight: bold; -fx-text-fill: #38bdf8; -fx-font-size: 14px;");
        Label lblSubLis = new Label("Cada vértice mantiene su propia ListaSimple de aristas salientes encadenadas.");
        lblSubLis.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");

        txtListasAdyacencia.setEditable(false);
        txtListasAdyacencia.setFont(javafx.scene.text.Font.font("Consolas", 12));
        txtListasAdyacencia.setStyle("-fx-text-fill: #FFFFFF; -fx-background-color: #27272a;");
        VBox.setVgrow(txtListasAdyacencia, Priority.ALWAYS);
        panelListas.getChildren().addAll(lblLis, lblSubLis, txtListasAdyacencia);

        split.getItems().addAll(panelMatriz, panelListas);
        split.setDividerPositions(0.5);
        return split;
    }

    private VBox construirTabBenchmark() {
        VBox raiz = new VBox(12);
        raiz.setPadding(new Insets(16));

        HBox barraAcciones = new HBox(12);
        barraAcciones.setAlignment(Pos.CENTER_LEFT);

        Button btnEjecutarBench = new Button("Ejecutar Benchmark (20, 100, 500, 1000 Nodos)");
        btnEjecutarBench.getStyleClass().add("button-exito");
        barraBenchmark.setPrefWidth(220);

        barraAcciones.getChildren().addAll(btnEjecutarBench, barraBenchmark, lblEstadoBenchmark);

        TableColumn<BenchmarkService.MetricaBenchmark, String> cTam = new TableColumn<>("Tamaño Grafo");
        cTam.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tamañoNombre));
        cTam.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, Number> cV = new TableColumn<>("Vértices");
        cV.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().vertices));
        cV.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, Number> cE = new TableColumn<>("Aristas");
        cE.setCellValueFactory(d -> new SimpleIntegerProperty(d.getValue().aristas));
        cE.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cImport = new TableColumn<>("Import CSV (ms)");
        cImport.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.3f", d.getValue().tiempoImportacionMs)));
        cImport.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cConst = new TableColumn<>("Construcción (ms)");
        cConst.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.3f", d.getValue().tiempoConstruccionMs)));
        cConst.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cBFS = new TableColumn<>("BFS (ms)");
        cBFS.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.3f", d.getValue().tiempoBFSMs)));
        cBFS.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cDFS = new TableColumn<>("DFS (ms)");
        cDFS.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.3f", d.getValue().tiempoDFSMs)));
        cDFS.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cDij = new TableColumn<>("Dijkstra (ms)");
        cDij.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.3f", d.getValue().tiempoDijkstraMs)));
        cDij.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cQuick = new TableColumn<>("QuickSort (ms)");
        cQuick.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.4f", d.getValue().tiempoQuickSortMs)));
        cQuick.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        TableColumn<BenchmarkService.MetricaBenchmark, String> cShell = new TableColumn<>("ShellSort (ms)");
        cShell.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.4f", d.getValue().tiempoShellSortMs)));
        cShell.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");

        tablaBenchmark.getColumns().setAll(cTam, cV, cE, cImport, cConst, cBFS, cDFS, cDij, cQuick, cShell);
        tablaBenchmark.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaBenchmark.setPrefHeight(150);

        HBox panelGraficas = new HBox(12);
        VBox.setVgrow(panelGraficas, Priority.ALWAYS);

        CategoryAxis xAxisAlg = new CategoryAxis();
        xAxisAlg.setLabel("Tamaño del Grafo");
        NumberAxis yAxisAlg = new NumberAxis();
        yAxisAlg.setLabel("Tiempo (ms)");
        chartAlgoritmos = new LineChart<>(xAxisAlg, yAxisAlg);
        chartAlgoritmos.setTitle("Comparativa de Búsqueda y Rutas (BFS, DFS, Dijkstra)");
        HBox.setHgrow(chartAlgoritmos, Priority.ALWAYS);

        CategoryAxis xAxisOrd = new CategoryAxis();
        xAxisOrd.setLabel("Tamaño del Grafo");
        NumberAxis yAxisOrd = new NumberAxis();
        yAxisOrd.setLabel("Tiempo (ms)");
        chartOrdenamiento = new BarChart<>(xAxisOrd, yAxisOrd);
        chartOrdenamiento.setTitle("Comparativa de Ordenamientos: QuickSort vs Shell Sort");
        HBox.setHgrow(chartOrdenamiento, Priority.ALWAYS);

        panelGraficas.getChildren().addAll(chartAlgoritmos, chartOrdenamiento);

        btnEjecutarBench.setOnAction(e -> {
            btnEjecutarBench.setDisable(true);
            lblEstadoBenchmark.setText("Ejecutando pruebas empíricas...");
            barraBenchmark.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);

            new Thread(() -> {
                File dir = new File("datasets");
                List<BenchmarkService.MetricaBenchmark> lista = BenchmarkService.ejecutarBenchmarkCompleto(dir);

                Platform.runLater(() -> {
                    tablaBenchmark.setItems(FXCollections.observableArrayList(lista));
                    actualizarGraficasBenchmark(lista);
                    barraBenchmark.setProgress(1.0);
                    lblEstadoBenchmark.setText("Pruebas completadas exitosamente.");
                    btnEjecutarBench.setDisable(false);
                });
            }).start();
        });

        raiz.getChildren().addAll(barraAcciones, tablaBenchmark, panelGraficas);
        return raiz;
    }

    private void actualizarGraficasBenchmark(List<BenchmarkService.MetricaBenchmark> lista) {
        chartAlgoritmos.getData().clear();
        chartOrdenamiento.getData().clear();

        XYChart.Series<String, Number> sBFS = new XYChart.Series<>();
        sBFS.setName("BFS");
        XYChart.Series<String, Number> sDFS = new XYChart.Series<>();
        sDFS.setName("DFS");
        XYChart.Series<String, Number> sDij = new XYChart.Series<>();
        sDij.setName("Dijkstra");

        XYChart.Series<String, Number> sQuick = new XYChart.Series<>();
        sQuick.setName("QuickSort");
        XYChart.Series<String, Number> sShell = new XYChart.Series<>();
        sShell.setName("ShellSort");

        for (BenchmarkService.MetricaBenchmark m : lista) {
            sBFS.getData().add(new XYChart.Data<>(m.tamañoNombre, m.tiempoBFSMs));
            sDFS.getData().add(new XYChart.Data<>(m.tamañoNombre, m.tiempoDFSMs));
            sDij.getData().add(new XYChart.Data<>(m.tamañoNombre, m.tiempoDijkstraMs));

            sQuick.getData().add(new XYChart.Data<>(m.tamañoNombre, m.tiempoQuickSortMs));
            sShell.getData().add(new XYChart.Data<>(m.tamañoNombre, m.tiempoShellSortMs));
        }

        chartAlgoritmos.getData().addAll(sBFS, sDFS, sDij);
        chartOrdenamiento.getData().addAll(sQuick, sShell);
    }

    private void accionImportarCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Seleccionar archivo CSV de grafo");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV (*.csv)", "*.csv"));
        File dir = new File("datasets");
        if (dir.exists()) fc.setInitialDirectory(dir);

        File f = fc.showOpenDialog(stage);
        if (f != null) {
            ResultadoImportacion res = GestorCSV.importarCSV(f, grafo);
            txtSalidaAlgoritmos.setText(res.generarReporte());
            actualizarTodo();
            if (!res.esExitoso()) {
                mostrarAlerta(Alert.AlertType.ERROR, "Hubo errores al importar:\n" + String.join("\n", res.getErrores()));
            } else if (!res.getAdvertencias().isEmpty()) {
                mostrarAlerta(Alert.AlertType.WARNING, "Importación completada con " + res.getAdvertencias().size() + " advertencias. Revisa la salida de texto.");
            }
        }
    }

    private void accionExportarCSV(Stage stage) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Guardar grafo en archivo CSV");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos CSV (*.csv)", "*.csv"));
        fc.setInitialFileName("grafo_guardado.csv");
        File f = fc.showSaveDialog(stage);
        if (f != null) {
            if (GestorCSV.exportarCSV(f, grafo)) {
                txtSalidaAlgoritmos.setText("Grafo guardado exitosamente en: " + f.getName() + "\n(Vértices aislados omitidos conforme a la especificación).");
                mostrarAlerta(Alert.AlertType.INFORMATION, "Grafo guardado exitosamente.");
            } else {
                mostrarAlerta(Alert.AlertType.ERROR, "No se pudo guardar el archivo CSV.");
            }
        }
    }

    private void actualizarTodo() {
        int orden = grafo.getOrden();
        int tam = grafo.getTamaño();

        lblOrden.setText(String.valueOf(orden));
        lblTamaño.setText(String.valueOf(tam));
        lblTipo.setText(grafo.isDirigido() ? "Dirigido" : "No Dirigido");

        grafoCanvas.setGrafo(grafo);

        List<Integer> verts = grafo.getVertices();
        Integer selBFS = cbInicioBFS.getValue();
        Integer selDFS = cbInicioDFS.getValue();
        Integer selOrig = cbOrigenDijkstra.getValue();
        Integer selDest = cbDestinoDijkstra.getValue();

        cbInicioBFS.getItems().setAll(verts);
        cbInicioDFS.getItems().setAll(verts);
        cbOrigenDijkstra.getItems().setAll(verts);
        cbDestinoDijkstra.getItems().setAll(verts);

        if (verts.contains(selBFS)) cbInicioBFS.setValue(selBFS);
        else if (!verts.isEmpty()) cbInicioBFS.setValue(verts.get(0));

        if (verts.contains(selDFS)) cbInicioDFS.setValue(selDFS);
        else if (!verts.isEmpty()) cbInicioDFS.setValue(verts.get(0));

        if (verts.contains(selOrig)) cbOrigenDijkstra.setValue(selOrig);
        else if (!verts.isEmpty()) cbOrigenDijkstra.setValue(verts.get(0));

        if (verts.contains(selDest)) cbDestinoDijkstra.setValue(selDest);
        else if (verts.size() > 1) cbDestinoDijkstra.setValue(verts.get(verts.size() - 1));

        listaAristasOrdenadas.setAll(grafo.getTodasLasAristas());
        lblTiempoOrdenamiento.setText("Total aristas: " + listaAristasOrdenadas.size());

        actualizarVistasDuales();
    }

    private void actualizarVistasDuales() {

        tablaMatriz.getColumns().clear();
        tablaMatriz.getItems().clear();

        List<Integer> verts = grafo.getVertices();
        int n = verts.size();
        if (n == 0) return;

        int nVisual = Math.min(n, 100);

        TableColumn<ObservableList<String>, String> colVertice = new TableColumn<>("Vértice");
        colVertice.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(0)));
        colVertice.setStyle("-fx-font-weight: bold; -fx-alignment: center; -fx-text-fill: #38bdf8;");
        tablaMatriz.getColumns().add(colVertice);

        for (int j = 0; j < nVisual; j++) {
            final int colIndex = j + 1;
            int destId = verts.get(j);
            TableColumn<ObservableList<String>, String> col = new TableColumn<>(String.valueOf(destId));
            col.setCellValueFactory(param -> new SimpleStringProperty(param.getValue().get(colIndex)));
            col.setStyle("-fx-alignment: center; -fx-text-fill: #FFFFFF;");
            col.setPrefWidth(55);
            tablaMatriz.getColumns().add(col);
        }

        double[][] matriz = grafo.getMatrizAdyacencia();
        ObservableList<ObservableList<String>> datosMatriz = FXCollections.observableArrayList();

        for (int i = 0; i < nVisual; i++) {
            ObservableList<String> fila = FXCollections.observableArrayList();
            fila.add(String.valueOf(verts.get(i)));
            for (int j = 0; j < nVisual; j++) {
                double val = matriz[i][j];
                if (val > 0) {
                    fila.add((val == (long) val) ? String.valueOf((long) val) : String.format("%.1f", val));
                } else {
                    fila.add("-");
                }
            }
            datosMatriz.add(fila);
        }
        tablaMatriz.setItems(datosMatriz);

        StringBuilder sb = new StringBuilder();
        sb.append("LISTAS DE ADYACENCIA (Total vértices: ").append(n).append(")\n\n");
        for (Integer u : verts) {
            sb.append(" [Vértice ").append(u).append("] -> ");
            ListaSimple<Arista> ady = grafo.obtenerAdyacentes(u);
            if (ady == null || ady.estaVacia()) {
                sb.append("(sin conexiones salientes)\n");
            } else {
                Nodo<Arista> act = ady.getInicio();
                while (act != null) {
                    Arista a = act.getInfo();
                    sb.append("Destino: ").append(a.getDestino())
                            .append(" (peso: ").append(a.getPeso()).append(")");
                    if (act.getSig() != null) sb.append("  -->  ");
                    act = act.getSig();
                }
                sb.append("\n");
            }
        }
        txtListasAdyacencia.setText(sb.toString());
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alert = new Alert(tipo, mensaje);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
