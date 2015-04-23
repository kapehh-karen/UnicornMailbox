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

        try {
            // TODO: Remove
            if (sender instanceof Player) {
                pluginAsyncTimer.runTask(this, Integer.parseInt(args[0]), sender, ((Player) sender).getItemInHand());
                return true;
            }

            if ((args.length >= 2) && args[0].equalsIgnoreCase("send") && (sender instanceof Player)) {

                Player player = (Player) sender;
                ItemStack itemStack = player.getItemInHand();
                if (MailSender.isCorrectItem(itemStack)) {
                    String namePlayerReceiver = args[1];
                    MailSender.sendMail(dbHelper, dbInfo, itemStack, player.getName(), namePlayerReceiver);
                    player.setItemInHand(null);
                    player.sendMessage(Main.getNormalMessage("Посылка отправлена!"));
                    Player playerReceiver = PlayerUtil.getOnlinePlayer(namePlayerReceiver, true);
                    if (playerReceiver != null) {
                        playerReceiver.sendMessage(Main.getNormalMessage("Вам пришла посылка, забрать /mail receiv"));
                    }
                } else {
                    player.sendMessage(Main.getErrorMessage("Возьмите в руку предмет для отправки"));
                }

            } else if ((args.length >= 1) && args[0].equalsIgnoreCase("receiv") && (sender instanceof Player)) {

                Player player = (Player) sender;
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

            } else if (args.length >= 3 && args[0].equalsIgnoreCase("give") && sender.isOp()) {

                String namePlayerReceiver = args[1];
                String chestName = args[2];
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

            } else {
                sender.sendMessage(Main.getErrorMessage("Некорректные аргументы"));
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    @Override
    public Object doRun(int i, Object[] objects) throws Throwable {
        System.out.println("Test access " + objects[0]);
        System.out.println("Test access " + objects[1]);

        System.out.println("try cast");
        ItemStack itemStack = (ItemStack) objects[1];

        System.out.println("try get field: " + itemStack.getType());

        System.out.println("try serialize");
        byte[] bItem = ItemStackSerializer.toBytes(itemStack);

        System.out.println("try cast");
        Player player = (Player) objects[0];

        System.out.printf("try get field: " + player.getName());

        System.out.printf("try message");
        player.sendMessage(ChatColor.RED + "SWAAAG");

        System.out.println("success");
        return null;
    }

    @Override
    public void onSuccess(int id, Object o) {
        System.out.println("VSE OK: " + id);
    }

    @Override
    public void onFailure(int id, Throwable throwable) {
        System.out.println("Error in: " + id);
        throwable.printStackTrace();
    }
}
