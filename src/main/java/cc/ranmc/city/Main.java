package cc.ranmc.city;

import java.io.File;
import java.util.Objects;

import cc.ranmc.city.command.CityCommand;
import cc.ranmc.city.command.CityTabComplete;
import cc.ranmc.city.command.GameModeCommand;
import cc.ranmc.city.listener.BlockListener;
import cc.ranmc.city.listener.CatSeedLoginListener;
import cc.ranmc.city.listener.FurnaceListener;
import cc.ranmc.city.listener.GUIListener;
import cc.ranmc.city.listener.MinePayListener;
import cc.ranmc.city.listener.PlayerListener;
import cc.ranmc.city.papi.Papi;
import cc.ranmc.city.util.BasicUtil;
import cc.ranmc.city.util.MoneyUtil;
import cc.ranmc.city.util.TreasureUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.inventory.FurnaceRecipe;
import org.bukkit.plugin.java.JavaPlugin;

import net.milkbowl.vault.economy.Economy;

import static cc.ranmc.city.util.BasicUtil.checkPlugin;
import static cc.ranmc.city.util.BasicUtil.print;

public class Main extends JavaPlugin implements Listener{

    @Getter
	private YamlConfiguration ipData, treasureData;
    @Getter
    private File ipYml, treasureYml;
    @Getter
  	private Economy econ;
    @Getter
    private static Main instance;
    
	@Override
	public void onEnable() {
        instance = this;
		// 注册 Event
		Bukkit.getPluginManager().registerEvents(new BlockListener(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerListener(), this);
        Bukkit.getPluginManager().registerEvents(new GUIListener(), this);
        Bukkit.getPluginManager().registerEvents(new CatSeedLoginListener(), this);
        Bukkit.getPluginManager().registerEvents(new MinePayListener(), this);

        FurnaceRecipe coinRecipe100 = new FurnaceRecipe(
                new NamespacedKey(this, "city_coin_100"),
                MoneyUtil.getMoneyItem(100),
                Material.MUSIC_DISC_STAL,
                0.0f,
                70 * 20
        );
        FurnaceRecipe coinRecipe200 = new FurnaceRecipe(
                new NamespacedKey(this, "city_coin_200"),
                MoneyUtil.getMoneyItem(200),
                Material.MUSIC_DISC_PRECIPICE,
                0.0f,
                60 * 20
        );
        FurnaceRecipe coinRecipe300 = new FurnaceRecipe(
                new NamespacedKey(this, "city_coin_300"),
                MoneyUtil.getMoneyItem(300),
                Material.MUSIC_DISC_RELIC,
                0.0f,
                50 * 20
        );
        FurnaceRecipe coinRecipe500 = new FurnaceRecipe(
                new NamespacedKey(this, "city_coin_400"),
                MoneyUtil.getMoneyItem(400),
                Material.MUSIC_DISC_CREATOR_MUSIC_BOX,
                0.0f,
                40 * 20
        );
        FurnaceRecipe coinRecipe1000 = new FurnaceRecipe(
                new NamespacedKey(this, "city_coin_500"),
                MoneyUtil.getMoneyItem(500),
                Material.MUSIC_DISC_TEARS,
                0.0f,
                30 * 20
        );
        Bukkit.addRecipe(coinRecipe100);
        Bukkit.addRecipe(coinRecipe200);
        Bukkit.addRecipe(coinRecipe300);
        Bukkit.addRecipe(coinRecipe500);
        Bukkit.addRecipe(coinRecipe1000);

        Bukkit.getPluginManager().registerEvents(new FurnaceListener(), this);

        // 注册指令
        PluginCommand cityCommand = Objects.requireNonNull(Bukkit.getPluginCommand("city"));
        cityCommand.setExecutor(new CityCommand());
        cityCommand.setTabCompleter(new CityTabComplete());
        Objects.requireNonNull(Bukkit.getPluginCommand("g")).setExecutor(new GameModeCommand());

		// 加载配置文件
		loadConfig();
		
		// 输出成功启动
		Bukkit.getConsoleSender().sendMessage(BasicUtil.color("&b[夜城] &a插件已经成功加载 &dBy Ranica"));

        int delay = getConfig().getInt("treasure.time", 600) * 20;
        Bukkit.getScheduler().runTaskTimer(this, TreasureUtil::generate, delay, delay);
	}
	
	// 加载配置文件
	public void loadConfig() {
		// 检查配置文件
        if (!new File(getDataFolder() + File.separator + "config.yml").exists()) {
            saveDefaultConfig();
        }
        reloadConfig();
		
		// 加载 IP 地址
		ipYml = new File(getDataFolder(), "ip.yml");
		if (!ipYml.exists()) saveResource("ip.yml", true);
		ipData = YamlConfiguration.loadConfiguration(ipYml);

        // 加载宝藏地址
        treasureYml = new File(getDataFolder(), "treasure.yml");
        if (!treasureYml.exists()) saveResource("treasure.yml", true);
        treasureData = YamlConfiguration.loadConfiguration(treasureYml);
		
        // Vault插件
        if (Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            econ = Objects.requireNonNull(getServer().getServicesManager().getRegistration(Economy.class)).getProvider();
            print("§b[夜城] §a成功加载Vault插件");
        } else {
            print("§b[夜城] §c无法找到Vault插件,部分功能受限");
        }

        // 插件
        if (checkPlugin("Vault")) {
            econ = Objects.requireNonNull(getServer().getServicesManager().getRegistration(Economy.class)).getProvider();
        }
        if (checkPlugin("PlaceholderAPI")) {
            new Papi().register();
        }
        checkPlugin("CatSeedLogin");
        checkPlugin("ViaVersion");
	}
	
}
