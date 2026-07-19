package cc.ranmc.city.command;

import cc.ranmc.city.Main;
import cc.ranmc.city.util.BasicUtil;
import cc.ranmc.city.util.MoneyUtil;
import cc.ranmc.city.util.TitleUtil;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.VipUtil.durationToDays;

public class CityCommand implements CommandExecutor {

    private static final String PREFIX = "&b[夜城] ";

    @Override
    public boolean onCommand(@NotNull CommandSender sender, 
                             @NotNull Command cmd, 
                             @NotNull String label, 
                             String[] args) {

        if (args.length == 1) {
            // 重载
            if (args[0].equalsIgnoreCase("reload")) {
                if (!sender.hasPermission("city.admin")) {
                    sender.sendMessage(color(PREFIX + "&c你没有足够的权限执行"));
                    return true;
                }
                Main.getInstance().loadConfig();
                sender.sendMessage(color(PREFIX + "&a重载成功"));

                return true;
            }
            // 停止服务器倒计时
            if (args[0].equalsIgnoreCase("stop")) {
                if (!sender.hasPermission("city.admin")) {
                    sender.sendMessage(color(PREFIX + "&c你没有足够的权限执行"));
                    return true;
                }
                Bukkit.broadcastMessage(color(PREFIX + "&c请大家不要呆在死亡掉落的世界！"));
                for (int i = 10; i > 0; i--) {
                    int second = i;
                    Bukkit.getScheduler().runTaskLater(Main.getInstance(), () ->
                            Bukkit.broadcastMessage(color(PREFIX + "&c服务器将在 " + second + " 秒后重启！")), (long) (10 - second + 1) * 20);
                }
                Bukkit.getScheduler().runTaskLater(Main.getInstance(), () ->
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "stop"), 11 * 20);
                return true;
            }
            // 存钱
            if (args[0].equalsIgnoreCase("money") &&
                    sender instanceof Player player) {
                MoneyUtil.openGUI(player);
                return true;
            }
            // 自定义称号
            if (args[0].equalsIgnoreCase("title") &&
                    sender instanceof Player player) {
                TitleUtil.openGUI(player);
                return true;
            }
        }

        if (args.length == 2) {
            // 获取玩家信息
            if (args[0].equalsIgnoreCase("info")) {
                if (!sender.hasPermission("city.admin")) {
                    sender.sendMessage(color(PREFIX + "&c你没有足够的权限执行"));
                    return true;
                }
                List<String> ipList = Main.getInstance().getIpData().getStringList(args[1]);
                if (ipList.isEmpty()) {
                    sender.sendMessage(color(PREFIX + "&c没有找到该玩家的IP地址"));
                    return true;
                }
                sender.sendMessage(color("&e找到" + ipList.size() + "个" + args[1] + "使用过的IP地址"));
                for (String ipl : ipList) {
                    sender.sendMessage(BasicUtil.color("&e- " + ipl));
                }
                return true;
            }
            // 给予自定义称号
            if (args[0].equalsIgnoreCase("title")) {
                if (!sender.hasPermission("city.admin")) {
                    sender.sendMessage(color(PREFIX + "&c你没有足够的权限执行"));
                    return true;
                }
                TitleUtil.give(args[1]);
                return true;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("vip")) {
                if (!sender.hasPermission("city.admin")) {
                    sender.sendMessage(color(PREFIX + "&c你没有足够的权限执行"));
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage(color(PREFIX + "&c该玩家不在线"));
                    return true;
                }
                int plus = 30;
                try {
                    plus = Integer.parseInt(args[2]);
                } catch (NumberFormatException ignored) {}
                plus += durationToDays(PlaceholderAPI.setPlaceholders(target, "%luckperms_group_expiry_time_vip%"));
                BasicUtil.run("lp user " + target.getName() + " parent removetemp vip");
                BasicUtil.run("lp user " + target.getName() + " parent addtemp vip " + plus + "d");
                sender.sendMessage(color(PREFIX + "&a玩家" + target.getName() + "会员时间已更新至" + plus + "天"));
                return true;
            }
        }
        sender.sendMessage(color(PREFIX + "&c未知指令,请检查后重新输入"));
        return true;

    }

}
