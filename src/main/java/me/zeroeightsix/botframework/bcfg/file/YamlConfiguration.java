package me.zeroeightsix.botframework.bcfg.file;

import me.zeroeightsix.botframework.bcfg.InvalidConfigurationException;

import java.io.File;
import java.io.IOException;

/**
 * Created by 086 on 3/06/2017.
 */
public class YamlConfiguration extends YamlBasicConfiguration {

    File configurationFile;

    public YamlConfiguration(String fileName) {
        this(new File(fileName));
    }

    public YamlConfiguration(File configurationFile) {
        this.configurationFile = configurationFile;

        if (!configurationFile.exists()){
            configurationFile.getParentFile().mkdirs();
            try {
                configurationFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            load(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        try {
            super.save(configurationFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public File getConfigurationFile() {
        return configurationFile;
    }

}
