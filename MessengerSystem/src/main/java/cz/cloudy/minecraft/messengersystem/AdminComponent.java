/*
  User: Cloudy
  Date: 17/01/2022
  Time: 23:56
*/

package cz.cloudy.minecraft.messengersystem;

import cz.cloudy.minecraft.core.componentsystem.annotations.CheckCondition;
import cz.cloudy.minecraft.core.componentsystem.annotations.CheckPermission;
import cz.cloudy.minecraft.core.componentsystem.annotations.CommandListener;
import cz.cloudy.minecraft.core.componentsystem.annotations.Component;
import cz.cloudy.minecraft.core.componentsystem.interfaces.IComponent;
import cz.cloudy.minecraft.core.componentsystem.types.CommandData;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.ErrorCommandResponse;
import cz.cloudy.minecraft.core.componentsystem.types.command_responses.InfoCommandResponse;
import cz.cloudy.minecraft.core.database.Database;
import cz.cloudy.minecraft.messengersystem.pojo.UserAccount;
import org.bukkit.Color;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * @author Cloudy
 */
@Component
public class AdminComponent
        implements IComponent {

    @Component
    private Database database;

    @CommandListener("get_uuid")
    @CheckPermission(CheckPermission.OP)
    @CheckCondition(CheckCondition.ARGS_IS_1)
    private Object onGetUuid(CommandData data) {
        String playerName = data.arguments()[0];

        Player player = getPlugin().getServer().getPlayer(playerName);
        if (player == null) {
            return new ErrorCommandResponse("Tento hráč neexistuje.");
        }

        return new InfoCommandResponse(player.getUniqueId().toString());
    }

    @CommandListener("generate_uuid")
    @CheckPermission(CheckPermission.OP)
    @CheckCondition(CheckCondition.ARGS_IS_1)
    private Object onGenerateUuid(CommandData data) {
        String text = data.arguments()[0];
        UUID uuid = UUID.nameUUIDFromBytes(text.getBytes(StandardCharsets.UTF_8));
        return new InfoCommandResponse(uuid.toString());
    }

    @CommandListener("set_color")
    @CheckPermission(CheckPermission.OP)
    @CheckCondition(CheckCondition.ARGS_IS_2)
    private Object onSetColor(CommandData data) {
        String playerName = data.arguments()[0];
        byte colorByte;
        try {
            colorByte = Byte.parseByte(data.arguments()[1]);
        } catch (NumberFormatException e) {
            return new ErrorCommandResponse("Barva musí být číslo.");
        }

        Player player = getPlugin().getServer().getPlayer(playerName);
        if (player == null) {
            return new ErrorCommandResponse("Tento hráč neexistuje.");
        }

        UserAccount userAccount = database.findEntity(UserAccount.class, player.getUniqueId());
        if (userAccount == null) {
            return new ErrorCommandResponse("Tento hráč není registrovaný.");
        }

        userAccount.setChatColor(colorByte);
        userAccount.save();

        return new InfoCommandResponse("Barva byla nastavena.");
    }

    @CommandListener("draw_line")
    @CheckPermission(CheckPermission.OP)
    private Object onDrawLine(CommandData data) {
        for (float i = 0; i < 5; i += .1f) {
            data.getPlayer().getWorld().spawnParticle(
                    Particle.REDSTONE,
                    data.getPlayer().getLocation().clone().add(new Vector(0f, i, 0f)),
                    1, 0, 2, 0,
                    new Particle.DustOptions(Color.LIME, 1f)
            );
        }

        return new InfoCommandResponse("OK");
    }
}
