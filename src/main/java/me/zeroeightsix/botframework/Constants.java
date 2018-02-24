package me.zeroeightsix.botframework;

import java.util.HashMap;

/**
 * Constants class, full of absolutely changeable non-constants.
 * Also provides a way to communicate between plugins if impossible to use dependencies.
 */
public class Constants {
    private static HashMap<String, Object> valueMap = new HashMap<>();

    public static long FLAG_DOWN_TIME = 60000;         // Time to wait before checking if the server is still down
    public static int FLAG_RECONNECT_TIME = 10000;    // Time to wait before reconnecting when disconnected
    public static int FLAG_DOWN_RETRY = 20;           // Times to recheck before exiting when server appears down

    public static boolean FLAG_PRINT_LOCALE_INFO = true;
    public static boolean FLAG_PRINT_BUILD_INFO = true;
    public static boolean FLAG_PRINT_PLUGIN_INFO = true;
    public static boolean FLAG_PRINT_SERVER_INFO = true;
    public static boolean FLAG_PRINT_AUTH_INFO = true;

    /**
     * Puts an item in the constants map.
     * @param key
     * @param value
     */
    public static void push(String key, Object value) {
        valueMap.put(key, value);
    }

    /**
     * Get a value from the constants map. Returns null if no value is present for given key.
     * @param key
     * @return
     */
    public static Object get(String key) {
        return valueMap.get(key);
    }

    /**
     * Get a value from the constants map and cast it to the specified generic type. Returns null if no value is present for given key.
     * Gets angry if the present value isn't an instance of the specified type.
     * @param key
     * @param <T>
     * @return
     */
    public static <T> T getT(String key) {
        Object v = get(key);
        try{
            return (T) get(key);
        }catch (ClassCastException e) {
            throw new IllegalArgumentException("Value paired with key '" + key + "' isn't an instance of provided type!");
        }
    }

    /**
     * Returns true if the specified key has a paired value in the constants map
     * @param key
     * @return
     */
    public static boolean isPresent(String key) {
        return valueMap.containsKey(key);
    }

}
