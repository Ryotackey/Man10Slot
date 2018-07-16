package red.man10.man10slot

import org.bukkit.Material
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

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

    val lose = mutableListOf<MutableList<ItemStack>>()

    val stoplist = mutableListOf<Int>()

    val prefix = "§l[§d§lMa§f§ln§a§l10§e§lSlot§f§l]"

    @Synchronized
    override fun run() {

        for (loc in plugin.frameloc[key]!!){
            for(i in p.location.world.getNearbyEntities(loc, loc.x, loc.y, loc.z)){
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

        p.sendMessage(lose.toString())

        while (true){

            if (!slot.spin1){
                if (!stoplist.contains(0)) {
                    winProcess(0)
                    sripProcess(0)
                    loseProcess(0)
                    stoplist.add(0)
                }
            }else spin1()

            if (!slot.spin2){
                if (!stoplist.contains(1)) {
                    winProcess(1)
                    sripProcess(1)
                    loseProcess(1)
                    stoplist.add(1)
                }
            }else spin2()

            if (!slot.spin3){
                if (!stoplist.contains(2)) {
                    winProcess(2)
                    sripProcess(2)
                    loseProcess(2)
                    stoplist.add(2)
                }
            }else spin3()

            if (!slot.spin1 && !slot.spin2 && !slot.spin3){

                if (win != "0"){

                    if (!win_item!!.contains(ItemStack(Material.AIR))) {
                        if (comCheck(win_item!!)) {
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
        p.sendMessage(prefix + "§e§lおめでとうございます！${win_name!!}§e§lです！")
        slot.win = "0"
    }

    @Synchronized
    fun loseProcess(num: Int){

        if (stoplist.size == 2) {

            for (l in lose) {

                if (!l.contains(ItemStack(Material.AIR))) {
                    loop@ for (i in 0 until 20) {
                        when (num) {

                            0 -> {
                                if (comCheck(l)) {
                                    spin2()
                                    spin3()
                                } else break@loop
                            }

                            1 -> {
                                if (comCheck(l)) {
                                    spin1()
                                    spin3()
                                } else break@loop
                            }

                            2 -> {
                                if (comCheck(l)) {
                                    spin1()
                                    spin2()
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
    fun winProcess(num: Int){

        if (win != "0"){
            val size = stoplist.size

            if (!win_item!!.contains(ItemStack(Material.AIR))) {
                loop@ for (i in 0 until win_step!!) {

                    when (size) {
                        0 -> {
                            if (frame[num].item == win_item!![num] || frame[num + 3].item == win_item!![num] || frame[num + 3].item == win_item!![num]) {
                                break@loop
                            } else {
                                spin1()
                                spin2()
                                spin3()
                            }
                        }
                        1 -> {
                            when (stoplist[0]) {
                                0 -> {
                                    if (reachCheck(win_item!!, num, 0)) {
                                        break@loop
                                    } else {
                                        spin2()
                                        spin3()
                                    }
                                }
                                1 -> {
                                    if (reachCheck(win_item!!, num, stoplist[0])) {
                                        break@loop
                                    } else {
                                        spin1()
                                        spin3()
                                    }
                                }
                                2 -> {
                                    if (reachCheck(win_item!!, num, stoplist[0])) {
                                        break@loop
                                    } else {
                                        spin1()
                                        spin2()
                                    }
                                }
                            }
                        }
                        2 -> {
                            when(num){
                                0->{
                                    if (comCheck(win_item!!)){
                                        break@loop
                                    }else spin1()
                                }
                                1->{
                                    if (comCheck(win_item!!)){
                                        break@loop
                                    }else spin2()
                                }
                                2->{
                                    if (comCheck(win_item!!)){
                                        break@loop
                                    }else spin3()
                                }
                            }
                        }
                    }
                    step++
                    sleep(sleep)
                }
            }else{
                loop@ for (i in 0 until win_step!!) {
                    if (win_item!![num] != ItemStack(Material.AIR)) {
                        if (frame[num].item == win_item!![num] || frame[num + 3].item == win_item!![num] || frame[num + 6].item == win_item!![num]) {
                            break@loop
                        }else{
                            when(size){

                                0->{
                                    spin1()
                                    spin2()
                                    spin3()
                                }
                                1->{
                                    when(stoplist[0]){
                                        0->{
                                            spin2()
                                            spin3()
                                        }
                                        1->{
                                            spin1()
                                            spin3()
                                        }
                                        2->{
                                            spin1()
                                            spin3()
                                        }
                                    }
                                }
                                2->{
                                    when(num){
                                        0->spin1()
                                        1->spin2()
                                        3->spin3()
                                    }
                                }

                            }
                        }
                        step++
                        sleep(sleep)
                    }else break@loop
                }
            }

        }

    }

    @Synchronized
    fun sripProcess(num: Int){

        for (l in lose){
            if (l.contains(ItemStack(Material.AIR))){
                if (l[num] != ItemStack(Material.AIR)){
                    val size = stoplist.size
                    for (i in 0 until 20) {
                        if (frame[num].item == l[num] || frame[num + 3].item == l[num] || frame[num + 6].item == l[num]) {
                            when(size) {
                                0 -> {
                                    spin1()
                                    spin2()
                                    spin3()
                                }
                                1 -> {
                                    when (stoplist[0]) {
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
                                            spin3()
                                        }
                                    }
                                }
                                2 -> {
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
        return (frame[0].item == item[0] && (frame[1].item == item[1] && frame[2].item == item[2]) || (frame[4].item == item[1] && frame[8].item == item[2])) ||
                (frame[3].item == item[0] && frame[4].item == item[1] && frame[5].item == item[2]) ||
                (frame[6].item == item[0] && (frame[7].item == item[1] && frame[8].item == item[2]) || (frame[4].item == item[1] && frame[2].item == item[2]))
    }

    fun reachCheck(item: MutableList<ItemStack>, num: Int, num2: Int): Boolean{

        when(num2){

            0->{
                return if (num == 1){
                    (frame[0].item == item[0] && (frame[1].item == item[1] || frame[4].item == item[1])) || (frame[3].item == item[0] && frame[4].item == item[1]) || (frame[6].item == item[0] && (frame[7].item == item[1] || frame[4].item == item[1]))
                }else{
                    (frame[0].item == item[0] && (frame[2].item == item[2] || frame[8].item == item[2])) || (frame[3].item == item[0] && frame[5].item == item[2]) || (frame[6].item == item[0] && (frame[8].item == item[2] || frame[2].item == item[2]))
                }
            }
            1->{
                return if (num == 0){
                    (frame[1].item == item[1] && frame[0].item == item[0]) || (frame[4].item == item[1] && (frame[0].item == item[0] || frame[3].item == item[0] || frame[6].item == item[0])) || (frame[7].item == item[1] && frame[6].item == item[0])
                }else{
                    (frame[1].item == item[1] && frame[2].item == item[2]) || (frame[4].item == item[1] && (frame[2].item == item[2] || frame[5].item == item[2] || frame[8].item == item[2])) || (frame[7].item == item[1] && frame[8].item == item[2])
                }
            }
            2->{
                return if (num == 1){
                    (frame[2].item == item[2] && (frame[1].item == item[1] || frame[4].item == item[1])) || (frame[5].item == item[2] && frame[4].item == item[1]) || (frame[8].item == item[2] && (frame[7].item == item[1] || frame[4].item == item[1]))
                }else{
                    (frame[2].item == item[2] && (frame[0].item == item[0] || frame[6].item == item[0])) || (frame[5].item == item[2] && frame[3].item == item[0]) || (frame[8].item == item[2] && (frame[6].item == item[0] || frame[3].item == item[0]))
                }
            }
        }
        return false
    }

}