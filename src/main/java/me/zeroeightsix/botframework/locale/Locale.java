package me.zeroeightsix.botframework.locale;

import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import me.zeroeightsix.botframework.MinecraftBot;

import java.io.*;
import java.util.IllegalFormatException;
import java.util.Map;
import java.util.regex.Pattern;

public class Locale
{
    /** Splits on "=" */
    private static final Splitter SPLITTER = Splitter.on('=').limit(2);
    private static final Pattern PATTERN = Pattern.compile("%(\\d+\\$)?[\\d\\.]*[df]");
    Map<String, String> properties = Maps.<String, String>newHashMap();
    private boolean unicode;

    public boolean isUnicode()
    {
        return this.unicode;
    }

    private void checkUnicode()
    {
        this.unicode = false;
        int i = 0;
        int j = 0;

        for (String s : this.properties.values())
        {
            int k = s.length();
            j += k;

            for (int l = 0; l < k; ++l)
            {
                if (s.charAt(l) >= 256)
                {
                    ++i;
                }
            }
        }

        float f = (float)i / (float)j;
        this.unicode = (double)f > 0.1D;
    }

    public void loadLocale(String locale) throws IOException {
        loadLocale(getClass().getClassLoader().getResourceAsStream(locale + ".lang"));
    }

    public void loadLocale(InputStream inputStreamIn) throws IOException {
        loadLocaleData(inputStreamIn);
        inputStreamIn.close();
    }

    public void loadLocale(File file){
        try {
            InputStream stream = new FileInputStream(file);
            loadLocale(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadLocaleData(InputStream inputStreamIn) throws IOException
    {
        BufferedReader r = new BufferedReader(new InputStreamReader(inputStreamIn));
        String s;
        while ((s=r.readLine()) != null)
        {
            if (!s.isEmpty() && s.charAt(0) != 35)
            {
                String[] astring = (String[])Iterables.toArray(SPLITTER.split(s), String.class);

                if (astring != null && astring.length == 2)
                {
                    String s1 = astring[0];
                    String s2 = PATTERN.matcher(astring[1]).replaceAll("%$1s");
                    this.properties.put(s1, s2);
                }
            }
        }

        MinecraftBot.getLogger().info("Locale: " + properties.size() + " translations loaded");

        r.close();
    }

    /**
     * Returns the translation, or the key itself if the key could not be translated.
     */
    private String translateKeyPrivate(String translateKey)
    {
        String s = (String)this.properties.get(translateKey);
        return s == null ? translateKey : s;
    }

    /**
     * Calls String.format(translateKey(key), params)
     */
    public String formatMessage(String translateKey, Object[] parameters)
    {
        String s = this.translateKeyPrivate(translateKey);

        try
        {
            return String.format(s, parameters);
        }
        catch (IllegalFormatException var5)
        {
            return s;
        }
    }

    public boolean hasKey(String key)
    {
        return this.properties.containsKey(key);
    }
}
