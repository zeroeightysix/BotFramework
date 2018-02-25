package me.zeroeightsix.botframework.plugin.standard;

import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.data.game.entity.metadata.Position;
import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerBlockChangePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerUnloadChunkPacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.event.BlockModifiedEvent;
import me.zeroeightsix.botframework.math.BlockPos;
import me.zeroeightsix.botframework.plugin.EventHandler;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.PluginManager;

import java.util.ArrayList;
import java.util.EventListener;

/**
 * Created by 086 on 16/09/2017.
 */
@Deprecated
public class WorldHandler extends Plugin implements EventListener {

    private static ArrayList<Column> columns = new ArrayList<>();
    private static WorldHandler INSTANCE;

    public static boolean FLAG_LOG = false;

    public WorldHandler() {
        super("worldhandler", "1");
        PluginManager.getInstance().registerListener(this, this);
        INSTANCE = this;
    }

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ServerChunkDataPacket) {
            ServerChunkDataPacket chunkDataPacket = event.getPacket();
            Column column = chunkDataPacket.getColumn();
            addColumn(column);
            if (FLAG_LOG)
                MinecraftBot.getLogger().info("[WorldHandler] Added chunk: " + column.getX() + ", " + column.getZ());
        }else if (event.getPacket() instanceof ServerUnloadChunkPacket) {
            ServerUnloadChunkPacket unloadChunkPacket = event.getPacket();
            removeColumnAt(unloadChunkPacket.getX(), unloadChunkPacket.getZ());
            if (FLAG_LOG)
                MinecraftBot.getLogger().info("[WorldHandler] Removed chunk: " + unloadChunkPacket.getX() + ", " + unloadChunkPacket.getZ());
        }else if (event.getPacket() instanceof ServerBlockChangePacket) {
            ServerBlockChangePacket blockChangePacket = event.getPacket();
            Position position = blockChangePacket.getRecord().getPosition();
            int x = position.getX();
            int y = position.getY();
            int z = position.getZ();

            Chunk chunk = getBlockChunk(position.getX(), position.getY(), position.getZ());
            try{
                BlockModifiedEvent event1 = new BlockModifiedEvent(new BlockPos(position), blockChangePacket.getRecord().getBlock(), chunk.getBlocks().get(x, y, z));
                PluginManager.getInstance().fireEvent(event1);
                BlockState newState = event1.getNewState();
                x = event1.getLocation().getX();
                y = event1.getLocation().getY();
                z = event1.getLocation().getZ();

                if (x > 0)  x %= 16;
                else        x = 16 - Math.abs(x)%16;
                if (y > 0)  y %= 16;
                else        y = 16 - Math.abs(y)%16;
                if (z > 0)  z %= 16;
                else        z = 16 - Math.abs(z)%16;
                if (chunk != null)
                    chunk.getBlocks().set(x, y, z, newState);
                if (FLAG_LOG)
                    MinecraftBot.getLogger().info("[WorldHandler] Modified block: " + position.getX() + ", " + position.getY() + ", "+ position.getZ());
            }catch (Exception e){}
        }
    }

    private void addColumn(Column column) {
        removeColumnAt(column.getX(), column.getZ());

        columns.add(column);
    }

    private void removeColumnAt(int x, int z) {
        Column c = getColumnAt(x, z);
        if (c != null)
            columns.remove(c);
    }

    private Column getColumnAt(int x, int z) {
        ArrayList<Column> columns = (ArrayList<Column>) this.INSTANCE.columns.clone();
        return columns.stream().filter(column -> column!=null && column.getX() == x && column.getZ() == z).findFirst().orElse(null);
    }

    private Column getBlockColumn(int x, int z) {
        return getColumnAt(x/16, z/16);
    }

    private Chunk getBlockChunk(Column parent, int y) {
        return parent.getChunks()[y/16];
    }

    private Chunk getBlockChunk(int x, int y, int z) {
        return getBlockChunk(getBlockColumn(x, z), y);
    }

    public static BlockState getBlockAt(int x, int y, int z) {
        Column column = INSTANCE.getBlockColumn(x, z);
        if (column == null) return null;
        Chunk chunk = INSTANCE.getBlockChunk(column, y);

        int bX = x;
        int bY = y;
        int bZ = z;
        if (bX > 0) bX %= 16;
        else        bX = 16-Math.abs(bX)%16;
        if (bY > 0) bY %= 16;
        else        bY = 16-Math.abs(bY)%16;
        if (bZ > 0) bZ %= 16;
        else        bZ = 16-Math.abs(bZ)%16;

        BlockState blockState = chunk.getBlocks().get(bX, bY, bZ);
        return blockState;
    }

}
