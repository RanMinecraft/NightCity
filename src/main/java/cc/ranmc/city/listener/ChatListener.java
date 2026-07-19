package cc.ranmc.city.listener;

import cc.ranmc.city.util.AIUtil;
import com.alibaba.fastjson2.JSONObject;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

public class ChatListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAsyncPlayerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        JSONObject json = new JSONObject();
        json.put("sender", event.getPlayer().getName());
        json.put("content", ChatColor.stripColor(event.getMessage()));
        synchronized (AIUtil.MSG_ARRAY) {
            AIUtil.MSG_ARRAY.add(json);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) return;
        String cmd = event.getMessage();
        // /msg <player> <message>  或  /tell <player> <message>
        if (cmd.startsWith("/msg ") || cmd.startsWith("/tell ") || cmd.startsWith("/w ")) {
            String[] parts = cmd.split(" ", 3);
            if (parts.length >= 3) {
                JSONObject json = new JSONObject();
                json.put("sender", event.getPlayer().getName());
                json.put("content", "[私聊] " + ChatColor.stripColor(parts[2]));
                synchronized (AIUtil.MSG_ARRAY) {
                    AIUtil.MSG_ARRAY.add(json);
                }
            }
        }
    }
}
