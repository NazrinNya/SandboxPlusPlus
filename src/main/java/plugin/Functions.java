package plugin;

import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.struct.Seq;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.game.Team;
import mindustry.gen.Call;
import mindustry.gen.Player;
import mindustry.world.Block;

import static arc.util.Strings.parseInt;
import static mindustry.Vars.*;
import static mindustry.Vars.state;
import static plugin.Main.*;

public class Functions {
    public static void circleDraw(Player player, int radius, Block block, String type, int tileX, int tileY){
        if (player.team().core() == null){
            return;
        }
        Geometry.circle(tileX, tileY, radius, (cx, cy) -> {
            Team team = player.team();
            if (block.isFloor() && type.equals("floor")){
                if (team.core().dst(cx*8, cy*8) / 8 < coreRadius){
                    world.tile(cx,cy).setFloorNet(block);
                }
            } else if (block.isOverlay() && type.equals("overlay")){
                if (team.core().dst(cx*8, cy*8) / 8 < coreRadius){
                    world.tile(cx,cy).setOverlayNet(block);
                }
            }
            else if (type.equals("block")){
                if (team.core().dst(cx * 8, cy * 8) / 8 < coreRadius && world.tile(cx, cy).build ==  null) {
                    world.tile(cx, cy).setNet(block, player.team(), 0);
                }
            }
        });
    }
    public static Plot containsPlayer(String uuid){
        for (Plot plt : plots){
            if (plt.plotPlayers.contains(uuid)){
                return plt;
            }
        }
        return null;
    }
    public static DrawTool containsPlayerDraw(String uuid){
        for (DrawTool drw : draws){
            if (drw.player.equals(uuid)){
                return drw;
            }
        }
        return null;
    }
    public static void plotCreation(Player player){
        int randomTeam = Mathf.random(3, 255);
        int iterator = 0;
        for (Plot plt : plots){
            if (plt.plotPlayers.contains(player.uuid())){
                player.sendMessage("You can create only one plot!");
                return;
            }
        }
        if (state.teams.getActive().contains(t -> t.cores.contains(core -> core.dst(player.x, player.y) < coreRadius * 2 * tilesize))){
            player.sendMessage("You cant create a plot on another!");
            return;
        }
        while (iterator < 250 && Team.get(randomTeam).active()){
            randomTeam = Mathf.random(3, 255);
            iterator += 1;
        }
        Geometry.circle(player.tileX(), player.tileY(), 5, (cx, cy) -> {
            if (world.tile(cx,cy) != null) {
                world.tile(cx, cy).setFloorNet(Blocks.coreZone.asFloor());
            }
        });
        Geometry.circle(player.tileX(), player.tileY(), coreRadius, (cx, cy) -> {
            if (world.tile(cx,cy) != null) {
                SetBlock block = new SetBlock(cx, cy, Blocks.grass);
                setBlocks.add(block);
            }
        });
        world.tile(player.tileX(), player.tileY()).setBlock(Blocks.coreCitadel, Team.get(randomTeam), 0);
        world.tile(player.tileX(), player.tileY()).setNet(Blocks.coreCitadel, Team.get(randomTeam), 0);
        player.team(Team.get(randomTeam));
        Call.infoToast("Plot created!", 3);
        PlotSettings settings = new PlotSettings(true);
        Seq<String> players = new Seq<>();
        players.add(player.uuid());
        Plot plot = new Plot(players, player.tileX(), player.tileY(), player.team(), settings);
        plots.add(plot);
        Vars.state.rules.teams.get(plot.team).blockHealthMultiplier = 999;
        state.rules.teams.get(plot.team).rtsAi = true;
    }
}
