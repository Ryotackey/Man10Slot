package red.man10.man10slot

import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

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
                        p.sendMessage(i.key + "/" + i.value.wining_name)
                        p.sendMessage(i.key + "/" + i.value.wining_item)
                        p.sendMessage(i.key + "/" + i.value.wining_chance)
                    }
                }

                if (args[0].equals("save", ignoreCase = true)){
                    plugin.locationSave()
                    p.sendMessage(plugin.prefix + "§asave complete")
                    return true
                }

            }

            2->{

                if (args[0].equals("create", ignoreCase = true)){

                    if (!plugin.slotmap.containsKey(args[1])){
                        p.sendMessage(plugin.prefix + "§cそのスロットは見つかりませんでした")
                        return true
                    }

                    plugin.buttonselectmap[p] = mutableListOf(args[1])
                    p.sendMessage(plugin.prefix + "§aボタンとなるブロックを3つ設置してください")

                    return true
                }

            }

        }

        return false

    }

}