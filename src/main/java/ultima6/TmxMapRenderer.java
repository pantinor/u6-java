package ultima6;

import static com.badlogic.gdx.graphics.g2d.Batch.C1;
import static com.badlogic.gdx.graphics.g2d.Batch.C2;
import static com.badlogic.gdx.graphics.g2d.Batch.C3;
import static com.badlogic.gdx.graphics.g2d.Batch.C4;
import static com.badlogic.gdx.graphics.g2d.Batch.U1;
import static com.badlogic.gdx.graphics.g2d.Batch.U2;
import static com.badlogic.gdx.graphics.g2d.Batch.U3;
import static com.badlogic.gdx.graphics.g2d.Batch.U4;
import static com.badlogic.gdx.graphics.g2d.Batch.V1;
import static com.badlogic.gdx.graphics.g2d.Batch.V2;
import static com.badlogic.gdx.graphics.g2d.Batch.V3;
import static com.badlogic.gdx.graphics.g2d.Batch.V4;
import static com.badlogic.gdx.graphics.g2d.Batch.X1;
import static com.badlogic.gdx.graphics.g2d.Batch.X2;
import static com.badlogic.gdx.graphics.g2d.Batch.X3;
import static com.badlogic.gdx.graphics.g2d.Batch.X4;
import static com.badlogic.gdx.graphics.g2d.Batch.Y1;
import static com.badlogic.gdx.graphics.g2d.Batch.Y2;
import static com.badlogic.gdx.graphics.g2d.Batch.Y3;
import static com.badlogic.gdx.graphics.g2d.Batch.Y4;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.BatchTiledMapRenderer;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import ultima6.Constants.Map;

public class TmxMapRenderer extends BatchTiledMapRenderer {

    private final Map map;
    private float stateTime = 0;

    public TmxMapRenderer(Map map, TiledMap tiledMap, float unitScale) {
        super(tiledMap, unitScale);
        this.map = map;
    }

    @Override
    public void render() {
        beginRender();
        for (MapLayer layer : getMap().getLayers()) {
            renderMapLayer(layer);
        }
        for (Moongate m : this.map.getBaseMap().getMoongates()) {
            if (viewBounds.contains(m.getX() * unitScale + 32, m.getY() * unitScale)) {
                Animation[] anim = m.getAnimation();
                TextureRegion tr1 = (TextureRegion) anim[0].getKeyFrame(this.stateTime, true);
                TextureRegion tr2 = (TextureRegion) anim[1].getKeyFrame(this.stateTime, true);
                batch.draw(tr1, (m.getX() - 16) * unitScale, m.getY() * unitScale, tr1.getRegionWidth() * unitScale, tr1.getRegionHeight() * unitScale);
                batch.draw(tr2, m.getX() * unitScale, m.getY() * unitScale, tr2.getRegionWidth() * unitScale, tr2.getRegionHeight() * unitScale);
            }
        }
        endRender();

    }

