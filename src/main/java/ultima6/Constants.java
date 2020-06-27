package ultima6;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.ai.pfa.indexed.IndexedAStarPathFinder;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.utils.Array;
import java.util.HashMap;
import java.util.Iterator;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import java.io.InputStream;
import org.apache.commons.io.IOUtils;
import ultima6.LocationGraph.TileConnection;
import ultima6.LocationGraph.Location;

public class Constants {

    public static final int TILE_DIM = 32;

    public static TextureRegion[] TILES;

    static {
        TILES = new TextureRegion[32 * 64];
        TextureRegion[][] tmp = TextureRegion.split(new Texture(Gdx.files.classpath("data/u6tiles+objects.png")), 16, 16);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 32; x++) {
                TILES[y * 32 + x] = tmp[y][x];
            }
        }
    }

    public static enum Map {

        WORLD(0, "u6world.tmx"),
        //TEST(0, "test.tmx"),
        DUNGEON1(1, "u6dungeon_1.tmx"),
        DUNGEON2(2, "u6dungeon_2.tmx"),
        DUNGEON3(3, "u6dungeon_3.tmx"),
        DUNGEON4(4, "u6dungeon_4.tmx"),
        DUNGEON5(5, "u6dungeon_5.tmx");

        private int id;
        private final String tmxFile;
        private TiledMap tiledMap;
        private final BaseMap baseMap = new BaseMap();
        private BaseScreen screen;

        private IndexedAStarPathFinder<Location> pathfinder;
        private Location[][] nodes;

        private Map(int id, String tmx) {
            this.id = id;
            this.tmxFile = tmx;
        }

        public TiledMap getTiledMap() {
            if (this.tiledMap == null) {
                init();
            }
            return this.tiledMap;
        }

        public int getId() {
            return id;
        }

        public BaseMap getBaseMap() {
            return baseMap;
        }

        public BaseScreen getScreen() {
            if (this.screen == null) {
                init();
            }
            return screen;
        }

        public IndexedAStarPathFinder<Location> getPathfinder() {
            return pathfinder;
        }

        public Location[][] getNodes() {
            return nodes;
        }

        public int getHeight() {
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) tiledMap.getLayers().get("base");
            return baseLayer.getHeight();
        }

        public int getWidth() {
            TiledMapTileLayer baseLayer = (TiledMapTileLayer) tiledMap.getLayers().get("base");
            return baseLayer.getWidth();
        }

        public void init() {
            TmxMapLoader loader = new TmxMapLoader(CLASSPTH_RSLVR);
            this.tiledMap = loader.load("data/" + this.tmxFile);

            this.screen = new GameScreen(this);

            MapLayer eggLayer = this.tiledMap.getLayers().get("eggs");
            eggLayer.setVisible(false);
            Iterator<MapObject> iter = eggLayer.getObjects().iterator();
            while (iter.hasNext()) {
                MapObject obj = iter.next();
                Egg egg = Egg.create(obj, getHeight());
                this.baseMap.getEggs().add(egg);
                //System.out.println(egg);
            }

            MapLayer portalsLayer = this.tiledMap.getLayers().get("portals");
            if (portalsLayer != null) {
                iter = portalsLayer.getObjects().iterator();
                while (iter.hasNext()) {
                    MapObject obj = iter.next();

                    float x = obj.getProperties().get("x", Float.class);
                    float y = obj.getProperties().get("y", Float.class);
                    int sx = (int) (x / 16);
                    int sy = getHeight() - 1 - (int) (y / 16);

                    Object pdx = obj.getProperties().get("portal_dest_x");
                    Object pdy = obj.getProperties().get("portal_dest_y");
                    Object pdz = obj.getProperties().get("portal_dest_z");

                    int dx = pdx != null ? Integer.parseInt((String) pdx) : -1;
                    int dy = pdy != null ? Integer.parseInt((String) pdy) : -1;
                    int dz = pdz != null ? Integer.parseInt((String) pdz) : -1;

                    Map dm = Map.values()[dz];

                    this.baseMap.addPortal(dm, sx, sy, dx, dy, dz);

                }
            }

            MapLayer npcLayer = this.tiledMap.getLayers().get("actors");
            if (npcLayer != null) {
                iter = npcLayer.getObjects().iterator();
                while (iter.hasNext()) {
                    MapObject obj = iter.next();
                    String name = obj.getName();
                    Object tmp = obj.getProperties().get("npc");
                    if (tmp == null || name.equals("AVATAR")) {
                        continue;
                    }
                    int npc = Integer.valueOf((String) tmp);
                    int tile = obj.getProperties().get("gid", Integer.class) - 1;
                    float x = obj.getProperties().get("x", Float.class);
                    float y = obj.getProperties().get("y", Float.class);
                    int sx = (int) (x / 16);
                    int sy = (int) (y / 16);

                    if (tile == 684 || tile == 685) {
                        tile = 1648;//chuckles sleeping at start
                    }
                    if (tile == 0) {
                        tile = 416;//an empty tile for shrines to show the force field
                    }

                    ActorAnimation icon = ActorAnimation.find(tile);
                    //System.out.printf("Loading actor: %s %d %d %s\n", name, npc, tile, icon);

                    Actor actor = new Actor(icon, npc, name);

                    boolean sitting = icon.isSittingTile(tile);

                    actor.set(sx, getHeight() - 1 - sy, x, y, sitting);

                    actor.setDir(icon.direction(tile));

                    //System.out.printf("Loading actor: %s %d %d %s at [%d] [%d]\n", name, npc, tile, icon, sx, getHeight() - 1 - sy);
                    this.baseMap.addActor(actor);
                }
            }

            TiledMapTileLayer baseLayer = (TiledMapTileLayer) this.tiledMap.getLayers().get("base");

            int numRows = getHeight();
            int numCols = getWidth();
            this.nodes = new Location[numCols][numRows];
            Array<Location> indexedNodes = new Array<>(numCols * numRows);
            int index = 0;
            for (int y = 0; y < numRows; y++) {
                for (int x = 0; x < numCols; x++, index++) {
                    TiledMapTileLayer.Cell cell = baseLayer.getCell(x, numRows - 1 - y);
                    if (cell != null) {
                        TileFlags tf = Ultima6.TILE_FLAGS.get(cell.getTile().getId() - 1);
                        nodes[x][y] = new Location(index, x, y, tf);
                        indexedNodes.add(nodes[x][y]);
                    }
                }
            }
            for (int y = 0; y < numRows; y++) {
                for (int x = 0; x < numCols; x++) {
                    if (x + 1 < nodes.length) {
                        nodes[x][y].getConnections().add(new TileConnection(nodes[x][y], nodes[x + 1][y]));
                    }
                    if (x - 1 >= 0) {
                        nodes[x][y].getConnections().add(new TileConnection(nodes[x][y], nodes[x - 1][y]));
                    }
                    if (y + 1 < nodes[x].length) {
                        nodes[x][y].getConnections().add(new TileConnection(nodes[x][y], nodes[x][y + 1]));
                    }
                    if (y - 1 >= 0) {
                        nodes[x][y].getConnections().add(new TileConnection(nodes[x][y], nodes[x][y - 1]));
                    }
                }
            }

            LocationGraph graph = new LocationGraph(indexedNodes);
            this.pathfinder = new IndexedAStarPathFinder<>(graph);

            TiledMapTileSets sets = this.tiledMap.getTileSets();
            TiledMapTileSet set = sets.getTileSet(0);
            for (PaletteCycledTiles at : PaletteCycledTiles.values()) {
                for (int i = 0; i < at.tiles.length; i++) {
                    Array<StaticTiledMapTile> staticTiles = new Array<>();
                    Object[] frames = at.getAnims()[i].getKeyFrames();
                    for (Object frame : frames) {
                        staticTiles.add(new StaticTiledMapTile((TextureRegion) frame));
                    }
                    AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(.2f, staticTiles);
                    animatedTile.setId(at.tiles[i] + 1);
                    set.putTile(at.tiles[i] + 1, animatedTile);

                    for (int y = 0; y < baseLayer.getHeight(); y++) {
                        for (int x = 0; x < baseLayer.getWidth(); x++) {
                            Cell cell = baseLayer.getCell(x, y);
                            if (cell != null) {
                                if (cell.getTile().getId() == at.tiles[i] + 1) {
                                    cell.setTile(animatedTile);
                                }
                            }
                        }
                    }
                }

            }

        }

    }

    public static final FileHandleResolver CLASSPTH_RSLVR = new FileHandleResolver() {
        @Override
        public FileHandle resolve(String fileName) {
            return Gdx.files.classpath(fileName);
        }
    };

    public static int MOVETYPE_U6_NONE = 0;
    public static int MOVETYPE_U6_LAND = 1;
    public static int MOVETYPE_U6_WATER_LOW = 2;// skiffs, rafts
    public static int MOVETYPE_U6_WATER_HIGH = 3; // ships
    public static int MOVETYPE_U6_AIR_LOW = 4; // balloon, birds... this movetype cannot cross mountain tops.
    public static int MOVETYPE_U6_AIR_HIGH = 5; // dragons
    public static int MOVETYPE_U6_ETHEREAL = 6;

    public static enum Direction {
        NORTH, EAST, SOUTH, WEST;
    }

    public static enum ActorAnimation {
        FORCE_FIELD(416, 51, 0, 0, 1),//shrines tile 0 -> 416 empty tile
        GIANT_RAT(1280, 342, 2, 2, 1),
        INSECTS(1288, 343, 0, 0, 1),
        GIANT_BAT(1292, 344, 0, 0, 1),
        GIANT_SQUID(1296, 345, 0, 0, 1),
        SEA_SERPENT(1300, 346, 2, 2, 1),
        REAPER(1308, 347, 0, 0, 1),
        SHEEP(1312, 348, 2, 2, 1),
        DOG(1320, 349, 2, 2, 1),
        DEER(1328, 350, 2, 2, 1),
        WOLF(1336, 351, 2, 2, 1),
        GHOST(1344, 352, 0, 0, 1),
        GREMLIN(1348, 353, 0, 0, 1),
        MOUSE(1352, 354, 1, 1, 1),
        GAZER(1356, 355, 0, 0, 1),
        BIRD(1360, 356, 0, 0, 1),
        CORPSER(1364, 357, 0, 0, 1),
        SNAKE(1368, 358, 2, 2, 1),
        RABBIT(1376, 359, 0, 0, 1),
        ROT_WORMS(1380, 360, 0, 0, 1),
        GIANT_SPIDER(1384, 361, 2, 2, 1),
        TANGLE_VINE(1456, 365, 1, 2, 1),
        DAEMON(1464, 367, 2, 2, 1),
        SKELETON(1472, 368, 2, 2, 1),
        DRAKE(1480, 369, 2, 2, 1),
        HEADLESS(1488, 370, 2, 2, 1),
        TROLL(1496, 371, 2, 2, 1),
        MONGBAT(1504, 372, 2, 2, 1),
        WISP(1512, 373, 0, 0, 1),
        HYDRA(1516, 374, 0, 0, 1),
        SLIME(1520, 375, 0, 0, 0),
        ACID_SLUG(1452, 364, 0, 0, 1),
        WINGED_GARGOYLE(1392, 362, 3, 12, 4),
        GARGOYLE(1440, 363, 3, 3, 1),
        FIGHTER(1536, 376, 3, 4, 1),
        SWASHBUCKLER(1552, 377, 3, 4, 1),
        MAGE(1568, 378, 3, 4, 1),
        VILLAGER(1584, 379, 3, 4, 1),
        MERCHANT(1600, 380, 3, 4, 1),
        CHILD(1616, 381, 3, 4, 1),
        GUARD(1632, 382, 3, 4, 1),
        JESTER(1648, 383, 3, 4, 1),
        PEASANT(1664, 384, 3, 4, 1),
        FARMER(1680, 385, 3, 4, 1),
        MUSICIAN(1696, 386, 3, 4, 1),
        WOMAN(1712, 387, 3, 4, 1),
        CAT(1728, 388, 1, 1, 1),
        LORD_BRITISH(1760, 409, 3, 4, 1),
        AVATAR(1776, 410, 3, 4, 1),
        DRAGON(1792, 411, 2, 2, 1),
        SILVER_SERPENT(1856, 413, 1, 2, 1),
        CYCLOPS(1888, 424, 2, 8, 4),
        SHIP(1832, 412, 1, 2, 6),
        SKIFF(1872, 414, 1, 1, 1),
        RAFT(1876, 415, 0, 0, 0),
        GIANT_SCORPION(1952, 426, 2, 2, 2),
        GIANT_ANT(1968, 427, 2, 2, 2),
        COW(1984, 428, 2, 2, 2),
        ALLIGATOR(2000, 429, 2, 2, 2),
        HORSE(2016, 430, 2, 2, 2),
        STOCKS(903, 263, 0, 0, 1);

        private final int tile;
        private final int object;
        private final int framesPerDirection;
        private final int tilesPerDirection;
        private final int tilesPerFrame;

        private final java.util.Map<Direction, Animation<TextureRegion>> animMap = new HashMap<>();
        private final java.util.Map<Direction, TextureRegion> textureMap = new HashMap<>();
        private final java.util.Map<Direction, TextureRegion> sittingTextureMap = new HashMap<>();

        private ActorAnimation(
                int tile,
                int object,
                int framesPerDirection,
                int tilesPerDirection,
                int tilesPerFrame) {

            this.tile = tile;
            this.object = object;
            this.framesPerDirection = framesPerDirection;
            this.tilesPerDirection = tilesPerDirection;
            this.tilesPerFrame = tilesPerFrame;
        }

        public static ActorAnimation find(int tile) {
            for (ActorAnimation aa : ActorAnimation.values()) {
                if (aa.tilesPerDirection > 0) {
                    if (tile >= aa.tile && tile < aa.tile + aa.tilesPerDirection * 4) {
                        return aa;
                    }
                } else if (tile >= aa.tile && tile <= aa.tile + 4) {
                    return aa;
                }
            }
            return null;
        }

        public Direction direction(int tile) {
            Direction dir = Direction.NORTH;
            if (tile >= this.tile && tile < this.tile + this.tilesPerDirection) {
                dir = Direction.NORTH;
            }
            if (tile >= this.tile + this.tilesPerDirection && tile < this.tile + this.tilesPerDirection * 2) {
                dir = Direction.EAST;
            }
            if (tile >= this.tile + this.tilesPerDirection * 2 && tile < this.tile + this.tilesPerDirection * 3) {
                dir = Direction.SOUTH;
            }
            if (tile >= this.tile + this.tilesPerDirection * 3 && tile < this.tile + this.tilesPerDirection * 4) {
                dir = Direction.WEST;
            }
            return dir;
        }

        public boolean isSittingTile(int tile) {
            if (this.framesPerDirection == 3) {
                if (tile == this.tile + this.tilesPerDirection - 1) {
                    return true;
                }
                if (tile == this.tile + this.tilesPerDirection * 2 - 1) {
                    return true;
                }
                if (tile == this.tile + this.tilesPerDirection * 3 - 1) {
                    return true;
                }
                if (tile == this.tile + this.tilesPerDirection * 4 - 1) {
                    return true;
                }
            }
            return false;
        }

        public Animation<TextureRegion> getAnimation(Direction dir) {
            return this.animMap.get(dir);
        }

        public TextureRegion getTexture(Direction dir) {
            return this.textureMap.get(dir);
        }

        public TextureRegion getSittingTexture(Direction dir) {
            return this.sittingTextureMap.get(dir);
        }

        public static void init() throws Exception {
            TextureRegion[] tiles = TILES;

            for (ActorAnimation aa : ActorAnimation.values()) {

                if (aa == ActorAnimation.SHIP) {
                    for (int i = 0; i < 4; i++) {
                        TextureRegion tr = new TextureRegion(mergeTiles(Direction.values()[i],
                                tiles[aa.tile + 0 + 2 * i], tiles[aa.tile + 1 + 2 * i],
                                tiles[aa.tile + 8 + 2 * i], tiles[aa.tile + 9 + 2 * i],
                                tiles[aa.tile + 16 + 2 * i], tiles[aa.tile + 17 + 2 * i]
                        ));
                        aa.textureMap.put(Direction.values()[i], tr);
                    }
                } else if (aa == ActorAnimation.STOCKS) {
                    TextureRegion tr1 = new TextureRegion(mergeTiles(Direction.WEST, tiles[312], tiles[314]));
                    TextureRegion tr2 = new TextureRegion(mergeTiles(Direction.WEST, tiles[313], tiles[315]));
                    Array<TextureRegion> arr = new Array<>();
                    arr.add(tr1);
                    arr.add(tr2);
                    Animation a = new Animation(.3f, arr);
                    for (int i = 0; i < 4; i++) {
                        aa.animMap.put(Direction.values()[i], a);
                        aa.textureMap.put(Direction.values()[i], arr.first());
                    }
                } else if (aa == ActorAnimation.HYDRA) {
                    int[] t = new int[]{1920, 1921, 1922, 1923};
                    int[] b = new int[]{1936, 1937, 1938, 1939};
                    int[] l = new int[]{1944, 1945, 1946, 1947};
                    int[] r = new int[]{1928, 1929, 1930, 1931};
                    int[] tr = new int[]{1924, 1925, 1926, 1927};
                    int[] tl = new int[]{1948, 1949, 1950, 1951};
                    int[] bl = new int[]{1940, 1941, 1942, 1943};
                    int[] br = new int[]{1932, 1933, 1934, 1935};
                    Array<TextureRegion> arr = new Array<>();
                    for (int i = 0; i < 4; i++) {
                        TextureRegion txr = new TextureRegion(mergeTiles(null,
                                tiles[tl[i]], tiles[t[i]], tiles[tr[i]],
                                tiles[l[i]], tiles[aa.tile + i], tiles[r[i]],
                                tiles[bl[i]], tiles[b[i]], tiles[br[i]]
                        ));
                        arr.add(txr);
                    }
                    Animation a = new Animation(.3f, arr);
                    for (int i = 0; i < 4; i++) {
                        aa.animMap.put(Direction.values()[i], a);
                        aa.textureMap.put(Direction.values()[i], arr.first());
                    }
                } else if (aa == ActorAnimation.DRAGON) {
                    int[] t = new int[]{1800, 1801};
                    int[] b = new int[]{1808, 1809};
                    int[] l = new int[]{1816, 1817};
                    int[] r = new int[]{1824, 1825};
                    int[] m = new int[]{1792, 1793};
                    {
                        Array<TextureRegion> arr = new Array<>();
                        for (int i = 0; i < 2; i++) {
                            TextureRegion txr = new TextureRegion(mergeTiles(null,
                                    null, tiles[t[i]], null,
                                    tiles[l[i]], tiles[m[i]], tiles[r[i]],
                                    null, tiles[b[i]], null
                            ));
                            arr.add(txr);
                        }
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.NORTH, a);
                        aa.textureMap.put(Direction.NORTH, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        for (int i = 0; i < 2; i++) {
                            TextureRegion txr = new TextureRegion(mergeTiles(null,
                                    null, tiles[l[i] + 2], null,
                                    tiles[b[i] + 2], tiles[m[i] + 2], tiles[t[i] + 2],
                                    null, tiles[r[i] + 2], null
                            ));
                            arr.add(txr);
                        }
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.EAST, a);
                        aa.textureMap.put(Direction.EAST, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        for (int i = 0; i < 2; i++) {
                            TextureRegion txr = new TextureRegion(mergeTiles(null,
                                    null, tiles[r[i] + 6], null,
                                    tiles[t[i] + 6], tiles[m[i] + 6], tiles[b[i] + 6],
                                    null, tiles[l[i] + 6], null
                            ));
                            arr.add(txr);
                        }
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.WEST, a);
                        aa.textureMap.put(Direction.WEST, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        for (int i = 0; i < 2; i++) {
                            TextureRegion txr = new TextureRegion(mergeTiles(null,
                                    null, tiles[b[i] + 4], null,
                                    tiles[r[i] + 4], tiles[m[i] + 4], tiles[l[i] + 4],
                                    null, tiles[t[i] + 4], null
                            ));
                            arr.add(txr);
                        }
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.SOUTH, a);
                        aa.textureMap.put(Direction.SOUTH, arr.first());
                    }
                } else if (aa == ActorAnimation.SILVER_SERPENT) {
                    TextureRegion txr = new TextureRegion(mergeTiles(null,
                            tiles[1856], tiles[1867], tiles[1868],
                            tiles[1866], tiles[1869], tiles[1864],
                            tiles[1859], tiles[1865], tiles[1869]
                    ));
                    TextureRegion txr2 = new TextureRegion(mergeTiles(null,
                            tiles[1870], tiles[1867], tiles[1868],
                            tiles[1866], tiles[1869], tiles[1864],
                            tiles[1859], tiles[1865], tiles[1869]
                    ));
                    TextureRegion rotated1 = rotate90(txr);
                    TextureRegion rotated2 = rotate90(txr2);
                    {
                        Array<TextureRegion> arr = new Array<>();
                        arr.add(txr);
                        arr.add(txr2);
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.NORTH, a);
                        aa.textureMap.put(Direction.NORTH, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        arr.add(rotated1);
                        arr.add(rotated2);
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.EAST, a);
                        aa.textureMap.put(Direction.EAST, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        arr.add(new TextureRegion(txr));
                        arr.get(0).flip(false, true);
                        arr.add(new TextureRegion(txr2));
                        arr.get(1).flip(false, true);
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.SOUTH, a);
                        aa.textureMap.put(Direction.SOUTH, arr.first());
                    }
                    {
                        Array<TextureRegion> arr = new Array<>();
                        arr.add(new TextureRegion(rotated1));
                        arr.get(0).flip(true, false);
                        arr.add(new TextureRegion(rotated2));
                        arr.get(1).flip(true, false);
                        Animation a = new Animation(.3f, arr);
                        aa.animMap.put(Direction.WEST, a);
                        aa.textureMap.put(Direction.WEST, arr.first());
                    }
                } else if (aa == ActorAnimation.TANGLE_VINE) {
                    TextureRegion txr = new TextureRegion(mergeTiles(null,
                            null, tiles[1459], tiles[1461],
                            tiles[1458], tiles[1456], tiles[1463],
                            null, tiles[1460], tiles[1462]
                    ));
                    TextureRegion txr2 = new TextureRegion(mergeTiles(null,
                            null, tiles[1459], tiles[1461],
                            tiles[1458], tiles[1457], tiles[1463],
                            null, tiles[1460], tiles[1462]
                    ));
                    Array<TextureRegion> arr = new Array<>();
                    arr.add(txr);
                    arr.add(txr2);
                    Animation a = new Animation(.3f, arr);
                    for (int i = 0; i < 4; i++) {
                        aa.animMap.put(Direction.values()[i], a);
                        aa.textureMap.put(Direction.values()[i], arr.first());
                    }
                } else if (aa == ActorAnimation.RAFT || aa == ActorAnimation.FORCE_FIELD) {
                    for (int i = 0; i < 4; i++) {
                        aa.textureMap.put(Direction.values()[i], tiles[aa.tile]);
                    }
                } else if (aa.framesPerDirection == 0) {
                    Array<TextureRegion> arr = new Array<>();
                    for (int i = 0; i < 4; i++) {
                        arr.add(tiles[aa.tile + i]);
                    }
                    Animation a = new Animation(.3f, arr);
                    for (int i = 0; i < 4; i++) {
                        aa.animMap.put(Direction.values()[i], a);
                        aa.textureMap.put(Direction.values()[i], tiles[aa.tile]);
                    }
                } else {
                    for (int i = 0; i < 4; i++) {

                        if (aa.tilesPerFrame == 1) {

                            Array<TextureRegion> arr = new Array<>();
                            for (int j = 0; j < aa.framesPerDirection; j++) {
                                arr.add(tiles[aa.tile + j + i * aa.tilesPerDirection]);
                            }
                            aa.animMap.put(Direction.values()[i], new Animation(.3f, arr));
                            aa.textureMap.put(Direction.values()[i], tiles[aa.tile + i * aa.tilesPerDirection]);

                        } else if (aa.tilesPerFrame == 2 && aa.framesPerDirection == 2) {

                            Array<TextureRegion> arr = new Array<>();
                            arr.add(new TextureRegion(mergeTiles(Direction.values()[i], tiles[aa.tile + 0 + 2 * i], tiles[aa.tile + 8 + 2 * i])));
                            arr.add(new TextureRegion(mergeTiles(Direction.values()[i], tiles[aa.tile + 1 + 2 * i], tiles[aa.tile + 8 + 1 + 2 * i])));
                            aa.animMap.put(Direction.values()[i], new Animation(.3f, arr));

                            aa.textureMap.put(Direction.values()[i], arr.first());

                        } else if (aa.tilesPerFrame == 4) {

                            Array<TextureRegion> arr = new Array<>();
                            for (int j = 0; j < aa.framesPerDirection; j++) {
                                arr.add(new TextureRegion(mergeTiles(Direction.values()[i],
                                        tiles[aa.tile + 0 + (4 * j) + (aa.tilesPerDirection * i)],
                                        tiles[aa.tile + 1 + (4 * j) + (aa.tilesPerDirection * i)],
                                        tiles[aa.tile + 2 + (4 * j) + (aa.tilesPerDirection * i)],
                                        tiles[aa.tile + 3 + (4 * j) + (aa.tilesPerDirection * i)]
                                )));
                            }

                            aa.animMap.put(Direction.values()[i], new Animation(.3f, arr));

                            aa.textureMap.put(Direction.values()[i], arr.first());

                        }
                    }

                    if (aa.framesPerDirection == 3 && aa.tilesPerDirection == 4) {

                        aa.textureMap.put(Direction.NORTH, tiles[aa.tile + 4 - 1 - 2]);
                        aa.textureMap.put(Direction.EAST, tiles[aa.tile + 8 - 1 - 2]);
                        aa.textureMap.put(Direction.SOUTH, tiles[aa.tile + 12 - 1 - 2]);
                        aa.textureMap.put(Direction.WEST, tiles[aa.tile + 16 - 1 - 2]);

                        aa.sittingTextureMap.put(Direction.NORTH, tiles[aa.tile + 4 - 1]);
                        aa.sittingTextureMap.put(Direction.EAST, tiles[aa.tile + 8 - 1]);
                        aa.sittingTextureMap.put(Direction.SOUTH, tiles[aa.tile + 12 - 1]);
                        aa.sittingTextureMap.put(Direction.WEST, tiles[aa.tile + 16 - 1]);
                    }
                }

            }
        }

    }

    private static Texture mergeTiles(Direction dir, TextureRegion... tr) {
        Texture ret = null;
        if (tr.length == 2) {
            Pixmap dest = (dir == Direction.EAST || dir == Direction.WEST ? new Pixmap(32, 16, Format.RGBA8888) : new Pixmap(16, 32, Format.RGBA8888));
            Pixmap p1 = pixmapFromRegion(tr[0]);
            Pixmap p2 = pixmapFromRegion(tr[1]);
            if (dir == Direction.EAST || dir == Direction.WEST) {
                dest.drawPixmap(p1, (dir == Direction.EAST ? 16 : 0), 0);
                dest.drawPixmap(p2, (dir == Direction.EAST ? 0 : 16), 0);
            } else {
                dest.drawPixmap(p1, 0, (dir == Direction.NORTH ? 0 : 16));
                dest.drawPixmap(p2, 0, (dir == Direction.NORTH ? 16 : 0));
            }
            p1.dispose();
            p2.dispose();
            ret = new Texture(dest);
        } else if (tr.length == 4) {
            Pixmap p1 = pixmapFromRegion(tr[0]);
            Pixmap p2 = pixmapFromRegion(tr[1]);
            Pixmap p3 = pixmapFromRegion(tr[2]);
            Pixmap p4 = pixmapFromRegion(tr[3]);
            Pixmap dest = new Pixmap(32, 32, Format.RGBA8888);
            dest.drawPixmap(p1, 0, 0);
            dest.drawPixmap(p2, 16, 0);
            dest.drawPixmap(p3, 0, 16);
            dest.drawPixmap(p4, 16, 16);
            p1.dispose();
            p2.dispose();
            p3.dispose();
            p4.dispose();
            ret = new Texture(dest);
        } else if (tr.length == 6) {
            Pixmap p1 = pixmapFromRegion(tr[0]);
            Pixmap p2 = pixmapFromRegion(tr[1]);
            Pixmap p3 = pixmapFromRegion(tr[2]);
            Pixmap p4 = pixmapFromRegion(tr[3]);
            Pixmap p5 = pixmapFromRegion(tr[4]);
            Pixmap p6 = pixmapFromRegion(tr[5]);
            Pixmap dest = (dir == Direction.EAST || dir == Direction.WEST ? new Pixmap(48, 32, Format.RGBA8888) : new Pixmap(32, 48, Format.RGBA8888));
            if (dir == Direction.EAST || dir == Direction.WEST) {
                dest.drawPixmap(p1, (dir == Direction.EAST ? 32 : 0), 0);
                dest.drawPixmap(p2, (dir == Direction.EAST ? 32 : 0), 16);
                dest.drawPixmap(p3, 16, 0);
                dest.drawPixmap(p4, 16, 16);
                dest.drawPixmap(p5, (dir == Direction.EAST ? 0 : 32), 0);
                dest.drawPixmap(p6, (dir == Direction.EAST ? 0 : 32), 16);
            } else {
                dest.drawPixmap(p1, 0, (dir == Direction.NORTH ? 0 : 32));
                dest.drawPixmap(p2, 16, (dir == Direction.NORTH ? 0 : 32));
                dest.drawPixmap(p3, 0, 16);
                dest.drawPixmap(p4, 16, 16);
                dest.drawPixmap(p5, 0, (dir == Direction.NORTH ? 32 : 0));
                dest.drawPixmap(p6, 16, (dir == Direction.NORTH ? 32 : 0));
            }
            p1.dispose();
            p2.dispose();
            p3.dispose();
            p4.dispose();
            p5.dispose();
            p6.dispose();
            ret = new Texture(dest);
        } else if (tr.length == 9) {
            Pixmap dest = new Pixmap(48, 48, Format.RGBA8888);
            Pixmap[] sources = new Pixmap[tr.length];
            for (int i = 0; i < tr.length; i++) {
                sources[i] = pixmapFromRegion(tr[i]);
            }
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    Pixmap p = sources[y * 3 + x];
                    if (p != null) {
                        dest.drawPixmap(p, x * 16, y * 16);
                        p.dispose();
                    }
                }
            }
            ret = new Texture(dest);
        }
        return ret;

    }

    private static Pixmap pixmapFromRegion(TextureRegion tr) {
        if (tr == null) {
            return null;
        }
        if (!tr.getTexture().getTextureData().isPrepared()) {
            tr.getTexture().getTextureData().prepare();
        }
        Pixmap source = tr.getTexture().getTextureData().consumePixmap();
        Pixmap dest = new Pixmap(tr.getRegionWidth(), tr.getRegionHeight(), Format.RGBA8888);
        for (int x = 0; x < tr.getRegionWidth(); x++) {
            for (int y = 0; y < tr.getRegionHeight(); y++) {
                int colorInt = source.getPixel(tr.getRegionX() + x, tr.getRegionY() + y);
                dest.drawPixel(x, y, colorInt);
            }
        }
        source.dispose();
        return dest;
    }

    private static TextureRegion rotate90(TextureRegion tr) {
        Pixmap srcPix = tr.getTexture().getTextureData().consumePixmap();
        final int width = srcPix.getWidth();
        final int height = srcPix.getHeight();
        Pixmap rotatedPix = new Pixmap(height, width, srcPix.getFormat());
        for (int x = 0; x < height; x++) {
            for (int y = 0; y < width; y++) {
                rotatedPix.drawPixel(x, y, srcPix.getPixel(y, x));
            }
        }
        Texture t = new Texture(rotatedPix);
        srcPix.dispose();
        return new TextureRegion(t);
    }

    public static Animation[] RED_MOONGATE = new Animation[2];
    public static Animation[] BLUE_MOONGATE = new Animation[2];

    public static enum PaletteCycledTiles {

        FIRE_CYCLES(0xe0, 8, 221, 222, 223, 797, 602, 603, 683, 702, 703, 717, 719, 788, 789, 790, 791, 890, 1160, 1140, 1126, 1127, 1132, 1133, 1164),
        BLUE_CYCLES(0xe8, 8, 3, 217, 218, 219, 252, 254, 798, 604, 605),
        BROWN_CYCLES(0xf4, 4, 687),
        PINK_CYCLES(0xf0, 4, 253, 562, 799, 1167),
        GREEN_CYCLES(0xf8, 4, 1165);

        private int palIndex;
        private int length;
        private int[] tiles;
        private Animation[] anims;

        private PaletteCycledTiles(int palIndex, int length, int... tiles) {
            this.palIndex = palIndex;
            this.length = length;
            this.tiles = tiles;
            this.anims = new Animation[tiles.length];
        }

        public Animation[] getAnims() {
            return anims;
        }

        public int getLength() {
            return length;
        }

        public int[] getTiles() {
            return tiles;
        }

        public static void init() throws Exception {
            TextureRegion[] tiles = TILES;

            InputStream is = Gdx.files.classpath("data/U6PAL").read();
            byte[] palette = IOUtils.toByteArray(is);
            java.util.Map<Integer, Integer[]> palMap = new HashMap<>();
            for (int i = 0, j = 0; i < 256; i++, j += 3) {
                Integer[] ints = new Integer[3];
                ints[0] = palette[j] << 2;
                ints[1] = palette[j + 1] << 2;
                ints[2] = palette[j + 2] << 2;
                palMap.put(i, ints);
            }

            for (PaletteCycledTiles t : PaletteCycledTiles.values()) {

                for (int k = 0; k < t.tiles.length; k++) {

                    Integer[][] cycle = new Integer[t.length][3];
                    for (int i = 0; i < t.length; i++) {
                        cycle[i] = palMap.get(t.palIndex + i);
                    }

                    Pixmap lastpx = pixmapFromRegion(tiles[t.tiles[k]]);

                    Array<TextureRegion> frames = new Array<>();
                    frames.add(new TextureRegion(new Texture(lastpx)));

                    for (int i = t.length - 1; i > 0; i--) {
                        Pixmap p = new Pixmap(16, 16, Format.RGBA8888);
                        p.drawPixmap(lastpx, 0, 0);

                        for (int y = 0; y < 16; y++) {
                            for (int x = 0; x < 16; x++) {
                                int rgba = p.getPixel(x, y);
                                int r = ((rgba & 0xff000000) >>> 24);
                                int g = ((rgba & 0x00ff0000) >>> 16);
                                int b = ((rgba & 0x0000ff00) >>> 8);
                                for (int c = 0; c < t.length; c++) {
                                    Integer[] crgb = cycle[c];
                                    if (r == crgb[0] && g == crgb[1] && b == crgb[2]) {
                                        int next = (c == t.length - 1 ? 0 : c + 1);
                                        Integer[] nextrgba = cycle[next];
                                        p.drawPixel(x, y, (nextrgba[0] << 24) | (nextrgba[1] << 16) | (nextrgba[2] << 8) | 255);
                                    }
                                }
                            }
                        }

                        frames.add(new TextureRegion(new Texture(p)));
                        lastpx.dispose();
                        lastpx = p;

                        for (int c = t.length - 1; c >= 0; c--) {
                            Integer[] ctmp = cycle[c];
                            int next = (c == 0 ? t.length - 1 : c - 1);
                            cycle[c] = cycle[next];
                            cycle[next] = ctmp;
                        }
                    }

                    t.anims[k] = new Animation(.2f, frames);
                }

            }
            RED_MOONGATE[0] = PaletteCycledTiles.FIRE_CYCLES.anims[4];
            RED_MOONGATE[1] = PaletteCycledTiles.FIRE_CYCLES.anims[5];
            BLUE_MOONGATE[0] = PaletteCycledTiles.BLUE_CYCLES.anims[7];
            BLUE_MOONGATE[1] = PaletteCycledTiles.BLUE_CYCLES.anims[8];
        }

    }

    public static final String[] DUNGEONS = new String[]{
        "Deceit",
        "Despise",
        "Destard",
        "Wrong",
        "Covetous",
        "Shame",
        "Hythloth",
        "GSA",
        "Control",
        "Passion",
        "Diligence",
        "Tomb of Kings",
        "Ant Mound",
        "Swamp Cave",
        "Spider Cave",
        "Cyclops Cave",
        "Heftimus Cave",
        "Heroes' Hole",
        "Pirate Cave",
        "Buccaneer's Cave"
    };

    public static enum RedMoongates {
        MOONGLOW(-2, 2, 899, 499, 0),
        SHRINE_HONESTY(-1, 2, 935, 262, 0),
        BRITAIN(0, 2, 435, 395, 0),
        SHRINE_COMPASSION(1, 2, 503, 358, 0),
        JHELOM(2, 2, 147, 883, 0),
        //
        SHRINE_HUMILITY(-2, 1, 919, 934, 0),
        SHRINE_CONTROL(-1, 1, 68, 45, 5),
        CASTLE_BRITANNIA(0, 1, 307, 352, 0),
        SHRINE_PASSION(1, 1, 188, 45, 5),
        SHRINE_VALOR(2, 1, 159, 942, 0),
        //
        NEW_MAGINCIA(-2, 0, 739, 699, 0),
        YEW(2, 0, 227, 131, 0),
        //
        SHRINE_SPIRITUALITY(-2, -1, 23, 22, 1),
        GARGOYLE_CITY(-1, -1, 128, 86, 5),
        SHRINE_DILIGENCE(0, -1, 108, 221, 5),
        SHRINE_CODEX(1, -1, 923, 876, 0),
        SHRINE_JUSTICE(2, -1, 295, 38, 0),
        //
        SKARA_BRAE(-2, -2, 75, 507, 0),
        SHRINE_HONOR(-1, -2, 327, 822, 0),
        TRINSIC(0, -2, 387, 787, 0),
        SHRINE_SACRIFICE(1, -2, 831, 166, 0),
        MINOC(2, -2, 667, 67, 0);

        int x;
        int y;
        int dx;
        int dy;
        int dz;

        private RedMoongates(int x, int y, int dx, int dy, int dz) {
            this.x = x;
            this.y = y;
            this.dx = dx;
            this.dy = dy;
            this.dz = dz;
        }

        public static RedMoongates get(int diffx, int diffy) {
            int x = -1;
            int y = -1;

            if (diffx <= 80 && diffx > 48) {
                x = -2;
            } else if (diffx <= 48 && diffx > 16) {
                x = -1;
            } else if (diffx <= 16 && diffx > -16) {
                x = 0;
            } else if (diffx <= -16 && diffx > -48) {
                x = 1;
            } else if (diffx <= -48 && diffx > -80) {
                x = 2;
            }

            if (diffy <= 80 && diffy > 48) {
                y = 2;
            } else if (diffy <= 48 && diffy > 16) {
                y = 1;
            } else if (diffy <= 16 && diffy > -16) {
                y = 0;
            } else if (diffy <= -16 && diffy > -48) {
                y = -1;
            } else if (diffy <= -48 && diffy > -80) {
                y = -2;
            }

            for (RedMoongates m : RedMoongates.values()) {
                if (m.x == x && m.y == y) {
                    return m;
                }
            }
            return null;
        }

        public int getDx() {
            return dx;
        }

        public int getDy() {
            return dy;
        }

        public int getDz() {
            return dz;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }

    }

}
