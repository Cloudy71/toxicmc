/*
  User: Cloudy
  Date: 22/01/2022
  Time: 17:15
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.annotations.ActionListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cron;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.maps.MapCanvas;
import cz.cloudy.minecraft.core.scoreboard.Scoreboard;
import cz.cloudy.minecraft.core.scoreboard.ScoreboardObject;
import cz.cloudy.minecraft.core.scoreboard.fields.ObjectiveScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.fields.ScoreScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.fields.SpaceScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.logics.ChangeBasedScoreboardLogic;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.messengersystem.ChatComponent;
import cz.cloudy.minecraft.messengersystem.MessengerComponent;
import cz.cloudy.minecraft.messengersystem.MessengerConstants;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * @author Cloudy
 */
// TODO: Fake employees
// TODO: Daily salary
@Component
public class EconomicsComponent
        implements IComponent {
    private static final Logger logger = LoggerFactory.getLogger(EconomicsComponent.class);

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

    private final DecimalFormat decimalFormat = new DecimalFormat("0.00");

    public BankAccount getBankAccount(Player player) {
        return database.findEntity(BankAccount.class, player.getUniqueId());
    }

    @Cron("0 0 0 * * *")
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
            transactionManager.send(
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
                    employee.getSalary()
            );
        }

        for (PlayerEmployee employee : database.findEntities(
                PlayerEmployee.class,
                null,
                null,
                FetchLevel.Primitive
        )) {
            transactionManager.send(
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
        if (employee != null)
            data.player().setMetadata(ToxicConstants.PLAYER_EMPLOYEE, new FixedMetadataValue(getPlugin(), employee));

        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální stav účtu: $" + ChatColor.RESET + bankAccount.getTotalBalance());
        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální zaměstnání: " + ChatColor.RESET +
                                  (employee != null ? employee.getCompany().getName() : "Žádné"));

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
                                       22L * ((long) Bukkit.getServer().getTPS()[0]);
                            }

                            @Override
                            public List<String> getDataList() {
                                return List.of(
                                        Long.toString(finalBankAccount.getTotalBalance()),
                                        employee != null ? employee.getCompany().getName() : "-",
                                        employee != null ? Long.toString(employee.getCompany().getBankAccount().getTotalBalance()) : "-",
                                        employee != null ? Integer.toString(employee.getCompany().getExpenses().stream()
                                                                                    .mapToInt(Expense::getAmount)
                                                                                    .sum()) : "-",
                                        employee != null ? "1" : "-",
                                        decimalFormat.format(Bukkit.getServer().getTPS()[0])
                                );
                            }
                        }
                )
                        .add(new ObjectiveScoreboardField("private", ChatColor.DARK_GREEN + "ToxicMc"))
                        .add(new ScoreScoreboardField(ChatColor.AQUA + "" + ChatColor.BOLD + "Soukromá ekonomika:"))
                        .add(new ScoreScoreboardField(ChatColor.YELLOW + "Stav účtu: " + ChatColor.RESET + "${0}"))
                        .add(new SpaceScoreboardField())
                        .add(new SpaceScoreboardField())
                        .add(new ScoreScoreboardField(ChatColor.AQUA + "" + ChatColor.BOLD + "Firemní ekonomika:"))
                        .add(new ScoreScoreboardField(ChatColor.YELLOW + "Název: " + ChatColor.RESET + "{1}"))
                        .add(new ScoreScoreboardField(ChatColor.YELLOW + "Stav účtu: " + ChatColor.RESET + "${2}"))
                        .add(new ScoreScoreboardField(ChatColor.YELLOW + "Výdaje: " + ChatColor.RESET + "${3}"))
                        .add(new ScoreScoreboardField(ChatColor.YELLOW + "Počet zaměstnanců: " + ChatColor.RESET + "{4}"))
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

    @CommandListener("company")
    private Object onCompanyCommand(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");
        String sub = data.arguments()[0];
        CommandData commandData = new CommandData(data.sender(), data.command(), Arrays.copyOfRange(data.arguments(), 1, data.arguments().length));
        if (sub.equals("create"))
            return onCompanyCreate(commandData);
        if (sub.equals("banner"))
            return onCompanyBanner(commandData);
        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onCompanyCreate(CommandData data) {
        if (data.getPlayer().hasMetadata(ToxicConstants.PLAYER_EMPLOYEE))
            return new ErrorCommandResponse("Aktuálně máš společnost nebo jsi zaměstnaný.");
        String name = String.join(" ", data.arguments());

        Company company = database.findEntity(
                Company.class,
                "name = :name",
                ImmutableMap.of("name", name),
                FetchLevel.None
        );
        if (company != null)
            return new ErrorCommandResponse("Společnost s názvem \"" + name + "\" již existuje.");

        UserAccount userAccount = (UserAccount) data.getPlayer().getMetadata(MessengerConstants.USER_ACCOUNT).get(0).value();

        company = new Company();
        company.setUuid(UUID.nameUUIDFromBytes(("company-" + name).getBytes(StandardCharsets.UTF_8)));
        company.setCreator(userAccount);
        company.setName(name);
        company.save();

        BankAccount bankAccount = new BankAccount();
        bankAccount.setUuid(company.getUuid());
        bankAccount.save();

        PlayerEmployee employee = new PlayerEmployee();
        employee.setEmployee(userAccount);
        employee.setLevel(PlayerEmployee.LEVEL_OWNER);
        employee.setCompany(company);
        employee.save();
        data.getPlayer().setMetadata("employee", new FixedMetadataValue(getPlugin(), employee));

        return new InfoCommandResponse(
                "Tvá nová společnost byla úspěšně vytvořena.\nPokud chceš svoji společnost spravovat, použij příkaz /company manage help");
    }

    private Object onCompanyBanner(CommandData data) {
        Entity targetEntity = data.getPlayer().getTargetEntity(4);
        if (!(targetEntity instanceof ItemFrame itemFrame))
            return new InfoCommandResponse("Před použitím příkazu prosím zamiř na levý horní item frame.");
//        if (itemFrame.getItem().getType() != Material.AIR)
//            return new InfoCommandResponse("Item frame musí být prázdný.");
        if (!Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH).contains(itemFrame.getAttachedFace()))
            return new InfoCommandResponse("Item frame musí být v horizontální poloze.");

        PlayerEmployee employee;
        if (!data.getPlayer().hasMetadata(ToxicConstants.PLAYER_EMPLOYEE) ||
            (employee = (PlayerEmployee) data.getPlayer().getMetadata(ToxicConstants.PLAYER_EMPLOYEE).get(0).value()).getLevel() != PlayerEmployee.LEVEL_OWNER)
            return new ErrorCommandResponse("Aktuálně nemáš společnost.");

        Banner banner = database.findEntity(
                Banner.class,
                "owner = :owner",
                ImmutableMap.of("owner", employee.getCompany().getUuid().toString()),
                FetchLevel.Primitive
        );

        if (itemFrame.getItem().getType() != Material.AIR) {

        } else {
            List<ItemFrame> itemFrames;
            if (banner == null) {
//                if (data.arguments().length != 1)
//                    return new InfoCommandResponse("Prosím vyplň číslo banneru.");
                String fileName = employee.getCompany().getUuid().toString();
                Int2 size = bannerComponent.getBannerSize(fileName);
                if (size == null)
                    return new ErrorCommandResponse("Banner se nepodařilo vytvořit");
                itemFrames = bannerComponent.mapAllItemFrames(itemFrame, size);
                if (itemFrames == null)
                    return new InfoCommandResponse("Nelze vyplnit, nedostatek item framu.");
                int price = PriceConst.COMPANY_BANNER[size.getX() - 1][size.getY() - 1];
                if (!transactionManager.hasBalance(employee.getCompany().getUuid(), price))
                    return new ErrorCommandResponse("Nemáš dostatek financí! Potřebuješ $" + price);
                banner = bannerComponent.createBanner(data.getPlayer(), employee.getCompany(), data.arguments()[0]);
                if (banner == null)
                    return new ErrorCommandResponse("Banner se nepodařilo vytvořit");
                transactionManager.pay(
                        employee.getCompany().getUuid(),
                        PaymentType.COMPANY_BANNER.getMessage(),
                        price
                );
            } else {
                Int2 size = bannerComponent.getBannerSize(banner.getImagePath());
                if (size == null)
                    return new ErrorCommandResponse("Banner se nepodařilo vytvořit");
                itemFrames = bannerComponent.mapAllItemFrames(itemFrame, size);
                if (itemFrames == null)
                    return new InfoCommandResponse("Nelze vyplnit, nedostatek item framu.");
            }
            bannerComponent.redrawBanner(banner);
            bannerComponent.placeBanner(banner, itemFrames);
        }

        return new InfoCommandResponse("OK!");
    }
}
