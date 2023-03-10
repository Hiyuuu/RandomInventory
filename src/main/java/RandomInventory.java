import Listener.RandomInventoryListener;
import org.bukkit.plugin.java.JavaPlugin;

public class RandomInventory extends JavaPlugin {

    private static JavaPlugin plugin;
    public static RandomInventoryListener randomInventoryListener;

    @Override
    public void onEnable() {
        plugin = this;

        new CommentCommand();
        randomInventoryListener = new RandomInventoryListener();
        this.getLogger().info("[RandomInventory] プラグインが有効になった");
    }

    @Override
    public void onDisable(){
        this.getLogger().info("[RandomInventory] プラグインが無効になった");
    }

    public static JavaPlugin getPlugin(){ return plugin; }

}
