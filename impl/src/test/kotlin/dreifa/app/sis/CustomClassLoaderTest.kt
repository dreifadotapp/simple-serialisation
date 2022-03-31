package dreifa.app.sis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dreifa.app.types.UniqueId
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.io.File
import java.lang.RuntimeException
import java.net.URLClassLoader

class CustomClassLoaderTest {
    @Test
    fun `should serialise classes in custom classloader`() {
        // 1. Custom class loader
        val loader = terraFormTasksClassLoader()

        // 2. Create an instance of a class via reflections
        val className = "dreifa.app.terraform.tasks.TFInitModuleRequest"
        val clazz = loader.loadClass(className).kotlin
        val data = clazz
            .constructors
            .single { it.parameters.size == 1 }
            .call(UniqueId.alphanumeric())

        // 3. works if  class loader is passed
        val roundTripped = roundTrip(JsonSerialiser(loader), data)
        assertThat(roundTripped, equalTo(data))

        // 4. fails for regular serialiser
        assertThrows<ClassNotFoundException> { roundTrip(JsonSerialiser(), data) }
    }

    private fun roundTrip(serialiser: JsonSerialiser, data: Any): Any {
        val serialised = serialiser.toPacket(data)
        return serialiser.fromPacket(serialised).any()
    }

    private fun terraFormTasksClassLoader(): ClassLoader {
        val jarFilePath = File(File("src/test/resources/terraform-tasks.jar").canonicalPath)
        if (!jarFilePath.exists()) throw RuntimeException("opps")
        return URLClassLoader(
            arrayOf(jarFilePath.toURI().toURL()),
            javaClass.classLoader
        )
    }
}