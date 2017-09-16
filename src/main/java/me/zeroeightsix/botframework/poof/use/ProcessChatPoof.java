package me.zeroeightsix.botframework.poof.use;

import me.zeroeightsix.botframework.poof.EraPoofInfo;
import me.zeroeightsix.botframework.poof.Poofable;

/**
 * Created by 086 on 29/07/2017.
 */
public abstract class ProcessChatPoof<T extends Poofable, S extends ProcessChatPoof.ProcessChatPoofInfo> extends Poof<T, S> {

    public void execute(T poofed, S info) {}
    public static class ProcessChatPoofInfo extends EraPoofInfo {
        String message;
        String sender;

        public ProcessChatPoofInfo(Era era) {
            super(era);
        }

        public String getMessage() {
            return message;
        }
        public void setMessage(String message) {
            this.message = message;
        }

        public String getSender() {
            return sender;
        }
        public void setSender(String sender) {
            this.sender = sender;
        }
    }
}
