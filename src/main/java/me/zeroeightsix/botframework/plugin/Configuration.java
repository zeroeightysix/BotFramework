package me.zeroeightsix.botframework.plugin;

import com.google.gson.*;

import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.stream.IntStream;

/**
 * A simple but powerful configuration class for all of your configuring needs.
 * Loads and saves to JSON
 */
public class Configuration {

    /**
     * The file this configuration represents
     */
    File configurationFile;
    /**
     * The parsed JSON from the file's contents
     */
    JsonObject rootJson;

    /**
     * The user-provided defaults: if something is missing in the configuration, this is the next step.
     */
    HashMap<String, JsonElement> defaultMap = new HashMap<>();
    /**
     * Enable/disable defaults. If this is false, no defaults will be returned when getting elements.
     */
    private boolean allowDefaults = true;

    Gson gson = new Gson();

    /**
     * Returns whether or not defaults are currently enabled
     * @return
     */
    public boolean isAllowDefaults() {
        return allowDefaults;
    }

    /**
     * Set whether or not defaults should be returned when getting values from the configuration
     * @param allowDefaults
     */
    public void setAllowDefaults(boolean allowDefaults) {
        this.allowDefaults = allowDefaults;
    }

    /**
     * Allow defaults to be returned when getting values from the configuration
     */
    public void allowDefaults() { setAllowDefaults(true); }
    /**
     * Deny defaults to be returned when getting values from the configuration
     */
    public void denyDefaults() { setAllowDefaults(false); }

    /**
     * Creates a new configuration from a specified file.
     * @param configurationFile
     */
    public Configuration(File configurationFile) {
        this.configurationFile = configurationFile;

        if (!configurationFile.exists()) {
            try {
                configurationFile.createNewFile();
            } catch (IOException e) {
                throw new IllegalArgumentException(e);
            }
        }

        load(configurationFile);
    }

    /**
     * Creates a new configuration from a specified file.
     * @param filename
     */
    public Configuration(String filename) {
        this(new File(filename));
    }

    /**
     * Creates a new, empty configuration. No file is created, and this configuration contains no values.
     */
    public Configuration() {
        load("{}");
    }

    /**
     * Load this configuration again from the specified configuration file
     */
    public void reload() {
        if (configurationFile == null)
            throw new IllegalStateException("Cannot load: no file specified");
        load(configurationFile);
    }

