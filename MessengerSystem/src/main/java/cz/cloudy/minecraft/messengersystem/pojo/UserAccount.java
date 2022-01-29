/*
  User: Cloudy
  Date: 06/01/2022
  Time: 19:00
*/

package cz.cloudy.minecraft.messengersystem.pojo;

import cz.cloudy.minecraft.core.data_transforming.transformers.UUIDToStringTransformer;
import cz.cloudy.minecraft.core.database.DatabaseEntity;
import cz.cloudy.minecraft.core.database.annotation.*;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.time.ZonedDateTime;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Table("user_account")
public class UserAccount
        extends DatabaseEntity {

    @Column("uuid")
    @PrimaryKey
    @Transform(UUIDToStringTransformer.class)
    @Size(37)
    protected UUID uuid;

    @Column("password")
    @Size(64)
    protected String password;

    @Column("permissions")
    protected byte permissions;

    @Column("chat_color")
    protected byte chatColor;

    @Column("date_registered")
    @Default("NOW()")
    protected ZonedDateTime dateRegistered;

    @Column("date_last_online")
    @Default("NOW()")
    protected ZonedDateTime dateLastOnline;

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte getPermissions() {
        return permissions;
    }

    public void setPermissions(byte permissions) {
        this.permissions = permissions;
    }

    public byte getChatColor() {
        return chatColor;
    }

    public void setChatColor(byte chatColor) {
        this.chatColor = chatColor;
    }

    public ZonedDateTime getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(ZonedDateTime dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public ZonedDateTime getDateLastOnline() {
        return dateLastOnline;
    }

    public void setDateLastOnline(ZonedDateTime dateLastOnline) {
        this.dateLastOnline = dateLastOnline;
    }

    //==============================================================

    public Player getPlayer() {
        return Bukkit.getPlayer(uuid);
    }

    public OfflinePlayer getOfflinePlayer() {
        return Bukkit.getOfflinePlayer(uuid);
    }
}
