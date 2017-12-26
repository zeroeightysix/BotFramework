package me.zeroeightsix.botframework.poof.use;

import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.locale.Locale;
import me.zeroeightsix.botframework.poof.EraPoofInfo;

/**
 * Created by 086 on 26/12/2017.
 */
public class LoadLocalePoof extends Poof<MinecraftBot, LoadLocalePoof.LocalePoofInfo> {

    public static class LocalePoofInfo extends EraPoofInfo {

        String localeName;
        Locale locale;

        public LocalePoofInfo(Era era, String localeName, Locale locale) {
            super(era);
            this.localeName = localeName;
            this.locale = locale;
        }

        public Locale getLocale() {
            return locale;
        }

        public String getLocaleName() {
            return localeName;
        }

        public void setLocale(Locale locale) {
            this.locale = locale;
        }
    }

}
