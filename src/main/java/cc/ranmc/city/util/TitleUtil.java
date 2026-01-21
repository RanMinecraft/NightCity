package cc.ranmc.city.util;

import com.handy.playertitle.api.PlayerTitleApi;
import com.handy.playertitle.api.param.TitleBuffParam;
import com.handy.playertitle.api.param.TitleListParam;
import com.handy.playertitle.constants.BuyTypeEnum;
import com.handy.playertitle.lib.attribute.PotionEffectParam;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.BasicUtil.print;
import static cc.ranmc.city.util.BasicUtil.rgbString;

public class TitleUtil {

    private static final Map<String, String> nameMap = new HashMap<>();
    private static final Map<String, List<TitleBuffParam>> buffMap = new HashMap<>();
    public static final String TITLE_GUI_TITLE = BasicUtil.color("&9&l夜城 &0- &e&l定制称号");
    public static final String TITLE_BUFF_GUI_TITLE = BasicUtil.color("&9&l夜城 &0- &e&l定制属性");
    public static final ItemStack PANE = BasicUtil.getItem(Material.GRAY_STAINED_GLASS_PANE, 1, " ");
    public static final int TITLE_NAME_PRICE = 20;
    public static final int TITLE_BUFF_PRE_PRICE = 30;

    public static void openGUI(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 9, TITLE_GUI_TITLE);
        inventory.setItem(0, PANE);
        inventory.setItem(1, PANE);

        ItemStack item = BasicUtil.getItem(Material.NAME_TAG, 1,
                rgbString("&b称号名字 &f[" + getName(player.getName()) + "&f]"));
        ItemMeta meta = item.getItemMeta();
        meta.setLore(getColorLore());
        item.setItemMeta(meta);
        inventory.setItem(2, item);

        inventory.setItem(3, PANE);

