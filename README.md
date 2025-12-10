# JavaFX Fork/Join DAG Visualizer

Interactive visualizer for **Directed Acyclic Graphs (DAGs)** that:
- Loads graph definitions from `.txt` files  
- Draws the graph with a layered layout in **JavaFX**  
- Generates **Fork/Join** Java code based on the graph structure  
- Simulates the parallel execution step by step with colors per branch  
- Validates the graph (cycles, disconnected nodes, missing joins, etc.)

> 🎓 This project was developed as an academic project at Tecnológico de Monterrey, Campus Toluca.

---

## 🏆 Recognition

This project achieved **Top 3 in the Expo Ingeniería (Tec de Monterrey, Campus Toluca)** in the **Digital Prototype** category, thanks to:

- Its practical focus on modeling parallel tasks as a DAG  
- Automatic generation of Fork/Join code  
- A clear and intuitive visual simulation of concurrent execution  

---

## 🚀 Features

- **Graph loading from files**
  - Reads graphs from `.txt` files (adjacency matrix).
  - Multiple predefined graphs (e.g. `Grafo-1.txt`, `Grafo-2.txt`, …).

- **Automatic layout by levels**
  - Computes the *level* of each node using a BFS-like traversal:
    - Nodes with `inDegree = 0` → level 0  
    - Their children → level 1  
    - And so on…
  - Nodes of the same level are drawn in the same horizontal row.

- **JavaFX visualization**
  - Nodes drawn as circles with labels (S1, S2, …).  
  - Edges drawn as lines with arrowheads from parent to child.  
  - Smooth fade-in animation when the graph is displayed.  
  - Tooltips and visual effects to make the graph easier to understand.

- **Fork/Join code generation**
  - From the DAG structure, the program generates a **Java Fork/Join** template:
    - Detects branches (forks) and synchronizations (joins).
    - Builds recursive tasks that can run in parallel.
  - Code is shown in a separate panel / tab.

- **Execution simulation**
  - Simulates the execution of the Fork/Join structure:
    - Highlights nodes as they are “executed”.
    - Colors branches:
      - Branch 1 → one color  
      - Branch 2 → another color  
      - etc.
    - Highlights the corresponding lines of generated code.

- **Graph validation**
  - Detects common structural errors:
    - Cycles in the graph (not allowed in a DAG).
    - Nodes without any connection (isolated nodes).
    - Forks without corresponding joins.
    - Joins with invalid or missing counters.
  - Shows visual warnings when a problem is found.

---

## 🧱 Project Structure

Example structure of the repository:

```text
grafos-fork-join-visualizer/
├─ src/
│  └─ ActividadGrafos/
│     ├─ Main.java
│     ├─ GrafoViewer.java
│     ├─ ForkJoinGenerator.java
│     └─ (other helper classes)
├─ graphs/
│  ├─ Grafo-1.txt
│  ├─ Grafo-2.txt
│  ├─ Grafo-3.txt
│  └─ Grafo-4.txt
├─ README.md
├─ .gitignore
└─ LICENSE
