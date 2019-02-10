package ultima6;


import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.utils.Array;
import com.google.common.io.LittleEndianDataInputStream;
import java.io.InputStream;
import java.util.HashMap;
//import utils.Hud;

public class Ultima6 extends Game {

    public static final int SCREEN_WIDTH = 1024;
    public static final int SCREEN_HEIGHT = 768;

    public static final int MAP_VIEWPORT_DIM = 624;

    //public static Context CTX;
    public static Texture backGround;
    public static TextureAtlas heroesAtlas;
    public static TextureAtlas mapAtlas;

    public static Array<TextureAtlas.AtlasRegion> moongateTextures = new Array<>();

    public static BitmapFont font;
    public static BitmapFont smallFont;
    public static BitmapFont largeFont;
    public static BitmapFont hudLogFont;
    public static BitmapFont titleFont;

    public static Ultima6 mainGame;
    //public static StartScreen startScreen;

    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static final java.util.Map<Integer, TileFlags> TILE_FLAGS = new HashMap<>();

    public static Animation AVATAR;

    //public static Hud HUD;
    //public static Conversations CONVERSATIONS;
//    public static TextureRegion[] faceTiles = new TextureRegion[6 * 6];
//    public static TextureRegion[] invIcons = new TextureRegion[67 * 12];
//
//    public static java.util.List<Item> ITEMS;
//    public static final java.util.Map<String, Item> ITEMS_MAP = new HashMap<>();
//
//    public static java.util.List<Monster> MONSTERS;
//    public static final java.util.Map<String, Monster> MONSTER_MAP = new HashMap<>();
//    public static final java.util.Map<Integer, java.util.List<Monster>> MONSTER_LEVELS = new HashMap<>();
//
//    public static java.util.List<Reward> REWARDS;
    public static void main(String[] args) {

        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.title = "Ultima6";
        cfg.width = SCREEN_WIDTH;
        cfg.height = SCREEN_HEIGHT;
        cfg.addIcon("data/ankh.png", Files.FileType.Classpath);
        new LwjglApplication(new Ultima6(), cfg);

    }

