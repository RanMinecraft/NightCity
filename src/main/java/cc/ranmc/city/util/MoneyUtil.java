package cc.ranmc.city.util;

import cc.ranmc.city.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Objects;

import static cc.ranmc.city.util.BasicUtil.returnItem;

public class MoneyUtil {

    public static final ItemStack PANE = BasicUtil.getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
    public static final String BANK_GUI_TITLE = BasicUtil.color("&9&l夜城 &0- &e&l存入现金");

    public static ItemStack getMoneyItem(int money) {
        ItemStack item = BasicUtil.getItem(Material.FIRE_CHARGE, 1,
                "&b夜城币", "&e面值: " + money);
        ItemMeta meta = item.getItemMeta();
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        meta.addEnchant(Enchantment.FLAME, 1, false);
        item.setItemMeta(meta);
        return item;
    }

    public static boolean isMoney(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        return item.getType() == Material.FIRE_CHARGE && meta.hasLore() &&
                Objects.requireNonNull(meta.getLore()).getFirst().startsWith(BasicUtil.color("&e面值: "));
    }

    public static int countMoney(ItemStack item) {
        if (item == null) return 0;
        ItemMeta meta = item.getItemMeta();
        if (item.getType() != Material.FIRE_CHARGE || !meta.hasLore()) return 0;
        String lore = Objects.requireNonNull(meta.getLore()).getFirst();
        if (!lore.startsWith(BasicUtil.color("&e面值: "))) return 0;
        int money = 0;
        try {
            money = Integer.parseInt(lore.split(" ")[1]) * item.getAmount();
        } catch (NullPointerException ignored) {}
        return money;
    }

    public static void openGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, BANK_GUI_TITLE);

        inventory.setItem(45, BasicUtil.getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c返回菜单"));
        inventory.setItem(46, PANE);
        inventory.setItem(47, PANE);
        inventory.setItem(48, PANE);
        inventory.setItem(49, BasicUtil.getItem(Material.VAULT, 1,
                "&b存入现金",
                "&e你的余额&c " + (int) Main.getInstance().getEcon().getBalance(player),
                "&e请在上方放入现金",
                "&e不要放置其他物品",
                "&e否则可能造成遗失"));
        inventory.setItem(50, PANE);
        inventory.setItem(51, PANE);
        inventory.setItem(52, PANE);
        inventory.setItem(53, BasicUtil.getItem(Material.RED_STAINED_GLASS_PANE, 1, "&c关闭菜单"));
        player.openInventory(inventory);
    }

    public static void save(Player player, Inventory inventory) {
        int money = 0;
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            if (MoneyUtil.isMoney(item)) {
                money += MoneyUtil.countMoney(item);
            } else {
                returnItem(player, item);
            }
            inventory.setItem(i, new ItemStack(Material.AIR));
        }
        Main.getInstance().getEcon().depositPlayer(player, money);
        player.sendMessage(BasicUtil.color("&a夜城币 " + money + " 已经存入你的账户"));
    }

}
