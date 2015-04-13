package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.mailbox.MailPack;
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
import java.util.List;

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
        try {
            if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
                MailSender.sendMail(dbHelper, player.getItemInHand(), player.getName(), args[1]);
                player.setItemInHand(null);
                player.sendMessage("Otpravleno!");
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("receiv")) {
                MailPack mailPack = MailSender.receiveMails(dbHelper, player.getName(), 2);
                player.sendMessage(mailPack.toString());
            } else {
                player.sendMessage("Takoy argument not found...");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
