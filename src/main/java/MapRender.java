
import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import com.google.common.io.LittleEndianDataInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;

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

    private static AnimData animData;

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

        int[][][] dungeon = new int[5][32][32];
        for (int i = 0; i < 5; i++) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x += 2) {
                    int a = is.read() & 0xff;
                    int b = is.read() & 0xff;
                    int c = is.read() & 0xff;
                    int d = 256 * (b % 16) + a;
                    int e = 16 * c + (b / 16);
                    dungeon[i][y][x] = d;
                    dungeon[i][y][x + 1] = e;
                }
            }
        }

        int[] dtiles = new int[5 * 256 * 256];
        pos = 0;
        for (int i = 0; i < 5; i++) {
            for (int y = 0; y < 32; y++) {
                for (int j = 0; j < 8; j++) {
                    for (int x = 0; x < 32; x++) {
                        int map_ptr = dungeon[i][y][x];
                        for (int k = 0; k < 8; k++) {
                            dtiles[pos] = chunks[map_ptr][j][k];
                            pos++;
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

        String worldLayer1 = sb.toString();
        worldLayer1 = worldLayer1.substring(0, worldLayer1.length() - 1);

        String[] dLayer1 = new String[5];
        count = 1;
        for (int i = 0; i < 5; i++) {
            sb = new StringBuilder();
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    sb.append(dtiles[(i * 256 * 256) + 256 * y + x] + 1 + ",");
                    count++;
                    if (count > 256) {
                        count = 0;
                        sb.append("\n");
                    }
                }
            }
            dLayer1[i] = sb.toString();
            dLayer1[i] = dLayer1[i].substring(0, dLayer1[i].length() - 1);

        }

        animData = readAnimData();

        List<Map<String, Object>> tileflags = readTileFlags();

        short[] basetiles = new short[1024];
        is = new FileInputStream("D:\\ultima\\ULTIMA6\\BASETILE");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        for (int i = 0; i < 1024; i++) {
            basetiles[i] = dis.readShort();
        }

        List<U6Object> worldObjects = new ArrayList<>();
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                worldObjects.addAll(readObjBlock(x, y, basetiles, tileflags));
            }
        }

        U6Object[][] grid = new U6Object[1024][1024];
        for (U6Object obj : worldObjects) {
            grid[obj.y][obj.x] = obj;
        }

        count = 1;
        sb = new StringBuilder();
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                if (grid[y][x] != null) {
                    sb.append((grid[y][x].tile) + 1 + ",");
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
        String worldLayer2 = sb.toString();
        worldLayer2 = worldLayer2.substring(0, worldLayer2.length() - 1);

        String[] dLayer2 = new String[5];

        for (int i = 0; i < 5; i++) {

            List<U6Object> dungeonObjects = (readObjBlockDungeon(i, basetiles, tileflags));

            grid = new U6Object[256][256];
            for (U6Object obj : dungeonObjects) {
                grid[obj.y][obj.x] = obj;
            }

            count = 1;
            sb = new StringBuilder();
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    if (grid[y][x] != null) {
                        sb.append((grid[y][x].tile) + 1 + ",");
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
            dLayer2[i] = sb.toString();
            dLayer2[i] = dLayer2[i].substring(0, dLayer2[i].length() - 1);

        }

        U6Object[] objectList = readObjList(basetiles);
        grid = new U6Object[1024][1024];
        for (U6Object obj : objectList) {
            grid[obj.y][obj.x] = obj;
        }

        count = 1;
        sb = new StringBuilder();
        for (int y = 0; y < 1024; y++) {
            for (int x = 0; x < 1024; x++) {
                if (grid[y][x] != null && grid[y][x].z == 0) {
                    sb.append((grid[y][x].tile) + 1 + ",");
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

        String worldLayer3 = sb.toString();
        worldLayer3 = worldLayer3.substring(0, worldLayer3.length() - 1);

        String[] dLayer3 = new String[5];
        for (int i = 1; i < 6; i++) {

            count = 1;
            sb = new StringBuilder();
            for (int y = 0; y < 256; y++) {
                for (int x = 0; x < 256; x++) {
                    if (grid[y][x] != null && grid[y][x].z == i) {
                        sb.append((grid[y][x].tile) + 1 + ",");
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
            dLayer3[i - 1] = sb.toString();
            dLayer3[i - 1] = dLayer3[i - 1].substring(0, dLayer3[i - 1].length() - 1);
        }

        FileUtils.writeStringToFile(new File("u6world.tmx"), String.format(WORLD_TMX, worldLayer1, worldLayer2, worldLayer3));
        for (int i = 0; i < 5; i++) {
            FileUtils.writeStringToFile(new File("u6dungeon_" + (i+1) + ".tmx"), String.format(DUNGEON_TMX, dLayer1[i], dLayer2[i], dLayer3[i]));
        }

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

    private static List<U6Object> readObjBlock(int idx, int idy, short[] basetiles, List<Map<String, Object>> tileflags) throws Exception {

        String chars = "ABCDEFGH";

        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idy) + chars.charAt(idx));
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        short count = dis.readShort();

        List<U6Object> objects = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int status = dis.readUnsignedByte();

            int x = dis.readUnsignedByte();
            int b1 = dis.readUnsignedByte();
            x += (b1 & 0x3) << 8;

            int y = (b1 & 0xfc) >> 2;
            int b2 = dis.readUnsignedByte();
            y += (b2 & 0xf) << 6;

            int z = (b2 & 0xf0) >> 4;

            b1 = dis.readUnsignedByte();
            b2 = dis.readUnsignedByte();
            int object = b1;
            object += (b2 & 0x3) << 8;
            int frame = (b2 & 0xfc) >> 2;

            byte quantity = dis.readByte();
            byte quality = dis.readByte();

            boolean on_map = (status & 0x18) == 0;
            if (z == 0 && on_map) {
                int tile = basetiles[object] + frame;
                int objtile = tile;
                int vsize = (Integer) tileflags.get(objtile).get("vsize");
                int hsize = (Integer) tileflags.get(objtile).get("hsize");

                for (int vs = 0; vs < vsize; vs++) {
                    for (int hs = 0; hs < hsize; hs++) {
                        U6Object obj = new U6Object();
                        obj.x = x - hs;
                        obj.y = y - vs;
                        obj.z = z;
                        obj.frame = frame;
                        obj.object = object;
                        obj.tile = tile;
                        objects.add(obj);
                        tile--;
                    }
                }

            }
        }
        return objects;
    }

    private static List<U6Object> readObjBlockDungeon(int idx, short[] basetiles, List<Map<String, Object>> tileflags) throws Exception {

        String chars = "ABCDEFGH";

        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idx) + "I");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        short count = dis.readShort();

        List<U6Object> objects = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            int status = dis.readUnsignedByte();

            int x = dis.readUnsignedByte();
            int b1 = dis.readUnsignedByte();
            x += (b1 & 0x3) << 8;

            int y = (b1 & 0xfc) >> 2;
            int b2 = dis.readUnsignedByte();
            y += (b2 & 0xf) << 6;

            int z = (b2 & 0xf0) >> 4;

            b1 = dis.readUnsignedByte();
            b2 = dis.readUnsignedByte();
            int object = b1;
            object += (b2 & 0x3) << 8;
            int frame = (b2 & 0xfc) >> 2;

            byte quantity = dis.readByte();
            byte quality = dis.readByte();

            boolean on_map = (status & 0x18) == 0;
            if (z == idx + 1 && on_map) {

                int tile = basetiles[object] + frame;
                int objtile = tile;
                int vsize = (Integer) tileflags.get(objtile).get("vsize");
                int hsize = (Integer) tileflags.get(objtile).get("hsize");

                for (int vs = 0; vs < vsize; vs++) {
                    for (int hs = 0; hs < hsize; hs++) {
                        U6Object obj = new U6Object();
                        obj.x = x - hs;
                        obj.y = y - vs;
                        obj.z = z;
                        obj.frame = frame;
                        obj.object = object;
                        obj.tile = tile;
                        objects.add(obj);
                        tile--;
                    }
                }

            }
        }
        return objects;
    }

    static class U6Object {

        int x;
        int y;
        int z;
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
            int b1 = dis.readUnsignedByte();
            int b2 = dis.readUnsignedByte();
            int object = b1;
            object += (b2 & 0x3) << 8;
            int frame = (b2 & 0xfc) >> 2;

            objects[i].object = object;
            objects[i].frame = frame;
            objects[i].tile = basetiles[objects[i].object] + objects[i].frame;
        }
        return objects;
    }

    static class AnimData {

        int count;
        int[] tiles;
        int[] firsts;
        int[] masks;
        int[] shifts;

    }

    private static AnimData readAnimData() throws Exception {

        /*
          <tile id="115">
            <animation>
             <frame tileid="115" duration="500"/>
             <frame tileid="116" duration="500"/>
             <frame tileid="117" duration="500"/>
             <frame tileid="118" duration="500"/>
            </animation>
           </tile>
         */
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\animdata");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        AnimData data = new AnimData();
        data.count = dis.readUnsignedShort();
        data.tiles = new int[32];
        for (int i = 0; i < 32; i++) {
            data.tiles[i] = dis.readUnsignedShort();
        }
        data.firsts = new int[32];
        for (int i = 0; i < 32; i++) {
            data.firsts[i] = dis.readUnsignedShort();
        }
        data.masks = new int[32];
        for (int i = 0; i < 32; i++) {
            data.masks[i] = dis.readUnsignedByte();
        }
        data.shifts = new int[32];
        for (int i = 0; i < 32; i++) {
            data.shifts[i] = dis.readUnsignedByte();
        }

//        for (int i = 0; i < 32; i++) {
//            System.out.println("<tile id=\"" + data.tiles[i] + "\">\n<animation>");
//            for (int timer = 0; timer < 10; timer++) {
//                int current_anim_frame = (timer & data.masks[i]) >> data.shifts[i];
//                int tile = data.firsts[i] + current_anim_frame;
//                System.out.println("<frame tileid=\"" + tile + "\" duration=\"300\"/>");
//            }
//            System.out.println("</animation>\n</tile>");
//        }
        return data;

    }

    private static final String DUNGEON_TMX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<map version=\"1.0\" tiledversion=\"1.1.5\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"256\" height=\"256\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\" nextobjectid=\"1\">\n"
            + "    <tileset firstgid=\"1\" name=\"u6tiles+objects\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"2048\" columns=\"32\">\n"
            + "        <image source=\"u6tiles+objects.png\" trans=\"ff00ff\" width=\"512\" height=\"1024\"/>\n"
            + "<tile id=\"8\">\n"
            + "<animation>\n"
            + "<frame tileid=\"448\" duration=\"300\"/>\n"
            + "<frame tileid=\"448\" duration=\"300\"/>\n"
            + "<frame tileid=\"449\" duration=\"300\"/>\n"
            + "<frame tileid=\"449\" duration=\"300\"/>\n"
            + "<frame tileid=\"450\" duration=\"300\"/>\n"
            + "<frame tileid=\"450\" duration=\"300\"/>\n"
            + "<frame tileid=\"451\" duration=\"300\"/>\n"
            + "<frame tileid=\"451\" duration=\"300\"/>\n"
            + "<frame tileid=\"452\" duration=\"300\"/>\n"
            + "<frame tileid=\"452\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"9\">\n"
            + "<animation>\n"
            + "<frame tileid=\"456\" duration=\"300\"/>\n"
            + "<frame tileid=\"456\" duration=\"300\"/>\n"
            + "<frame tileid=\"457\" duration=\"300\"/>\n"
            + "<frame tileid=\"457\" duration=\"300\"/>\n"
            + "<frame tileid=\"458\" duration=\"300\"/>\n"
            + "<frame tileid=\"458\" duration=\"300\"/>\n"
            + "<frame tileid=\"459\" duration=\"300\"/>\n"
            + "<frame tileid=\"459\" duration=\"300\"/>\n"
            + "<frame tileid=\"460\" duration=\"300\"/>\n"
            + "<frame tileid=\"460\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"10\">\n"
            + "<animation>\n"
            + "<frame tileid=\"464\" duration=\"300\"/>\n"
            + "<frame tileid=\"464\" duration=\"300\"/>\n"
            + "<frame tileid=\"465\" duration=\"300\"/>\n"
            + "<frame tileid=\"465\" duration=\"300\"/>\n"
            + "<frame tileid=\"466\" duration=\"300\"/>\n"
            + "<frame tileid=\"466\" duration=\"300\"/>\n"
            + "<frame tileid=\"467\" duration=\"300\"/>\n"
            + "<frame tileid=\"467\" duration=\"300\"/>\n"
            + "<frame tileid=\"468\" duration=\"300\"/>\n"
            + "<frame tileid=\"468\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"11\">\n"
            + "<animation>\n"
            + "<frame tileid=\"472\" duration=\"300\"/>\n"
            + "<frame tileid=\"472\" duration=\"300\"/>\n"
            + "<frame tileid=\"473\" duration=\"300\"/>\n"
            + "<frame tileid=\"473\" duration=\"300\"/>\n"
            + "<frame tileid=\"474\" duration=\"300\"/>\n"
            + "<frame tileid=\"474\" duration=\"300\"/>\n"
            + "<frame tileid=\"475\" duration=\"300\"/>\n"
            + "<frame tileid=\"475\" duration=\"300\"/>\n"
            + "<frame tileid=\"476\" duration=\"300\"/>\n"
            + "<frame tileid=\"476\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"12\">\n"
            + "<animation>\n"
            + "<frame tileid=\"480\" duration=\"300\"/>\n"
            + "<frame tileid=\"480\" duration=\"300\"/>\n"
            + "<frame tileid=\"481\" duration=\"300\"/>\n"
            + "<frame tileid=\"481\" duration=\"300\"/>\n"
            + "<frame tileid=\"482\" duration=\"300\"/>\n"
            + "<frame tileid=\"482\" duration=\"300\"/>\n"
            + "<frame tileid=\"483\" duration=\"300\"/>\n"
            + "<frame tileid=\"483\" duration=\"300\"/>\n"
            + "<frame tileid=\"484\" duration=\"300\"/>\n"
            + "<frame tileid=\"484\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"13\">\n"
            + "<animation>\n"
            + "<frame tileid=\"488\" duration=\"300\"/>\n"
            + "<frame tileid=\"488\" duration=\"300\"/>\n"
            + "<frame tileid=\"489\" duration=\"300\"/>\n"
            + "<frame tileid=\"489\" duration=\"300\"/>\n"
            + "<frame tileid=\"490\" duration=\"300\"/>\n"
            + "<frame tileid=\"490\" duration=\"300\"/>\n"
            + "<frame tileid=\"491\" duration=\"300\"/>\n"
            + "<frame tileid=\"491\" duration=\"300\"/>\n"
            + "<frame tileid=\"492\" duration=\"300\"/>\n"
            + "<frame tileid=\"492\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"14\">\n"
            + "<animation>\n"
            + "<frame tileid=\"496\" duration=\"300\"/>\n"
            + "<frame tileid=\"496\" duration=\"300\"/>\n"
            + "<frame tileid=\"497\" duration=\"300\"/>\n"
            + "<frame tileid=\"497\" duration=\"300\"/>\n"
            + "<frame tileid=\"498\" duration=\"300\"/>\n"
            + "<frame tileid=\"498\" duration=\"300\"/>\n"
            + "<frame tileid=\"499\" duration=\"300\"/>\n"
            + "<frame tileid=\"499\" duration=\"300\"/>\n"
            + "<frame tileid=\"500\" duration=\"300\"/>\n"
            + "<frame tileid=\"500\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"15\">\n"
            + "<animation>\n"
            + "<frame tileid=\"504\" duration=\"300\"/>\n"
            + "<frame tileid=\"504\" duration=\"300\"/>\n"
            + "<frame tileid=\"505\" duration=\"300\"/>\n"
            + "<frame tileid=\"505\" duration=\"300\"/>\n"
            + "<frame tileid=\"506\" duration=\"300\"/>\n"
            + "<frame tileid=\"506\" duration=\"300\"/>\n"
            + "<frame tileid=\"507\" duration=\"300\"/>\n"
            + "<frame tileid=\"507\" duration=\"300\"/>\n"
            + "<frame tileid=\"508\" duration=\"300\"/>\n"
            + "<frame tileid=\"508\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"699\">\n"
            + "<animation>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"299\" duration=\"300\"/>\n"
            + "<frame tileid=\"299\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"862\">\n"
            + "<animation>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"290\" duration=\"300\"/>\n"
            + "<frame tileid=\"290\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"831\">\n"
            + "<animation>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "<frame tileid=\"294\" duration=\"300\"/>\n"
            + "<frame tileid=\"295\" duration=\"300\"/>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "<frame tileid=\"294\" duration=\"300\"/>\n"
            + "<frame tileid=\"295\" duration=\"300\"/>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"860\">\n"
            + "<animation>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"301\" duration=\"300\"/>\n"
            + "<frame tileid=\"301\" duration=\"300\"/>\n"
            + "<frame tileid=\"302\" duration=\"300\"/>\n"
            + "<frame tileid=\"302\" duration=\"300\"/>\n"
            + "<frame tileid=\"303\" duration=\"300\"/>\n"
            + "<frame tileid=\"303\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1166\">\n"
            + "<animation>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"306\" duration=\"300\"/>\n"
            + "<frame tileid=\"306\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"846\">\n"
            + "<animation>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"321\" duration=\"300\"/>\n"
            + "<frame tileid=\"321\" duration=\"300\"/>\n"
            + "<frame tileid=\"322\" duration=\"300\"/>\n"
            + "<frame tileid=\"322\" duration=\"300\"/>\n"
            + "<frame tileid=\"323\" duration=\"300\"/>\n"
            + "<frame tileid=\"323\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"847\">\n"
            + "<animation>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"325\" duration=\"300\"/>\n"
            + "<frame tileid=\"325\" duration=\"300\"/>\n"
            + "<frame tileid=\"326\" duration=\"300\"/>\n"
            + "<frame tileid=\"326\" duration=\"300\"/>\n"
            + "<frame tileid=\"327\" duration=\"300\"/>\n"
            + "<frame tileid=\"327\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1016\">\n"
            + "<animation>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1018\">\n"
            + "<animation>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1019\">\n"
            + "<animation>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1008\">\n"
            + "<animation>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"285\" duration=\"300\"/>\n"
            + "<frame tileid=\"285\" duration=\"300\"/>\n"
            + "<frame tileid=\"286\" duration=\"300\"/>\n"
            + "<frame tileid=\"286\" duration=\"300\"/>\n"
            + "<frame tileid=\"287\" duration=\"300\"/>\n"
            + "<frame tileid=\"287\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1009\">\n"
            + "<animation>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"273\" duration=\"300\"/>\n"
            + "<frame tileid=\"273\" duration=\"300\"/>\n"
            + "<frame tileid=\"274\" duration=\"300\"/>\n"
            + "<frame tileid=\"274\" duration=\"300\"/>\n"
            + "<frame tileid=\"275\" duration=\"300\"/>\n"
            + "<frame tileid=\"275\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1010\">\n"
            + "<animation>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "<frame tileid=\"278\" duration=\"300\"/>\n"
            + "<frame tileid=\"279\" duration=\"300\"/>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "<frame tileid=\"278\" duration=\"300\"/>\n"
            + "<frame tileid=\"279\" duration=\"300\"/>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1011\">\n"
            + "<animation>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"281\" duration=\"300\"/>\n"
            + "<frame tileid=\"281\" duration=\"300\"/>\n"
            + "<frame tileid=\"282\" duration=\"300\"/>\n"
            + "<frame tileid=\"282\" duration=\"300\"/>\n"
            + "<frame tileid=\"283\" duration=\"300\"/>\n"
            + "<frame tileid=\"283\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"829\">\n"
            + "<animation>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"309\" duration=\"300\"/>\n"
            + "<frame tileid=\"309\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"902\">\n"
            + "<animation>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"903\">\n"
            + "<animation>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1021\">\n"
            + "<animation>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"345\" duration=\"300\"/>\n"
            + "<frame tileid=\"345\" duration=\"300\"/>\n"
            + "<frame tileid=\"346\" duration=\"300\"/>\n"
            + "<frame tileid=\"346\" duration=\"300\"/>\n"
            + "<frame tileid=\"347\" duration=\"300\"/>\n"
            + "<frame tileid=\"347\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"735\">\n"
            + "<animation>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "<frame tileid=\"350\" duration=\"300\"/>\n"
            + "<frame tileid=\"351\" duration=\"300\"/>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "<frame tileid=\"350\" duration=\"300\"/>\n"
            + "<frame tileid=\"351\" duration=\"300\"/>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>"
            + "    </tileset>\n"
            + "    <layer name=\"base\" width=\"256\" height=\"256\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "    <layer name=\"objects\" width=\"256\" height=\"256\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "    <layer name=\"actors\" width=\"256\" height=\"256\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "</map>";

    private static final String WORLD_TMX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<map version=\"1.0\" tiledversion=\"1.1.5\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"1024\" height=\"1024\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\" nextobjectid=\"1\">\n"
            + "    <tileset firstgid=\"1\" name=\"u6tiles+objects\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"2048\" columns=\"32\">\n"
            + "        <image source=\"u6tiles+objects.png\" trans=\"ff00ff\" width=\"512\" height=\"1024\"/>\n"
            + "<tile id=\"8\">\n"
            + "<animation>\n"
            + "<frame tileid=\"448\" duration=\"300\"/>\n"
            + "<frame tileid=\"448\" duration=\"300\"/>\n"
            + "<frame tileid=\"449\" duration=\"300\"/>\n"
            + "<frame tileid=\"449\" duration=\"300\"/>\n"
            + "<frame tileid=\"450\" duration=\"300\"/>\n"
            + "<frame tileid=\"450\" duration=\"300\"/>\n"
            + "<frame tileid=\"451\" duration=\"300\"/>\n"
            + "<frame tileid=\"451\" duration=\"300\"/>\n"
            + "<frame tileid=\"452\" duration=\"300\"/>\n"
            + "<frame tileid=\"452\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"9\">\n"
            + "<animation>\n"
            + "<frame tileid=\"456\" duration=\"300\"/>\n"
            + "<frame tileid=\"456\" duration=\"300\"/>\n"
            + "<frame tileid=\"457\" duration=\"300\"/>\n"
            + "<frame tileid=\"457\" duration=\"300\"/>\n"
            + "<frame tileid=\"458\" duration=\"300\"/>\n"
            + "<frame tileid=\"458\" duration=\"300\"/>\n"
            + "<frame tileid=\"459\" duration=\"300\"/>\n"
            + "<frame tileid=\"459\" duration=\"300\"/>\n"
            + "<frame tileid=\"460\" duration=\"300\"/>\n"
            + "<frame tileid=\"460\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"10\">\n"
            + "<animation>\n"
            + "<frame tileid=\"464\" duration=\"300\"/>\n"
            + "<frame tileid=\"464\" duration=\"300\"/>\n"
            + "<frame tileid=\"465\" duration=\"300\"/>\n"
            + "<frame tileid=\"465\" duration=\"300\"/>\n"
            + "<frame tileid=\"466\" duration=\"300\"/>\n"
            + "<frame tileid=\"466\" duration=\"300\"/>\n"
            + "<frame tileid=\"467\" duration=\"300\"/>\n"
            + "<frame tileid=\"467\" duration=\"300\"/>\n"
            + "<frame tileid=\"468\" duration=\"300\"/>\n"
            + "<frame tileid=\"468\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"11\">\n"
            + "<animation>\n"
            + "<frame tileid=\"472\" duration=\"300\"/>\n"
            + "<frame tileid=\"472\" duration=\"300\"/>\n"
            + "<frame tileid=\"473\" duration=\"300\"/>\n"
            + "<frame tileid=\"473\" duration=\"300\"/>\n"
            + "<frame tileid=\"474\" duration=\"300\"/>\n"
            + "<frame tileid=\"474\" duration=\"300\"/>\n"
            + "<frame tileid=\"475\" duration=\"300\"/>\n"
            + "<frame tileid=\"475\" duration=\"300\"/>\n"
            + "<frame tileid=\"476\" duration=\"300\"/>\n"
            + "<frame tileid=\"476\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"12\">\n"
            + "<animation>\n"
            + "<frame tileid=\"480\" duration=\"300\"/>\n"
            + "<frame tileid=\"480\" duration=\"300\"/>\n"
            + "<frame tileid=\"481\" duration=\"300\"/>\n"
            + "<frame tileid=\"481\" duration=\"300\"/>\n"
            + "<frame tileid=\"482\" duration=\"300\"/>\n"
            + "<frame tileid=\"482\" duration=\"300\"/>\n"
            + "<frame tileid=\"483\" duration=\"300\"/>\n"
            + "<frame tileid=\"483\" duration=\"300\"/>\n"
            + "<frame tileid=\"484\" duration=\"300\"/>\n"
            + "<frame tileid=\"484\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"13\">\n"
            + "<animation>\n"
            + "<frame tileid=\"488\" duration=\"300\"/>\n"
            + "<frame tileid=\"488\" duration=\"300\"/>\n"
            + "<frame tileid=\"489\" duration=\"300\"/>\n"
            + "<frame tileid=\"489\" duration=\"300\"/>\n"
            + "<frame tileid=\"490\" duration=\"300\"/>\n"
            + "<frame tileid=\"490\" duration=\"300\"/>\n"
            + "<frame tileid=\"491\" duration=\"300\"/>\n"
            + "<frame tileid=\"491\" duration=\"300\"/>\n"
            + "<frame tileid=\"492\" duration=\"300\"/>\n"
            + "<frame tileid=\"492\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"14\">\n"
            + "<animation>\n"
            + "<frame tileid=\"496\" duration=\"300\"/>\n"
            + "<frame tileid=\"496\" duration=\"300\"/>\n"
            + "<frame tileid=\"497\" duration=\"300\"/>\n"
            + "<frame tileid=\"497\" duration=\"300\"/>\n"
            + "<frame tileid=\"498\" duration=\"300\"/>\n"
            + "<frame tileid=\"498\" duration=\"300\"/>\n"
            + "<frame tileid=\"499\" duration=\"300\"/>\n"
            + "<frame tileid=\"499\" duration=\"300\"/>\n"
            + "<frame tileid=\"500\" duration=\"300\"/>\n"
            + "<frame tileid=\"500\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"15\">\n"
            + "<animation>\n"
            + "<frame tileid=\"504\" duration=\"300\"/>\n"
            + "<frame tileid=\"504\" duration=\"300\"/>\n"
            + "<frame tileid=\"505\" duration=\"300\"/>\n"
            + "<frame tileid=\"505\" duration=\"300\"/>\n"
            + "<frame tileid=\"506\" duration=\"300\"/>\n"
            + "<frame tileid=\"506\" duration=\"300\"/>\n"
            + "<frame tileid=\"507\" duration=\"300\"/>\n"
            + "<frame tileid=\"507\" duration=\"300\"/>\n"
            + "<frame tileid=\"508\" duration=\"300\"/>\n"
            + "<frame tileid=\"508\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"699\">\n"
            + "<animation>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"298\" duration=\"300\"/>\n"
            + "<frame tileid=\"299\" duration=\"300\"/>\n"
            + "<frame tileid=\"299\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"862\">\n"
            + "<animation>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"288\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"289\" duration=\"300\"/>\n"
            + "<frame tileid=\"290\" duration=\"300\"/>\n"
            + "<frame tileid=\"290\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"831\">\n"
            + "<animation>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "<frame tileid=\"294\" duration=\"300\"/>\n"
            + "<frame tileid=\"295\" duration=\"300\"/>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "<frame tileid=\"294\" duration=\"300\"/>\n"
            + "<frame tileid=\"295\" duration=\"300\"/>\n"
            + "<frame tileid=\"292\" duration=\"300\"/>\n"
            + "<frame tileid=\"293\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"860\">\n"
            + "<animation>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"301\" duration=\"300\"/>\n"
            + "<frame tileid=\"301\" duration=\"300\"/>\n"
            + "<frame tileid=\"302\" duration=\"300\"/>\n"
            + "<frame tileid=\"302\" duration=\"300\"/>\n"
            + "<frame tileid=\"303\" duration=\"300\"/>\n"
            + "<frame tileid=\"303\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "<frame tileid=\"300\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1166\">\n"
            + "<animation>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"304\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"305\" duration=\"300\"/>\n"
            + "<frame tileid=\"306\" duration=\"300\"/>\n"
            + "<frame tileid=\"306\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"846\">\n"
            + "<animation>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"321\" duration=\"300\"/>\n"
            + "<frame tileid=\"321\" duration=\"300\"/>\n"
            + "<frame tileid=\"322\" duration=\"300\"/>\n"
            + "<frame tileid=\"322\" duration=\"300\"/>\n"
            + "<frame tileid=\"323\" duration=\"300\"/>\n"
            + "<frame tileid=\"323\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "<frame tileid=\"320\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"847\">\n"
            + "<animation>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"325\" duration=\"300\"/>\n"
            + "<frame tileid=\"325\" duration=\"300\"/>\n"
            + "<frame tileid=\"326\" duration=\"300\"/>\n"
            + "<frame tileid=\"326\" duration=\"300\"/>\n"
            + "<frame tileid=\"327\" duration=\"300\"/>\n"
            + "<frame tileid=\"327\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "<frame tileid=\"324\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1016\">\n"
            + "<animation>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"337\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "<frame tileid=\"336\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1018\">\n"
            + "<animation>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"341\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "<frame tileid=\"340\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1019\">\n"
            + "<animation>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"343\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "<frame tileid=\"342\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1008\">\n"
            + "<animation>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"285\" duration=\"300\"/>\n"
            + "<frame tileid=\"285\" duration=\"300\"/>\n"
            + "<frame tileid=\"286\" duration=\"300\"/>\n"
            + "<frame tileid=\"286\" duration=\"300\"/>\n"
            + "<frame tileid=\"287\" duration=\"300\"/>\n"
            + "<frame tileid=\"287\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "<frame tileid=\"284\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1009\">\n"
            + "<animation>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"273\" duration=\"300\"/>\n"
            + "<frame tileid=\"273\" duration=\"300\"/>\n"
            + "<frame tileid=\"274\" duration=\"300\"/>\n"
            + "<frame tileid=\"274\" duration=\"300\"/>\n"
            + "<frame tileid=\"275\" duration=\"300\"/>\n"
            + "<frame tileid=\"275\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "<frame tileid=\"272\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1010\">\n"
            + "<animation>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "<frame tileid=\"278\" duration=\"300\"/>\n"
            + "<frame tileid=\"279\" duration=\"300\"/>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "<frame tileid=\"278\" duration=\"300\"/>\n"
            + "<frame tileid=\"279\" duration=\"300\"/>\n"
            + "<frame tileid=\"276\" duration=\"300\"/>\n"
            + "<frame tileid=\"277\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1011\">\n"
            + "<animation>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"281\" duration=\"300\"/>\n"
            + "<frame tileid=\"281\" duration=\"300\"/>\n"
            + "<frame tileid=\"282\" duration=\"300\"/>\n"
            + "<frame tileid=\"282\" duration=\"300\"/>\n"
            + "<frame tileid=\"283\" duration=\"300\"/>\n"
            + "<frame tileid=\"283\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "<frame tileid=\"280\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"829\">\n"
            + "<animation>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"308\" duration=\"300\"/>\n"
            + "<frame tileid=\"309\" duration=\"300\"/>\n"
            + "<frame tileid=\"309\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"902\">\n"
            + "<animation>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"313\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "<frame tileid=\"312\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"903\">\n"
            + "<animation>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"315\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "<frame tileid=\"314\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1021\">\n"
            + "<animation>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"345\" duration=\"300\"/>\n"
            + "<frame tileid=\"345\" duration=\"300\"/>\n"
            + "<frame tileid=\"346\" duration=\"300\"/>\n"
            + "<frame tileid=\"346\" duration=\"300\"/>\n"
            + "<frame tileid=\"347\" duration=\"300\"/>\n"
            + "<frame tileid=\"347\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "<frame tileid=\"344\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"735\">\n"
            + "<animation>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "<frame tileid=\"350\" duration=\"300\"/>\n"
            + "<frame tileid=\"351\" duration=\"300\"/>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "<frame tileid=\"350\" duration=\"300\"/>\n"
            + "<frame tileid=\"351\" duration=\"300\"/>\n"
            + "<frame tileid=\"348\" duration=\"300\"/>\n"
            + "<frame tileid=\"349\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"311\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "<frame tileid=\"310\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"339\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "<frame tileid=\"338\" duration=\"300\"/>\n"
            + "</animation>\n"
            + "</tile>"
            + "    </tileset>\n"
            + "    <layer name=\"base\" width=\"1024\" height=\"1024\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "    <layer name=\"objects\" width=\"1024\" height=\"1024\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "    <layer name=\"actors\" width=\"1024\" height=\"1024\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "</map>";
}
