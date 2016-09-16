/*
 * Keeps track of note styles... hence the name
 */
package mygame;

/**
 *
 * @author SeanTheBest
 */
public class NoteStyle {
    
    public float curve = 2f;
    public float curveWidth = 0.5f;
    public float curveHeight = 0.5f;
    public float borderWidth = 0.2f;
    public boolean proportionalSide = false;
    public boolean connectingLines = false;
    
    public NoteStyle(float curve, float curveWidth, float curveHeight, float borderWidth, boolean proportionalSide, boolean connectingLines) {
        this.curve = curve;
        this.curveWidth = curveWidth;
        this.curveHeight = curveHeight;
        this.borderWidth = borderWidth;
        this.proportionalSide = proportionalSide;
        this.connectingLines = connectingLines;
    }
    
}
