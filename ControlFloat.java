/*
 * Option control of type Float
 * (these are basically wrappers to ensure our floats stay within a certain range)
 */
package mygame;

/**
 *
 * @author SeanTheBest
 */
public class ControlFloat {
    
    private float var, min, max;
    private float def; // default
    
    public ControlFloat(float var, float min, float max) {
        this.var = var;
        this.min = min;
        this.max = max;
        def = var;
    }
    
    public float getValue() {
        return var;
    }
    
    public void setValue(float newVar) {
        var = newVar;
    }
    
    public float add(float toAdd) {
        if (var + toAdd > max) {
            var = max;
            return var;
        }
        else {
            var += toAdd;
            return var;
        }
    }
    
    public float subtract(float toSub) {
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
