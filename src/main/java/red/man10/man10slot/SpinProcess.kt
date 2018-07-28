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

    val sleep: Long = 80
    var step = 0

    val frame = mutableListOf<ItemFrame>()

    var win_item: MutableList<MutableList<ItemStack>>? = null
    var win_prize: Double? = null
    var win_name: String? = null
    var win_odds: Double? = null
    var win_step: Int? = null
    var win_command: MutableList<String>? = null
    var win_sound: Man10Slot.Sound? = null

    var winlist = mutableListOf<ItemStack>()
    var reachlist = mutableListOf<MutableList<ItemStack>>()

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
            win_item = slot.wining_item[win]!!
            win_prize = slot.wining_prize[win]
            win_name = slot.wining_name[win]
            win_odds = slot.wining_stockodds[win]
            win_step = slot.wining_step[win]
            win_command = slot.wining_command[win]
            win_sound = slot.wining_sound[win]
        }

        val list = slot.wining_item

        for (i in list){
            loop@for(j in i.value){
                if (win != "0"){
                    for (l in win_item!!){
                        if (l == j) continue@loop
                    }
                }
                lose.add(j)
            }
        }

        while (true){

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
                    winCheck2(1)
                }
            }else spin2()

            if (!slot.spin3){
                if (!slist.contains(2)) {
                    slist.add(2)
                    sripProcess(2)
                    loseProcess(2)
                    winCheck3(2)
                }
            }else spin3()

            if (!slot.spin1 && !slot.spin2 && !slot.spin3){

                if (win != "0"){

                    if (comCheck2(winlist)) {
                        hit()
                        return
                    } else {
                        p.sendMessage("$prefix§c外れました")
                        return
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
            step++
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
            val r = Random().nextDouble()
            if (r <= par.chance!!) {
                frame[4].location.world.spawnParticle(par.par!!, frame[4].location, par.count!!)
            }
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
                                if (comCheck2(l)) {
                                    spin1()
                                } else break@loop
                            }

                            1 -> {
                                if (comCheck2(l)) {
                                    spin2()
                                } else break@loop
                            }

                            2 -> {
                                if (comCheck2(l)) {
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
                        if (frame[num].item == l[num] && frame[num+3].item == l[num+3] && frame[num+6].item == l[num+6]) {
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

    @Synchronized
    fun comCheck(item: MutableList<ItemStack>, num: Int): Boolean{

        val r1 = arrayOf(reel1[(step + 1) % reel1.size], reel1[step % reel1.size], reel1[(step + reel1.size - 1) % reel1.size])

        val r2 = arrayOf(reel2[(step + 1) % reel2.size], reel2[step % reel2.size], reel2[(step + reel2.size - 1) % reel2.size])

        val r3 = arrayOf(reel3[(step + 1) % reel3.size], reel3[step % reel3.size], reel3[(step + reel3.size - 1) % reel3.size])

        return when (num) {
            0 -> (r1[0] == item[0] && r1[1] == item[3] && r1[2] == item[6])
            1 -> (r2[0] == item[1] && r2[1] == item[4] && r2[2] == item[7])
            2 -> (r3[0] == item[2] && r3[1] == item[5] && r3[2] == item[8])
            else -> false
        }
    }

    @Synchronized
    fun comCheck2(item: MutableList<ItemStack>): Boolean{
        if (!item.contains(ItemStack(Material.AIR))) {
            return (frame[0].item == item[0] && frame[1].item == item[1] && frame[2].item == item[2] && frame[3].item == item[3] && frame[4].item == item[4] && frame[5].item == item[5] && frame[6].item == item[6] && frame[7].item == item[7] && frame[8].item == item[8])
        }else{
            val num = mutableListOf<Int>()
            var num2 = 0
            for (i in 0 until item.size){
                if (item[i] == ItemStack(Material.AIR)) num.add(i)
                else num2 = i
            }

            when(num.size){
                3->{
                    when(num[0]%3){
                        0->{
                            return ((frame[1].item == item[1] && frame[2].item == item[2]) || (frame[4].item == item[4] && frame[2].item == item[2]) || (frame[4].item == item[4] && frame[5].item == item[5]) || (frame[4].item == item[4] && frame[8].item == item[8]) || (frame[7].item == item[7] && frame[8].item == item[8]))
                        }
                        1->{
                            return ((frame[0].item == item[0] && frame[2].item == item[2]) || (frame[3].item == item[3] && frame[2].item == item[2]) || (frame[3].item == item[3] && frame[5].item == item[5]) || (frame[3].item == item[3] && frame[8].item == item[8]) || (frame[6].item == item[6] && frame[8].item == item[8]))
                        }
                        2->{
                            return ((frame[1].item == item[1] && frame[0].item == item[0]) || (frame[4].item == item[4] && frame[0].item == item[0]) || (frame[4].item == item[4] && frame[3].item == item[3]) || (frame[4].item == item[4] && frame[6].item == item[6]) || (frame[7].item == item[7] && frame[6].item == item[6]))
                        }
                    }
                }
                6->{
                    return (frame[num2].item == item[num2] || frame[num2+3].item == item[num2] || frame[num2+6].item == item[num2])
                }
                9->{
                    return true
                }
            }
        }
        return false
    }

    @Synchronized
    fun winCheck(num: Int){

        var count = -1

        val size = slist.size

        var pass = false

        if (win != "0"){

            val item = win_item!!

            if (reachlist.size == 0) {
                for (i in item) {
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        reachlist.add(i)
                        pass = true
                    }
                }
            }else if (winlist.size == 0){
                for (i in reachlist){
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        winlist = i
                        pass = true
                        break
                    }
                }
                if (winlist.size == 0){
                    winlist = reachlist[Random().nextInt(reachlist.size)]
                }
            }else{
                if (winlist[num] == frame[num].item && winlist[num + 3] == frame[num + 3].item && winlist[num + 6] == frame[num + 6].item) {
                    pass = true
                }
            }

            if (reachlist.size == 0){
                val r = Random().nextInt(item.size)
                reachlist.add(item[r])
                winlist = item[r]
            }

            if (!pass) {

                if (reachlist[0][num] != ItemStack(Material.AIR)) {

                    loop@ for (i in 0 until win_step!!) {
                        if (comCheck(winlist, num)) {
                            count = i
                            break@loop
                        }
                        step++
                    }

                    if (count >= 0) {
                        step -= count
                        for (i in 0..count) {
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

    @Synchronized
    fun winCheck2(num: Int){

        var count = -1

        val size = slist.size

        var pass = false

        if (win != "0"){

            val item = win_item!!

            if (reachlist.size == 0) {
                for (i in item) {
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        reachlist.add(i)
                        pass = true
                    }
                }
            }else if (winlist.size == 0){
                for (i in reachlist){
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        winlist = i
                        pass = true
                        break
                    }
                }
                if (winlist.size == 0){
                    winlist = reachlist[Random().nextInt(reachlist.size)]
                }
            }else{
                if (winlist[num] == frame[num].item && winlist[num + 3] == frame[num + 3].item && winlist[num + 6] == frame[num + 6].item) {
                    pass = true
                }
            }

            if (reachlist.size == 0){
                val r = Random().nextInt(item.size)
                reachlist.add(item[r])
                winlist = item[r]
            }

            if (!pass) {

                if (reachlist[0][num] != ItemStack(Material.AIR)) {

                    loop@ for (i in 0 until win_step!!) {
                        if (comCheck(winlist, num)) {
                            count = i
                            break@loop
                        }
                        step++
                    }

                    if (count >= 0) {
                        step -= count
                        for (i in 0..count) {
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

    @Synchronized
    fun winCheck3(num: Int){

        var count = -1

        val size = slist.size

        var pass = false

        if (win != "0"){

            val item = win_item!!

            if (reachlist.size == 0) {
                for (i in item) {
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        reachlist.add(i)
                        pass = true
                    }
                }
            }else if (winlist.size == 0){
                for (i in reachlist){
                    if (i[num] == frame[num].item && i[num + 3] == frame[num + 3].item && i[num + 6] == frame[num + 6].item) {
                        winlist = i
                        pass = true
                        break
                    }
                }
                if (winlist.size == 0){
                    winlist = reachlist[Random().nextInt(reachlist.size)]
                }
            }else{
                if (winlist[num] == frame[num].item && winlist[num + 3] == frame[num + 3].item && winlist[num + 6] == frame[num + 6].item) {
                    pass = true
                }
            }

            if (reachlist.size == 0){
                val r = Random().nextInt(item.size)
                reachlist.add(item[r])
                winlist = item[r]
            }

            if (!pass) {

                if (reachlist[0][num] != ItemStack(Material.AIR)) {

                    loop@ for (i in 0 until win_step!!) {
                        if (comCheck(winlist, num)) {
                            count = i
                            break@loop
                        }
                        step++
                    }


                    if (count >= 0) {
                        step -= count
                        for (i in 0..count) {
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
