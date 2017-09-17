package me.zeroeightsix.botframework;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import com.google.common.reflect.ClassPath;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by 086 on 11/09/2017.
 */
public class Util {

    public static List<Class> generateClassList(String pack) {
        ArrayList<Class> classes = new ArrayList<>();

        final ClassLoader loader = Thread.currentThread().getContextClassLoader();

        try {
            for (final ClassPath.ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
                if (info.getName().startsWith(pack + ".")) {
                    final Class<?> clazz = info.load();
                    if (clazz == null) continue;
                    classes.add(clazz);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return classes;
    }

    public static String[] removeElement(String[] input, int indexToDelete) {
        ArrayList<String> result = new ArrayList<String>();

        for (int i = 0; i < input.length; i++){
            if (i != indexToDelete) result.add(input[i]);
        }

        return result.toArray(input);
    }

    public static String strip(String str, String key){
        if (str.startsWith(key) && str.endsWith(key)) return str.substring(key.length(), str.length()-key.length());
        return str;
    }

    public static void sendChatMessage(String message) {
        message = ChatAllowedCharacters.filterAllowedCharacters(message);
        if (message.length() > 255) {
            String s = message.substring(255);
            message = message.substring(0,255);
            sendChatMessage(message);
            sendChatMessage(s);
        }else{
            MinecraftBot.getInstance().getClient().getSession().send(new ClientChatPacket(message));
        }
    }

    public static String stretchNumber(int value, int length, String replacement) {
        int vLength = (value+"").length();
        int dif = length-vLength;
        if (dif <= 0)
            return value + "";
        return String.join("", Collections.nCopies(dif, replacement)) + value;
    }

    public static String stretchNumber(int value, int length) {
        return stretchNumber(value, length, " ");
    }

    /**
     * Checks if a server is available. Returns a code based on availability
     * 0 - Available
     * 1 - Down
     * 2 - Unreachable host
     * @param address
     * @param port
     * @return
     */
    public static int hostAvailabilityCheck(String address, int port) {
        try (Socket s = new Socket(address, port)) {
            return 0;
        } catch (IOException ex) {
            if (ex.getMessage().toLowerCase().contains("host"))
                return 2;
        }
        return 1;
    }

    public static String getSource(String link){
        try{
            URL u = new URL(link);
            URLConnection con = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuffer buffer = new StringBuffer();
            String inputLine;
            while ((inputLine = in.readLine()) != null)
                buffer.append(inputLine);
            in.close();

            return buffer.toString();
        }catch(Exception e){
            return null;
        }
    }

    public static String msToTime(int ms) {
        if (ms < 1000) return ms + "ms";
        return sToTime(ms%1000, "hh, mm minutes and ss seconds");
    }

    public static String sToTime(int seconds, String format) {
        long s = seconds % 60;
        long m = (seconds / 60) % 60;
        long h = (seconds / (60 * 60)) % 24;
        return format.replace("hh", h+"").replace("mm", m+"").replace("ss", s+"");
    }

}
