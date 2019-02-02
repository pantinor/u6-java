
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
//import utils.PartyDeathException;
//import utils.TmxMapRenderer;
//import utils.TmxMapRenderer.CreatureLayer;
//import utils.Utils;

public class GameScreen extends BaseScreen {

    private final Map map;
    private final TmxMapRenderer renderer;
    private final Batch batch;
    private final Viewport mapViewPort;

    public GameScreen(Map map) {

        this.map = map;

        batch = new SpriteBatch();

        stage = new Stage(viewport);

        camera = new OrthographicCamera(Ultima6.MAP_VIEWPORT_DIM, Ultima6.MAP_VIEWPORT_DIM);

        mapViewPort = new ScreenViewport(camera);

//        addButtons(this.map);
        renderer = new TmxMapRenderer(this.map, this.map.getTiledMap(), 2f);

//        renderer.registerCreatureLayer(new CreatureLayer() {
//            @Override
//            public void render(float time) {
//                renderer.getBatch().draw(Andius.game_scr_avatar.getKeyFrame(time, true), newMapPixelCoords.x, newMapPixelCoords.y - TILE_DIM + 8);
//                for (Actor cr : GameScreen.this.map.getMap().actors) {
//                    if (renderer.shouldRenderCell(currentRoomId, cr.getWx(), cr.getWy())) {
//                        renderer.getBatch().draw(cr.getAnimation().getKeyFrame(time, true), cr.getX(), cr.getY() + 8);
//                    }
//                }
//            }
//        });
        mapPixelHeight = this.map.getHeight();

        setMapPixelCoords(newMapPixelCoords, 307, 349);
        //setMapPixelCoords(newMapPixelCoords, 84, 105);

//        if (this.map.getRoomIds() != null) {
//            currentRoomId = this.map.getRoomIds()[this.map.getStartX()][this.map.getStartY()][0];
//        }
    }

    @Override
    public void show() {
        //setRoomName();
        //this.map.syncRemovedActors(CTX.saveGame);
        Gdx.input.setInputProcessor(new InputMultiplexer(this, stage));
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
        batch.draw((TextureRegion) Ultima6.AVATAR.getKeyFrame(time, true), TILE_DIM * 11, TILE_DIM * 11, TILE_DIM, TILE_DIM);
        //Andius.HUD.render(batch, Andius.CTX);

        Vector3 v = new Vector3();
        setCurrentMapCoords(v);
        Ultima6.font.draw(batch, String.format("%s, %s\n", v.x, v.y), 200, Ultima6.SCREEN_HEIGHT - 64);

        batch.end();

        stage.act();
        stage.draw();

    }

    @Override
    public boolean keyUp(int keycode) {
        Vector3 v = new Vector3();
        setCurrentMapCoords(v);

        if (keycode == Keys.UP) {
            if (!preMove(v, Direction.NORTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y + TILE_DIM;
            v.y -= 1;
        } else if (keycode == Keys.DOWN) {
            if (!preMove(v, Direction.SOUTH)) {
                return false;
            }
            newMapPixelCoords.y = newMapPixelCoords.y - TILE_DIM;
            v.y += 1;
        } else if (keycode == Keys.RIGHT) {
            if (!preMove(v, Direction.EAST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x + TILE_DIM;
            v.x += 1;
        } else if (keycode == Keys.LEFT) {
            if (!preMove(v, Direction.WEST)) {
                return false;
            }
            newMapPixelCoords.x = newMapPixelCoords.x - TILE_DIM;
            v.x -= 1;
        } else if (keycode == Keys.D || keycode == Keys.U) {//elevators

        } else if (keycode == Keys.E || keycode == Keys.K) {//stairs
//            Portal p = this.map.getMap().getPortal((int) v.x, (int) v.y);
//            if (p != null && p.getMap() != this.map && !p.isElevator()) {
//                Vector3 dv = p.getDest();
//                int dx = (int) dv.x;
//                int dy = (int) dv.y;
//                if (dx >= 0 && dy >= 0) {
//                    if (p.getMap().getRoomIds() != null) {
//                        p.getMap().getScreen().currentRoomId = p.getMap().getRoomIds()[dx][dy][0];
//                    }
//                    p.getMap().getScreen().setMapPixelCoords(p.getMap().getScreen().newMapPixelCoords, dx, dy);
//                }
//                Andius.mainGame.setScreen(p.getMap().getScreen());
//            }
//            return false;
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
            TileFlags tf = Ultima6.TILE_FLAGS.get(cell.getTile().getId());
            if (tf.isWall() || tf.isImpassable() || tf.isWet()) {
                Sounds.play(Sound.BLOCKED);
                //return false;
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
//        Portal p = this.map.getMap().getPortal((int) nx, (int) ny);
//        if (p != null && p.getMap() == this.map) { //go to a portal on the same map ie ali-baba map has this
//            Vector3 dv = p.getDest();
//            if (this.map.getRoomIds() != null) {
//                currentRoomId = this.map.getRoomIds()[(int) dv.x][(int) dv.y][0];
//                setRoomName();
//            }
//            setMapPixelCoords(newMapPixelCoords, (int) dv.x, (int) dv.y);
//
//            for (Actor act : this.map.getMap().actors) {//so follower can follow thru portal
//                if (act.getMovement() == MovementBehavior.FOLLOW_AVATAR) {
//                    int dist = Utils.movementDistance(act.getWx(), act.getWy(), (int) nx, (int) ny);
//                    if (dist < 5) {
//                        act.setWx((int) dv.x);
//                        act.setWy((int) dv.y);
//                        Vector3 pixelPos = new Vector3();
//                        setMapPixelCoords(pixelPos, act.getWx(), act.getWy());
//                        act.setX(pixelPos.x);
//                        act.setY(pixelPos.y);
//                    }
//                }
//            }
//            return false;
//        }
        return true;
    }

//    @Override
//    public void endCombat(boolean isWon, andius.objects.Actor opponent) {
//        if (isWon) {
//            this.map.getMap().removeCreature(opponent);
//        }
//    }
//
//    @Override
//    public void finishTurn(int x, int y) {
//
//        if (this.map.getRoomIds() != null && this.map.getRoomIds()[x][y][1] == 0) {
//            this.currentRoomId = this.map.getRoomIds()[x][y][0];
//            setRoomName();
//        }
//
//        try {
//            this.map.getMap().moveObjects(this.map, this, x, y);
//        } catch (PartyDeathException t) {
//            partyDeath();
//        }
//    }
//
//    @Override
//    public void partyDeath() {
//    }
    @Override
    public void finishTurn(int currentX, int currentY) {
    }

    @Override
    public void log(String s) {
        //Andius.HUD.add(s);
    }

}
