/*
  User: Cloudy
  Date: 30/01/2022
  Time: 14:31
*/

package cz.cloudy.minecraft.core.scoreboard;

import com.google.common.base.Preconditions;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Cloudy
 */
public class ScoreboardObject {
    protected String                name;
    protected ScoreboardLogic       logic;
    protected Player                player;
    protected Scoreboard            scoreboard;
    protected List<ScoreboardField> fields;
    protected boolean               created;

    private Pattern      parsePattern;
    private boolean      firstUpdate    = true;
    private List<String> cachedDataList = null;

    public ScoreboardObject(String name, ScoreboardLogic logic) {
        this.parsePattern = Pattern.compile("\\{(\\d+)}");
        this.fields = new ArrayList<>();
        this.name = name;
        this.logic = logic;

        if (logic != null) {
            logic.checkUpdate();
            cachedDataList = logic.getDataList();
        }
    }

    public ScoreboardObject(ScoreboardLogic logic) {
        this(null, logic);
    }

    public ScoreboardObject(String name) {
        this(name, null);
    }

    public ScoreboardObject() {
        this(null, null);
    }

    protected void create(Player player) {
        Preconditions.checkState(scoreboard == null, "Scoreboard is already created");
        if (logic != null)
            logic.scoreboardObject = this;
        scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        createFields();
        player.setScoreboard(scoreboard);
        created = true;
    }

    public void update() {
        if (logic == null || !logic.checkUpdate())
            return;

        cachedDataList = logic.getDataList();
        updateFields();
    }

    protected void createFields() {
        Preconditions.checkNotNull(scoreboard, "Scoreboard is not created");
        for (ScoreboardField field : fields) {
            field.create(this);
        }
    }

    protected void updateFields() {
        for (ScoreboardField field : fields) {
            field.updateField(this);
        }
    }

    public ScoreboardObject add(ScoreboardField field) {
        field.index = fields.size();
        fields.add(field);
        if (isCreated())
            update();
        return this;
    }

    public String parse(String text) {
        Matcher matcher = parsePattern.matcher(text);
        while (matcher.find()) {
            int num = Integer.parseInt(matcher.group(1));
            text = text.substring(0, matcher.start()) +
                   cachedDataList.get(num) +
                   text.substring(matcher.end());
        }
        return text;
    }

//    protected abstract Scoreboard createScoreboard();
//
//    protected abstract void updateScoreboard();

    public boolean isCreated() {
        return created;
    }
}
