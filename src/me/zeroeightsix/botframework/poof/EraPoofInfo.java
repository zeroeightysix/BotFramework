package me.zeroeightsix.botframework.poof;

/**
 * Created by 086 on 12/09/2017.
 */
public class EraPoofInfo extends PoofInfo implements Cancellable {

    Era era;
    boolean cancellled;

    public EraPoofInfo(Era era) {
        this.era = era;
    }

    public Era getEra() {
        return era;
    }

    @Override
    public boolean isCancelled() {
        return cancellled;
    }

    @Override
    public void cancel() {
        cancellled = true;
    }

    public enum Era {
        PRE,
        PERI,
        POST
    }
}
