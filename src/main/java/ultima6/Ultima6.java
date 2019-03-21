package ultima6;

import com.badlogic.gdx.Files;
import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;
import ultima6.Constants.Direction;
import ultima6.Conversations.Conversation;

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
    public static final Conversations CONVS = new Conversations();

    public static Skin skin;

    public static boolean playMusic = true;
    public static float musicVolume = 0.1f;
    public static Music music;

    public static final java.util.Map<Integer, TileFlags> TILE_FLAGS = new HashMap<>();
    public static final byte[] OBJ_WEIGHTS = new byte[1024];
    public static final java.util.Map<Integer, java.util.List<Schedule>> SCHEDULES = new HashMap<>();

    public static TextureRegion AVATAR_TEXTURE;
    public static Direction currentDirection = Direction.NORTH;

    public static Party PARTY = new Party();
    public static Player AVATAR = new Player(1, "Avatar");
    public static final Clock CLOCK = new Clock();

    public static TextureRegion[] faceTiles = new TextureRegion[13 * 16];

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

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.classpath("fonts/gnuolane.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 16;
        hudLogFont = generator.generateFont(parameter);

        parameter.size = 18;
        font = generator.generateFont(parameter);
        font.setColor(Color.BLACK);

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

        try {

            backGround = new Texture(Gdx.files.classpath("data/frame.png"));

            Constants.ActorAnimation.init();
            Constants.PaletteCycledTiles.init();

            initTileFlags();
            initConversations();
            initSchedules();

            AVATAR_TEXTURE = Constants.ActorAnimation.AVATAR.getTexture(Constants.Direction.NORTH);

            PARTY.add(AVATAR);

            Constants.Map.WORLD.init();
            Constants.Map.WORLD.getScreen().setMapPixelCoords(Constants.Map.WORLD.getScreen().newMapPixelCoords, 307, 352);

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
        byte[] none = new byte[1024];
        byte[] f3 = new byte[2048];

        dis.read(f1);
        dis.read(f2);
        dis.read(OBJ_WEIGHTS);
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

    private static void initConversations() throws Exception {

        TextureRegion[][] trs = TextureRegion.split(new Texture(Gdx.files.classpath("data/Portraits.gif")), 56, 64);
        for (int y = 0; y < 13; y++) {
            for (int x = 0; x < 16; x++) {
                faceTiles[y * 16 + x] = trs[y][x];
            }
        }

        GZIPInputStream is = new GZIPInputStream(Gdx.files.classpath("data/conversations").read());
        byte[] conv = IOUtils.toByteArray(is);
        is.close();

        ByteBuffer bba = ByteBuffer.wrap(conv);
        while (bba.position() < bba.limit()) {
            short len = bba.getShort();
            byte[] data = new byte[len];
            bba.get(data);
            StringBuilder sb = new StringBuilder();
            byte b = 0;
            for (int i = 2; i < 20; i++) {
                b = data[i];
                if (b == (byte) 0xf1) {
                    break;
                }
                sb.append((char) b);
            }
            int npc = data[1] & 0xff;
            Conversation c = CONVS.put(npc, sb.toString(), data);
            c.setPortait(faceTiles[npc - 1]);
        }

    }

    private static void initSchedules() throws Exception {
        InputStream is = Gdx.files.classpath("data/SCHEDULE").read();
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        int[] sched_offsets = new int[256];
        int[] num_schedules = new int[256];

        for (int i = 0; i < 256; i++) {
            sched_offsets[i] = dis.readShort() & 0xff;
        }
        int total_schedules = dis.readShort() & 0xff;
        for (int i = 0; i < 256; i++) {
            if (sched_offsets[i] > (total_schedules - 1)) {
                num_schedules[i] = 0;
            } else if (i == 256 - 1) {
                num_schedules[i] = total_schedules - sched_offsets[i];
            } else if (sched_offsets[i + 1] > (total_schedules - 1)) {
                num_schedules[i] = total_schedules - sched_offsets[i];
            } else {
                num_schedules[i] = sched_offsets[i + 1] - sched_offsets[i];
            }
        }

        byte[] data = new byte[total_schedules * 5];
        dis.read(data);

        for (int i = 0; i < 256; i++) {
            int idx = sched_offsets[i] * 5;
            int num = num_schedules[i];
            if (num > 0) {
                java.util.List<Schedule> scheds = new ArrayList<>();
                for (int j = 0; j < num; j++) {
                    Schedule sched = new Schedule();
                    byte hour = data[idx];
                    sched.setHour(hour & 0x1f);
                    sched.setDayOfWeek(hour >> 5);
                    sched.setWorktype(data[idx + 1] & 0xff);

                    int x = data[idx + 2];
                    x += (data[idx + 3] & 0x3) << 8;
                    int y = (data[idx + 3] & 0xfc) >> 2;
                    y += (data[idx + 4] & 0xf) << 6;
                    int z = (data[idx + 4] & 0xf0) >> 4;

                    sched.setX(x);
                    sched.setY(y);
                    sched.setZ(z);
                    scheds.add(sched);
                    idx += 5;
                    System.out.printf("npc[%d] - %s\n", i, sched);
                }
                SCHEDULES.put(i, scheds);
            }
        }
    }

}
