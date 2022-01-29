/*
  User: Cloudy
  Date: 22/01/2022
  Time: 17:15
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.componentsystem.annotations.ActionListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.messengersystem.ChatComponent;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.messengersystem.types.LoginEventData;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BankAccount;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.Company;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.PlayerEmployee;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author Cloudy
 */
// TODO: Fake employees
// TODO: Daily salary
@Component
public class EconomicsComponent
        implements IComponent {

    @Component
    private Database database;

    @Component
    private ChatComponent messenger;

    @ActionListener(value = "MessengerSystem.onLogin", priority = 9)
    private void onLogin(LoginEventData data) {
        BankAccount bankAccount = database.findEntity(BankAccount.class, data.player().getUniqueId());
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
            data.player().setMetadata("employee", new FixedMetadataValue(getPlugin(), employee));

        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální stav účtu: " + ChatColor.RESET + bankAccount.getTotalBalance() + "˙Q");
        data.player().sendMessage(ChatColor.YELLOW + "" + ChatColor.BOLD + "Aktuální zaměstnání: " + ChatColor.RESET +
                                  (employee != null ? employee.getCompany().getName() : "Žádné"));
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        e.getPlayer().removeMetadata("employee", getPlugin());
    }

    @CommandListener("company")
    private Object onCompanyCommand(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");
        String sub = data.arguments()[0];
        CommandData commandData = new CommandData(data.sender(), data.command(), Arrays.copyOfRange(data.arguments(), 1, data.arguments().length));
        if (sub.equals("create"))
            return onCompanyCreate(commandData);
        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onCompanyCreate(CommandData data) {
        if (data.getPlayer().hasMetadata("employee"))
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

        UserAccount userAccount = (UserAccount) data.getPlayer().getMetadata("userAccount").get(0).value();

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
}
