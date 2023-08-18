package plugin;

import arc.Events;
import arc.math.Mathf;
import arc.math.geom.Geometry;
import arc.math.geom.Position;
import arc.struct.Seq;
import arc.util.CommandHandler;
import arc.util.Interval;
import arc.util.Threads;
import arc.util.Timer;
import mindustry.Vars;
import mindustry.content.Blocks;
import mindustry.content.StatusEffects;
import mindustry.game.EventType;
import mindustry.game.Team;
import mindustry.gen.*;
import mindustry.mod.Plugin;
import mindustry.type.UnitType;
import mindustry.world.Block;

import java.util.Objects;

import static arc.util.Strings.canParseInt;
import static arc.util.Strings.parseInt;
import static mindustry.Vars.*;
import static plugin.Functions.*;
import static plugin.Menus.callMenu;

public class Main extends Plugin{
    public static Seq<Plot> plots = new Seq<>();
    public static Seq<DrawTool> draws = new Seq<>();
    public static int coreRadius = 50;
    public static Seq<SetBlock> setBlocks = new Seq<>();
    @Override
    public void init(){
        Events.on(EventType.PlayerJoin.class, event -> {
            Plot plot = containsPlayer(event.player.uuid());
            if (plot != null){
                event.player.team(plot.team);
            }
        });
        Timer.schedule(() -> {
            for (Unit unit : Groups.unit){
                Team unitTeam = unit.team();
                if (unit.team != Team.crux) {
                    if (unitTeam.core() == null) {
                        unit.apply(StatusEffects.disarmed, 1500);
                        return;
                    }
                    if (unitTeam.core().dst(unit.x, unit.y) / tilesize > coreRadius) {
                        unit.apply(StatusEffects.disarmed, 90);
                    }
                }
            }
        }, 0, 1);
        Timer.schedule(() -> {
            for (Player plr : Groups.player){
                Team unitTeam = plr.unit().team();
                if (unitTeam.core() == null){
                    plr.unit().apply(StatusEffects.disarmed, 1500);
                    return;
                }
                if (unitTeam.core().dst(plr.unit().x, plr.unit().y) / tilesize > coreRadius){
                    plr.unit().apply(StatusEffects.disarmed, 90);
                }
            }
        }, 0, 1);
        Events.run(EventType.Trigger.update, () ->{
            for (Player plr : Groups.player) {
                if (containsPlayerDraw(plr.uuid()) != null && plr.shooting()) {
                    DrawTool tool = containsPlayerDraw(plr.uuid());
                    circleDraw(plr, tool.radius, tool.block, tool.type, (int) (plr.mouseX/8), (int) (plr.mouseY/8));
                }
            }
        });
        Timer.schedule(() -> {
            if (!setBlocks.isEmpty()){
                int random = Mathf.random(setBlocks.size-1);
                SetBlock setBlock = setBlocks.get(random);
                if (setBlock.block.isFloor()){
                    world.tile(setBlock.x, setBlock.y).setFloorNet(setBlock.block);
                }
                setBlocks.remove(setBlock);
            }
        }, 0, 0.0015f);
    }

    //register commands that run on the server
    @Override
    public void registerServerCommands(CommandHandler handler){

    }

