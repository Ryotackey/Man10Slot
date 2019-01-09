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
            Bukkit.broadcastMessage(win)
        }

        val list = slot.wining_item

        for (i in list){
            val list1 = i.value
            if (i.key != win){
                if (list1.contains(ItemStack(Material.AIR))) {
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
                    wildlist.remove("(${m})+${win_item!![m].type}+${win_item!![m].durability}")
                    winwild[m] = win_item!![m]
                }

            }

        }

        while (true){

            step++

            if (!slot.spin1){
                if (!slist.contains(0)) {
                    slist.add(0)
                    sripProcess(0)
                    winCheck(0)
                }
            }else spin1()

            if (!slot.spin2){
                if (!slist.contains(1)) {
                    slist.add(1)
                    sripProcess2(1)
                    winCheck1(1)
                }
            }else spin2()

            if (!slot.spin3){
                if (!slist.contains(2)) {
                    slist.add(2)
                    sripProcess3(2)
                    winCheck2(2)
                }
            }else spin3()

            if (!slot.spin1 && !slot.spin2 && !slot.spin3){

                if (win != "0" && slot.wining_light[win]!! && !slot.wining_con[win]!!){
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
                            if (slot.wining_con[win]!!){

                            }
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

        if (slot.wining_light[win]!! && slot.win == "0" ){
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

    fun sripProcess(num: Int){

        val size = slist.size

        loop@ while (true) {

            var flag = false

            var flag2 = false


            if (slist.size == 3) {
                for (i in lose) {

                    if (i.contains(ItemStack(Material.AIR))) {
                        continue
                    }

                    if ((frame[0].item == i[0] && frame[1].item == i[1] && frame[2].item == i[2]) || (frame[0].item == i[0] && frame[4].item == i[1] && frame[8].item == i[2]) || (frame[3].item == i[0] && frame[4].item == i[1] && frame[5].item == i[2]) || (frame[6].item == i[0] && frame[4].item == i[1] && frame[2].item == i[2]) || (frame[6].item == i[0] && frame[7].item == i[1] && frame[8].item == i[2])) {
                        flag = true
                    }

                }
            }

            for (i in wildlist){

                if (!i.contains("(${num})")) {
                    continue
                }

                val str = i.split("+")

                val item = `ItemStack+`(Material.getMaterial(str[1]), str[2].toShort()).build()

                if (frame[num].item == item || frame[num+3].item == item || frame[num+6].item == item){
                    flag2 = true
                }

            }

            if (flag || flag2) {

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

            } else {
                break@loop
            }

        }
    }

    fun sripProcess2(num: Int){

        if (slist.size == 3) {

            val size = slist.size

            loop@ while (true) {

                var flag = false

                var flag2 = false

                for (i in lose) {

                    if (i.contains(ItemStack(Material.AIR))) {
                        continue
                    }

                    if ((frame[0].item == i[0] && frame[1].item == i[1] && frame[2].item == i[2]) || (frame[0].item == i[0] && frame[4].item == i[1] && frame[8].item == i[2]) || (frame[3].item == i[0] && frame[4].item == i[1] && frame[5].item == i[2]) || (frame[6].item == i[0] && frame[4].item == i[1] && frame[2].item == i[2]) || (frame[6].item == i[0] && frame[7].item == i[1] && frame[8].item == i[2])) {
                        flag = true
                    }

                }

                for (i in wildlist){

                    if (!i.contains("(${num})")) {
                        continue
                    }

                    val str = i.split("+")

                    val item = `ItemStack+`(Material.getMaterial(str[1]), str[2].toShort()).build()

                    if (frame[num].item == item || frame[num+3].item == item || frame[num+6].item == item){
                        flag2 = true
                    }

                }

                if (flag || flag2) {

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

                } else {
                    break@loop
                }

            }
        }

    }

    fun sripProcess3(num: Int){

        if (slist.size == 3) {

            val size = slist.size

            loop@ while (true) {

                var flag = false

                var flag2 = false

                for (i in lose) {

                    if (i.contains(ItemStack(Material.AIR))) {
                        continue
                    }

                    if ((frame[0].item == i[0] && frame[1].item == i[1] && frame[2].item == i[2]) || (frame[0].item == i[0] && frame[4].item == i[1] && frame[8].item == i[2]) || (frame[3].item == i[0] && frame[4].item == i[1] && frame[5].item == i[2]) || (frame[6].item == i[0] && frame[4].item == i[1] && frame[2].item == i[2]) || (frame[6].item == i[0] && frame[7].item == i[1] && frame[8].item == i[2])) {
                        flag = true
                    }

                }

                for (i in wildlist){

                    if (!i.contains("(${num})")) {
                        continue
                    }

                    val str = i.split("+")

                    val item = `ItemStack+`(Material.getMaterial(str[1]), str[2].toShort()).build()

                    if (frame[num].item == item || frame[num+3].item == item || frame[num+6].item == item){
                        flag2 = true
                    }

                }

                if (flag || flag2) {

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

                } else {
                    break@loop
                }

            }
        }

    }

    @Synchronized
    fun comCheck(): Boolean{

        val item = win_item!!

        return ((frame[0].item == item[0] && frame[1].item == item[1] && frame[2].item == item[2]) || (frame[0].item == item[0] && frame[4].item == item[1] && frame[8].item == item[2]) || (frame[3].item == item[0] && frame[4].item == item[1] && frame[5].item == item[2])
                || (frame[6].item == item[0] && frame[4].item == item[1] && frame[2].item == item[2]) || (frame[6].item == item[0] && frame[7].item == item[1] && frame[8].item == item[2]))

    }

    fun simulation(time: Int, reel: MutableList<ItemStack>, hitposi: MutableList<Int>, posi: Int, item: ItemStack): Int{

        for(i in 0 until time){

            if (hitposi.size == 1){
                if (reel[(posi+i+reel.size+(1-(hitposi[0])))%reel.size] == item)return i
            }else if(hitposi.size == 0){

                return 0

            }else if(hitposi.size == 2){

                if (reel[(posi+i+reel.size+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+reel.size+(1-(hitposi[1]*2)))%reel.size] == item)return i

            }else if (hitposi.size == 3){
                if (reel[(posi+i+reel.size+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+reel.size+(1-(hitposi[1]*2)))%reel.size] == item || reel[(posi+i+reel.size+(1-(hitposi[2]*2)))%reel.size] == item)return i
            }

        }

        return -1

    }

    fun simulation1(time: Int, reel: MutableList<ItemStack>, hitposi: MutableList<Int>, posi: Int, item: ItemStack): Int{

        for(i in 0 until time){

            if (hitposi.size == 1){
                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item)return i
            }else if(hitposi.size == 0){

                return 0

            }else if(hitposi.size == 2){

                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+(1-(hitposi[1]*2)))%reel.size] == item)return i

            }else if (hitposi.size == 3){
                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+(1-(hitposi[1]*2)))%reel.size] == item || reel[(posi+i+(1-(hitposi[2]*2)))%reel.size] == item)return i
            }

        }

        return -1

    }

    fun simulation2(time: Int, reel: MutableList<ItemStack>, hitposi: MutableList<Int>, posi: Int, item: ItemStack): Int{

        for(i in 0 until time){

            if (hitposi.size == 1){
                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item)return i
            }else if(hitposi.size == 0){

                return 0

            }else if(hitposi.size == 2){

                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+(1-(hitposi[1]*2)))%reel.size] == item)return i

            }else if (hitposi.size == 3){
                if (reel[(posi+i+(1-(hitposi[0])))%reel.size] == item || reel[(posi+i+(1-(hitposi[1]*2)))%reel.size] == item || reel[(posi+i+(1-(hitposi[2]*2)))%reel.size] == item)return i
            }

        }

        return -1

    }

    fun slotCheck(num: Int): MutableList<Int>{

        val item = win_item!!

        val list = mutableListOf<Int>()

        if (win_item!!.contains(ItemStack(Material.AIR))){

            if (winwild.contains(num)){

                when(winwild.size){
                    1-> {
                        if(winwild.contains(num)){

                            list.add(0)
                            list.add(1)
                            list.add(2)

                        }
                    }

                    2->{

                        if (!winwild.contains(0)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 0){

                                        if (num == 1){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 1){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(1)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 1){

                                        if (num == 0){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(2)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(2)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(2)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 2){

                                        if (num == 0){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }else if (num == 1){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }else if (num == 1){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }

                                }


                            }

                        }

                    }
                }

            }

        }else{

            when (slist.size){

                1->{
                    list.add(0)
                    list.add(1)
                    list.add(2)
                }

                2->{

                    when(slist[0]){

                        0->{

                            if (num == 1){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==2){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        1->{

                            if (num == 0){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }else if(num==2){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        2->{

                            if (num == 1){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==0){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                    }

                }

                3->{

                    when(num){

                        0->{

                            if (frame[1].item == item[1] && frame[2].item == item[2] || frame[4].item == item[1] && frame[8].item == item[2])list.add(0)
                            if (frame[4].item == item[1] && frame[5].item == item[2])list.add(1)
                            if (frame[4].item == item[1] && frame[2].item == item[2] || frame[7].item == item[1] && frame[8].item == item[2])list.add(2)

                        }

                        1->{

                            if (frame[0].item == item[0] && frame[2].item == item[2])list.add(0)
                            if (frame[3].item == item[0] && frame[5].item == item[2] || frame[0].item == item[0] && frame[8].item == item[2] || frame[6].item == item[0] && frame[2].item == item[2])list.add(1)
                            if (frame[6].item == item[0] && frame[2].item == item[2])list.add(2)

                        }

                        2->{

                            if (frame[1].item == item[1] && frame[0].item == item[0] || frame[4].item == item[1] && frame[6].item == item[0])list.add(0)
                            if (frame[4].item == item[1] && frame[3].item == item[0])list.add(1)
                            if (frame[4].item == item[1] && frame[0].item == item[0] || frame[7].item == item[1] && frame[6].item == item[0])list.add(2)

                        }

                    }

                }

            }

        }

        return list

    }

    fun slotCheck1(num: Int): MutableList<Int>{

        val item = win_item!!

        val list = mutableListOf<Int>()

        if (win_item!!.contains(ItemStack(Material.AIR))){

            if (winwild.contains(num)){

                when(winwild.size){
                    1-> {
                        if(winwild.contains(num)){

                            list.add(0)
                            list.add(1)
                            list.add(2)

                        }
                    }

                    2->{

                        if (!winwild.contains(0)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 0){

                                        if (num == 1){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 1){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(1)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 1){

                                        if (num == 0){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(2)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(2)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(2)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 2){

                                        if (num == 0){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }else if (num == 1){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }else if (num == 1){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }

                                }


                            }

                        }

                    }
                }

            }

        }else{

            when (slist.size){

                1->{
                    list.add(0)
                    list.add(1)
                    list.add(2)
                }

                2->{

                    when(slist[0]){

                        0->{

                            if (num == 1){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==2){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        1->{

                            if (num == 0){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }else if(num==2){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        2->{

                            if (num == 1){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==0){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                    }

                }

                3->{

                    when(num){

                        0->{

                            if (frame[1].item == item[1] && frame[2].item == item[2] || frame[4].item == item[1] && frame[8].item == item[2])list.add(0)
                            if (frame[4].item == item[1] && frame[5].item == item[2])list.add(1)
                            if (frame[4].item == item[1] && frame[2].item == item[2] || frame[7].item == item[1] && frame[8].item == item[2])list.add(2)

                        }

                        1->{

                            if (frame[0].item == item[0] && frame[2].item == item[2])list.add(0)
                            if (frame[3].item == item[0] && frame[5].item == item[2] || frame[0].item == item[0] && frame[8].item == item[2] || frame[6].item == item[0] && frame[2].item == item[2])list.add(1)
                            if (frame[6].item == item[0] && frame[2].item == item[2])list.add(2)

                        }

                        2->{

                            if (frame[1].item == item[1] && frame[0].item == item[0] || frame[4].item == item[1] && frame[6].item == item[0])list.add(0)
                            if (frame[4].item == item[1] && frame[3].item == item[0])list.add(1)
                            if (frame[4].item == item[1] && frame[0].item == item[0] || frame[7].item == item[1] && frame[6].item == item[0])list.add(2)

                        }

                    }

                }

            }

        }

        return list

    }

    fun slotCheck2(num: Int): MutableList<Int>{

        val item = win_item!!

        val list = mutableListOf<Int>()

        if (win_item!!.contains(ItemStack(Material.AIR))){

            if (winwild.contains(num)){

                when(winwild.size){
                    1-> {
                        if(winwild.contains(num)){

                            list.add(0)
                            list.add(1)
                            list.add(2)

                        }
                    }

                    2->{

                        if (!winwild.contains(0)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 0){

                                        if (num == 1){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 1){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(1)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 1){

                                        if (num == 0){
                                            if (frame[2].item == win_item!![2]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[5].item == win_item!![2]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[8].item == win_item!![2]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }else if (num == 2){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(2)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[2].item == win_item!![2]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[5].item == win_item!![2]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[8].item == win_item!![2]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }else if (num == 2){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(2)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }

                                }


                            }

                        }else if (!winwild.contains(2)){

                            when(slist.size){

                                1->{

                                    list.add(0)
                                    list.add(1)
                                    list.add(2)

                                }

                                2->{

                                    if (slist[0] != 2){

                                        if (num == 0){
                                            if (frame[1].item == win_item!![1]){
                                                list.add(0)
                                            }

                                            if (frame[4].item == win_item!![1]){
                                                if (!list.contains(0)) list.add(0)
                                                if (!list.contains(1)) list.add(1)
                                                if (!list.contains(2)) list.add(2)
                                            }

                                            if (frame[7].item == win_item!![1]){
                                                if (!list.contains(2)) list.add(2)
                                            }
                                        }else if (num == 1){
                                            if (frame[0].item == win_item!![0]){
                                                list.add(0)
                                                list.add(1)
                                            }

                                            if (frame[3].item == win_item!![0]){
                                                if (!list.contains(1)) list.add(1)
                                            }

                                            if (frame[6].item == win_item!![0]){
                                                if (!list.contains(2)) list.add(2)
                                                if (!list.contains(1)) list.add(1)
                                            }
                                        }

                                    }else{
                                        list.add(0)
                                        list.add(1)
                                        list.add(2)
                                    }

                                }

                                3->{

                                    if (num == 0){
                                        if (frame[1].item == win_item!![1]){
                                            list.add(0)
                                        }

                                        if (frame[4].item == win_item!![1]){
                                            if (!list.contains(0)) list.add(0)
                                            if (!list.contains(1)) list.add(1)
                                            if (!list.contains(2)) list.add(2)
                                        }

                                        if (frame[7].item == win_item!![1]){
                                            if (!list.contains(2)) list.add(2)
                                        }
                                    }else if (num == 1){
                                        if (frame[0].item == win_item!![0]){
                                            list.add(0)
                                            list.add(1)
                                        }

                                        if (frame[3].item == win_item!![0]){
                                            if (!list.contains(1)) list.add(1)
                                        }

                                        if (frame[6].item == win_item!![0]){
                                            if (!list.contains(2)) list.add(2)
                                            if (!list.contains(1)) list.add(1)
                                        }
                                    }

                                }


                            }

                        }

                    }
                }

            }

        }else{

            when (slist.size){

                1->{
                    list.add(0)
                    list.add(1)
                    list.add(2)
                }

                2->{

                    when(slist[0]){

                        0->{

                            if (num == 1){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==2){

                                if (frame[0].item == win_item!![0]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[3].item == win_item!![0]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[6].item == win_item!![0]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        1->{

                            if (num == 0){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }else if(num==2){

                                if (frame[1].item == win_item!![1]){
                                    list.add(0)
                                }

                                if (frame[4].item == win_item!![1]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(1)) list.add(1)
                                    if (!list.contains(2)) list.add(2)
                                }

                                if (frame[7].item == win_item!![1]){
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                        2->{

                            if (num == 1){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(1)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(2)) list.add(2)
                                    if (!list.contains(1)) list.add(1)
                                }

                            }else if(num==0){

                                if (frame[2].item == win_item!![2]){
                                    list.add(0)
                                    list.add(2)
                                }

                                if (frame[5].item == win_item!![2]){
                                    if (!list.contains(1)) list.add(1)
                                }

                                if (frame[8].item == win_item!![2]){
                                    if (!list.contains(0)) list.add(0)
                                    if (!list.contains(2)) list.add(2)
                                }

                            }

                        }

                    }

                }

                3->{

                    when(num){

                        0->{

                            if (frame[1].item == item[1] && frame[2].item == item[2] || frame[4].item == item[1] && frame[8].item == item[2])list.add(0)
                            if (frame[4].item == item[1] && frame[5].item == item[2])list.add(1)
                            if (frame[4].item == item[1] && frame[2].item == item[2] || frame[7].item == item[1] && frame[8].item == item[2])list.add(2)

                        }

                        1->{

                            if (frame[0].item == item[0] && frame[2].item == item[2])list.add(0)
                            if (frame[3].item == item[0] && frame[5].item == item[2] || frame[0].item == item[0] && frame[8].item == item[2] || frame[6].item == item[0] && frame[2].item == item[2])list.add(1)
                            if (frame[6].item == item[0] && frame[2].item == item[2])list.add(2)

                        }

                        2->{

                            if (frame[1].item == item[1] && frame[0].item == item[0] || frame[4].item == item[1] && frame[6].item == item[0])list.add(0)
                            if (frame[4].item == item[1] && frame[3].item == item[0])list.add(1)
                            if (frame[4].item == item[1] && frame[0].item == item[0] || frame[7].item == item[1] && frame[6].item == item[0])list.add(2)

                        }

                    }

                }

            }

        }

        return list

    }

    fun winCheck(num: Int){

        val size = slist.size

        if (win != "0"){

            var reel = mutableListOf<ItemStack>()

            when(num){
                0-> reel = reel1
                1-> reel = reel2
                2-> reel = reel3
            }

            val time = simulation(win_step!!, reel, slotCheck(num), step, win_item!![num])

            if(time == -1) return

            for (i in 0 until time){

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

            }
        }
    }

    fun winCheck1(num: Int){

        val size = slist.size

        if (win != "0"){

            var reel = mutableListOf<ItemStack>()

            when(num){
                0-> reel = reel1
                1-> reel = reel2
                2-> reel = reel3
            }

            val time = simulation1(win_step!!, reel, slotCheck1(num), step, win_item!![num])

            if(time == -1) return

            for (i in 0 until time){

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

            }
        }
    }

    fun winCheck2(num: Int){

        val size = slist.size

        if (win != "0"){

            var reel = mutableListOf<ItemStack>()

            when(num){
                0-> reel = reel1
                1-> reel = reel2
                2-> reel = reel3
            }

            val time = simulation2(win_step!!, reel, slotCheck2(num), step, win_item!![num])

            if(time == -1) return

            for (i in 0 until time){

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

            }
        }
    }

}
