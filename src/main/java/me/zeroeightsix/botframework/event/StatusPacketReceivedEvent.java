package me.zeroeightsix.botframework.event;

import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;

/**
 * Created by 086 on 25/02/2018.
 */
public class StatusPacketReceivedEvent implements SessionEvent {

    public StatusPacketReceivedEvent(PacketReceivedEvent event) {
        this.event = event;
    }

    PacketReceivedEvent event;

    public PacketReceivedEvent getEvent() {
        return event;
    }

    @Override
    public void call(SessionListener sessionListener) {}
}
