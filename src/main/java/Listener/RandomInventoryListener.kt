package Listener

import RandomInventory
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.Inventory
import org.bukkit.scheduler.BukkitRunnable

class RandomInventoryListener : Listener {

    init { every() }

    /**
     * ランダムデータの構造を定義するクラス
     */
    class RandomData(
        val player: Player,
        val inventory: Inventory,
        val location: Location
    )

    private val plugin = RandomInventory.getPlugin()
    private var config = plugin.config.apply { plugin.saveDefaultConfig() }

    var changeTime = config.getInt("ChangeTime", 60)

    fun every() {

        var count = -1
        object:  BukkitRunnable() { override fun run() { count++
            if (count < changeTime) return
            count = 0

            val players = Bukkit.getOnlinePlayers()
            val invData = players
                .map { RandomData(it, it.inventory, it.location) }
                .toMutableList()

            players.forEach { P ->

                // ランダム取得
                val d = invData.shuffled().firstOrNull() ?: return@forEach

                val player = d.player
                val inventory = d.inventory
                val location = d.location

                // プレイヤーへ反映
                applyInventory(P, inventory)
                applyLocation(P, location)

                invData.remove(d)

                // 通知
                P.playSound(P.location, Sound.BLOCK_NOTE_BLOCK_PLING, 0.2F, 2.0F)
                P.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6§l${player.name}§a§lのインベントリに変更しました"))
            }

        }}.runTaskTimer(RandomInventory.getPlugin(), 0, 20L)
    }

    /**
     * プレイヤーを指定したロケーションへTPします
     */
    fun applyLocation(player: Player, location: Location)
        = player.teleport(location)

    /**
     * プレイヤーのインベントリを指定したインベントリデータで上書きします
     */
    fun applyInventory(player: Player, inventory: Inventory)
        = inventory.contents.forEachIndexed { index, item -> player.inventory.setItem(index, item) }

}