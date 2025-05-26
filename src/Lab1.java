import java.io.*;
import java.util.*;

public class Lab1 {
    public static class Graph {
        Map<String, Map<String, Integer>> adjList = new HashMap<>();
        Map<String, Double> pageRanks = new HashMap<>();
        Map<String, Integer> termFrequency = new HashMap<>(); // 新增词频存储
    }

    public static Graph graph = new Graph();

    public static void main(String[] args) {
        // 直接指定文件路径
        String filePath = "Easy Test.txt"; // 文件需放在项目根目录下

        // 自动构建有向图
        buildGraph(filePath);

        // 检查图是否为空
        if (graph.adjList.isEmpty()) {
            System.out.println("Error: Failed to build graph from 'Easy Test.txt'");
            return;
        }

        // 进入主菜单交互
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.println("\n1. Show Graph\n2. Query Bridge Words\n3. Generate New Text\n4. Shortest Path\n5. PageRank\n6. Random Walk\n0. Exit");
            System.out.print("Choose function: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // 消费换行符

            switch (choice) {
                case 1:
                    showDirectedGraph();
                    break;
                case 2:
                    System.out.print("Enter word1: ");
                    String word1 = scanner.nextLine().toLowerCase();
                    System.out.print("Enter word2: ");
                    String word2 = scanner.nextLine().toLowerCase();
                    System.out.println(queryBridgeWords(word1, word2));
                    break;
                case 3:
                    System.out.print("Enter text: ");
                    String inputText = scanner.nextLine();
                    System.out.println(generateNewText(inputText));
                    break;
                case 4:
                    System.out.print("Enter start word: ");
                    String start = scanner.nextLine().toLowerCase();
                    System.out.print("Enter end word: ");
                    String end = scanner.nextLine().toLowerCase();
                    System.out.println(calcShortestPath(start, end));
                    break;
                case 5:
                    System.out.print("Enter word: ");
                    String word = scanner.nextLine().toLowerCase();
                    System.out.printf("PageRank: %.4f\n", calcPageRank(word));
                    break;
                case 6:
                    System.out.println(randomWalk());
                    break;
                case 0:
                    return;
                default:
                    System.out.println("Invalid choice!");
            }
        }
    }

