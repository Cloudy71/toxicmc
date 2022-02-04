/*
  User: Cloudy
  Date: 30/01/2022
  Time: 17:07
*/

package cz.cloudy.minecraft.core.scoreboard.fields;

import cz.cloudy.minecraft.core.scoreboard.ScoreboardObject;

/**
 * @author Cloudy
 */
public class SpaceScoreboardField
        extends ScoreScoreboardField {
    public SpaceScoreboardField() {
        super("");
    }

    @Override
    protected void createField(ScoreboardObject scoreboardObject) {
        text = " ".repeat((int) getFields(scoreboardObject).stream()
                                                           .filter(field -> field instanceof SpaceScoreboardField && !field.isCreated())
                                                           .count());
        super.createField(scoreboardObject);
    }

    @Override
    protected void updateField(ScoreboardObject scoreboardObject) {
    }
}
