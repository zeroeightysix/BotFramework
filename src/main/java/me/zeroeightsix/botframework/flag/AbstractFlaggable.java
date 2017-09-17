package me.zeroeightsix.botframework.flag;

/**
 * Created by 086 on 15/09/2017.
 */
public class AbstractFlaggable implements Flaggable {

    @Override
    public void fsetEnabled(int FLAG, boolean enabled) {
        flags.put(FLAG, enabled);
    }

    @Override
    public boolean fisEnabled(int FLAG) {
        return flags.containsKey(FLAG) ? flags.get(FLAG) : false;
    }

    @Override
    public boolean fcondition(int FLAG, boolean extra) {
        return fisEnabled(FLAG) && extra;
    }

    @Override
    public boolean fanticondition(int FLAG, boolean extra) {
        return !fisEnabled(FLAG) && extra;
    }

    @Override
    public double vgetValue(int FLAG) {
        return values.containsKey(FLAG) ? values.get(FLAG) : -1;
    }

    @Override
    public void vsetValue(int FLAG, double value) {
        values.put(FLAG, value);
    }
}
