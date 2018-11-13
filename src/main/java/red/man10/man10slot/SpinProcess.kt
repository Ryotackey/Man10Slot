package red.man10.man10slot

import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.ItemFrame
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import red.man10.man10vaultapiplus.Man10VaultAPI
import red.man10.man10vaultapiplus.enums.TransactionCategory
import red.man10.man10vaultapiplus.enums.TransactionType
import java.util.*
import kotlin.collections.HashMap

class SpinProcess(val plugin: Man10Slot, val p: Player, val win: String, val slot: Man10Slot.SlotInformation, val key: String): Thread() {

    val reel1 = slot.reel1
    val reel2 = slot.reel2
    val reel3 = slot.reel3

    val sleep: Long = 80
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
    val slist = mutableListOf<Int>()
    val wildlist = mutableListOf<String>()
    val winwild = HashMap<Int,ItemStack>()

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
            val list1 = i.value
            p.sendMessage(list1.toString())
            if (i.key != win){
                if (list1.contains(ItemStack(Material.AIR))) {
                    p.sendMessage("a")
                    for (m in 0 until list1.size){

                        if (list1[m] != ItemStack(Material.AIR)){
                            wildlist.add("(${m})+${list1[m].type}+${list1[m].durability}")
                        }

                    }
                }else{
                    lose.add(list1)
                }
            }
        }

        if (win != "0" && win_item!!.contains(ItemStack(Material.AIR))){

            for (m in 0 until win_item!!.size){

                if (win_item!![m] != ItemStack(Material.AIR)) {
                    winwild[m] = win_item!![m]
                }

            }

        }

        p.sendMessage(lose.toString())
        p.sendMessage(wildlist.toString())
        p.sendMessage(winwild.toString())

        while (true){

            step++

            if (!slot.spin1){
                if (!slist.contains(0)) {
                    slist.add(0)
                    winCheck(0)
                    sripProcess(0)
                }
            }else spin1()

            if (!slot.spin2){
                if (!slist.contains(1)) {
                    slist.add(1)
                    winCheck(1)
                    sripProcess(1)
                }
            }else spin2()

            if (!slot.spin3){
                if (!slist.contains(2)) {
                    slist.add(2)
                    winCheck(2)
                    sripProcess(2)
                }
            }else spin3()

            if (!slot.spin1 && !slot.spin2 && !slot.spin3){

                if (slot.win != "0" && slot.wining_light[win]!!){
                    plugin.blockPlace(slot, key)
                }

                if (slot.stopeffect != null){
                    if (slot.stopeffect!!.command != null){
                        plugin.runCommand(slot.stopeffect!!.command!!, "", p, slot.slot_name, 0.0)
                    }
                    if (slot.stopeffect!!.particle != null){
                        val par = slot.stopeffect!!.particle!!
                        val r = Random().nextDouble()
                        if (r <= par.chance!!) {
                            frame[4].location.world.spawnParticle(par.par!!, frame[4].location, par.count!!)
                        }
                    }
                    if (slot.stopeffect!!.sound != null){
                        val sound = slot.stopeffect!!.sound!!
                        p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                    }
                }

                if (slot.countgame > 0){
                    slot.countgame--
                }

                if (slot.countgame == 0){
                    slot.chancenum = 0
                    if (slot.changeeffect != null){
                        if (slot.changeeffect!!.command != null){
                            plugin.runCommand(slot.changeeffect!!.command!!, "", p, slot.slot_name, 0.0)
                        }
                        if (slot.changeeffect!!.particle != null){
                            val par = slot.changeeffect!!.particle!!
                            val r = Random().nextDouble()
                            if (r <= par.chance!!) {
                                frame[4].location.world.spawnParticle(par.par!!, frame[4].location, par.count!!)
                            }
                        }
                        if (slot.changeeffect!!.sound != null){
                            val sound = slot.changeeffect!!.sound!!
                            p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)
                        }
                    }
                }

                if (win != "0"){

                    if (!win_item!!.contains(ItemStack(Material.AIR))) {
                        if (comCheck()) {
                            hit()
                            return
                        } else {
                            p.sendMessage("$prefix§c外れました")
                            return
                        }
                    }else{

                        if (winwild.size == 1) {

                            for (i in winwild) {

                                if (frame[i.key].item == i.value || frame[i.key+3].item == i.value || frame[i.key+6].item == i.value) {

                                } else {
                                    p.sendMessage("$prefix§c外れました")
                                    return
                                }

                            }
                            hit()
                            return
                        }else if (winwild.size == 2){

                            if (winwild.contains(0) && winwild.contains(1)){

                                if ((frame[0].item == winwild[0] && frame[1].item == winwild[1]) || (frame[0].item == winwild[0] && frame[4].item == winwild[1]) || (frame[3].item == winwild[0] && frame[4].item == winwild[1]) || (frame[6].item == winwild[0] && frame[4].item == winwild[1]) || (frame[6].item == winwild[0] && frame[7].item == winwild[1])){

                                } else {
                                    p.sendMessage("$prefix§c外れました")
                                    return
                                }

                            }else if (winwild.contains(0) && winwild.contains(2)){

                                if ((frame[0].item == winwild[0] && frame[2].item == winwild[2]) || (frame[0].item == winwild[0] && frame[8].item == winwild[2]) || (frame[3].item == winwild[0] && frame[5].item == winwild[2]) || (frame[6].item == winwild[0] && frame[2].item == winwild[2]) || (frame[6].item == winwild[0] && frame[8].item == winwild[2])){

                                } else {
                                    p.sendMessage("$prefix§c外れました")
                                    return
                                }

                            }else if (winwild.contains(1) && winwild.contains(2)){

                                if ((frame[2].item == winwild[2] && frame[1].item == winwild[1]) || (frame[2].item == winwild[2] && frame[4].item == winwild[1]) || (frame[5].item == winwild[2] && frame[4].item == winwild[1]) || (frame[8].item == winwild[2] && frame[4].item == winwild[1]) || (frame[8].item == winwild[2] && frame[7].item == winwild[1])){

                                } else {
                                    p.sendMessage("$prefix§c外れました")
                                    return
                                }

                            }

                            hit()
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

        if (win_command != null) {
            plugin.runCommand(win_command!!, win_name!!, p, slot.slot_name, totalprize)
        }

        if (slot.chancechange[win] != null){
            slot.chancenum = slot.chancechange[win]!!
        }

        if (slot.changegame[win] != -1){
            slot.countgame = slot.changegame[win]!!
        }

        v.transferMoneyCountryToPlayer(p.uniqueId, totalprize, TransactionCategory.GAMBLE, TransactionType.WIN, "slot win")
        slot.pool!!.sendRemainderToCountry("slot tax")

        val sign = plugin.signloc[key]!!.block.state as Sign
        sign.setLine(2, "§6§l" + slot.pool!!.balance.toString())
        sign.update()

        val sound = win_sound!!
        p.location.world.playSound(p.location, sound.sound, sound.volume, sound.pitch)

        val list = slot.chancewin[win]
        if (list != null){
            val r = Random().nextInt(list.size)
            slot.win = list[r]
        }

        if (slot.wining_light[win]!! && slot.win == "0"){
            plugin.blockPlace(slot, key)
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
    fun loseCheck(num: Int): Boolean{

        if (slist.size == 3) {

                for (i in lose){

                    if (i.contains(ItemStack(Material.AIR))) {
                        continue
                    }

                    return (frame[0].item == i[0] && frame[1].item == i[1] && frame[2].item == i[2]) || (frame[0].item == i[0] && frame[4].item == i[1] && frame[8].item == i[2]) || (frame[3].item == i[0] && frame[4].item == i[1] && frame[5].item == i[2])
                            || (frame[6].item == i[0] && frame[4].item == i[1] && frame[2].item == i[2]) || (frame[6].item == i[0] && frame[7].item == i[1] && frame[8].item == i[2])

                }

        }

        return false

    }

    @Synchronized
    fun wildCeck(num: Int): Boolean{

       for (i in wildlist){

           if (!i.contains("(${num})")) {
                continue
           }

           val str = i.split("+")

           val item = `ItemStack+`(Material.getMaterial(str[1]), str[2].toShort()).build()

           if (frame[num].item == item || frame[num+3].item == item || frame[num+6].item == item){
               return true
           }

       }
        return false
    }

    @Synchronized
    fun sripProcess(num: Int){

        val size = slist.size

        loop@while (true){

            if (wildCeck(num) || loseCheck(num)){

                step++

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

                sleep(sleep)

            }else{
                break@loop
            }

        }

    }

    @Synchronized
    fun reachCheck(num: Int): Boolean{

        when(slist[0]){

            0->{

                when(num){

                    1->{

                        if ((frame[0].item == win_item!![0] && (frame[1].item == win_item!![1] || frame[4].item == win_item!![2])) || (frame[3].item == win_item!![0] && frame[4].item == win_item!![1]) || (frame[6].item == win_item!![0] && (frame[7].item == win_item!![1] || frame[4].item == win_item!![2]))) return true

                    }

                    2->{

                        if ((frame[0].item == win_item!![0] && (frame[2].item == win_item!![2] || frame[8].item == win_item!![2])) || (frame[3].item == win_item!![0] && frame[5].item == win_item!![2]) || (frame[6].item == win_item!![0] && (frame[8].item == win_item!![2] || frame[2].item == win_item!![2]))) return true

                    }

                }

            }

            1->{

                when(num){

                    0->{

                        if ((frame[1].item == win_item!![1] && frame[0].item == win_item!![0]) || (frame[4].item == win_item!![1] && (frame[0].item == win_item!![0] || frame[3].item == win_item!![0] || frame[6].item == win_item!![0])) || (frame[7].item == win_item!![1] && frame[6].item == win_item!![0])) return true

                    }

                    2->{

                        if ((frame[1].item == win_item!![1] && frame[2].item == win_item!![2]) || (frame[4].item == win_item!![1] && (frame[2].item == win_item!![2] || frame[5].item == win_item!![2] || frame[8].item == win_item!![2])) || (frame[7].item == win_item!![1] && frame[8].item == win_item!![2])) return true

                    }

                }

            }

            2->{

                when(num){

                    1->{

                        if ((frame[2].item == win_item!![2] && (frame[1].item == win_item!![1] || frame[4].item == win_item!![2])) || (frame[5].item == win_item!![2] && frame[4].item == win_item!![1]) || (frame[8].item == win_item!![2] && (frame[7].item == win_item!![1] || frame[4].item == win_item!![2]))) return true

                    }

                    0->{

                        if ((frame[2].item == win_item!![2] && (frame[0].item == win_item!![0] || frame[6].item == win_item!![0])) || (frame[5].item == win_item!![2] && frame[3].item == win_item!![0]) || (frame[8].item == win_item!![2] && (frame[0].item == win_item!![0] || frame[6].item == win_item!![0]))) return true

                    }

                }

            }

        }

        return false

    }

    @Synchronized
    fun comCheck(): Boolean{

        val item = win_item!!

        return ((frame[0].item == item[0] && frame[1].item == item[1] && frame[2].item == item[2]) || (frame[0].item == item[0] && frame[4].item == item[1] && frame[8].item == item[2]) || (frame[3].item == item[0] && frame[4].item == item[1] && frame[5].item == item[2])
                || (frame[6].item == item[0] && frame[4].item == item[1] && frame[2].item == item[2]) || (frame[6].item == item[0] && frame[7].item == item[1] && frame[8].item == item[2]))

    }

    @Synchronized
    fun winCheck(num: Int){

        val size = slist.size

        if (win != "0"){

            val item = win_item!!

            if (item.contains(ItemStack(Material.AIR))){

                if (winwild.contains(num)){

                    val winitem = winwild[num]!!

                    if (winwild.size == 1) {

                        loop@for (i in 0 until win_step!!) {

                            if (frame[num].item != winitem && frame[num + 3].item != winitem && frame[num + 6].item != winitem) {

                                step++

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

                            }else break@loop

                            sleep(sleep)

                        }
                        
                    }else if (winwild.size == 2){

                        if (winwild.contains(0) && winwild.contains(1)){

                            if ((frame[0].item == winwild[0] && frame[1].item == winwild[1]) || (frame[0].item == winwild[0] && frame[4].item == winwild[1]) || (frame[3].item == winwild[0] && frame[4].item == winwild[1]) || (frame[6].item == winwild[0] && frame[4].item == winwild[1]) || (frame[6].item == winwild[0] && frame[7].item == winwild[1])){

                            } else {
                                p.sendMessage("$prefix§c外れました")
                                return
                            }

                        }else if (winwild.contains(0) && winwild.contains(2)){

                            if ((frame[0].item == winwild[0] && frame[2].item == winwild[2]) || (frame[0].item == winwild[0] && frame[8].item == winwild[2]) || (frame[3].item == winwild[0] && frame[5].item == winwild[2]) || (frame[6].item == winwild[0] && frame[2].item == winwild[2]) || (frame[6].item == winwild[0] && frame[8].item == winwild[2])){

                            } else {
                                p.sendMessage("$prefix§c外れました")
                                return
                            }

                        }else if (winwild.contains(1) && winwild.contains(2)){

                            if ((frame[2].item == winwild[2] && frame[1].item == winwild[1]) || (frame[2].item == winwild[2] && frame[4].item == winwild[1]) || (frame[5].item == winwild[2] && frame[4].item == winwild[1]) || (frame[8].item == winwild[2] && frame[4].item == winwild[1]) || (frame[8].item == winwild[2] && frame[7].item == winwild[1])){

                            } else {
                                p.sendMessage("$prefix§c外れました")
                                return
                            }

                        }

                        hit()
                        return

                    }

                }

            }else {

                when(size){

                    1->{

                        loop@for (i in 0 until win_step!!){

                            if (frame[num].item != item[num] || frame[num+3].item != item[num] || frame[num+6].item != item[num]){
                                step++
                                spin1()
                                spin2()
                                spin3()
                            }else break@loop

                            sleep(sleep)

                        }

                    }

                    2->{

                        when(slist[0]){

                            0->{

                                loop@for (i in 0 until win_step!!) {

                                    if (!reachCheck(num)) {
                                        step++

                                        spin2()
                                        spin3()

                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                            1->{

                                loop@for (i in 0 until win_step!!) {

                                    if (!reachCheck(num)) {
                                        step++

                                        spin1()
                                        spin3()

                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                            2->{

                                loop@for (i in 0 until win_step!!) {

                                    if (!reachCheck(num)) {
                                        step++

                                        spin1()
                                        spin2()

                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                        }

                    }

                    3->{

                        when(num){

                            0->{

                                loop@for (i in 0 until win_step!!) {

                                    if(!comCheck()){
                                        step++
                                        spin1()
                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                            1->{

                                loop@for (i in 0 until win_step!!) {

                                    if(!comCheck()){
                                        step++
                                        spin2()
                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                            2->{

                                loop@for (i in 0 until win_step!!) {

                                    if(!comCheck()){
                                        step++
                                        spin3()
                                    }else break@loop

                                    sleep(sleep)

                                }

                            }

                        }

                    }

                }

            }
        }
    }

}
