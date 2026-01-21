package cc.ranmc.city.listener;

import cc.ranmc.city.util.BasicUtil;
import cc.ranmc.city.util.InputUtil;
import cc.ranmc.city.util.MoneyUtil;
import cc.ranmc.city.util.TitleUtil;
import com.handy.playertitle.api.param.TitleBuffParam;
import com.handy.playertitle.constants.BuffApplyTypeEnum;
import com.handy.playertitle.constants.BuffTypeEnum;
import com.handy.playertitle.lib.attribute.PotionEffectParam;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import static cc.ranmc.city.util.BasicUtil.returnItem;
import static cc.ranmc.city.util.MoneyUtil.BANK_GUI_TITLE;
import static cc.ranmc.city.util.TitleUtil.TITLE_BUFF_GUI_TITLE;
import static cc.ranmc.city.util.TitleUtil.TITLE_GUI_TITLE;

public class GUIListener implements Listener {

    /**
     * 菜单关闭
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals(BANK_GUI_TITLE)) return;
        Player player = (Player) event.getPlayer();
        Inventory inventory = event.getInventory();
        for (int i = 0; i < 45; i++) {
            ItemStack item = inventory.getItem(i);
            if (item == null) continue;
            returnItem(player, item);
        }
    }

    /**
     * 菜单点击
     * @param event 事件
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();
        Inventory inventory = event.getClickedInventory();
        if (inventory == null) return;
        if (event.getView().getTitle().equals(BasicUtil.color(BANK_GUI_TITLE))) {
            if (inventory != player.getInventory() && event.getRawSlot() >= 45) {
                event.setCancelled(true);
            }
            if (clicked == null && inventory == player.getInventory()) return;

            if (event.getRawSlot() == 45) {
                player.chat("/earn");
                return;
            }
            if (event.getRawSlot() == 49) {
                MoneyUtil.save(player, inventory);
                return;
            }
            if (event.getRawSlot() == 53) {
                player.closeInventory();
                return;
            }
        }

        if (event.getView().getTitle().equals(BasicUtil.color(TITLE_GUI_TITLE))) {
            event.setCancelled(true);
            if (clicked == null) return;
            if (event.getRawSlot() == 2) {
                InputUtil.open(player, "定制称号", "称号名字", context -> {
                    TitleUtil.setName(player, context);
                });
                return;
            }
            if (event.getRawSlot() == 4) {
                if (event.isRightClick()) {
                    TitleUtil.clearBuff(player);
                } else {
                    TitleUtil.openBuffInventory(player);
                }
            }
            if (event.getRawSlot() == 6) {
                player.chat("/mp buy 定制称号");
                return;
            }
        }

        if (event.getView().getTitle().equals(BasicUtil.color(TITLE_BUFF_GUI_TITLE))) {
            event.setCancelled(true);
            if (clicked == null) return;

            if (clicked.getType() == Material.ENCHANTED_BOOK) {
                TitleBuffParam buff = new TitleBuffParam();
                buff.setBuffType(BuffTypeEnum.POTION_EFFECT);
                PotionEffectParam potion = new PotionEffectParam();
                String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
                potion.setPotionName(itemName.replaceAll("[^a-zA-Z_]", ""));
                potion.setPotionChinesizationName(itemName.replaceAll("[^一-龥]", ""));
                potion.setPotionLevel(1);
                buff.setPotionEffectParam(potion);
                TitleUtil.addBuff(player, buff);
            }
        }
    }
}
