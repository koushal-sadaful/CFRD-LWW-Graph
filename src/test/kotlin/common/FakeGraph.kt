package common

import ConflictFreeReplicatedGraph
import Edge
import Vertex

class FakeGraph<T> : ConflictFreeReplicatedGraph<T> {
    override fun addVertex(vertex: Vertex<T>) {
        TODO("Not yet implemented")
    }

    override fun removeVertex(vertex: Vertex<T>) {
        TODO("Not yet implemented")
    }

    override fun addEdge(edge: Edge<T>) {
        TODO("Not yet implemented")
    }

    override fun removeEdge(edge: Edge<T>) {
        TODO("Not yet implemented")
    }

    override fun exists(vertex: Vertex<T>): Boolean {
        TODO("Not yet implemented")
    }

    override fun getAllVertices(): List<Vertex<T>> {
        TODO("Not yet implemented")
    }

    override fun getAllConnectedVertices(fromVertex: Vertex<T>): List<Vertex<T>> {
        TODO("Not yet implemented")
    }

    override fun findPath(fromVertex: Vertex<T>, toVertex: Vertex<T>): List<Vertex<T>> {
        TODO("Not yet implemented")
    }

    override fun merge(graph: ConflictFreeReplicatedGraph<T>) {
        TODO("Not yet implemented")
    }

}