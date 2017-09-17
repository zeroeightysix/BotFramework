package me.zeroeightsix.botframework.event;

import com.github.steveice10.mc.protocol.data.game.world.block.BlockState;
import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;
import me.zeroeightsix.botframework.math.BlockPos;

/**
 * Created by 086 on 16/09/2017.
 */
public class BlockModifiedEvent implements SessionEvent {

    BlockPos location;
    BlockState newState;
    BlockState oldState;

    public BlockModifiedEvent(BlockPos location, BlockState newState, BlockState oldState) {
        this.location = location;
        this.newState = newState;
        this.oldState = oldState;
    }

    public BlockPos getLocation() {
        return location;
    }

    public BlockState getNewState() {
        return newState;
    }

    public BlockState getOldState() {
        return oldState;
    }

    public void setNewState(BlockState newState) {
        this.newState = newState;
    }

    public void setOldState(BlockState oldState) {
        this.oldState = oldState;
    }

    public void setLocation(BlockPos location) {
        this.location = location;
    }

    @Override
    public void call(SessionListener sessionListener) {}
}
