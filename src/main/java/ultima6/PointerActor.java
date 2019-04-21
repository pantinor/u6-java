package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import static ultima6.Constants.TILE_DIM;

public class PointerActor extends com.badlogic.gdx.scenes.scene2d.Actor {

    boolean visible = false;
    private final TextureRegion texture;

    public PointerActor(TextureRegion texture) {
        this.texture = texture;
    }

    public void set(float x, float y) {
        setX(x);
        setY(y);
    }

    @Override
    public void setVisible(boolean v) {
        this.visible = v;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {

        Color color = getColor();
        batch.setColor(color.r, color.g, color.b, color.a * parentAlpha);

        if (visible) {
            batch.draw(texture, getX(), getY(), TILE_DIM, TILE_DIM);
        }
    }
}
