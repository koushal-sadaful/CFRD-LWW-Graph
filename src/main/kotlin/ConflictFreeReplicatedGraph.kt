interface ConflictFreeReplicatedGraph<T> {

    fun addVertex(vertex: Vertex<T>)
    fun removeVertex(vertex: Vertex<T>)
    fun addEdge(edge: Edge<T>)
    fun removeEdge(edge: Edge<T>)

    fun exists(vertex: Vertex<T>): Boolean
    fun getAllVertices(): List<Vertex<T>>
    fun getAllConnectedVertices(fromVertex: Vertex<T>): List<Vertex<T>>
    fun findPath(fromVertex: Vertex<T>, toVertex: Vertex<T>): List<Vertex<T>>

    fun merge(otherGraph: ConflictFreeReplicatedGraph<T>)
}