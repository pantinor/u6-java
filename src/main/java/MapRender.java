
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import com.google.common.io.LittleEndianDataInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*
 * The file "chunks" contains 1024 8x8 byte arrays giving tile indices.
  These consititute the building blocks of which the map is composed.
  
  The file "map" contains the world map, stored as an 8x8 array of
  16x16 arrays of indices into the set of chunks. Each index occupies
  3 nibbles, each pair of which is stored with the nibbles permuted
  strangely. The world map is followed by 5 dungeons, which are 32x32
  arrays of chunk indices.
 */
public class MapRender {

    public static void main(String[] args) throws Exception {

        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\CHUNKS");
        byte[] bytes = IOUtils.toByteArray(is);
        int[][][] chunks = new int[1024][8][8];
        for (int chunk_num = 0; chunk_num < 1024; chunk_num++) {
            for (int y = 0; y < 8; y++) {
                for (int x = 0; x < 8; x++) {
                    int offset = 64 * chunk_num + 8 * y + x;
                    int d = bytes[offset] & 0xff;
                    chunks[chunk_num][y][x] = d;
                }
            }
        }

        is = new FileInputStream("D:\\ultima\\ULTIMA6\\MAP");
        int[][][][] britannia = new int[8][8][16][16];
        for (int yy = 0; yy < 8; yy++) {
            for (int xx = 0; xx < 8; xx++) {
                for (int y = 0; y < 16; y++) {
                    for (int x = 0; x < 16; x += 2) {
                        int a = is.read() & 0xff;
                        int b = is.read() & 0xff;
                        int c = is.read() & 0xff;
                        int d = 256 * (b % 16) + a;
                        int e = 16 * c + (b / 16);
                        britannia[yy][xx][y][x] = d;
                        britannia[yy][xx][y][x + 1] = e;
                    }
                }
            }
        }

        int[] tiles = new int[1024 * 1024];
        int pos = 0;
        for (int yy = 0; yy < 8; yy++) {
            for (int y = 0; y < 16; y++) {
                for (int cy = 0; cy < 8; cy++) {
                    for (int xx = 0; xx < 8; xx++) {
                        for (int x = 0; x < 16; x++) {
                            for (int cx = 0; cx < 8; cx++) {
                                int tile = chunks[britannia[yy][xx][y][x]][cy][cx];
                                tiles[pos] = tile;
                                pos++;
                            }
                        }
                    }
                }
            }
        }

        
        int count = 1;
        StringBuilder sb = new StringBuilder();
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                sb.append(tiles[x + y * 1024] + 1 + ",");
                count++;
                if (count > 1024) {
                    count = 0;
                    sb.append("\n");
                }
            }
        }

        //System.out.println(sb.toString());
         
        List<Map<String, Object>> tileflags = readTileFlags();

        short[] basetiles = new short[1024];
        is = new FileInputStream("D:\\ultima\\ULTIMA6\\BASETILE");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        for (int i = 0; i < 1024; i++) {
            basetiles[i] = dis.readShort();
        }

        int[][] objects = new int[1024][1024];
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                readObjBlock(x, y, basetiles, tileflags, objects);
            }
        }

        count = 1;
        sb = new StringBuilder();
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                if (objects[y][x] > 0) {
                    sb.append((257 + objects[y][x]) + ",");
                } else {
                    sb.append("0,");
                }
                count++;
                if (count > 1024) {
                    count = 0;
                    sb.append("\n");
                }
            }
        }

        //System.out.println(sb.toString());

        U6Object[] objectList = readObjList(basetiles);
        U6Object[][] objectListGrid = new U6Object[1024][1024];
        for (U6Object obj : objectList) {
            objectListGrid[obj.y][obj.x] = obj;
        }
        
        count = 1;
        sb = new StringBuilder();
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                if (objectListGrid[y][x] != null && objectListGrid[y][x].z == 0) {
                    sb.append((257 + objectListGrid[y][x].tile) + ",");
                } else {
                    sb.append("0,");
                }
                count++;
                if (count > 1024) {
                    count = 0;
                    sb.append("\n");
                }
            }
        }

        //System.out.println(sb.toString());

    }

    private static List<Map<String, Object>> readTileFlags() throws Exception {
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\TILEFLAG");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        byte[] f1 = new byte[2048];
        byte[] f2 = new byte[2048];
        byte[] none = new byte[2048];
        byte[] f3 = new byte[2048];

        dis.read(f1);
        dis.read(f2);
        dis.read(none);
        dis.read(f3);

        List<Map<String, Object>> list = new ArrayList<>();

        for (int i = 0; i < 2048; i++) {
            Map<String, Object> map = new HashMap<>();

            map.put("wet", (f1[i] & 0x1) != 0);
            map.put("impassable", (f1[i] & 0x2) != 0);
            map.put("wall", (f1[i] & 0x4) != 0);
            map.put("damaging", (f1[i] & 0x8) != 0);
            map.put("sides", (((f1[i] & 0x10) != 0 ? "w" : "")
                    + ((f1[i] & 0x20) != 0 ? "s" : "")
                    + ((f1[i] & 0x40) != 0 ? "e" : "")
                    + ((f1[i] & 0x80) != 0 ? "n" : "")));

            map.put("lightlsb", (f2[i] & 0x1) != 0);
            map.put("lightmsb", (f2[i] & 0x2) != 0);
            map.put("boundary", (f2[i] & 0x4) != 0);
            map.put("lookthruboundary", (f2[i] & 0x8) != 0);
            map.put("ontop", (f2[i] & 0x10) != 0);
            map.put("noshootthru", (f2[i] & 0x20) != 0);
            map.put("vsize", ((f2[i] & 0x40) != 0 ? 2 : 1));
            map.put("hsize", ((f2[i] & 0x80) != 0 ? 2 : 1));

            map.put("warm", (f3[i] & 0x1) != 0);
            map.put("support", (f3[i] & 0x2) != 0);
            map.put("breakthruable", (f3[i] & 0x4) != 0);
            map.put("ignore", (f3[i] & 0x10) != 0);
            map.put("background", (f3[i] & 0x20) != 0);

            list.add(map);

        }
        return list;

    }

    private static void readObjBlock(int idx, int idy, short[] basetiles, List<Map<String, Object>> tileflags, int[][] objects) throws Exception {

        String chars = "ABCDEFGH";

        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idy) + chars.charAt(idx));
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        short count = dis.readShort();
        //System.out.printf("Reading block [%s][%s] count [%d]\n", chars.charAt(idy), chars.charAt(idx), count);
        for (int i = 0; i < count; i++) {
            int status = dis.readUnsignedByte();

            int x = dis.readUnsignedByte();
            int b1 = dis.readUnsignedByte();
            x += (b1 & 0x3) << 8;

            int y = (b1 & 0xfc) >> 2;
            int b2 = dis.readUnsignedByte();
            y += (b2 & 0xf) << 6;

            int z = (b2 & 0xf0) >> 4;

            int type = dis.readUnsignedShort();
            byte quantity = dis.readByte();
            byte quality = dis.readByte();

            int object = type & 0x3ff;
            int frame = type >> 10;

            boolean on_map = (status & 0x18) == 0;
            if (z == 0 && on_map) {
                int tile = basetiles[object] + frame;
                int objtile = tile;

                objects[y][x] = tile;

                int vsize = (Integer) tileflags.get(objtile).get("vsize");
                int hsize = (Integer) tileflags.get(objtile).get("hsize");
                for (int vs = 0; vs < vsize; vs++) {
                    for (int hs = 0; hs < hsize; hs++) {

                    }
                }
            }
        }
    }

    static class U6Object {

        int x;
        int y;
        int z;
        int type;
        int frame;
        int tile;
        int object;
    }

    private static U6Object[] readObjList(short[] basetiles) throws Exception {
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJLIST");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        dis.skipBytes(0x100);
        U6Object[] objects = new U6Object[256];
        for (int i = 0; i < 256; i++) {
            U6Object obj = new U6Object();
            int h = dis.readUnsignedByte();
            int d1 = dis.readUnsignedByte();
            int d2 = dis.readUnsignedByte();
            obj.x = ((d1 & 0x3) << 8 | h);
            obj.y = ((d2 & 0xf) << 6 | (d1 >> 2));
            obj.z = (d2 >> 4);
            objects[i] = obj;
        }
        for (int i = 0; i < 256; i++) {
            objects[i].type = dis.readUnsignedShort();
            objects[i].object = objects[i].type & 0x3ff;
            objects[i].frame = objects[i].type >> 10;
            objects[i].tile = basetiles[objects[i].object] + objects[i].frame;
        }
        return objects;
    }

}
