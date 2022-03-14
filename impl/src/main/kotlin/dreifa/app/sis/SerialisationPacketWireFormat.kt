package dreifa.app.sis

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider

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

//class XX : JsonSerializer<SerialisationPacketWireFormat>() {
//    override fun serialize(value: SerialisationPacketWireFormat, gen: JsonGenerator, provider: SerializerProvider?) {
//        gen.writeStartObject()
//        value.scalar.let {
//                gen.write("scaler", value.scalar)
//
//            }
//        }
//        gen.writeEndObject()
//    }
//
//
//}