    /**
     * Load this configuration from a file
     * @param readFrom
     */
    public void load(File readFrom) {
        try {
            JsonParser parser = new JsonParser();
            try {
                JsonObject obj = parser.parse(new FileReader(readFrom)).getAsJsonObject();
                rootJson = obj;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }catch (IllegalStateException e) {
            rootJson = new JsonParser().parse("{}").getAsJsonObject();
        }
    }

    private void load(String json) {
        try{
            rootJson = new JsonParser().parse(json).getAsJsonObject();
        }catch (IllegalStateException e) {
            rootJson = new JsonParser().parse("{}").getAsJsonObject();
        }
    }

    /**
     * Save the current configuration to the pre-defined configuration file
     */
    public void save() {
        if (configurationFile == null)
            throw new IllegalStateException("Cannot save: no file specified");
        save(configurationFile);
    }

    /**
     * Save the current configuration to the specified file
     * @param file
     */
    public void save(File file) {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String s = gson.toJson(rootJson);
        try {
            FileWriter writer = new FileWriter(file);
            writer.write(s);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get a value from the configuration as a JsonElement
     * Note: does <b>NOT</b> check for defaults. See {@link Configuration#getJsonElementOrDefault(String)} for defaults.
     * @param path
     * @return
     */
    public JsonElement getJsonElement(String path) {
        String[] list = resolvePath(path);
        JsonObject parent = rootJson;
        for (int i = 0; i < list.length-1; i++) {
            try {
                parent = parent.get(list[i]).getAsJsonObject();
            }catch (IllegalStateException e) {
                e.printStackTrace();
                return null;
            }
        }
        return parent.get(list[list.length-1]);
    }

    /**
     * Gets a value from the defaults
     * Returns null if not present
     * @param path
     * @return
     */
    public JsonElement getDefault(String path) {
        return defaultMap.get(path);
    }

    /**
     * Set the new default for specified path
     * @param path
     * @param value
     */
    public void setDefault(String path, JsonElement value) {
        defaultMap.put(path, value);
    }

    /**
     * Set the new default for specified path as number
     * @param path
     * @param number
     */
    public void setDefaultNumber(String path, Number number) {
        setDefault(path, createPrimitive(number));
    }

    /**
     * Set the new default for specified path as character
     * @param path
     * @param character
     */
    public void setDefaultCharacter(String path, Character character) {
        setDefault(path, createPrimitive(character));
    }

    /**
     * Set the new default for specified path as boolean
     * @param path
     * @param bool
     */
    public void setDefaultBoolean(String path, Boolean bool) {
        setDefault(path, createPrimitive(bool));
    }

    /**
     * Set the new default for specified path as string
     * @param path
     * @param string
     */
    public void setDefaultString(String path, String string) {
        setDefault(path, createPrimitive(string));
    }

    /**
     * Set the value of a given path in the defaults map to a string array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultStringArray(String path, String[] array) {
        setDefault(path, createArray(array));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a character array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultCharacterArray(String path, char[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToObj(i -> array[i]).toArray(Character[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a boolean array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultBooleanArray(String path, boolean[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToObj(i -> array[i]).toArray(Boolean[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a integer array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultIntArray(String path, int[] array) {
        setDefault(path, createArray(Arrays.stream(array).boxed().toArray(Integer[]::new))); // int[] -> Integer[]
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a byte array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultByteArray(String path, byte[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a float array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultFloatArray(String path, float[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a double array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultDoubleArray(String path, double[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to a short array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultShortArray(String path, short[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the defaults map to an array of longs
     * @param path
     * @param array
     * @return
     */
    public Configuration setDefaultLongArray(String path, long[] array) {
        setDefault(path, createArray(IntStream.range(0, array.length).mapToLong(i -> array[i]).boxed().toArray(Long[]::new)));
        return this;
    }

    /**
     * Get a value from the configuration as a JsonElement. If not present, returns from the defaults.
     * @param path
     * @return
     */
    public JsonElement getJsonElementOrDefault(String path) {
        JsonElement element = getJsonElement(path);
        if (element == null && isAllowDefaults()) element = getDefault(path);
        return element;
    }

    private JsonElement getJsonElementOrDefaultOrException(String path) {
        JsonElement element = getJsonElement(path);
        if (element == null && isAllowDefaults()) element = getDefault(path);
        if (element == null) throw new IllegalStateException("No value for path: " + path);
        return element;
    }

    /**
     * Get a value from the configuration as string.
     * @param path
     * @returns Requested string value
     */
    public String getString(String path) {
        try{
            return getJsonElementOrDefaultOrException(path).getAsString();
        }catch (IllegalStateException e) { return null; }
    }

    /**
     * Get a value from the configuration as boolean.
     * @param path
     * @returns Requested boolean value
     */
    public boolean getBoolean(String path) {
        return getJsonElementOrDefaultOrException(path).getAsBoolean();
    }

    /**
     * Get a value from the configuration as integer.
     * @param path
     * @returns Requested int value
     */
    public int getInt(String path) {
        return getJsonElementOrDefaultOrException(path).getAsInt();
    }

    /**
     * Get a value from the configuration as double.
     * @param path
     * @returns Requested double value
     */
    public double getDouble(String path) {
        return getJsonElementOrDefaultOrException(path).getAsDouble();
    }

    /**
     * Get a value from the configuration as long.
     * @param path
     * @returns Requested long value
     */
    public long getLong(String path) {
        return getJsonElementOrDefaultOrException(path).getAsLong();
    }

    /**
     * Get a value from the configuration as float.
     * @param path
     * @returns Requested float value
     */
    public float getFloat(String path) {
        return getJsonElementOrDefaultOrException(path).getAsFloat();
    }

    /**
     * Get a value from the configuration as character.
     * @param path
     * @returns Requested character
     */
    public char getChar(String path) {
        return (char) getJsonElementOrDefaultOrException(path).getAsInt(); // Characters are stored as numbers -> we convert the char code to the representing character
    }

    /**
     * Get an array of any type as a JsonArray
     * @param path
     * @return
     */
    public JsonArray getArray(String path) {
        return getJsonElementOrDefaultOrException(path).getAsJsonArray();
    }

    /**
     * Gets an array of integers
     * @param path
     * @return
     */
    public int[] getIntArray(String path) {
        return gson.fromJson(getJsonElementOrDefaultOrException(path), int[].class);
    }

    /**
     * Gets an array of bytes
     * @param path
     * @return
     */
    public byte[] getByteArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), byte[].class);
    }

    /**
     * Gets an array of floats
     * @param path
     * @return
     */
    public float[] getFloatArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), float[].class);
    }

    /**
     * Gets an array of doubles
     * @param path
     * @return
     */
    public double[] getDoubleArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), double[].class);
    }

    /**
     * Gets an array of shorts
     * @param path
     * @return
     */
    public short[] getShortArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), short[].class);
    }

    /**
     * Gets an array of longs
     * @param path
     * @return
     */
    public long[] getLongArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), long[].class);
    }

    /**
     * Gets an array of characters
     * @param path
     * @return
     */
    public char[] getCharacterArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), char[].class);
    }

    /**
     * Gets an array of strings
     * @param path
     * @return
     */
    public String[] getStringArray(String path) {
        return gson.fromJson(getJsonElementOrDefault(path), String[].class);
    }

    /**
     * Creates objects to make a path complete if not complete already. Will get angry if there's something in the way.
     * @param path
     * @return
     */
    private CfgEntry digPath(String path) {
        String[] list = resolvePath(path);

        JsonObject parent = rootJson;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < list.length-1; i++) {
            String part = list[i];
            sb.append(part);
            if (!parent.has(part)) parent.add(part, new JsonObject());
            else if (!parent.get(part).isJsonObject()) {
                throw new IllegalArgumentException(sb.toString() + " is not an object, can't set children!");
            }
            parent = parent.get(part).getAsJsonObject();
            sb.append('.');
        }
        return new CfgEntry(parent, list[list.length-1]);
    }

    public Configuration set(String path, JsonElement element) {
        CfgEntry entry = digPath(path);
        entry.getParent().add(entry.getName(), element);
        return this;
    }

    // These are for making arrays and primitive->boxed->jsonprimitive conversion(s)
    private JsonPrimitive createPrimitive(String val) { return new JsonPrimitive(val); }
    private JsonPrimitive createPrimitive(Number val) { return new JsonPrimitive(val); }
    private JsonPrimitive createPrimitive(Character val) { return new JsonPrimitive(val); }
    private JsonPrimitive createPrimitive(Boolean val) { return new JsonPrimitive(val); }
    private JsonArray createArray(Object[] contents) {
        JsonArray array = new JsonArray();
        for (Object o : contents) {
            if (o instanceof String) array.add(createPrimitive(o.toString()));
            else if (Number.class.isInstance(o)) array.add(createPrimitive((Number) o));
            else if (Character.class.isInstance(o)) array.add(createPrimitive((Character) o));
            else if (Boolean.class.isInstance(o)) array.add(createPrimitive((Boolean) o));
        }
        return array;
    }

    /**
     * Set the value of a given path in the configuration to a string array
     * @param path
     * @param array
     * @return
     */
    public Configuration setStringArray(String path, String[] array) {
        set(path, createArray(array));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a character array
     * @param path
     * @param array
     * @return
     */
    public Configuration setCharacterArray(String path, char[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToObj(i -> array[i]).toArray(Character[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a boolean array
     * @param path
     * @param array
     * @return
     */
    public Configuration setBooleanArray(String path, boolean[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToObj(i -> array[i]).toArray(Boolean[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a integer array
     * @param path
     * @param array
     * @return
     */
    public Configuration setIntArray(String path, int[] array) {
        set(path, createArray(Arrays.stream(array).boxed().toArray(Integer[]::new))); // int[] -> Integer[]
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a byte array
     * @param path
     * @param array
     * @return
     */
    public Configuration setByteArray(String path, byte[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a float array
     * @param path
     * @param array
     * @return
     */
    public Configuration setFloatArray(String path, float[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a double array
     * @param path
     * @param array
     * @return
     */
    public Configuration setDoubleArray(String path, double[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a short array
     * @param path
     * @param array
     * @return
     */
    public Configuration setShortArray(String path, short[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToDouble(i -> array[i]).boxed().toArray(Double[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to an array of longs
     * @param path
     * @param array
     * @return
     */
    public Configuration setLongArray(String path, long[] array) {
        set(path, createArray(IntStream.range(0, array.length).mapToLong(i -> array[i]).boxed().toArray(Long[]::new)));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given string
     * @param path
     * @param value
     * @return
     */
    public Configuration setString(String path, String value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given integer
     * @param path
     * @param value
     * @return
     */
    public Configuration setInt(String path, int value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given double
     * @param path
     * @param value
     * @return
     */
    public Configuration setDouble(String path, double value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given float
     * @param path
     * @param value
     * @return
     */
    public Configuration setFloat(String path, float value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given byte
     * @param path
     * @param value
     * @return
     */
    public Configuration setByte(String path, byte value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given short
     * @param path
     * @param value
     * @return
     */
    public Configuration setShort(String path, short value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given long
     * @param path
     * @param value
     * @return
     */
    public Configuration setLong(String path, long value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the value of a given path in the configuration to a given character
     * @param path
     * @param value
     * @return
     */
    public Configuration setChar(String path, char value) {
        set(path, createPrimitive(value));
        return this;
    }

    /**
     * Set the file this configuration saves and reads from
     * @param configurationFile
     */
    public void setFile(File configurationFile) {
        this.configurationFile = configurationFile;
    }

    /**
     * Returns true if the specified path exists
     * @param path
     * @return boolean
     */
    public boolean has(String path) {
        String[] list = resolvePath(path);
        JsonObject parent = rootJson;
        for (int i = 0; i < list.length-1; i++) {
            if (!parent.has(list[i])) return false;
            parent = parent.get(list[i]).getAsJsonObject();
        }
        if (!parent.has(list[list.length-1])) return false;
        return true;
    }

    /**
     * This configuration works with "paths", simple words seperated by dots to specify the json key (last element) and its parents.
     * f.e. the path 'a.b.c' will point to the value of 'c' in object b that is in object a which lies in the root object.
     * This function resolves that path: simply splits by every dot that is not following a backslash and then removing the escape characters from the strings.
     * @param path
     * @return
     */
    private String[] resolvePath(String path) {
        String[] list = path.split("(?<!\\\\)\\."); // every dot not prepended by a backslash
        for (int i = 0; i < list.length; i++)
            list[i] = list[i].replace("\\.", "."); // replace \. with .
        return list;
    }

    private class CfgEntry {
        JsonObject parent;
        String name;

        public JsonObject getParent() {
            return parent;
        }

        public String getName() {
            return name;
        }

        public CfgEntry(JsonObject parent, String name) {
            this.parent = parent;
            this.name = name;
        }

    }

}