    @Override
    public void create() {

        font = new BitmapFont();

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 16;
        hudLogFont = generator.generateFont(parameter);

        parameter.size = 18;
        font = generator.generateFont(parameter);

        parameter.size = 24;
        largeFont = generator.generateFont(parameter);

        parameter.size = 72;
        titleFont = generator.generateFont(parameter);

        generator.dispose();

        skin = new Skin(Gdx.files.classpath("skin/uiskin.json"));
        skin.remove("default-font", BitmapFont.class);
        skin.add("default-font", font, BitmapFont.class);
        skin.add("larger-font", largeFont, BitmapFont.class);
        skin.add("title-font", titleFont, BitmapFont.class);
        smallFont = skin.get("verdana-10", BitmapFont.class);
        skin.add("small-font", smallFont, BitmapFont.class);
        {
            Label.LabelStyle ls = skin.get("default", Label.LabelStyle.class);
            ls.font = font;
            Label.LabelStyle ls2 = skin.get("hudLogFont", Label.LabelStyle.class);
            ls2.font = hudLogFont;
            Label.LabelStyle ls3 = skin.get("hudSmallFont", Label.LabelStyle.class);
            ls3.font = smallFont;
            TextButton.TextButtonStyle tbs = skin.get("default", TextButton.TextButtonStyle.class);
            tbs.font = font;
            TextButton.TextButtonStyle tbsred = skin.get("red", TextButton.TextButtonStyle.class);
            tbsred.font = font;
            TextButton.TextButtonStyle tbsbr = skin.get("brown", TextButton.TextButtonStyle.class);
            tbsbr.font = font;
            SelectBox.SelectBoxStyle sbs = skin.get("default", SelectBox.SelectBoxStyle.class);
            sbs.font = font;
            sbs.listStyle.font = font;
            CheckBox.CheckBoxStyle cbs = skin.get("default", CheckBox.CheckBoxStyle.class);
            cbs.font = font;
            List.ListStyle lis = skin.get("default", List.ListStyle.class);
            lis.font = font;
            TextField.TextFieldStyle tfs = skin.get("default", TextField.TextFieldStyle.class);
            tfs.font = font;
        }
        {
            Label.LabelStyle ls = skin.get("larger", Label.LabelStyle.class);
            ls.font = largeFont;
            TextButton.TextButtonStyle tbs = skin.get("larger", TextButton.TextButtonStyle.class);
            tbs.font = largeFont;
            TextButton.TextButtonStyle tbsred = skin.get("red-larger", TextButton.TextButtonStyle.class);
            tbsred.font = largeFont;
            TextButton.TextButtonStyle tbsbr = skin.get("brown-larger", TextButton.TextButtonStyle.class);
            tbsbr.font = largeFont;
            SelectBox.SelectBoxStyle sbs = skin.get("larger", SelectBox.SelectBoxStyle.class);
            sbs.font = largeFont;
            sbs.listStyle.font = largeFont;
            List.ListStyle lis = skin.get("larger", List.ListStyle.class);
            lis.font = largeFont;
            TextField.TextFieldStyle tfs = skin.get("larger", TextField.TextFieldStyle.class);
            tfs.font = largeFont;
        }

//        HUD = new Hud();
        try {

            backGround = new Texture(Gdx.files.classpath("data/frame.png"));
//
//            TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_portraits_example.png")), 48, 48);
//            for (int row = 0; row < 6; row++) {
//                for (int col = 0; col < 6; col++) {
//                    faceTiles[row * 6 + col] = trs[row][col];
//                }
//            }
//
//            TextureRegion[][] inv = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/inventory.png")), 44, 44);
//            Texture tx = new Texture(Gdx.files.classpath("assets/data/inventory.png"));
//            for (int row = 0; row < tx.getHeight() / 44; row++) {
//                for (int col = 0; col < tx.getWidth() / 44; col++) {
//                    invIcons[row * tx.getWidth() / 44 + col] = inv[row][col];
//                }
//            }
//
//            heroesAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/heroes-atlas.txt"));
//            mapAtlas = new TextureAtlas(Gdx.files.classpath("assets/data/map-atlas.txt"));
//            moongateTextures = mapAtlas.findRegions("moongate");

            Constants.ActorAnimation.init();
            Constants.PaletteCycledTiles.init();

            initTileFlags();
            AVATAR = Constants.ActorAnimation.AVATAR.getAnimation(Constants.Direction.NORTH);

            Constants.Map.WORLD.init();
            Constants.Map.WORLD.getScreen().setMapPixelCoords(Constants.Map.WORLD.getScreen().newMapPixelCoords, 307, 349);

            //Constants.Map.TEST.init();
            //Constants.Map.TEST.getScreen().setMapPixelCoords(Constants.Map.TEST.getScreen().newMapPixelCoords, 4, 1);

//            TextureRegion[][] expl = TextureRegion.split(new Texture(Gdx.files.classpath("assets/data/uf_FX.png")), 24, 24);
//            EXPLMAP.put(Color.GRAY, new Animation(.1f, getTextureArray(expl, 0, 0)));
//            EXPLMAP.put(Color.BLUE, new Animation(.1f, getTextureArray(expl, 0, 5)));
//            EXPLMAP.put(Color.RED, new Animation(.1f, getTextureArray(expl, 1, 0)));
//            EXPLMAP.put(Color.GREEN, new Animation(.1f, getTextureArray(expl, 1, 5)));
//            EXPLMAP.put(Color.PURPLE, new Animation(.1f, getTextureArray(expl, 4, 5)));
//            EXPLMAP.put(Color.YELLOW, new Animation(.1f, getTextureArray(expl, 5, 5)));
//            InputStream is = this.getClass().getResourceAsStream("/assets/json/items-json.txt");
//            String json = IOUtils.toString(is);
//
//            is = this.getClass().getResourceAsStream("/assets/json/rewards-json.txt");
//            String json2 = IOUtils.toString(is);
//
//            is = this.getClass().getResourceAsStream("/assets/json/monsters-json.txt");
//            String json3 = IOUtils.toString(is);
//
//            GsonBuilder builder = new GsonBuilder();
//            Gson gson = builder.create();
//            ITEMS = gson.fromJson(json, new TypeToken<java.util.List<Item>>() {
//            }.getType());
//            for (Item i : ITEMS) {
//                ITEMS_MAP.put(i.name, i);
//            }
//            REWARDS = gson.fromJson(json2, new TypeToken<java.util.List<Reward>>() {
//            }.getType());
//            MONSTERS = gson.fromJson(json3, new TypeToken<java.util.List<Monster>>() {
//            }.getType());
//            for (int i = 0; i < 11; i++) {
//                MONSTER_LEVELS.put(i, new ArrayList<>());
//            }
//            for (Monster m : MONSTERS) {
//                MONSTER_MAP.put(m.name, m);
//                MONSTER_LEVELS.get(m.getLevel()).add(m);
//            }
//
//            Icons.init();
//            Constants.Moongate.init();
//            CONVERSATIONS = Conversations.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        mainGame = this;
        //startScreen = new StartScreen();
        setScreen(Constants.Map.WORLD.getScreen());

    }

    private static void initTileFlags() throws Exception {
        InputStream is = Gdx.files.classpath("data/TILEFLAG").read();
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        byte[] f1 = new byte[2048];
        byte[] f2 = new byte[2048];
        byte[] none = new byte[2048];
        byte[] f3 = new byte[2048];

        dis.read(f1);
        dis.read(f2);
        dis.read(none);
        dis.read(f3);

        for (int i = 0; i < 2048; i++) {

            TileFlags tf = new TileFlags();

            tf.setWet((f1[i] & 0x1) != 0);
            tf.setImpassable((f1[i] & 0x2) != 0);
            tf.setWall((f1[i] & 0x4) != 0);
            tf.setDamaging((f1[i] & 0x8) != 0);

            tf.setSides((((f1[i] & 0x10) != 0 ? "w" : "")
                    + ((f1[i] & 0x20) != 0 ? "s" : "")
                    + ((f1[i] & 0x40) != 0 ? "e" : "")
                    + ((f1[i] & 0x80) != 0 ? "n" : "")));

            tf.setLightlsb((f2[i] & 0x1) != 0);
            tf.setLightmsb((f2[i] & 0x2) != 0);
            tf.setBoundary((f2[i] & 0x4) != 0);
            tf.setLookthruboundary((f2[i] & 0x8) != 0);
            tf.setOntop((f2[i] & 0x10) != 0);
            tf.setNoshootthru((f2[i] & 0x20) != 0);
            tf.setVsize(((f2[i] & 0x40) != 0 ? 2 : 1));
            tf.setHsize(((f2[i] & 0x80) != 0 ? 2 : 1));

            tf.setWarm((f3[i] & 0x1) != 0);
            tf.setSupport((f3[i] & 0x2) != 0);
            tf.setBreakthruable((f3[i] & 0x4) != 0);
            tf.setBackground((f3[i] & 0x20) != 0);

            TILE_FLAGS.put(i, tf);

        }

    }

}
