package red.man10.man10slot

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import red.man10.man10vaultapiplus.Man10VaultAPI
import red.man10.man10vaultapiplus.enums.TransactionCategory
import red.man10.man10vaultapiplus.enums.TransactionType
import java.util.*

class SpinProcess(val plugin: Man10Slot, val p: Player, val win: String, val slot: Man10Slot.SlotInformation, val key: String): Thread() {

    val reel1 = slot.reel1
    val reel2 = slot.reel2
    val reel3 = slot.reel3

    val sleep: Long = 100
    var step = 0

    val frame = mutableListOf<ItemFrame>()

    var win_item: MutableList<ItemStack>? = null
    var win_prize: Double? = null
    var win_name: String? = null
    var win_odds: Double? = null
    var win_step: Int? = null
    var win_command: MutableList<String>? = null
    var win_sound: Man10Slot.Sound? = null

    var slotnum = -1

    val lose = mutableListOf<MutableList<ItemStack>>()
    val slist = mutableListOf<Int>()

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    @Synchronized
    override fun run() {

        for (loc in plugin.frameloc[key]!!){
            for(i in p.location.world.entities){
                if (i.location.block.location == loc){
                    frame.add(i as ItemFrame)
                }
            }
        }

        if (win != "0"){
            win_item = slot.wining_item[win]
            win_prize = slot.wining_prize[win]
            win_name = slot.wining_name[win]
            win_odds = slot.wining_stockodds[win]
            win_step = slot.wining_step[win]
            win_command = slot.wining_command[win]
            win_sound = slot.wining_sound[win]
            p.sendMessage(win_item!!.toString())
        }

        val list = slot.wining_item

        for (i in list){
            if (win != "0"){
                if (win_item!! == i.value)continue
                if (win_item!!.contains(ItemStack(Material.AIR))){
                    for (j in 0 until i.value.size){
                        if (win_item!![j] == i.value[j])i.value[j].type = Material.AIR
                    }
                }
            }
            lose.add(i.value)
        }

        while (true){
            step++

            if (!slot.spin1){
                if (!slist.contains(0)) {
                    slist.add(0)
                    sripProcess(0)
                    loseProcess(0)
                    winCheck(0)
                }
            }else spin1()

            if (!slot.spin2){
                if (!slist.contains(1)) {
                    slist.add(1)
                    sripProcess(1)
                    loseProcess(1)
                    winCheck(1)
                }
            }else spin2()

            if (!slot.spin3){
                if (!slist.contains(2)) {
                    slist.add(2)
                    sripProcess(2)
                    loseProcess(2)
                    winCheck(2)
                }
            }else spin3()

            if (!slot.spin1 && !slot.spin2 && !slot.spin3){

                if (win != "0"){

                    if (!win_item!!.contains(ItemStack(Material.AIR))) {
                        if (comCheck2(win_item!!)) {
                            hit()
                            return
                        } else {
                            p.sendMessage("$prefix§c外れました")
                            return
                        }
                    }else{
                        var check1 = false
                        var check2 = false
                        var check3 = false
                        if (win_item!![0] != ItemStack(Material.AIR)){
                            if (win_item!![0] == frame[0].item || win_item!![0] == frame[3].item || win_item!![0] == frame[6].item) check1 = true
                        }else check1 = true

                        if (win_item!![1] != ItemStack(Material.AIR)){
                            if (win_item!![1] == frame[1].item || win_item!![1] == frame[4].item || win_item!![1] == frame[7].item) check2 = true
                        }else check2 = true

                        if (win_item!![2] != ItemStack(Material.AIR)){
                            if (win_item!![2] == frame[2].item || win_item!![2] == frame[5].item || win_item!![2] == frame[8].item) check3 = true
                        }else check3 = true

                        if (check1 && check2 && check3){
                            hit()
                            return
                        } else {
                            p.sendMessage("$prefix§c外れました")
                            return
                        }
                    }

                }else{
                    p.sendMessage("$prefix§c外れました")
                    return
                }

            }

            if (step > 450){
                slot.spin1 = false
                slot.spin2 = false
                slot.spin3 = false
            }
            sleep(sleep)
        }

    }

    @Synchronized
    fun spin1(){
        frame[0].item = reel1[(step+1)%reel1.size]
        frame[3].item = reel1[step%reel1.size]
        frame[6].item = reel1[(step+reel1.size-1)%reel1.size]
    }

    @Synchronized
    fun spin2(){
        frame[1].item = reel2[(step+1)%reel2.size]
        frame[4].item = reel2[step%reel2.size]
        frame[7].item = reel2[(step+reel2.size-1)%reel2.size]
    }

    @Synchronized
    fun spin3(){
        frame[2].item = reel3[(step+1)%reel3.size]
        frame[5].item = reel3[step%reel3.size]
        frame[8].item = reel3[(step+reel3.size-1)%reel3.size]
    }