        List<String> lore = new ArrayList<>(List.of(color("&b价格:&e " + TITLE_BUFF_PRE_PRICE + "元/级")));
        buffMap.getOrDefault(player.getName(), new ArrayList<>()).forEach(buff ->
                lore.add(color("&a" + buff.getPotionEffectParam().getPotionChinesizationName()
                        + ": " + buff.getPotionEffectParam().getPotionLevel() + "级")));
        lore.addAll(List.of("&e左键新增称号属性", "&e右键清空全部属性"));
        inventory.setItem(4, BasicUtil.getItem(Material.ENCHANTED_BOOK, 1,
                color("&b称号属性"), lore));
        inventory.setItem(5, PANE);
        inventory.setItem(6, BasicUtil.getItem(Material.SLIME_BALL, 1,
                color("&a确认购买"),
                "&b合计:&e " + getPrice(player.getName()) + "元",
                "&b购买时间:&e 永久使用",
                "&9确认称号名与属性无误"));
        inventory.setItem(7, PANE);
        inventory.setItem(8, PANE);
        player.openInventory(inventory);
    }

    public static List<String> getColorLore() {
        List<String> lore = new ArrayList<>();
        lore.add(color("&b价格:&e " + TITLE_NAME_PRICE + "元"));
        lore.add(color("&e点击更改称号名称"));
        lore.add(" ");
        lore.add(color("&9颜色符号实例"));
        lore.add("§f&a亮绿->"+ color("&a亮绿")+"  §f&b亮蓝->"+ color("&b亮蓝"));
        lore.add("§f&c红色->"+ color("&c红色")+"  §f&d粉色->"+ color("&d粉色"));
        lore.add("§f&e黄色->"+ color("&e黄色")+"  §f&f白色->"+ color("&f白色"));
        lore.add("§f&0黑色->"+ color("&0黑色")+"  §f&1蓝色->"+ color("&1蓝色"));
        lore.add("§f&2绿色->"+ color("&2绿色")+"  §f&3青色->"+ color("&3青色"));
        lore.add("§f&4深红->"+ color("&4深红")+"  §f&5紫色->"+ color("&5紫色"));
        lore.add("§f&6金色->"+ color("&6金色")+"  §f&7浅灰->"+ color("&7浅灰"));
        lore.add("§f&8深灰->"+ color("&8深灰")+"  §f&9浅蓝->"+ color("&9浅蓝"));
        lore.add("§f&#2196f3->"+ rgbString("&#2196f3 支持6位RGB颜色代码"));
        return lore;
    }

    public static int getPrice(String playerName) {
        int price = TITLE_NAME_PRICE;
        for (TitleBuffParam param : buffMap.getOrDefault(playerName, new ArrayList<>())) {
            price += param.getPotionEffectParam().getPotionLevel() * TITLE_BUFF_PRE_PRICE;
        }
        return price;
    }

    public static void give(String playerName) {
        TitleListParam param = new TitleListParam();
        param.setAmount(99999);
        param.setIsHide(1);
        param.setTitleName(getName(playerName));
        param.setDescription(playerName + "定制专属称号");
        param.setBuyTypeEnum(BuyTypeEnum.ACTIVITY);
        param.setTitleBuffs(buffMap.getOrDefault(playerName, new ArrayList<>()));
        Player player = Bukkit.getPlayerExact(playerName);
        UUID uuid;
        if (player == null) {
            uuid = Bukkit.getPlayerUniqueId(playerName);
        } else {
            uuid = player.getUniqueId();
            player.sendMessage(color("&a定制称号已经发往您的仓库。"));
        }
        if (uuid == null) {
            print("&c无法发放定制称号" + playerName);
            return;
        }
        int id = PlayerTitleApi.getInstance().add(param);
        PlayerTitleApi.getInstance().set(uuid, id);
    }

    public static String getName(String playerName) {
        return nameMap.getOrDefault(playerName, "&e鸽子");
    }

    public static void setName(Player player, String name) {
        name = name.toLowerCase().replace(" ", "")
                .replace("§", "")
                .replace("&k", "")
                .replace("&l", "")
                .replace("&m", "")
                .replace("&n", "")
                .replace("&o", "")
                .replace("&r", "");
        Pattern pattern = Pattern.compile("^[一-龥]{2,5}$");
        if (pattern.matcher(clearColor(name)).matches() && checkFormat(name)) {
            nameMap.put(player.getName(), name);
            player.sendMessage(color("&a设置成功"));
            TitleUtil.openGUI(player);
        } else {
            player.sendMessage(color("&c称号名称不规范,由2~5位中文组成"));
        }
    }

    public static String clearColor(@NotNull String text) {
        text = ChatColor.stripColor(color(text));
        if (text.contains("#")) {
            text = Pattern.compile("(?i)#[A-Fa-f0-9]{6}")
                    .matcher(text)
                    .replaceAll("")
                    .replace("§", "");
        }
        return text;
    }

    public static boolean checkFormat(String input) {
        for (int i = 1; i < input.length(); i++) {
            if (input.charAt(i) == '&' || input.charAt(i) == '§') {
                char prevChar = input.charAt(i - 1);
                if (!String.valueOf(prevChar).matches("[一-龥]")) {
                    return false;
                }
            }
            if (input.charAt(i) == '#') {
                char prevChar = input.charAt(i - 1);
                if (!String.valueOf(prevChar).equalsIgnoreCase("&")) {
                    return false;
                }
            }
        }
        return true;
    }

    public static void addBuff(Player player, TitleBuffParam newParam) {
        List<TitleBuffParam> list = buffMap.getOrDefault(player.getName(), new ArrayList<>());
        boolean exists = false;
        for (TitleBuffParam param : list) {
            PotionEffectParam buff = param.getPotionEffectParam();
            if (buff.getPotionName().equals(newParam.getPotionEffectParam().getPotionName())) {
                if (buff.getPotionLevel() == 2) {
                    player.sendMessage(color("&c定制属性等级最高为2"));
                    TitleUtil.openGUI(player);
                    return;
                }
                buff.setPotionLevel(2);
                exists = true;
                break;
            }
        }
        if (!exists) list.add(newParam);
        buffMap.put(player.getName(), list);
        player.sendMessage(color("&a添加成功"));
        TitleUtil.openGUI(player);
    }

    public static void openBuffInventory(Player player) {
        Inventory inv = Bukkit.createInventory(null, 18, TITLE_BUFF_GUI_TITLE);
        addBuffBtn(inv, "&b夜视&8(NIGHT_VISION)", "调亮在黑暗中的视野");
        addBuffBtn(inv, "&b发光&8(GLOWING)" , "高亮显示玩家");
        addBuffBtn(inv, "&b迅捷&8(SPEED)", "让你跑的更快");
        addBuffBtn(inv, "&b跳跃提升&8(JUMP)", "让你跳的更高");
        addBuffBtn(inv, "&b饱和&8(SATURATION)", "不断恢复饱和");
        addBuffBtn(inv, "&b抗性提升&8(DAMAGE_RESISTANCE)", "减少受到伤害");
        addBuffBtn(inv, "&b防火&8(FIRE_RESISTANCE)", "免疫火焰伤害");
        addBuffBtn(inv, "&b力量&8(INCREASE_DAMAGE)", "造成更高伤害");
        addBuffBtn(inv, "&b急迫&8(FAST_DIGGING)", "提升挖掘速度");
        addBuffBtn(inv, "&b幸运&8(LUCK)", "提高掉落物奖励及钓鱼收益");
        addBuffBtn(inv, "&b生命恢复&8(REGENERATION)", "不断恢复生命");
        addBuffBtn(inv, "&b伤害吸收&8(ABSORPTION)", "吸收受到伤害");
        addBuffBtn(inv, "&b海豚的恩惠&8(DOLPHINS_GRACE)", "水下游得更快");
        addBuffBtn(inv, "&b潮涌能量&8(CONDUIT_POWER)", "在水下获得呼吸、夜视、速掘加成");
        addBuffBtn(inv, "&b村庄英雄&8(HERO_OF_THE_VILLAGE)", "村民交易时候享受折扣或赠送礼物");
        player.openInventory(inv);
    }

    private static void addBuffBtn(Inventory inventory, String name, String lore) {
        inventory.addItem(BasicUtil.getItem(Material.ENCHANTED_BOOK, 1, name,
                "&9" + lore,
                "&9可重复选择叠加等级,最高2级",
                "&e点击添加属性"));
    }

    public static void clearBuff(Player player) {
        buffMap.remove(player.getName());
        player.sendMessage(color("&a清空成功"));
        TitleUtil.openGUI(player);
    }
}
