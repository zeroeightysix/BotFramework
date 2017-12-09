package me.zeroeightsix.botframework.plugin.command;

import me.zeroeightsix.botframework.MinecraftBot;

/**
 * Created by 086 on 10/05/2017.
 */
public abstract class Command implements ICommand {

    String label;

    public Command(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    public static Command createCommand(String name, ICommand run) {
        return new Command(name) {
            @Override
            public void call(String[] args) {
                try {
                    run.call(args);
                }catch (Exception e){
                    MinecraftBot.getLogger().logTrace(e);
                }
            }
        };
    }

}
