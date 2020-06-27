package ultima6;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class HudTest implements ApplicationListener {

    public static void main(String[] args) throws Exception {
        LwjglApplicationConfiguration cfg = new LwjglApplicationConfiguration();
        cfg.width = 1024;
        cfg.height = 768;
        new LwjglApplication(new HudTest(), cfg);
    }

    final Viewport viewport = new ScreenViewport();
    Stage stage;

    Batch batch;

    @Override
    public void create() {
        stage = new Stage(viewport);

        batch = new SpriteBatch();

        try {
            Constants.ActorAnimation.init();

            Constants.ActorAnimation icon = Constants.ActorAnimation.find(1696);

            Actor actor = new Actor(icon, 5, "test");
        } catch (Exception e) {

        }

    }

    @Override
    public void resize(int i, int i1) {
    }

    @Override
    public void render() {

        Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        batch.end();

        stage.act();
        stage.draw();

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