    public static void buildGraph(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            List<String> words = new ArrayList<>();
            Map<String, Integer> termFrequency = new HashMap<>(); // 新增词频统计Map

            String line;
            // 1. 逐行读取并处理文本
            while ((line = br.readLine()) != null) {
                // 移除非字母字符并转小写
                String cleanedLine = line.replaceAll("[^a-zA-Z ]", " ").toLowerCase();
                // 分割为单词列表
                String[] tokens = cleanedLine.split("\\s+");

                // 2. 统计词频并收集有效单词
                for (String token : tokens) {
                    if (!token.isEmpty()) { // 过滤空字符串
                        words.add(token);     // 收集单词用于构建邻接表
                        termFrequency.put(token, termFrequency.getOrDefault(token, 0) + 1); // 更新词频
                    }
                }
            }

            // 3. 将词频存储到Graph对象中
            graph.termFrequency = termFrequency;

            // 4. 构建邻接表
            for (int i = 0; i < words.size() - 1; i++) {
                String from = words.get(i);
                String to = words.get(i + 1);

                // 初始化源节点的邻接表
                graph.adjList.putIfAbsent(from, new HashMap<>());

                // 更新边的权重（相邻出现次数+1）
                Map<String, Integer> edges = graph.adjList.get(from);
                edges.put(to, edges.getOrDefault(to, 0) + 1);
            }

        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    public static void showDirectedGraph() {
        // CLI文本展示
        System.out.println("\nGraph Structure (CLI View):");
        for (String node : graph.adjList.keySet()) {
            StringBuilder sb = new StringBuilder();
            sb.append(node).append(" -> ");
            for (Map.Entry<String, Integer> edge : graph.adjList.get(node).entrySet()) {
                sb.append(edge.getKey())
                        .append("(").append(edge.getValue()).append(") ")
                        .append("-> ");
            }
            if (sb.length() > 4) sb.setLength(sb.length() - 3); // 移除末尾多余的箭头
            System.out.println(sb);
        }

        // 图形文件生成（可选）
        System.out.print("\nSave graph to image? (y/n): ");
        Scanner scanner = new Scanner(System.in);
        if (scanner.nextLine().trim().equalsIgnoreCase("y")) {
            generateGraphImage();
        }
    }

    private static void generateGraphImage() {
        try {
            // 生成DOT文件
            File dotFile = new File("graph.dot");
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(dotFile))) {
                writer.write("digraph G {\n");
                for (String node : graph.adjList.keySet()) {
                    for (Map.Entry<String, Integer> edge : graph.adjList.get(node).entrySet()) {
                        writer.write(String.format(
                                "    \"%s\" -> \"%s\" [label=\"%d\"];\n",
                                node, edge.getKey(), edge.getValue()
                        ));
                    }
                }
                writer.write("}");
            }

            // 调用Graphviz生成图片
            ProcessBuilder pb = new ProcessBuilder("dot", "-Tpng", "graph.dot", "-o", "graph.png");
            Process process = pb.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                System.out.println("Graph image saved as graph.png");
            } else {
                System.err.println("Graphviz failed. Ensure Graphviz is installed and added to PATH.");
            }
        } catch (IOException | InterruptedException e) {
            System.err.println("Error generating graph image: " + e.getMessage());
        }
    }

    public static String queryBridgeWords(String word1, String word2) {
        // 转换为小写以统一处理
        word1 = word1.toLowerCase();
        word2 = word2.toLowerCase();

        // 1. 检查单词存在性
        if (!graph.adjList.containsKey(word1)) {
            return "No " + word1 + " in the graph!";
        }
        if (!graph.adjList.containsKey(word2)) {
            return "No " + word2 + " in the graph!";
        }

        // 2. 遍历所有可能的桥接词
        List<String> bridgeWords = new ArrayList<>();
        Map<String, Integer> word1Edges = graph.adjList.get(word1);
        for (String candidate : word1Edges.keySet()) {
            // 检查候选词是否有指向word2的边
            if (graph.adjList.containsKey(candidate) &&
                    graph.adjList.get(candidate).containsKey(word2)) {
                bridgeWords.add(candidate);
            }
        }

        // 3. 格式化输出结果
        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return formatBridgeWordsOutput(bridgeWords, word1, word2);
        }
    }

    // 辅助函数：格式化桥接词输出
    private static String formatBridgeWordsOutput(List<String> bridges, String word1, String word2) {
        StringBuilder sb = new StringBuilder();
        sb.append("The bridge words from ").append(word1).append(" to ").append(word2).append(" are: ");

        for (int i = 0; i < bridges.size(); i++) {
            if (i == bridges.size() - 1 && bridges.size() > 1) {
                sb.append("and ").append(bridges.get(i));
            } else if (bridges.size() == 1) {
                sb.append(bridges.get(i));
            } else {
                sb.append(bridges.get(i)).append(", ");
            }
        }
        sb.append(".");
        return sb.toString();
    }

    public static String generateNewText(String inputText) {
        String[] tokens = inputText.replaceAll("[^a-zA-Z ]", " ").toLowerCase().split("\\s+");
        List<String> words = new ArrayList<>();
        for (String token : tokens) {
            if (!token.isEmpty()) words.add(token);
        }

        List<String> result = new ArrayList<>();
        for (int i = 0; i < words.size() - 1; i++) {
            String current = words.get(i);
            String next = words.get(i + 1);
            result.add(current);

            List<String> bridges = new ArrayList<>();
            if (graph.adjList.containsKey(current) && graph.adjList.containsKey(next)) {
                for (String candidate : graph.adjList.get(current).keySet()) {
                    if (graph.adjList.containsKey(candidate) &&
                            graph.adjList.get(candidate).containsKey(next)) {
                        bridges.add(candidate);
                    }
                }
            }

            if (!bridges.isEmpty()) {
                Random rand = new Random();
                String bridge = bridges.get(rand.nextInt(bridges.size()));
                result.add(bridge);
            }
        }
        result.add(words.get(words.size() - 1));
        return String.join(" ", result);
    }

    public static String calcShortestPath(String word1, String word2) {
        // 处理单单词模式
        if (word2 == null || word2.isEmpty()) {
            return calculateAllShortestPaths(word1);
        }

        // 原双单词模式逻辑
        if (!graph.adjList.containsKey(word1)) {
            return word1 + " not in graph!";
        }
        if (!graph.adjList.containsKey(word2)) {
            return word2 + " not in graph!";
        }

        // Dijkstra算法实现（保持原有代码）
        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        for (String node : graph.adjList.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(word1, 0);
        pq.add(word1);

        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (u.equals(word2)) break;
            if (!graph.adjList.containsKey(u)) continue;

            for (Map.Entry<String, Integer> edge : graph.adjList.get(u).entrySet()) {
                String v = edge.getKey();
                int weight = edge.getValue();
                int alt = dist.get(u) + weight;
                if (alt < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        if (dist.get(word2) == Integer.MAX_VALUE) {
            return "No path from " + word1 + " to " + word2 + "!";
        }

        List<String> path = new LinkedList<>();
        String current = word2;
        while (current != null) {
            path.add(0, current);
            current = prev.get(current);
        }
        return "Shortest path: " + String.join(" -> ", path) + " (Length: " + dist.get(word2) + ")";
    }

    // 新增辅助函数：处理单单词模式
    private static String calculateAllShortestPaths(String startWord) {
        if (!graph.adjList.containsKey(startWord)) {
            return startWord + " not in graph!";
        }

        Map<String, Integer> dist = new HashMap<>();
        Map<String, String> prev = new HashMap<>();
        PriorityQueue<String> pq = new PriorityQueue<>(Comparator.comparingInt(dist::get));

        // 初始化
        for (String node : graph.adjList.keySet()) {
            dist.put(node, Integer.MAX_VALUE);
        }
        dist.put(startWord, 0);
        pq.add(startWord);

        // Dijkstra核心算法
        while (!pq.isEmpty()) {
            String u = pq.poll();
            if (!graph.adjList.containsKey(u)) continue;

            for (Map.Entry<String, Integer> edge : graph.adjList.get(u).entrySet()) {
                String v = edge.getKey();
                int weight = edge.getValue();
                int alt = dist.get(u) + weight;
                if (alt < dist.getOrDefault(v, Integer.MAX_VALUE)) {
                    dist.put(v, alt);
                    prev.put(v, u);
                    pq.add(v);
                }
            }
        }

        // 构建结果字符串
        StringBuilder result = new StringBuilder();
        result.append("Shortest paths from ").append(startWord).append(":\n");
        for (String node : graph.adjList.keySet()) {
            if (node.equals(startWord)) continue;

            if (dist.get(node) == Integer.MAX_VALUE) {
                result.append("  [X] ").append(node).append(": Unreachable\n");
            } else {
                List<String> path = new LinkedList<>();
                String current = node;
                while (current != null) {
                    path.add(0, current);
                    current = prev.get(current);
                }
                result.append("  [✓] ")
                        .append(String.join(" -> ", path))
                        .append(" (Total weight: ").append(dist.get(node)).append(")\n");
            }
        }
        return result.toString();
    }
    public static double calcPageRank(String word) {
        final double d = 0.85; // 阻尼因子，可调整为实验所需值
        final int maxIter = 100;
        final double tolerance = 1e-6;

        Map<String, Double> pr = new HashMap<>();
        int N = graph.adjList.size();
        if (N == 0) return 0.0;

        //=========== 改进初始PR值分配（基于词频TF）===========
        double totalTF = graph.termFrequency.values().stream().mapToInt(Integer::intValue).sum();
        for (String node : graph.adjList.keySet()) {
            double tf = graph.termFrequency.getOrDefault(node, 0);
            pr.put(node, tf / totalTF); // 归一化词频作为初始PR值
        }

        for (int iter = 0; iter < maxIter; iter++) {
            Map<String, Double> newPR = new HashMap<>();
            double danglingSum = 0.0;

            // 1. 计算悬挂节点的PR总和
            for (String node : graph.adjList.keySet()) {
                if (graph.adjList.get(node).isEmpty()) {
                    danglingSum += pr.get(node);
                }
            }
            double danglingContribution = d * danglingSum / N;

            // 2. 遍历所有节点更新PR值
            for (String u : graph.adjList.keySet()) {
                double sum = 0.0;
                // 遍历所有可能指向u的节点v (B_u集合)
                for (String v : graph.adjList.keySet()) {
                    if (graph.adjList.get(v).containsKey(u)) { // v指向u
                        int Lv = graph.adjList.get(v).size();
                        sum += pr.get(v) / Lv;
                    }
                }
                newPR.put(u, (1 - d)/N + d * (sum + danglingContribution));
            }

            // 3. 检查收敛
            boolean converged = true;
            for (String node : pr.keySet()) {
                if (Math.abs(newPR.get(node) - pr.get(node)) > tolerance) {
                    converged = false;
                    break;
                }
            }
            if (converged) break;
            pr = new HashMap<>(newPR);
        }

        graph.pageRanks = pr;
        return pr.getOrDefault(word.toLowerCase(), 0.0);
    }

    public static String randomWalk() {
        List<String> pathNodes = new ArrayList<>();
        Set<String> visitedEdges = new HashSet<>();
        Random rand = new Random();

        List<String> nodes = new ArrayList<>(graph.adjList.keySet());
        if (nodes.isEmpty()) return "Graph is empty!";
        String current = nodes.get(rand.nextInt(nodes.size()));
        pathNodes.add(current);

        while (true) {
            if (!graph.adjList.containsKey(current) || graph.adjList.get(current).isEmpty()) {
                break;
            }

            List<String> outEdges = new ArrayList<>(graph.adjList.get(current).keySet());
            String next = outEdges.get(rand.nextInt(outEdges.size()));
            String edge = current + "->" + next;

            if (visitedEdges.contains(edge)) {
                break;
            }
            visitedEdges.add(edge);
            pathNodes.add(next);
            current = next;
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter("random_walk.txt"))) {
            String output = String.join(" ", pathNodes);
            writer.write(output);
            return output;
        } catch (IOException e) {
            return "Error writing file: " + e.getMessage();
        }
    }
}
