package me.zeroeightsix.botframework.math;

public class BlockPos extends Vec3d {

    public BlockPos(double x, double y, double z) {
        super(x, y, z);
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

}