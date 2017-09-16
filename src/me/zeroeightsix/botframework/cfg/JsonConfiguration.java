package me.zeroeightsix.botframework.cfg;

import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.cfg.json.JSONArray;
import me.zeroeightsix.botframework.cfg.json.JSONObject;

import java.io.*;
import java.util.Arrays;

public class JsonConfiguration {

    File configurationFile;
    JSONObject config = new JSONObject();

    public JsonConfiguration(String fileName) {
        this(new File("Plugins/" + fileName));
    }

    public JsonConfiguration(File configurationFile) {
        this.configurationFile = configurationFile;

        if (!configurationFile.exists()){
            try {
                configurationFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to create bcfg file " + configurationFile.getName() + "!");
            }
        }

        try {
            InputStream is = new FileInputStream(configurationFile);
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            StringBuilder sb = new StringBuilder();

            String line = br.readLine();
            while (line!=null){
                sb.append(line);
                line = br.readLine();
            }

            br.close();
            is.close();

            String file = sb.toString();

            try{
                config = new JSONObject(file);
            }catch (Exception e){
                config = new JSONObject("{}");
            }
        } catch  (IOException e){
            MinecraftBot.getLogger().severe("Failed to read bcfg!");
        }
    }

    public void saveConfig(){
        try {
            OutputStream is = new FileOutputStream(configurationFile);
            BufferedWriter br = new BufferedWriter(new OutputStreamWriter(is));

            br.write(config.toString());

            br.close();
            is.close();
        } catch  (IOException e){
            MinecraftBot.getLogger().severe("Failed to save bcfg!");
        }
    }

    public void set(String key, Object value){
        String[] tree = key.split(".");
        JSONObject a = config;
        for (String s : tree){
            if (!a.has(s)){
                a.put(s, new JSONObject());
            }
            a = a.getJSONObject(s);
        }
        a.put(key, value);
    }

    public <T> T get(String name, T def){
        if (!config.has(name)) {
            set(name, def);
            return def;
        }
        try{
            return (T) def.getClass().cast(config.get(name));
        }catch (Exception e) {
            return def;
        }
    }

    public String[] getStringArray(String name){
        return getStringArray(name, new String[]{});
    }

    public String[] getStringArray(String name, String[] def){
        Object[] objectArray = get(name, new JSONArray(def)).toList().toArray();
        return Arrays.copyOf(objectArray, objectArray.length, String[].class);
    }

    public String get(String name){
        return get(name, "null").toString();
    }

    public JSONArray getJsonArray(String name){
        return get(name, new JSONArray("[]"));
    }

}
