/*
  User: Cloudy
  Date: 22/01/2022
  Time: 21:58
*/

package cz.cloudy.minecraft.toxicmc.components.economics.pojo;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableMap;
import cz.cloudy.minecraft.core.LoggerFactory;
import cz.cloudy.minecraft.core.componentsystem.ComponentLoader;
import cz.cloudy.minecraft.core.componentsystem.annotations.Cached;
import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import cz.cloudy.minecraft.core.database.enums.FetchLevel;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import cz.cloudy.minecraft.toxicmc.components.economics.enums.AreaType;
import org.slf4j.Logger;

import java.time.ZonedDateTime;
import java.util.Set;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("company")
public class Company
        extends DatabaseEntity {

    @PrimaryKey
    @Column("uuid")
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuid;

    @Column("creator")
    @ForeignKey
    @Lazy
    @Null
    protected UserAccount creator;

    @Column("name")
    @Size(48)
    @Index(unique = true)
    protected String name;

    @Column("date_created")
    @Default("NOW()")
    protected ZonedDateTime dateCreated;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public UserAccount getCreator() {
        return creator;
    }

    public void setCreator(UserAccount creator) {
        this.creator = creator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ZonedDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(ZonedDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    // ==========================================
    private static final Logger logger = LoggerFactory.getLogger(Company.class);

//    private static LoadingCache<UUID, Set<Expense>> expenseCache =
//            CacheBuilder.newBuilder()
//                    .build(
//                            new CacheLoader<>() {
//                                @Override
//                                public Set<Expense> load(UUID key) throws Exception {
//                                    return ComponentLoader.get(Database.class).findEntities(
//                                            Expense.class,
//                                            "uuid = :uuid",
//                                            ImmutableMap.of("uuid", key.toString()),
//                                            FetchLevel.Primitive
//                                    );
//                                }
//                            }
//                    );

    @Join(table = BankAccount.class, where = "uuid = :uuid")
    public BankAccount getBankAccount() {
        return null;
    }

    @Join(table = Expense.class, where = "uuid = :uuid")
    public Set<Expense> getExpenses() {
        return null;
    }

    @Join(table = CompanyArea.class, where = "company.uuid = :uuid")
    public CompanyArea getAreas() {
        return null;
    }

    @Join(table = CompanyArea.class, where = "company.uuid = :uuid AND area_type = " + AreaType.STOCK_VALUE)
    public CompanyArea getStockArea() {
        return null;
    }

    @Join(table = CompanyArea.class, where = "company.uuid = :uuid AND area_type = " + AreaType.SHOP_VALUE)
    public Set<CompanyArea> getShopAreas() {
        return null;
    }


//    @Cached
//    public Set<Expense> getExpenses() {
//        return ComponentLoader.get(Database.class).findEntities(
//                Expense.class,
//                "uuid = :uuid",
//                ImmutableMap.of("uuid", uuid.toString()),
//                FetchLevel.Primitive
//        );
//    }

//    @Deprecated
//    public Set<Expense> getExpenses(boolean refresh) {
//        if (refresh)
//            expenseCache.refresh(uuid);
//        return expenseCache.getUnchecked(uuid);
//    }

//    @Deprecated
//    public Set<Expense> getExpenses() {
//        return getExpenses(false);
//    }
}
