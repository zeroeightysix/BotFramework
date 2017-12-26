package me.zeroeightsix.botframework.poof.use;

import me.zeroeightsix.botframework.Logger;
import me.zeroeightsix.botframework.poof.PoofInfo;

/**
 * Created by 086 on 26/12/2017.
 */
public class LogPoof extends Poof<Logger, LogPoof.LogPoofInfo> {

    public static class LogPoofInfo extends PoofInfo {
        String content;
        String fullMessage;

        public LogPoofInfo(String content, String fullMessage) {
            this.content = content;
            this.fullMessage = fullMessage;
        }

        public String getContent() {
            return content;
        }

        public String getFullMessage() {
            return fullMessage;
        }

        public void setFullMessage(String fullMessage) {
            this.fullMessage = fullMessage;
        }
    }

}
