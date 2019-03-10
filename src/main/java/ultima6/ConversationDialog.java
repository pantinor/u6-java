package ultima6;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.fadeOut;
import static com.badlogic.gdx.scenes.scene2d.actions.Actions.sequence;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.FocusListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import ultima6.Conversations.Conversation;
import ultima6.Conversations.OutputStream;

public class ConversationDialog extends Window {

    public static int WIDTH = 350;
    public static int HEIGHT = 327;

    Actor previousKeyboardFocus, previousScrollFocus;
    private final FocusListener focusListener;
    private final GameScreen screen;
    private final Conversation conv;
    private final TextField input;
    private final Label debug;
    private final LogScrollPane scrollPane;
    private final Image portrait = new Image();

    protected InputListener ignoreTouchDown = new InputListener() {
        @Override
        public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
            event.cancel();
            return false;
        }
    };

    public ConversationDialog(GameScreen screen, Player player, Conversation conv) {
        super(conv.getName(), Ultima6.skin.get("dialog", Window.WindowStyle.class));
        this.screen = screen;
        this.conv = conv;

        setSkin(Ultima6.skin);
        setModal(true);
        defaults().pad(5);

        portrait.setScale(2.0f);
        portrait.setDrawable(new TextureRegionDrawable(conv.getPortait()));

        scrollPane = new LogScrollPane(Ultima6.skin, new Table(), WIDTH);
        scrollPane.setHeight(HEIGHT);

        TextButton close = new TextButton("X", Ultima6.skin);
        close.addListener(new EventListener() {
            @Override
            public boolean handle(Event event) {
                if (event.toString().equals("touchDown")) {
                    hide();
                }
                return false;
            }
        });

        getTitleTable().add(close).height(getPadTop());

        OutputStream output = new OutputStream() {
            @Override
            public void print(String text, Color color) {
                scrollPane.add(text, color);
            }

            @Override
            public void close() {
                hide();
            }

            @Override
            public void setPortrait(int npc) {
                portrait.setDrawable(new TextureRegionDrawable(Ultima6.faceTiles[npc - 1]));
            }
        };

        debug = new Label("", Ultima6.skin);

        input = new TextField("", Ultima6.skin);
        input.setTextFieldListener(new TextField.TextFieldListener() {
            @Override
            public void keyTyped(TextField tf, char key) {
                if (key == '\r') {
                    String input = tf.getText();
                    conv.process(player, input, output);
                    debug.setText("" + conv.data().position());
                    tf.setText("");
                }
            }
        });
                
        add().height(55).minHeight(55).maxHeight(55);//padding
        row();
        
        add(this.portrait);
        row();

        add(scrollPane).maxWidth(WIDTH).width(WIDTH).expand().fill().colspan(2);
        row();
        
        add(new Label("you say:", Ultima6.skin)).maxWidth(50).width(50);
        add(input).expand().fill();
        row();
        
        //add(debug).fill();
        //row();
        
        //debugAll();

        focusListener = new FocusListener() {
            @Override
            public void keyboardFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            @Override
            public void scrollFocusChanged(FocusListener.FocusEvent event, Actor actor, boolean focused) {
                if (!focused) {
                    focusChanged(event);
                }
            }

            private void focusChanged(FocusListener.FocusEvent event) {
                Stage stage = getStage();
                if (isModal() && stage != null && stage.getRoot().getChildren().size > 0 && stage.getRoot().getChildren().peek() == ConversationDialog.this) {
                    Actor newFocusedActor = event.getRelatedActor();
                    if (newFocusedActor != null && !newFocusedActor.isDescendantOf(ConversationDialog.this) && !(newFocusedActor.equals(previousKeyboardFocus) || newFocusedActor.equals(previousScrollFocus))) {
                        event.cancel();
                    }
                }
            }
        };

        scrollPane.add("You see " + conv.getDescription());

    }

    public void show(Stage stage) {

        clearActions();

        removeCaptureListener(ignoreTouchDown);

        previousKeyboardFocus = null;
        Actor actor = stage.getKeyboardFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousKeyboardFocus = actor;
        }

        previousScrollFocus = null;
        actor = stage.getScrollFocus();
        if (actor != null && !actor.isDescendantOf(this)) {
            previousScrollFocus = actor;
        }

        pack();

        stage.addActor(this);
        stage.setKeyboardFocus(input);
        stage.setScrollFocus(this);

        Gdx.input.setInputProcessor(stage);

        Action action = sequence(Actions.alpha(0), Actions.fadeIn(0.4f, Interpolation.fade));
        addAction(action);

        setPosition(Math.round((stage.getWidth() - getWidth()) / 2), Math.round((stage.getHeight() - getHeight()) / 2));
    }

    public void hide() {
        Action action = sequence(fadeOut(0.4f, Interpolation.fade), Actions.removeListener(ignoreTouchDown, true), Actions.removeActor());

        Stage stage = getStage();

        if (stage != null) {
            removeListener(focusListener);
        }

        if (action != null) {
            addCaptureListener(ignoreTouchDown);
            addAction(sequence(action, Actions.removeListener(ignoreTouchDown, true), Actions.removeActor()));
        } else {
            remove();
        }

        Gdx.input.setInputProcessor(new InputMultiplexer(screen, stage));
    }

}
