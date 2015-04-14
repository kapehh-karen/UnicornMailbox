package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.mailbox.MailPack;
import me.kapehh.UnicornMailbox.mailbox.MailSender;
import me.kapehh.UnicornMailbox.mailbox.PlayerInv;
import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.utils.PlayerUtil;
import org.bukkit.ChatColor;
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
        if (dbHelper == null) {
            sender.sendMessage(Main.getErrorMessage("Ошибка соединения с БД."));
            return true;
        }

        Player player = (Player) sender;
        try {
            if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
                ItemStack itemStack = player.getItemInHand();
                if (MailSender.isCorrectItem(itemStack)) {
                    MailSender.sendMail(dbHelper, itemStack, player.getName(), args[1]);
                    player.setItemInHand(null);
                    player.sendMessage(ChatColor.GREEN + "Посылка отправлена!");
                } else {
                    player.sendMessage(Main.getErrorMessage("Возьмите в руку предмет для отправки."));
                }
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("receiv")) {
                int canItems = PlayerInv.getPlayerEmptySize(player);
                if (canItems > 0) {
                    MailPack mailPack = MailSender.receiveMails(dbHelper, player.getName(), canItems);
                    PlayerInv.putPlayerMailPack(player, mailPack);
                    player.sendMessage(ChatColor.YELLOW + "Получено " + mailPack.getSize_pack() + " из " + mailPack.getSize_all() + " посылок.");
                } else {
                    player.sendMessage(Main.getErrorMessage("В инвентаре нет места."));
                }
            } else {
                player.sendMessage(Main.getErrorMessage("Некорректные аргументы."));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
