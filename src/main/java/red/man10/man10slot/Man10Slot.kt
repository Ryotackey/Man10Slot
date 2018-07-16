package red.man10.man10slot

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import red.man10.kotlin.CustomConfig
import java.io.File
import java.util.*
import kotlin.collections.HashMap

class Man10Slot : JavaPlugin() {

    val loccon = CustomConfig(this, "location.yml")

    var slotfile = File(this.dataFolder.absolutePath + "\\slots")

    var slotlist = mutableListOf<FileConfiguration>()

    val slotmap = HashMap<String, SlotInformation>()

    val buttonselectmap = HashMap<Player, MutableList<String>>()
    val frameselectmap = HashMap<Player, MutableList<String>>()
    val leverselect = HashMap<Player, String>()
    val lightselect = HashMap<Player, String>()
    val signselect = HashMap<Player, String>()

    var button1loc = HashMap<Location, String>()
    var button2loc = HashMap<Location, String>()
    var button3loc = HashMap<Location, String>()
    val frameloc = HashMap<String, MutableList<Location>>()
    var leverloc = HashMap<Location, String>()
    val signloc = HashMap<String, Location>()
    val lightloc = HashMap<String, Location>()

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    override fun onEnable() {
        // Plugin startup logic

        logger.info(this.dataFolder.absolutePath + "\\slots")

        loccon.saveDefaultConfig()

        slotfile.mkdir()

        val filelist = slotfile.listFiles()

        slotlist = fileToConfig(filelist)
        slotLoad()

        loadLocation()

        server.pluginManager.registerEvents(Event(this), this)
        getCommand("mslot").executor = Command(this)
    }

    override fun onDisable() {
        // Plugin shutdown logic
        locationSave()
    }

    fun locationSave(){

        val config = loccon.getConfig() ?: return

        saveLocationString(config, "button1loc", button1loc)
        saveLocationString(config, "button2loc", button2loc)
        saveLocationString(config, "button3loc", button3loc)

        saveLocationString(config, "leverloc", leverloc)

        for (i in signloc){
            val loc = "${i.value.blockX}/${i.value.blockY}/${i.value.blockZ}/${i.value.world.name}"
            config.set("location.signloc.${i.key}", loc)
        }

        for (i in lightloc){
            val loc = "${i.value.blockX}/${i.value.blockY}/${i.value.blockZ}/${i.value.world.name}"
            config.set("location.lightloc.${i.key}", loc)
        }

        for (i in frameloc){

            val list = mutableListOf<String>()

            for (j in i.value){
                val loc = "${j.blockX}/${j.blockY}/${j.blockZ}/${j.world.name}"
                list.add(loc)
            }

            config.set("location.frameloc.${i.key}", list)

        }

        loccon.saveConfig()

    }

    fun saveLocationString(config: FileConfiguration, name: String, map: HashMap<Location, String>){
        for (i in map){
            val loc = "${i.key.blockX}/${i.key.blockY}/${i.key.blockZ}/${i.key.world.name}"
            config.set("location.$name.${i.value}", loc)
        }
    }

    fun loadLocation(){

        val config = loccon.getConfig() ?: return

        button1loc = loadLocationString(config, "button1loc")
        button2loc = loadLocationString(config, "button2loc")
        button3loc = loadLocationString(config, "button3loc")

        leverloc = loadLocationString(config, "leverloc")

        for (key in config.getConfigurationSection("location.signloc").getKeys(false)){

            val loc = strToLocation(config.getString("location.signloc.$key"))

            signloc[key] = loc
        }

        for (key in config.getConfigurationSection("location.lightloc").getKeys(false)){

            val loc = strToLocation(config.getString("location.lightloc.$key"))

            lightloc[key] = loc
        }

        for (key in config.getConfigurationSection("location.frameloc").getKeys(false)){

            val loclist = mutableListOf<Location>()

            for (i in config.getList("location.frameloc.$key") as MutableList<String>) {
                val loc = strToLocation(i)
                loclist.add(loc)
            }

            frameloc[key] = loclist
        }

    }

    fun loadLocationString(config: FileConfiguration, name: String): HashMap<Location, String>{

        val map = HashMap<Location, String>()

        for (key in config.getConfigurationSection("location.$name").getKeys(false)){

            val loc = strToLocation(config.getString("location.$name.$key"))

            map[loc] = key
        }
        return map
    }

    fun strToLocation(str: String): Location{

        val locstr = str.split(Regex("/"))

        val loc = Location(Bukkit.getWorld(locstr[3]), locstr[0].toDouble(), locstr[1].toDouble(), locstr[2].toDouble())

        return loc
    }

