package me.zeroeightsix.botframework.plugin;

import com.github.steveice10.packetlib.event.session.SessionEvent;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.plugin.standard.UtilPlugin;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by 086 on 9/05/2017.
 */
public class PluginManager {

    private static final PluginManager INSTANCE = new PluginManager();

    final ArrayList<Plugin> plugins = new ArrayList<>();
    final HashMap<EventListener, Plugin> listeners = new HashMap<>();

    public ArrayList<Plugin> getPlugins() {
        return plugins;
    }

    public void reload(){
        for (Plugin p : plugins)
            p.onDisable();
        listeners.clear();
        plugins.clear();

        PluginManager.getInstance().addPlugin(new UtilPlugin());
//        PluginManager.getInstance().addPlugin(new WorldHandler());

        PluginLoader pluginLoader = new PluginLoader();
        pluginLoader.loadPlugins();

        while (!pluginLoader.isDone){
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void addPlugin(Plugin newPlugin){
        if (!plugins.contains(newPlugin))
            plugins.add(newPlugin);
    }

    public void registerListener(EventListener listener, Plugin parent){
        if (!listeners.containsKey(listener))
            listeners.put(listener, parent);
    }

    public void unregisterListener(EventListener listener){
        listeners.remove(listener);
    }

    public void fireEvent(final SessionEvent event) {
        new Thread(() -> {
            if (event == null) return;
            try{
                call(event);
            }catch (Exception e){
                MinecraftBot.getInstance().getLogger().severe("Exception firing event " + event.getClass().getName());
                e.printStackTrace();
            }
        }).start();
    }

    private void call(final SessionEvent event) {
        for (Map.Entry<EventListener, Plugin> entry : listeners.entrySet()) {
            EventListener registered = entry.getKey();

            Method[] methods = registered.getClass().getMethods();

            for (int i = 0; i < methods.length; i++) {
                if (methods[i].isAnnotationPresent(EventHandler.class)) {
                    Class<?>[] methodParams = methods[i].getParameterTypes();

                    if (methodParams.length < 1) {
                        continue;
                    }

                    if (!event.getClass().getSimpleName().equals(methodParams[0].getSimpleName())) {
                        continue;
                    }

                    try {
                        methods[i].invoke(registered, event);
                    } catch (InvocationTargetException exception) {
                        System.err.println("InvocationTargetException:");
                        exception.getCause().printStackTrace();
                    } catch (Exception exception) {
                        exception.printStackTrace();
                    }
                }
            }

        }
    }

    public static Plugin getPluginByName(String name) {
        return getInstance().getPlugins().stream()
                            .filter(plugin -> plugin.getName().equalsIgnoreCase(name))
                            .findFirst()
                            .orElse(null);
    }

    public static PluginManager getInstance() {
        return INSTANCE;
    }
}
