package cc.ranmc.city.papi;

import cc.ranmc.city.Main;
import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Papi extends PlaceholderExpansion {

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public @NotNull String getAuthor() {
        return "Ranica";
    }

    @Override
    public @NotNull String getIdentifier() {
        return "city";
    }

    @Override
    public @NotNull String getVersion() {
        return Main.getInstance().getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onRequest(OfflinePlayer player, @NotNull String identifier) {

        if (player == null || !player.isOnline()) return "&c玩家不在线";

        if (identifier.equals("viptime")) {
            Player onlinePlayer = (Player) player;
            if (onlinePlayer.hasPermission("city.svip")) return "永久";
            if (onlinePlayer.hasPermission("city.vip")) {
                return PlaceholderAPI.setPlaceholders(player, "%luckperms_group_expiry_time_vip%")
                        .replace("y ", "年")
                        .replace("mo ", "月")
                        .replace("w ", "周")
                        .replace("d ", "日")
                        .replace("h ", "时")
                        .replace("m ", "分")
                        .replace("s", "秒");
            }
            return "未开通";
        }

        return "暂无";
    }
}