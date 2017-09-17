package me.zeroeightsix.botframework;

import me.zeroeightsix.botframework.locale.text.TextFormatting;
import org.fusesource.jansi.Ansi;
import jline.console.ConsoleReader;

import java.io.IOException;
import java.io.PrintStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * Created by Gebruiker on 9/05/2017.
 */
public class Logger {
    PrintStream out;

    private String quickprefix = Ansi.ansi().a(Ansi.Attribute.RESET).toString();

    private String FORMAT;

    private static ConsoleReader reader;

    boolean colors = false;
    private final Map<TextFormatting, String> replacements = new EnumMap<>(TextFormatting.class);

    static {
        try {
            reader = new ConsoleReader();
            reader.setExpandEvents(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Logger() {
        this(System.out, "[%1$tH:%1$tM:%1$tS]");
    }

    public Logger(String FORMAT) {
        this(System.out, FORMAT);
    }

    public Logger(PrintStream out, String FORMAT) {
        this.out = out;
        this.FORMAT = FORMAT;

        replacements.put(TextFormatting.BLACK, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).boldOff().toString());
        replacements.put(TextFormatting.DARK_BLUE,  Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).boldOff().toString());
        replacements.put(TextFormatting.DARK_GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).boldOff().toString());
        replacements.put(TextFormatting.DARK_AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).boldOff().toString());
        replacements.put(TextFormatting.DARK_RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString());
        replacements.put(TextFormatting.DARK_PURPLE,  Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).boldOff().toString());
        replacements.put(TextFormatting.GOLD, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString());
        replacements.put(TextFormatting.GRAY, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).boldOff().toString());
        replacements.put(TextFormatting.DARK_GRAY,  Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLACK).bold().toString());
        replacements.put(TextFormatting.BLUE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.BLUE).bold().toString());
        replacements.put(TextFormatting.GREEN, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.GREEN).bold().toString());
        replacements.put(TextFormatting.AQUA, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.CYAN).bold().toString());
        replacements.put(TextFormatting.RED, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).bold().toString());
        replacements.put(TextFormatting.LIGHT_PURPLE,  Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.MAGENTA).bold().toString());
        replacements.put(TextFormatting.YELLOW, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).bold().toString());
        replacements.put(TextFormatting.WHITE, Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.WHITE).bold().toString());
        replacements.put(TextFormatting.OBFUSCATED, Ansi.ansi().a(Ansi.Attribute.BLINK_SLOW).toString());
        replacements.put(TextFormatting.BOLD, Ansi.ansi().a(Ansi.Attribute.UNDERLINE_DOUBLE).toString());
        replacements.put(TextFormatting.STRIKETHROUGH, Ansi.ansi().a(Ansi.Attribute.STRIKETHROUGH_ON).toString());
        replacements.put(TextFormatting.UNDERLINE, Ansi.ansi() .a(Ansi.Attribute.UNDERLINE).toString());
        replacements.put(TextFormatting.ITALIC, Ansi.ansi().a(Ansi.Attribute.ITALIC).toString());
        replacements.put(TextFormatting.RESET, Ansi.ansi().a(Ansi.Attribute.RESET).toString());
    }

    public void rawLog(PrintStream stream, String text){
        if (colors)
            text = colorize(text);
        else
            text = colorize('\u00A7' + "r" + text);

        try {
            reader.print("\r");
            reader.print(quickprefix + text + "\n");
            reader.redrawLine();
            reader.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void logWithPrefix(PrintStream stream, String prefix, String text){
        long cTime = System.currentTimeMillis();
        rawLog(stream, "[" + prefix + "] " + String.format(FORMAT, cTime) + " " + text);
    }

    public void info(String text){
        logWithPrefix(System.out, "INFO", text);
    }

    public void severe(String text){
        quickprefix = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.RED).boldOff().toString();
        logWithPrefix(System.err, "SEVR", text);
        quickprefix = Ansi.ansi().a(Ansi.Attribute.RESET).toString();
    }

    public void warn(String text){
        quickprefix = Ansi.ansi().a(Ansi.Attribute.RESET).fg(Ansi.Color.YELLOW).boldOff().toString();
        logWithPrefix(System.err, "WARN", text);
        quickprefix = Ansi.ansi().a(Ansi.Attribute.RESET).toString();
    }

    public void logTrace(Throwable throwable){
        logTrace("", throwable);
    }

    public void logTrace(String prefix, Throwable throwable) {
        severe(throwable.toString());
        StackTraceElement[] trace = throwable.getStackTrace();
        for (StackTraceElement element : trace)
            severe("\tat " + element.toString());

        // Print suppressed exceptions, if any
        for (Throwable se : throwable.getSuppressed())
            logTrace(prefix + "> ", se);

        // Print cause, if any
        Throwable ourCause = throwable.getCause();
        if (ourCause != null){
            severe("Caused by:");
            logTrace("< ", ourCause);
        }
    }

    public void setFormat(String FORMAT) {
        this.FORMAT = FORMAT;
    }

    public void setOut(PrintStream out) {
        this.out = out;
    }

    public static void setReader(ConsoleReader reader) {
        Logger.reader = reader;
    }

    public static ConsoleReader getReader() {
        return reader;
    }

    public void enableColours() {
        this.colors = true;
    }

    public void disableColours() {
        this.colors = true;
    }

    private String colorize(String text){
        String result = text;
        for (TextFormatting color : TextFormatting.values()){
            if (replacements.containsKey(color)){
                result = result.replaceAll("(?i)" + color.toString(), replacements.get(color));
            } else {
                result = result.replaceAll("(?i)" + color.toString(), "");
            }
        }

        return result;
    }
}
