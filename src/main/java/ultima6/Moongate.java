package ultima6;

import com.badlogic.gdx.graphics.g2d.Animation;
import ultima6.Constants.Map;
import ultima6.Constants.RedMoongates;

public class Moongate {

    private final Animation[] anim;
    private final RedMoongates dest;
    private final float x;
    private final float y;
    private final int wx;
    private final int wy;

    public Moongate(Animation[] anim, RedMoongates dest, float x, float y, int wx, int wy) {
        this.anim = anim;
        this.dest = dest;
        this.x = x;
        this.y = y;
        this.wx = wx;
        this.wy = wy;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public int getWx() {
        return wx;
    }

    public int getWy() {
        return wy;
    }

    public Animation[] getAnimation() {
        return anim;
    }

    public RedMoongates getDestination() {
        return dest;
    }

    public Map getMap() {
        return Constants.Map.values()[this.dest.getDz()];
    }

}
