package ActividadGrafos;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main {

    // lee la matriz de adyacencia desde un archivo de texto y construye el grafo
    public static Grafo leerGrafo(String nombre) throws IOException {
        List<int[]> filas = new ArrayList<>();
        int maxCols = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(nombre))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;
                String[] partes = linea.split("\\s+");
                int[] f = new int[partes.length];
                for (int i = 0; i < partes.length; i++) {
                    f[i] = Integer.parseInt(partes[i]);
                }
                filas.add(f);
                if (partes.length > maxCols) {
                    maxCols = partes.length;
                }
            }
        }

        int n = maxCols;
        Grafo g = new Grafo(n);

        for (int i = 0; i < filas.size(); i++) {
            int[] fila = filas.get(i);
            for (int j = 0; j < fila.length; j++) {
                if (fila[j] == 1) {
                    g.setArco(i, j);
                }
            }
        }

        return g;
    }


    public static void main(String[] args) {
        try {

            Grafo g = leerGrafo("D:\\Documentos\\Programacion\\java\\ActividadGrafos\\Grafo.txt");

            ForkJoinGenerator gen = new ForkJoinGenerator(g);
            gen.generar();

            System.out.println("=== Codigo FORK/JOIN generado ===");
            for (String linea : gen.getSalida()) {
                System.out.println(linea);
            }

        } catch (IOException e) {
            System.out.println("Error al leer el archivo: " + e.getMessage());
        }
    }
}

