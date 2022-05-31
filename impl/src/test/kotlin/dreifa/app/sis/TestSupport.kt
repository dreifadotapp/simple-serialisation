package dreifa.app.sis

import dreifa.app.helpers.random
import dreifa.app.types.*
import java.io.File
import java.lang.RuntimeException
import java.util.*
import kotlin.collections.ArrayList

enum class Colour {
    Red, Green, Blue;

    companion object {
        fun random(): Colour = values()[Random().nextInt(2)]
    }
}

enum class Weapon(val weaponName: String, val damage: Int) {
    Sword("Sword", 12),
    Axe("Axe", 13),
    Bow("Bow", 14);

    companion object {
        fun random(): Weapon = values()[Random().nextInt(2)]
    }
}

enum class BadEnum(val enumName: String, val bad: BadModelA = BadModelA()) {
    One("One"),
    Two("Two"),
    Three("Three");

    companion object {
        fun random(): BadEnum = values()[Random().nextInt(2)]
    }
}


class DemoException(message: String) : RuntimeException(message) {
    override fun equals(other: Any?): Boolean {
        return if (other is DemoException) {
            other.message == this.message
        } else false
    }

    override fun hashCode(): Int {
        return (super.hashCode())
    }
}

// Should include all valid types
data class DemoModel(
    val string: String = String.random(80),
    val int: Int = Random().nextInt(),
    val long: Long = Random().nextLong(),
    val double: Double = Random().nextDouble(),
    val float: Float = Random().nextFloat(),
    val boolean: Boolean = Random().nextBoolean(),
    val colour: Colour = Colour.random(),
    val uniqueId: UniqueId = UniqueId.randomUUID(),
    //val notRequired : NotRequired = NotRequired.instance(), // need to fix equality on NotRequired for tests to pass
    val stringList: StringList = StringList(listOf(String.random(), String.random(), String.random())),
    val immutableStringList: ImmutableStringList = ImmutableStringList(listOf("foo", "bar")),
    val exception: DemoException = DemoException("oops"),
    val mapOfAny: Map<String, Any?> = mapOf("string" to String.random(), "null" to null),
    val nested: DemoModel? = null
)

// not serializable
// as we are testing failures, only a single attribute is allowed, hence BadModelA, BadModelB and so on
data class BadModelA(
    val file: File = File("."),

    //val badEnum: BadEnum = BadEnum.random()
)

data class BadModelB(val rawList: List<Int> = listOf(1, 2, 3))

data class BadModelC(val rawList: List<Int> = ArrayList())

data class BadModelD(val rawList: List<Int> = LinkedList())




class ImmutableStringList(items: List<String>) : SimpleImmutableList<String>(items)


class MapModel(private val name: String) : ToMapOfAny {

    override fun toMap(): MapOfAny {
        return mapOf("name" to name).toMapOfAny()
    }

    companion object : FromMapOfAny<MapModel> {
        override fun fromMap(data: MapOfAny): MapModel {
            return MapModel(data["name"] as String)
        }
    }

    override fun equals(other: Any?): Boolean {
        return if (other is MapModel) {
            other.name == this.name
        } else false
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}

data class EmbeddedUniqueId(val theId: UniqueId)

//open class CustomError(): Error
//
//sealed class Colour()
//
//sealed class IOError(): Colour
//

