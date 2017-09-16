package me.zeroeightsix.botframework.poof;

/**
 * Created by 086 on 21/07/2017.
 */
public interface IPoof<T extends Poofable, S extends PoofInfo> {
    public void execute(T poofed, S info);
    public Class getPoofedClass();
    public Class getInfoClass();
}
