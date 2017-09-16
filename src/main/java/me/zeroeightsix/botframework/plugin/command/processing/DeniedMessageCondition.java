package me.zeroeightsix.botframework.plugin.command.processing;

/**
 * Created by Gebruiker on 4/06/2017.
 */
public class DeniedMessageCondition implements ICondition<String, String> {
    @Override
    public String commit(String input) {
        return "You're not allowed to execute this command, " + input;
    }
}
