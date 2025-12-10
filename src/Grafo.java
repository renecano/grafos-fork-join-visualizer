package ActividadGrafos;

import java.util.ArrayList;
import java.util.List;

public class Grafo {
    private int n;
    private int[][] mat;

    public Grafo(int n) {
        this.n = n;
        this.mat = new int[n][n];
    }

    public void setArco(int i, int j) {
        mat[i][j] = 1;
    }

    public int getN() {
        return n;
    }

    public int[][] getMatriz() {
        return mat;
    }

    public int outDegree(int v) {
        int c = 0;
        for (int j = 0; j < n; j++) {
            if (mat[v][j] == 1) c++;
        }
        return c;
    }

    public int inDegree(int v) {
        int c = 0;
        for (int i = 0; i < n; i++) {
            if (mat[i][v] == 1) c++;
        }
        return c;
    }

    public List<Integer> sucesores(int v) {
        List<Integer> r = new ArrayList<>();
        for (int j = 0; j < n; j++) {
            if (mat[v][j] == 1) r.add(j);
        }
        return r;
    }
}
