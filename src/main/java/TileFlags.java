public class TileFlags {

    private boolean wet;
    private boolean impassable;
    private boolean wall;
    private boolean damaging;
    private boolean lightlsb;
    private boolean lightmsb;
    private boolean boundary;
    private boolean lookthruboundary;
    private boolean ontop;
    private boolean noshootthru;
    private boolean warm;
    private boolean support;
    private boolean breakthruable;
    private boolean background;
    private int vsize;
    private int hsize;
    private String sides;

    public boolean isWet() {
        return wet;
    }

    public void setWet(boolean wet) {
        this.wet = wet;
    }

    public boolean isImpassable() {
        return impassable;
    }

    public void setImpassable(boolean impassable) {
        this.impassable = impassable;
    }

    public boolean isWall() {
        return wall;
    }

    public void setWall(boolean wall) {
        this.wall = wall;
    }

    public boolean isDamaging() {
        return damaging;
    }

    public void setDamaging(boolean damaging) {
        this.damaging = damaging;
    }

    public boolean isLightlsb() {
        return lightlsb;
    }

    public void setLightlsb(boolean lightlsb) {
        this.lightlsb = lightlsb;
    }

    public boolean isLightmsb() {
        return lightmsb;
    }

    public void setLightmsb(boolean lightmsb) {
        this.lightmsb = lightmsb;
    }

    public boolean isBoundary() {
        return boundary;
    }

    public void setBoundary(boolean boundary) {
        this.boundary = boundary;
    }

    public boolean isLookthruboundary() {
        return lookthruboundary;
    }

    public void setLookthruboundary(boolean lookthruboundary) {
        this.lookthruboundary = lookthruboundary;
    }

    public boolean isOntop() {
        return ontop;
    }

    public void setOntop(boolean ontop) {
        this.ontop = ontop;
    }

    public boolean isNoshootthru() {
        return noshootthru;
    }

    public void setNoshootthru(boolean noshootthru) {
        this.noshootthru = noshootthru;
    }

    public boolean isWarm() {
        return warm;
    }

    public void setWarm(boolean warm) {
        this.warm = warm;
    }

    public boolean isSupport() {
        return support;
    }

    public void setSupport(boolean support) {
        this.support = support;
    }

    public boolean isBreakthruable() {
        return breakthruable;
    }

    public void setBreakthruable(boolean breakthruable) {
        this.breakthruable = breakthruable;
    }

    public boolean isBackground() {
        return background;
    }

    public void setBackground(boolean background) {
        this.background = background;
    }

    public int getVsize() {
        return vsize;
    }

    public void setVsize(int vsize) {
        this.vsize = vsize;
    }

    public int getHsize() {
        return hsize;
    }

    public void setHsize(int hsize) {
        this.hsize = hsize;
    }

    public String getSides() {
        return sides;
    }

    public void setSides(String sides) {
        this.sides = sides;
    }

}
