import org.junit.Before;
import org.junit.Test;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class queryBridgeWordsTest {

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
        Lab1.buildGraph("Easy Test.txt");
        String result = Lab1.queryBridgeWords("a", "report");
        assertEquals("The bridge words from a to report are: detailed.", result);
    }

    // 测试用例 4：多个桥接词（通过测试文件构建图）
    @Test
    public void testMultipleBridgeWords() {
        // 测试文件内容：word1 bridge1 word2 word1 bridge2 word2
        Lab1.buildGraph("Easy Test.txt");
        String result = Lab1.queryBridgeWords("the", "team");
        assertEquals("The bridge words from the to team are: a, and r.", result);
    }

    // 测试用例 5：无桥接词（通过测试文件构建图）
    @Test
    public void testNoBridgeWords() {
        // 测试文件内容：apple banana
        Lab1.buildGraph("Easy Test.txt");
        String result = Lab1.queryBridgeWords("the", "data");
        assertEquals("No bridge words from the to data!", result);
    }
}