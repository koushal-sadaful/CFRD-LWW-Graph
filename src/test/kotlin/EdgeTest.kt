import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.math.BigDecimal

internal class EdgeTest {

    @Test
    fun `hashCode() - returns same hashcode for 2 edges of same value created at different times`() {
        val edgeOne = Edge(1, 2, epochTime = 0L)
        val edgeTwo = Edge(1, 2, epochTime = 90L)
        assertEquals(edgeOne.hashCode(), edgeTwo.hashCode())
    }

    @Test
    fun `isOutdatedComparedTo() - returns true when one edge has higher epoch time than another edge of same value`() {
        val edgeOne = Edge(1, 2, epochTime = 0L)
        val edgeTwo = Edge(1, 2, epochTime = 90L)
        assertTrue(edgeOne.isOutdatedComparedTo(edgeTwo))
        assertFalse(edgeTwo.isOutdatedComparedTo(edgeOne))
    }

    @Test
    fun `equals() - returns true if two edges have same value but not same epoch time`() {
        val edgeOne = Edge(1, 2, epochTime = 0L)
        val edgeTwo = Edge(1, 2, epochTime = 90L)
        assertTrue(edgeOne.equals(edgeTwo))
    }

    @Test
    fun `equals() - returns true if two edges have same values but not same direction`() {
        val edgeOne = Edge(1, 2, epochTime = 90L)
        val edgeTwo = Edge(2, 1, epochTime = 90L)
        assertTrue(edgeOne.equals(edgeTwo))
    }

    @Test
    fun `equals() - returns false if two edges have same epoch time but not same value`() {
        val edgeOne = Edge(1, 3, epochTime = 90L)
        val edgeTwo = Edge(2, 1, epochTime = 90L)
        assertFalse(edgeOne.equals(edgeTwo))
    }

    @Test
    fun `equals() - returns false if edge is compared to another edge type`() {
        val edgeOne = Edge(1, 2,epochTime = 90L)
        val edgeTwo = Edge("1", "2", epochTime = 90L)
        assertFalse(edgeOne.equals(edgeTwo))
    }

    @Test
    fun `equals() - returns false if edge is compared to another object type`() {
        val edgeOne = Edge(1, 3, epochTime = 90L)
        assertFalse(edgeOne.equals(BigDecimal(1)))
    }
}