package me.zeroeightsix.botframework.locale.text.translate;

import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.locale.Locale;

@Deprecated
public class I18n
{
    private static final Locale localizedName = MinecraftBot.getInstance().getLocale();

    /**
     * A StringTranslate instance using the hardcoded default locale (en_US).  Used as a fallback in case the shared
     * StringTranslate singleton instance fails to translate a key.
     */
    private static final Locale fallbackTranslator = localizedName;

    @Deprecated

    /**
     * Translates a Stat name
     */
    public static String translateToLocal(String key)
    {
        return localizedName.formatMessage(key, new Object[0]);
    }

    @Deprecated

    /**
     * Translates a Stat name with format args
     */
    public static String translateToLocalFormatted(String key, Object... format)
    {
        return localizedName.formatMessage(key, format);
    }

    @Deprecated

    /**
     * Translates a Stat name using the fallback (hardcoded en_US) locale.  Looks like it's only intended to be used if
     * translateToLocal fails.
     */
    public static String translateToFallback(String key)
    {
        return fallbackTranslator.formatMessage(key, new Object[0]);
    }

    @Deprecated

    /**
     * Determines whether or not translateToLocal will find a translation for the given key.
     */
    public static boolean canTranslate(String key)
    {
        return localizedName.hasKey(key);
    }

    /**
     * Gets the time, in milliseconds since epoch, that the translation mapping was last updated
     */
    public static long getLastTranslationUpdateTimeInMilliseconds()
    {
        return 0;
    }
}
