package me.zeroeightsix.botframework.plugin.command;

import java.util.function.BiFunction;

/**
 * Created by Babbaj on 1/2/2018.
 *
 * Interface for 'sub commands' that exist within Commands
 *
 * @param <R> The return type of the command
 * String = name of player calling the command
 * String[] = arguments that the player supplies to the command
 */
@FunctionalInterface
public interface ISubCommand<R> extends BiFunction<String, String[], R> {
}
