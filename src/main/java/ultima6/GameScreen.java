package ultima6;

import static ultima6.Constants.TILE_DIM;
import static ultima6.Constants.Direction;
import ultima6.Constants.Map;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.ai.pfa.DefaultGraphPath;
import com.badlogic.gdx.ai.pfa.GraphPath;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.objects.TiledMapTileMapObject;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import java.util.Iterator;
import ultima6.Objects.Object;
import ultima6.Constants.RedMoongates;
import ultima6.Conversations.Conversation;
import static ultima6.Ultima6.AVATAR_TEXTURE;
import static ultima6.Ultima6.CLOCK;
import static ultima6.Ultima6.POINTER;
import static ultima6.Ultima6.SCHEDULES;

public class GameScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;
    private final Stage mapStage;

    private final HUD hud;
    private final OrbInputProcessor orbip;
    private final PointerActor pointer;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        hud = new HUD(this, Ultima6.skin);

        orbip = new OrbInputProcessor();

        camera = new OrthographicCamera(Ultima6.MAP_VIEWPORT_DIM, Ultima6.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);
        
        mapStage = new Stage(mapViewPort);

        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 2f);

        SequenceAction seq1 = Actions.action(SequenceAction.class);
        seq1.addAction(Actions.delay(5f));
        seq1.addAction(Actions.run(new GameTimer()));
        stage.addAction(Actions.forever(seq1));

        pointer = new PointerActor(POINTER);
        mapStage.addActor(pointer);
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));

        if (Ultima6.playMusic) {
            if (Ultima6.music != null) {
                Ultima6.music.stop();
            }
            Sound snd = Sound.RULE_BRIT;
            Ultima6.music = Sounds.play(snd, Ultima6.musicVolume);
        }
    }

    @Override
    public void hide() {
        if (Ultima6.music != null) {
            Ultima6.music.stop();
        }
    }

    @Override
    public void setMapPixelCoords(Vector3 v, int x, int y) {
        v.set(x * TILE_DIM, this.map.getHeight() - y * TILE_DIM, 0);
    }

    @Override
    public void setCurrentMapCoords(Vector3 v) {
        float dy = (this.map == Constants.Map.WORLD ? TILE_DIM * 818 + 16 : TILE_DIM * 214 + 0);
        Vector3 tmp = camera.unproject(new Vector3(TILE_DIM * 8, dy, 0), 32, 96, Ultima6.MAP_VIEWPORT_DIM, Ultima6.MAP_VIEWPORT_DIM);
        v.set(Math.round(tmp.x / TILE_DIM) - 0, ((this.map.getHeight() - Math.round(tmp.y) - TILE_DIM) / TILE_DIM) - 0, 0);
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

        hud.renderStrip(batch, this.map.getId(), Ultima6.CLOCK.getDay(), Ultima6.CLOCK.getHour());

        Vector3 v = new Vector3();
        setCurrentMapCoords(v);
        Ultima6.font.draw(batch, String.format("%s  [%.0f, %.0f] %s\n", CLOCK.getTimeString(), v.x, v.y, stage.getRoot().hasActions()), 200, Ultima6.SCREEN_HEIGHT - 24);

        batch.end();
        
        mapStage.act();
        mapStage.draw();

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
        } else if (keycode == Keys.U) {
            Gdx.input.setInputProcessor(orbip);
            orbip.init(null, keycode, Object.ORB_OF_THE_MOONS, v.x, v.y);
            return false;
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
                    stage.addActor(hud);
                    Gdx.input.setInputProcessor(new InputMultiplexer(stage));
                    hud.set(stage, Ultima6.AVATAR, Ultima6.PARTY, c);
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
                for (MapObject obj : objLayer.getObjects()) {
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

        return true;
    }

    @Override
    public void finishTurn(int currentX, int currentY) {

        if (this.map.getBaseMap().getMoongates().size() > 0) {
            Iterator<Moongate> iter = this.map.getBaseMap().getMoongates().iterator();
            while (iter.hasNext()) {
                Moongate m = iter.next();
                if (m.getWx() == currentX && m.getWy() == currentY) {
                    Sounds.play(Sound.MOONGATE);
                    Map dmap = m.getMap();
                    dmap.getScreen().setMapPixelCoords(dmap.getScreen().newMapPixelCoords, m.getDestination().getDx(), m.getDestination().getDy());
                    Ultima6.mainGame.setScreen(dmap.getScreen());
                    iter.remove();
                    break;
                }
            }
        }

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
                            mapStage.addAction(seq);
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

    public class GameTimer implements Runnable {

        public boolean active = true;

        @Override
        public void run() {
            if (active) {
                if (System.currentTimeMillis() - CLOCK.getLastIncrementTime() > 15 * 1000) {
                    keyUp(Keys.SPACE);
                }
            }
        }
    }

    private class OrbInputProcessor extends InputAdapter {

        private int keyCode;
        private float wx;
        private float wy;
        private ultima6.Actor player;
        private Object object;
        private boolean active;

        public void init(ultima6.Actor player, int keyCode, Object object, float wx, float wy) {
            this.object = object;
            this.player = player;
            this.keyCode = keyCode;
            this.wx = wx;
            this.wy = wy;
            this.active = true;
            GameScreen.this.pointer.setVisible(true);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            this.active = false;
            GameScreen.this.pointer.setVisible(false);

            Gdx.input.setInputProcessor(new InputMultiplexer(GameScreen.this, GameScreen.this.stage));

            int diffx = TILE_DIM * 11 + 16 - screenX;
            int diffy = TILE_DIM * 11 + 48 - screenY;

            if (Math.abs(diffx) > 80 || Math.abs(diffy) > 80) {
                //oob
            } else {
                RedMoongates dest = RedMoongates.get(diffx, diffy);
                if (dest != null) {
                    Moongate m = new Moongate(Constants.RED_MOONGATE, dest,
                            (this.wx + dest.getX()) * 16f,
                            (GameScreen.this.map.getHeight() - this.wy + dest.getY() - 1) * 16f,
                            (int) this.wx + dest.getX(),
                            (int) this.wy - dest.getY());

                    GameScreen.this.map.getBaseMap().getMoongates().clear();
                    GameScreen.this.map.getBaseMap().getMoongates().add(m);
                }
            }

            return false;
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {

            int diffx = TILE_DIM * 11 + 16 - screenX;
            int diffy = TILE_DIM * 11 + 48 - screenY;

            if (Math.abs(diffx) > 80 || Math.abs(diffy) > 80) {
                //oob
            } else {
                RedMoongates dest = RedMoongates.get(diffx, diffy);
                if (dest != null) {
                    float mx = (this.wx + dest.getX()) * TILE_DIM;
                    float my = (GameScreen.this.map.getHeight() - this.wy + dest.getY() - 1) * TILE_DIM;
                    GameScreen.this.pointer.set(mx, my);
                }
            }

            return false;
        }

    }

}
