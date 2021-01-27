import java.io.File
import java.io.FileInputStream
import java.nio.file.Paths
import java.util.*

object AndroidConfig {
    @JvmStatic
    fun packageCloudToken(localPropFile: File): String {

        val prop = Properties()
        val path = Paths.get(localPropFile.absolutePath, "local.properties")
        FileInputStream(path.toFile()).use { prop.load(it) }

        return prop.getProperty("STONE_AUTH_PACKAGECLOUD_READ_TOKEN")
    }
}