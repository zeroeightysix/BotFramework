package me.zeroeightsix.botframework.event;

import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;

/**
 * Created by Gebruiker on 29/05/2017.
 */
public class ChatEvent implements SessionEvent {

    private String message;

    public ChatEvent(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public void call(SessionListener sessionListener) {

    }

}
