/*
  User: Cloudy
  Date: 09/02/2022
  Time: 02:29
*/

package cz.cloudy.minecraft.toxicmc.components.economics;

import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.await.Await;
import cz.cloudy.minecraft.core.await.AwaitConsumer;
import cz.cloudy.minecraft.core.await.AwaitTimedConsumer;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cached;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.core.game.EntityUtils;
import cz.cloudy.minecraft.core.game.TextUtils;
import cz.cloudy.minecraft.core.items.ItemStackBuilder;
import cz.cloudy.minecraft.core.math.VectorUtils;
import cz.cloudy.minecraft.core.particles.ParticleJob;
import cz.cloudy.minecraft.core.particles.Particles;
import cz.cloudy.minecraft.core.types.Int2;
import cz.cloudy.minecraft.messengersystem.MessengerConstants;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.toxicmc.ToxicConstants;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.AreaType;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.PaymentType;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.*;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.util.Vector;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * @author Cloudy
 */
// TODO: Potential bug that creating company area bigger than other company area which is inside the one creating area might hide the one created
@Component
public class CompanyComponent
        implements IComponent, Listener {
    private static final Logger logger = LoggerFactory.getLogger(CompanyComponent.class);

    @Component
    private Database database;

    @Component
    private BannerComponent bannerComponent;

    @Component
    private TransactionManager transactionManager;

    @Component
    private EntityUtils entityUtils;

    @Component
    private ItemStackBuilder itemStackBuilder;

    @Component
    private Particles particles;

    @Cached
    public CompanyArea getSelectedCompanyAreaFromVector(Company company, Vector vector) {
        return database.findEntity(
                CompanyArea.class,
                "company.uuid = :uuid AND :x >= start_x AND :x <= end_x AND :z >= start_z AND :z <= end_z AND area_type = :type",
                ImmutableMap.of(
                        "uuid", company.getUuid().toString(),
                        "x", vector.getBlockX(),
                        "z", vector.getBlockZ(),
                        "type", AreaType.GLOBAL.getValue()
                ),
                FetchLevel.Primitive
        );
    }

    @Cached
    public CompanyArea getAnyCompanyAreaFromVector(Vector vector) {
        return database.findEntity(
                CompanyArea.class,
                ":x >= start_x AND :x <= end_x AND :z >= start_z AND :z <= end_z AND area_type = :type",
                ImmutableMap.of(
                        "x", vector.getBlockX(),
                        "z", vector.getBlockZ(),
                        "type", AreaType.GLOBAL.getValue()
                ),
                FetchLevel.Primitive
        );
    }

    @Cached
    public CompanyArea getAnyCompanyAreaFromChunkVector(Int2 vector) {
        return database.findEntity(
                CompanyArea.class,
                ":x >= FLOOR(start_x/16) AND :x <= FLOOR(end_x/16) AND :z >= FLOOR(start_z/16) AND :z <= FLOOR(end_z/16) AND area_type = :type",
                ImmutableMap.of(
                        "x", vector.getX(),
                        "z", vector.getY(),
                        "type", AreaType.GLOBAL.getValue()
                ),
                FetchLevel.Primitive
        );
    }

    public boolean isVectorInSelectedCompanyArea(Company company, Vector vector) {
        return getSelectedCompanyAreaFromVector(company, vector) != null;
    }

    public boolean isVectorInAnyCompanyArea(Vector vector) {
        return getAnyCompanyAreaFromVector(vector) != null;
    }

    public ItemStack giveManageTool(Player player, String usage) {
        ItemStack axe = itemStackBuilder.create()
                                        .material(Material.WOODEN_AXE)
                                        .itemMeta(Damageable.class, damageable -> {
                                            damageable.setDamage(0);
                                            damageable.displayName(TextUtils.get(ToxicConstants.ITEM_COMPANY_TOOL_NAME));
                                            damageable.lore(List.of(
                                                    TextUtils.get(usage)
                                            ));
                                        })
                                        .build();
        Map<Integer, ItemStack> result = player.getInventory().addItem(axe);
        if (!result.isEmpty())
            return null;

        Await.playerEvent(
                player,
                PlayerDropItemEvent.class,
                (self, e) -> {
                    if (!e.getPlayer().getInventory().contains(axe)) {
                        self.dismiss();
                        return;
                    }
                    if (!e.getItemDrop().getItemStack().isSimilar(axe))
                        return;

                    e.getItemDrop().remove();
                    self.dismiss();
                }
        );

        player.getInventory().setItemInMainHand(axe);
        return axe;
    }

    private boolean canInteractOnChunk(Player player, Int2 chunk) {
        CompanyArea companyArea = getAnyCompanyAreaFromChunkVector(chunk);
        if (companyArea != null) {
            PlayerEmployee playerEmployee = entityUtils.getMetadata(player, ToxicConstants.PLAYER_EMPLOYEE);
            if (playerEmployee != null && companyArea.getCompany() == playerEmployee.getCompany())
                companyArea = null;
        }

        return companyArea == null;
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (!canInteractOnChunk(e.getPlayer(), new Int2(e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ()))) {
            e.getPlayer().sendMessage(ChatColor.RED + "Tato plocha již patří nějaké firmě!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (!canInteractOnChunk(e.getPlayer(), new Int2(e.getBlock().getLocation().getChunk().getX(), e.getBlock().getLocation().getChunk().getZ()))) {
            e.getPlayer().sendMessage(ChatColor.RED + "Tato plocha již patří nějaké firmě!");
            e.setCancelled(true);
        }
    }

    @EventHandler(ignoreCancelled = true, priority = EventPriority.HIGH)
    public void onPlayerInteractEvent2(PlayerInteractEvent e) {
        if (e.getClickedBlock() == null)
            return;

        if (!canInteractOnChunk(e.getPlayer(),
                                new Int2(e.getClickedBlock().getLocation().getChunk().getX(), e.getClickedBlock().getLocation().getChunk().getZ()))) {
            e.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        Await.process(e);
    }

    @EventHandler
    public void onPlayerDropItemEvent(PlayerDropItemEvent e) {
        Await.process(e);
    }

    @CommandListener("yes")
    private void onYes(CommandData data) {
        Await.process(data);
    }

    @CommandListener("no")
    private void onNo(CommandData data) {
        Await.process(data);
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
        if (sub.equals("stock"))
            return onCompanyManageStock(commandData);
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
        if (!isVectorInSelectedCompanyArea(employee.getCompany(), targetEntity.getLocation().toVector()))
            return new ErrorCommandResponse("Tento item frame musí ležet na pozemku společnosti.");

        Banner banner = database.findEntity(
                Banner.class,
                "owner = :owner",
                ImmutableMap.of("owner", employee.getCompany().getUuid().toString()),
                FetchLevel.Primitive
        );

        if (itemFrame.getItem().getType() != Material.AIR) {
            // TODO: Delete or replace if path provided or something...
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

    private boolean setCompanyManageAreaEnds(PlayerInteractEvent e, ItemStack axe, Vector[] ends) {
        if (!axe.equals(e.getItem()) || e.getClickedBlock() == null ||
            (e.getAction() != Action.LEFT_CLICK_BLOCK && e.getAction() != Action.RIGHT_CLICK_BLOCK))
            return false;

        if (e.getAction() == Action.LEFT_CLICK_BLOCK) {
            ends[0] = e.getClickedBlock().getLocation().toBlockLocation().toVector();
            e.getPlayer().sendMessage(ChatColor.GOLD + "První roh nastaven!");
        } else {
            ends[1] = e.getClickedBlock().getLocation().toBlockLocation().toVector();
            e.getPlayer().sendMessage(ChatColor.GOLD + "Protější roh nastaven!");
        }

        e.setCancelled(true);
        return ends[0] != null && ends[1] != null;
    }

    private Object onCompanyManageArea(CommandData data) {
        PlayerEmployee employee;
        if ((employee = entityUtils.getMetadata(data.getPlayer(), ToxicConstants.PLAYER_EMPLOYEE)).getLevel() != PlayerEmployee.LEVEL_OWNER)
            return new ErrorCommandResponse("Nejsi vlastníkem společnosti.");

        CompanyArea companyArea = getAnyCompanyAreaFromVector(data.getPlayer().getLocation().toVector());
        if (companyArea != null) {
            // TODO: Check if player is the owner of the area.
        }

        ItemStack axe = giveManageTool(data.getPlayer(), ToxicConstants.ITEM_LORE_USAGE_AREA);
        if (axe == null)
            return new ErrorCommandResponse("Nemáš žádné volné místo v inventáři pro firemní nástroj!");
        Vector[] ends = new Vector[2];
        Await.playerEvent(
                data.getPlayer(),
                PlayerInteractEvent.class,
                new AwaitTimedConsumer<>() {
                    @Override
                    public int ticks() {
                        return 5 * 60 * 20;
                    }

                    @Override
                    public void accept(AwaitConsumer<PlayerInteractEvent> self, PlayerInteractEvent e) {
                        if (!setCompanyManageAreaEnds(e, axe, ends))
                            return;

                        e.getPlayer().sendMessage(ChatColor.AQUA + "Oba rohy byly nastaveny.");
                        e.getPlayer().getInventory().remove(axe);
                        self.dismiss();
                        VectorUtils.boundingBoxNormalize(ends[0], ends[1]);
                        if (isVectorInAnyCompanyArea(ends[0]) || isVectorInAnyCompanyArea(ends[1])) {
                            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Na tomto místě již někdo má firemní areál.");
                            return;
                        }

                        Int2 size = new Int2((int) (ends[1].getX() - ends[0].getX()), (int) (ends[1].getZ() - ends[0].getZ()));
                        double y = Math.max(ends[0].getY(), ends[1].getY()) + 2d;
                        int expenseAmount = (int) Math.ceil(size.getProduct() * PriceConst.AREA_BLOCK_EXPENSE);
                        e.getPlayer().sendMessage(ChatColor.GRAY + "Velikost: " + ChatColor.WHITE + size.getX() + "x" + size.getY());
                        e.getPlayer().sendMessage(
                                ChatColor.GRAY + "Záloha: " + ChatColor.WHITE + transactionManager.printMoney(size.getProduct() * PriceConst.AREA_BLOCK));
                        e.getPlayer().sendMessage(
                                ChatColor.GRAY + "Výdaje: " + ChatColor.WHITE + transactionManager.printMoney(expenseAmount));
                        e.getPlayer().sendMessage(
                                ChatColor.AQUA + "Pro potvrzení nebo zrušení použij " + ChatColor.GOLD + "/yes " + ChatColor.AQUA + "nebo " + ChatColor.GOLD +
                                "/no");
                        ParticleJob particleJob = particles.collection(
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[0].getZ()),
                                        new Vector(ends[1].getX() + 1, y, ends[0].getZ()),
                                        AreaType.GLOBAL.getColor(),
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[0].getZ()),
                                        new Vector(ends[0].getX(), y, ends[1].getZ() + 1),
                                        AreaType.GLOBAL.getColor(),
                                        .5f,
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[1].getX() + 1, y, ends[0].getZ()),
                                        new Vector(ends[1].getX() + 1, y, ends[1].getZ() + 1),
                                        AreaType.GLOBAL.getColor(),
                                        .5f,
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[1].getZ() + 1),
                                        new Vector(ends[1].getX() + 1, y, ends[1].getZ() + 1),
                                        AreaType.GLOBAL.getColor(),
                                        .5f,
                                        240,
                                        5
                                )
                        );
                        Await.playerCommand(
                                e.getPlayer(),
                                "yes",
                                new AwaitTimedConsumer<>() {
                                    @Override
                                    public int ticks() {
                                        return 20 * 60;
                                    }

                                    @Override
                                    public void accept(AwaitConsumer<CommandData> consumer, CommandData obj) {
                                        particleJob.stop();
                                        transactionManager.confirm(
                                                data.getPlayer(),
                                                employee.getCompany().getUuid(),
                                                TransactionManager.BANK,
                                                /*6400*/size.getProduct() * PriceConst.AREA_BLOCK,
                                                size.getX() + "x" + size.getY() + " firemní areál\n" + ChatColor.GRAY + "Výdaje: " + ChatColor.WHITE + "+ " +
                                                transactionManager.printMoney(expenseAmount) + "\n" +
                                                "Po zrušení areálu bude vyplacená záloha vrácena.",
                                                PaymentType.COMPANY_AREA,
                                                player -> {
                                                    Expense expense = new Expense();
                                                    expense.setUuid(employee.getCompany().getUuid());
                                                    expense.setExpenseType(PaymentType.COMPANY_AREA);
                                                    expense.setAmount(expenseAmount);
                                                    expense.save();

                                                    CompanyArea companyArea = new CompanyArea();
                                                    companyArea.setCompany(employee.getCompany());
                                                    companyArea.setAreaType(AreaType.GLOBAL);
                                                    companyArea.setStart(new Int2((int) ends[0].getX(), (int) ends[0].getZ()));
                                                    companyArea.setEnd(new Int2((int) ends[1].getX(), (int) ends[1].getZ()));
                                                    companyArea.save();

                                                    data.getPlayer().sendMessage(ChatColor.DARK_GREEN + "Areál pro tvoji společnost byl úspěšně vytvořen.");
                                                },
                                                player -> {
                                                }
                                        );
                                        consumer.dismiss();
                                        Await.dismissPlayerCommand(e.getPlayer(), "no");
                                    }
                                }
                        );
                        Await.playerCommand(
                                e.getPlayer(),
                                "no",
                                new AwaitTimedConsumer<>() {
                                    @Override
                                    public int ticks() {
                                        return 20 * 60;
                                    }

                                    @Override
                                    public void accept(AwaitConsumer<CommandData> consumer, CommandData obj) {
                                        particleJob.stop();
                                        consumer.dismiss();
                                        Await.dismissPlayerCommand(e.getPlayer(), "yes");
                                    }
                                }
                        );
                    }

                    @Override
                    public void timeout() {
                        data.getPlayer().getInventory().remove(axe);
                    }
                }
        );
        return new InfoCommandResponse("Použij tento nástroj pro označení hranic.\n" +
                                       "Použij levé tlačítko myši pro nastavení prvního rohu a poté použij pravé tlačítko myši pro nastavení protějšího rohu.\n" +
                                       "Velikost areálu bude automaticky vypočítáná a zobrazena po dobu jedné minuty. Tento čas využij pro kontrolu, a pro finální rozhodnutí " +
                                       "použij příkaz " + ChatColor.GOLD + "/yes" + ChatColor.AQUA + " nebo " + ChatColor.GOLD + "/no");
    }

    private Object onCompanyManageStock(CommandData data) {
        PlayerEmployee employee;
        if ((employee = entityUtils.getMetadata(data.getPlayer(), ToxicConstants.PLAYER_EMPLOYEE)).getLevel() != PlayerEmployee.LEVEL_OWNER)
            return new ErrorCommandResponse("Nejsi vlastníkem společnosti.");
        if (employee.getCompany().getStockArea() != null)
            return new ErrorCommandResponse("Tvoje firma již má postavený sklad! Aktuálně není možné postavit více než jeden sklad.");
        CompanyArea companyArea = getAnyCompanyAreaFromVector(data.getPlayer().getLocation().toVector());
        if (companyArea == null)
            return new ErrorCommandResponse("Abys postavil sklad, musíš nejdříve být ve firemním areálu.");

        ItemStack axe = giveManageTool(data.getPlayer(), ToxicConstants.ITEM_LORE_USAGE_STOCK);
        Vector[] ends = new Vector[2];
        Await.playerEvent(
                data.getPlayer(),
                PlayerInteractEvent.class,
                new AwaitTimedConsumer<>() {
                    @Override
                    public int ticks() {
                        return 5 * 60 * 20;
                    }

                    @Override
                    public void accept(AwaitConsumer<PlayerInteractEvent> self, PlayerInteractEvent e) {
                        if (!setCompanyManageAreaEnds(e, axe, ends))
                            return;

                        e.getPlayer().sendMessage(ChatColor.AQUA + "Oba rohy byly nastaveny.");
                        e.getPlayer().getInventory().remove(axe);
                        self.dismiss();
                        VectorUtils.boundingBoxNormalize(ends[0], ends[1]);
                        if (!isVectorInSelectedCompanyArea(employee.getCompany(), ends[0]) && !isVectorInSelectedCompanyArea(employee.getCompany(), ends[1])) {
                            e.getPlayer().sendMessage(ChatColor.DARK_RED + "Sklad můžeš mít jen na svém firemním areálu.");
                            return;
                        }
                        Int2 size = new Int2((int) (ends[1].getX() - ends[0].getX()), (int) (ends[1].getZ() - ends[0].getZ()));
                        double y = Math.max(ends[0].getY(), ends[1].getY()) + 2d;
                        e.getPlayer().sendMessage(
                                ChatColor.AQUA + "Pro potvrzení nebo zrušení použij " + ChatColor.GOLD + "/yes " + ChatColor.AQUA + "nebo " + ChatColor.GOLD +
                                "/no");
                        ParticleJob particleJob = particles.collection(
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[0].getZ()),
                                        new Vector(ends[1].getX() + 1, y, ends[0].getZ()),
                                        AreaType.STOCK.getColor(),
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[0].getZ()),
                                        new Vector(ends[0].getX(), y, ends[1].getZ() + 1),
                                        AreaType.STOCK.getColor(),
                                        .5f,
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[1].getX() + 1, y, ends[0].getZ()),
                                        new Vector(ends[1].getX() + 1, y, ends[1].getZ() + 1),
                                        AreaType.STOCK.getColor(),
                                        .5f,
                                        240,
                                        5
                                ),
                                particles.pulseLine(
                                        e.getPlayer().getWorld(),
                                        new Vector(ends[0].getX(), y, ends[1].getZ() + 1),
                                        new Vector(ends[1].getX() + 1, y, ends[1].getZ() + 1),
                                        AreaType.STOCK.getColor(),
                                        .5f,
                                        240,
                                        5
                                )
                        );

                        Await.playerCommand(
                                e.getPlayer(),
                                "yes",
                                new AwaitTimedConsumer<>() {
                                    @Override
                                    public int ticks() {
                                        return 20 * 60;
                                    }

                                    @Override
                                    public void accept(AwaitConsumer<CommandData> consumer, CommandData obj) {

                                    }
                                }
                        );
                        Await.playerCommand(
                                e.getPlayer(),
                                "no",
                                new AwaitTimedConsumer<>() {
                                    @Override
                                    public int ticks() {
                                        return 20 * 60;
                                    }

                                    @Override
                                    public void accept(AwaitConsumer<CommandData> consumer, CommandData obj) {
                                        particleJob.stop();
                                        consumer.dismiss();
                                        Await.dismissPlayerCommand(e.getPlayer(), "yes");
                                    }
                                }
                        );
                    }

                    @Override
                    public void timeout() {
                        data.getPlayer().getInventory().remove(axe);
                    }
                }
        );

        return new InfoCommandResponse("OK");
    }
}
