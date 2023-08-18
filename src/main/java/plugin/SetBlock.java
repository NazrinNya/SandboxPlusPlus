package plugin;

import mindustry.world.Block;

public class SetBlock{
    public final int x;
    public final int y;
    public final Block block;

    public SetBlock(int x1, int y1, Block block){
        this.x = x1;
        this.y = y1;
        this.block = block;
    }
}