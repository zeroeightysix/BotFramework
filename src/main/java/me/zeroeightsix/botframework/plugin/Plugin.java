package me.zeroeightsix.botframework.plugin;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.zeroeightsix.botframework.Logger;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.CommandEvent;
import me.zeroeightsix.botframework.flag.AbstractFlaggable;
import me.zeroeightsix.botframework.plugin.command.ChatCommand;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.plugin.command.processing.CommandProcessor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public abstract class Plugin extends AbstractFlaggable {

    String name;
    String version;
    String description;

    ChatQueue queue = new ChatQueue();
    CommandProcessor processor = new CommandProcessor(this);

    ArrayList<Command> registeredInternalCommands = new ArrayList<>();
    ArrayList<ChatCommand> registerChatCommands = new ArrayList<>();

    File dataFolder;

    public Plugin(String name) {
        this(name, "1.0");
    }

    public Plugin(String name, String version) {
        this(name, version, "Descriptionless");
    }

    public Plugin(String name, String version, String description) {
        this.name = name;
        this.version = version;
        this.description = description;
        this.dataFolder = new File("Plugins/" + name);
        if (!dataFolder.exists() || !dataFolder.isDirectory()){
            dataFolder.mkdir();
        }
        queue.start();

        try {
            initializeFlags();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Failed to initialize flag(s) of plugin " + name + "!");
        }
    }

    public void sendChatMessage(String message){

        if (message.length() > 256){
            String s = message.substring(0,256);
            sendChatMessage(s);
            sendChatMessage(message.substring(256));
            return;
        }

        if (queue.getWaitTime() < 10){
            getBot().getClient().getSession().send(new ClientChatPacket(message));
            return;
        }
        queue.addMessage(message);

    }

    public void callCommand(CommandEvent event){
        callCommand(event, false, null);
    }
    public void callCommand(CommandEvent event, boolean isAdmin){
        callCommand(event, isAdmin, null);
    }

    public void callCommand(CommandEvent event, boolean isAdmin, String deniedMessage){
        for (ChatCommand c : registerChatCommands){
            if (c.getLabel().equalsIgnoreCase(event.getLabel())){
                if (c.isAdminCommand() && !isAdmin) {
                    if (deniedMessage != null)
                        sendChatMessage(String.format(deniedMessage, event.getUsername()));
                    continue;
                }
                c.call(event.getUsername(), event.getArguments());
            }
        }
    }

    public void onEnable(){}
    public void onDisable(){}

    public void registerInternalCommand(Command command){
        registeredInternalCommands.add(command);
    }
    public void registerChatCommand(ChatCommand command){
        registerChatCommands.add(command);
    }
    protected void registerChatCommands(String packagename) {
        List<Class> classList = Util.generateClassList(packagename);
        for (Class s : classList) {
            classif:
            if (ChatCommand.class.isAssignableFrom(s)){
                try {
                    for (Constructor constructor : s.getConstructors()) {
                        Class[] types = constructor.getParameterTypes();
                        if (types.length == 0) { // Empty constructor!
                            ChatCommand command = (ChatCommand) constructor.newInstance();
                            registerChatCommand(command);
                            break classif;
                        }else if(types.length == 1) {
                            if (Plugin.class.isAssignableFrom(types[0])) { // Constructor taking in only a plugin (parent plugin)
                                ChatCommand command = (ChatCommand) constructor.newInstance(this);
                                registerChatCommand(command);
                                break classif;
                            }
                        }
                    }

                    throw new RuntimeException("Unsupported constructor");
//                    ChatCommand command = (ChatCommand) s.getConstructor().newInstance();
//                    registerChatCommand(command);
                } catch (Exception e) {
                    getLogger().severe("Couldn't register chatcommand " + s.getSimpleName());
                    getLogger().logTrace(e);
                }
            }
        }
    }

    public ArrayList<Command> getInternalCommands() {
        return registeredInternalCommands;
    }
    public ArrayList<ChatCommand> getChatCommands() {
        return registerChatCommands;
    }
    protected PluginManager getPluginManager(){
        return PluginManager.getInstance();
    }
    protected MinecraftBot getBot() {
        return MinecraftBot.getInstance();
    }
    public ChatQueue getQueue() {
        return queue;
    }
    public String getName() {
        return name;
    }
    public String getDescription() {
        return description;
    }
    public String getVersion() {
        return version;
    }
    public Logger getLogger() {
        return MinecraftBot.getLogger();
    }
    public File getDataFolder() {
        return dataFolder;
    }

    public CommandProcessor getCommandProcessor() {
        return processor;
    }
}
