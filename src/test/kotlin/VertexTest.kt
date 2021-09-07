import org.junit.jupiter.api.Test

import org.junit.jupiter.api.Assertions.*
import java.math.BigDecimal

internal class VertexTest {

    @Test
    fun `hashCode() - returns same hashcode for 2 vertex of same value created at different times`() {
        val vertextOne = Vertex(1, epochTime = 0L)
        val vertextTwo = Vertex(1, epochTime = 90L)
        assertEquals(vertextOne.hashCode(), vertextTwo.hashCode())
    }

    @Test
    fun `isOutdatedComparedTo() - returns true when one vertex has higher epoch time than another vertex of same value`() {
        val vertextOne = Vertex(1, epochTime = 0L)
        val vertextTwo = Vertex(1, epochTime = 90L)
        assertTrue(vertextOne.isOutdatedComparedTo(vertextTwo))
        assertFalse(vertextTwo.isOutdatedComparedTo(vertextOne))
    }

    @Test
    fun `equals() - returns true if two vertices have same value but not same epoch time`() {
        val vertextOne = Vertex(1, epochTime = 0L)
        val vertextTwo = Vertex(1, epochTime = 90L)
        assertTrue(vertextOne.equals(vertextTwo))
    }

    @Test
    fun `equals() - returns false if two vertices have same epoch time but not same value`() {
        val vertextOne = Vertex(1, epochTime = 90L)
        val vertextTwo = Vertex(2, epochTime = 90L)
        assertFalse(vertextOne.equals(vertextTwo))
    }

    @Test
    fun `equals() - returns false if vertex is compared to another vertex type`() {
        val vertextOne = Vertex(1, epochTime = 90L)
        val vertextTwo = Vertex("1", epochTime = 90L)
        assertFalse(vertextOne.equals(vertextTwo))
    }

    @Test
    fun `equals() - returns false if vertex is compared to another object type`() {
        val vertextOne = Vertex(1, epochTime = 90L)
        assertFalse(vertextOne.equals(BigDecimal(1)))
    }

    @Test
    fun `isDummy() - returns false if is not a dummy`() {
        val vertextOne = Vertex(1, epochTime = 90L)
        assertFalse(vertextOne.isDummy())
    }

    @Test
    fun `isDummy() - returns true if vertex is a dummy`() {
        val vertextOne = Vertex(1, epochTime = 0)
        assertTrue(vertextOne.isDummy())
    }
}