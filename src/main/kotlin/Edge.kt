data class Edge<T>(
    val fromValue: T,
    val toValue: T,
    var epochTime: Long = System.currentTimeMillis()
) {
    override fun hashCode(): Int {
        return fromValue!!.hashCode().and(toValue!!.hashCode())
    }

    fun isConnectedTo(vertex: Vertex<T>): Boolean {
        return (vertex.value === fromValue || vertex.value === toValue)
    }

    fun isOutdatedComparedTo(other: Edge<T>): Boolean {
        return this.epochTime < other.epochTime
    }

    fun isOutdatedComparedTo(other: Vertex<T>): Boolean {
        return this.epochTime < other.epochTime
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Edge<*>)
            return false
        else {
            if (other.fromValue == this.fromValue && other.toValue == this.toValue ||
                other.toValue == this.fromValue && other.fromValue == this.toValue
            )
                return true
        }
        return false
    }
}