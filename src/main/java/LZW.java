
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.apache.commons.io.FileUtils;

public class LZW {

    public static int U6TILE_PLAIN = 0x0;
    public static int U6TILE_TRANS = 0x5;
    public static int U6TILE_PBLCK = 0xA;

    public static void main(String args[]) throws Exception {

        byte[] animmask = FileUtils.readFileToByteArray(new File("D:\\Nuvie\\tools\\animmask.uncompressed"));
        byte[] maptiles = FileUtils.readFileToByteArray(new File("D:\\Nuvie\\tools\\maptiles.uncompressed"));
        byte[] objtiles = FileUtils.readFileToByteArray(new File("D:\\ultima\\ULTIMA6\\OBJTILES.VGA"));
        byte[] tileidx = FileUtils.readFileToByteArray(new File("D:\\ultima\\ULTIMA6\\TILEINDX.VGA"));
        byte[] masktypes = FileUtils.readFileToByteArray(new File("D:\\Nuvie\\tools\\masktype.uncompressed"));
        byte[] u6mcga = FileUtils.readFileToByteArray(new File("D:\\Nuvie\\tools\\u6mcga.uncompressed"));
        byte[] palette = FileUtils.readFileToByteArray(new File("D:\\ultima\\ULTIMA6\\U6PAL"));

        Map<Integer, Color> palMap = new HashMap<>();
        for (int i = 0, j = 0; i < 256; i++, j += 3) {
            Color col = new Color(palette[j] << 2, palette[j + 1] << 2, palette[j + 2] << 2);
            palMap.put(i, col);
        }

        ByteBuffer allbb = ByteBuffer.allocate(maptiles.length + objtiles.length);
        allbb.put(maptiles);
        allbb.put(objtiles);
        allbb.rewind();

        ByteBuffer tileIdxBuf = ByteBuffer.wrap(tileidx);
        List<Tile> tiles = new ArrayList<>();

        for (int i = 0; i < 2048; i++) {
            Tile t = new Tile();
            t.id = i;
            t.data = new byte[256];

            int tile_offset = bytesToUnsignedShort(tileIdxBuf.get(), tileIdxBuf.get()) * 16;
            allbb.position(tile_offset);

            if (masktypes[i] == U6TILE_TRANS) {
                t.transparent = true;
                allbb.get(t.data, 0, 256);
            } else if (masktypes[i] == U6TILE_PLAIN) {
                allbb.get(t.data, 0, 256);
            } else {
                t.transparent = true;
                decodePixelBlockTile(allbb, t);
            }
            tiles.add(t);
        }
        allbb.rewind();
        tileIdxBuf.rewind();

        ByteBuffer bb = ByteBuffer.wrap(u6mcga);

        int[] sources = new int[32];
        int[] dests = new int[32];
        bb.position(0x2C00);
        for (int i = 0; i < 32; i++) {
            sources[i] = bytesToUnsignedShort(bb.get(), bb.get());
        }
        bb.position(0x2C40);
        for (int i = 0; i < 32; i++) {
            dests[i] = bytesToUnsignedShort(bb.get(), bb.get());
        }

        parseMask(tiles, ByteBuffer.wrap(animmask), sources, dests);

        tilesToPng(tiles, palMap);

//        BufferedImage output = new BufferedImage(8 * 16, 4 * 16, BufferedImage.TYPE_INT_ARGB);
//        int i = 0;
//        for (int y = 0; y < 4; y++) {
//            for (int x = 0; x < 8; x++) {
//                BufferedImage tile = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
//                byte[] mask = hybrids[sources[i]];
//                byte[] original_tile = tiles.get(sources[i]).data;
//
//                for (int yy = 0; yy < 16; yy++) {
//                    for (int xx = 0; xx < 16; xx++) {
//                        //if (mask[yy * 16 + xx] != 0) {
//                        Color color = palMap.get(original_tile[yy * 16 + xx] & 0xff);
//                        tile.setRGB(xx, yy, color.getRGB());
//                        //}
//                    }
//                }
//                output.getGraphics().drawImage(tile, x * 16, y * 16, null);
//                i++;
//            }
//        }
//
//        ImageIO.write(output, "PNG", new File("hybrids.png"));
    }

