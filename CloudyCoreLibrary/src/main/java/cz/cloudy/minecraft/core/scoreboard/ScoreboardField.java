/*
  User: Cloudy
  Date: 30/01/2022
  Time: 14:43
*/

package cz.cloudy.minecraft.core.scoreboard;

import org.bukkit.scoreboard.Scoreboard;

import java.util.List;

/**
 * @author Cloudy
 */
public abstract class ScoreboardField {
    protected boolean created;
    protected int     index;

    protected void create(ScoreboardObject scoreboardObject) {
        if (created)
            return;
        createField(scoreboardObject);
        created = true;
    }

    protected abstract void createField(ScoreboardObject scoreboardObject);

    protected abstract void updateField(ScoreboardObject scoreboardObject);

    protected Scoreboard getScoreboard(ScoreboardObject scoreboardObject) {
        return scoreboardObject.scoreboard;
    }

    protected List<ScoreboardField> getFields(ScoreboardObject scoreboardObject) {
        return scoreboardObject.fields;
    }

    public boolean isCreated() {
        return created;
    }

    public int getIndex() {
        return index;
    }
}
