import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandExecutor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class CommentCommand : CommandExecutor {

    init { Bukkit.getPluginCommand("c")!!.setExecutor(this) }

    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<out String>): Boolean {
        if (sender !is Player) return true

        val comment = args.joinToString("").replace("&", "§")
        RandomInventory.randomInventoryListener.applyComment(sender, comment)
        sender.sendMessage("§f[§6§l伝言§f] §a§l伝言を「${comment}」に設定しました")
        return true
    }
}