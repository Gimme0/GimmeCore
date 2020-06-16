package me.gimme.gimmecore.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

public class PerPlayerScoreboardProvider {

    private static final String PER_PLAYER_SCOREBOARD_KEY = "a9058f0jmyu8hela";

    public static Scoreboard setupScoreboard(Player player) {
        if (player.getScoreboard().getObjective(PER_PLAYER_SCOREBOARD_KEY) != null) return player.getScoreboard();

        ScoreboardManager scoreboardManager = Bukkit.getScoreboardManager();
        assert scoreboardManager != null; // All worlds have loaded
        Scoreboard scoreboard = scoreboardManager.getNewScoreboard();

        player.setScoreboard(scoreboard);
        scoreboard.registerNewObjective(PER_PLAYER_SCOREBOARD_KEY, "dummy", "");
        return scoreboard;
    }

}
