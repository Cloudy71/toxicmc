/*
  User: Cloudy
  Date: 30/01/2022
  Time: 15:02
*/

package cz.cloudy.minecraft.core.scoreboard.fields;

import cz.cloudy.minecraft.core.scoreboard.ScoreboardField;
import cz.cloudy.minecraft.core.scoreboard.ScoreboardObject;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

import java.util.Comparator;

/**
 * @author Cloudy
 */
public class ScoreScoreboardField
        extends ScoreboardField {
    protected String text;
    protected Score  score;

    public ScoreScoreboardField(String text) {
        this.text = text;
    }

    @Override
    protected void createField(ScoreboardObject scoreboardObject) {
        ObjectiveScoreboardField objective = (ObjectiveScoreboardField)
                getFields(scoreboardObject).stream()
                                           .filter(field -> field instanceof ObjectiveScoreboardField && field.getIndex() < index)
                                           .max(Comparator.comparingInt(ScoreboardField::getIndex))
                                           .orElse(null);
        if (objective == null)
            return;

        score = objective.objective.getScore(scoreboardObject.parse(text));
        score.setScore((int) getFields(scoreboardObject).stream()
                                                        .filter(field -> field instanceof ScoreScoreboardField s && !s.created)
                                                        .count() - 1);
    }

    @Override
    protected void updateField(ScoreboardObject scoreboardObject) {
        int num = score.getScore();
        Objective objective = score.getObjective();
        score.resetScore();
        score = objective.getScore(scoreboardObject.parse(text));
        score.setScore(num);
    }
}
