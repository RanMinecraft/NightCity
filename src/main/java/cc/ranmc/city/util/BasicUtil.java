package cc.ranmc.city.util;

import cc.ranmc.city.Main;
import cc.ranmc.city.papi.Papi;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BasicUtil {

    // 输出日志
    public static void print(String message) {
        Bukkit.getConsoleSender().sendMessage(message);
    }

    // 服务器输出
    public static void say(String message) {
        Bukkit.broadcastMessage(message);
    }

    // 文本替换
    public static String color(String text) {
        if (text == null) {
            text = "";
            print("§b[夜城] §c插件输出文本信息出错");
        } else {
            text=text.replace("&", "§");
        }
        return text;
    }

    public static String color(String text, Player p) {
        if (text == null) {
            text = "";
            print("§b[夜城] §c插件输出文本信息出错");
        } else {
            text=text.replace("&", "§")
                    .replace("%player%",p.getName())
                    .replace("%player_x%", String.valueOf(p.getLocation().getBlockX()))
                    .replace("%player_y%", String.valueOf(p.getLocation().getBlockY()))
                    .replace("%player_z%", String.valueOf(p.getLocation().getBlockZ()));
        }
        return text;
    }

    public static String rgbString(@NotNull String text) {
        return translateColorCodes(color(text));
    }

    private static String translateColorCodes(String str) {
        Matcher matcher = Pattern.compile("&#([0-9a-fA-F]){6}|&#([0-9a-fA-F]){3}|§#([0-9a-fA-F]){6}|§#([0-9a-fA-F]){3}").matcher(str);

        StringBuilder sb;
        String hex;
        for(sb = new StringBuilder(); matcher.find(); matcher.appendReplacement(sb, net.md_5.bungee.api.ChatColor.of(hex.substring(1)).toString())) {
            hex = matcher.group();
            if (hex.length() == 5) {
                hex = hex.substring(0, 2) + doubleCharacters(hex.substring(2));
            }
        }

        matcher.appendTail(sb);
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('§', sb.toString());
    }

    private static String doubleCharacters(String str) {
        StringBuilder sb = new StringBuilder();
        char[] var2 = str.toCharArray();
        for (char c : var2) {
            sb.append(c);
            sb.append(c);
        }
        return sb.toString();
    }

    public static String getLocation(Location location) {
        return Objects.requireNonNull(location.getWorld()).getName() + "," +
                location.getX() + "," +
                location.getY() + "," +
                location.getZ() + "," +
                location.getYaw() + "," +
                location.getPitch();
    }

    public static Location getLocation(String locationStr) {
        if (locationStr == null || locationStr.isEmpty()) return null;
        String[] date = locationStr.split(",");
        World world = Bukkit.getWorld(date[0]);
        if (world == null) {
            print("不存在世界" + locationStr);
            return null;
        }
        Location location = new Location(world,
                Double.parseDouble(date[1]),
                Double.parseDouble(date[2]),
                Double.parseDouble(date[3]));
        if (date.length == 6) {
            location.setYaw(Float.parseFloat(date[4]));
            location.setPitch(Float.parseFloat(date[5]));
        }
        return location;
    }

    /**
     * 获取物品
     */
    public static ItemStack getItem(Material material, int count) {
        return new ItemStack(material,count);
    }

    public static ItemStack getItem(Material material, int count, String name) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        item.setItemMeta(meta);
        return item;
    }

    public static ItemStack getItem(Material material, int count, String name, String... lore) {
        return getItem(material, count, name, Arrays.asList(lore));
    }

    public static ItemStack getItem(Material material, int count, String name, List<String> lore) {
        ItemStack item = new ItemStack(material,count);
        ItemMeta meta = item.getItemMeta();
        Objects.requireNonNull(meta).setDisplayName(color(name));
        List<String> newLore = new ArrayList<>();
        lore.forEach(line -> newLore.add(color(line)));
        meta.setLore(newLore);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isInventoryFull(Player player) {
        return isInventoryFull(player.getInventory());
    }

    public static boolean isInventoryFull(Inventory inventory) {
        for(int i = 0; i < 36; ++i) {
            if (inventory.getItem(i) == null) {
                return false;
            }
        }
        return true;
    }

    public static void returnItem(Player player, ItemStack item) {
        if (BasicUtil.isInventoryFull(player)) {
            player.getWorld().dropItem(player.getLocation(), item);
            player.sendMessage(color("&c请勿放入非现金,已掉落地面"));
        } else {
            player.getInventory().addItem(item);
            player.sendMessage(color("&c请勿放入非现金,已返还背包"));
        }
    }

    public static boolean checkPlugin(String pluginName) {
        if (Bukkit.getPluginManager().getPlugin(pluginName) != null) {
            print("§b[夜城] §a成功加载" + pluginName + "插件");
            return true;
        } else {
            print("§b[夜城] §c无法找到" + pluginName + "插件,部分功能受限");
            return false;
        }
    }

    /**
     * 执行指令
     */
    public static void run(String command) {
        Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(Main.getInstance(), () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
    }
}
