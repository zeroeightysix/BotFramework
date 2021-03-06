package me.zeroeightsix.botframework;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.beust.jcommander.ParameterException;
import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.auth.exception.request.InvalidCredentialsException;
import com.github.steveice10.mc.auth.exception.request.RequestException;
import com.github.steveice10.mc.protocol.MinecraftConstants;
import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.mc.protocol.data.status.handler.ServerInfoHandler;
import com.github.steveice10.mc.protocol.data.status.handler.ServerPingTimeHandler;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerChatPacket;
import com.github.steveice10.packetlib.Client;
import com.github.steveice10.packetlib.Session;
import com.github.steveice10.packetlib.event.session.*;
import com.github.steveice10.packetlib.packet.Packet;
import com.github.steveice10.packetlib.tcp.TcpSessionFactory;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jline.console.ConsoleReader;
import me.zeroeightsix.botframework.event.ChatEvent;
import me.zeroeightsix.botframework.event.StatusPacketReceivedEvent;
import me.zeroeightsix.botframework.event.StatusPacketSentEvent;
import me.zeroeightsix.botframework.forge.ForgeHandshakeHandler;
import me.zeroeightsix.botframework.forge.ForgeMinecraftProtocol;
import me.zeroeightsix.botframework.locale.Locale;
import me.zeroeightsix.botframework.locale.text.ITextComponent;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.PluginManager;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.poof.EraPoofInfo;
import me.zeroeightsix.botframework.poof.PoofHandler;
import me.zeroeightsix.botframework.poof.Poofable;
import me.zeroeightsix.botframework.poof.use.LoadLocalePoof;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.Arrays;

import static me.zeroeightsix.botframework.Constants.*;

public class MinecraftBot implements Poofable {

    @Parameter(names = {"-username", "-u"}, description = "Username or email for authentication", required = true)
    public String USERNAME = null;
    @Parameter(names = {"-password", "-pw"}, description = "Password for authentication")
    public String PASSWORD = null;
    @Parameter(names = {"-proxy", "-p"}, description = "Proxy IP to connect through")
    private String PROXY_IP = null;
    @Parameter(names = {"-proxyport", "-pp"}, description = "Proxy port to connect through")
    private int PROXY_PORT = Integer.MIN_VALUE;
    @Parameter(names = "-debug", description = "Enables debug mode")
    public static boolean DEBUG = false;
    @Parameter(names = "-ip", description = "IP to connect to")
    private String HOST = null;
    @Parameter(names = "-port", description = "Port to connect to")
    private int PORT = 25565;
    @Parameter(names = "-verifyusers", description = "Disable cracked accounts")
    private boolean VERIFY_USERS = false;
    @Parameter(names = "-skiptest", description = "Skip the connection timeout test")
    private boolean SKIP_TEST = false;
    @Parameter(names = "-locale", description = "The locale to load botframework in")
    private String locale_name = "en_us";
    @Parameter(names = {"-forge", "-f"}, description = "Specifies that this connection is modded using FML")
    private boolean isForge = false;

    @Parameter(names = {"-h", "--help"},
        help = true,
        description = "Displays help information")
    private boolean help;

    private Client client = null;
    MinecraftProtocol protocol;
    private final static Logger LOGGER = new Logger();
    private static MinecraftBot INSTANCE;
    public static GameProfile SELF_PROFILE;
    private String SELF_TOKEN;
    public static final String BUILD_NUMBER = "55";
    private String BOTFRAMEWORKVERSION = "build " + BUILD_NUMBER;
    Locale locale;

    boolean logged_in = false;

