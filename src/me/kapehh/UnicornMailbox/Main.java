package me.kapehh.UnicornMailbox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.beans.XMLEncoder;
import java.io.IOException;

/**
 * Created by Karen on 08.04.2015.
 */
public class Main extends JavaPlugin {
    @Override
    public void onDisable() {
        super.onDisable();
    }

    @Override
    public void onEnable() {
        getCommand("mailbox").setExecutor(new CommandExecutor() {
            @Override
            public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
                Player player = (Player) sender;
                ItemStack itemStack = player.getItemInHand();
                try {
                    BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(System.out);
                    bukkitObjectOutputStream.writeObject(itemStack);
                    bukkitObjectOutputStream.flush();
                    //bukkitObjectOutputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }
}
