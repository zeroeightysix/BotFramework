package me.zeroeightsix.botframework.forge;

import com.github.steveice10.mc.protocol.MinecraftProtocol;
import com.github.steveice10.mc.protocol.data.SubProtocol;
import com.github.steveice10.packetlib.Session;

/**
 * Created by 086 on 25/02/2018.
 * Extending class of MinecraftProtocol that overrides the default StatusResponsePacket so we can handle the forge-provided modList attribute
 */
public class ForgeMinecraftProtocol extends MinecraftProtocol {
    public ForgeMinecraftProtocol(SubProtocol subProtocol) {
        super(subProtocol);
    }

    @Override
    protected void setSubProtocol(SubProtocol subProtocol, boolean client, Session session) {
        super.setSubProtocol(subProtocol, client, session);
        register(0x00, ForgeStatusResponsePacket.class);
    }

}
