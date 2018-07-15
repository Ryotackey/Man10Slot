package red.man10.man10slot

import org.bukkit.plugin.java.JavaPlugin
import red.man10.kotlin.CustomConfig
import java.io.File

class Man10Slot : JavaPlugin() {

    val loccon = CustomConfig(this, "location.yml")

    override fun onEnable() {
        // Plugin startup logic

        logger.info(this.file.absolutePath)

    }

    override fun onDisable() {
        // Plugin shutdown logic
    }
}
