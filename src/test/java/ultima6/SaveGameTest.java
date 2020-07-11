/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ultima6;

import com.badlogic.gdx.maps.MapObject;
import com.google.common.io.LittleEndianDataInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import org.apache.commons.io.IOUtils;

public class SaveGameTest {

    public static void main(String[] args) throws Exception {

        int[] basetiles = new int[1024];
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\BASETILE");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        for (int j = 0; j < 1024; j++) {
            basetiles[j] = dis.readUnsignedShort();
        }

        List<Map<String, java.lang.Object>> tileflags = readTileFlags();

        List<U6Object>[] surface = new ArrayList[64];
        List<U6Object>[] dungeon = new ArrayList[5];

        for (int i = 0; i < 64; i++) {
            surface[i] = new ArrayList<>();
        }

        for (int i = 0; i < 5; i++) {
            dungeon[i] = new ArrayList<>();
        }

        int offset = 0;
        for (int y = 0; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                surface[offset] = readObjBlock(x, y, basetiles, tileflags);
                for (U6Object obj : surface[offset]) {
                    System.out.println(obj);
                }
                offset++;
            }
        }

        for (int i = 0; i < 5; i++) {
            dungeon[i] = readObjBlockDungeon(i, basetiles, tileflags);
            for (U6Object obj : dungeon[i]) {
                System.out.println(obj);
            }
        }

        U6Object[] actorList = readObjList(basetiles);

    }

    private void loadActors(ByteBuffer bb) {

    }

    private void loadClock(ByteBuffer bb) {

    }

    private void loadPlayer(ByteBuffer bb) {

    }

    private void loadParty(ByteBuffer bb) {

    }

    private static List<U6Object> readObjBlock(int idx, int idy, int[] basetiles, List<Map<String, java.lang.Object>> tileflags) throws Exception {
        String chars = "ABCDEFGH";
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idy) + chars.charAt(idx));
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        return readObjBlock(basetiles, tileflags, dis);
    }

    private static List<U6Object> readObjBlockDungeon(int idx, int[] basetiles, List<Map<String, java.lang.Object>> tileflags) throws Exception {
        String chars = "ABCDEFGH";
        FileInputStream is = new FileInputStream("D:\\ultima\\ULTIMA6\\SAVEGAME\\OBJBLK" + chars.charAt(idx) + "I");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);
        return readObjBlock(basetiles, tileflags, dis);
    }

    private static List<U6Object> readObjBlock(int[] basetiles, List<Map<String, java.lang.Object>> tileflags, LittleEndianDataInputStream dis) throws Exception {

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

            int mask = status & Objects.OBJ_STATUS_MASK_GET;

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

                    if (mask == Objects.OBJ_STATUS_ON_MAP) {
                        obj.on_map = true;
                    } else if (mask == Objects.OBJ_STATUS_IN_CONTAINER) {
                        obj.in_container = true;
                    } else if (mask == Objects.OBJ_STATUS_IN_INVENTORY) {
                        obj.in_inventory = true;
                        obj.npc = obj.x;
                    } else if (mask == Objects.OBJ_STATUS_READIED) {
                        obj.readied = true;
                    }

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

                    if (obj.object == Objects.Object.CRANK.getId()) {
                        obj.tile = 273;
                    }

                    if (obj.object == Objects.Object.CHAIN.getId()) {
                        obj.tile = 311;
                    }

                    objects.add(obj);
                    tile--;
                }
            }

        }

        //fix for stone lions and floor2 layering issue
        Collections.sort(objects, new Comparator<U6Object>() {
            @Override
            public int compare(U6Object o1, U6Object o2) {
                if (o1.name.equals("STONE_LION") && o2.name.equals("FLOOR2")) {
                    return 1;
                }
                if (o1.name.equals("FLOOR2") && o2.name.equals("STONE_LION")) {
                    return -1;
                }
                return 0;
            }
        });

        return objects;
    }

    private static List<Map<String, java.lang.Object>> readTileFlags() throws Exception {
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

    private static boolean isStackable(int id) {
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

    private static class U6Object extends MapObject {

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
        boolean in_container;
        boolean readied;
        boolean in_inventory;

        int portal_dest_x;
        int portal_dest_y;
        int portal_dest_z;

        List<U6Object> contents = new ArrayList<>();
        
        @Override
        public String getName () {
            return name;
	}

        @Override
        public String toString() {

            StringBuilder contentProperties = new StringBuilder();
            for (int j = 0; j < contents.size(); j++) {
                U6Object obj = contents.get(j);
                contentProperties.append(String.format("\tname=\"stack-%d\" value=\"%s, %d, %d, %d\"/>\n", j, obj.name, obj.quantity, obj.quality, obj.status));
            }

            String statMask = String.format("%8s", Integer.toBinaryString(0xFF & status)).replaceAll(" ", "0");

            return name + ", id=" + id + ", x=" + x + ", y=" + y + ", z=" + z + ", frame=" + frame + ", tile=" + tile + ", object="
                    + object + ", npc=" + npc + ", status=" + status + " [" + statMask + "], quality=" + quality + ", quantity="
                    + quantity + ", on_top=" + on_top + ", on_map=" + on_map + ", in_container=" + in_container + ", readied=" + readied + ", in_inventory=" + in_inventory
                    + (contents.size() > 0 ? "\n" + contentProperties : "");
        }

    }

    private static U6Object[] readObjList(int[] basetiles) throws Exception {

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
            Conversations.Conversation c = convs.get(objects[i].npc);
            objects[i].name = c != null ? c.getName() : Objects.Object.getName(object);
            objects[i].object = object;
            objects[i].frame = frame;
            objects[i].tile = basetiles[objects[i].object] + objects[i].frame;
        }
        return objects;
    }

}
