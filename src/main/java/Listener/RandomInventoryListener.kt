package Listener

import RandomInventory
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import kotlin.collections.HashMap

class RandomInventoryListener : Listener {

    init { every() ; everyComment() }

    /**
     * ランダムデータの構造を定義するクラス
     */
    class RandomData(
        val player: Player,
        val inventory: List<ItemStack?>,
        val cursorItem: ItemStack,
        val hotbarSlot: Int,
        val location: Location
        )

    private val plugin = RandomInventory.getPlugin()
    private var config = plugin.config.apply { plugin.saveDefaultConfig() }
    private val commentData = WeakHashMap<Player, String>()
    private val previousPlayer = WeakHashMap<Player, Player>()

    var changeTime = config.getInt("ChangeTime", 60)

    /**
     * 定期的にアクションを起こすファンクション
     */
    private fun every() {

        var count = -1
        object:  BukkitRunnable() { override fun run() { count++

            // 全プレイヤー取得
            val players = Bukkit.getOnlinePlayers()

            // 差分取得
            val diffTime = changeTime - count

            // 3秒前カウント
            if (diffTime <= 3) {
                val pitch = if (diffTime == 3) 2.0F else if (diffTime == 2) 1.0F else if (diffTime == 1) 0.0F else 0.0F
                players.forEach {
                    it.sendTitle("", "§c§l§n ${diffTime} ", 0, 20, 20)
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, 1.0F, pitch)
                }
            }

            if (count < changeTime) return
            count = 0

            val invData = players
                .map {
                    RandomData(it, it.inventory.contents.toList(), it.itemOnCursor, it.inventory.heldItemSlot, it.location, )
                }
                .toMutableList()
            val commentData_Clone = commentData.toList()

            players.forEach { P ->

                // ランダム取得
                val d = invData
                    .filter { it.player != P }
                    .shuffled()
                    .firstOrNull() ?: return@forEach

                val player = d.player
                val inventory = d.inventory
                val cursorItem = d.cursorItem
                val hotbarSlot = d.hotbarSlot
                val location = d.location

                // プレイヤーへ反映
                applyInventory(P, inventory)
                applyCursorItem(P, cursorItem)
                applyHotbarSlot(P, hotbarSlot)
                applyLocation(P, location)
                applyPreviousPlayer(P, player)
                val comment = commentData_Clone.find { it.first == player }?.second ?: "§c§l指示なし"
                applyComment(P, comment)

                invData.remove(d)

                // 通知
                P.playSound(P.location, Sound.ENTITY_PLAYER_LEVELUP, 0.2F, 2.0F)
                P.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§6§l${player.name}§a§lの人生に移り変わりました"))
            }

        }}.runTaskTimer(RandomInventory.getPlugin(), 0, 20L)
    }

    /**
     * 定期アクション
     */
    private fun everyComment() {
        object: BukkitRunnable() { override fun run() {

            val players = Bukkit.getOnlinePlayers()

            players.forEach { P ->
                val comment = commentData.getOrPut(P, { "§c§l指示なし" })
                val previousPlayer = previousPlayer.get(P)?.name ?: "プレイヤー不明"
                P.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent("§b§l$previousPlayer §f > §a$comment"))
            }

        }}.runTaskTimer(RandomInventory.getPlugin(), 0, 10L)
    }

    /**
     * プレイヤーを指定したロケーションへTPします
     */
    fun applyLocation(player: Player, location: Location)
        = player.teleport(location)

    /**
     * プレイヤーのインベントリを指定したインベントリデータで上書きします
     */
    fun applyInventory(player: Player, inventory: List<ItemStack?>)
        = inventory.forEachIndexed { index, item -> player.inventory.setItem(index, item) }

    /**
     * プレイヤーのカーソルアイテムを上書きします
     */
    fun applyCursorItem(player: Player, item: ItemStack?)
        = item?.let { player.setItemOnCursor(item) }

    /**
     * プレイヤーのホットバー位置を変更します
     */
    fun applyHotbarSlot(player: Player, number: Int)
        = player.inventory.setHeldItemSlot(number)

    /**
     * 指示を上書きします
     */
    fun applyComment(player: Player, comment: String)
        = commentData.set(player, comment)

    /**
     * 前回のプレイヤーを上書きします
     */
    fun applyPreviousPlayer(player: Player, applyPlayer: Player)
        = previousPlayer.set(player, applyPlayer)

}