    @Override
    public void renderTileLayer(TiledMapTileLayer layer) {

        this.stateTime += Gdx.graphics.getDeltaTime() / 4;

        final Color batchColor = batch.getColor();
        final float color = Color.toFloatBits(batchColor.r, batchColor.g, batchColor.b, batchColor.a * layer.getOpacity());

        int layerWidth = layer.getWidth();
        int layerHeight = layer.getHeight();

        int layerTileWidth = (int) (layer.getTileWidth() * unitScale);
        int layerTileHeight = (int) (layer.getTileHeight() * unitScale);

        int col1 = Math.max(0, (int) (viewBounds.x / layerTileWidth));
        int col2 = Math.min(layerWidth, (int) ((viewBounds.x + viewBounds.width + layerTileWidth) / layerTileWidth));
        int row1 = Math.max(0, (int) (viewBounds.y / layerTileHeight));
        int row2 = Math.min(layerHeight, (int) ((viewBounds.y + viewBounds.height + layerTileHeight) / layerTileHeight));

        float y = row2 * layerTileHeight;
        float startX = col1 * layerTileWidth;

        for (int row = row2; row >= row1; row--) {

            float x = startX;
            for (int col = col1; col < col2; col++) {

                TiledMapTileLayer.Cell cell = layer.getCell(col, row);

                if (cell == null) {
                    x += layerTileWidth;
                    continue;
                }

                TiledMapTile tile = cell.getTile();

                if (tile != null) {
                    final boolean flipX = cell.getFlipHorizontally();
                    final boolean flipY = cell.getFlipVertically();
                    final int rotations = cell.getRotation();

                    TextureRegion region = tile.getTextureRegion();

                    float x1 = x + tile.getOffsetX() * unitScale;
                    float y1 = y + tile.getOffsetY() * unitScale;
                    float x2 = x1 + region.getRegionWidth() * unitScale;
                    float y2 = y1 + region.getRegionHeight() * unitScale;

                    float u1 = region.getU();
                    float v1 = region.getV2();
                    float u2 = region.getU2();
                    float v2 = region.getV();

                    vertices[X1] = x1;
                    vertices[Y1] = y1;
                    vertices[C1] = color;
                    vertices[U1] = u1;
                    vertices[V1] = v1;

                    vertices[X2] = x1;
                    vertices[Y2] = y2;
                    vertices[C2] = color;
                    vertices[U2] = u1;
                    vertices[V2] = v2;

                    vertices[X3] = x2;
                    vertices[Y3] = y2;
                    vertices[C3] = color;
                    vertices[U3] = u2;
                    vertices[V3] = v2;

                    vertices[X4] = x2;
                    vertices[Y4] = y1;
                    vertices[C4] = color;
                    vertices[U4] = u2;
                    vertices[V4] = v1;

                    if (flipX) {
                        float temp = vertices[U1];
                        vertices[U1] = vertices[U3];
                        vertices[U3] = temp;
                        temp = vertices[U2];
                        vertices[U2] = vertices[U4];
                        vertices[U4] = temp;
                    }
                    if (flipY) {
                        float temp = vertices[V1];
                        vertices[V1] = vertices[V3];
                        vertices[V3] = temp;
                        temp = vertices[V2];
                        vertices[V2] = vertices[V4];
                        vertices[V4] = temp;
                    }
                    if (rotations != 0) {
                        switch (rotations) {
                            case TiledMapTileLayer.Cell.ROTATE_90: {
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V2];
                                vertices[V2] = vertices[V3];
                                vertices[V3] = vertices[V4];
                                vertices[V4] = tempV;

                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U2];
                                vertices[U2] = vertices[U3];
                                vertices[U3] = vertices[U4];
                                vertices[U4] = tempU;
                                break;
                            }
                            case TiledMapTileLayer.Cell.ROTATE_180: {
                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U3];
                                vertices[U3] = tempU;
                                tempU = vertices[U2];
                                vertices[U2] = vertices[U4];
                                vertices[U4] = tempU;
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V3];
                                vertices[V3] = tempV;
                                tempV = vertices[V2];
                                vertices[V2] = vertices[V4];
                                vertices[V4] = tempV;
                                break;
                            }
                            case TiledMapTileLayer.Cell.ROTATE_270: {
                                float tempV = vertices[V1];
                                vertices[V1] = vertices[V4];
                                vertices[V4] = vertices[V3];
                                vertices[V3] = vertices[V2];
                                vertices[V2] = tempV;

                                float tempU = vertices[U1];
                                vertices[U1] = vertices[U4];
                                vertices[U4] = vertices[U3];
                                vertices[U3] = vertices[U2];
                                vertices[U2] = tempU;
                                break;
                            }
                        }
                    }
                    batch.draw(region.getTexture(), vertices, 0, NUM_VERTICES);
                }
                x += layerTileWidth;
            }
            y -= layerTileHeight;
        }
    }

    @Override
    public void renderObjects(MapLayer layer) {

        if (layer.getName().equals("actors")) {
            for (Actor a : this.map.getBaseMap().getActors()) {
                if (viewBounds.contains(a.getX() * unitScale + 32, a.getY() * unitScale)) {
                    if (a.isSitting()) {
                        TextureRegion tr = a.getSittingTexture();
                        batch.draw(tr, a.getX() * unitScale, a.getY() * unitScale,
                                tr.getRegionWidth() * unitScale, tr.getRegionHeight() * unitScale);
                    } else if (!a.isMoving()) {
                        TextureRegion tr = a.getTexture();
                        batch.draw(tr, a.getX() * unitScale, a.getY() * unitScale,
                                tr.getRegionWidth() * unitScale, tr.getRegionHeight() * unitScale);
                    } else {
                        Animation anim = a.getAnimation();
                        if (anim == null) {
                            continue;
                        }
                        TextureRegion tr = (TextureRegion) anim.getKeyFrame(this.stateTime, true);
                        float x = a.getX();
                        float y = a.getY();
                        if (tr.getRegionWidth() > 16) {
                            x -= 16;
                        }
                        if (tr.getRegionHeight() > 16) {
                            y -= 16;
                        }
                        batch.draw(tr, x * unitScale, y * unitScale,
                                tr.getRegionWidth() * unitScale, tr.getRegionHeight() * unitScale);
                    }
                }
            }
            return;
        }

        for (MapObject object : layer.getObjects()) {
            if (object instanceof TextureMapObject) {
                TextureMapObject textureObj = (TextureMapObject) object;
                if (viewBounds.contains(textureObj.getX() * unitScale + 32, textureObj.getY() * unitScale)) {
                    Object gid = object.getProperties().get("gid");
                    TiledMapTile t = this.map.getTiledMap().getTileSets().getTile((Integer) gid);
                    if (t instanceof AnimatedTiledMapTile) {
                        batch.draw(t.getTextureRegion(),
                                textureObj.getX() * unitScale, textureObj.getY() * unitScale,
                                textureObj.getOriginX() * unitScale, textureObj.getOriginY() * unitScale,
                                textureObj.getTextureRegion().getRegionWidth() * unitScale, textureObj.getTextureRegion().getRegionHeight() * unitScale,
                                textureObj.getScaleX(), textureObj.getScaleY(), textureObj.getRotation());
                    } else {
                        batch.draw(textureObj.getTextureRegion(),
                                textureObj.getX() * unitScale, textureObj.getY() * unitScale,
                                textureObj.getOriginX() * unitScale, textureObj.getOriginY() * unitScale,
                                textureObj.getTextureRegion().getRegionWidth() * unitScale, textureObj.getTextureRegion().getRegionHeight() * unitScale,
                                textureObj.getScaleX(), textureObj.getScaleY(), textureObj.getRotation());
                    }
                }
            }
        }
    }

}
