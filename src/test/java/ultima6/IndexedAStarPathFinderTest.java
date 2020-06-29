package ultima6;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

public class IndexedAStarPathFinderTest implements ApplicationListener {

    public static void main(String[] args) throws Exception {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.width = 1600;
        cfg.height = 1100;
        new LwjglApplication(new IndexedAStarPathFinderTest(), cfg);
    }

    @Override
    public void create() {

        Ultima6 u6 = new Ultima6();
        u6.create();

        for (Integer npc : Ultima6.SCHEDULES.keySet()) {
            for (Actor actor : Constants.Map.WORLD.getBaseMap().getActors()) {
                if (actor.getId() == npc) {
                    for (Schedule sched : Ultima6.SCHEDULES.get(npc)) {

                        GraphPath<LocationGraph.Location> path = new DefaultGraphPath<>();
                        
                        IndexedAStarPathFinder<LocationGraph.Location> pathfinder = Constants.Map.WORLD.getPathfinder();

                        System.out.printf("Starting searchNodePath npc [%d] from loc [%d][%d] to scheduled loc [%d][%d] for hour [%d] ",
                                actor.getId(), actor.getWx(), actor.getWy(), sched.getX(), sched.getY(), sched.getHour());
                        try {
                            pathfinder.searchNodePath(
                                    Constants.Map.WORLD.getNodes()[actor.getWx()][actor.getWy()],
                                    Constants.Map.WORLD.getNodes()[sched.getX()][sched.getY()],
                                    new LocationGraph.ManhattanDistance(),
                                    path);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        System.out.printf("Found PathFindingRunner npc [%d] path count [%d]\n", actor.getId(), path.getCount());

                    }
                    break;
                }
            }
        }

    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void render() {
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
