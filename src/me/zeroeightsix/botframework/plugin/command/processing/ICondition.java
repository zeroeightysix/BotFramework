package me.zeroeightsix.botframework.plugin.command.processing;

/**
 * Created by Gebruiker on 4/06/2017.
 */
public interface ICondition<S, T> {
    public S commit(T input);
}
