data class Vertex<T>(
    val value: T,
    var epochTime: Long = System.currentTimeMillis()
) {

    //Override this make built-in functions search the value instead of comparing the epoch time
    override fun hashCode(): Int {
        return value?.hashCode() ?: super.hashCode()
    }

    fun isOutdatedComparedTo(other: Vertex<T>): Boolean {
        return this.epochTime < other.epochTime
    }

    fun isDummy(): Boolean {
        return this.epochTime == 0L
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Vertex<*>)
            return false
        else {
            if (other.value == this.value)
                return true
        }
        return false
    }
}
