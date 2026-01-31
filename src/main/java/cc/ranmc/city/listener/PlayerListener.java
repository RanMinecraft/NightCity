package cc.ranmc.city.listener;

import cc.baka9.catseedlogin.bukkit.CatSeedLoginAPI;
import cc.ranmc.city.Main;
import cc.ranmc.city.util.BasicUtil;
import cc.ranmc.city.util.TreasureUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.LoginUtil.sendDialog;
import static org.bukkit.event.block.Action.RIGHT_CLICK_AIR;
import static org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK;

public class PlayerListener implements Listener {

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;
        if (event.getAction() == RIGHT_CLICK_AIR || event.getAction() == RIGHT_CLICK_BLOCK) {
            if (item.getType() == Material.DIAMOND) {
                if (TreasureUtil.showDistance(player)) {
                    item.setAmount(item.getAmount() - 1);
                }
            }
        }
    }

    // 玩家退出事件
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        // 玩家退出提示
        if (player.hasPermission("city.svip")) {
            event.setQuitMessage(color(Main.getInstance().getConfig().getString("QuitMessageSvip"), player));
        } else if (player.hasPermission("city.vip")) {
            event.setQuitMessage(color(Main.getInstance().getConfig().getString("QuitMessageVip"), player));
        } else {
            event.setQuitMessage(color(Main.getInstance().getConfig().getString("QuitMessage"), player));
        }
    }

    // SHIFT + F 指令
    @EventHandler
    public void onPlayerSwapHandItemsEvent(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();

        if (player.isSneaking() && player.hasPermission("city.shift")) {
            player.chat("/cd");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerLogin(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.KICK_FULL) {
            Player player = event.getPlayer();
            if (player.hasPermission("city.vip")) {
                event.allow();
            }
        }
    }

    @EventHandler
    public void onPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.getPlayer().hasPermission("city.vip") && event.getMessage().length() <= 40) {
            event.setMessage(BasicUtil.color(event.getMessage()));
        }
    }

    // 玩家加入事件
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();

        // 玩家进入提示
        if (player.hasPermission("city.svip")) {
            event.setJoinMessage(color(Main.getInstance().getConfig().getString("JoinMessageSvip"),player));
        } else if (player.hasPermission("city.vip")) {
            event.setJoinMessage(color(Main.getInstance().getConfig().getString("JoinMessageVip"),player));
        } else {
            event.setJoinMessage(color(Main.getInstance().getConfig().getString("JoinMessage"),player));
        }

        // 保存玩家IP地址
        List<String> ipList = Main.getInstance().getIpData().getStringList(player.getName());

        String address = Objects.requireNonNull(player.getAddress()).getHostString();
        if (!ipList.contains(address)) ipList.add(address);
        Main.getInstance().getIpData().set(player.getName(), ipList);
        try {
            Main.getInstance().getIpData().save(Main.getInstance().getIpYml());
        } catch (IOException ignored) {}

        sendDialog(player);
    }

    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        Bukkit.getScheduler().runTaskLater(Main.getInstance(), () -> {
            if (CatSeedLoginAPI.isLogin(player.getName())) {
                player.closeDialog();
            } else {
                sendDialog(player);
            }
        }, 20);
    }
}
