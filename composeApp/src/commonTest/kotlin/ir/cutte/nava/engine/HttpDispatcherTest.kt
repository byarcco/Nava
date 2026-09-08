package ir.cutte.nava.engine

import kotlin.test.Test
import kotlin.test.assertEquals

class HttpDispatcherTest {

    @Test
    fun verifiesDefaultAuthTokenConstant() {
        assertEquals("85d8ecf6-5641-451a-a41d-20927eeccd28", HttpDispatcher.DEFAULT_AUTH_TOKEN)
    }
}
