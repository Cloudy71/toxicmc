/*
  User: Cloudy
  Date: 22/01/2022
  Time: 17:15
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.ActionListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.WorldOnly;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.interactions.InteractiveInventory;
import cz.cloudy.minecraft.core.items.ItemStackBuilder;
import cz.cloudy.minecraft.core.maps.MapCanvas;
import cz.cloudy.minecraft.core.scoreboard.Scoreboard;
import cz.cloudy.minecraft.core.scoreboard.ScoreboardObject;
import cz.cloudy.minecraft.core.scoreboard.fields.ObjectiveScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.fields.ScoreScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.fields.SpaceScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.logics.ChangeBasedScoreboardLogic;
import cz.cloudy.minecraft.messengersystem.ChatComponent;
import cz.cloudy.minecraft.messengersystem.MessengerComponent;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import java.text.DecimalFormat;
import java.util.List;

/**
 * @author Cloudy
 */
// TODO: Fake employees
// TODO: Daily salary
@Component
@WorldOnly(filter = ToxicConstants.WORLDS_ECONOMY)
public class EconomyComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(EconomyComponent.class);

    @Component
    private Database database;

    @Component
    private ChatComponent chat;

    @Component
    private MessengerComponent messenger;

    @Component
    private Scoreboard scoreboard;

    @Component
    private TransactionManager transactionManager;

    @Component
    private MapCanvas mapCanvas;

    @Component
    private BannerComponent bannerComponent;

    @Component
    private InteractiveInventory interactiveInventory;

    @Component
    private ItemStackBuilder itemStackBuilder;

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public BankAccount getBankAccount(Player player) {
        return database.findEntity(BankAccount.class, player.getUniqueId());
    }

    @Override
    public void onStart() {
    }

    //    @Cron("0 0 0 * * *")
    private void everydayPays() {
        for (Expense expense : database.findEntities(
                Expense.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
            transactionManager.pay(
                    expense.getUuid(),
                    expense.getExpenseType().getMessage(),
                    expense.getAmount()
            );
        }

        for (Contract contract : database.findEntities(
                Contract.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
            transactionManager.pay(
                    contract.getUuidFrom(),
                    contract.getUuidTo(),
                    PaymentType.CONTRACT.getMessage(),
                    contract.getAmount()
            );
        }

        for (Employee employee : database.findEntities(
                Employee.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
            transactionManager.send(
                    employee.getCompany().getUuid(),
                    TransactionManager.BANK,
                    PaymentType.COMPANY_WORKER.getMessage(),
                    employee.getSalary(),
                    0
            );
        }

        for (PlayerEmployee employee : database.findEntities(
                PlayerEmployee.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
            transactionManager.pay(
                    employee.getCompany().getUuid(),
                    employee.getEmployee().getUuid(),
                    PaymentType.COMPANY_WORKER.getMessage(),
                    employee.getSalary()
            );
        }
    }

    @ActionListener(value = "MessengerSystem.onLogin", priority = 9)
    private void onLogin(LoginEventData data) {
        BankAccount bankAccount = getBankAccount(data.player());
        if (bankAccount == null) {
            bankAccount = new BankAccount();
            bankAccount.setUuid(data.account().getUuid());
            bankAccount.setBalance(0);
            bankAccount.setDept(0);
            bankAccount.save();
        }

        PlayerEmployee employee = database.findEntity(
                PlayerEmployee.class,
                "employee.uuid = :uuid",
                ImmutableMap.of("uuid", data.player().getUniqueId().toString()),
                FetchLevel.Full
        );
        if (employee != null) {
            data.player().setMetadata(ToxicConstants.PLAYER_EMPLOYEE, new FixedMetadataValue(getPlugin(), employee));
            if (employee.getLevel() == PlayerEmployee.LEVEL_OWNER)
                employee.getCompany().getExpenses();
        }

        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktu??ln?? stav ????tu: " + ChatColor.RESET +
                                  transactionManager.printMoney(bankAccount.getTotalBalance()));
        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktu??ln?? zam??stn??n??: " + ChatColor.RESET +
                                  (employee != null ? employee.getCompany().getName() : "????dn??"));

        final BankAccount finalBankAccount = bankAccount;
        scoreboard.addScoreboard(
                data.player(),
                new ScoreboardObject(
                        "economy",
                        new ChangeBasedScoreboardLogic() {
                            @Override
                            public long calculateHash() {
                                return finalBankAccount.getTotalBalance() +
                                       21L * (employee != null ? employee.getCompany().getBankAccount().getTotalBalance() : 0) +
                                       22L * ((int) Bukkit.getServer().getTPS()[0]);
                            }

                            @Override
                            public List<String> getDataList() {
                                return List.of(
                                        transactionManager.printMoney(finalBankAccount.getTotalBalance()),
                                        employee != null ? employee.getCompany().getName() : "-",
                                        employee != null ? transactionManager.printMoney(employee.getCompany().getBankAccount().getTotalBalance()) : "-",
                                        employee != null ? transactionManager.printMoney(employee.getCompany().getExpenses().stream()
                                                                                                 .mapToInt(Expense::getAmount)
                                                                                                 .sum()) : "-",
                                        employee != null ? "1" : "-",
                                        decimalFormat.format(Bukkit.getServer().getTPS()[0])
                                );
                            }
                        }
                ).add(new ObjectiveScoreboardField("private", ChatColor.DARK_GREEN + "ToxicMc"))
                 .add(new ScoreScoreboardField(ChatColor.AQUA + "" + ChatColor.BOLD + "Soukrom?? ekonomika:"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "Stav ????tu: " + ChatColor.RESET + "{0}"))
                 .add(new SpaceScoreboardField())
                 .add(new SpaceScoreboardField())
                 .add(new ScoreScoreboardField(ChatColor.AQUA + "" + ChatColor.BOLD + "Firemn?? ekonomika:"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "N??zev: " + ChatColor.RESET + "{1}"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "Stav ????tu: " + ChatColor.RESET + "{2}"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "V??daje: " + ChatColor.RESET + "{3}"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "Po??et zam??stnanc??: " + ChatColor.RESET + "{4}"))
                 .add(new SpaceScoreboardField())
                 .add(new SpaceScoreboardField())
                 .add(new ScoreScoreboardField(ChatColor.AQUA + "" + ChatColor.BOLD + "Server:"))
                 .add(new ScoreScoreboardField(ChatColor.YELLOW + "TPS: " + ChatColor.RESET + "{5}"))
        );
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        e.getPlayer().removeMetadata(ToxicConstants.PLAYER_EMPLOYEE, getPlugin());
    }
}
