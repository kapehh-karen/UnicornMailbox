package me.kapehh.UnicornMailbox;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;

import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by Karen on 08.04.2015.
 */
public class Main extends JavaPlugin {
    byte[] tmp = null;

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
                ItemStack itemStack;
                try {
                    if (tmp == null) {
                        itemStack = player.getItemInHand();
                        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                        BukkitObjectOutputStream bukkitObjectOutputStream = new BukkitObjectOutputStream(byteArrayOutputStream);
                        bukkitObjectOutputStream.writeObject(itemStack);
                        bukkitObjectOutputStream.flush();
                        bukkitObjectOutputStream.close();
                        tmp = byteArrayOutputStream.toByteArray();
                        player.sendMessage("Geted: " + itemStack);
                    } else {
                        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(tmp);
                        BukkitObjectInputStream bukkitObjectInputStream = new BukkitObjectInputStream(byteArrayInputStream);
                        itemStack = (ItemStack) bukkitObjectInputStream.readObject();
                        player.setItemInHand(itemStack);
                        player.sendMessage("Seted: " + itemStack);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }
}
