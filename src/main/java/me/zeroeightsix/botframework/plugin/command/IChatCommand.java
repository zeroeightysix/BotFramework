package me.zeroeightsix.botframework.plugin.command;

/**
 * Created by 086 on 12/05/2017.
 */
public interface IChatCommand {
    void call(String username, String[] args);
}
