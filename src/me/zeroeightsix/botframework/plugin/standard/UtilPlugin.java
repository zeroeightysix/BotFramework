package me.zeroeightsix.botframework.plugin.standard;

import com.github.steveice10.mc.auth.data.GameProfile;
import com.github.steveice10.mc.protocol.data.game.ClientRequest;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntry;
import com.github.steveice10.mc.protocol.data.game.PlayerListEntryAction;
import com.github.steveice10.mc.protocol.data.game.chunk.Chunk;
import com.github.steveice10.mc.protocol.data.game.chunk.Column;
import com.github.steveice10.mc.protocol.packet.ingame.client.ClientRequestPacket;
import com.github.steveice10.mc.protocol.packet.ingame.client.player.ClientPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPlayerListEntryPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerRespawnPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerHealthPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.entity.player.ServerPlayerPositionRotationPacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.world.ServerChunkDataPacket;
import com.github.steveice10.packetlib.event.session.ConnectedEvent;
import com.github.steveice10.packetlib.event.session.DisconnectedEvent;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import jline.console.completer.Completer;
import jline.internal.Preconditions;
import me.zeroeightsix.botframework.Logger;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.PlayerLogEvent;
import me.zeroeightsix.botframework.plugin.EventHandler;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.PluginManager;
import me.zeroeightsix.botframework.plugin.command.ChatCommand;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.plugin.command.ICommand;
import me.zeroeightsix.botframework.math.BlockPos;

import java.awt.*;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Gebruiker on 10/05/2017.
 */
public class UtilPlugin extends Plugin implements EventListener {

    // location
    private static BlockPos playerPos = new BlockPos(0,0,0);
    private BlockPos reach = null;
    private Random random = new Random();

    //commands
    private static ArrayList<GameProfile> players = new ArrayList<>();
    private boolean SHOULD_LOG_PLAYERUPDATES = false;
    PlayernameCompleter completer = new PlayernameCompleter();

    public UtilPlugin() {
        super("Util", "4", "AutoRespawn, inbuilt commands, player tracker and location handling.");
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
                    getLogger().info("? There appears to be noone online.");
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
                SHOULD_LOG_PLAYERUPDATES = true;
            }
        }).start();
        Logger.getReader().addCompleter(completer);
    }

    @EventHandler
    public void onDisconnected(DisconnectedEvent event){
        players.clear();
        SHOULD_LOG_PLAYERUPDATES = false;
    }

    public static BlockPos getPlayerPos() {
        return playerPos;
    }

    public void onSecond(){
        if (playerPos == null || reach == null) return;
        if (playerPos.yCoord > reach.yCoord-1)
            playerPos.yCoord -= 1;
        playerPos.yCoord = Math.max(playerPos.yCoord, reach.yCoord-1);
        getBot().getClient().getSession().send(new ClientPlayerPositionRotationPacket(true, playerPos.xCoord, playerPos.yCoord, playerPos.zCoord, random.nextInt(360), 0));
    }

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event){

        if (event.getPacket() instanceof ServerRespawnPacket){
            SHOULD_LOG_PLAYERUPDATES = false;
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    SHOULD_LOG_PLAYERUPDATES = true;
                }
            }).start();
        }

        if (event.getPacket() instanceof ServerPlayerHealthPacket){
            if (((ServerPlayerHealthPacket) event.getPacket()).getHealth() <= 0F)
                event.getSession().send(new ClientRequestPacket(ClientRequest.RESPAWN));
        }

        if (event.getPacket() instanceof ServerPlayerPositionRotationPacket){
            ServerPlayerPositionRotationPacket packet = (ServerPlayerPositionRotationPacket) event.getPacket();
            playerPos.xCoord = packet.getX();
            playerPos.yCoord = packet.getY();
            playerPos.zCoord = packet.getZ();
            return;
        }

        if (event.getPacket() instanceof ServerChunkDataPacket){
            ServerChunkDataPacket packet = event.getPacket();
            Column column = packet.getColumn();
            int sx = column.getX();
            int sz = column.getZ();
            Rectangle area = new Rectangle(sx*16, sz*16, 16, 16);
            int hY = -1;
            if (area.contains(playerPos.xCoord, playerPos.zCoord)) { // Player is inside this chunk
                int cHc = 0;
                for (Chunk c : column.getChunks()){
                    for (int y = 0; y < 16; y++){
                        int rY = 16*cHc+y;
                        if (rY > playerPos.yCoord) continue;
                        for (int z = 0; z < 16; z++){
                            for (int x = 0; x < 16; x++){
                                if (sx*16+x == (int) playerPos.xCoord && sz*16+z == (int) playerPos.zCoord){
                                    int id = c.getBlocks().get(x, y, z).getId();
                                    if (id == 0) continue;
                                    hY = Math.max(hY, rY);
                                }
                            }
                        }
                    }
                    cHc++;
                }
                if (hY != -1)
                    reach = new BlockPos(playerPos.xCoord, hY, playerPos.zCoord);
            }
        }

        try{
            if (event.getPacket() instanceof ServerPlayerListEntryPacket){

                PlayerListEntryAction action = ((ServerPlayerListEntryPacket) event.getPacket()).getAction();
                PlayerListEntry[] entries = ((ServerPlayerListEntryPacket) event.getPacket()).getEntries();

                if (entries.length > 5) return;
                if (action == PlayerListEntryAction.UPDATE_LATENCY) return;

                if (action == PlayerListEntryAction.ADD_PLAYER){
                    for (PlayerListEntry entry : entries){
                        addPlayer(entry.getProfile());
                        if (entry.getProfile().getName().equals(MinecraftBot.SELF_PROFILE.getName())) continue;
                        if (SHOULD_LOG_PLAYERUPDATES)
                            PluginManager.getInstance().fireEvent(new PlayerLogEvent(entry.getProfile(), PlayerLogEvent.Action.JOIN));
                    }
                }
                if (action == PlayerListEntryAction.REMOVE_PLAYER){
                    for (PlayerListEntry entry : entries){
                        GameProfile s = removeFromPlayers(entry.getProfile());
                        if (s == null) continue; // get rid of this disgusting line later
                        if (s.getName().equals(MinecraftBot.SELF_PROFILE.getName())) continue;

                        if (SHOULD_LOG_PLAYERUPDATES)
                            PluginManager.getInstance().fireEvent(new PlayerLogEvent(s, PlayerLogEvent.Action.LEAVE));
                    }
                }
                if (action == PlayerListEntryAction.UPDATE_DISPLAY_NAME){
                    for (PlayerListEntry entry : entries){
                        addPlayer(entry.getProfile());
                    }
                }
            }
        }catch (Exception e){}
    }

    private GameProfile removeFromPlayers(GameProfile gameProfile){
        for (GameProfile gameProfile1 : players){
            if (gameProfile.getId().equals(gameProfile1.getId())){
                players.remove(gameProfile1);
                return gameProfile1;
            }
        }
        return null;
    }

    private GameProfile addPlayer(GameProfile gameProfile){
        if (gameProfile == null || gameProfile.getName() == null) return gameProfile;
        for (GameProfile gameProfile1 : players){
            if (gameProfile.getId().equals(gameProfile1.getId())){
                players.remove(gameProfile1);
            }
        }
        players.add(gameProfile);
        return gameProfile;
    }

    public static ArrayList<GameProfile> getOnlinePlayers() {
        return players;
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
