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
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BankAccount;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.BankTransaction;
import cz.cloudy.minecraft.toxicmc.components.economics.pojo.Company;
import org.slf4j.Logger;

import java.nio.charset.StandardCharsets;
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

    @Override
    public void onStart() {
        Company bankCompany = database.findEntity(Company.class, BANK);
        if (bankCompany != null)
            return;

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

    public void send(UUID source, UUID target, String message, int amount) {
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
        bankTransaction.save();

        bankAccountSource.setBalance(bankAccountSource.getBalance() - amount);
        bankAccountSource.save();

        bankAccountTarget.setBalance(bankAccountTarget.getBalance() + amount);
        bankAccountTarget.save();

        logger.info("Payment: {} => {} ({}, {})", source, target, amount, message);
    }

    public void pay(UUID source, String message, int amount) {
        send(source, BANK, message, amount);
    }

    public boolean hasBalance(UUID source, int amount) {
        BankAccount bankAccount = database.findEntity(BankAccount.class, source);
        return bankAccount.getTotalBalance() >= amount;
    }
}
