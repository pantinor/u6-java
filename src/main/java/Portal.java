import com.badlogic.gdx.math.Vector3;

public class Portal {

    private final Constants.Map map;
    private final int sx;
    private final int sy;
    private final Vector3 dest;

    public Portal(Constants.Map map, int sx, int sy, int dx, int dy, int dz) {
        this.map = map;
        this.sx = sx;
        this.sy = sy;
        this.dest = new Vector3(dx, dy, 0);

    }

    public Constants.Map getMap() {
        return this.map;
    }

    public int getSx() {
        return sx;
    }

    public int getSy() {
        return sy;
    }

    public Vector3 getDestination() {
        return dest;
    }

}
