package ultima6;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import ultima6.Constants.ActorAnimation;

public class Actor {

    private final int id;
    private final String name;
    private final ActorAnimation icon;
    private Constants.Direction dir;
    private boolean sitting;
    private boolean moving;
    private int wx;
    private int wy;
    private float x;
    private float y;

    public Actor(ActorAnimation icon, int id, String name) {
        this.id = id;
        this.name = name;
        this.icon = icon;
    }

    public void set(int wx, int wy, float x, float y, boolean sitting) {
        this.wx = wx;
        this.wy = wy;
        this.x = x;
        this.y = y;
        this.sitting = sitting;
    }

    public int getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Animation getAnimation() {
        return this.icon.getAnimation(this.dir);
    }
    
    public TextureRegion getTexture() {
        return this.icon.getTexture(dir);
    }

    public TextureRegion getSittingTexture() {
        return this.icon.getSittingTexture(dir);
    }

    public int getWx() {
        return wx;
    }

    public void setWx(int wx) {
        this.wx = wx;
    }

    public int getWy() {
        return wy;
    }

    public void setWy(int wy) {
        this.wy = wy;
    }

    public float getX() {
        return x;
    }

    public void setX(float x) {
        this.x = x;
    }

    public float getY() {
        return y;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Constants.Direction getDir() {
        return dir;
    }

    public void setDir(Constants.Direction dir) {
        this.dir = dir;
    }

    public boolean isSitting() {
        return sitting;
    }

    public void setSitting(boolean sitting) {
        this.sitting = sitting;
    }

    public boolean isMoving() {
        return moving;
    }

    public void setMoving(boolean moving) {
        this.moving = moving;
    }

}
