package io.mochadwi.yukmengaji

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see [Testing documentation](http://d.android.com/tools/testing)
 */
class ExampleUnitTest {

    @Test
    fun `trim all spaces`() {
        assertEquals("alikhlas", "al ikhlas".replace("\\s".toRegex(), ""))
    }
}