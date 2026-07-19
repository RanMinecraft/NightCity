package cc.ranmc.city.listener;

import cc.ranmc.city.Main;
import cc.ranmc.city.util.BasicUtil;
import cc.ranmc.city.util.TreasureUtil;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.block.BlockPistonRetractEvent;

import java.util.List;

import static cc.ranmc.city.util.BasicUtil.color;

public class BlockListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        // 限制生存世界挖矿
        if (event.getBlock().getWorld().getName()
                .equalsIgnoreCase(Main.getInstance().getConfig().getString("ProtectWorld"))) {
            for (String ore : Main.getInstance().getConfig().getStringList("ProtectOre")) {
                if (event.getBlock().getType() == Material.getMaterial(ore)) {
                    player.sendMessage(color("&b[夜城] &c请前往资源世界挖矿"));
                    event.setExpToDrop(0);
                    event.setDropItems(false);
                    return;
                }
            }
        }
        TreasureUtil.blockBreak(event.getBlock());
    }

    // 禁止活塞推动宝藏
    @EventHandler
    public void onBlockPistonExtendEvent(BlockPistonExtendEvent event) {
        if (event.getBlocks().isEmpty()) return;
        String treasureWorldName = Main.getInstance().getConfig().getString("treasure.world", "zy");
        if (!event.getBlock().getWorld().getName().equalsIgnoreCase(treasureWorldName)) return;
        List<String> worldList = Main.getInstance().getTreasureData().getStringList(treasureWorldName);
        for (Block block : event.getBlocks()) {
            String blockLoc = BasicUtil.getLocation(block.getLocation());
            for (String treasureLoc : worldList) {
                if (blockLoc.equals(treasureLoc)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }

    @EventHandler
    public void onBlockPistonRetractEvent(BlockPistonRetractEvent event) {
        if (event.getBlocks().isEmpty()) return;
        String treasureWorldName = Main.getInstance().getConfig().getString("treasure.world", "zy");
        if (!event.getBlock().getWorld().getName().equalsIgnoreCase(treasureWorldName)) return;
        List<String> worldList = Main.getInstance().getTreasureData().getStringList(treasureWorldName);
        for (Block block : event.getBlocks()) {
            String blockLoc = BasicUtil.getLocation(block.getLocation());
            for (String treasureLoc : worldList) {
                if (blockLoc.equals(treasureLoc)) {
                    event.setCancelled(true);
                    break;
                }
            }
        }
    }
}
