package me.zeroeightsix.botframework.plugin;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.zeroeightsix.botframework.Logger;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.CommandEvent;
import me.zeroeightsix.botframework.plugin.command.ChatCommand;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.plugin.command.processing.CommandProcessor;

import java.io.File;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public abstract class Plugin {

    private String name;
    private String version;
    private String description;

    private ChatQueue queue = new ChatQueue();
    private CommandProcessor processor = new CommandProcessor(this);

    private ArrayList<Command> registeredInternalCommands = new ArrayList<>();
    private ArrayList<ChatCommand> registeredChatCommands = new ArrayList<>();

    private File dataFolder;

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
            if (!dataFolder.mkdir()) {
                getLogger().warn("Unable to create plugin data folder: Might cause issues!");
            }
        }
        queue.start();
    }

    public void sendChatMessage(String message){

        if (message.length() > 256){
            String s = message.substring(0, 256);
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
        for (ChatCommand c : registeredChatCommands){
            if (c.getLabel().equalsIgnoreCase(event.getLabel())){
                if (c.isAdminCommand() && !isAdmin) {
                    if (deniedMessage != null)
                        sendChatMessage(String.format(deniedMessage, event.getUsername()));
                    continue;
                }
                if (c.getPermissions().size() == 0 ||
                        c.getPermissions().stream().anyMatch(perm -> perm.test(event.getUsername()))) {
                    c.call(event.getUsername(), event.getArguments());
                }
            }
        }
    }

    public void onEnable(){}
    public void onDisable(){}

    protected void registerInternalCommand(Command command){
        registeredInternalCommands.add(command);
    }
    protected void registerChatCommand(ChatCommand command){
        registeredChatCommands.add(command);
    }

    @SuppressWarnings("unchecked")
    protected void registerChatCommands(String packagename) {
        List<Class> classList = Util.generateClassList(packagename);
        classList.stream()
                 .filter(this::isClassValid)
                 .map(clazz -> (Class<? extends ChatCommand>)clazz)
                 .forEach(this::registerClass);
    }

    private void registerClass(Class<? extends ChatCommand> clazz) {
        try {
            Constructor c = clazz.getDeclaredConstructors()[0];
            Class[] params = c.getParameterTypes();
            if (params.length == 0) {
                ChatCommand command = (ChatCommand) c.newInstance();
                registerChatCommand(command);
            }
            else  {
                ChatCommand command = (ChatCommand) c.newInstance(this);
                registerChatCommand(command);
            }

        } catch (Exception e) {
            getLogger().severe("Failed to register chatcommand " + clazz.getSimpleName());
            getLogger().logTrace(e);
        }
    }

    private boolean isClassValid(Class<?> clazz) {
        try {
            Objects.requireNonNull(clazz);
            if (!clazz.isAnnotationPresent(RegisterCommand.class))
                return false;
                //throw new Exception("missing @RegisterCommand annotation");
            if (!ChatCommand.class.isAssignableFrom(clazz))
                throw new Exception("does not extend ChatCommand class");
            if (!isValidConstructor(clazz.getDeclaredConstructors()[0]))
                throw new Exception("invalid constructor");

            return true;
        } catch (Exception e) {
            getLogger().warn(String.format("Invalid class \"%s\": %s", clazz.getName(), e.getMessage()));
            return false;
        }
    }
    private boolean isValidConstructor(Constructor c) {
        int count = c.getParameterCount();
        return count == 0 ||
                (Plugin.class.isAssignableFrom(c.getParameterTypes()[0]) && count == 1);
    }

    public ArrayList<Command> getInternalCommands() {
        return registeredInternalCommands;
    }
    public ArrayList<ChatCommand> getChatCommands() {
        return registeredChatCommands;
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
