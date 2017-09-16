package me.zeroeightsix.botframework.flag;

import java.util.HashMap;

/**
 * Created by 086 on 15/09/2017.
 */
public interface Flaggable {
    HashMap<Integer, Boolean> flags = new HashMap<>();
    public void fsetEnabled(int FLAG, boolean enabled);
    public boolean fisEnabled(int FLAG);
    public boolean fcondition(int FLAG, boolean extra);
    public boolean fanticondition(int FLAG, boolean extra);
}
