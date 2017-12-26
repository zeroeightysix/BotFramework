package me.zeroeightsix.botframework.poof.use;

import me.zeroeightsix.botframework.poof.IPoof;
import me.zeroeightsix.botframework.poof.PoofInfo;
import me.zeroeightsix.botframework.poof.Poofable;

import java.lang.reflect.ParameterizedType;

/**
 * Created by 086 on 21/07/2017.
 */
public abstract class Poof<T extends Poofable, S extends PoofInfo> implements IPoof<T, S> {

    private Class<T> componentclass;
    private Class<S> infoclass;

    public Poof() {
        this.componentclass = (Class<T>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[0];
        this.infoclass = (Class<S>) ((ParameterizedType) getClass()
                .getGenericSuperclass()).getActualTypeArguments()[1];
    }

    @Override
    public Class getPoofedClass() {
        return componentclass;
    }

    @Override
    public Class<S> getInfoClass() {
        return infoclass;
    }

    @Override
    public void execute(T poofed, S info) { }
}
