package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.mailbox.MailPack;
import me.kapehh.UnicornMailbox.mailbox.MailSender;
import me.kapehh.UnicornMailbox.mailbox.PlayerInv;
import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseInfo;
import me.kapehh.main.pluginmanager.utils.PlayerUtil;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Karen on 12.04.2015.
 */
public class MailCore implements Listener, CommandExecutor {
    PluginDatabase dbHelper;
    PluginDatabaseInfo dbInfo;

    public PluginDatabase getDbHelper() {
        return dbHelper;
    }

    public void setDbHelper(PluginDatabase dbHelper) {
        this.dbHelper = dbHelper;
    }

    public PluginDatabaseInfo getDbInfo() {
        return dbInfo;
    }

    public void setDbInfo(PluginDatabaseInfo dbInfo) {
        this.dbInfo = dbInfo;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (dbHelper == null || dbInfo == null) {
            return;
        }

        Player player = playerJoinEvent.getPlayer();
        int countMails;
        try {
            countMails = MailSender.countMails(dbHelper, dbInfo, player.getName());
            if (countMails > 0) {
                player.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (dbHelper == null || dbInfo == null) {
            sender.sendMessage(Main.getErrorMessage("Ошибка соединения с БД"));
            return true;
        }

        Player player = (Player) sender;
        try {
            if (args.length >= 2 && args[0].equalsIgnoreCase("send")) {
                ItemStack itemStack = player.getItemInHand();
                if (MailSender.isCorrectItem(itemStack)) {
                    String namePlayerReceiver = args[1];
                    MailSender.sendMail(dbHelper, dbInfo, itemStack, player.getName(), namePlayerReceiver);
                    player.setItemInHand(null);
                    player.sendMessage(Main.getNormalMessage("Посылка отправлена!"));
                    Player playerReceiver = PlayerUtil.getOnlinePlayer(namePlayerReceiver, true);
                    if (playerReceiver != null) {
                        player.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                    }
                } else {
                    player.sendMessage(Main.getErrorMessage("Возьмите в руку предмет для отправки"));
                }
            } else if (args.length >= 1 && args[0].equalsIgnoreCase("receiv")) {
                int canItems = PlayerInv.getPlayerEmptySize(player);
                if (canItems > 0) {
                    MailPack mailPack = MailSender.receiveMails(dbHelper, dbInfo, player.getName(), canItems);
                    if (mailPack.getSize_pack() > 0) {
                        PlayerInv.putPlayerMailPack(player, mailPack);
                        player.sendMessage(Main.getNormalMessage("Получено " + mailPack.getSize_pack() + " из " + mailPack.getSize_all() + " посылок"));
                    } else {
                        player.sendMessage(Main.getNormalMessage("Нет посылок"));
                    }
                } else {
                    player.sendMessage(Main.getErrorMessage("В инвентаре нет места"));
                }
            } else {
                player.sendMessage(Main.getErrorMessage("Некорректные аргументы"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }
}
