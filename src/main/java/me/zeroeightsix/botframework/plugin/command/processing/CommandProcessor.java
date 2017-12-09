package me.zeroeightsix.botframework.plugin.command.processing;

import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.CommandEvent;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.poof.EraPoofInfo;
import me.zeroeightsix.botframework.poof.PoofHandler;
import me.zeroeightsix.botframework.poof.Poofable;
import me.zeroeightsix.botframework.poof.use.ProcessChatPoof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 086 on 4/06/2017.
 */
public class CommandProcessor implements Poofable {

    private String COMMAND_PREFIX = "!";
    private String REGEX_RULE = "\\<(.*?)\\> ";
    private String stripPrefixChar = "<";
    private String stripSuffixChar = ">";

    Plugin parent;

    IsAdminCondition adminCondition = new IsAdminCondition() {
        @Override
        public Boolean commit(String input) {
            return super.commit(input);
        }
    };

    DeniedMessageCondition deniedMessageCondition = new DeniedMessageCondition();

    public CommandProcessor(Plugin parent) {
        this.parent = parent;
    }

    public void setCommandPrefix(String commandPrefix) {
        this.COMMAND_PREFIX = commandPrefix;
    }

    public void setRegexRule(String regexRule) {
        this.REGEX_RULE = regexRule;
    }

    public void setStripPrefixChar(String stripPrefixChar) {
        this.stripPrefixChar = stripPrefixChar.replace(" ", "");
    }

    public void setStripSuffixChar(String stripSuffixChar) {
        this.stripSuffixChar = stripSuffixChar.replace(" ", "");
    }

    public String getCommandPrefix() {
        return COMMAND_PREFIX;
    }

    public String getRegexRule() {
        return REGEX_RULE;
    }

    public String getStripPrefixChar() {
        return stripPrefixChar;
    }

    public void setAdminCondition(IsAdminCondition adminCondition) {
        this.adminCondition = adminCondition;
    }

    public void setDeniedMessageCondition(DeniedMessageCondition deniedMessageCondition) {
        this.deniedMessageCondition = deniedMessageCondition;
    }

    public String getStripSuffixChar() {
        return stripSuffixChar;
    }

    public void processMessage(String message){

        ProcessChatPoof.ProcessChatPoofInfo chatPoofInfo = new ProcessChatPoof.ProcessChatPoofInfo(EraPoofInfo.Era.PRE);
        chatPoofInfo.setMessage(message);
        PoofHandler.callPoof(ProcessChatPoof.class, chatPoofInfo, this);
        if (chatPoofInfo.isCancelled()) return;
        message = chatPoofInfo.getMessage();

        String previousMessage = message;
        String username = findUsername(REGEX_RULE, message, stripPrefixChar, stripSuffixChar);
        message = message.replaceAll(REGEX_RULE, "");

        chatPoofInfo = new ProcessChatPoof.ProcessChatPoofInfo(EraPoofInfo.Era.POST);
        chatPoofInfo.setMessage(message);
        PoofHandler.callPoof(ProcessChatPoof.class, chatPoofInfo, this);
        if (chatPoofInfo.isCancelled()) return;
        message = chatPoofInfo.getMessage();

        if (!message.equals(previousMessage) && username != null){
            if (message.startsWith(COMMAND_PREFIX) && message.length() > COMMAND_PREFIX.length()){
                String[] parts = message.split(" (?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"); // Split by every space if it isn't surrounded by quotes
                String command = parts[0].substring(COMMAND_PREFIX.length());

                String[] args = Util.removeElement(parts, 0);
                for (int i = 0; i < args.length; i++){
                    if (args[i]==null) continue;
                    args[i] = Util.strip(args[i], "\"");
                }

                // Remove last entry which is always null for some stupid fucking reason idk??
                ArrayList<String> w = new ArrayList<String>(Arrays.asList(args));
                w.remove(w.size()-1);
                args = w.toArray(new String[0]);

                try{
                    parent.callCommand(new CommandEvent(username, command, args), adminCondition.commit(username), deniedMessageCondition.commit(username));
                }catch (Exception e) {
                    MinecraftBot.getLogger().severe("Exception parsing command for " + parent.getName() + " (text: " + message + ")");
                    MinecraftBot.getLogger().logTrace(e);
                }
            }
        }
    }

    private String findUsername(String regexRule, String line, String prefix, String suffix){
        Matcher m = Pattern.compile(regexRule).matcher(line);
        String userName = null;

        while (m.find()){
            String g = m.group();
            g = g.replaceAll(" ", ""); // Remove spaces (usernames don't have spaces, silly!)
            if (g.startsWith(prefix)) g = g.substring(prefix.length()); // Strip prefix
            if (g.endsWith(suffix)) g = g.substring(0, g.length()-suffix.length()); // Strip suffix

            userName = g;
            break;
        }

        return userName;
    }

}
