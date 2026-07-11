package cc.ranmc.city.command;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static cc.ranmc.city.util.BasicUtil.color;
import static cc.ranmc.city.util.BasicUtil.print;

public class GameModeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender,
                             @NotNull Command cmd,
                             @NotNull String label,
                             String @NotNull [] args) {
        // 以下指令不能在控制台输入
        if (!(sender instanceof Player player)) {
            print("&c该指令不能在控制台输入");
            return true;
        }

        if (args.length == 1) {
            if (sender.hasPermission("city.admin")) {
                switch (args[0]) {
                    case "0", "survival" -> {
                        player.setGameMode(GameMode.SURVIVAL);
                        sender.sendMessage(color("&a你已切换为&c生存模式"));
                    }
                    case "1", "creative" -> {
                        player.setGameMode(GameMode.CREATIVE);
                        sender.sendMessage(color("&a你已切换为&e创造模式"));
                    }
                    case "2", "adventure" -> {
                        player.setGameMode(GameMode.ADVENTURE);
                        sender.sendMessage(color("&a你已切换为&5冒险模式"));
                    }
                    case "3", "spectator" -> {
                        player.setGameMode(GameMode.SPECTATOR);
                        sender.sendMessage(color("&a你已切换为&f旁观模式"));
                    }
                    default -> sender.sendMessage(color("&c未知模式"));
                }
            } else {
                sender.sendMessage(color("&c权限不足"));
            }
            return true;
        }

        sender.sendMessage(color("&c未知指令"));
        return true;
    }
}

