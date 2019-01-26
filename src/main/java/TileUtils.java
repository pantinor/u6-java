
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

public class TileUtils implements ApplicationListener {

    public static void main(String[] args) throws Exception {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.width = 1600;
        cfg.height = 1100;
        new LwjglApplication(new TileUtils(), cfg);
    }

    float time = 0;
    Batch batch;
    BitmapFont font;

    @Override
    public void create() {
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);

        try {
            Constants.ActorAnimation.init();
        } catch (Exception e) {

        }

    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void render() {

        time += Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        int x = 20, y = 1000;
        for (Constants.ActorAnimation aa : Constants.ActorAnimation.values()) {
            for (Constants.Direction d : Constants.Direction.values()) {
                TextureRegion tr = aa.getTexture(d);
                if (tr != null) {
                    batch.draw(tr, x, y);
                }
                x += 20;
                if (x > 1500) {
                    x = 20;
                    y -= 20;
                }

            }
        }

        x = 20;
        y = 800;
        for (Constants.Direction d : Constants.Direction.values()) {
            TextureRegion tr = Constants.ActorAnimation.DAEMON.getTexture(d);
            if (tr != null) {
                batch.draw(tr, x, y);
            }
            x += 20;
            if (x > 1500) {
                x = 20;
                y -= 20;
            }
        }

        x = 36;
        y = 600;
        for (Constants.ActorAnimation aa : Constants.ActorAnimation.values()) {
            for (Constants.Direction d : Constants.Direction.values()) {
                Animation anim = aa.getAnimation(d);
                if (anim != null) {
                    batch.draw((TextureRegion) anim.getKeyFrame(time, true), x, y);
                }
                x += 36;
                if (x > 1500) {
                    x = 36;
                    y -= 36;
                }
            }
        }

        batch.end();

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    @Override
    public void dispose() {
    }

}
