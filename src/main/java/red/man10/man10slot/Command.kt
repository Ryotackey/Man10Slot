package red.man10.man10slot

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class Command(val plugin: Man10Slot): CommandExecutor {

    override fun onCommand(sender: CommandSender?, command: Command?, label: String?, args: Array<out String>?): Boolean {

        if (sender !is Player)return false
        val p: Player = sender

        if (args == null)return false

        when(args.size){

            0->{}

            1->{

                if (args[0].equals("list", ignoreCase = true)){
                    for (i in plugin.slotmap){
                        p.sendMessage("${i.key}")
                        for (j in i.value.wining_item){
                            p.sendMessage(j.value.size.toString())
                        }
                    }
                }

                if (args[0].equals("save", ignoreCase = true)){
                    plugin.locationSave()
                    p.sendMessage(plugin.prefix + "§asave complete")
                    return true
                }

                if (args[0].equals("reload", ignoreCase = true)){
                    plugin.slotmap.clear()
                    plugin.slotLoad()
                    p.sendMessage(plugin.prefix + "§areload complete")
                    return true
                }

            }

            2->{

                if (args[0].equals("create", ignoreCase = true)){

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

                    if (!plugin.signloc.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは設置されていません")
                        return true
                    }

                    plugin.removeLocation(args[1])

                    return true

                }

                if (args[0].equals("show", ignoreCase = true)){
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

                    val winlist = plugin.mapSort(slot.wining_chance)

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

                    for (i in count){
                        p.sendMessage("§a§l${i.key} §f§l: §e§l${i.value}回")
                    }

                }
            }

        }

        return false

    }

}