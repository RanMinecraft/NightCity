package cc.ranmc.city.listener;

import cc.baka9.catseedlogin.bukkit.event.CatSeedPlayerRegisterEvent;
import cc.ranmc.city.Main;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.BasicUtil.say;

public class CatSeedLoginListener implements Listener {

    @EventHandler
    public void onCatSeedPlayerRegisterEvent(CatSeedPlayerRegisterEvent event) {
        Player player = event.getPlayer();
        // 首次进入欢迎语
        say(color(Main.getInstance().getConfig().getString("FirstJoinMessage"), player));
        // 首次进入执行指令
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), color(Main.getInstance().getConfig().getString("RunCommandFirstJoin"), player));
        Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), "eco give " + player.getName() + " 500");
        Component welcome = Component.text(color("&a[点击欢迎]"))
                .hoverEvent(Component.text("点击欢迎" + player.getName()))
                .clickEvent(ClickEvent.suggestCommand("欢迎 " + player.getName() + "~"));
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p != player) p.sendMessage(welcome);
        }
    }



}
