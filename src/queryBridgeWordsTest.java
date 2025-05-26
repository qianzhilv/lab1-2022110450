import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class queryBridgeWordsTest {
    // 通过反射重置 Graph 内部状态
    @Before
    public void resetGraph() throws Exception {
        Field adjList = Lab1.Graph.class.getDeclaredField("adjList");
        adjList.setAccessible(true);
        ((Map<?, ?>) adjList.get(Lab1.graph)).clear();

        Field termFrequency = Lab1.Graph.class.getDeclaredField("termFrequency");
        termFrequency.setAccessible(true);
        ((Map<?, ?>) termFrequency.get(Lab1.graph)).clear();
    }

    // 测试用例 1：两个单词均不存在
    @Test
    public void testBothWordsNotExist() {
        String result = Lab1.queryBridgeWords("apple", "banana");
        assertEquals("No apple in the graph!", result);
    }

    // 测试用例 2：只有 word1 不存在（手动构建 word2 的邻接表）
    @Test
    public void testWord1NotExist() throws Exception {
        // 使用反射添加 word2 到图中
        Field adjList = Lab1.Graph.class.getDeclaredField("adjList");
        adjList.setAccessible(true);
        Map<String, Map<String, Integer>> map = (Map<String, Map<String, Integer>>) adjList.get(Lab1.graph);
        map.put("banana", new HashMap<>());

        String result = Lab1.queryBridgeWords("apple", "banana");
        assertEquals("No apple in the graph!", result);
    }

    // 测试用例 3：存在单个桥接词（通过测试文件构建图）
    @Test
    public void testSingleBridgeWord() {
        // 测试文件内容：word1 bridge word2
        Lab1.buildGraph("single_bridge.txt");
        String result = Lab1.queryBridgeWords("word1", "word2");
        assertEquals("The bridge words from word1 to word2 are: bridge.", result);
    }

    // 测试用例 4：多个桥接词（通过测试文件构建图）
    @Test
    public void testMultipleBridgeWords() {
        // 测试文件内容：word1 bridge1 word2 word1 bridge2 word2
        Lab1.buildGraph("multi_bridge.txt");
        String result = Lab1.queryBridgeWords("word1", "word2");
        assertEquals("The bridge words from word1 to word2 are: bridge1, and bridge2.", result);
    }

    // 测试用例 5：无桥接词（通过测试文件构建图）
    @Test
    public void testNoBridgeWords() {
        // 测试文件内容：apple banana
        Lab1.buildGraph("no_bridge.txt");
        String result = Lab1.queryBridgeWords("apple", "banana");
        assertEquals("No bridge words from apple to banana!", result);
    }

    // 测试用例 6：验证三个桥接词的格式化输出
    @Test
    public void testThreeBridgeWordsFormatting() {
        // 测试文件内容：start b1 end start b2 end start b3 end
        Lab1.buildGraph("src/test/resources/three_bridge.txt");
        String result = Lab1.queryBridgeWords("start", "end");
        assertEquals("The bridge words from start to end are: b1, b2, and b3.", result);
    }
}