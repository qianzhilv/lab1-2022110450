import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class calcShortestPathTest {
    private static final String TEST_FILE = "Easy Test.txt";

    @Before
    public void setUp() {
        // 初始化图并读取测试文件构建图
        Lab1.graph = new Lab1.Graph();
        Lab1.buildGraph(TEST_FILE); // 根据文件内容自动构建图
    }

    //--------------------- 双单词模式测试 ---------------------
    @Test
    public void testValidPathScientistToData() {
        String result = Lab1.calcShortestPath("scientist", "data");
        // 预期路径：scientist -> analyzed -> the -> data (权重1+1+1=3)
        assertTrue(result.contains("scientist -> analyzed -> the -> data (Length: 3)"));
    }

    //--------------------- 边界条件测试 ---------------------
    @Test
    public void testSameStartAndEnd() {
        String result = Lab1.calcShortestPath("data", "data");
        assertEquals("Shortest path: data (Length: 0)", result);
    }

    @Test
    public void testInvalidNode() {
        String result = Lab1.calcShortestPath("unknown", "data");
        assertEquals("unknown not in graph!", result);
    }

    @Test
    public void testnopath() {
        String result = Lab1.calcShortestPath("data", "he");
        assertEquals("No path from data to he!", result);
    }
}