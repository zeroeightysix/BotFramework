package me.zeroeightsix.botframework.plugin.standard;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.entity.player.CombatState;
import com.github.steveice10.mc.protocol.data.game.entity.player.Hand;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerSwingArmPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerCombatPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import jline.console.completer.Completer;
import jline.internal.Preconditions;
import me.zeroeightsix.botframework.Logger;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.BlockModifiedEvent;
import me.zeroeightsix.botframework.event.PlayerLogEvent;
import me.zeroeightsix.botframework.math.BlockPos;
import me.zeroeightsix.botframework.plugin.EventHandler;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.PluginManager;
import me.zeroeightsix.botframework.plugin.command.ChatCommand;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.plugin.command.ICommand;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.stream.Collectors;

/**
 * Created by 086 on 10/05/2017.
 */
public class UtilPlugin extends Plugin implements EventListener {

    // location
    private static BlockPos playerPos = new BlockPos(0,0,0);
    private BlockPos reach = null;

    //commands
    private static ArrayList<GameProfile> players = new ArrayList<>();
    private boolean SHOULD_POST_PLAYERUPDATES = false;
    PlayernameCompleter completer = new PlayernameCompleter();

    public static boolean FLAG_ANTIAFK = false;

    public UtilPlugin() {
        super("Util", "5", "AutoRespawn, inbuilt commands, player tracker and location handling.");
        getPluginManager().registerListener(this, this);

        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    onSecond();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();

        registerInternalCommand(Command.createCommand("plugins", new ICommand() {
            @Override
            public void call(String[] args) {
                MinecraftBot.getLogger().info(PluginManager.getInstance().getPlugins().size() + " plugins present:");
                for (Plugin plugin : PluginManager.getInstance().getPlugins()){
                    getLogger().info(plugin.getName() + " " + plugin.getVersion());
                }
            }
        }));

        registerInternalCommand(Command.createCommand("exit", new ICommand() {
            @Override
            public void call(String[] args) {
                getBot().getClient().getSession().disconnect("Lost connection");
                System.exit(0);
            }
        }));

        registerInternalCommand(Command.createCommand("disconnect", new ICommand() {
            @Override
            public void call(String[] args) {
                if (!getBot().getClient().getSession().isConnected()){
                    getLogger().warn("You're not connected to a server!");
                    return;
                }
                getBot().getClient().getSession().disconnect("Lost connection");
            }
        }));

        registerInternalCommand(Command.createCommand("changehost", new ICommand() {
            @Override
            public void call(String[] args) {
                if (getBot().getClient().getSession().isConnected()){
                    getBot().getClient().getSession().disconnect("Lost connection");
                    return;
                }
                MinecraftBot.getInstance().inputHostname();
            }
        }));

        registerInternalCommand(Command.createCommand("help", new ICommand() {
            @Override
            public void call(String[] args) {
                for (Plugin plugin : getPluginManager().getPlugins()){
                    getLogger().info(plugin.getName() + " " + plugin.getVersion() + ": (" + plugin.getInternalCommands().size() + " command(s))");
                    int i = 0;
                    int len = (plugin.getInternalCommands().size()+"").length();
                    for (Command c : plugin.getInternalCommands()){
                        getLogger().info("|" + Util.stretchNumber(i, len) + ": -" + c.getLabel());
                        i++;
                    }
                }
            }
        }));

        registerInternalCommand(Command.createCommand("chatcommands", new ICommand() {
            @Override
            public void call(String[] args) {
                for (Plugin plugin : getPluginManager().getPlugins()){
                    getLogger().info(plugin.getName() + " " + plugin.getVersion() + ": (" + plugin.getChatCommands().size() + " chatcommand(s))");
                    int i = 0;
                    int len = (plugin.getChatCommands().size()+"").length();
                    String prefix = plugin.getCommandProcessor().getCommandPrefix();
                    for (ChatCommand c : plugin.getChatCommands()){
                        getLogger().info("|" + Util.stretchNumber(i, len) + ": " + prefix + c.getLabel());
                        i++;
                    }
                }
            }
        }));

        registerInternalCommand(Command.createCommand("reload", new ICommand() {
            @Override
            public void call(String[] args) {
                getLogger().info("Reloading plugins");
                getPluginManager().reload();
                getLogger().info(PluginManager.getInstance().getPlugins().size() + " plugins loaded");
            }
        }));

        registerInternalCommand(Command.createCommand("players", new ICommand() {
            @Override
            public void call(String[] args) {
                if (players.isEmpty()){
                    getLogger().info("No one but you is online.");
                    return;
                }

                getLogger().info("Currently online: " + getOnlinePlayers().size() + " player(s)");
                String s = "";
                for (GameProfile gameProfile : getOnlinePlayers()){
                    if (gameProfile == null || gameProfile.getName() == null){
                        s += "(null), ";
                        continue;
                    }
                    s += gameProfile.getName() + ", ";
                }
                getLogger().info(s.substring(0,s.length()-2));
            }
        }));
    }