    @Synchronized
    fun hit(){

        val v = Man10VaultAPI("Man10Slot")

        p.sendMessage(prefix + "§e§lおめでとうございます！${win_name!!}§e§lです！")
        slot.win = "0"
        val totalprize = win_prize!! + win_odds!!*slot.pool!!.balance

        Bukkit.broadcastMessage(prefix + "§e${p.displayName}§aさんは§l${slot.slot_name}§aに勝利し§6§l$${totalprize}§a" +
                "手に入れた！！")

        if (win_command != null) {
            plugin.runCommand(win_command!!, win_name!!, p, slot.slot_name, totalprize)
        }

        v.transferMoneyCountryToPlayer(p.uniqueId, totalprize, TransactionCategory.GAMBLE, TransactionType.WIN, "slot win")
        slot.pool!!.sendRemainderToCountry("slot tax")

        val sign = plugin.signloc[key]!!.block.state as Sign
        sign.setLine(2, "§6§l" + slot.pool!!.balance.toString())
        sign.update()

        val sound = win_sound!!
        p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)
        if (slot.wining_light[win]!!){
            plugin.blockPlace(slot, key)
        }

        val list = slot.chancewin[win]
        if (list != null){
            val r = Random().nextInt(list.size)
            slot.win = list[r]
        }

        if (slot.win_particle[win] != null) {
            val par = slot.win_particle[win]!!
            frame[4].location.world.spawnParticle(par.par!!, frame[4].location, par.count!!)
        }

