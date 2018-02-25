package me.zeroeightsix.botframework.event;

import com.github.steveice10.packetlib.event.session.PacketSentEvent;
import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;

/**
 * Created by 086 on 25/02/2018.
 */
public class StatusPacketSentEvent implements SessionEvent {

    public StatusPacketSentEvent(PacketSentEvent event) {
        this.event = event;
    }

    PacketSentEvent event;

    public PacketSentEvent getEvent() {
        return event;
    }

    @Override
    public void call(SessionListener sessionListener) {

    }
}
