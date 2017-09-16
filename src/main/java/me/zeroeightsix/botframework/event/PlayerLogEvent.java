package me.zeroeightsix.botframework.event;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;

public class PlayerLogEvent implements SessionEvent {

    GameProfile player;
    Action action;

    public PlayerLogEvent(GameProfile player, Action action) {
        this.player = player;
        this.action = action;
    }

    @Override
    public void call(SessionListener sessionListener) {

    }

    public Action getAction() {
        return action;
    }

    public GameProfile getPlayer() {
        return player;
    }

    public enum Action {
        JOIN,
        LEAVE
    }
}
