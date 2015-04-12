package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.mailbox.MailSender;
import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.utils.PlayerUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;

/**
 * Created by Karen on 12.04.2015.
 */
public class MailCommandExecutor implements CommandExecutor {
    PluginDatabase dbHelper;

    public PluginDatabase getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(PluginDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        Player player = (Player) sender;
        if (args.length < 2) {
            return false;
        }
        try {
            if (args[0].equalsIgnoreCase("send")) {
                /*Player to = PlayerUtil.getOnlinePlayer(args[1]);
                if (to == null) {
                    player.sendMessage("Sorri, player ne nayden!");
                    return true;
                }*/
                MailSender.sendMail(dbHelper, player.getItemInHand(), player.getName(), args[1]);
                player.setItemInHand(null);
                player.sendMessage("Otpravleno!");
            } else if (args[0].equalsIgnoreCase("receiv")) {
                int id = Integer.parseInt(args[1]);
                ItemStack itemStack = MailSender.receiveMail(dbHelper, id);
                player.setItemInHand(itemStack);
                player.sendMessage("Polucheno!!");
            } else {
                player.sendMessage("Takoy argument not found...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
