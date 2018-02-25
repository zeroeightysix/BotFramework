package me.zeroeightsix.botframework.forge;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientPluginMessagePacket;
import com.github.steveice10.mc.protocol.packet.ingame.server.ServerPluginMessagePacket;
import com.github.steveice10.packetlib.event.session.PacketReceivedEvent;
import com.google.common.base.Joiner;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.event.StatusPacketReceivedEvent;
import me.zeroeightsix.botframework.plugin.EventHandler;
import org.apache.commons.lang3.Validate;

import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Created by 086 on 25/02/2018.
 * Utility class to complete the forge handshake
 */
public class ForgeHandshakeHandler implements EventListener {

    ArrayList<FMLHandshakeMessage.ModList.ModContainer> mods = new ArrayList<>();

    @EventHandler
    public void onStatusPacketReceived(StatusPacketReceivedEvent event) {
        if (event.getEvent().getPacket() instanceof ForgeStatusResponsePacket) {
            this.mods = ((ForgeStatusResponsePacket) event.getEvent().getPacket()).getModInfo().mods;
            MinecraftBot.getLogger().info("\tForge: obtained mod list of target server");
            if (mods.isEmpty())
                MinecraftBot.getLogger().info("\t\tNo mods present.");
            for (FMLHandshakeMessage.ModList.ModContainer container : mods)
                MinecraftBot.getLogger().info("\t\t" + container.modId + " " + container.version);
        }
    }

    @EventHandler
    public void onPacketReceived(PacketReceivedEvent event) {
        if (event.getPacket() instanceof ServerPluginMessagePacket) {
            ServerPluginMessagePacket packet = event.getPacket();
            String channel = packet.getChannel();
            byte[] data = packet.getData();
            switch (channel) {
                case "REGISTER":
                    MinecraftBot.getLogger().info("\tForge connection established, completing handshake!");
                    MinecraftBot.sendPacket(new ClientPluginMessagePacket("REGISTER", packet.getData()));
                    sendHandshakeMessage(new FMLHandshakeMessage.ClientHello());
                    sendHandshakeMessage(new FMLHandshakeMessage.ModList(mods));
                    MinecraftBot.getLogger().info("\tForge modList sent");
                    break;
                case "FML|HS":
                    ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
                    buf.writeBytes(data);
                    byte disc = buf.readByte();

                    switch (disc) {
                        case 2: // ModList
                            // Honestly.. we don't really care about what your mods are, server.
                            // I'm so sorry. If we ever were to care, here's how you should interpret this packet though:
                            /*
                            FMLHandshakeMessage.ModList modList = new FMLHandshakeMessage.ModList(new ArrayList<>());
                            modList.fromBytes(buf);
                            */
                            // However: do not worry. We're still happy you let us know, as it remains an essential part in continuing our search for a finished handshake.
                            MinecraftBot.getLogger().info("\tForge modList accepted. Waiting for forge<->client chitchat yada yada, who cares");
                            sendHandshakeMessage(new FMLHandshakeMessage.HandshakeAck(2)); // 2 = WAITINGSERVERDATA
                            break;
                        case 3: // RegistryData
                            // We also don't give a fucking flying fuck about these packets. Keep on throwing them at us until hasMore==false, jackass
                            boolean hasMore = buf.readBoolean();
                            if (!hasMore) { // This is the last RegistryData packet! woohoo!
                                MinecraftBot.getLogger().info("\tForge: Server registry data received, left unprocessed because.. we don't care.");
                                sendHandshakeMessage(new FMLHandshakeMessage.HandshakeAck(3)); // 3 = WAITINGSERVERCOMPLETE
                            }
                            break;
                        case -1: // HandshakeAck
                            FMLHandshakeMessage.HandshakeAck ack = new FMLHandshakeMessage.HandshakeAck();
                            ack.fromBytes(buf);
                            int response = ack.phase == 2 ? 4 : (ack.phase == 3 ? 5 : -1);
                            if (response == -1) { // This should never happen!
                                MinecraftBot.getLogger().info("\tForge: deep shit. Server sent unknown ack packet with phase " + ack.phase);
                                if (MinecraftBot.getInstance().isDebug()) {
                                    MinecraftBot.getLogger().info(event.getPacket().toString());
                                }
                            }else{
                                MinecraftBot.getLogger().info("\tForge: ACK " + ack.phase + " -> Responding with " + response);
                                if (response == 5) {
                                    MinecraftBot.getLogger().info("\tForge handshake expected to be complete.");
                                }
                            }
                            sendHandshakeMessage(new FMLHandshakeMessage.HandshakeAck(response));
                            break;
                    }
                    buf.release();
                    break;
            }
        }
    }

