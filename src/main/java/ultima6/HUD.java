package ultima6;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
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

    private static int WIDTH = 285;
    private static int HEIGHT = 644;

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
        
        add(this.filler).maxHeight(85).height(85).maxWidth(WIDTH).width(WIDTH).expand().fill().colspan(2);
        row();

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
        
        this.conv.init(avatar, party);
        
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
        Gdx.input.setInputProcessor(new InputMultiplexer(stage));

    }

}
