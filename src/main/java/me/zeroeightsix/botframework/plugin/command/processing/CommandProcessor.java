package me.zeroeightsix.botframework.plugin.command.processing;

import com.google.common.collect.Lists;
import me.zeroeightsix.botframework.MinecraftBot;
import me.zeroeightsix.botframework.Util;
import me.zeroeightsix.botframework.event.CommandEvent;
import me.zeroeightsix.botframework.plugin.Plugin;
import me.zeroeightsix.botframework.plugin.command.Command;
import me.zeroeightsix.botframework.poof.EraPoofInfo;
import me.zeroeightsix.botframework.poof.PoofHandler;
import me.zeroeightsix.botframework.poof.Poofable;
import me.zeroeightsix.botframework.poof.use.ProcessChatPoof;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by 086 on 4/06/2017.
 */
public class CommandProcessor implements Poofable {

    private String COMMAND_PREFIX = "!";
    private String REGEX_RULE = "\\<(.*?)\\> ";
    private List<Pattern> REGEX_IGNORE_RULES = Lists.newArrayList();
    private String stripPrefixChar = "<";
    private String stripSuffixChar = ">";

    Plugin parent;

    Predicate<String> adminCondition = str -> false;

    UnaryOperator<String> deniedMessage = input -> "You are not allowed to execute this command, " + input;

    public CommandProcessor(Plugin parent) {
        this.parent = parent;
    }

    public CommandProcessor setCommandPrefix(String commandPrefix) {
        this.COMMAND_PREFIX = commandPrefix;
        return this;
    }

    public CommandProcessor setRegexRule(String regexRule) {
        this.REGEX_RULE = regexRule;
        return this;
    }

    public CommandProcessor setStripPrefixChar(String stripPrefixChar) {
        this.stripPrefixChar = stripPrefixChar.replace(" ", "");
        return this;
    }

    public CommandProcessor setStripSuffixChar(String stripSuffixChar) {
        this.stripSuffixChar = stripSuffixChar.replace(" ", "");
        return this;
    }

    public String getCommandPrefix() {
        return COMMAND_PREFIX;
    }

    public CommandProcessor ignoreRegex(String regex) {
        this.REGEX_IGNORE_RULES.add(Pattern.compile(regex));
        return this;
    }

    public List<Pattern> getRegexIgnoreRules() {
        return REGEX_IGNORE_RULES;
    }

    public String getRegexRule() {
        return REGEX_RULE;
    }

    public String getStripPrefixChar() {
        return stripPrefixChar;
    }

    public CommandProcessor setAdminCondition(Predicate<String> condition) {
        this.adminCondition = condition;
        return this;
    }

    public CommandProcessor setDeniedMessageCondition(UnaryOperator<String> messageOperator) {
        this.deniedMessage = messageOperator;
        return this;
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

        final String toCheck = message;
        if (REGEX_IGNORE_RULES.stream().anyMatch(p -> p.matcher(toCheck).find())) return;

        String previousMessage = message;
        String username = findUsername(REGEX_RULE, message, stripPrefixChar, stripSuffixChar);
        message = message.replaceAll(REGEX_RULE, "");

        chatPoofInfo = new ProcessChatPoof.ProcessChatPoofInfo(EraPoofInfo.Era.POST);
        chatPoofInfo.setMessage(message);
        chatPoofInfo.setSender(username);
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
                    parent.callCommand(new CommandEvent(username, command, args), adminCondition.test(username), deniedMessage.apply(username));
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
