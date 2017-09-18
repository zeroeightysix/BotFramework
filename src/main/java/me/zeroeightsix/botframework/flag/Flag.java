package me.zeroeightsix.botframework.flag;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by 086 on 18/09/2017.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Flag {
    boolean state() default false;
}
