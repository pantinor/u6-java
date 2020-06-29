package ultima6;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.util.ArrayList;
import java.util.List;
import ultima6.Constants.ActorAnimation;
import ultima6.Objects.Readiable;

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

    private int strength;
    private int dex;
    private int intelligence;
    private int hp;
    private int level;
    private int exp;
    private int magic;
    private int combat_mode;
    private int alignment;
    private int bodyArmorClass;
    private int readiedArmorClass;

    private final Readiable[] readiedObjects = new Readiable[8];

    private final List<ObjectWrapper> inventory = new ArrayList<>();

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

    public int getStrength() {
        return strength;
    }

    public void setStrength(int strength) {
        this.strength = strength;
    }

    public int getDex() {
        return dex;
    }

    public void setDex(int dex) {
        this.dex = dex;
    }

    public int getIntelligence() {
        return intelligence;
    }

    public void setIntelligence(int intelligence) {
        this.intelligence = intelligence;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getExp() {
        return exp;
    }

    public void setExp(int exp) {
        this.exp = exp;
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        this.magic = magic;
    }

    public int getCombatMode() {
        return combat_mode;
    }

    public void setCombatMode(int mode) {
        this.combat_mode = mode;
    }

    public int getAlignment() {
        return alignment;
    }

    public void setAlignment(int alignment) {
        this.alignment = alignment;
    }

    public int getBodyArmorClass() {
        return bodyArmorClass;
    }

    public void setBodyArmorClass(int bodyArmorClass) {
        this.bodyArmorClass = bodyArmorClass;
    }

    public int getReadiedArmorClass() {
        return readiedArmorClass;
    }

    public void setReadiedArmorClass(int readiedArmorClass) {
        this.readiedArmorClass = readiedArmorClass;
    }

    public Readiable getReadiable(int idx) {
        return readiedObjects[idx];
    }

    public List<ObjectWrapper> getInventory() {
        return inventory;
    }

    public static class ObjectWrapper {

        private Objects.Object object;
        private int count = 1;

        public ObjectWrapper(Objects.Object object) {
            this.object = object;
        }

        public void incrementCount() {
            this.count++;
        }

        public void decrementCount() {
            this.count--;
        }

        public Objects.Object getObject() {
            return object;
        }

        public int getCount() {
            return count;
        }

    }

}
