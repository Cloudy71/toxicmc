/*
  User: Cloudy
  Date: 09/02/2022
  Time: 02:29
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.game.EntityUtils;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.messengersystem.MessengerConstants;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.event.Listener;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Component
public class CompanyComponent
        implements IComponent, Listener {

    @Component
    private Database database;

    @Component
    private BannerComponent bannerComponent;

    @Component
    private TransactionManager transactionManager;

    @Component
    private EntityUtils entityUtils;

    public CompanyArea getCompanyAreaFromVector(Company company, Vector vector) {
        Set<CompanyArea> companyAreaSet = database.findEntities(
                CompanyArea.class,
                "company.uuid = :uuid",
                ImmutableMap.of("uuid", company.getUuid().toString()),
                FetchLevel.Primitive
        );

        for (CompanyArea companyArea : companyAreaSet) {
            if (!companyArea.isVectorInArea(vector))
                continue;

            return companyArea;
        }

        return null;
    }

    public CompanyArea getAnyCompanyAreaFromVector(Vector vector) {
        for (CompanyArea companyArea : database.findEntities(CompanyArea.class)) {
            if (!companyArea.isVectorInArea(vector))
                continue;

            return companyArea;
        }

        return null;
    }

    public boolean isVectorInCompanyArea(Company company, Vector vector) {
        return getCompanyAreaFromVector(company, vector) != null;
    }

    public boolean isVectorInAnyCompanyArea(Vector vector) {
        return getAnyCompanyAreaFromVector(vector) != null;
    }

    @CommandListener("company")
    private Object onCompanyCommand(CommandData data) {
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");
        String sub = data.arguments()[0];
        CommandData commandData = new CommandData(data.sender(), data.command(), Arrays.copyOfRange(data.arguments(), 1, data.arguments().length));
        if (sub.equals("create"))
            return onCompanyCreate(commandData);
        if (sub.equals("manage"))
            return onCompanyManage(commandData);
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

    private Object onCompanyManage(CommandData data) {
        if (!data.getPlayer().hasMetadata(ToxicConstants.PLAYER_EMPLOYEE))
            return new ErrorCommandResponse("Aktuálně nemáš nebo nejsi ve společnosti.");
        if (data.arguments().length < 1)
            return new InfoCommandResponse("Specifikuj požadavek.");
        String sub = data.arguments()[0];
        CommandData commandData = new CommandData(data.sender(), data.command(), Arrays.copyOfRange(data.arguments(), 1, data.arguments().length));
        if (sub.equals("banner"))
            return onCompanyManageBanner(commandData);
        if (sub.equals("area"))
            return onCompanyManageArea(commandData);
        return new InfoCommandResponse("Neznámý požadavek.");
    }

    private Object onCompanyManageBanner(CommandData data) {
        Entity targetEntity = data.getPlayer().getTargetEntity(4);
        if (!(targetEntity instanceof ItemFrame itemFrame))
            return new InfoCommandResponse("Před použitím příkazu prosím zamiř na levý horní item frame.");
//        if (itemFrame.getItem().getType() != Material.AIR)
//            return new InfoCommandResponse("Item frame musí být prázdný.");
        if (!Arrays.asList(BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH).contains(itemFrame.getAttachedFace()))
            return new InfoCommandResponse("Item frame musí být v horizontální poloze.");

        PlayerEmployee employee;
        if ((employee = entityUtils.getMetadata(data.getPlayer(), ToxicConstants.PLAYER_EMPLOYEE)).getLevel() != PlayerEmployee.LEVEL_OWNER)
            return new ErrorCommandResponse("Nejsi vlastníkem společnosti");
        if (!isVectorInCompanyArea(employee.getCompany(), targetEntity.getLocation().toVector()))
            return new ErrorCommandResponse("Tento item frame musí ležet na pozemku společnosti.");

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
                    return new ErrorCommandResponse("Nemáš dostatek financí! Potřebuješ " + transactionManager.printMoney(price));
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

    //    @Async
    private Object onCompanyManageArea(CommandData data) {
        PlayerEmployee employee;
        if ((employee = entityUtils.getMetadata(data.getPlayer(), ToxicConstants.PLAYER_EMPLOYEE)).getLevel() != PlayerEmployee.LEVEL_OWNER)
            return new ErrorCommandResponse("Nejsi vlastníkem společnosti");

        Int2 size = new Int2(16, 16);
        int expenseAmount = (int) Math.ceil(size.getProduct() * PriceConst.AREA_BLOCK_EXPENSE);
        transactionManager.confirm(
                data.getPlayer(),
//                employee.getCompany().getUuid(),
                TransactionManager.BANK,
                /*6400*/size.getProduct() * PriceConst.AREA_BLOCK,
                size.getX() + "x" + size.getY() + " firemní areál\n" + ChatColor.GRAY + "Výdaje: " + ChatColor.WHITE + "+ " +
                transactionManager.printMoney(expenseAmount) + "\n" +
                "Po zrušení areálu bude vyplacena záloha vrácena.",
                PaymentType.COMPANY_AREA,
                player -> {
                    Expense expense = new Expense();
                    expense.setUuid(employee.getCompany().getUuid());
                    expense.setExpenseType(PaymentType.COMPANY_AREA);
                    expense.setAmount(expenseAmount);
                    expense.save();
                },
                player -> {}
        );
        return new InfoCommandResponse("OK AREA!");
    }
}
