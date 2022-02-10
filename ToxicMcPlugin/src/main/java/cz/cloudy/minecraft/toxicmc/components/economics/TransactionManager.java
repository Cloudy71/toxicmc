/*
  User: Cloudy
  Date: 31/01/2022
  Time: 03:03
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.base.Preconditions;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.game.TextUtils;
import cz.cloudy.minecraft.core.interactions.InteractiveInventory;
import cz.cloudy.minecraft.core.interactions.interfaces.IInteractiveInventoryClickHandler;
import cz.cloudy.minecraft.core.items.ItemStackBuilder;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BankAccount;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BankTransaction;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.Company;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Component
public class TransactionManager
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(TransactionManager.class);

    public static final UUID BANK = UUID.nameUUIDFromBytes("__TOXIC_BANK__".getBytes(StandardCharsets.UTF_8));

    @Component
    private Database database;

    @Component
    private EconomicsComponent economics;

    @Component
    private InteractiveInventory interactiveInventory;

    @Component
    private ItemStackBuilder itemStackBuilder;

    private float tax = .15f;

    @Override
    public void onStart() {
        Company bankCompany = database.findEntity(Company.class, BANK);
        if (bankCompany == null) {
            bankCompany = new Company();
            bankCompany.setName("Toxic Bank");
            bankCompany.setUuid(BANK);
            bankCompany.setCreator(null);
            bankCompany.save();

            BankAccount bankAccount = new BankAccount();
            bankAccount.setUuid(BANK);
            bankAccount.setBalance(0);
            bankAccount.setDept(0);
            bankAccount.save();

            logger.info("Created Toxic Bank company");
        }

        tax = (float) getPlugin().getConfig().getDouble("toxic.tax", tax);
    }

    public String printMoney(long amount) {
        boolean isNegative = amount < 0;
        String money = Long.toString(Math.abs(amount));
        int len = money.length();
        for (int i = len - 3; i > 0; i -= 3) {
            money = money.substring(0, i) + "," + money.substring(i);
        }
        return (isNegative ? "-" : "") + "$" + money;
    }

    public String printMoney(int amount) {
        return printMoney((long) amount);
    }

    public void send(UUID source, UUID target, String message, int amount, int tax) {
        if (amount <= 0)
            return;

        BankAccount bankAccountSource = database.findEntity(BankAccount.class, source);
        BankAccount bankAccountTarget = database.findEntity(BankAccount.class, target);
        Preconditions.checkNotNull(bankAccountSource);
        Preconditions.checkNotNull(bankAccountTarget);

        BankTransaction bankTransaction = new BankTransaction();
        bankTransaction.setFromUuid(source);
        bankTransaction.setToUuid(target);
        bankTransaction.setMessage(message);
        bankTransaction.setAmount(amount);
        bankTransaction.setTax(tax);
        bankTransaction.save();

        bankAccountSource.setBalance(bankAccountSource.getBalance() - amount);
        bankAccountSource.save();

        bankAccountTarget.setBalance(bankAccountTarget.getBalance() + amount - tax);
        bankAccountTarget.save();

        if (tax > 0) {
            BankAccount bankAccountBank = database.findEntity(BankAccount.class, BANK);
            bankAccountBank.setBalance(bankAccountBank.getBalance() + tax);
            bankAccountBank.save();
        }

        logger.info("Payment: {} => {} ({}-{}, {})", source, target, amount, tax, message);
    }

    public void pay(UUID source, UUID target, String message, int amount) {
        if (target.equals(BANK)) {
            send(source, BANK, message, amount, 0);
            return;
        }

        // This has tax calculation
        int targetAmount = (int) Math.ceil(amount * (1f - tax));
        int taxAmount = amount - targetAmount;

        send(source, target, message, amount, taxAmount);
    }

    public void pay(UUID source, String message, int amount) {
        pay(source, BANK, message, amount);
    }

    public long getBalance(UUID source) {
        BankAccount bankAccount = database.findEntity(BankAccount.class, source);
        return bankAccount.getTotalBalance();
    }

    public boolean hasBalance(UUID source, int amount) {
        return getBalance(source) >= amount;
    }

    public void confirm(Player player, UUID source, UUID target, int amount, String text, PaymentType paymentType,
                        IInteractiveInventoryClickHandler acceptHandler,
                        IInteractiveInventoryClickHandler refuseHandler) {
        Company sourceCompany = !source.equals(player.getUniqueId()) ? database.findEntity(Company.class, source) : null;
        Company targetCompany = database.findEntity(Company.class, target);

        String sourceName = source.equals(player.getUniqueId()) ? player.getName() : sourceCompany.getName();
        String targetName = targetCompany.getName();
        String title =
                ChatColor.BOLD + "Transakce" + ChatColor.RESET + " (" + sourceName + ")";
        long balance = getBalance(source);
        boolean hasBalance = balance >= amount;
        List<net.kyori.adventure.text.Component> textList = new ArrayList<>();
        for (String txt : text.split("\n")) {
            textList.add(TextUtils.get(ChatColor.WHITE + txt));
        }
        ItemStack green = itemStackBuilder.create()
                                          .material(hasBalance ? Material.GREEN_STAINED_GLASS_PANE : Material.BLACK_STAINED_GLASS_PANE)
                                          .itemMeta(itemMeta -> itemMeta.displayName(
                                                  TextUtils.get((hasBalance ? ChatColor.DARK_GREEN : ChatColor.GRAY) + "Přijmout")))
                                          .build();
        ItemStack paper = itemStackBuilder.create()
                                          .material(Material.PAPER)
                                          .itemMeta(itemMeta -> {
                                              itemMeta.displayName(TextUtils.get(
                                                      ChatColor.GRAY + "Transakce"));
//                                              List<net.kyori.adventure.text.Component> lore = new ArrayList<>(List.of(
//                                                      TextUtils.get(ChatColor.WHITE + text),
//                                                      TextUtils.get(" ")
//                                              ));
                                              List<net.kyori.adventure.text.Component> lore = new ArrayList<>(textList);
                                              lore.add(TextUtils.get(" "));
                                              if (targetName != null)
                                                  lore.add(TextUtils.get(ChatColor.GRAY + "Cíl: " + ChatColor.WHITE + targetName));
                                              lore.addAll(List.of(
                                                      TextUtils.get(ChatColor.GRAY + "Cena: " + ChatColor.WHITE + printMoney(amount)),
                                                      TextUtils.get(" "),
                                                      TextUtils.get(ChatColor.GRAY + "Zdroj: " + ChatColor.WHITE + sourceName),
                                                      TextUtils.get(ChatColor.GRAY + "Zbytek: " + (hasBalance ? ChatColor.WHITE : ChatColor.RED) +
                                                                    printMoney(balance - amount))
                                              ));
                                              itemMeta.lore(lore);
                                          })
                                          .build();
        ItemStack red = itemStackBuilder.create()
                                        .material(Material.RED_STAINED_GLASS_PANE)
                                        .itemMeta(itemMeta -> itemMeta.displayName(TextUtils.get(ChatColor.DARK_RED + "Zamítnout")))
                                        .build();
        IInteractiveInventoryClickHandler accept = p -> {
            // TODO: Calculate amount etc...
            pay(source, target, paymentType.getMessage(), amount);
            acceptHandler.onClick(p);
            interactiveInventory.destroyOnetimeInventory(title, player);
            player.sendMessage(ChatColor.DARK_GREEN + "Transakce proběhla úspěšně.");
        };
        IInteractiveInventoryClickHandler reject = p -> {
            refuseHandler.onClick(p);
            interactiveInventory.destroyOnetimeInventory(title, player);
            player.sendMessage(ChatColor.DARK_RED + "Transakce byla ukončena.");
        };
        interactiveInventory.openOnetimeInventory(9, title, player)
                            .addButton(green, 0, accept)
                            .addButton(green, 1, accept)
                            .addButton(green, 2, accept)
                            .addButton(green, 3, accept)
                            .addButton(paper, 4, null)
                            .addButton(red, 5, reject)
                            .addButton(red, 6, reject)
                            .addButton(red, 7, reject)
                            .addButton(red, 8, reject);
    }

    public void confirm(Player player, UUID target, int amount, String text, PaymentType paymentType, IInteractiveInventoryClickHandler acceptHandler,
                        IInteractiveInventoryClickHandler refuseHandler) {
        confirm(player, player.getUniqueId(), target, amount, text, paymentType, acceptHandler, refuseHandler);
    }
}
