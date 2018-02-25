package me.zeroeightsix.botframework.forge;

import java.util.ArrayList;

/**
 * Created by 086 on 25/02/2018.
 */
public class ModdedServerInfo {

    String type; // Usually if not always FML
    ArrayList<ForgeHandshakeHandler.FMLHandshakeMessage.ModList.ModContainer> mods;

    public ModdedServerInfo(String type, ArrayList<ForgeHandshakeHandler.FMLHandshakeMessage.ModList.ModContainer> mods) {
        this.type = type;
        this.mods = mods;
    }

    public ArrayList<ForgeHandshakeHandler.FMLHandshakeMessage.ModList.ModContainer> getMods() {
        return mods;
    }

    public String getType() {
        return type;
    }

}
