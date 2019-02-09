
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
            Constants.PaletteCycledTiles.init();
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

        int x = 48, y = 1000;
        for (Constants.ActorAnimation aa : Constants.ActorAnimation.values()) {
            for (Constants.Direction d : Constants.Direction.values()) {
                TextureRegion tr = aa.getTexture(d);
                if (tr != null) {
                    batch.draw(tr, x, y);
                }
                x += 48;
                if (x > 1500) {
                    x = 48;
                    y -= 48;
                }

            }
        }

        x = 48;
        y = 500;
        for (Constants.ActorAnimation aa : Constants.ActorAnimation.values()) {
            for (Constants.Direction d : Constants.Direction.values()) {
                Animation anim = aa.getAnimation(d);
                if (anim != null) {
                    batch.draw((TextureRegion) anim.getKeyFrame(time, true), x, y);
                }
                x += 48;
                if (x > 1500) {
                    x = 48;
                    y -= 48;
                }
            }
        }

        batch.draw((TextureRegion) Constants.PaletteCycledTiles.POT_BLUE.getAnims()[0].getKeyFrame(time, true), 10, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.POT_PINK.getAnims()[0].getKeyFrame(time, true), 40, 100, 32, 32);
        
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.STARS.getAnims()[0].getKeyFrame(time, true), 80, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.STARS.getAnims()[1].getKeyFrame(time, true), 120, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.STARS.getAnims()[2].getKeyFrame(time, true), 160, 100, 32, 32);

        batch.draw((TextureRegion) Constants.PaletteCycledTiles.ROCK.getAnims()[0].getKeyFrame(time, true), 200, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.ROCK.getAnims()[1].getKeyFrame(time, true), 240, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.ROCK.getAnims()[2].getKeyFrame(time, true), 280, 100, 32, 32);

        batch.draw((TextureRegion) Constants.PaletteCycledTiles.FIRE.getAnims()[0].getKeyFrame(time, true), 320, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.FIRE.getAnims()[1].getKeyFrame(time, true), 360, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.FIRE.getAnims()[2].getKeyFrame(time, true), 400, 100, 32, 32);
        batch.draw((TextureRegion) Constants.PaletteCycledTiles.FIRE.getAnims()[3].getKeyFrame(time, true), 440, 100, 32, 32);

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
