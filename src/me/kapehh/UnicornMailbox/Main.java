package me.kapehh.UnicornMailbox;

import me.kapehh.main.pluginmanager.checker.PluginChecker;
import me.kapehh.main.pluginmanager.config.EventPluginConfig;
import me.kapehh.main.pluginmanager.config.EventType;
import me.kapehh.main.pluginmanager.config.PluginConfig;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseInfo;
import me.kapehh.main.pluginmanager.thread.PluginAsyncTimer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

/**
 * Created by Karen on 08.04.2015.
 */
public class Main extends JavaPlugin {
    PluginConfig pluginConfig;
    PluginDatabaseInfo dbInfo = new PluginDatabaseInfo();
    PluginDatabase dbHelper;
    MailCore mailCore;

    @EventPluginConfig(EventType.LOAD)
    public void onLoadConfig(FileConfiguration cfg) {
        mailCore.setDbHelper(null);
        mailCore.setDbInfo(null);

        if (dbHelper != null) {
            try {
                dbHelper.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            dbHelper = null;
        }

        dbInfo.setIp(cfg.getString("connect.ip", ""));
        dbInfo.setDb(cfg.getString("connect.db", ""));
        dbInfo.setLogin(cfg.getString("connect.login", ""));
        dbInfo.setPassword(cfg.getString("connect.password", ""));
        dbInfo.setTable(cfg.getString("connect.table", ""));

        // коннектимся
        try {
            // создаем экземпляр класса для соединения с БД
            dbHelper = new PluginDatabase(
                dbInfo.getIp(),
                dbInfo.getDb(),
                dbInfo.getLogin(),
                dbInfo.getPassword()
            );

            dbHelper.connect();
            mailCore.setDbHelper(dbHelper);
            mailCore.setDbInfo(dbInfo);
            getLogger().info("Success connect to MySQL!");
        } catch (SQLException e) {
            dbHelper = null;
            e.printStackTrace();
        }
    }

    @Override
    public void onDisable() {
        if (pluginConfig != null) {
            pluginConfig.saveData();
        }
        if (dbHelper != null) {
            try {
                dbHelper.disconnect();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            dbHelper = null;
        }
    }

    @Override
    public void onEnable() {
        mailCore = new MailCore(this);
        mailCore.setRandomChestExists(new PluginChecker(this).check("RandomChest", false));

        pluginConfig = new PluginConfig(this);
        pluginConfig.addEventClasses(this);
        pluginConfig.setup();
        pluginConfig.loadData();

        getCommand("mailbox").setExecutor(mailCore);
        getServer().getPluginManager().registerEvents(mailCore, this);
    }

    public static String getErrorMessage(String message) {
        return ChatColor.BOLD + "[Mailbox] " + ChatColor.RESET + ChatColor.RED + message;
    }

    public static String getNormalMessage(String message) {
        return ChatColor.BOLD + "[Mailbox] " + ChatColor.RESET + ChatColor.YELLOW + message;
    }
}
