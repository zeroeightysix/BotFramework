package me.zeroeightsix.bot;

import me.zeroeightsix.botframework.bcfg.InvalidConfigurationException;
import me.zeroeightsix.botframework.bcfg.file.YamlBasicConfiguration;
import me.zeroeightsix.botframework.plugin.EventHandler;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.command.ChatCommand;
import me.zeroeightsix.botframework.event.CommandEvent;
import me.zeroeightsix.botframework.plugin.command.IChatCommand;

import java.io.File;
import java.io.IOException;
import java.util.EventListener;

/**
 * Created by 086 on 11/05/2017.
 */
public class Bot extends Plugin implements EventListener {

    YamlBasicConfiguration configuration;

    public Bot() {
        super("ExampleBot", "1.1", "An example bot plugin for botframework");
    }

    @Override
    public void onEnable() {
        // Register the event listener
        getPluginManager().registerListener(this, this);

        // Set the queue wait time. This means there is a 3000 ms (3s) delay between messages.
        getQueue().setWaitTime(3000);

        // Register a chat command with label 'greet' that greets the player
        registerChatCommand(ChatCommand.createCommand("greet", new IChatCommand() {
            @Override
            public void call(String username, String[] args) {
                sendChatMessage("Hello, "  + username + "!");
            }
        }, this));

        registerChatCommand(ChatCommand.createCommand("tpa", new IChatCommand() {
            @Override
            public void call(String username, String[] args) {
                // The arguments array excludes the label.
                if (args.length == 0){
                    sendChatMessage("Please specify a player.");
                    return;
                }
                sendChatMessage("Player " + username + " wishes to teleport to " + args[0]);
            }
        }, this));

        // Load a (yml) bcfg. This uses YAML, however there is also a "JsonConfiguration" object (botframework.cfg.JsonConfiguration) which uses JSON
        configuration = new YamlBasicConfiguration();
        try {
            configuration.load(new File(getDataFolder(), "myConfiguration.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }

        // Basic method for getting a string from the bcfg.
        getLogger().info("The value of 'helloworld' in the bcfg is: " + configuration.get("helloworld"));
    }

    // Event called when a player executes a command
    @EventHandler
    public void onCommand(CommandEvent event){
        // Loop through the registered chat commands & call those needed
        callCommand(event);

        // NOTE: A chatCommand also has a "setAdmin()" method, however, it only works if you pass through callCommand whether or not the player is an admin!
        // Do it like so:
        // callCommand(event, admins.contains(event.getUsername()));
        callCommand(event, true);
    }

}
