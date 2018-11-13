package red.man10.man10slot

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

class `ItemStack+`(val mate: Material, val damage: Short=0, val amount: Int=1, var iname: String? = null, var lore: MutableList<String>? = null) {

    fun build(): ItemStack {

        val item = ItemStack(mate, amount, damage)
        val meta = item.itemMeta
        meta.displayName = iname
        meta.lore = lore

        item.itemMeta = meta

        return item
    }

}