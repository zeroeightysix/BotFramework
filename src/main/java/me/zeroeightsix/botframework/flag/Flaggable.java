package me.zeroeightsix.botframework.flag;

import java.util.HashMap;

/**
 * Created by 086 on 15/09/2017.
 */
public interface Flaggable {
    HashMap<Integer, Boolean> flags = new HashMap<>();
    HashMap<Integer, Double> values = new HashMap<>();

    void fsetEnabled(int FLAG, boolean enabled);
    boolean fisEnabled(int FLAG);
    boolean fcondition(int FLAG, boolean extra);
    boolean fanticondition(int FLAG, boolean extra);

    double vgetValue(int FLAG);
    void vsetValue(int FLAG, double value);
}
