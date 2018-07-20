package red.man10.man10slot

import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.EntityType
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.hanging.HangingPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import red.man10.man10vaultapiplus.Man10VaultAPI
import red.man10.man10vaultapiplus.enums.TransactionCategory
import red.man10.man10vaultapiplus.enums.TransactionType
import java.util.*

class Event(val plugin: Man10Slot): Listener {

    @EventHandler
    fun blockPlace(e: BlockPlaceEvent){

        val p = e.player
        val b = e.block

        if (plugin.buttonselectmap.containsKey(p)) {

            val key = plugin.buttonselectmap[p]!![0]

            val loc = b.location

            p.sendMessage(plugin.prefix + "§a登録しました")

            when (plugin.buttonselectmap[p]!!.size) {

                1 -> {
                    plugin.button1loc[loc] = key
                    plugin.buttonselectmap[p]!!.add(key)
                }

                2 -> {

                    plugin.button2loc[loc] = key

                    plugin.buttonselectmap[p]!!.add(key)
                }

                3 -> {
                    plugin.button3loc[loc] = key
                    plugin.buttonselectmap.remove(p)
                    p.sendMessage(plugin.prefix + "§a次に額縁を")
                    p.sendMessage("§l123\n§l456\n§l789")
                    p.sendMessage(plugin.prefix + "§aとなるように設置してください")
                    plugin.frameselectmap[p] = mutableListOf(key)
                }

            }
        }

        if (plugin.leverselect.containsKey(p)){

            if (b.type != Material.LEVER)return

            val key = plugin.leverselect[p]!!

            plugin.leverloc[b.location] = key
            plugin.leverselect.remove(p)
            p.sendMessage(plugin.prefix + "§a登録しました")
            p.sendMessage(plugin.prefix + "§a次にペカる部分となるブロックをクリックしてください")
            plugin.lightselect[p] = key

        }

    }


    @EventHandler
    fun hanging(e: HangingPlaceEvent){

        if (e.entity.type != EntityType.ITEM_FRAME){
            return
        }

        val p = e.player
        val b = e.entity

        if (plugin.frameselectmap.containsKey(p)){

            val key = plugin.frameselectmap[p]!![0]

            val loc = b.location.block.location

            p.sendMessage(plugin.prefix + "§a登録しました")

            when(plugin.frameselectmap[p]!!.size){

                1->{
                    plugin.frameloc[key] = mutableListOf(loc)
                    plugin.frameselectmap[p]!!.add(key)
                }

                2,3,4,5,6,7,8->{
                    for (i in plugin.frameloc.entries){
                        if (i.key != key) continue
                        i.value.add(loc)
                    }
                    plugin.frameselectmap[p]!!.add(key)
                }

                9->{
                    for (i in plugin.frameloc.entries){
                        if (i.key != key) continue
                        i.value.add(loc)
                    }
                    plugin.frameselectmap.remove(p)
                    p.sendMessage(plugin.prefix + "§a次にレバーを設置してください")
                    plugin.leverselect[p] = key
                }

            }

        }

    }

    @EventHandler
    fun onClick(e: PlayerInteractEvent){

        val p = e.player

        if (e.action == Action.RIGHT_CLICK_AIR || e.action == Action.LEFT_CLICK_AIR)return

        if (plugin.lightselect.containsKey(p)){
            e.isCancelled = true

            val key = plugin.lightselect[p]!!

            val loc = e.clickedBlock.location

            plugin.lightloc[key] = loc
            plugin.lightselect.remove(p)

            p.sendMessage(plugin.prefix + "§a次に看板を設置してください")

            plugin.signselect[p] = key
        }

        val b = e.clickedBlock

        if (plugin.leverloc.containsKey(b.location)){

            val v = Man10VaultAPI("Man10Slot")

            val key = plugin.leverloc[b.location]!!
            val slot = plugin.slotmap[key]!!

            if (slot.spin1 || slot.spin2 || slot.spin3){
                e.isCancelled = true
                p.sendMessage(plugin.prefix + "§c今回っています")
                return
            }

            if (v.getBalance(p.uniqueId) < slot.price){
                p.sendMessage(plugin.prefix + "§cお金が足りません")
                return
            }

            v.transferMoneyPlayerToCountry(p.uniqueId, slot.price, TransactionCategory.GAMBLE, TransactionType.BET, "slot bet")
            v.transferMoneyCountryToPool(slot.pool!!.id, slot.price*slot.stock, TransactionCategory.GAMBLE, TransactionType.BET, "slot bet")

            val sign = plugin.signloc[key]!!.block.state as Sign
            sign.setLine(2, "§6§l" + slot.pool!!.balance.toString())
            sign.update()

            val winlist = plugin.mapSort(slot.wining_chance)

            var win = "0"

            for (i in winlist){

                val ran = Random().nextDouble()

                if (i.key > ran) win = i.value
            }

            if (slot.win != "0"){
                win = slot.win
            }

            if (win != "0"){
                if (slot.wining_con[win]!!){
                    slot.win = win
                }

                if (slot.wining_light[win]!!){
                    val bl = plugin.lightloc[key]!!.world.getBlockAt(plugin.lightloc[key]!!)
                    slot.block = bl.type
                    plugin.lightloc[key]!!.block.type = Material.REDSTONE_BLOCK
                }

                if (slot.wining_lightsound[win] != null){
                    val sound = slot.wining_lightsound[win]!!
                    p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                }

                if (slot.light_particle[win] != null) {
                    val par = slot.light_particle[win]!!
                    plugin.frameloc[key]!![4].world.spawnParticle(par.par!!, plugin.frameloc[key]!![4], par.count!!)
                }
            }

            val sound = slot.spinSound
            p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)

            slot.spin1 = true
            slot.spin2 = true
            slot.spin3 = true

            slot.allstop = false

            slot.p = p

            p.sendMessage(win)

            slot.spincount++

            val spin = SpinProcess(plugin, p, win, slot, key)
            spin.start()

        }

        if (plugin.button1loc.containsKey(b.location)){
            e.isCancelled = true
            val key = plugin.button1loc[b.location]!!
            val slot = plugin.slotmap[key]!!

            if (!slot.spin1)return

            if (slot.p != p){
                p.sendMessage(plugin.prefix + "§c回してる人しか止められません")
                return
            }

            slot.spin1 = false

        }

        if (plugin.button2loc.containsKey(b.location)){
            e.isCancelled = true
            val key = plugin.button2loc[b.location]!!
            val slot = plugin.slotmap[key]!!

            if (!slot.spin2)return

            if (slot.p != p){
                p.sendMessage(plugin.prefix + "§c回してる人しか止められません")
                return
            }

            slot.spin2 = false

        }

        if (plugin.button3loc.containsKey(b.location)){
            e.isCancelled = true
            val key = plugin.button3loc[b.location]!!
            val slot = plugin.slotmap[key]!!

            if (!slot.spin3)return

            if (slot.p != p){
                p.sendMessage(plugin.prefix + "§c回してる人しか止められません")
                return
            }

            slot.spin3 = false

        }

    }

    @EventHandler
    fun onSign(e: SignChangeEvent){

        val p = e.player

        if (plugin.signselect.containsKey(p)){

            val key = plugin.signselect[p]!!

            e.setLine(0, "§3§l======================================")
            e.setLine(3, "§3§l======================================")
            e.setLine(1, "§e§lStock:")
            e.setLine(2, "§6§l0.0")

            plugin.signloc[key] = e.block.location
            plugin.signselect.remove(p)

            p.sendMessage(plugin.prefix + "§aすべて完了しました")

        }

    }

}