        if (slot.wincount.containsKey(win)){
            slot.wincount[win] = slot.wincount[win]!! + 1
        }else slot.wincount[win] = 1

    }

    @Synchronized
    fun loseProcess(num: Int){

        if (slist.size == 3) {

            for (l in lose) {

                if (!l.contains(ItemStack(Material.AIR))) {
                    loop@ for (i in 0 until 20) {
                        when (num) {

                            0 -> {
                                if (comCheck(l)) {
                                    spin1()
                                } else break@loop
                            }

                            1 -> {
                                if (comCheck(l)) {
                                    spin2()
                                } else break@loop
                            }

                            2 -> {
                                if (comCheck(l)) {
                                    spin3()
                                } else break@loop
                            }

                        }
                        step++
                        sleep(sleep)
                    }
                }
            }
        }

    }

    @Synchronized
    fun sripProcess(num: Int){
       val size = slist.size

        for (l in lose){
            if (l.contains(ItemStack(Material.AIR))){
                if (l[num] != ItemStack(Material.AIR)){
                    for (i in 0 until 20) {
                        if (frame[num].item == l[num] || frame[num + 3].item == l[num] || frame[num + 6].item == l[num]) {
                            when(size) {
                                1 -> {
                                    spin1()
                                    spin2()
                                    spin3()
                                }
                                2 -> {
                                    when (slist[0]) {
                                        0 -> {
                                            spin2()
                                            spin3()
                                        }
                                        1 -> {
                                            spin1()
                                            spin3()
                                        }
                                        2 -> {
                                            spin1()
                                            spin2()
                                        }
                                    }
                                }
                                3 -> {
                                    when (num) {
                                        0 -> spin1()
                                        1 -> spin2()
                                        3 -> spin3()
                                    }
                                }
                            }
                        }else break
                        step++
                        sleep(sleep)
                    }
                }
            }
        }

    }

    fun comCheck(item: MutableList<ItemStack>): Boolean{
        return (frame[0].item == item[0] && frame[1].item == item[1] && frame[2].item == item[2]) || (frame[0].item == item[0] && frame[4].item == item[1] && frame[8].item == item[2]) ||
                    (frame[3].item == item[0] && frame[4].item == item[1] && frame[5].item == item[2]) || (frame[6].item == item[0] && frame[7].item == item[1] && frame[8].item == item[2]) ||
                    (frame[6].item == item[0] && frame[4].item == item[1] && frame[2].item == item[2])
    }

    fun comCheck2(item: MutableList<ItemStack>): Boolean{
        return (frame[0].item == item[0] && frame[1].item == item[1] && frame[2].item == item[2]) || (frame[0].item == item[0] && frame[4].item == item[1] && frame[8].item == item[2]) ||
                (frame[3].item == item[0] && frame[4].item == item[1] && frame[5].item == item[2]) ||
                (frame[6].item == item[0] && frame[7].item == item[1] && frame[8].item == item[2]) || (frame[6].item == item[0] && frame[4].item == item[1] && frame[2].item == item[2])
    }

    fun reachCheck(item: MutableList<ItemStack>, num: Int, num2: Int): Boolean{

        val r1_0 = reel1[(step+1)%reel1.size]
        val r1_1 = reel1[step%reel1.size]
        val r1_2 = reel1[(step+reel1.size-1)%reel1.size]

        val r2_0 = reel2[(step+1)%reel2.size]
        val r2_1 = reel2[step%reel2.size]
        val r2_2 = reel2[(step+reel2.size-1)%reel2.size]

        val r3_0 = reel3[(step+1)%reel3.size]
        val r3_1 = reel3[step%reel3.size]
        val r3_2 = reel3[(step+reel3.size-1)%reel3.size]

        when(num2){

            0->{
                return if (num == 1){
                    (frame[0].item == item[0] && (r2_0 == item[1] || r2_1 == item[1])) || (frame[3].item == item[0] && r2_1 == item[1]) || (frame[6].item == item[0] && (r2_2 == item[1] || r2_1 == item[1]))
                }else{
                    (frame[0].item == item[0] && (r3_0 == item[2] || r3_2 == item[2])) || (frame[3].item == item[0] && r3_1 == item[2]) || (frame[6].item == item[0] && (r3_2 == item[2] || r3_0 == item[2]))
                }
            }
            1->{
                return if (num == 0){
                    (frame[1].item == item[1] && r1_0 == item[0]) || (frame[4].item == item[1] && (r1_0 == item[0] || r1_1 == item[0] || r1_2 == item[0])) || (frame[7].item == item[1] && r1_2 == item[0])
                }else{
                    (frame[1].item == item[1] && r3_0 == item[2]) || (frame[4].item == item[1] && (r3_0 == item[2] || r3_1 == item[2] || r3_2 == item[2])) || (frame[7].item == item[1] && r3_2 == item[2])
                }
            }
            2->{
                return if (num == 1){
                    (frame[2].item == item[2] && (r2_0 == item[1] || r2_1 == item[1])) || (frame[5].item == item[2] && r2_1 == item[1]) || (frame[8].item == item[2] && (r2_2 == item[1] || r2_1 == item[1]))
                }else{
                    (frame[2].item == item[2] && (r1_0 == item[0] || r1_2 == item[0])) || (frame[5].item == item[2] && r1_1 == item[0]) || (frame[8].item == item[2] && (r1_2 == item[0] || r1_1 == item[0]))
                }
            }
        }
        return false
    }

    fun winCheck(num: Int){

        var count = -1

        var pass = false

        val size = slist.size

        p.sendMessage(slist.toString())

        if (win != "0"){

            val item = win_item!!

            if (!win_item!!.contains(ItemStack(Material.AIR))) {

                when (item[num]) {

                    frame[num].item -> {
                        if (slotnum == 0 || slotnum == -1) {
                            slotnum = 0
                            pass = true
                        }
                    }

                    frame[num + 3].item -> {
                        if (slotnum == 1 || slotnum == -1) {
                            slotnum = 1
                            pass = true
                        }
                    }

                    frame[num + 6].item -> {
                        if (slotnum == 2 || slotnum == -1) {
                            slotnum = 2
                            pass = true
                        }
                    }

                    else -> {
                        if (slotnum == -1) {
                            val r = Random().nextInt(3)
                            slotnum = r
                        }
                    }

                }

                p.sendMessage("slotnum$slotnum")

                if (!pass) {
                    if (slotnum != -1) {

                        loop@ for (i in 0 until win_step!!) {

                            val r1 = arrayOf(reel1[(step + 1) % reel1.size], reel1[step % reel1.size], reel1[(step + reel1.size - 1) % reel1.size])

                            val r2 = arrayOf(reel2[(step + 1) % reel2.size], reel2[step % reel2.size], reel2[(step + reel2.size - 1) % reel2.size])

                            val r3 = arrayOf(reel3[(step + 1) % reel3.size], reel3[step % reel3.size], reel3[(step + reel3.size - 1) % reel3.size])

                            when (num) {
                                0 -> {
                                    if (r1[slotnum] == item[num]) {
                                        count = i
                                        break@loop
                                    }
                                }
                                1 -> {
                                    if (r2[slotnum] == item[num]) {
                                        count = i
                                        break@loop
                                    }
                                }
                                2 -> {
                                    if (r3[slotnum] == item[num]) {
                                        count = i
                                        break@loop
                                    }
                                }
                            }
                            step++
                        }
                    }

                    p.sendMessage("count$count")

                    if (count >= 0) {
                        step -= count
                        for (i in 0..count) {
                            p.sendMessage("a")
                            when (size) {
                                1 -> {
                                    spin1()
                                    spin2()
                                    spin3()
                                }
                                2 -> {
                                    when (slist[0]) {
                                        0 -> {
                                            spin2()
                                            spin3()
                                        }
                                        1 -> {
                                            spin1()
                                            spin3()
                                        }
                                        2 -> {
                                            spin1()
                                            spin2()
                                        }
                                    }
                                }
                                3 -> {
                                    when (num) {
                                        0 -> spin1()
                                        1 -> spin2()
                                        2 -> spin3()
                                    }
                                }
                            }
                            step++
                            sleep(sleep)
                        }
                    }
                }
            }
        }
    }

}
