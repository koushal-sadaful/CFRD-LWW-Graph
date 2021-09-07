import java.lang.RuntimeException
import java.util.*

class LastWriteWinsGraph<T>() : ConflictFreeReplicatedGraph<T> {

    private var addedGraph: MutableMap<Vertex<T>, MutableList<Vertex<T>>> = mutableMapOf()

    private var removedVertices: MutableList<Vertex<T>> = mutableListOf()
    private var removedEdges: MutableList<Edge<T>> = mutableListOf()

    @Synchronized
    override fun addVertex(vertex: Vertex<T>) {
        // If vertex not present, add vertex
        // If vertex exists, only update if received vertex is latest
        val existingVertex = addedGraph.keys.find { key -> key.equals(vertex) }
        if (existingVertex != null && existingVertex.isOutdatedComparedTo(vertex)) {
            updateVertexInGraph(addedGraph, existingVertex, vertex)
        } else if (existingVertex == null)
            addedGraph[vertex] = mutableListOf()
    }

    @Synchronized
    override fun addEdge(edge: Edge<T>) {
        val fromNode = Vertex(edge.fromValue, edge.epochTime)
        val toNode = Vertex(edge.toValue, edge.epochTime)

        //Setup dummy vertices if addEdge operation is received before addVertex
        if (!exists(fromNode))
            addVertex(fromNode.copy(epochTime = 0L))
        if (!exists(toNode))
            addVertex(toNode.copy(epochTime = 0L))

        addOneWayEdgeToList(addedGraph, fromNode, toNode)
        addOneWayEdgeToList(addedGraph, toNode, fromNode)
    }

    @Synchronized
    override fun removeVertex(vertex: Vertex<T>) {
        val existingVertex = removedVertices.find { removedVertex -> removedVertex.equals(vertex) }
        if (existingVertex == null) {
            removedVertices.add(vertex)
        } else if (existingVertex.isOutdatedComparedTo(vertex)) {
            val currentOutdatedVertexIndex = removedVertices.indexOf(vertex)
            removedVertices[currentOutdatedVertexIndex] = vertex
        }
    }

    @Synchronized
    override fun removeEdge(edge: Edge<T>) {
        val edgeRemovedAlready = removedEdges.find { removedEdge -> removedEdge.equals(edge) }
        if (edgeRemovedAlready == null) {
            removedEdges.add(edge)
        } else if (edgeRemovedAlready.isOutdatedComparedTo(edge)) {
            val currentOutdatedEdgeIndex = removedEdges.indexOf(edge)
            removedEdges[currentOutdatedEdgeIndex] = edge
        }
    }

    @Synchronized
    override fun getAllVertices(): List<Vertex<T>> {
        val addedVertices = addedGraph.keys.toList()
        val resultingListOfVertices = mutableListOf<Vertex<T>>()

        addedVertices.forEach { addedVertex ->
            run {
                val removedVertex = removedVertices.find { removedVertex -> removedVertex.equals(addedVertex) }
                if ((removedVertex == null || removedVertex.isOutdatedComparedTo(addedVertex)) && !addedVertex.isDummy())
                    resultingListOfVertices.add(addedVertex)
            }
        }
        return resultingListOfVertices
    }

    @Synchronized
    override fun exists(vertex: Vertex<T>): Boolean {
        val allVertices = getAllVertices()
        return allVertices.contains(vertex)
    }

    //Get the resulting graph after merging both added and removed set
    @Synchronized
    fun getGraphSnapshot(): MutableMap<Vertex<T>, MutableList<Vertex<T>>> {
        val resultingGraph: MutableMap<Vertex<T>, MutableList<Vertex<T>>> = mutableMapOf()
        val existingVertices = getAllVertices()

        existingVertices.forEach { existingVertex ->
            run {
                val adjList = addedGraph[existingVertex] ?: emptyList()
                val removedEdges = removedEdges.filter { edge -> edge.isConnectedTo(existingVertex) }
                val resultingEdges = mutableListOf<Vertex<T>>()

                adjList.forEach { vertexInAdjList ->
                    val vertexInAdjStillExists = existingVertices.find { vertex -> vertex.equals(vertexInAdjList) }
                    val addedEdgeAlreadyRemoved = removedEdges.find { edge -> edge.isConnectedTo(vertexInAdjList) }
                    if ((addedEdgeAlreadyRemoved == null || addedEdgeAlreadyRemoved.isOutdatedComparedTo(existingVertex)) && vertexInAdjStillExists != null) {
                        resultingEdges.add(vertexInAdjList)
                    }
                }
                resultingGraph[existingVertex] = resultingEdges
            }
        }
        return resultingGraph
    }

