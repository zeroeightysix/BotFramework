package me.zeroeightsix.botframework.flag;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;

/**
 * Created by 086 on 15/09/2017.
 */
public class AbstractFlaggable implements Flaggable {

    protected HashMap<Integer, Boolean> flags = new HashMap<>();
    protected HashMap<Integer, Double> values = new HashMap<>();

    @Override
    public void fsetEnabled(int FLAG, boolean enabled) {
        flags.put(FLAG, enabled);
    }

    @Override
    public boolean fisEnabled(int FLAG) {
        return flags.containsKey(FLAG) ? flags.get(FLAG) : false;
    }

    @Override
    public boolean fcondition(int FLAG, boolean extra) {
        return fisEnabled(FLAG) && extra;
    }

    @Override
    public boolean fanticondition(int FLAG, boolean extra) {
        return !fisEnabled(FLAG) && extra;
    }

    @Override
    public double vgetValue(int FLAG) {
        return values.containsKey(FLAG) ? values.get(FLAG) : -1;
    }

    @Override
    public void vsetValue(int FLAG, double value) {
        values.put(FLAG, value);
    }

    protected void initializeFlags() throws IllegalAccessException {
        int FLAG = 0;
        Field[] fields = getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.isAnnotationPresent(Flag.class)) {
                if (Modifier.isFinal(field.getModifiers()))
                    throw new RuntimeException("Flagged field mustn't be final (" + field.getName() + ")");

                boolean accessible = field.isAccessible();
                if (!accessible) field.setAccessible(true);
                if (!field.getType().equals(int.class))
                    throw new RuntimeException("Flagged field id must be an integer ("  + field.getName() + ")");
                field.set(this, FLAG);
                flags.put(FLAG, field.getAnnotation(Flag.class).state());
                FLAG++;

                field.setAccessible(accessible);
            }

            if (field.isAnnotationPresent(Value.class)) {
                if (Modifier.isFinal(field.getModifiers()))
                    throw new RuntimeException("Value field mustn't be final (" + field.getName() + ")");

                boolean accessible = field.isAccessible();
                if (!accessible) field.setAccessible(true);
                if (!field.getType().equals(int.class))
                    throw new RuntimeException("Value field id must be an integer ("  + field.getName() + ")");
                field.set(this, FLAG);
                values.put(FLAG, field.getAnnotation(Value.class).value());
                FLAG++;

                field.setAccessible(accessible);
            }
        }
    }
}
