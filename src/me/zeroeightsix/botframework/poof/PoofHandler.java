package me.zeroeightsix.botframework.poof;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 086 on 12/09/2017.
 */
public class PoofHandler {

    static HashMap<Poofable, ArrayList<IPoof>> poofs = new HashMap<>();

    public static void addPoof(Poofable poofed, IPoof poof) {
        if (!poofs.containsKey(poofed))
            poofs.put(poofed, new ArrayList<>());
        poofs.get(poofed).add(poof);
    }

    public static void callPoof(Class<? extends IPoof> target, PoofInfo info, Poofable poofed) {
        for (Map.Entry<Poofable, ArrayList<IPoof>> poofedArrayListEntry : poofs.entrySet()) {
            for (IPoof poof : poofedArrayListEntry.getValue()) {
                if (target.isAssignableFrom(poof.getClass()))
                    poof.execute(poofed, info);
            }
        }
    }

}