    //Using a DFS approach
    @Synchronized
    override fun getAllConnectedVertices(fromVertex: Vertex<T>): List<Vertex<T>> {
        val currentGraphSnapshot = getGraphSnapshot()
        val vertexQueue = LinkedList<Vertex<T>>()
        val discovered = mutableMapOf<Vertex<T>, Boolean>()

        discovered[fromVertex] = true
        vertexQueue.add(fromVertex)

        while (!vertexQueue.isEmpty()) {
            val currentVertex = vertexQueue.poll()
            val adjacentVertices = currentGraphSnapshot[currentVertex] ?: emptyList()
            for (adjacentVertex in adjacentVertices) {
                val isDiscovered = discovered.getOrDefault(adjacentVertex, false)
                if (!isDiscovered) {
                    discovered[adjacentVertex] = true
                    vertexQueue.add(adjacentVertex)
                }
            }
        }
        return discovered.keys.filter { vertex -> !vertex.equals(fromVertex) }.toList()
    }

    //Not necessarily the shortest path
    @Synchronized
    override fun findPath(fromVertex: Vertex<T>, toVertex: Vertex<T>): List<Vertex<T>> {
        val currentGraphSnapshot = getGraphSnapshot()
        val discovered = mutableMapOf<Vertex<T>, Boolean>()
        val pathList = Stack<Vertex<T>>()

        pathList.add(fromVertex)


        val (found, pathListResult) = findPathRecursive(
            graph = currentGraphSnapshot,
            sourceVertex = fromVertex,
            destinationVertex = toVertex,
            discovered = discovered,
            pathList = pathList
        )

        return if (found)
            pathListResult
        else
            emptyList()
    }

    private fun findPathRecursive(
        graph: MutableMap<Vertex<T>, MutableList<Vertex<T>>>,
        sourceVertex: Vertex<T>,
        destinationVertex: Vertex<T>,
        discovered: MutableMap<Vertex<T>, Boolean>,
        pathList: Stack<Vertex<T>>
    ): Pair<Boolean, Stack<Vertex<T>>> {

        if (sourceVertex.equals(destinationVertex)) {
            return Pair(true, pathList)
        }

        discovered[sourceVertex] = true

        val edgesFromSource = graph[sourceVertex] ?: emptyList()
        edgesFromSource.forEach { vertex ->
            run {
                val isDiscovered = discovered[vertex] ?: false
                if (!isDiscovered) {
                    pathList.add(vertex)
                    val (found, pathListUpdated) = findPathRecursive(
                        graph,
                        vertex,
                        destinationVertex,
                        discovered,
                        pathList
                    )
                    if (found)
                        return Pair(true, pathListUpdated)
                    else
                        pathList.pop()
                }
            }
        }
        return Pair(false, pathList)
    }


    private fun addOneWayEdgeToList(
        graph: MutableMap<Vertex<T>, MutableList<Vertex<T>>>,
        fromNode: Vertex<T>,
        toNode: Vertex<T>
    ) {
        val fromVertexKey = graph.keys.toList().find { vertex -> vertex.equals(fromNode) } ?: fromNode
        val existingEdges = graph[fromVertexKey] ?: mutableListOf()

        //if an edge already exists, just update otherwise add to list
        val existingEdgeIndex = existingEdges.indexOf(toNode)
        if (existingEdgeIndex > -1 && existingEdges[existingEdgeIndex].isOutdatedComparedTo(toNode)) {
            existingEdges[existingEdgeIndex] = toNode
        } else if (existingEdgeIndex < 0) {
            existingEdges.add(toNode)
        }
        graph[fromVertexKey] = existingEdges
    }


    private fun updateVertexInGraph(
        graph: MutableMap<Vertex<T>, MutableList<Vertex<T>>>,
        oldVertex: Vertex<T>,
        newVertex: Vertex<T>
    ) {
        //re-assign existing edge list to new vertex
        val existingEdgeList = graph[oldVertex] ?: mutableListOf()
        graph.remove(oldVertex)
        graph[newVertex] = existingEdgeList
    }


    override fun merge(otherGraph: ConflictFreeReplicatedGraph<T>) {
        if (otherGraph is LastWriteWinsGraph) {
            otherGraph.getGraphSnapshot().forEach { (vertex, adjVertexList) ->
                addVertex(vertex)
                adjVertexList.forEach { adjVertex ->
                    val edge = Edge(vertex.value, adjVertex.value, adjVertex.epochTime)
                    addEdge(edge)
                }
            }
        } else {
            throw RuntimeException("Cannot merge graph of type ${otherGraph.javaClass.name} with ${LastWriteWinsGraph::class.simpleName}")
        }
    }

}