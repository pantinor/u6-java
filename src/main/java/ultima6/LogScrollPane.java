package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Align;

public class LogScrollPane extends ScrollPane {

    private final Table internalTable;
    private final int width;
    private final Skin skin;
    private static final Color ORANGE = new Color(0.98f, 0.56f, 0, 1);

    public LogScrollPane(Skin skin, Table table, int width) {

        super(table, skin);
        this.skin = skin;
        this.internalTable = table;
        this.width = width;

        clear();
        setScrollingDisabled(true, false);

        internalTable.align(Align.topLeft);
    }

    public void add(String text) {
        add(text, Color.ORANGE, true);
    }
    
    public void add(String text, Color color) {
        add(text, color, true);
    }

    public void add(String text, Color color, boolean scrollBottom) {

        if (text == null) {
            return;
        }

        LabelStyle ls = new LabelStyle(this.skin.get("default-font", BitmapFont.class), color != null ? color : Color.ORANGE);

        Label label = new Label(text, ls);
        label.setWrap(true);
        label.setAlignment(Align.topLeft, Align.left);

        internalTable.add(label).pad(1).width(width - 10);
        internalTable.row();

        pack();
        if (scrollBottom) {
            scrollTo(0, 0, 0, 0);
        }

    }

    @Override
    public void clear() {
        internalTable.clear();
        pack();
    }

    @Override
    public float getPrefWidth() {
        return this.getWidth();
    }

    @Override
    public float getPrefHeight() {
        return this.getHeight();
    }

    @Override
    public float getMaxWidth() {
        return this.getWidth();
    }

    @Override
    public float getMaxHeight() {
        return this.getHeight();
    }
}
