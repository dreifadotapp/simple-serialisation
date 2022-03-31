package dreifa.app.sis

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dreifa.app.types.UniqueId

data class SerialisationPacketWireFormat(
    val scalar: String? = null,
    val data: String? = null,
    val map: String? = null,
    val list: String? = null,
    val exception: String? = null,
    val clazzName: String
) {
    private fun all(): List<Any?> = listOf(scalar, data, map, list, exception)
    fun any(): Any = all().single { it != null }!!
}
