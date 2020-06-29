package ultima6;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ultima6.Conversations.Conversation;
import ultima6.Conversations.OutputStream;

public class HUD extends Table {

    private final static int WIDTH = 285;
    private final static int HEIGHT = 565;

    private final GameScreen screen;
    private final TextField input;
    private final Label debug;
    private final LogScrollPane scrollPane;
    private final Image portrait = new Image();
    private final Image filler = new Image();
    private final OutputStream output;

    private Conversation conv;

    public HUD(GameScreen screen, Skin skin) {
        super(skin);

        this.screen = screen;

        setX(695);
        setY(43);
        setWidth(WIDTH);
        setHeight(HEIGHT);

        this.scrollPane = new LogScrollPane(Ultima6.skin, new Table(), WIDTH);

        this.input = new TextField("", Ultima6.skin);

        this.debug = new Label("", Ultima6.skin);

        this.output = new OutputStream() {
            @Override
            public void print(String text, Color color) {
                scrollPane.add(text, color);
            }

            @Override
            public void close() {
                HUD.this.remove();
                Gdx.input.setInputProcessor(new InputMultiplexer(HUD.this.screen));
            }

            @Override
            public void setPortrait(int npc) {
                int idx = npc - 1 < Ultima6.faceTiles.length ? npc - 1 : 0;
                portrait.setDrawable(new TextureRegionDrawable(Ultima6.faceTiles[idx]));
            }
        };

        add(this.portrait).maxHeight(64 * 2).height(64 * 2).maxWidth(56 * 2).width(56 * 2).left();
        row();

        //add(this.filler).maxHeight(85).height(85).maxWidth(WIDTH).width(WIDTH).expand().fill().colspan(2);
        //row();
        add(scrollPane).maxHeight(380).height(380).maxWidth(WIDTH).width(WIDTH).expand().fill().colspan(2);
        row();

        Table internalTable = new Table();

        internalTable.add(new Label("you say:", Ultima6.skin)).maxHeight(25).height(25).maxWidth(60).width(60).left();
        internalTable.add(input).maxHeight(25).height(25).expand().fill();

        add(internalTable).maxHeight(25).height(25).maxWidth(WIDTH).width(WIDTH).expand().fill().colspan(2);
        row();

        //debugAll();
    }

    public void set(Stage stage, Player avatar, Party party, Conversation conv) {

        this.conv = conv;

        this.conv.init(avatar, party, output);

        this.scrollPane.clear();

        this.portrait.setDrawable(new TextureRegionDrawable(conv.getPortait()));

        this.input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {
                    String input = tf.getText();
                    conv.process(avatar, party, input, output);
                    debug.setText("" + conv.data().position());
                    tf.setText("");
                }
            }
        });

        this.scrollPane.add("You see " + conv.getDescription());

        stage.setKeyboardFocus(input);
    }

    private static final int STRIP_X = 700;
    private static final int STRIP_Y = Ultima6.SCREEN_HEIGHT - 45;

    public void renderStrip(Batch batch, int level, int day, int hour) {
        if (level == 0 || level == 5) {
            renderSurfaceStrip(batch, day, hour);
        } else {
            renderDungeonStrip(batch);
        }
    }

    private void renderSurfaceStrip(Batch batch, int day, int hour) {
        boolean eclipse = false;
        renderSun(batch, hour, false);
        if (!eclipse) {
            renderMoons(batch, day, hour);
        }
        for (int i = 0; i < 9; i++) {
            TextureRegion tile = Constants.TILES[352 + i];
            batch.draw(tile, STRIP_X + i * 32, STRIP_Y, 32, 32);
        }
    }

    private void renderDungeonStrip(Batch batch) {
        TextureRegion tile = Constants.TILES[372];
        batch.draw(tile, STRIP_X + 16, STRIP_Y, 32, 32);

        tile = Constants.TILES[373];
        for (int i = 1; i < 8; i++) {
            batch.draw(tile, STRIP_X + i * 32, STRIP_Y, 32, 32);
        }

        tile = Constants.TILES[374];
        batch.draw(tile, STRIP_X + 16 + 7 * 32 + 16, STRIP_Y, 32, 32);
    }

    private static final int[][] SKY_POS = {
        {16 + 7 * 32 - 0 * 16, 6},
        {16 + 7 * 32 - 1 * 16, 3},
        {16 + 7 * 32 - 2 * 16, 1},
        {16 + 7 * 32 - 3 * 16, -1},
        {16 + 7 * 32 - 4 * 16, -2},
        {16 + 7 * 32 - 5 * 16, -3},
        {16 + 7 * 32 - 6 * 16, -4},
        {16 + 7 * 32 - 7 * 16, -4},
        {16 + 7 * 32 - 8 * 16, -4},
        {16 + 7 * 32 - 9 * 16, -3},
        {16 + 7 * 32 - 10 * 16, -2},
        {16 + 7 * 32 - 11 * 16, -1},
        {16 + 7 * 32 - 12 * 16, 1},
        {16 + 7 * 32 - 13 * 16, 3},
        {16 + 7 * 32 - 14 * 16, 6}
    };

    private void renderSun(Batch batch, int hour, boolean eclipse) {
        int sun_tile = 0;
        if (eclipse) {
            sun_tile = 363; //eclipsed sun
        } else if (hour == 5 || hour == 19) {
            sun_tile = 361; //orange sun
        } else if (hour > 5 && hour < 19) {
            sun_tile = 362; //yellow sun
        } else {
            return; //no sun
        }
        renderSunMoon(batch, Constants.TILES[sun_tile], hour - 5);
    }

    private void renderMoons(Batch batch, int day, int hour) {

        int phase = (Math.round((day - 1) / Clock.TRAMMEL_PHASE)) % 8;
        TextureRegion tileA = Constants.TILES[(phase == 0) ? 584 : 584 + (8 - phase)];
        int posA = ((hour + 1) + 3 * phase) % 24;

        int phaseb = (day - 1) % (Math.round(Clock.FELUCCA_PHASE * 8)) - 1;
        phase = (phaseb >= 0) ? phaseb : 0;
        TextureRegion tileB = Constants.TILES[(phase == 0) ? 584 : 584 + (8 - phase)];
        int posB = ((hour - 1) + 3 * phase) % 24;

        if (posA >= 5 && posA <= 19) {
            renderSunMoon(batch, tileA, posA - 5);
        }

        if (posB >= 5 && posB <= 19) {
            renderSunMoon(batch, tileB, posB - 5);
        }
    }

    private void renderSunMoon(Batch batch, TextureRegion tile, int pos) {

        int x = STRIP_X + SKY_POS[pos][0];
        int y = STRIP_Y + 5 - SKY_POS[pos][1];

        if (SKY_POS[pos][1] == 6) {

        }

        batch.draw(tile, x, y, 32, 32);
    }

}
