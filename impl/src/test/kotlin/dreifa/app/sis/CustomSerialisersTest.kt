package dreifa.app.sis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dreifa.app.types.UniqueId
import org.junit.jupiter.api.Test

class CustomSerialisersTest {
    private val serialiser = JsonSerialiser()
    private val testId = UniqueId.fromString("abc123")

    @Test
    fun `should serialise UniqueId as scalar`() {
        val serialised = serialiser.toPacketPayload(testId)
        assertThat(serialised, equalTo("abc123"))
    }

    fun `should deserialise UniqueId as scalar`() {
        val deserialised = serialiser.fromPacketPayload("abc123", "dreifa.app.types.UniqueId")
        assertThat(deserialised, equalTo(testId))
    }

    @Test
    fun `should serialise UniqueId inside data class as scalar`() {
        val data = EmbeddedUniqueId(testId)
        val serialised = serialiser.toPacketPayload(data)
        assertThat(serialised, equalTo("{\"theId\":\"abc123\"}"))
    }

    @Test
    fun `should deserialise UniqueId inside data class as scalar`() {
        val deserialised = serialiser.fromPacketPayload("{\"theId\":\"abc123\"}", "dreifa.app.sis.EmbeddedUniqueId")
        assertThat(deserialised, equalTo(EmbeddedUniqueId(testId)))
    }

}