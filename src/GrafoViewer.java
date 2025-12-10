package ActividadGrafos;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.Cursor;
import javafx.scene.control.Tooltip;
import javafx.scene.effect.DropShadow;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.image.WritableImage;
import javafx.scene.SnapshotParameters;
import javafx.embed.swing.SwingFXUtils;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.scene.control.ScrollPane;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.File;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class GrafoViewer extends Application {

    private Timeline timelineSimulacion;

    private static final String BASE_PATH = "D:\\Documentos\\Programacion\\java\\ActividadGrafos\\";
    private static final String[] ARCHIVOS = {
            "Grafo-1.txt",
            "Grafo-2.txt",
            "Grafo-3.txt",
            "Grafo-4.txt"
    };
    private static final String[] TITULOS = {
            "Caso 1", "Caso 2", "Caso 3", "Caso 4"
    };

    public static void main(String[] args) {
        launch(args);
    }

    private void drawArrow(Pane root, double x1, double y1, double x2, double y2) {
        Line line = new Line(x1, y1, x2, y2);
        line.setStroke(Color.web("#B0BEC5"));
        line.setStrokeWidth(1.8);
        root.getChildren().add(line);

        double angle = Math.atan2(y1 - y2, x1 - x2);
        double arrowLength = 12;
        double arrowAngle = Math.toRadians(25);

        double x3 = x2 + arrowLength * Math.cos(angle + arrowAngle);
        double y3 = y2 + arrowLength * Math.sin(angle + arrowAngle);

        double x4 = x2 + arrowLength * Math.cos(angle - arrowAngle);
        double y4 = y2 + arrowLength * Math.sin(angle - arrowAngle);

        Line arrow1 = new Line(x2, y2, x3, y3);
        Line arrow2 = new Line(x2, y2, x4, y4);

        arrow1.setStroke(Color.web("#B0BEC5"));
        arrow2.setStroke(Color.web("#B0BEC5"));
        arrow1.setStrokeWidth(1.8);
        arrow2.setStrokeWidth(1.8);

        root.getChildren().addAll(arrow1, arrow2);
    }

    private void agregarFadeAlSeleccionar(Tab tab) {
        tab.setOnSelectionChanged(ev -> {
            if (tab.isSelected()) {
                BorderPane root = (BorderPane) tab.getContent();
                Pane graphPane = (Pane) root.getCenter();

                graphPane.setOpacity(0);
                FadeTransition ft = new FadeTransition(Duration.millis(700), graphPane);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.play();
            }
        });
    }

    private double[][] calcularPosiciones(Grafo grafo, double width, double height, double radioNodo) {
        int n = grafo.getN();
        int[] nivel = new int[n];
        Arrays.fill(nivel, -1);

        @SuppressWarnings("unchecked")
        List<Integer>[] preds = new List[n];
        for (int i = 0; i < n; i++) {
            preds[i] = new ArrayList<>();
        }
        for (int u = 0; u < n; u++) {
            for (int v : grafo.sucesores(u)) {
                preds[v].add(u);
            }
        }

        ArrayDeque<Integer> cola = new ArrayDeque<>();
        for (int i = 0; i < n; i++) {
            if (grafo.inDegree(i) == 0) {
                nivel[i] = 0;
                cola.add(i);
            }
        }

        if (cola.isEmpty()) {
            for (int i = 0; i < n; i++) {
                nivel[i] = 0;
            }
        } else {
            while (!cola.isEmpty()) {
                int u = cola.poll();
                for (int v : grafo.sucesores(u)) {
                    int nuevoNivel = nivel[u] + 1;
                    if (nivel[v] < nuevoNivel) {
                        nivel[v] = nuevoNivel;
                        cola.add(v);
                    }
                }
            }
        }

        for (int i = 0; i < n; i++) {
            if (nivel[i] < 0) nivel[i] = 0;
        }

        int maxNivel = 0;
        for (int v : nivel) {
            if (v > maxNivel) maxNivel = v;
        }

        List<List<Integer>> porNivel = new ArrayList<>();
        for (int i = 0; i <= maxNivel; i++) {
            porNivel.add(new ArrayList<>());
        }
        for (int i = 0; i < n; i++) {
            porNivel.get(nivel[i]).add(i);
        }

        double[][] pos = new double[n][2];

        double margenX = 60;
        double margenY = 60;

        double areaAltura = height - 2 * margenY;
        double maxGapY = radioNodo * 4.0;
        double totalHeight = (maxNivel == 0) ? 0 : Math.min(areaAltura, maxGapY * maxNivel);
        double startY = margenY + (areaAltura - totalHeight) / 2.0;
        double deltaY = (maxNivel == 0) ? 0 : totalHeight / maxNivel;

        for (int lv = 0; lv <= maxNivel; lv++) {
            List<Integer> nodos = porNivel.get(lv);
            int k = nodos.size();
            if (k == 0) continue;

            double y = (maxNivel == 0) ? height / 2.0 : startY + lv * deltaY;

            class NodeOrder {
                int id;
                double key;

                NodeOrder(int id, double key) {
                    this.id = id;
                    this.key = key;
                }
            }
            List<NodeOrder> ordenados = new ArrayList<>();

            if (lv == 0) {
                for (int idx = 0; idx < k; idx++) {
                    int nodo = nodos.get(idx);
                    ordenados.add(new NodeOrder(nodo, idx));
                }
            } else {
                for (int nodo : nodos) {
                    List<Integer> ps = preds[nodo];
                    double sumX = 0.0;
                    int count = 0;
                    for (int p : ps) {
                        if (nivel[p] < lv) {
                            sumX += pos[p][0];
                            count++;
                        }
                    }
                    double key = (count == 0) ? nodo : (sumX / count);
                    ordenados.add(new NodeOrder(nodo, key));
                }
                ordenados.sort((a, b) -> Double.compare(a.key, b.key));
            }

            double areaAncho = width - 2 * margenX;
            double maxGapX = radioNodo * 4.5;
            double totalWidth = (k <= 1) ? 0 : Math.min(areaAncho, maxGapX * (k - 1));
            double startX = margenX + (areaAncho - totalWidth) / 2.0;

            for (int idx = 0; idx < k; idx++) {
                int nodo = ordenados.get(idx).id;
                double x = (k == 1)
                        ? width / 2.0
                        : startX + idx * (totalWidth / (k - 1));
                pos[nodo][0] = x;
                pos[nodo][1] = y;
            }
        }

        return pos;
    }

    @Override
    public void start(Stage stage) {
        TabPane tabPane = new TabPane();

        for (int i = 0; i < ARCHIVOS.length; i++) {
            String archivo = ARCHIVOS[i];
            String titulo = TITULOS[i];

            try {
                Grafo g = Main.leerGrafo(BASE_PATH + archivo);
                BorderPane vista = crearVista(g);

                Tab tab = new Tab(titulo);
                tab.setContent(vista);
                tab.setClosable(false);
                agregarFadeAlSeleccionar(tab);
                tabPane.getTabs().add(tab);
            } catch (IOException e) {

                VBox box = new VBox();
                Text t = new Text("Error al leer " + archivo + ":\n" + e.getMessage());
                box.getChildren().add(t);
                Tab tabError = new Tab(titulo + " (error)");
                tabError.setContent(box);
                tabError.setClosable(false);
                tabPane.getTabs().add(tabError);
            }
        }

        Scene scene = new Scene(tabPane, 1150, 600);
        stage.setScene(scene);
        stage.setTitle("Visualizador de Grafo Fork/Join");
        stage.show();
    }

    private BorderPane crearVista(Grafo grafo) {
        double totalWidth = 1150;
        double height = 600;
        double graphWidth = 750;
        double codeWidth = totalWidth - graphWidth;
        double radioNodo = 22;

        Pane graphPane = new Pane();
        graphPane.setPrefSize(graphWidth, height);
        graphPane.setStyle(
                "-fx-background-color: linear-gradient(to bottom, #ffffff, #eef1f7);" +
                        "-fx-border-color: #d0d7e2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;"
        );

        int n = grafo.getN();
        double[][] pos = calcularPosiciones(grafo, graphWidth, height, radioNodo);

        VBox codeBox = new VBox(4);
        codeBox.setPadding(new Insets(10));
        codeBox.setPrefWidth(codeWidth);
        codeBox.setStyle(
                "-fx-background-color: #ffffff;" +
                        "-fx-border-color: #d0d7e2;" +
                        "-fx-border-radius: 10;" +
                        "-fx-background-radius: 10;" +
                        "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.10), 10, 0, 0, 2);"
        );

        ScrollPane codeScroll = new ScrollPane(codeBox);
        codeScroll.setFitToWidth(true);
        codeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        codeScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        BorderPane.setMargin(codeScroll, new Insets(10, 10, 10, 0));

        Text infoNodo = new Text("Pasa el mouse sobre un nodo...");
        infoNodo.setFont(Font.font("Consolas", 12));
        infoNodo.setFill(Color.web("#546E7A"));
        codeBox.getChildren().add(infoNodo);

        Text titulo = new Text("Código FORK/JOIN generado:");
        titulo.setFont(Font.font("Consolas", 16));
        titulo.setFill(Color.web("#263238"));
        codeBox.getChildren().add(titulo);

        Button btnExport = new Button("Exportar grafo a PNG");
        Button btnRun    = new Button("Ejecutar Fork/Join paso a paso");
        codeBox.getChildren().addAll(btnExport, btnRun);

        List<Label> lineLabels = new ArrayList<>();

        Circle[] nodoCircles = new Circle[n];
        boolean[] esForkArr  = new boolean[n];
        boolean[] esJoinArr  = new boolean[n];

        for (int i = 0; i < n; i++) {
            for (int j : grafo.sucesores(i)) {
                double x1 = pos[i][0];
                double y1 = pos[i][1];
                double x2 = pos[j][0];
                double y2 = pos[j][1];
                double dx = x2 - x1;
                double dy = y2 - y1;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < 1e-6) continue;
                double nx1 = x1 + dx * (radioNodo / dist);
                double ny1 = y1 + dy * (radioNodo / dist);
                double nx2 = x2 - dx * (radioNodo / dist);
                double ny2 = y2 - dy * (radioNodo / dist);

                drawArrow(graphPane, nx1, ny1, nx2, ny2);
            }
        }

        for (int i = 0; i < n; i++) {
            int in = grafo.inDegree(i);
            int out = grafo.outDegree(i);

            boolean esFork = out > 1;
            boolean esJoin = in > 1;
            esForkArr[i] = esFork;
            esJoinArr[i] = esJoin;

            double x = pos[i][0];
            double y = pos[i][1];

            Circle c = new Circle(x, y, radioNodo);
            if (esFork)       c.setFill(Color.web("#FFB74D"));
            else if (esJoin)  c.setFill(Color.web("#4FC3F7"));
            else              c.setFill(Color.web("#ECEFF1"));

            c.setStroke(Color.web("#37474F"));
            c.setStrokeWidth(1.6);
            graphPane.getChildren().add(c);

            nodoCircles[i] = c;

            DropShadow ds = new DropShadow();
            ds.setRadius(8);
            ds.setOffsetX(0);
            ds.setOffsetY(2);
            ds.setColor(Color.rgb(0, 0, 0, 0.2));
            c.setEffect(ds);

            Text label = new Text("S" + (i + 1));
            label.setFont(Font.font(14));
            label.setX(x - 10);
            label.setY(y + 5);
            graphPane.getChildren().add(label);

            if (esFork || esJoin) {
                Text type = new Text(esFork ? "FORK" : "JOIN");
                type.setFont(Font.font(10));
                type.setFill(Color.DARKGREEN);

                double offsetX = radioNodo + 6;
                double offsetY = 4;

                if (x > graphPane.getPrefWidth() * 0.65) {
                    offsetX = -radioNodo - 26;
                }

                type.setX(x + offsetX);
                type.setY(y + offsetY);

                graphPane.getChildren().add(type);
            }

            final int nodoIndex = i;
            String tipoTexto = esFork ? "FORK" : (esJoin ? "JOIN" : "NORMAL");
            String tooltipText =
                    "Nodo: S" + (nodoIndex + 1) + "\n" +
                            "Tipo: " + tipoTexto + "\n" +
                            "inDegree: " + in + "\n" +
                            "outDegree: " + out;

            Tooltip tooltip = new Tooltip(tooltipText);
            Tooltip.install(c, tooltip);

            c.setOnMouseEntered(ev -> {
                c.setStroke(Color.RED);
                c.setStrokeWidth(3);
                graphPane.setCursor(Cursor.HAND);

                c.setScaleX(1.07);
                c.setScaleY(1.07);

                infoNodo.setText(
                        "Nodo S" + (nodoIndex + 1) +
                                "  |  Tipo: " + tipoTexto +
                                "  |  in=" + in +
                                "  |  out=" + out
                );
            });

            c.setOnMouseExited(ev -> {
                c.setStroke(Color.web("#37474F"));
                c.setStrokeWidth(1.6);
                graphPane.setCursor(Cursor.DEFAULT);

                c.setScaleX(1.0);
                c.setScaleY(1.0);

                infoNodo.setText("Pasa el mouse sobre un nodo...");
            });

            c.setOnMouseClicked(ev -> {
                String target = "S" + (nodoIndex + 1) + ";";

                for (Label lbl : lineLabels) {
                    lbl.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 12; -fx-padding: 2 4 2 4;");
                    if (lbl.getText().contains(target)) {
                        lbl.setStyle(
                                "-fx-font-family: 'Consolas'; -fx-font-size: 12;" +
                                        "-fx-padding: 2 4 2 4;" +
                                        "-fx-background-color: #FFF9C4;" +
                                        "-fx-border-color: #FBC02D;"
                        );
                    }
                }
            });
        }

        ForkJoinGenerator gen = new ForkJoinGenerator(grafo);
        gen.generar();
        for (String linea : gen.getSalida()) {
            Label lbl = new Label(linea);
            lbl.setFont(Font.font("Consolas", 12));
            lbl.setTextFill(Color.web("#37474F"));
            lbl.setStyle("-fx-padding: 2 4 2 4;");
            lineLabels.add(lbl);
            codeBox.getChildren().add(lbl);
        }


        btnExport.setOnAction(ev -> {
            try {
                WritableImage img = graphPane.snapshot(new SnapshotParameters(), null);
                String nombre = "grafo_" + grafo.getN() + "_nodos.png";
                File archivo = new File(nombre);
                ImageIO.write(SwingFXUtils.fromFXImage(img, null), "png", archivo);
                infoNodo.setText("Grafo exportado como " + nombre);
            } catch (IOException e) {
                infoNodo.setText("Error al exportar imagen: " + e.getMessage());
            }
        });

        btnRun.setOnAction(ev -> {
            ejecutarPasoAPaso(lineLabels, nodoCircles, esForkArr, esJoinArr);
        });

        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #f3f4f7;");
        root.setCenter(graphPane);
        root.setRight(codeScroll);

        return root;
    }

    private void ejecutarPasoAPaso(List<Label> lineLabels,
                                   Circle[] nodoCircles,
                                   boolean[] esForkArr,
                                   boolean[] esJoinArr) {

        if (lineLabels.isEmpty()) return;

        if (timelineSimulacion != null) {
            timelineSimulacion.stop();
        }

        int m = lineLabels.size();
        int[] branchForLine = new int[m];
        Map<String, Integer> labelToBranch = new HashMap<>();
        int nextBranch = 1;

        int currentBranch = 0;

        for (int i = 0; i < m; i++) {
            String line = lineLabels.get(i).getText().trim();

            if (line.startsWith("FORK")) {
                int posL = line.indexOf('L');
                if (posL >= 0) {
                    int posFin = line.indexOf(';', posL);
                    String etiqueta = (posFin > posL) ? line.substring(posL, posFin) : line.substring(posL);
                    if (!labelToBranch.containsKey(etiqueta)) {
                        labelToBranch.put(etiqueta, nextBranch++);
                    }
                }
                branchForLine[i] = currentBranch;
            } else if (line.matches("L\\d+:.*")) {
                int colon = line.indexOf(':');
                String etiqueta = line.substring(0, colon);
                if (labelToBranch.containsKey(etiqueta)) {
                    currentBranch = labelToBranch.get(etiqueta);
                } else {
                    currentBranch = 0;
                }
                branchForLine[i] = currentBranch;
            } else {
                branchForLine[i] = currentBranch;
            }
        }

        Color[] branchColors = new Color[] {
                Color.web("#FFE082"),
                Color.web("#81D4FA"),
                Color.web("#C5E1A5"),
                Color.web("#FFCC80"),
                Color.web("#F48FB1")
        };

        timelineSimulacion = new Timeline();
        double stepMs = 700;

        for (int i = 0; i < m; i++) {
            final int idx = i;

            KeyFrame kf = new KeyFrame(Duration.millis(stepMs * (idx + 1)), ev -> {


                for (Label lbl : lineLabels) {
                    lbl.setStyle(
                            "-fx-font-family: 'Consolas';" +
                                    "-fx-font-size: 12;" +
                                    "-fx-padding: 2 4 2 4;"
                    );
                }


                for (int j = 0; j < nodoCircles.length; j++) {
                    Circle c = nodoCircles[j];
                    if (c == null) continue;

                    if (esForkArr[j])      c.setFill(Color.web("#FFB74D"));
                    else if (esJoinArr[j]) c.setFill(Color.web("#4FC3F7"));
                    else                   c.setFill(Color.web("#ECEFF1"));

                    c.setStroke(Color.web("#37474F"));
                    c.setStrokeWidth(1.6);
                    c.setScaleX(1.0);
                    c.setScaleY(1.0);
                }

                Label actual = lineLabels.get(idx);
                String line = actual.getText().trim();

                String baseStyle =
                        "-fx-font-family: 'Consolas';" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 2 4 2 4;";

                String estiloActual = baseStyle;

                if (line.matches("S\\d+;")) {
                    estiloActual +=
                            "-fx-background-color: #C5E1A5;" +
                                    "-fx-border-color: #558B2F;";
                } else if (line.matches("L\\d+:.*")) {
                    estiloActual +=
                            "-fx-background-color: #E1BEE7;" +
                                    "-fx-border-color: #8E24AA;";
                } else if (line.startsWith("FORK")) {
                    estiloActual +=
                            "-fx-background-color: #FFE0B2;" +
                                    "-fx-border-color: #FB8C00;";
                } else if (line.contains("JOIN")) {
                    estiloActual +=
                            "-fx-background-color: #BBDEFB;" +
                                    "-fx-border-color: #1976D2;";
                } else if (line.startsWith("GOTO")) {
                    estiloActual +=
                            "-fx-background-color: #FFCDD2;" +
                                    "-fx-border-color: #E53935;";
                } else if (line.contains("CONT")) {
                    estiloActual +=
                            "-fx-background-color: #FFF9C4;" +
                                    "-fx-border-color: #FBC02D;";
                } else {
                    estiloActual +=
                            "-fx-background-color: #CFD8DC;" +
                                    "-fx-border-color: #607D8B;";
                }

                actual.setStyle(estiloActual);

                int nodoNum = -1;

                if (line.matches("S\\d+;")) {
                    nodoNum = Integer.parseInt(line.substring(1, line.length() - 1));
                } else {
                    int posS = line.indexOf('S');
                    if (posS >= 0) {
                        int posPuntoYComa = line.indexOf(';', posS);
                        if (posPuntoYComa > posS + 1) {
                            String numStr = line.substring(posS + 1, posPuntoYComa);
                            try {
                                nodoNum = Integer.parseInt(numStr);
                            } catch (NumberFormatException ex) {
                                nodoNum = -1;
                            }
                        }
                    }
                }

                if (nodoNum >= 1 && nodoNum <= nodoCircles.length) {
                    Circle c = nodoCircles[nodoNum - 1];
                    if (c != null) {
                        int branchId = branchForLine[idx];
                        if (branchId < 0) branchId = 0;
                        if (branchId >= branchColors.length) branchId = branchColors.length - 1;

                        c.setFill(branchColors[branchId]);
                        c.setStroke(Color.RED);
                        c.setStrokeWidth(3);
                        c.setScaleX(1.07);
                        c.setScaleY(1.07);
                    }
                }
            });

            timelineSimulacion.getKeyFrames().add(kf);
        }

        KeyFrame kfFinal = new KeyFrame(Duration.millis(stepMs * (m + 1)), ev -> {
            for (Label lbl : lineLabels) {
                lbl.setStyle(
                        "-fx-font-family: 'Consolas';" +
                                "-fx-font-size: 12;" +
                                "-fx-padding: 2 4 2 4;"
                );
            }

            for (int j = 0; j < nodoCircles.length; j++) {
                Circle c = nodoCircles[j];
                if (c == null) continue;

                if (esForkArr[j])      c.setFill(Color.web("#FFB74D"));
                else if (esJoinArr[j]) c.setFill(Color.web("#4FC3F7"));
                else                   c.setFill(Color.web("#ECEFF1"));

                c.setStroke(Color.web("#37474F"));
                c.setStrokeWidth(1.6);
                c.setScaleX(1.0);
                c.setScaleY(1.0);
            }
        });

        timelineSimulacion.getKeyFrames().add(kfFinal);

        timelineSimulacion.play();
    }

}
