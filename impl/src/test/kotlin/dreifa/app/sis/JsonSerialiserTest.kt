package dreifa.app.sis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dreifa.app.helpers.random
import dreifa.app.types.NotRequired
import dreifa.app.types.StringList
import org.junit.jupiter.api.Assertions.fail
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.lang.Exception
import java.lang.RuntimeException
import java.math.BigDecimal
import java.util.*
import kotlin.collections.ArrayList

class JsonSerialiserTest {
    private val serialiser = JsonSerialiser()
    private val random = Random()

    @Test
    fun `should serialise a simple scalar`() {
        val serialiser = JsonSerialiser()
        val aUUID = UUID.randomUUID()

        // wire formt
        val serialised = serialiser.toPacket(aUUID)

        // a packet that can hold any data type
        val deserialisedPacket = serialiser.fromPacket(serialised)

        // get the actual value from the packet
        val roundTrippped = deserialisedPacket.value()

        assertThat(roundTrippped, equalTo(aUUID))
    }

    @Test
    fun `should round-trip data`() {
        val examples = commonExamples() + exceptionExamples()

        examples.forEach {
            try {
                val roundTripped = roundTrip(it)
                if (it is Exception) {
                    // internal stack trace doesn't serialize exactly, so object equality
                    // fails
                    assertThat(
                        it.message ?: "",
                        equalTo((roundTripped as Exception).message ?: "")
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                } else {
                    assertThat(
                        it,
                        equalTo(roundTripped)
                    ) { "Failed to round-trip $it of class ${it::class.qualifiedName}" }
                }
            } catch (ex: Exception) {
                ex.printStackTrace()
                fail("Exception ${ex.message} for round-trip $it of class ${it::class.qualifiedName}")
            }
        }
    }

    @Test
    fun `should round-trip Unit`() {
        val roundTripped = roundTrip(Unit)
        assert(roundTripped is Unit)
    }

    @Test
    fun `should round-trip NotRequired`() {
        val roundTripped = roundTrip(NotRequired.instance())
        assert(roundTripped is NotRequired)
    }

    @Test
    fun `should not serialize unsupported types`() {
        assertThrows<RuntimeException> { serialiser.toPacket(BadModel()) }
        assertThrows<RuntimeException> { serialiser.toPacket(BadEnum.random()) }
    }

    @Test
    fun `should map to SerialisationPacket`() {
        val examples = commonExamples() + exceptionExamples() + nothingExamples()

        examples.forEach {
            try {
                SerialisationPacket.create(it)
            } catch (ex: Exception) {
                fail("Exception ${ex.message} for mapDataToSerialisationPacket $it of class ${it::class.qualifiedName}")
            }
        }
    }

    @Test
    fun `should not map to SerialisationPacket for unsupported types`() {
        val examples = listOf(
            Pair(ArrayList<String>(), "Raw List classes are not allowed. Must use a subclass"),
            Pair(Date(), "Don't know how to serialise class: java.util.Date"),
            Pair(
                mapOf<Any, Any>(UUID.randomUUID() to "id"),
                "Raw maps must conform to the rules of for MapofAny"
            )
        )

        examples.forEach {
            try {
                SerialisationPacket.create(it.first)
                fail("should have thrown an exception")
            } catch (ex: Exception) {
                assertThat(ex.message, equalTo(it.second))
            }
        }
    }

    @Test
    fun shouldSerialisePayloadAlone() {
        val examples = commonExamples()

        examples.forEach {
            val startValue = it
            val serialised = serialiser.toPacketPayload(startValue)
            val deserialised = serialiser.fromPacketPayload(serialised, it::class.java.name)
            assertThat(startValue, equalTo(deserialised))
        }
    }

    private fun roundTrip(data: Any): Any {
        val serialised = serialiser.toPacket(data)
        return serialiser.fromPacket(serialised).any()
    }

    // these should work in all test cases
    private fun commonExamples(): List<Any> {
        return listOf(
            // scalars
            Colour.random(),
            Weapon.random(),
            random.nextInt(),
            random.nextLong(),
            random.nextDouble(),
            random.nextFloat(),
            random.nextBoolean(),
            BigDecimal.valueOf(random.nextDouble()),
            String.random(10),
            UUID.randomUUID(),

            // data class
            DemoModel(),

            // MapofAny
            mapOf("name" to "bob", "age" to random.nextInt(99)),

            // lists
            StringList(listOf("Mary", "had", "a", "little", "lamb")),
            ImmutableStringList(listOf("foo", "bar")),
        )
    }

    // exception types - as exceptions don't fully serialise
    // they need different assertions in test cases
    private fun exceptionExamples(): List<Any> {
        return listOf(
            RuntimeException("This went wrong"),
            DemoException("opps!"),
        )
    }

    // Types of "Nothingness"
    private fun nothingExamples(): List<Any> {
        return listOf(
            Unit,
            NotRequired(),
        )
    }
}