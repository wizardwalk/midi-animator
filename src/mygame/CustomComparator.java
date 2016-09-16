/*
 * This custom comparator determines transparency render sorting
 * based on z value rather than distance from camera...
 */
package mygame;

import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.GeometryComparator;
import com.jme3.scene.Geometry;

/**
 *
 * @author SeanTheBest
 */
public class CustomComparator implements GeometryComparator {
    
    public void setCamera(Camera cam) {
        // not implemented... no need
    }
    
    public int compare(Geometry g1, Geometry g2) {
        float z1 = g1.getWorldTranslation().z;
        float z2 = g2.getWorldTranslation().z;
        
        if (z1 == z2)
            return 0;
        else if (z1 > z2)
            return 1;
        else
            return -1;
    }
    
}