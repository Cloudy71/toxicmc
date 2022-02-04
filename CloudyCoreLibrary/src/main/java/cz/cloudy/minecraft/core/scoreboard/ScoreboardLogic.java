/*
  User: Cloudy
  Date: 30/01/2022
  Time: 17:26
*/

package cz.cloudy.minecraft.core.scoreboard;

import java.util.Collections;
import java.util.List;

/**
 * @author Cloudy
 */
public abstract class ScoreboardLogic {
    protected ScoreboardObject scoreboardObject;

    public abstract boolean checkUpdate();

    public List<String> getDataList() {
        return Collections.emptyList();
    }
}
