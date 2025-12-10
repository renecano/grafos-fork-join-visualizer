package ActividadGrafos;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ForkJoinGenerator {

    private final Grafo g;
    private final List<String> codigo = new ArrayList<>();


    private int etiquetaCounter = 1;

    // --- información de los nodos JOIN ---
    private boolean[] esJoin;
    private String[] etqJoin;
    private String[] varCont;
    private int[] ramas;
    private int[] pendientesJoin;
    private boolean[] bloqueGenerado;
    private final List<Integer> listaJoins = new ArrayList<>();


    private static class RamaPendiente {
        String etiqueta;
        int inicio;

        RamaPendiente(String etiqueta, int inicio) {
            this.etiqueta = etiqueta;
            this.inicio = inicio;
        }
    }
    private final List<RamaPendiente> pilaRamas = new ArrayList<>();

    public ForkJoinGenerator(Grafo g) {
        this.g = g;
    }

    public void generar() {
        codigo.clear();
        etiquetaCounter = 1;
        pilaRamas.clear();
        listaJoins.clear();
        int n = g.getN();

        int inicio = -1;
        for (int i = 0; i < n; i++) {
            if (g.inDegree(i) == 0) {
                inicio = i;
                break;
            }
        }

        boolean hayFork = false;
        for (int i = 0; i < n; i++) {
            if (g.outDegree(i) > 1) {
                hayFork = true;
                break;
            }
        }

        if (!hayFork) {
            int v = inicio;
            while (true) {
                codigo.add("S" + (v + 1) + ";");
                List<Integer> suc = g.sucesores(v);
                if (suc.isEmpty()) break;
                v = suc.get(0);
            }
            return;
        }

        esJoin         = new boolean[n];
        etqJoin        = new String[n];
        varCont        = new String[n];
        ramas          = new int[n];
        pendientesJoin = new int[n];
        bloqueGenerado = new boolean[n];

        List<Integer> ordenBFS = bfsDesde(inicio, n);
        int numCont = 1;
        for (int v : ordenBFS) {
            int indeg = g.inDegree(v);
            if (indeg > 1) {
                esJoin[v] = true;
                varCont[v] = "CONT" + numCont++;
                ramas[v] = indeg;
                pendientesJoin[v] = indeg;
                listaJoins.add(v);
            }
        }

        codigo.add("S" + (inicio + 1) + ";");

        for (int v : listaJoins) {
            codigo.add(varCont[v] + ":=" + ramas[v] + ";");
        }

        List<Integer> sucsIni = sucesoresOrdenados(inicio);
        if (sucsIni.size() > 1) {
            for (int i = 1; i < sucsIni.size(); i++) {
                int s = sucsIni.get(i);
                String etq = nuevaEtiqueta();
                codigo.add("FORK " + etq + ";");
                pilaRamas.add(new RamaPendiente(etq, s));
            }
        }

        if (!sucsIni.isEmpty()) {
            recorrerRama(sucsIni.get(0), null);
        }

        while (!pilaRamas.isEmpty()) {
            RamaPendiente r = pilaRamas.remove(pilaRamas.size() - 1);
            // En estas ramas sí usamos la etiqueta Lk al inicio
            recorrerRama(r.inicio, r.etiqueta);
        }

        for (int v : listaJoins) {
            if (!bloqueGenerado[v]) {
                generarBloqueJoinInline(v);
            }
        }
        renumerarEtiquetas();
    }

    private List<Integer> bfsDesde(int inicio, int n) {
        List<Integer> orden = new ArrayList<>();
        boolean[] vis = new boolean[n];
        ArrayDeque<Integer> q = new ArrayDeque<>();
        vis[inicio] = true;
        q.add(inicio);
        while (!q.isEmpty()) {
            int v = q.removeFirst();
            orden.add(v);
            for (int s : sucesoresOrdenados(v)) {
                if (!vis[s]) {
                    vis[s] = true;
                    q.addLast(s);
                }
            }
        }
        return orden;
    }

    private void recorrerRama(int nodo, String etiquetaInicial) {
        int actual = nodo;
        boolean primera = true;

        while (true) {
            if (esJoin[actual]) {
                String etq = etiquetaJoin(actual);
                codigo.add("GOTO " + etq + ";");
                consumoRamaJoin(actual);
                return;
            }

            if (primera && etiquetaInicial != null) {
                codigo.add(etiquetaInicial + ": S" + (actual + 1) + ";");
                primera = false;
            } else {
                codigo.add("S" + (actual + 1) + ";");
            }

            List<Integer> sucs = sucesoresOrdenados(actual);

            if (sucs.isEmpty()) return;

            if (sucs.size() == 1) {
                int s = sucs.get(0);
                if (esJoin[s]) {
                    String etq = etiquetaJoin(s);
                    codigo.add("GOTO " + etq + ";");
                    consumoRamaJoin(s);
                    return;
                }

                actual = s;
            } else {
                for (int i = 1; i < sucs.size(); i++) {
                    int s = sucs.get(i);
                    String etq = nuevaEtiqueta();
                    codigo.add("FORK " + etq + ";");
                    pilaRamas.add(new RamaPendiente(etq, s));
                }
                actual = sucs.get(0);
            }
        }
    }

    private List<Integer> sucesoresOrdenados(int v) {
        List<Integer> original = g.sucesores(v);
        List<Integer> sucs = new ArrayList<>(original);

        return sucs;
    }

    private String etiquetaJoin(int v) {
        if (etqJoin == null) return null;
        if (etqJoin[v] == null) {
            etqJoin[v] = nuevaEtiqueta();
        }
        return etqJoin[v];
    }

    private void consumoRamaJoin(int join) {
        pendientesJoin[join]--;
        if (pendientesJoin[join] == 0 && !bloqueGenerado[join]) {
            generarBloqueJoinInline(join);
        }
    }

    private void generarBloqueJoinInline(int nodoJoin) {
        bloqueGenerado[nodoJoin] = true;

        String etq = etiquetaJoin(nodoJoin);
        String cont = varCont[nodoJoin];

        codigo.add(etq + ": JOIN " + cont + ";");

        int actual = nodoJoin;
        while (true) {
            codigo.add("S" + (actual + 1) + ";");

            List<Integer> sucs = g.sucesores(actual);
            if (sucs.isEmpty()) return;

            int s = sucs.get(0);

            if (esJoin[s]) {
                String etq2 = etiquetaJoin(s);
                codigo.add("GOTO " + etq2 + ";");
                consumoRamaJoin(s);
                return;
            } else {
                actual = s;
            }
        }
    }


    private String nuevaEtiqueta() {
        return "L" + (etiquetaCounter++);
    }

    private void renumerarEtiquetas() {
        Pattern p = Pattern.compile("L\\d+");
        Map<String, String> mapa = new LinkedHashMap<>();
        int next = 1;

        for (String linea : codigo) {
            Matcher m = p.matcher(linea);
            while (m.find()) {
                String vieja = m.group();
                if (!mapa.containsKey(vieja)) {
                    mapa.put(vieja, "L" + (next++));
                }
            }
        }

        for (int i = 0; i < codigo.size(); i++) {
            String linea = codigo.get(i);
            Matcher m = p.matcher(linea);
            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String vieja = m.group();
                String nueva = mapa.get(vieja);
                m.appendReplacement(sb, nueva);
            }
            m.appendTail(sb);
            codigo.set(i, sb.toString());
        }
    }

    public List<String> getSalida() {
        return codigo;
    }
}