    @EventHandler
    public void onConnected(ConnectedEvent event){
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SHOULD_POST_PLAYERUPDATES = true;
            }
        }).start();
        Logger.getReader().addCompleter(completer);
    }

    @EventHandler
    public void onDisconnected(DisconnectedEvent event){
        players.clear();
        SHOULD_POST_PLAYERUPDATES = false;
    }

    public static BlockPos getPlayerPos() {
        return playerPos;
    }

    public void onSecond(){
        if (playerPos == null || reach == null) return;
        if (playerPos.yCoord > reach.yCoord-1)
            playerPos.yCoord -= 1;
        playerPos.yCoord = Math.max(playerPos.yCoord, reach.yCoord-1);
        getBot().getClient().getSession().send(new ClientPlayerPositionPacket(true, playerPos.xCoord, playerPos.yCoord, playerPos.zCoord));

        if (FLAG_ANTIAFK && System.currentTimeMillis()%5000<1000)
            getBot().getClient().getSession().send(new ClientPlayerSwingArmPacket(Hand.MAIN_HAND));
    }

    @EventHandler
    public void onBlockModified(BlockModifiedEvent event) {
        if (event.getLocation().getY() == reach.getY())
            recalculateReach();
    }

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event){
        if (event.getPacket() instanceof ServerRespawnPacket){
            SHOULD_POST_PLAYERUPDATES = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SHOULD_POST_PLAYERUPDATES = true;
                }
            }).start();
        }

        // autorespawn
        if (event.getPacket() instanceof ServerPlayerHealthPacket){
            if (((ServerPlayerHealthPacket) event.getPacket()).getHealth() <= 0F)
                event.getSession().send(new ClientRequestPacket(ClientRequest.RESPAWN));
        }
        if (event.getPacket() instanceof ServerCombatPacket) {
            if (((ServerCombatPacket) event.getPacket()).getCombatState() == CombatState.ENTITY_DEAD)
                event.getSession().send(new ClientRequestPacket(ClientRequest.RESPAWN));
        }

        if (event.getPacket() instanceof ServerPlayerPositionRotationPacket){
            ServerPlayerPositionRotationPacket packet = event.getPacket();
            playerPos.xCoord = packet.getX();
            playerPos.yCoord = packet.getY();
            playerPos.zCoord = packet.getZ();

            recalculateReach();
            return;
        }

        try{
            if (event.getPacket() instanceof ServerPlayerListEntryPacket){
                PlayerListEntryAction action = ((ServerPlayerListEntryPacket) event.getPacket()).getAction();
                PlayerListEntry[] entries = ((ServerPlayerListEntryPacket) event.getPacket()).getEntries();

                boolean post = true;
                if (entries.length > 5) post = false;
                if (action == PlayerListEntryAction.ADD_PLAYER){
                    for (PlayerListEntry entry : entries){
                        addPlayer(entry.getProfile());
                        if (entry.getProfile().getName().equals(MinecraftBot.SELF_PROFILE.getName())) continue;
                        if (SHOULD_POST_PLAYERUPDATES && post)
                            PluginManager.getInstance().fireEvent(new PlayerLogEvent(entry.getProfile(), PlayerLogEvent.Action.JOIN));
                    }
                }
                if (action == PlayerListEntryAction.REMOVE_PLAYER){
                    for (PlayerListEntry entry : entries){
                        GameProfile s = removeFromPlayers(entry.getProfile());
                        if (s == null) continue; // get rid of this disgusting line later
                        if (s.getName().equals(MinecraftBot.SELF_PROFILE.getName())) continue;

                        if (SHOULD_POST_PLAYERUPDATES && post)
                            PluginManager.getInstance().fireEvent(new PlayerLogEvent(s, PlayerLogEvent.Action.LEAVE));
                    }
                }
                if (action == PlayerListEntryAction.UPDATE_DISPLAY_NAME || action == PlayerListEntryAction.UPDATE_LATENCY || action == PlayerListEntryAction.UPDATE_GAMEMODE){
                    for (PlayerListEntry entry : entries){
                        addPlayer(entry.getProfile());
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void recalculateReach() {
        try{
            reach = playerPos.clone();
            while (WorldHandler.getBlockAt((int)reach.xCoord, (int)reach.yCoord, (int)reach.zCoord).getId() == 0){
                reach.yCoord --;
                if (reach.yCoord <= 0)
                    return;
            }
            reach.yCoord--;
        }catch (Exception e){}
    }

    private synchronized GameProfile removeFromPlayers(GameProfile gameProfile){
        for (GameProfile gameProfile1 : players){
            if (gameProfile.getId().equals(gameProfile1.getId())){
                players.remove(gameProfile1);
                return gameProfile1;
            }
        }
        return null;
    }

    private synchronized GameProfile addPlayer(GameProfile gameProfile){
        if (gameProfile == null || gameProfile.getName() == null) return gameProfile;
        players.removeAll(players.stream().filter(gameProfile1 -> gameProfile1.getId().equals(gameProfile.getId())).collect(Collectors.toList()));
        players.add(gameProfile);
        return gameProfile;
    }

    public static ArrayList<GameProfile> getOnlinePlayers() {
        return players;
    }

    public static boolean isPlayerOnline(String username) {
        return getOnlinePlayers().stream().anyMatch(gameProfile -> gameProfile.getName().equalsIgnoreCase(username));
    }

    private class PlayernameCompleter implements Completer {
        public int complete(String buffer, int cursor, java.util.List<CharSequence> candidates) {
            Preconditions.checkNotNull(candidates);

            ArrayList<String> names = new ArrayList<>();
            for (GameProfile gameProfile : getOnlinePlayers()){
                if (gameProfile == null || gameProfile.getName() == null) continue;
                names.add(gameProfile.getName());
            }

            if(buffer == null) {
                candidates.addAll(names);
            } else {
                Iterator var4 = names.iterator();

                while(var4.hasNext()) {
                    String match = (String)var4.next();
                    if(!match.startsWith(buffer)) {
                        break;
                    }

                    candidates.add(match);
                }
            }

            return candidates.isEmpty()?-1:0;
        }
    }

}
