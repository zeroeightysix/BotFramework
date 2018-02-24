package me.zeroeightsix.botframework.plugin;

import com.github.steveice10.mc.protocol.packet.ingame.client.ClientChatPacket;
import me.zeroeightsix.botframework.MinecraftBot;

import java.util.ArrayList;

public class ChatQueue {
    public boolean FLAG_LENGTHCHECK_FORMAT = true;
    private long lastMsgMS = System.currentTimeMillis();

    ArrayList<String> messages = new ArrayList<>();
    int wait = 100;
    String format = "%s";

    int maxlength = -1; // Maximum message size. -1 to ignore

    QueueCallback callback;
    Thread chatThread;

    public ChatQueue() {
        chatThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true){
                    boolean waitshort = messages.isEmpty();
                    flush();
                    try {
                        int wait = waitshort ? Math.min(100, getWaitTime()) : getWaitTime();
                        Thread.sleep(wait);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public void start() {
        chatThread.start();
    }

    public void interrupt() {
        chatThread.interrupt();
    }

    public int getMaxlength() {
        return maxlength;
    }

    public void setMaxlength(int maxlength) {
        this.maxlength = maxlength;
    }

    public void setCallback(QueueCallback callback) {
        this.callback = callback;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public void setWaitTime(int wait) {
        this.wait = wait;
    }

    public int getWaitTime() {
        return wait;
    }

    public void addMessage(String message){
        String formatted = FLAG_LENGTHCHECK_FORMAT ? String.format(format, message) : message;
        if (maxlength != -1 && formatted.length() > maxlength) {
            int dif = formatted.length()-message.length();
            String s = message.substring(maxlength-dif);
            String s1 = message.substring(0,maxlength-dif);
            addMessage(s1);
            addMessage(s);
            return;
        }
        if (callback != null) {
            StringBuffer buffer = new StringBuffer(message);
            callback.onAdded(buffer);
            messages.add(buffer.toString());
        }else
            messages.add(message);
    }

    public void flush(){
        if (messages.isEmpty() || !MinecraftBot.getInstance().getClient().getSession().isConnected()) return;
        String s = messages.get(0);
        if (callback != null)
            callback.onSent(s);
        messages.remove(0);
        MinecraftBot.getInstance().getClient().getSession().send(new ClientChatPacket(String.format(format, new Object[]{s})));
        lastMsgMS = System.currentTimeMillis();
    }

    public interface QueueCallback {
        public void onSent(String message);
        public void onAdded(StringBuffer message);
    }

    public long getTimeLastMessage() {
        return lastMsgMS;
    }
}