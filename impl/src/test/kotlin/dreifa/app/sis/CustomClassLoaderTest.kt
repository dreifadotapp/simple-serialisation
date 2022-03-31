package dreifa.app.sis

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import dreifa.app.types.UniqueId
import org.junit.jupiter.api.Test
import java.io.File
import java.lang.RuntimeException
import java.net.URLClassLoader

class CustomClassLoaderTest
{
    @Test
    fun `should serialise classes in custom classloader`() {
        val loader = terraFormTasksClassLoader()
        val serialiser = JsonSerialiser(loader)

        val className = "dreifa.app.terraform.tasks.TFInitModuleRequest"

        val clazz = loader.loadClass(className).kotlin

        val data = clazz
            .constructors
            .single {it.parameters.size == 1}
            .call(UniqueId.alphanumeric())

        //println(clazz)
        //println(data)

        val roundTripped = roundTrip(serialiser,data)
        //println(roundTripped)

        assertThat(roundTripped, equalTo(data))
    }

    private fun roundTrip(serialiser : JsonSerialiser, data: Any): Any {
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