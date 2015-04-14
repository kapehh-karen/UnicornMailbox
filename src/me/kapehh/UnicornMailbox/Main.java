package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.config.EventPluginConfig;
import me.kapehh.main.pluginmanager.config.EventType;
import me.kapehh.main.pluginmanager.config.PluginConfig;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseInfo;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Created by Karen on 08.04.2015.
 */
public class Main extends JavaPlugin {
    PluginConfig pluginConfig;
    PluginDatabaseInfo dbInfo = new PluginDatabaseInfo();
    PluginDatabase dbHelper;
    MailCommandExecutor mailCommandExecutor;

    @EventPluginConfig(EventType.LOAD)
    public void onLoadConfig(FileConfiguration cfg) {
        mailCommandExecutor.setDbHelper(null);

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
            mailCommandExecutor.setDbHelper(dbHelper);
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
        mailCommandExecutor = new MailCommandExecutor();

        pluginConfig = new PluginConfig(this);
        pluginConfig.addEventClasses(this);
        pluginConfig.setup();
        pluginConfig.loadData();

        getCommand("mailbox").setExecutor(mailCommandExecutor);
    }

    public static String getErrorMessage(String message) {
        return ChatColor.DARK_RED + "[Mailbox] " + message;
    }
}