    fun slotLoad(){

        if (slotlist.size == 0)return

        for (config in slotlist){
            for (key in config.getConfigurationSection("").getKeys(false)) {
                val slot = SlotInformation()

                slot.slot_name = config.getString("$key.general_setting.slot_name")
                slot.price = config.getDouble("$key.general_setting.price")

                val reel1 = stringToItemList(config.getString("$key.general_setting.items_reel1"))
                val reel2 = stringToItemList(config.getString("$key.general_setting.items_reel2"))
                val reel3 = stringToItemList(config.getString("$key.general_setting.items_reel3"))

                slot.reel1 = reel1
                slot.reel2 = reel2
                slot.reel3 = reel3

                slot.stock = config.getDouble("$key.general_setting.stock")

                for (win in config.getConfigurationSection("$key.wining_setting").getKeys(false)){

                    slot.wining_name[win] = config.getString("$key.wining_setting.$win.name")
                    slot.wining_item[win] = wining_itemCreate(config.getString("$key.wining_setting.$win.item"), reel1, reel2, reel3)
                    slot.wining_prize[win] = config.getDouble("$key.wining_setting.$win.prize")
                    slot.wining_stockodds[win] = config.getDouble("$key.wining_setting.$win.stockodds")
                    slot.wining_chance[config.getDouble("$key.wining_setting.$win.chance")] = win
                    slot.wining_step[win] = config.getInt("$key.wining_setting.$win.step")
                    slot.wining_command[win] = config.getList("$key.wining_setting.$win.command") as MutableList<String>
                    slot.wining_con[win] = config.getBoolean("$key.wining_setting.$win.flag_con")
                    slot.wining_light[win] = config.getBoolean("$key.wining_setting.$win.light")

                    if (config.contains("$key.wining_setting.$win.lightsound")){
                        val sound = Sound()
                        sound.sound = org.bukkit.Sound.valueOf(config.getString("$key.wining_setting.$win.lightsound.sound"))
                        sound.volume = config.getDouble("$key.wining_setting.$win.lightsound.volume").toFloat()
                        sound.pitch = config.getDouble("$key.wining_setting.$win.lightsound.pitch").toFloat()

                        slot.wining_lightsound[win] = sound
                    }

                    val sound = Sound()
                    sound.sound = org.bukkit.Sound.valueOf(config.getString("$key.wining_setting.$win.winsound.sound"))
                    sound.volume = config.getDouble("$key.wining_setting.$win.winsound.volume").toFloat()
                    sound.pitch = config.getDouble("$key.wining_setting.$win.winsound.pitch").toFloat()

                    slot.wining_sound[win] = sound

                }

                val sound = Sound()
                sound.sound = org.bukkit.Sound.valueOf(config.getString("$key.sound_setting.spinsound.sound"))
                sound.volume = config.getDouble("$key.sound_setting.spinsound.volume").toFloat()
                sound.pitch = config.getDouble("$key.sound_setting.spinsound.pitch").toFloat()

                slot.spinSound = sound

                slotmap[key] = slot
            }

        }

    }

    fun stringToItemList(itemstr: String): MutableList<ItemStack>{

        val reel = mutableListOf<ItemStack>()

        if (!itemstr.contains(Regex(",")))return reel

        val item = itemstr.split(Regex(",")) as MutableList<String>

        for (i in item){
            var reelitem: ItemStack? = null

            if (i.contains(Regex("-")))reelitem = ItemStack(Material.getMaterial(i.split(Regex("-"))[0].toInt()), 1, i.split(Regex("-"))[1].toShort())
            else reelitem = ItemStack(Material.getMaterial(i.toInt()))

            reel.add(reelitem)
        }
        return reel
    }

    fun wining_itemCreate(itemstr: String, reel1: MutableList<ItemStack>, reel2: MutableList<ItemStack>, reel3: MutableList<ItemStack>): MutableList<ItemStack>{

        val win_item = mutableListOf<ItemStack>()

        if (!itemstr.contains(Regex(",")))return win_item

        val item = itemstr.split(Regex(","))

        if (item.size > 3)return win_item

        for (i in 0 until item.size){

            if (item[i] == "*"){
                win_item.add(ItemStack(Material.AIR))
            }else {
                var num = 0

                try {
                    num = item[i].toInt()
                } catch (e: NumberFormatException) {
                    return win_item
                }

                when (i) {
                    0 -> win_item.add(reel1[num - 1])
                    1 -> win_item.add(reel2[num - 1])
                    2 -> win_item.add(reel3[num - 1])
                }
            }
        }
        return win_item
    }

    fun fileToConfig(list: Array<File>): MutableList<FileConfiguration>{

        val slotlist = mutableListOf<FileConfiguration>()

        for (i in list){

            if (!i.isFile)continue
            if (!i.path.endsWith(".yml"))continue

            val config = YamlConfiguration.loadConfiguration(i)
            slotlist.add(config)
        }
        return slotlist
    }

    fun mapSort(map: HashMap<Double, String>): MutableList<kotlin.collections.Map.Entry<Double, String>>{

        val list = mutableListOf<kotlin.collections.Map.Entry<Double, String>>()

        map.entries.stream()
                .sorted(java.util.Collections.reverseOrder(java.util.Map.Entry.comparingByKey<Double, String>()))
                .forEach { s -> list.add(s)}

        return list
    }

    class SlotInformation{

        var slot_name = ""
        var price: Double = 0.0

        var reel1 = mutableListOf<ItemStack>()
        var reel2 = mutableListOf<ItemStack>()
        var reel3 = mutableListOf<ItemStack>()

        var stock = 0.0

        var win = "0"
        var block: Block? = null

        val wining_name = HashMap<String, String>()
        val wining_item = HashMap<String, MutableList<ItemStack>>()
        val wining_prize = HashMap<String, Double>()
        val wining_stockodds = HashMap<String, Double>()
        val wining_chance = HashMap<Double, String>()
        val wining_step = HashMap<String, Int>()
        val wining_command = HashMap<String, MutableList<String>>()
        val wining_con = HashMap<String, Boolean>()
        val wining_light = HashMap<String, Boolean>()
        val wining_lightsound = HashMap<String, Sound?>()
        val wining_sound = HashMap<String, Sound>()
        val stopsound1 = HashMap<String, Sound?>()
        val stopsound2 = HashMap<String, Sound?>()
        val stopsound3 = HashMap<String, Sound?>()

        var spinSound = Sound()

        var spin1 = false
        var spin2 = false
        var spin3 = false

        var p: Player? = null

    }

    class Sound{
        var sound: org.bukkit.Sound? = null
        var volume = 0f
        var pitch = 0f
    }
}
