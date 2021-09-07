import common.FakeGraph
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test


internal class LastWriteWinsGraphTest {

    private lateinit var graphUnderTest: LastWriteWinsGraph<Int>

    @BeforeEach
    internal fun setUp() {
        graphUnderTest = LastWriteWinsGraph()
    }


    // =================== VERTEX TESTS =================

    @Test
    fun `addVertex() - trivial add and check if exists in graph`() {
        val vertexOne = Vertex(1)
        graphUnderTest.addVertex(vertexOne)
        val graph = graphUnderTest.getGraphSnapshot()

        assertTrue(graph.containsKey(vertexOne))
    }

    @Test
    fun `addVertex() - updates epoch time if adding same vertex twice`() {
        val vertexOne = Vertex(1, epochTime = 200L)
        val vertexOneUpdated = Vertex(1, epochTime = 300L)
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexOneUpdated)
        val vertexActual = graphUnderTest.getAllVertices()[0]
        assertEquals(vertexOneUpdated.epochTime, vertexActual.epochTime)
    }

    @Test
    fun `addVertex() - add same vertex twice but keeps the one with latest time`() {
        val addVertex = Vertex(1, epochTime = 100L)
        val addVertexLatest = Vertex(1, epochTime = 300L)
        graphUnderTest.addVertex(addVertexLatest)
        graphUnderTest.addVertex(addVertex)

        val resultingVertex = graphUnderTest.getAllVertices().find { v -> v.equals(addVertex) }

        assertEquals(addVertexLatest.epochTime, resultingVertex!!.epochTime)
        assertEquals(addVertexLatest.value, resultingVertex.value)
    }

    @Test
    fun `addVertex() - an old vertex operation cannot overwrite a newer vertex`() {
        val addVertex = Vertex(1, epochTime = 100L)
        val addVertexOld = Vertex(1, epochTime = 1L)
        graphUnderTest.addVertex(addVertex)
        graphUnderTest.addVertex(addVertexOld)

        val resultingVertex = graphUnderTest.getAllVertices().find { v -> v.equals(addVertex) }

        assertEquals(addVertex.epochTime, resultingVertex!!.epochTime)
        assertEquals(addVertex.value, resultingVertex.value)
    }

    @Test
    fun `removeVertex() - given a vertex exists, can remove vertex`() {
        val addVertex = Vertex(1, epochTime = 100L)
        val removeVertex = Vertex(1, epochTime = 101)
        graphUnderTest.addVertex(addVertex)
        graphUnderTest.removeVertex(removeVertex)
        assertFalse(graphUnderTest.exists(removeVertex))
    }

    @Test
    fun `removeVertex() - when two remove operations are received, the latest timestamp operation wins`() {
        val removeVertex = Vertex(1, epochTime = 101)
        val addVertex = Vertex(1, epochTime = 500L)
        val removeVertexLatest = Vertex(1, epochTime = 1200)
        graphUnderTest.addVertex(addVertex)
        graphUnderTest.removeVertex(removeVertex)
        graphUnderTest.removeVertex(removeVertexLatest)
        assertFalse(graphUnderTest.exists(removeVertex))
    }

    @Test
    fun `removeVertex() - add vertex and remove added vertex and check if value exists in graph`() {
        val addVertex = Vertex(1, epochTime = 100L)
        val removeVertex = Vertex(1, epochTime = 101)
        graphUnderTest.addVertex(addVertex)
        graphUnderTest.removeVertex(removeVertex)
        assertFalse(graphUnderTest.exists(removeVertex))
    }

    @Test
    fun `given a vertex has been removed, a new added vertex can exist with same value`() {
        val removeVertex = Vertex(1, epochTime = 50L)
        val addVertex = Vertex(1, epochTime = 100L)
        graphUnderTest.removeVertex(removeVertex)
        graphUnderTest.addVertex(addVertex)
        assertTrue(graphUnderTest.exists(addVertex))
    }

    @Test
    fun `add and remove added vertex and check if vertex exists in graph`() {
        val addVertex = Vertex(1, epochTime = 100L)
        val removeVertex = Vertex(1, epochTime = 101)
        graphUnderTest.addVertex(addVertex)
        graphUnderTest.removeVertex(removeVertex)
        assertFalse(graphUnderTest.exists(removeVertex))
    }

    @Test
    fun `when two operations with same time are received, the first one takes priority`() {
        val vertexOne = Vertex(1, epochTime = 200L)
        val vertexOneUpdated = Vertex(1, epochTime = 200L)
        graphUnderTest.removeVertex(vertexOneUpdated)
        graphUnderTest.addVertex(vertexOne)
        assertTrue(graphUnderTest.getAllVertices().isEmpty())
    }

    // =================== EDGES TESTS =================

    @Test
    fun `addEdge() - add edge between existing vertices`() {
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addEdge(Edge(1, 2))

        val graph = graphUnderTest.getGraphSnapshot()

        assertIterableEquals(listOf(vertexTwo), graph[vertexOne])
        assertIterableEquals(listOf(vertexOne), graph[vertexTwo])
    }

    @Test
    fun `addEdge() - updates edge epoch time if edge is added and edge direction is reversed`() {
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addEdge(Edge(1, 2, epochTime = 100L))
        graphUnderTest.addEdge(Edge(2, 1, epochTime = 500L))

        val graph = graphUnderTest.getGraphSnapshot()

        assertIterableEquals(listOf(vertexTwo), graph[vertexOne])
        assertIterableEquals(listOf(vertexOne), graph[vertexTwo])

        assertEquals(graph[vertexOne]?.get(0)?.epochTime, 500L)
        assertEquals(graph[vertexTwo]?.get(0)?.epochTime, 500L)
    }

    @Test
    fun `addEdge() - updates edge timestamp when two edges are added out of order`() {
        val vertexOne = Vertex(1, epochTime = 100)
        val vertexTwo = Vertex(2, epochTime = 100)
        graphUnderTest.addEdge(Edge(1, 2, epochTime = 499))
        graphUnderTest.addEdge(Edge(1, 2, epochTime = 600))
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)


        val graph = graphUnderTest.getGraphSnapshot()
        assertEquals(600, graph[vertexOne]?.get(0)?.epochTime)
    }

    @Test
    fun `removeEdge() - remove edge between existing vertices when operations are out of order`() {
        val vertexOne = Vertex(1, epochTime = 100)
        val vertexTwo = Vertex(2, epochTime = 100)
        graphUnderTest.removeEdge(Edge(1, 2, epochTime = 500))
        graphUnderTest.addEdge(Edge(1, 2, epochTime = 499))
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)

        val graph = graphUnderTest.getGraphSnapshot()
        assertIterableEquals(emptyList<Vertex<Int>>(), graph[vertexOne])
        assertIterableEquals(emptyList<Vertex<Int>>(), graph[vertexTwo])
    }

    @Test
    fun `removeEdge() - when two remove edge operations are received, latest one wins`() {
        val vertexOne = Vertex(1, epochTime = 100)
        val vertexTwo = Vertex(2, epochTime = 100)
        graphUnderTest.removeEdge(Edge(1, 2, epochTime = 500))
        graphUnderTest.addEdge(Edge(1, 2, epochTime = 550))
        graphUnderTest.removeEdge(Edge(1, 2, epochTime = 800))
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)

        val graph = graphUnderTest.getGraphSnapshot()
        assertIterableEquals(emptyList<Vertex<Int>>(), graph[vertexOne])
        assertIterableEquals(emptyList<Vertex<Int>>(), graph[vertexTwo])
    }


    // =================== GRAPH DISCOVERY TESTS =================

    @Test
    fun `exists() - return true if vertex exists in graph`() {
        val vertexOne = Vertex(1)
        graphUnderTest.addVertex(vertexOne)

        assertTrue(graphUnderTest.exists(vertexOne))
    }

    @Test
    fun `exists() - return false if vertex does not exists in graph`() {
        val vertexOne = Vertex(1)

        assertFalse(graphUnderTest.exists(vertexOne))
    }

    @Test
    fun `getAllVertices() - returns list of vertices in graph`() {
        //given added operations were received
        val addVertexOne = Vertex(1, epochTime = 300L)
        val addVertexTwo = Vertex(2, epochTime = 200L)
        graphUnderTest.addVertex(addVertexOne)
        graphUnderTest.addVertex(addVertexTwo)

        //then an outdated removed operation was received last
        val removeVertexOne = Vertex(1, epochTime = 101)
        graphUnderTest.removeVertex(removeVertexOne)

        assertIterableEquals(listOf(addVertexOne, addVertexTwo), graphUnderTest.getAllVertices())
    }

    @Test
    fun `getAllVertices() - returns empty if graph is empty`() {
        //given a removed operation was received first
        val removeVertexOne = Vertex(1, epochTime = 401)
        graphUnderTest.removeVertex(removeVertexOne)

        //then added operations were received
        val addVertexOne = Vertex(1, epochTime = 300L)
        graphUnderTest.addVertex(addVertexOne)

        assertIterableEquals(emptyList<Vertex<Int>>(), graphUnderTest.getAllVertices())
    }

    @Test
    fun `getAllVertices() - given an empty graph with an added edge, should return empty`() {
        // given an edge is added and dummy vertices are inserted
        // should not return dummy vertices
        val edge = Edge(1, 3, epochTime = 300L)
        graphUnderTest.addEdge(edge)

        assertIterableEquals(emptyList<Vertex<Int>>(), graphUnderTest.getAllVertices())
    }

    @Test
    fun `getAllConnectedVertices() - returns list of all vertices connect to a vertex`() {
        //form a graph with four vertex and they are connected
        // 1 - 2 - 3 - 4 - 1
        // should not return itself
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        val vertexThree = Vertex(3)
        val vertexFour = Vertex(4)
        graphUnderTest.addEdge(Edge(1, 2))
        graphUnderTest.addEdge(Edge(2, 3))
        graphUnderTest.addEdge(Edge(3, 4))
        graphUnderTest.addEdge(Edge(4, 1))
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexThree)
        graphUnderTest.addVertex(vertexFour)

        val allConnectedVertices = graphUnderTest.getAllConnectedVertices(vertexOne)
        assertIterableEquals(listOf(vertexTwo, vertexFour, vertexThree), allConnectedVertices)
    }

    @Test
    fun `getAllConnectedVertices() - returns empty if no vertices found`() {
        val vertexOne = Vertex(1)
        val allConnectedVertices = graphUnderTest.getAllConnectedVertices(vertexOne)
        assertIterableEquals(emptyList<Vertex<Int>>(), allConnectedVertices)
    }

    // =================== FIND PATH TESTS =================

    @Test
    fun `findPath() - returns list of vertices in path to reach a vertex`() {
        //form a graph with four vertex and they are connected
        // 1 - 2 - 3 - 4
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        val vertexThree = Vertex(3)
        val vertexFour = Vertex(4)
        graphUnderTest.addEdge(Edge(1, 2))
        graphUnderTest.addEdge(Edge(2, 3))
        graphUnderTest.addEdge(Edge(3, 4))
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexThree)
        graphUnderTest.addVertex(vertexFour)

        val pathBetweenVertices = graphUnderTest.findPath(vertexOne, vertexThree)
        assertIterableEquals(listOf(vertexOne, vertexTwo, vertexThree), pathBetweenVertices)
    }

    @Test
    fun `findPath() - returns list when the first edge is the target vertex`() {
        //form a graph with two vertex and they are connected
        // 1 - 2
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        graphUnderTest.addEdge(Edge(1, 2))
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)

        val pathBetweenVertices = graphUnderTest.findPath(vertexOne, vertexTwo)
        assertIterableEquals(listOf(vertexOne, vertexTwo), pathBetweenVertices)
    }

    @Test
    fun `findPath() - returns list of vertices in path to reach a vertex, not shortest one proved`() {
        //form a graph with four vertex and they are connected
        // 1 - 2 - 3 - 4 - 1
        // shortest path : 1 - 4
        // returns 1-2-3-4
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        val vertexThree = Vertex(3)
        val vertexFour = Vertex(4)
        graphUnderTest.addEdge(Edge(1, 2))
        graphUnderTest.addEdge(Edge(2, 3))
        graphUnderTest.addEdge(Edge(3, 4))
        graphUnderTest.addEdge(Edge(4, 1))
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexThree)
        graphUnderTest.addVertex(vertexFour)

        val pathBetweenVertices = graphUnderTest.findPath(vertexOne, vertexFour)
        assertIterableEquals(listOf(vertexOne, vertexTwo, vertexThree, vertexFour), pathBetweenVertices)
    }

    @Test
    fun `findPath() - when a vertex is removed and no path exists, returns empty list`() {
        //form a graph with three vertex and they are connected
        // 1 - 2 - 3 - 4
        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        val vertexThree = Vertex(3)
        val vertexFour = Vertex(4)
        graphUnderTest.addEdge(Edge(1, 2))
        graphUnderTest.addEdge(Edge(2, 3))
        graphUnderTest.addEdge(Edge(3, 4))
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexThree)
        graphUnderTest.addVertex(vertexFour)

        val pathBetweenVertices = graphUnderTest.findPath(vertexOne, vertexFour)
        assertIterableEquals(listOf(vertexOne, vertexTwo, vertexThree, vertexFour), pathBetweenVertices)

        //remove a vertex should remove edges associated with the vertex
        val vertexThreeWithNewTimestamp = Vertex(3)
        graphUnderTest.removeVertex(vertexThreeWithNewTimestamp)

        val pathBetweenVerticesLatest = graphUnderTest.findPath(vertexOne, vertexFour)
        assertIterableEquals(emptyList<Vertex<Int>>(), pathBetweenVerticesLatest)
    }

    @Test
    fun `findPath() - when operations were received out of order, returns expected path list`() {

        val vertexOne = Vertex(1)
        val vertexTwo = Vertex(2)
        val vertexThree = Vertex(3)
        val vertexFour = Vertex(4)

        graphUnderTest.removeEdge(Edge(4, 5, epochTime = 800L))
        graphUnderTest.removeVertex(Vertex(5, epochTime = 800L))

        graphUnderTest.addEdge(Edge(1, 2, epochTime = 200L))
        graphUnderTest.addEdge(Edge(2, 3, epochTime = 200L))
        graphUnderTest.addEdge(Edge(3, 4, epochTime = 200L))
        graphUnderTest.addEdge(Edge(4, 5, epochTime = 200L))

        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexThree)
        graphUnderTest.addVertex(vertexFour)
        graphUnderTest.addVertex(Vertex(5, epochTime = 200))

        val x = graphUnderTest.getGraphSnapshot()


        //remove a vertex should remove edges associated with the vertex
        val pathBetweenVertices = graphUnderTest.findPath(vertexOne, Vertex(5))
        assertIterableEquals(emptyList<Vertex<Int>>(), pathBetweenVertices)
    }


    // =================== MERGE GRAPH TESTS =================

    @Test
    fun `merge() - can merge a graph with target graph for adding a vertex`() {
        val vertexOne = Vertex(100)
        val vertexTwo = Vertex(200)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)

        val secondGraph = createSecondIntGraph()

        graphUnderTest.merge(secondGraph)

        val newlyAddedVertex = Vertex(300)

        val allConnectedVertices = graphUnderTest.getAllConnectedVertices(vertexOne)
        assertIterableEquals(listOf(vertexTwo, newlyAddedVertex), allConnectedVertices)
    }

    @Test
    fun `merge() - can merge a graph with target graph for adding an edge`() {
        val vertexOne = Vertex(100)
        val vertexTwo = Vertex(200)
        val vertexThree = Vertex(300)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)

        val secondGraph = createSecondIntGraph()

        val newEdge = Edge(200, 300)
        secondGraph.addEdge(newEdge)

        graphUnderTest.merge(secondGraph)

        val allConnectedVertices = graphUnderTest.getAllConnectedVertices(vertexTwo)
        assertIterableEquals(listOf(vertexOne, vertexThree), allConnectedVertices)
    }

    @Test
    fun `merge() - can merge with empty graph`() {
        val vertexOne = Vertex(100)
        val vertexTwo = Vertex(200)
        val vertexThree = Vertex(300)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)
        graphUnderTest.addVertex(vertexThree)
        val edgeOne = Edge(100, 300)
        val edgeTwo = Edge(100, 200)
        graphUnderTest.addEdge(edgeOne)
        graphUnderTest.addEdge(edgeTwo)

        val secondGraph = LastWriteWinsGraph<Int>()

        graphUnderTest.merge(secondGraph)

        val allConnectedVertices = graphUnderTest.getAllConnectedVertices(vertexOne)
        assertIterableEquals(listOf(vertexThree, vertexTwo), allConnectedVertices)
    }

    @Test
    fun `merge() - throws an error if not same graph type`() {
        val vertexOne = Vertex(100)
        val vertexTwo = Vertex(200)
        graphUnderTest.addVertex(vertexTwo)
        graphUnderTest.addVertex(vertexOne)

        val secondGraph = FakeGraph<Int>()

        val exception = assertThrows(RuntimeException::class.java) { graphUnderTest.merge(secondGraph) }
        assertEquals("Cannot merge graph of type common.FakeGraph with LastWriteWinsGraph", exception.localizedMessage)
    }

    private fun createSecondIntGraph(): LastWriteWinsGraph<Int> {
        // graph: 100 --> 300
        //         |-> 200
        val graph = LastWriteWinsGraph<Int>()
        graph.addVertex(Vertex(100))
        graph.addVertex(Vertex(200))
        graph.addVertex(Vertex(300))
        graph.addEdge(Edge(100, 200))
        graph.addEdge(Edge(300, 100))
        return graph
    }

}