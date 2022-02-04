/*
  User: Cloudy
  Date: 30/01/2022
  Time: 16:40
*/

package cz.cloudy.minecraft.core.scoreboard.fields;

import cz.cloudy.minecraft.core.scoreboard.ScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.ScoreboardObject;
import org.bukkit.scoreboard.Team;

/**
 * @author Cloudy
 */
public class TeamScoreboardField
        extends ScoreboardField {
    protected String name;
    protected Team   team;

    public TeamScoreboardField(String name) {
        this.name = name;
    }

    @Override
    protected void createField(ScoreboardObject scoreboardObject) {
        team = getScoreboard(scoreboardObject).registerNewTeam(name);

    }

    @Override
    protected void updateField(ScoreboardObject scoreboardObject) {
        // TODO: ...
    }
}
