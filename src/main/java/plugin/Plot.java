package plugin;

import arc.struct.Seq;
import mindustry.Vars;
import mindustry.game.Team;

public class Plot {
    public final Seq<String> plotPlayers = new Seq<>();
    public final int x;
    public final int y;
    public final Team team;
    public final PlotSettings settings;
    public Plot(Seq<String> plotPlayer, int x, int y, Team team, PlotSettings settings){
        this.settings = settings;
        this.plotPlayers.add(plotPlayer);
        this.x = x;
        this.y = y;
        this.team = team;
    }
    public Plot updatePlotSettings(PlotSettings settings){
        Plot newPlot = new Plot(plotPlayers, x, y, team, settings);
        if (newPlot.settings.isImmortal){
            Vars.state.rules.teams.get(newPlot.team).blockHealthMultiplier = 999;
        } else {
            Vars.state.rules.teams.get(newPlot.team).blockHealthMultiplier = 1;
        }
        return newPlot;
    }
}
