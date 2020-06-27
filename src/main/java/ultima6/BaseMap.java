package ultima6;

import java.util.ArrayList;
import java.util.List;

public class BaseMap {

    private final List<Portal> portals = new ArrayList<>();
    private final List<Actor> actors = new ArrayList<>();
    private final List<Moongate> moongates = new ArrayList<>();
    private final List<Egg> eggs = new ArrayList<>();

    public void addPortal(Constants.Map map, int sx, int sy, int dx, int dy, int dz) {
        portals.add(new Portal(map, sx, sy, dx, dy, dz));
    }

    public Portal getPortal(int sx, int sy) {
        for (Portal p : portals) {
            if (p.getSx() == sx && p.getSy() == sy) {
                return p;
            }
        }
        return null;
    }

    public Actor getActorAt(int x, int y) {
        for (Actor cr : this.actors) {
            if (cr.getWx() == x && cr.getWy() == y) {
                return cr;
            }
        }
        return null;
    }

    public List<Actor> getActors() {
        return actors;
    }

    public List<Moongate> getMoongates() {
        return moongates;
    }
    
    public List<Egg> getEggs() {
        return eggs;
    }

    public void addActor(Actor cr) {
        this.actors.add(cr);
    }

    public void removeActor(Actor cr) {
        this.actors.remove(cr);
    }
    
    

}
