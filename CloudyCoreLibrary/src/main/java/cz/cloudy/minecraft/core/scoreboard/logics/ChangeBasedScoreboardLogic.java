/*
  User: Cloudy
  Date: 30/01/2022
  Time: 17:28
*/

package cz.cloudy.minecraft.core.scoreboard.logics;

import cz.cloudy.minecraft.core.scoreboard.ScoreboardLogic;

/**
 * @author Cloudy
 */
public abstract class ChangeBasedScoreboardLogic
        extends ScoreboardLogic {
    protected Object[] variables;
    private   long     oldHash;

    public ChangeBasedScoreboardLogic(Object... variables) {
        this.variables = variables;
    }

    public abstract long calculateHash();

    @Override
    public boolean checkUpdate() {
        long newHash = calculateHash();
        boolean update = newHash != oldHash;
        oldHash = newHash;
        return update;
    }
}
