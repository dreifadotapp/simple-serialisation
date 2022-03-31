package dreifa.app.sis

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import dreifa.app.types.UniqueId

class JsonSerialiser(private val clazzLoader: ClassLoader? = null) {
    private val mapper: ObjectMapper
    private val reflectionsSupport: ReflectionsSupport

    init {
        mapper = if (clazzLoader == null) {
            CachedMapper.cached()
        } else {
            val newMapper = CachedMapper.fresh()
            newMapper.setTypeFactory(newMapper.typeFactory.withClassLoader(clazzLoader))
        }
        reflectionsSupport = if (clazzLoader == null) {
            ReflectionsSupport()
        } else {
            ReflectionsSupport(clazzLoader)
        }
    }

    fun fromPacket(serialised: String): SerialisationPacket {
        val raw = mapper.readValue(serialised, SerialisationPacketWireFormat::class.java)
        val clazz = reflectionsSupport.forClass(raw.clazzName)

        return when {
            raw.scalar != null -> {
                val scalar = reflectionsSupport.deserialiseScalar(raw.scalar, clazz)
                SerialisationPacket.create(scalar)
            }
            raw.data != null -> {
                val data = mapper.readValue(raw.data, clazz.java)
                SerialisationPacket.create(data)
            }
            raw.map != null -> {
                val data = mapper.readValue(raw.map, clazz.java)
                SerialisationPacket.create(data)
            }
            raw.list != null -> {
                val list = mapper.readValue(raw.list, clazz.java)
                SerialisationPacket.create(list)
            }
            raw.exception != null -> {
                val exception = mapper.readValue(raw.exception, clazz.java)
                SerialisationPacket.create(exception)
            }
            else -> {
                // only option left is one of the "nothing" types
                val nothing = reflectionsSupport.deserialiseNothing(raw.clazzName)
                SerialisationPacket.create(nothing)
            }
        }
    }

    fun toPacket(data: Any): String {
        val packet = SerialisationPacket.create(data)
        val wire = packetToWireFormat(packet)
        return mapper.writeValueAsString(wire)
    }

    fun toPacketPayload(data: Any): String? {
        val packet = SerialisationPacket.create(data)
        return serialisePacketPayload(packet)
    }

    fun fromPacketPayload(serialised: String, clazzName: String): Any {
        val clazz = reflectionsSupport.forClass(clazzName)
        return when {
            reflectionsSupport.isScalar(clazz) -> reflectionsSupport.deserialiseScalar(
                serialised,
                clazz
            )
            reflectionsSupport.isEnum(clazz) -> reflectionsSupport.deserialiseScalar(
                serialised,
                clazz
            )

            else -> mapper.readValue(serialised, clazz.java)
        }

    }


    private fun packetToWireFormat(packet: SerialisationPacket): SerialisationPacketWireFormat {
        val payload = serialisePacketPayload(packet)
        return when {
            packet.scalar != null -> SerialisationPacketWireFormat(clazzName = packet.clazzName(), scalar = payload)
            packet.data != null -> SerialisationPacketWireFormat(clazzName = packet.clazzName(), data = payload)
            packet.map != null -> SerialisationPacketWireFormat(clazzName = packet.clazzName(), map = payload)
            packet.list != null -> SerialisationPacketWireFormat(clazzName = packet.clazzName(), list = payload)
            packet.exception != null -> SerialisationPacketWireFormat(
                clazzName = packet.clazzName(),
                exception = payload
            )
            packet.nothingClazz != null -> SerialisationPacketWireFormat(clazzName = packet.clazzName())
            else -> throw java.lang.RuntimeException("Cannot map SerialisationPacket: $packet")
        }
    }

    private fun serialisePacketPayload(packet: SerialisationPacket): String? {
        return when {
            packet.scalar != null -> packet.scalar.toString()
            packet.data != null -> mapper.writeValueAsString(packet.data)
            packet.map != null -> mapper.writeValueAsString(packet.map)
            packet.list != null -> mapper.writeValueAsString(packet.list)
            packet.exception != null -> mapper.writeValueAsString(packet.exception)
            packet.nothingClazz != null -> null
            else -> throw java.lang.RuntimeException("Cannot map SerialisationPacket: $packet")
        }
    }

    object CachedMapper {
        private val module = KotlinModule()
        private val cached: ObjectMapper = fresh()

        fun fresh(): ObjectMapper {
            val mapper = ObjectMapper()
            module.addSerializer(UniqueId::class.java, UniqueIdSerialiser())
            module.addDeserializer(UniqueId::class.java, UniqueIdDeserialiser())
            mapper.registerModule(module)
            return mapper
        }

        fun cached(): ObjectMapper = cached
    }
}