    private void sendHandshakeMessage(FMLHandshakeMessage message) {
        MinecraftBot.sendPacket(new ClientPluginMessagePacket(message.getChannel(), message.compile()));
    }



    static abstract class FMLHandshakeMessage {

        public  String getChannel() { return "FML|HS"; }
        public void toBytes(ByteBuf buffer) {}
        public void fromBytes(ByteBuf buffer) {}

        public static class ClientHello extends FMLHandshakeMessage {
            public void toBytes(ByteBuf buffer)
            {
                buffer.writeByte(1); // Discriminator (ID) = 1 for ClientHello
                buffer.writeByte(2); // FML Protocol version. We're on 2.
            }

        }
        public static class ModList extends FMLHandshakeMessage {
            private HashMap<String,String> modTags = new HashMap<>();
            public ModList(List<ModContainer> modList) {
                for (ModContainer mod : modList)
                {
                    modTags.put(mod.getModId(), mod.getVersion());
                }
            }

            @Override
            public void toBytes(ByteBuf buffer)
            {
                buffer.writeByte(2); // ID=2
                writeVarInt(buffer, modTags.size(), 2);
                for (Map.Entry<String,String> modTag: modTags.entrySet())
                {
                    writeUTF8String(buffer, modTag.getKey());
                    writeUTF8String(buffer, modTag.getValue());
                }
            }

            @Override
            public void fromBytes(ByteBuf buffer)
            {
                int modCount = readVarInt(buffer, 2);
                for (int i = 0; i < modCount; i++)
                {
                    modTags.put(readUTF8String(buffer), readUTF8String(buffer));
                }
            }

            public String modListAsString()
            {
                return Joiner.on(',').withKeyValueSeparator("@").join(modTags);
            }

            public int modListSize()
            {
                return modTags.size();
            }
            public Map<String, String> modList()
            {
                return modTags;
            }

            public static class ModContainer {
                String modId;
                String version;

                public ModContainer(String modId, String version) {
                    this.modId = modId;
                    this.version = version;
                }

                public String getModId() {
                    return modId;
                }

                public String getVersion() {
                    return version;
                }
            }
        }

        public static class HandshakeAck extends FMLHandshakeMessage {
            int phase;
            public HandshakeAck() {}
            HandshakeAck(int phase) {
                this.phase = phase;
            }
            @Override
            public void fromBytes(ByteBuf buffer) {
                phase = buffer.readByte();
            }

            @Override
            public void toBytes(ByteBuf buffer) {
                buffer.writeByte(-1); // id -1/255 (wtf forge??)
                buffer.writeByte(phase);
            }
        }

        public byte[] compile() {
            ByteBuf buf = PooledByteBufAllocator.DEFAULT.buffer();
            toBytes(buf);
            byte[] bytes = new byte[buf.readableBytes()];
            buf.readBytes(bytes);
            buf.release();
            return bytes;
        }

    }

    public static int varIntByteCount(int toCount)
    {
        return (toCount & 0xFFFFFF80) == 0 ? 1 : ((toCount & 0xFFFFC000) == 0 ? 2 : ((toCount & 0xFFE00000) == 0 ? 3 : ((toCount & 0xF0000000) == 0 ? 4 : 5)));
    }

    public static void writeVarInt(ByteBuf to, int toWrite, int maxSize)
    {
        Validate.isTrue(varIntByteCount(toWrite) <= maxSize, "Integer is too big for %d bytes", maxSize);
        while ((toWrite & -128) != 0)
        {
            to.writeByte(toWrite & 127 | 128);
            toWrite >>>= 7;
        }

        to.writeByte(toWrite);
    }

    public static void writeUTF8String(ByteBuf to, String string)
    {
        byte[] utf8Bytes = string.getBytes(StandardCharsets.UTF_8);
        Validate.isTrue(varIntByteCount(utf8Bytes.length) < 3, "The string is too long for this encoding.");
        writeVarInt(to, utf8Bytes.length, 2);
        to.writeBytes(utf8Bytes);
    }

    public static int readVarInt(ByteBuf buf, int maxSize)
    {
        Validate.isTrue(maxSize < 6 && maxSize > 0, "Varint length is between 1 and 5, not %d", maxSize);
        int i = 0;
        int j = 0;
        byte b0;

        do
        {
            b0 = buf.readByte();
            i |= (b0 & 127) << j++ * 7;

            if (j > maxSize)
            {
                throw new RuntimeException("VarInt too big");
            }
        }
        while ((b0 & 128) == 128);

        return i;
    }

    public static String readUTF8String(ByteBuf from)
    {
        int len = readVarInt(from,2);
        String str = from.toString(from.readerIndex(), len, StandardCharsets.UTF_8);
        from.readerIndex(from.readerIndex() + len);
        return str;
    }
}