    //register commands that player can invoke in-game
    @Override
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("createplot", "Create plot! Start your journey!", (args, player) -> {
            plotCreation(player);
        });
        handler.<Player>register("drawtool", "<radius> <type> <block>", "Enables draw tool", (args, player) -> {
            if (containsPlayer(player.uuid()) == null){
                player.sendMessage("You dont have plot!");
                return;
            }
            if (!canParseInt(args[0])){
                player.sendMessage("Type a number!");
                return;
            }
            if (parseInt(args[0]) > 6){
                player.sendMessage("Radius cant be more than 6!");
                return;
            }
            if (content.block(args[2]) == null){
                player.sendMessage("Block is not found!");
                return;
            }
            if (!args[1].equals("overlay") && !args[1].equals("floor") && !args[1].equals("block")){
                player.sendMessage("Invalid type!");
                return;
            }
            int radius = Integer.parseInt(args[0]);
            String type = args[1];
            Block block = content.block(args[2]);
            DrawTool tool = new DrawTool(player.uuid(), radius, type, block);
            if (containsPlayerDraw(player.uuid()) != null){
                draws.remove(containsPlayerDraw(player.uuid()));
            }
            draws.add(tool);
        });
        handler.<Player>register("disabledraw", "Disables draw tool", (args, player) -> {
            if (containsPlayerDraw(player.uuid()) != null){
                draws.remove(containsPlayerDraw(player.uuid()));
                player.sendMessage("[green]Draw tool has been disabled!");
            } else {
                player.sendMessage("[red]There is nothing to disable!");
            }
        });
        handler.<Player>register("search", "<block>", "Search for blocks!", (args, player) -> {
            StringBuilder list = new StringBuilder();
            list.append("Blocks found: \n");
            for (Block block : content.blocks()){
                if (block.name.contains(args[0])) {
                    list.append(block.name + "; ");
                }
            }
            player.sendMessage(String.valueOf(list));
        });
        handler.<Player>register("changefloor", "<floor>", "Changes floor on your plot!", (args, player) -> {
            if (containsPlayer(player.uuid()) == null){
                player.sendMessage("You dont have plot!");
                return;
            }
            if (content.block(args[0]) == null){
                player.sendMessage("Floor is not found!");
                return;
            }
            if (!content.block(args[0]).isFloor()){
                player.sendMessage("Block should be a floor!");
                return;
            }
            Plot plot = containsPlayer(player.uuid());
            Block block = content.block(args[0]).asFloor();
            Geometry.circle(plot.x, plot.y, coreRadius, (cx, cy) -> {
                if (world.tile(cx,cy) != null) {
                    SetBlock blk = new SetBlock(cx, cy, block);
                    setBlocks.add(blk);
                }
            });
        });
        handler.<Player>register("deleteplot", "Deleted your plot", (args, player) -> {
            if (containsPlayer(player.uuid()) == null){
                player.sendMessage("You dont have plot!");
                return;
            }
            DrawTool drw = new DrawTool(player.uuid(), 7, "floor", content.block("sand-floor"));
            Plot plot = containsPlayer(player.uuid());
            if (containsPlayerDraw(player.uuid()) != null){
                drw = containsPlayerDraw(player.uuid());
            }
            Geometry.circle(plot.x, plot.y, coreRadius, (cx, cy) -> {
                if (world.tile(cx,cy) != null) {
                    SetBlock block = new SetBlock(cx, cy, Blocks.space);
                    setBlocks.add(block);
                }
            });
            draws.remove(drw);
            plots.remove(plot);
            plot.team.data().buildings.each(Building::kill);
        });
        handler.<Player>register("plotsettings", "Opens settings menu", (args, player) -> {
            if (containsPlayer(player.uuid()) == null){
                player.sendMessage("You dont have plot!");
                return;
            }
            callMenu(containsPlayer(player.uuid()), player);
        });
        handler.<Player>register("spawnunit", "<enemy/ally> <unit> <count>", "Spawns unit on your plot!", (args, player) -> {
            if (containsPlayer(player.uuid()) == null) {
                player.sendMessage("You dont have plot!");
                return;
            }
            if (!args[0].equals("ally") && !args[0].equals("enemy")){
                player.sendMessage("Invalid first argument! Should be either ally or enemy");
                return;
            }
            if (content.unit(args[1]) == null){
                player.sendMessage("Invalid unit!");
                return;
            }
            if (!canParseInt(args[2])){
                player.sendMessage("Count should be a number!");
                return;
            }
            if (parseInt(args[2]) > 5){
                player.sendMessage("Count should be 5 or less!");
                return;
            }
            UnitType unit = content.unit(args[1]);
            int count = Integer.parseInt(args[2]);
            Plot plot = containsPlayer(player.uuid());
            Team team;
            if (Objects.equals(args[0], "ally")){
                team = plot.team;
            } else {
                team = Team.crux;
            }
            if (plot.team.core().dst(player.x, player.y) / 8 > coreRadius){
                player.sendMessage("Cant spawn out of your plot!");
                return;
            }
            for (int i = 0; i < count; i++){
                unit.spawn(team, player.x, player.y);
            }
        });
    }
}