    public static void tilesToPng(List<Tile> tiles, Map<Integer, Color> palMap) throws Exception {
        BufferedImage output = new BufferedImage(32 * 16, 64 * 16, BufferedImage.TYPE_INT_ARGB);
        int i = 0;
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 32; x++) {
                Tile tile = tiles.get(i);
                BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
                for (int yy = 0; yy < 16; yy++) {
                    for (int xx = 0; xx < 16; xx++) {
                        if (tile.transparent && (tile.data[yy * 16 + xx] & 0xff) == 0xff) {
                            //transparent
                        } else {
                            Color color = palMap.get(tile.data[yy * 16 + xx] & 0xff);
                            img.setRGB(xx, yy, color.getRGB());
                        }
                    }
                }
                output.getGraphics().drawImage(img, x * 16, y * 16, null);
                i++;
            }
        }
        ImageIO.write(output, "PNG", new File("alltiles.png"));
    }

    public static void parseMask(List<Tile> tiles, ByteBuffer animmask, int[] sources, int[] dests) {

        byte[][] animmask_vga = new byte[32][64];
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 64; j++) {
                animmask_vga[i][j] = animmask.get();
            }
        }

        for (int i = 0; i < 32; i++) {

            byte[] source_tile = tiles.get(sources[i] / 2).data;
            byte[] dest_tile = tiles.get(dests[i] / 2).data;

            int copy_pos = 0;
            int db_index = 0;
            int displacement;
            int bytes2copy;

            bytes2copy = animmask_vga[i][db_index] & 0xff;

            if (bytes2copy != 0) {
                for (int j = 0; j < bytes2copy; j++) {
                    dest_tile[copy_pos] = source_tile[copy_pos];
                    copy_pos++;
                }
            }
            db_index++;

            displacement = animmask_vga[i][db_index] & 0xff;
            bytes2copy = animmask_vga[i][db_index + 1] & 0xff;
            db_index += 2;

            while ((displacement != 0) && (bytes2copy != 0)) {
                copy_pos += displacement;

                for (int j = 0; j < bytes2copy; j++) {
                    dest_tile[copy_pos] = source_tile[copy_pos];
                    copy_pos++;
                }

                displacement = animmask_vga[i][db_index] & 0xff;
                bytes2copy = animmask_vga[i][db_index + 1] & 0xff;
                db_index += 2;
            }
        }

    }

    public static void decodePixelBlockTile(ByteBuffer tile_data, Tile tile) {
        ByteBuffer bb = ByteBuffer.wrap(tile.data);
        while (true) {
            int disp = (tile_data.get() & 0xff + (tile_data.get() & 0xff << 8));
            int actual_add = disp % 160 + (disp >= 1760 ? 160 : 0);
            int len = tile_data.get() & 0xff;
            if (len == 0) {
                break;
            }
            try {
                bb.position(actual_add);

                for (int i = 0; i < len; i++) {
                    bb.put(tile_data.get());
                }
            } catch (Exception e) {
                System.out.printf("[%d] %s [%d] [%d] [%d]\n", tile.id, bb, len, actual_add, disp);

            }
        }

        /*
tiledata = array('c', "\xff" * 256)
length = unpack("B", sio.read(1))[0]
length = length * 16 - 1    # Value is in paragraphs.
twisted = sio.read(length) # removeme
twisted_io = StringIO(twisted)
tileidx = 0
# We break on encountering a runlength of 0 [since the tile
# can be padded with 0xED bytes], but also explicitly 
# stop at the end of the buffer as well.
while twisted_io.tell() < length:
        # Convert one run of pixels
        add, runlength = unpack("<HB", twisted_io.read(3))
        if runlength == 0: break
        run = array('c', twisted_io.read(runlength))

        actual_add = add % 160
        if add >= 1760:
                actual_add += 160
        tileidx += actual_add
        tiledata[tileidx:runlength] = run
        tileidx += runlength

tiledata = tiledata.tostring()
         */
    }

    static class Tile {

        int id;
        boolean transparent;
        byte[] data;
    }

    public static int bytesToUnsignedShort(byte byte1, byte byte2) {
        return (((byte2 & 0xFF) << 8) | (byte1 & 0xFF));
    }

}
