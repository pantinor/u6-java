package ultima6;

import static ultima6.Constants.TILE_DIM;
import static ultima6.Constants.Direction;
import ultima6.Constants.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.ParallelAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import ultima6.Conversations.Conversation;
import static ultima6.Ultima6.AVATAR_TEXTURE;
import static ultima6.Ultima6.CLOCK;
import static ultima6.Ultima6.SCHEDULES;

public class GameScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final HUD hud;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        hud = new HUD(this, Ultima6.skin);

        camera = new OrthographicCamera(Ultima6.MAP_VIEWPORT_DIM, Ultima6.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 2f);

        mapPixelHeight = this.map.getHeight();

//        SequenceAction seq1 = Actions.action(SequenceAction.class);
//        seq1.addAction(Actions.delay(10f));
//        seq1.addAction(Actions.run(new ScheduleRunner()));
//        stage.addAction(Actions.forever(seq1));
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this));
    }

    @Override
    public void hide() {

    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y) {
        v.set(x * TILE_DIM, mapPixelHeight - y * TILE_DIM, 0);
    }

    @Override
    public void setCurrentMapCoords(Vector3 v) {
        float dy = (this.map == Constants.Map.WORLD ? TILE_DIM * 818 + 16 : TILE_DIM * 214 + 0);
        Vector3 tmp = camera.unproject(new Vector3(TILE_DIM * 8, dy, 0), 32, 96, Ultima6.MAP_VIEWPORT_DIM, Ultima6.MAP_VIEWPORT_DIM);
        v.set(Math.round(tmp.x / TILE_DIM) - 0, ((mapPixelHeight - Math.round(tmp.y) - TILE_DIM) / TILE_DIM) - 0, 0);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, false);
        mapViewPort.update(width, height, false);
    }

    @Override
    public void render(float delta) {

        time += delta;

        Gdx.gl.glClearColor(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (renderer == null) {
            return;
        }

        float dy = (this.map == Constants.Map.WORLD ? TILE_DIM * 992 : TILE_DIM * 248);

        camera.position.set(
                newMapPixelCoords.x + TILE_DIM * 5,
                newMapPixelCoords.y + dy,
                0);

        camera.update();

        renderer.setView(camera.combined,
                camera.position.x - 464,
                camera.position.y - 336,
                Ultima6.MAP_VIEWPORT_DIM,
                Ultima6.MAP_VIEWPORT_DIM);

        renderer.render();

        batch.begin();

        batch.draw(Ultima6.backGround, 0, 0);
        batch.draw((TextureRegion) Ultima6.AVATAR_TEXTURE, TILE_DIM * 11, TILE_DIM * 11, TILE_DIM, TILE_DIM);

        Vector3 v = new Vector3();
        setCurrentMapCoords(v);
        Ultima6.font.draw(batch, String.format("%s  [%.0f, %.0f] %s\n", CLOCK.getTimeString(), v.x, v.y, stage.getRoot().hasActions()), 200, Ultima6.SCREEN_HEIGHT - 24);

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public boolean keyUp(int keycode) {
        Vector3 v = new Vector3();
        setCurrentMapCoords(v);

        if (keycode == Keys.UP) {
            if (Ultima6.currentDirection != Direction.NORTH) {
                Ultima6.currentDirection = Direction.NORTH;
                AVATAR_TEXTURE = Constants.ActorAnimation.AVATAR.getTexture(Constants.Direction.NORTH);
                return false;
            }
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            v.y -= 1;
        } else if (keycode == Keys.DOWN) {
            if (Ultima6.currentDirection != Direction.SOUTH) {
                Ultima6.currentDirection = Direction.SOUTH;
                AVATAR_TEXTURE = Constants.ActorAnimation.AVATAR.getTexture(Constants.Direction.SOUTH);
                return false;
            }
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            v.y += 1;
        } else if (keycode == Keys.RIGHT) {
            if (Ultima6.currentDirection != Direction.EAST) {
                Ultima6.currentDirection = Direction.EAST;
                AVATAR_TEXTURE = Constants.ActorAnimation.AVATAR.getTexture(Constants.Direction.EAST);
                return false;
            }
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            v.x += 1;
        } else if (keycode == Keys.LEFT) {
            if (Ultima6.currentDirection != Direction.WEST) {
                Ultima6.currentDirection = Direction.WEST;
                AVATAR_TEXTURE = Constants.ActorAnimation.AVATAR.getTexture(Constants.Direction.WEST);
                return false;
            }
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
            v.x -= 1;
        } else if (keycode == Keys.D || keycode == Keys.U) {

        } else if (keycode == Keys.E || keycode == Keys.K) {
            Portal p = this.map.getBaseMap().getPortal((int) v.x, (int) v.y);
            if (p != null && p.getMap() != this.map) {
                Vector3 dv = p.getDestination();
                int dx = (int) dv.x;
                int dy = (int) dv.y;
                if (dx >= 0 && dy >= 0) {
                    p.getMap().getTiledMap();
                    p.getMap().getScreen().setMapPixelCoords(p.getMap().getScreen().newMapPixelCoords, dx, dy);
                    Ultima6.mainGame.setScreen(p.getMap().getScreen());
                }
            }
            return false;
        } else if (keycode == Keys.G) {
//            MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
//            if (messagesLayer != null) {
//                Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
//                while (iter.hasNext()) {
//                    MapObject obj = iter.next();
//                    float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
//                    float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
//                    if (v.x == mx && this.map.getMap().getHeight() - v.y - 1 == my) {
//                        if ("REWARD".equals(obj.getName())) {
//                            StringBuilder sb = new StringBuilder();
//                            Iterator<String> iter2 = obj.getProperties().getKeys();
//                            while (iter2.hasNext()) {
//                                String key = iter2.next();
//                                if (key.startsWith("item")) {
//                                    Item found = Andius.ITEMS_MAP.get(obj.getProperties().get(key, String.class));
//                                    sb.append("Party found ").append(found.genericName).append(". ");
//                                    Andius.CTX.players()[0].inventory.add(found);
//                                }
//                            }
//                            animateText(sb.toString(), Color.GREEN, 100, 300, 100, 400, 3);
//                            messagesLayer.getObjects().remove(obj);
//                            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
//                            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getMap().getHeight() - 1 - (int) v.y);
//                            if (cell != null) {
//                                cell.setTile(null);
//                            }
//                            return false;
//                        }
//                    }
//                }
//            }
//            //random treasure chest
//            TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("props");
//            TiledMapTileLayer.Cell cell = layer.getCell((int) v.x, this.map.getMap().getHeight() - 1 - (int) v.y);
//            if (cell != null && cell.getTile().getId() >= 1321) { //items tileset
//                RewardScreen rs = new RewardScreen(CTX, this.map, 1, 0, REWARDS.get(rand.nextInt(10)), REWARDS.get(rand.nextInt(10)));
//                mainGame.setScreen(rs);
//                cell.setTile(null);
//                return false;
//            }
        } else if (keycode == Keys.T) {
            Direction dir = Ultima6.currentDirection;
            Actor a = null;
            if (dir == Direction.NORTH) {
                a = this.map.getBaseMap().getActorAt((int) v.x, (int) v.y - 1);
            }
            if (dir == Direction.EAST) {
                a = this.map.getBaseMap().getActorAt((int) v.x + 1, (int) v.y);
            }
            if (dir == Direction.SOUTH) {
                a = this.map.getBaseMap().getActorAt((int) v.x, (int) v.y + 1);
            }
            if (dir == Direction.WEST) {
                a = this.map.getBaseMap().getActorAt((int) v.x - 1, (int) v.y);
            }
            if (a != null) {
                int npc = a.getId();
                if (npc == 373) {
                    npc = 201; //wisp
                } else if (npc == 382) {
                    npc = 202; //guard
                }
                Conversation c = Ultima6.CONVS.get(a.getId());
                if (c != null) {
                    this.stage.addActor(this.hud);
                    this.hud.set(stage, Ultima6.AVATAR, Ultima6.PARTY, c);
                }

            }
        }

        finishTurn((int) v.x, (int) v.y);

        return false;
    }

    private boolean preMove(Vector3 current, Direction dir) {

        int nx = (int) current.x;
        int ny = (int) current.y;

        if (dir == Direction.NORTH) {
            ny = (int) current.y - 1;
        }
        if (dir == Direction.SOUTH) {
            ny = (int) current.y + 1;
        }
        if (dir == Direction.WEST) {
            nx = (int) current.x - 1;
        }
        if (dir == Direction.EAST) {
            nx = (int) current.x + 1;
        }

        if (nx > this.map.getWidth() - 1 || nx < 0 || ny > this.map.getHeight() - 1 || ny < 0) {
            //Andius.mainGame.setScreen(Map.WORLD.getScreen());
            //return false;
        }

        TiledMapTileLayer layer = (TiledMapTileLayer) this.map.getTiledMap().getLayers().get("base");
        TiledMapTileLayer.Cell cell = layer.getCell(nx, this.map.getHeight() - 1 - ny);
        if (cell != null) {
            TileFlags tf = Ultima6.TILE_FLAGS.get(cell.getTile().getId() - 1);
            if (tf.isWall() || tf.isImpassable() || tf.isWet()) {
                TileFlags otf = null;
                MapLayer objLayer = this.map.getTiledMap().getLayers().get("objects");
                for (Object obj : objLayer.getObjects()) {
                    TiledMapTileMapObject tmo = (TiledMapTileMapObject) obj;
                    float ox = ((Float) tmo.getProperties().get("x")) / 16;
                    float oy = ((Float) tmo.getProperties().get("y")) / 16;
                    if (ox == nx && oy == this.map.getHeight() - 1 - ny && !tf.isWall()) {
                        int gid = (Integer) tmo.getProperties().get("gid");
                        otf = Ultima6.TILE_FLAGS.get(gid - 1);
                        break;
                    }
                }
                if (otf != null) {
                    if (otf.isWall() || otf.isImpassable() || otf.isWet()) {
                        Sounds.play(Sound.BLOCKED);
                        //return false;
                    }
                } else {
                    Sounds.play(Sound.BLOCKED);
                    //return false;
                }
            }
        } else {
            Sounds.play(Sound.BLOCKED);
            //return false;
        }

//        MapLayer messagesLayer = this.map.getTiledMap().getLayers().get("messages");
//        if (messagesLayer != null) {
//            Iterator<MapObject> iter = messagesLayer.getObjects().iterator();
//            while (iter.hasNext()) {
//                MapObject obj = iter.next();
//                float mx = obj.getProperties().get("x", Float.class) / TILE_DIM;
//                float my = obj.getProperties().get("y", Float.class) / TILE_DIM;
//                if (nx == mx && this.map.getMap().getHeight() - 1 - ny == my) {
//                    String msg = obj.getProperties().get("type", String.class);
//
//                    animateText(msg, Color.WHITE, 100, 300, 100, 400, 3);
//
//                    String itemRequired = obj.getProperties().get("itemRequired", String.class);
//                    if (itemRequired != null) {
//                        Item found = Andius.ITEMS_MAP.get(itemRequired);
//                        boolean owned = false;
//                        for (int i = 0; i < Andius.CTX.players().length && found != null; i++) {
//                            if (Andius.CTX.players()[i].inventory.contains(found)) {
//                                owned = true;
//                            }
//                        }
//                        if (!owned) {
//                            Sounds.play(Sound.NEGATIVE_EFFECT);
//                            animateText("Cannot pass!", Color.RED, 100, 200, 100, 300, 3);
//                            return false;
//                        }
//                    }
//
//                    String itemObtained = obj.getProperties().get("itemObtained", String.class);
//                    if (itemObtained != null) {
//                        Item found = Andius.ITEMS_MAP.get(itemObtained);
//                        boolean owned = false;
//                        for (int i = 0; i < Andius.CTX.players().length; i++) {
//                            if (Andius.CTX.players()[i].inventory.contains(found)) {
//                                owned = true;
//                            }
//                        }
//                        if (found != null && !owned) {
//                            Sounds.play(Sound.POSITIVE_EFFECT);
//                            Andius.CTX.players()[0].inventory.add(found);
//                            animateText(Andius.CTX.players()[0].name + " obtained a " + found.name + "!", Color.GREEN, 100, 200, 100, 300, 3);
//                        }
//                    }
//
//                    String monsterFound = obj.getProperties().get("monsterId", String.class);
//                    if (monsterFound != null) {
//                        Monster found = Andius.MONSTER_MAP.get(monsterFound);
//                        if (found != null) {
//                            Actor actor = new Actor(found.getIcon(), -1, monsterFound);
//                            MutableMonster mm = new MutableMonster(found);
//                            String msx = obj.getProperties().get("monsterSpawnX", String.class);
//                            String msy = obj.getProperties().get("monsterSpawnY", String.class);
//                            Vector3 pixelPos = new Vector3();
//                            setMapPixelCoords(pixelPos, msx != null ? Integer.valueOf(msx) : nx, msy != null ? Integer.valueOf(msy) : ny);
//                            actor.set(mm, Role.MONSTER,
//                                    msx != null ? Integer.valueOf(msx) : nx,
//                                    msy != null ? Integer.valueOf(msy) : ny,
//                                    pixelPos.x, pixelPos.y, MovementBehavior.ATTACK_AVATAR);
//                            this.map.getMap().actors.add(actor);
//                        }
//                    }
//                }
//
//            }
//        }
        return true;
    }

    @Override
    public void finishTurn(int currentX, int currentY) {
        if (CLOCK.incMoveCounter()) {
            checkSchedules();
        }
    }

    @Override
    public void log(String s) {
    }

    private void checkSchedules() {

        for (Integer npc : SCHEDULES.keySet()) {
            for (Actor actor : map.getBaseMap().getActors()) {
                if (actor.getId() == npc) {
                    for (Schedule sched : SCHEDULES.get(npc)) {
                        if (sched.getHour() == CLOCK.getHour() && CLOCK.getMinute() == 0 
                                && (sched.getDayOfWeek() == 0 || sched.getDayOfWeek() == CLOCK.getDayOfWeek())) {

                            GraphPath<LocationGraph.Location> path = new DefaultGraphPath<>();

                            map.getPathfinder().searchNodePath(
                                    map.getNodes()[actor.getWx()][actor.getWy()],
                                    map.getNodes()[sched.getX()][sched.getY()],
                                    new LocationGraph.ManhattanDistance(),
                                    path);

                            SequenceAction seq = Actions.action(SequenceAction.class);
                            for (int i = 0; i < path.getCount(); i++) {
                                LocationRunner run = new LocationRunner(actor, path.get(i));
                                seq.addAction(Actions.delay(1f));
                                seq.addAction(Actions.run(run));
                            }
                            seq.addAction(Actions.removeAction(seq));
                            stage.addAction(seq);
                            break;
                        }
                    }
                    break;
                }
            }
        }

    }

    public class LocationRunner implements Runnable {

        final Actor actor;
        final LocationGraph.Location loc;

        public LocationRunner(Actor actor, LocationGraph.Location loc) {
            this.actor = actor;
            this.loc = loc;
        }

        @Override
        public void run() {
            actor.setWx(loc.getX());
            actor.setWy(loc.getY());
            actor.setX(loc.getX() * 16);
            actor.setY((map.getHeight() - 1 - loc.getY()) * 16);
        }

    }

}
