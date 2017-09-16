package me.zeroeightsix.botframework.plugin.command.processing;

/**
 * Created by Gebruiker on 4/06/2017.
 */
public abstract class IsAdminCondition implements ICondition<Boolean, String> {
    @Override
    public Boolean commit(String input) {
        return false;
    }
}
