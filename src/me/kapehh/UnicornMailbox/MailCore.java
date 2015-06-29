package me.kapehh.UnicornMailbox;

import me.kapehh.UnicornMailbox.mailbox.MailPack;
import me.kapehh.UnicornMailbox.mailbox.MailSender;
import me.kapehh.UnicornMailbox.mailbox.PlayerInv;
import me.kapehh.UnicornMailbox.serialize.ItemStackSerializer;
import me.kapehh.main.RandomChest.config.ChestData;
import me.kapehh.main.RandomChest.config.ChestManager;
import me.kapehh.main.pluginmanager.constants.ConstantSystem;
import me.kapehh.main.pluginmanager.db.PluginDatabase;
import me.kapehh.main.pluginmanager.db.PluginDatabaseInfo;
import me.kapehh.main.pluginmanager.thread.IPluginAsyncTask;
import me.kapehh.main.pluginmanager.thread.PluginAsyncTimer;
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
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Karen on 12.04.2015.
 */
public class MailCore implements Listener, CommandExecutor, IPluginAsyncTask {
    private static final int ACTION_SEND = 1;
    private static final int ACTION_RECEIV = 2;
    private static final int ACTION_GIVE = 3;
    private static final int ACTION_CHECK_MAILS = 4;
    private static final int ACTION_CUSTOM_SEND = 5;

    PluginAsyncTimer pluginAsyncTimer;
    PluginDatabase dbHelper;
    PluginDatabaseInfo dbInfo;
    boolean randomChestExists;

    public MailCore(JavaPlugin plugin) {
        pluginAsyncTimer = new PluginAsyncTimer(plugin);
        pluginAsyncTimer.start(ConstantSystem.ticksPerSec);
    }

    public boolean isRandomChestExists() {
        return randomChestExists;
    }

    public void setRandomChestExists(boolean randomChestExists) {
        this.randomChestExists = randomChestExists;
    }

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

    public void sendItemsToPlayer(String to, String from, ItemStack[] content) {
        pluginAsyncTimer.runTask(this, ACTION_CUSTOM_SEND, to, from, content);
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PlayerJoinEvent playerJoinEvent) {
        if (dbHelper == null || dbInfo == null) {
            return;
        }

        pluginAsyncTimer.runTask(this, ACTION_CHECK_MAILS, playerJoinEvent.getPlayer());
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {
        if (dbHelper == null || dbInfo == null) {
            sender.sendMessage(Main.getErrorMessage("Ошибка соединения с БД"));
            return true;
        }

        if ((args.length >= 2) && args[0].equalsIgnoreCase("send") && (sender instanceof Player)) {
            pluginAsyncTimer.runTask(this, ACTION_SEND, sender, args[1]);
        } else if ((args.length >= 1) && args[0].equalsIgnoreCase("receiv") && (sender instanceof Player)) {
            pluginAsyncTimer.runTask(this, ACTION_RECEIV, sender);
        } else if (args.length >= 3 && args[0].equalsIgnoreCase("give") && sender.isOp()) {
            pluginAsyncTimer.runTask(this, ACTION_GIVE, sender, args[1], args[2]);
        } else {
            sender.sendMessage(Main.getErrorMessage("Некорректные аргументы"));
            return false;
        }
        return true;
    }

    @Override
    public Object doRun(int id, Object[] args) throws Throwable {
        Player player;

        switch (id) {
            case ACTION_CHECK_MAILS:

                player = (Player) args[0];
                int countMails = MailSender.countMails(dbHelper, dbInfo, player.getName());
                if (countMails > 0) {
                    player.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                }

                break;
            case ACTION_SEND:

                player = (Player) args[0];
                ItemStack itemStack = player.getItemInHand();
                if (MailSender.isCorrectItem(itemStack)) {
                    String namePlayerReceiver = (String) args[1];
                    player.setItemInHand(null);
                    MailSender.sendMail(dbHelper, dbInfo, itemStack, player.getName(), namePlayerReceiver);
                    player.sendMessage(Main.getNormalMessage("Посылка отправлена!"));
                    Player playerReceiver = PlayerUtil.getOnlinePlayer(namePlayerReceiver, true);
                    if (playerReceiver != null) {
                        playerReceiver.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                    }
                } else {
                    player.sendMessage(Main.getErrorMessage("Возьмите в руку предмет для отправки"));
                }

                break;
            case ACTION_RECEIV:

                player = (Player) args[0];
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

                break;
            case ACTION_GIVE:

                CommandSender sender = (CommandSender) args[0];
                String namePlayerReceiver = (String) args[1];
                String chestName = (String) args[2];
                if (randomChestExists) {
                    ChestManager chestManager = me.kapehh.main.RandomChest.Main.instance.chestManager;
                    ChestData chestData = chestManager.getChestDataFromName(chestName);
                    if (chestData == null) {
                        sender.sendMessage(Main.getErrorMessage("Сундук '" + chestName + "' в RandomChest не найден"));
                        return true;
                    }
                    ItemStack[] stacks = chestData.getContents();
                    MailSender.sendMails(dbHelper, dbInfo, stacks, '~' + chestName + '~', namePlayerReceiver);
                    sender.sendMessage(Main.getNormalMessage("Посылка отправлена!"));
                    Player playerReceiver = PlayerUtil.getOnlinePlayer(namePlayerReceiver, true);
                    if (playerReceiver != null) {
                        playerReceiver.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                    }
                } else {
                    sender.sendMessage(Main.getErrorMessage("Плагин RandomChest не был обнаружен при загрузке"));
                }

                break;

            case ACTION_CUSTOM_SEND:

                String to = (String) args[0];
                String from = (String) args[1];
                ItemStack[] content = (ItemStack[]) args[2];
                MailSender.sendMails(dbHelper, dbInfo, content, from, to);
                Player playerReceiver = PlayerUtil.getOnlinePlayer(to, true);
                if (playerReceiver != null) {
                    playerReceiver.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                }

                break;
        }
        return null;
    }

    @Override
    public void onSuccess(int id, Object res) {

    }

    @Override
    public void onFailure(int id, Throwable err) {
        err.printStackTrace();
    }
}
