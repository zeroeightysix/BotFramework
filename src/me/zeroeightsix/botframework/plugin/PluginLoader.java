package me.zeroeightsix.botframework.plugin;

import me.zeroeightsix.botframework.MinecraftBot;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.ServiceLoader;

/**
 * Created by Gebruiker on 9/05/2017.
 */
public class PluginLoader {

    private final File pluginFolder;

    public boolean isDone = false;

    public PluginLoader() {
        this.pluginFolder = new File(("Plugins"));
        this.pluginFolder.mkdirs();
    }

    public void loadPlugins() {
        this.loadPlugins(null);
    }

    public void loadPlugins(Runnable callback) {
        new Thread(() -> {
            try {
                this.loadPluginJars(this.getPluginJars());
                Plugin[] externalPlugins = this.loadExternalPlugins();

                for (Plugin externalPlugin : externalPlugins) {
                    try{
                        externalPlugin.onEnable();
                        PluginManager.getInstance().addPlugin(externalPlugin);
                    }catch (Exception e){
                        MinecraftBot.getLogger().severe("Failed to load plugin '" + externalPlugin.getName() + "' due to an exception");
//                        e.printStackTrace();
                        MinecraftBot.getLogger().logTrace(e);
                    }
                }

                isDone = true;
            } catch (Exception x) {
                x.printStackTrace();
            } finally {
                if (callback != null) {
                    callback.run();
                }
            }
        }).start();
    }

    private File[] getPluginJars() {
        ArrayList<File> PluginJars = new ArrayList<>();
        PluginJars.addAll(Arrays.asList(this.pluginFolder.listFiles((f) -> f.getName().endsWith(".jar"))));
        return PluginJars.toArray(new File[PluginJars.size()]);
    }

    private void loadPluginJars(File[] PluginJars) {
        for (File PluginFile : PluginJars) {
            this.addToClassPath(PluginFile);
        }
    }

    private Plugin[] loadExternalPlugins() {
        ArrayList<Plugin> PluginList = new ArrayList<>();

        Iterator<Plugin> it = ServiceLoader.load(Plugin.class).iterator();

        while (it.hasNext()) {
            try {
                PluginList.add(it.next());
            } catch (Exception x) {
                x.printStackTrace();
            }
        }

        return PluginList.toArray(new Plugin[PluginList.size()]);
    }

    private void addToClassPath(File file) {
        URLClassLoader systemClassLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
        Class systemClassLoaderClass = URLClassLoader.class;

        try {
            Method method = systemClassLoaderClass.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            method.invoke(systemClassLoader, file.toURI().toURL());
        } catch (Exception e) {
            MinecraftBot.getInstance().getLogger().severe("Could not load plugin " + file.getName());
            e.printStackTrace();
        }

    }

}
