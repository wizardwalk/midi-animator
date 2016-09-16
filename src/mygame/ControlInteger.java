/*
 * Option control of type Integer
 * (these are basically wrappers to ensure our floats stay within a certain range)
 */
package mygame;

/**
 *
 * @author SeanTheBest
 */
public class ControlInteger {
    
    private int var, min, max;
    private int def; // default
    
    public ControlInteger(int var, int min, int max) {
        this.var = var;
        this.min = min;
        this.max = max;
        def = var;
    }
    
    public int getValue() {
        return var;
    }
    
    public void setValue(int newVar) {
        var = newVar;
    }
    
    public int add(int toAdd) {
        if (var + toAdd > max) {
            var = max;
            return var;
        }
        else {
            var += toAdd;
            return var;
        }
    }
    
    public int timesTwo() {
        if (var * 2 > max)
            var = max;
        else
            var *= 2;
        return var;
    }
    
    public int divTwo() {
        if (var / 2 < min)
            var = min;
        else
            var /= 2;
        return var;
    }
    
    public int subtract(int toSub) {
        if (var - toSub < min) {
            var = min;
            return var;
        }
        else {
            var -= toSub;
            return var;
        }
    }
    
    public void reset() {
        var = def;
    }
    
}
