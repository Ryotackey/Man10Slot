package red.man10.man10slot

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import red.man10.man10vaultapiplus.MoneyPoolObject
import java.util.*
import kotlin.collections.HashMap

class Command(val plugin: Man10Slot): CommandExecutor {

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (sender !is Player)return false
        val p: Player = sender

        if (args == null)return false

        when(args.size){

            0->{
                p.sendMessage("§e§l-------${plugin.prefix}§e§l-------")
                sender.sendMessage("§6§l/mslot create [名前]§f§r:[名前]のスロットを設置する")
                sender.sendMessage("§6§l/mslot list§f§r:ロードされているスロットのリストを見る")
                sender.sendMessage("§6§l/mslot save§f§r:スロットの設置してある場所を保存する")
                sender.sendMessage("§6§l/mslot reload§f§r:スロットファイルをリロードする")
                sender.sendMessage("§6§l/mslot show [名前]§f§r:[名前]のスロットの回された数などの情報を見れる")
                sender.sendMessage("§6§l/mslot simulate [名前] [回数]§f§r:[名前]のスロットを[回数]分回したシミュレーション結果が見れる")
                sender.sendMessage("§6§l/mslot on/off§f§r:スロットを回すのをon/offできる")
                sender.sendMessage("§b§lcreated by Ryotackey")
                sender.sendMessage("§e§l------------------------------")
            }

            1->{

                if (args[0].equals("list", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    for (i in plugin.slotmap){
                        p.sendMessage(i.key)
                    }
                    return true
                }

                if (args[0].equals("save", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    plugin.locationSave()
                    p.sendMessage(plugin.prefix + "§asave complete")
                    return true
                }

                if (args[0].equals("reload", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    plugin.slotmap.clear()
                    plugin.slotLoad()
                    p.sendMessage(plugin.prefix + "§areload complete")
                    return true
                }

                if (args[0].equals("on", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (plugin.enable){
                        p.sendMessage("${plugin.prefix}§cすでにオンです")
                        return true
                    }

                    plugin.loccon.getConfig()!!.set("enable", true)
                    plugin.loccon.saveConfig()
                    plugin.enable = true

                    p.sendMessage("${plugin.prefix}§aオンにしました")

                    return true

                }

                if (args[0].equals("off", ignoreCase = true)){

                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.enable){
                        p.sendMessage("${plugin.prefix}§cすでにオフです")
                        return true
                    }

                    plugin.loccon.getConfig()!!.set("enable", false)
                    plugin.loccon.saveConfig()
                    plugin.enable = false

                    p.sendMessage("${plugin.prefix}§aオフにしました")

                    return true

                }

                if (args[0].equals("countryview", ignoreCase = true)){

                    val pool = MoneyPoolObject("Man10Slot", 1)

                    p.sendMessage(pool.balance.toString())

                }

            }

            2->{

                if (args[0].equals("create", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.slotmap.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは見つかりませんでした")
                        return true
                    }

                    if (plugin.signloc.containsKey(args[1])){
                            p.sendMessage(plugin.prefix + "§cそのスロットは設置されています")
                        return true
                    }

                    plugin.buttonselectmap[p] = mutableListOf(args[1])
                    p.sendMessage(plugin.prefix + "§aボタンとなるブロックを3つ設置してください")

                    return true
                }

                if (args[0].equals("remove", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.signloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されていません")
                        return true
                    }

                    plugin.removeLocation(args[1])

                    p.sendMessage(plugin.prefix + "§aremove complete")

                    return true

                }

                if (args[0].equals("show", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true
                    if (!plugin.slotmap.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは見つかりませんでした")
                        return true
                    }

                    val slot = plugin.slotmap[args[1]]!!

                    p.sendMessage("§e§l--------§b§l${args[1]}§e§l--------")
                    p.sendMessage("§l回された回数 : §e§l${slot.spincount}回")
                    p.sendMessage("§l当たった役と回数")

                    for (i in slot.wincount){
                        p.sendMessage("§a§l${i.key} §f§l: §e§l${i.value}回")
                    }

                    return true

                }

            }

            3->{
                if (args[0].equals("simulate", ignoreCase = true)){
                    if (!p.hasPermission("mslot.setting")) return true

                    if (!plugin.slotmap.containsKey(args[1])) {
                        p.sendMessage(plugin.prefix + "§cこのスロットは存在しません")
                        return true
                    }

                    var amount = 0

                    try {
                       amount = args[2].toInt()
                    }catch (e: NumberFormatException){
                        p.sendMessage(plugin.prefix + "§c数字を指定してください")
                        return true
                    }

                    val count = HashMap<String, Int>()

                    val slot = plugin.slotmap[args[1]]!!

                    val map = HashMap<Double, String>()

                    for (i in slot.wining_chance){

                        val num = slot.chancenum % i.value.size

                        map[i.value[num]] = i.key

                    }

                    val winlist = plugin.mapSort(map)

                    for (i in 0 until amount) {

                        var win = "0"

                        for (j in winlist) {

                            val ran = Random().nextDouble()

                            if (j.key > ran) win = j.value
                        }

                        if (win != "0"){
                            if (count.containsKey(win)){
                                count[win] = count[win]!! + 1
                            }else{
                                count[win] = 1
                            }
                        }

                    }

                    p.sendMessage("§aシュミレーション結果")
                    p.sendMessage("§l${amount}回中")

                    var c = 0

                    for (i in count){
                        p.sendMessage("§a§l${i.key} §f§l: §e§l${i.value}回")
                        c += i.value
                    }

                    p.sendMessage("§l総当たり回数§e§l${c}回")

                    return true

                }

            }

        }

        return false

    }

}