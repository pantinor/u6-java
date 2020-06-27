package ultima6;

import java.io.FileInputStream;
import org.apache.commons.io.IOUtils;
import com.google.common.io.LittleEndianDataInputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.FileUtils;
import ultima6.Conversations.Conversation;
import ultima6.Objects.Object;

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

        AnimData animData = readAnimData();

        List<Map<String, java.lang.Object>> tileflags = readTileFlags();

        int[] basetiles = new int[1024];
        is = new FileInputStream("D:\\ultima\\ULTIMA6\\BASETILE");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        for (int i = 0; i < 1024; i++) {
            basetiles[i] = dis.readUnsignedShort();
        }

        count = 1;

        StringBuilder objectLayer = new StringBuilder();
        StringBuilder portalLayer = new StringBuilder();
        StringBuilder itemsLayer = new StringBuilder();
        StringBuilder onTopLayer = new StringBuilder();
        StringBuilder eggLayer = new StringBuilder();

        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                List<U6Object> worldObjects = readObjBlock(x, y, basetiles, tileflags);
                count = setObjects(count, worldObjects, eggLayer, portalLayer, onTopLayer, itemsLayer, objectLayer);
                for (U6Object obj : worldObjects) {
                    //System.out.println(obj);
                }
            }
        }

        String[] dungObjectLayers = new String[5];
        String[] dungPortalLayers = new String[5];
        String[] dungItemsLayers = new String[5];
        String[] dungOnTopLayers = new String[5];
        String[] dungEggLayers = new String[5];

        for (int i = 0; i < 5; i++) {

            List<U6Object> dungeonObjects = (readObjBlockDungeon(i, basetiles, tileflags));

            StringBuilder dungObjectLayer = new StringBuilder();
            StringBuilder dungOnTopLayer = new StringBuilder();
            StringBuilder dungItemLayer = new StringBuilder();
            StringBuilder dungPortalLayer = new StringBuilder();
            StringBuilder dungEggLayer = new StringBuilder();

            count = 1;
            setObjects(count, dungeonObjects, dungEggLayer, dungPortalLayer, dungOnTopLayer, dungItemLayer, dungObjectLayer);
            for (U6Object obj : dungeonObjects) {
                //System.out.println(obj);
            }

            dungObjectLayers[i] = dungObjectLayer.toString();
            dungOnTopLayers[i] = dungOnTopLayer.toString();
            dungItemsLayers[i] = dungItemLayer.toString();
            dungPortalLayers[i] = dungPortalLayer.toString();
            dungEggLayers[i] = dungEggLayer.toString();

        }

        U6Object[] actorList = readObjList(basetiles);

        StringBuilder actorLayer = new StringBuilder();
        count = 1;
        for (U6Object obj : actorList) {
            if (obj.z == 0 && obj.object > 0) {
                actorLayer.append(obj.toString(count++));
            }
        }

        String[] dungActorLayers = new String[5];
        for (int i = 1; i < 6; i++) {
            StringBuilder dungActorLayer = new StringBuilder();
            for (U6Object obj : actorList) {
                if (obj.z == i && obj.object > 0) {
                    dungActorLayer.append(obj.toString(count++));
                }
            }
            dungActorLayers[i - 1] = dungActorLayer.toString();
        }

        FileUtils.writeStringToFile(new File("src/main/resources/data/u6world.tmx"), String.format(WORLD_TMX, ANIMATIONS,
                worldLayer1, eggLayer.toString(), objectLayer.toString(), portalLayer.toString(), itemsLayer.toString(), actorLayer.toString(), onTopLayer.toString()));
        for (int i = 0; i < 5; i++) {
            FileUtils.writeStringToFile(new File("src/main/resources/data/u6dungeon_" + (i + 1) + ".tmx"), String.format(DUNGEON_TMX, ANIMATIONS,
                    dLayer1[i], dungEggLayers[i], dungObjectLayers[i], dungPortalLayers[i], dungItemsLayers[i], dungActorLayers[i], dungOnTopLayers[i]));

        }
    }

    public static List<Map<String, java.lang.Object>> readTileFlags() throws Exception {
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

        List<Map<String, java.lang.Object>> list = new ArrayList<>();

        for (int i = 0; i < 2048; i++) {
            Map<String, java.lang.Object> map = new HashMap<>();

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

    public static List<U6Object> readObjBlock(int idx, int idy, int[] basetiles, List<Map<String, java.lang.Object>> tileflags) throws Exception {
        String chars = "ABCDEFGH";
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idy) + chars.charAt(idx));
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        return readObjBlock(basetiles, tileflags, dis);
    }

    public static List<U6Object> readObjBlockDungeon(int idx, int[] basetiles, List<Map<String, java.lang.Object>> tileflags) throws Exception {
        String chars = "ABCDEFGH";
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idx) + "I");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        return readObjBlock(basetiles, tileflags, dis);
    }

    public static List<U6Object> readObjBlock(int[] basetiles, List<Map<String, java.lang.Object>> tileflags, LittleEndianDataInputStream dis) throws Exception {

        int count = dis.readUnsignedShort();

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

            int quantity = dis.readUnsignedByte();
            int quality = dis.readUnsignedByte();

            boolean on_map = (status & Objects.OBJ_STATUS_MASK_GET) == 0;

            int tile = basetiles[object] + frame;
            int objtile = tile;

            int vsize = (Integer) tileflags.get(objtile).get("vsize");
            int hsize = (Integer) tileflags.get(objtile).get("hsize");
            boolean on_top = (Boolean) tileflags.get(objtile).get("ontop");

            for (int vs = 0; vs < vsize; vs++) {
                for (int hs = 0; hs < hsize; hs++) {
                    U6Object obj = new U6Object();
                    obj.id = i;
                    obj.x = x - hs;
                    obj.y = y - vs;
                    obj.z = z;
                    obj.frame = frame;
                    obj.object = object;
                    obj.tile = tile;
                    obj.quality = quality;
                    obj.quantity = quantity;
                    obj.status = status;
                    obj.on_top = on_top;
                    obj.on_map = on_map;

                    if (isStackable(object)) {
                        obj.quantity = ((quality << 8) + quantity) & 0x000000ff;
                    }

                    obj.name = Objects.Object.getName(object);
                    if (obj.object == Objects.Object.LADDER.getId()) {
                        if (frame == 0) {
                            obj.name = "DOWN_LADDER";
                            if (z == 0) {
                                obj.portal_dest_x = (x & 0x07) | (x >> 2 & 0xF8);
                                obj.portal_dest_y = (y & 0x07) | (y >> 2 & 0xF8);
                            } else {
                                obj.portal_dest_x = x;
                                obj.portal_dest_y = y;
                            }
                            obj.portal_dest_z = z + 1;
                        } else {
                            obj.name = "UP_LADDER";
                            if (z == 1) {
                                obj.portal_dest_x = x / 8 * 8 * 4 + ((quality & 0x03) * 8) + (x - x / 8 * 8);
                                obj.portal_dest_y = y / 8 * 8 * 4 + ((quality >> 2 & 0x03) * 8) + (y - y / 8 * 8);
                            } else {
                                obj.portal_dest_x = x;
                                obj.portal_dest_y = y;
                            }
                            obj.portal_dest_z = z - 1;
                        }
                    }
                    if (obj.object == Objects.Object.MINE_SHAFT.getId()) {
                        if (z == 0) {
                            obj.portal_dest_x = (x & 0x07) | (x >> 2 & 0xF8);
                            obj.portal_dest_y = (y & 0x07) | (y >> 2 & 0xF8);
                            obj.portal_dest_z = z + 1;
                        } else {
                            obj.portal_dest_x = x;
                            obj.portal_dest_y = y;
                            obj.portal_dest_z = z - 1;
                        }
                    }

                    objects.add(obj);
                    tile--;
                }
            }

        }
        return objects;
    }

    public static class U6Object {

        int id;
        String name;
        int x;
        int y;
        int z;
        int frame;
        int tile;
        int object;
        int npc;
        int status;
        int quality;
        int quantity;
        boolean on_top;
        boolean on_map;
        int portal_dest_x;
        int portal_dest_y;
        int portal_dest_z;

        List<U6Object> contents = new ArrayList<>();

        @Override
        public String toString() {

            StringBuilder contentProperties = new StringBuilder();
            for (int j = 0; j < contents.size(); j++) {
                U6Object obj = contents.get(j);
                contentProperties.append(String.format("\tname=\"stack-%d\" value=\"%s, %d, %d, %d\"/>\n", j, obj.name, obj.quantity, obj.quality, obj.status));
            }

            return name + ", id=" + id + ", x=" + x + ", y=" + y + ", z=" + z + ", frame=" + frame + ", tile=" + tile + ", object="
                    + object + ", npc=" + npc + ", status=" + status + ", quality=" + quality + ", quantity="
                    + quantity + ", on_top=" + on_top + ", on_map=" + on_map + "" + (contents.size() > 0 ? "\n" + contentProperties : "");
        }

        public String toString(int id) {

            StringBuilder contentProperties = new StringBuilder();
            for (int j = 0; j < contents.size(); j++) {
                U6Object obj = contents.get(j);
                contentProperties.append(String.format("    <property name=\"stack-%d\" value=\"%s,%d,%d,%d\"/>\n", j, obj.name, obj.quantity, obj.quality, obj.status));
            }

            return "<object id=\"" + id + "\" name=\"" + name + "\" gid=\"" + (tile + 1) + "\" x=\"" + ((x) * 16)
                    + "\" y=\"" + ((y + 1) * 16) + "\" width=\"16\" height=\"16\">\n"
                    + "   <properties>\n"
                    + "    <property name=\"object\" value=\"" + object + "\"/>\n"
                    + (frame > 0 ? "    <property name=\"frame\" value=\"" + frame + "\"/>\n" : "")
                    + (quantity > 0 ? "    <property name=\"qty\" value=\"" + quantity + "\"/>\n" : "")
                    + (quality > 0 ? "    <property name=\"quality\" value=\"" + quality + "\"/>\n" : "")
                    + (status > 0 ? "    <property name=\"status\" value=\"" + status + "\"/>\n" : "")
                    + (npc > 0 ? "    <property name=\"npc\" value=\"" + npc + "\"/>\n" : "")
                    + (portal_dest_x > 0 || portal_dest_y > 0
                            ? "    <property name=\"portal_dest_x\" value=\"" + portal_dest_x + "\"/>\n"
                            + "    <property name=\"portal_dest_y\" value=\"" + portal_dest_y + "\"/>\n"
                            + "    <property name=\"portal_dest_z\" value=\"" + portal_dest_z + "\"/>\n"
                            : "")
                    + (contents.size() > 0 ? contentProperties : "")
                    + "   </properties>\n"
                    + "  </object>\n";
        }
    }

    public static boolean isStackable(int id) {
        Objects.Object obj = Objects.Object.get(id);
        switch (obj) {
            case TORCH:
            case LOCK_PICK:
            case GEM:
            case ARROW:
            case BOLT:
            case BLACK_PEARL:
            case BLOOD_MOSS:
            case GARLIC:
            case GINSENG:
            case MANDRAKE:
            case NIGHTSHADE:
            case SPIDER_SILK:
            case SULFUROUS_ASH:
            case EFFECT:
            case BREAD:
            case MEAT_PORTION:
            case FLASK_OF_OIL:
            case EGG:
            case GOLD_NUGGET:
            case ZU_YLEM:
            case SILVER_SNAKE_VENOM:
            case GOLD_COIN:
                return true;
            default:
                return false;
        }

    }

    public static int setObjects(int count, List<U6Object> objects, StringBuilder eggLayer, StringBuilder portalLayer,
            StringBuilder onTopLayer, StringBuilder itemLayer, StringBuilder objectLayer) {

        for (int j = 0; j < objects.size(); j++) {
            U6Object obj = objects.get(j);
            if (!obj.on_map || obj.status > 1) {
                U6Object container = findContainer(objects, obj.x);
                if (container != null) {
                    container.contents.add(obj);
                } else {
                    System.out.printf("%s cannot find container for id %d\n", obj.name, obj.x);
                }
            }
        }

        for (int j = 0; j < objects.size(); j++) {
            U6Object obj = objects.get(j);
            if (obj.object == Objects.Object.EGG.getId()) {
                eggLayer.append(obj.toString(count++));
            } else if (!obj.on_map || obj.status > 1) {
                //nothing
            } else if (obj.object == Objects.Object.MINE_SHAFT.getId() || obj.object == Objects.Object.LADDER.getId()) {
                portalLayer.append(obj.toString(count++));
            } else if (obj.on_top) {
                onTopLayer.append(obj.toString(count++));
            } else if (obj.status == 1 || obj.object == Objects.Object.MOONSTONE.getId()) {
                itemLayer.append(obj.toString(count++));
            } else {
                objectLayer.append(obj.toString(count++));
            }
        }

        return count;
    }

    public static U6Object findContainer(List<U6Object> objects, int id) {
        for (int j = 0; j < objects.size(); j++) {
            U6Object obj = objects.get(j);
            if (obj.id == id) {
                return obj;
            }
        }
        return null;
    }

    public static U6Object[] readObjList(int[] basetiles) throws Exception {

        InputStream is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));
        byte[] conv = IOUtils.toByteArray(is);
        is.close();

        Conversations convs = new Conversations();

        ByteBuffer bba = ByteBuffer.wrap(conv);
        while (bba.position() < bba.limit()) {
            short len = bba.getShort();
            byte[] data = new byte[len];
            bba.get(data);
            StringBuilder sb = new StringBuilder();
            byte b = 0;
            for (int i = 2; i < 20; i++) {
                b = data[i];
                if (b == (byte) 0xf1) {
                    break;
                }
                sb.append((char) b);
            }
            convs.put(data[1] & 0xff, sb.toString(), data);
        }

        is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJLIST");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        dis.skipBytes(0x100);
        U6Object[] objects = new U6Object[256];
        for (int i = 0; i < 256; i++) {
            U6Object obj = new U6Object();
            int h = dis.readUnsignedByte();
            int d1 = dis.readUnsignedByte();
            int d2 = dis.readUnsignedByte();
            obj.npc = i;
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
            Conversation c = convs.get(objects[i].npc);
            objects[i].name = c != null ? c.getName() : Objects.Object.getName(object);
            objects[i].object = object;
            objects[i].frame = frame;
            objects[i].tile = basetiles[objects[i].object] + objects[i].frame;
        }
        return objects;
    }

    public static class AnimData {

        int count;
        int[] tiles;
        int[] firsts;
        int[] masks;
        int[] shifts;

    }

    public static AnimData readAnimData() throws Exception {

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
//            List<Integer> l = new ArrayList<>();
//            for (int timer = 0; timer < 20 * 50; timer += 50) {
//                int current_anim_frame = (timer & data.masks[i]) >> data.shifts[i];
//                if (!l.contains(current_anim_frame)) {
//                    int tile = data.firsts[i] + current_anim_frame;
//                    System.out.println("<frame tileid=\"" + tile + "\" duration=\"150\"/>");
//                    l.add(current_anim_frame);
//                }
//
//            }
//            System.out.println("</animation>\n</tile>");
//        }
        return data;

    }

    private static final String DUNGEON_TMX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<map version=\"1.0\" tiledversion=\"1.1.5\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"256\" height=\"256\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\" nextobjectid=\"1\">\n"
            + "    <tileset firstgid=\"1\" name=\"u6tiles+objects\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"2048\" columns=\"32\">\n"
            + "        <image source=\"u6tiles+objects.png\" trans=\"ff00ff\" width=\"512\" height=\"1024\"/>\n"
            + "%s"
            + "    </tileset>\n"
            + "    <layer name=\"base\" width=\"256\" height=\"256\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "<objectgroup name=\"eggs\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"objects\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"portals\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"items\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"actors\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"on_top\" width=\"256\" height=\"256\">\n%s</objectgroup>\n"
            + "</map>";

    private static final String WORLD_TMX = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
            + "<map version=\"1.0\" tiledversion=\"1.1.5\" orientation=\"orthogonal\" renderorder=\"right-down\" width=\"1024\" height=\"1024\" tilewidth=\"16\" tileheight=\"16\" infinite=\"0\" nextobjectid=\"1\">\n"
            + "    <tileset firstgid=\"1\" name=\"u6tiles+objects\" tilewidth=\"16\" tileheight=\"16\" tilecount=\"2048\" columns=\"32\">\n"
            + "        <image source=\"u6tiles+objects.png\" trans=\"ff00ff\" width=\"512\" height=\"1024\"/>\n"
            + "%s"
            + "    </tileset>\n"
            + "    <layer name=\"base\" width=\"1024\" height=\"1024\">\n"
            + "        <data encoding=\"csv\">\n"
            + "%s"
            + "        </data>\n"
            + "    </layer>\n"
            + "<objectgroup name=\"eggs\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"objects\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"portals\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"items\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"actors\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "<objectgroup name=\"on_top\" width=\"1024\" height=\"1024\">\n%s</objectgroup>\n"
            + "</map>";

    public static final String ANIMATIONS = "<tile id=\"8\">\n"
            + "<animation>\n"
            + "<frame tileid=\"448\" duration=\"150\"/>\n"
            + "<frame tileid=\"449\" duration=\"150\"/>\n"
            + "<frame tileid=\"450\" duration=\"150\"/>\n"
            + "<frame tileid=\"451\" duration=\"150\"/>\n"
            + "<frame tileid=\"452\" duration=\"150\"/>\n"
            + "<frame tileid=\"453\" duration=\"150\"/>\n"
            + "<frame tileid=\"454\" duration=\"150\"/>\n"
            + "<frame tileid=\"455\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"9\">\n"
            + "<animation>\n"
            + "<frame tileid=\"456\" duration=\"150\"/>\n"
            + "<frame tileid=\"457\" duration=\"150\"/>\n"
            + "<frame tileid=\"458\" duration=\"150\"/>\n"
            + "<frame tileid=\"459\" duration=\"150\"/>\n"
            + "<frame tileid=\"460\" duration=\"150\"/>\n"
            + "<frame tileid=\"461\" duration=\"150\"/>\n"
            + "<frame tileid=\"462\" duration=\"150\"/>\n"
            + "<frame tileid=\"463\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"10\">\n"
            + "<animation>\n"
            + "<frame tileid=\"464\" duration=\"150\"/>\n"
            + "<frame tileid=\"465\" duration=\"150\"/>\n"
            + "<frame tileid=\"466\" duration=\"150\"/>\n"
            + "<frame tileid=\"467\" duration=\"150\"/>\n"
            + "<frame tileid=\"468\" duration=\"150\"/>\n"
            + "<frame tileid=\"469\" duration=\"150\"/>\n"
            + "<frame tileid=\"470\" duration=\"150\"/>\n"
            + "<frame tileid=\"471\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"11\">\n"
            + "<animation>\n"
            + "<frame tileid=\"472\" duration=\"150\"/>\n"
            + "<frame tileid=\"473\" duration=\"150\"/>\n"
            + "<frame tileid=\"474\" duration=\"150\"/>\n"
            + "<frame tileid=\"475\" duration=\"150\"/>\n"
            + "<frame tileid=\"476\" duration=\"150\"/>\n"
            + "<frame tileid=\"477\" duration=\"150\"/>\n"
            + "<frame tileid=\"478\" duration=\"150\"/>\n"
            + "<frame tileid=\"479\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"12\">\n"
            + "<animation>\n"
            + "<frame tileid=\"480\" duration=\"150\"/>\n"
            + "<frame tileid=\"481\" duration=\"150\"/>\n"
            + "<frame tileid=\"482\" duration=\"150\"/>\n"
            + "<frame tileid=\"483\" duration=\"150\"/>\n"
            + "<frame tileid=\"484\" duration=\"150\"/>\n"
            + "<frame tileid=\"485\" duration=\"150\"/>\n"
            + "<frame tileid=\"486\" duration=\"150\"/>\n"
            + "<frame tileid=\"487\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"13\">\n"
            + "<animation>\n"
            + "<frame tileid=\"488\" duration=\"150\"/>\n"
            + "<frame tileid=\"489\" duration=\"150\"/>\n"
            + "<frame tileid=\"490\" duration=\"150\"/>\n"
            + "<frame tileid=\"491\" duration=\"150\"/>\n"
            + "<frame tileid=\"492\" duration=\"150\"/>\n"
            + "<frame tileid=\"493\" duration=\"150\"/>\n"
            + "<frame tileid=\"494\" duration=\"150\"/>\n"
            + "<frame tileid=\"495\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"14\">\n"
            + "<animation>\n"
            + "<frame tileid=\"496\" duration=\"150\"/>\n"
            + "<frame tileid=\"497\" duration=\"150\"/>\n"
            + "<frame tileid=\"498\" duration=\"150\"/>\n"
            + "<frame tileid=\"499\" duration=\"150\"/>\n"
            + "<frame tileid=\"500\" duration=\"150\"/>\n"
            + "<frame tileid=\"501\" duration=\"150\"/>\n"
            + "<frame tileid=\"502\" duration=\"150\"/>\n"
            + "<frame tileid=\"503\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"15\">\n"
            + "<animation>\n"
            + "<frame tileid=\"504\" duration=\"150\"/>\n"
            + "<frame tileid=\"505\" duration=\"150\"/>\n"
            + "<frame tileid=\"506\" duration=\"150\"/>\n"
            + "<frame tileid=\"507\" duration=\"150\"/>\n"
            + "<frame tileid=\"508\" duration=\"150\"/>\n"
            + "<frame tileid=\"509\" duration=\"150\"/>\n"
            + "<frame tileid=\"510\" duration=\"150\"/>\n"
            + "<frame tileid=\"511\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"699\">\n"
            + "<animation>\n"
            + "<frame tileid=\"298\" duration=\"150\"/>\n"
            + "<frame tileid=\"299\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"862\">\n"
            + "<animation>\n"
            + "<frame tileid=\"288\" duration=\"150\"/>\n"
            + "<frame tileid=\"289\" duration=\"150\"/>\n"
            + "<frame tileid=\"290\" duration=\"150\"/>\n"
            + "<frame tileid=\"291\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"831\">\n"
            + "<animation>\n"
            + "<frame tileid=\"292\" duration=\"150\"/>\n"
            + "<frame tileid=\"294\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"860\">\n"
            + "<animation>\n"
            + "<frame tileid=\"300\" duration=\"150\"/>\n"
            + "<frame tileid=\"301\" duration=\"150\"/>\n"
            + "<frame tileid=\"302\" duration=\"150\"/>\n"
            + "<frame tileid=\"303\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1166\">\n"
            + "<animation>\n"
            + "<frame tileid=\"304\" duration=\"150\"/>\n"
            + "<frame tileid=\"305\" duration=\"150\"/>\n"
            + "<frame tileid=\"306\" duration=\"150\"/>\n"
            + "<frame tileid=\"307\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"846\">\n"
            + "<animation>\n"
            + "<frame tileid=\"320\" duration=\"150\"/>\n"
            + "<frame tileid=\"321\" duration=\"150\"/>\n"
            + "<frame tileid=\"322\" duration=\"150\"/>\n"
            + "<frame tileid=\"323\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"847\">\n"
            + "<animation>\n"
            + "<frame tileid=\"324\" duration=\"150\"/>\n"
            + "<frame tileid=\"325\" duration=\"150\"/>\n"
            + "<frame tileid=\"326\" duration=\"150\"/>\n"
            + "<frame tileid=\"327\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1016\">\n"
            + "<animation>\n"
            + "<frame tileid=\"336\" duration=\"150\"/>\n"
            + "<frame tileid=\"337\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"150\"/>\n"
            + "<frame tileid=\"339\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1018\">\n"
            + "<animation>\n"
            + "<frame tileid=\"340\" duration=\"150\"/>\n"
            + "<frame tileid=\"341\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1019\">\n"
            + "<animation>\n"
            + "<frame tileid=\"342\" duration=\"150\"/>\n"
            + "<frame tileid=\"343\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"150\"/>\n"
            + "<frame tileid=\"311\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1008\">\n"
            + "<animation>\n"
            + "<frame tileid=\"284\" duration=\"150\"/>\n"
            + "<frame tileid=\"285\" duration=\"150\"/>\n"
            + "<frame tileid=\"286\" duration=\"150\"/>\n"
            + "<frame tileid=\"287\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1009\">\n"
            + "<animation>\n"
            + "<frame tileid=\"272\" duration=\"150\"/>\n"
            + "<frame tileid=\"273\" duration=\"150\"/>\n"
            + "<frame tileid=\"274\" duration=\"150\"/>\n"
            + "<frame tileid=\"275\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1010\">\n"
            + "<animation>\n"
            + "<frame tileid=\"276\" duration=\"150\"/>\n"
            + "<frame tileid=\"278\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1011\">\n"
            + "<animation>\n"
            + "<frame tileid=\"280\" duration=\"150\"/>\n"
            + "<frame tileid=\"281\" duration=\"150\"/>\n"
            + "<frame tileid=\"282\" duration=\"150\"/>\n"
            + "<frame tileid=\"283\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"829\">\n"
            + "<animation>\n"
            + "<frame tileid=\"308\" duration=\"150\"/>\n"
            + "<frame tileid=\"309\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"902\">\n"
            + "<animation>\n"
            + "<frame tileid=\"312\" duration=\"150\"/>\n"
            + "<frame tileid=\"313\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"903\">\n"
            + "<animation>\n"
            + "<frame tileid=\"314\" duration=\"150\"/>\n"
            + "<frame tileid=\"315\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1021\">\n"
            + "<animation>\n"
            + "<frame tileid=\"344\" duration=\"150\"/>\n"
            + "<frame tileid=\"345\" duration=\"150\"/>\n"
            + "<frame tileid=\"346\" duration=\"150\"/>\n"
            + "<frame tileid=\"347\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"735\">\n"
            + "<animation>\n"
            + "<frame tileid=\"348\" duration=\"150\"/>\n"
            + "<frame tileid=\"350\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"150\"/>\n"
            + "<frame tileid=\"311\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1020\">\n"
            + "<animation>\n"
            + "<frame tileid=\"310\" duration=\"150\"/>\n"
            + "<frame tileid=\"311\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n"
            + "<tile id=\"1017\">\n"
            + "<animation>\n"
            + "<frame tileid=\"338\" duration=\"150\"/>\n"
            + "<frame tileid=\"339\" duration=\"150\"/>\n"
            + "</animation>\n"
            + "</tile>\n";
}
