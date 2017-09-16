package me.zeroeightsix.botframework.event;

import com.github.steveice10.packetlib.event.session.SessionEvent;
import com.github.steveice10.packetlib.event.session.SessionListener;

public class CommandEvent implements SessionEvent {

    String username;
    String label;
    String[] arguments;

    public CommandEvent(String username, String label, String[] arguments) {
        this.username = username;
        this.label = label;
        this.arguments = arguments;
    }

    public String getUsername() {
        return username;
    }

    public String[] getArguments() {
        return arguments;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public void call(SessionListener sessionListener) {

    }
}
