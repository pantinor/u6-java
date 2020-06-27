package ultima6;

import com.badlogic.gdx.maps.MapObject;
import java.util.ArrayList;
import java.util.List;

public class Egg {

    /* first hour of the day */
    public static final int EGG_DAY_HOUR = 6;

    /* first hour of night */
    public static final int EGG_NIGHT_HOUR = 19;

    public static enum Hatch {
        ALWAYS, DAY, NIGHT, DAY_OR_NIGHT;

        public static Hatch getHatchTime(int v) {
            return (v < 10) ? ALWAYS
                    : (v < 20) ? DAY
                            : (v < 30) ? NIGHT : DAY_OR_NIGHT;
        }
    }

    public static class StackItem {

        Objects.Object obj;
        int quantity;
        int worktype;
        int status;

        public String toString() {
            return String.format("%s spawns [%d] worktype [%d] status [%d]", obj, quantity, worktype, status);
        }
    }

    int x;
    int y;
    int z;
    int qty;
    int quality;

    List<StackItem> stack = new ArrayList<>();

    public static Egg create(MapObject obj, int mapHeight) {

        Egg egg = new Egg();

        float x = obj.getProperties().get("x", Float.class);
        float y = obj.getProperties().get("y", Float.class);
        int sx = (int) (x / 16);
        int sy = mapHeight - 1 - (int) (y / 16);
        egg.x = sx;
        egg.y = sy;

        Object pqty = obj.getProperties().get("qty");
        Object pquality = obj.getProperties().get("quality");

        egg.qty = pqty != null ? Integer.parseInt((String) pqty) : -1;
        egg.quality = pquality != null ? Integer.parseInt((String) pquality) : -1;

        for (int i = 0; i < 5; i++) {
            Object pstack = obj.getProperties().get("stack-" + i);
            String[] tmp = pstack != null ? ((String) pstack).split(",") : null;
            if (tmp != null) {
                StackItem item = new StackItem();
                item.obj = Objects.Object.valueOf(tmp[0]);
                item.quantity = Integer.parseInt(tmp[1]);
                item.worktype = Integer.parseInt(tmp[2]);
                item.status = Integer.parseInt(tmp[3]);
                egg.stack.add(item);
            }

        }

        return egg;
    }

    public String toString() {
        return String.format("egg %d,%d hatch probability [%d] hatch time [%s] stack [%s]", x, y, qty, Hatch.getHatchTime(quality), stack);
    }

}
