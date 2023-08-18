package plugin;

import mindustry.world.Block;

public class DrawTool {
    public final String player;
    public final int radius;
    public final String type;
    public final Block block;

    public DrawTool(String player, int radius, String type, Block block) {
        this.player = player;
        this.radius = radius;
        this.type = type;
        this.block = block;
    }
}