    public static void main(String[] args) {
        INSTANCE = new MinecraftBot();

        try{
            JCommander commander = new JCommander(INSTANCE, args);
            if (INSTANCE.help){
                commander.usage();
                System.exit(0);
            }
        }catch (Exception e){
            if (e instanceof ParameterException){
                if (e.getMessage().contains("Was passed")){
                    try{
                        String param = e.getMessage().substring(27);
                        param = param.substring(0,param.length()-53);
                        getLogger().severe("Unknown parameter '" + param + "'! Use --help for usage");
                        System.exit(0);
                        return;
                    }catch (Exception a){
                        getLogger().logTrace(a);
                        System.exit(0);
                        return;
                    }
                }
            }
            getLogger().severe(e.getMessage());
            System.exit(0);
            return;
        }

        INSTANCE.startUp();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            getLogger().warn("Shutting down! Disabling plugins");
            for (Plugin plugin : PluginManager.getInstance().getPlugins()){
                plugin.onDisable();
            }
            getLogger().info("Plugins disabled. Quitting.");
        }, "Shutdown-thread"));
    }

    public void startUp(){
        getLogger().setDisabled(!FLAG_PRINT_PLUGIN_INFO);
        getLogger().info("Loading plugins");
        PluginManager.getInstance().reload();
        getLogger().info(PluginManager.getInstance().getPlugins().size() + " plugins loaded");
        getLogger().setDisabled(false);

        locale = new Locale();

        getLogger().setDisabled(!FLAG_PRINT_LOCALE_INFO);

        LoadLocalePoof.LocalePoofInfo info = new LoadLocalePoof.LocalePoofInfo(EraPoofInfo.Era.PRE, locale_name, locale);
        PoofHandler.callPoof(LoadLocalePoof.class, info, this);
        locale = info.getLocale();
        locale_name = info.getLocaleName();

        try{
            locale.loadLocale(locale_name);
        }catch (Exception e) {
            try {
                boolean retry = !locale_name.equals("en_us");
                if (retry) {
                    getLogger().severe("Failed to load locale '" + locale_name + "'! Using en_us instead.");
                    locale.loadLocale("en_us");
                }else
                    throw new RuntimeException("Don't print me please :(");
            }catch (Exception eh) {
                getLogger().severe("Couldn't load default locale en_us! Is it even present? Check me.zeroeightsix.botframework.locale!");
                getLogger().severe("Continuing without locale.");
            }
        }
        getLogger().setDisabled(false);

        getLogger().setDisabled(!FLAG_PRINT_BUILD_INFO);
        getLogger().info("Minecraft BotFramework by 086, " + BOTFRAMEWORKVERSION);
        getLogger().setDisabled(false);

        if (isForge) {
            PluginManager.getInstance().registerListener(new ForgeHandshakeHandler(), null);
            getLogger().info("Simulating modded forge client.");
        }

        getLogger().setDisabled(!FLAG_PRINT_SERVER_INFO);
        status();   // Server status
        getLogger().setDisabled(false);
        login();    // Authentication

        new Thread(() -> {
            try{
                ConsoleReader reader = Logger.getReader();

                String command;
                while ((command = reader.readLine("> ")) != null){
                    if (command.startsWith("-")){
                        String[] parts = command.substring(1).split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by every space if it isn't surrounded by quotes

                        String label = parts[0];
                        String[] args = Util.removeElement(parts, 0);
                        if (args[0] == null)
                            args = new String[]{};

                        onInternalCommand(label, args);

                        reader.setPrompt("");
                        reader.redrawLine();
                    }else{
                        if (getClient().getSession().isConnected()){
                            if (!command.isEmpty())
                                getClient().getSession().send(new ClientChatPacket(command));
                        } else {
                            getLogger().info("Couldn't send message; bot not yet connected");
                        }
                    }
                }
            }catch (Exception e){
                e.printStackTrace();
            }finally {
                getLogger().severe("Command thread has been terminated. (not good)");
            }
        }).start();
    }

    public void inputHostname() {
        String host = null;
        try {
            host = getLogger().getReader().readLine("Hostname: ");
        } catch (IOException e) {
            System.exit(0);
        }
        if (host.equals("")) {
            System.exit(0);
        }
        String port = null;
        try {
            port = getLogger().getReader().readLine("Port: ");
        } catch (IOException e) {
            System.exit(0);
        }
        if (port.equals(""))
            System.exit(0);

        try{
            PORT = Integer.parseInt(port);
        }catch (NumberFormatException e){
            System.err.println("Port must be numerical");
            return;
        }
        HOST = host;
    }

    private boolean timeoutTest() {
        // Test connection before getting status
        if (!SKIP_TEST) {
            int code = Util.hostAvailabilityCheck(HOST, PORT);

            switch (code){
                case 0:
                    getLogger().info("Server is up & reachable");
                    break;
                case 1:
                    getLogger().severe("Server isn't reachable!");
                    return false;
                default:
                    getLogger().severe("Could not resolve hostname");
                    return false;
            }
        }else{
            getLogger().info("Skipping timeout test!");
        }
        return true;
    }

    private void status() {
        if (HOST == null || PORT == Integer.MIN_VALUE){
            System.err.println("No hostname or port defined! (hostname -ip, port -port)");
            inputHostname();
        }

        Proxy proxy;
        if (PROXY_IP != null){
            proxy = new Proxy(Proxy.Type.DIRECT, new InetSocketAddress(PROXY_IP, PROXY_PORT));
        }else {
            proxy = Proxy.NO_PROXY;
        }

        boolean success = timeoutTest();
        if (!success) {
            System.exit(0);
            return;
        }

        MinecraftProtocol protocol;

        if (isForge) {
            getLogger().info("\tForge: overwriting default status packet in order to fetch mod list");
            protocol = new ForgeMinecraftProtocol(SubProtocol.STATUS);
        }else{
            protocol = new MinecraftProtocol(SubProtocol.STATUS);
        }

        client = new Client(HOST, PORT, protocol, new TcpSessionFactory(proxy));
        client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, proxy);
        client.getSession().setFlag(MinecraftConstants.SERVER_INFO_HANDLER_KEY, (ServerInfoHandler) (session, info) -> {
            getLogger().setDisabled(!FLAG_PRINT_SERVER_INFO);
            if (isDebug()){
                getLogger().info("Version: " + info.getVersionInfo().getVersionName() + ", " + info.getVersionInfo().getProtocolVersion());
                getLogger().info("Player Count: " + info.getPlayerInfo().getOnlinePlayers() + " / " + info.getPlayerInfo().getMaxPlayers());
                getLogger().info("Players: " + Arrays.toString(info.getPlayerInfo().getPlayers()));
                getLogger().info("Icon: " + info.getIcon());
            }else{
                getLogger().info("Target server is online with " + info.getPlayerInfo().getOnlinePlayers() + " players.");
                getLogger().info("Version: " + info.getVersionInfo().getVersionName());// + ", Description: " + parseTextMessage(info.getDescription().toJsonString()).replace("\n", "").replace("\r", ""));
            }

            String MOTD = parseTextMessage(info.getDescription().toJsonString());
            String[] lines = MOTD.split("\n");
            getLogger().info("Description: " + lines[0]);
            if (lines.length > 1) // this is bad i know
                getLogger().info("             " + lines[1]);

            getLogger().setDisabled(false);
        });

        client.getSession().setFlag(MinecraftConstants.SERVER_PING_TIME_HANDLER_KEY, new ServerPingTimeHandler() {
            @Override
            public void handle(Session session, long pingTime) {
                getLogger().info("Server ping took " + pingTime + "ms");
            }
        });

        client.getSession().addListener(new SessionListener() {
            @Override
            public void packetReceived(PacketReceivedEvent packetReceivedEvent) {
                PluginManager.getInstance().fireEvent(new StatusPacketReceivedEvent(packetReceivedEvent));
            }
            @Override
            public void packetSent(PacketSentEvent packetSentEvent) {
                PluginManager.getInstance().fireEvent(new StatusPacketSentEvent(packetSentEvent));
            }
            @Override
            public void packetSending(PacketSendingEvent packetSendingEvent) { }
            @Override
            public void connected(ConnectedEvent connectedEvent) { }
            @Override
            public void disconnecting(DisconnectingEvent disconnectingEvent) { }
            @Override
            public void disconnected(DisconnectedEvent disconnectedEvent) { }
        });

        client.getSession().connect();
        while(client.getSession().isConnected()) {
            try {
                Thread.sleep(1);
            } catch(InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void login(){
        login(false);
    }

    private void login(boolean retry) {
        getLogger().setDisabled(!FLAG_PRINT_AUTH_INFO);
        if (!logged_in || retry){
            logged_in = true;
            if(VERIFY_USERS || PASSWORD != null) {
                try {
                    protocol = new MinecraftProtocol(USERNAME, PASSWORD, false);
                    getLogger().info("Successfully authenticated user " + protocol.getProfile().getName() + "!");
                } catch(RequestException e) {
                    if (e instanceof InvalidCredentialsException) {
                        if (isDebug()) e.printStackTrace();
                        getLogger().severe("Couldn't log in: Invalid credentials");
                        getLogger().severe("Quitting");
                        System.exit(0);
                        return;
                    }
                    e.printStackTrace();
                    return;
                }
            } else {
                getLogger().info("Logging into cracked account with username " + USERNAME);
                protocol = new MinecraftProtocol(USERNAME);
            }

            SELF_PROFILE = protocol.getProfile();
            SELF_TOKEN = protocol.getAccessToken();

            if (retry && !protocol.getProfile().isComplete()) {
                getClient().getSession().disconnect("Lost connection");
                getLogger().severe("Couldn't log in again after retrying. Quitting!");
                System.exit(0);
                return;
            }
        }else{
            getLogger().info("Reauthenticating:");
            if (!SELF_TOKEN.isEmpty()) {
                getLogger().info("  Using token");
                protocol = new MinecraftProtocol(SELF_PROFILE, SELF_TOKEN);
                if (!protocol.getProfile().isComplete()) {
                    getLogger().info("  Couldn't reauthenticate using token. Logging in again!");
                    login(true);
                }
                getLogger().info("  Reauthenticated using " + SELF_PROFILE.getName() + (isDebug() ? "(TOKEN " + SELF_TOKEN + ", ID " + protocol.getProfile().getName() + " )" : ""));
            } else {
                getLogger().info("   Using cracked account");
                protocol = new MinecraftProtocol(USERNAME);
            }
            getLogger().info("   Done!");
        }

        Proxy proxy;
        if (PROXY_IP != null){
            proxy = new Proxy(Proxy.Type.DIRECT, new InetSocketAddress(PROXY_IP, PROXY_PORT)); // We'll assume this proxy works. Too bad if it doesn't!
            getLogger().info("Connecting through proxy: " + PROXY_IP + ":" + PROXY_PORT);
        }else {
            proxy = Proxy.NO_PROXY;
            getLogger().info("Not using a proxy.");
        }

        client = new Client(HOST + (isForge ? "\u0000FML\u0000" : ""), PORT, protocol, new TcpSessionFactory(proxy));
        client.getSession().setFlag(MinecraftConstants.AUTH_PROXY_KEY, proxy);
        client.getSession().addListener(new SessionAdapter() {
            @Override
            public void packetReceived(PacketReceivedEvent event) {
                try{
                    MinecraftBot.getInstance().onPacketReceived(event);
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
            @Override
            public void packetSent(PacketSentEvent event) {
                PluginManager.getInstance().fireEvent(event);
            }
            @Override
            public void connected(ConnectedEvent event) {
                PluginManager.getInstance().fireEvent(event);
            }
            @Override
            public void disconnecting(DisconnectingEvent event) {
                PluginManager.getInstance().fireEvent(event);
            }
            @Override
            public void disconnected(DisconnectedEvent event) {
                MinecraftBot.getInstance().onDisconnected(event);
            }
        });

        try {
            client.getSession().connect();
        }catch (Exception e) {
            getLogger().severe("Failed to connect! Quitting");
            if (isDebug())
                getLogger().logTrace(e);
            else
                getLogger().severe("Message: " + e.getMessage());
            System.exit(0);
            return;
        }

        getLogger().setDisabled(false);
    }

    private void onInternalCommand(String label, String[] args){
        for (Plugin plugin : PluginManager.getInstance().getPlugins()){
            for (Command command : plugin.getInternalCommands()){
                if (command.getLabel().equals(label)) {
                    command.call(args);
                    return;
                }
            }
        }
        getLogger().warn("Unknown command.");
    }

    public void onPacketReceived(PacketReceivedEvent event){
        if (event.getPacket() instanceof ServerChatPacket) {
            ITextComponent component = ITextComponent.Serializer.jsonToComponent(removeExcessiveChatJSON(((ServerChatPacket) event.getPacket()).getMessage().toJsonString()));

            getLogger().enableColours();
            getLogger().info("[Chat] " + component.getFormattedText());
            getLogger().disableColours();

            PluginManager.getInstance().fireEvent(new ChatEvent(component.getUnformattedText()));

            for (Plugin p : PluginManager.getInstance().getPlugins()){
                p.getCommandProcessor().processMessage(component.getUnformattedText());
            }
        }
        PluginManager.getInstance().fireEvent(event);
    }

    public void onDisconnected(DisconnectedEvent event){
        // Because we can't deal with certain errors upon connection directly, we'll try to handle them here.
        String reason = event.getReason();
        if (reason.startsWith("Login failed:")) {
            if (reason.endsWith("Access Token can not be null or empty.")) {
                getLogger().severe("Couldn't log in; server requires premium account, and we're either on cracked or failed to authenticate");
                System.exit(0);
                return;
            }

            if (reason.endsWith("Invalid login session.")) {
                SELF_TOKEN = null;
                logged_in = false;
                getLogger().info("Couldn't connect because token was invalid; retrying with login details");
                login();
            }
        }

        getLogger().info("Disconnected with reason: " + reason);
        if(event.getCause() != null) {
            if (event.getCause() instanceof ConnectException){
                getLogger().info("Was unable to connect, retrying");
            }else {
                event.getCause().printStackTrace();
            }
        }
        final int waitTime = FLAG_RECONNECT_TIME;
        getLogger().info("Reconnecting in " + Util.msToTime(waitTime));
        new Thread(() -> {
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int retry = 0;
            while (!timeoutTest()) {
                retry++;
                int totalRetries = FLAG_DOWN_RETRY;
                if (retry >= totalRetries) {
                    getLogger().severe("The target server is still down after " + totalRetries + " retries! Exiting.");
                    System.exit(0);
                }else{
                    try {
                        long wait = FLAG_DOWN_TIME;
                        getLogger().info("Waiting " + Util.msToTime((int) wait) + " before retrying connection. " + (totalRetries - retry) + " retries remaining.");
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        getLogger().logTrace(e);
                    }
                }
            }

            getLogger().info("Reconnecting ..");
            login();
        }).start();
        PluginManager.getInstance().fireEvent(event);
    }

    public static boolean sendPacket(Packet packet) {
        if (!MinecraftBot.getInstance().getClient().getSession().isConnected()) return false;
        MinecraftBot.getInstance().getClient().getSession().send(packet);
        return true;
    }

    private static final JsonObject removeExcessiveChatJSON(JsonObject json) {
        if (json.has("hoverEvent")) json.remove("hoverEvent");
        if (json.has("clickEvent")) json.remove("clickEvent");
        return json;
    }

    private static final String removeExcessiveChatJSON(String json) {
        JsonParser parser = new JsonParser();
        if (json.startsWith("[")) {
            JsonArray array = parser.parse(json).getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                array.set(i, removeExcessiveChatJSON(array.get(i).getAsJsonObject()));
            }
            return array.toString();
        }else{
            JsonObject object = parser.parse(json).getAsJsonObject();
            if (object.has("extra")) return removeExcessiveChatJSON(object.getAsJsonArray("extra").toString());
        }
        return json;
    }

    public static final String parseTextMessage(String json) {
        return ITextComponent.Serializer.jsonToComponent(removeExcessiveChatJSON(json)).getFormattedText();
    }

    public static String format(String translateKey, Object... parameters)
    {
        return getInstance().getLocale().formatMessage(translateKey, parameters);
    }

    public static MinecraftBot getInstance() {
        return INSTANCE;
    }

    public String getHost() {
        return HOST;
    }

    public static Logger getLogger() {
        return LOGGER;
    }

    public Client getClient() {
        return client;
    }

    public Locale getLocale() {
        return locale;
    }

    public boolean isDebug() {
        return DEBUG;
    }

    public static GameProfile getProfile() {
        return SELF_PROFILE;
    }
}
