package me.zeroeightsix.botframework.math;

import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;

public class BlockPos extends Vec3d {

    public BlockPos(double x, double y, double z) {
        super(x, y, z);
    }

    public BlockPos(Position position) {
        this(position.getX(), position.getY(), position.getZ());
    }

    public int getX() {
        return (int) xCoord;
    }
    public int getY() {
        return (int) yCoord;
    }
    public int getZ() {
        return (int) zCoord;
    }

    public BlockPos clone(){
        return new BlockPos(xCoord, yCoord, zCoord);
    }

}