package ultima6;

import com.badlogic.gdx.graphics.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import ultima6.Conversations.Conversation;
import ultima6.Conversations.OutputStream;

public class Converse {

    public static void main2(String[] args) throws Exception {
        File dir = new File("D:\\Nuvie\\tools\\conv");
        File[] files = dir.listFiles();

        int size = 0;
        int count = 0;
        for (File f : files) {
            FileInputStream is = new FileInputStream(f);
            byte[] conv = IOUtils.toByteArray(is);
            size += conv.length;
            count++;
        }
        ByteBuffer bb = ByteBuffer.allocate(size + count * 2);
        for (File f : files) {
            FileInputStream is = new FileInputStream(f);
            byte[] conv = IOUtils.toByteArray(is);
            bb.putShort((short) conv.length);
            bb.put(conv);
        }
        GZIPOutputStream gz = new GZIPOutputStream(new FileOutputStream(new File("src\\main\\resources\\data\\conversations")));
        IOUtils.write(bb.array(), gz);
        gz.close();
    }

    public static void main(String[] args) throws Exception {
        
        Party party = new Party();
        Player player = new Player();
        player.setName("Paul");
        player.setParty(party);
        party.add(player);

        GZIPInputStream is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));
        byte[] tmp = IOUtils.toByteArray(is);
        is.close();

        Conversations convs = new Conversations();

        ByteBuffer bba = ByteBuffer.wrap(tmp);
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

        List<ByteTokenizer> list = new ArrayList<>();
        Iterator<Conversation> iter = convs.iter();
        while (iter.hasNext()) {
            Conversation conv = iter.next();
            ByteBuffer bb = conv.data();
            while (bb.position() < bb.limit()) {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    if (op == U6OP.DECL) {
                        int start = bb.position();
                        Conversations.seek(bb, U6OP.EVAL);
                        int end = bb.position();
                        ByteTokenizer t = new ByteTokenizer(bb, start, end - start + 1,
                                new byte[]{U6OP.DECL.code(), U6OP.EVAL.code()},
                                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code(), U6OP.VAR.code(), U6OP.SVAR.code()}
                        );
                        list.add(t);
                    } else {
                        bb.get();
                    }
                } else {
                    bb.get();
                }
            }
        }
        //Collections.sort(list);
        for (ByteTokenizer t : list) {
            System.out.println(debug(t.data()));

            Map<Integer, Integer> iVars = new HashMap<>();
            for (int i = 0; i < 30; i++) {
                iVars.put(i, 0);
            }
            Map<Integer, String> sVars = new HashMap<>();
            for (int i = 0; i < 30; i++) {
                sVars.put(i, "");
            }
            ByteBuffer bb = ByteBuffer.allocate(t.data().length + 5);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            bb.put(t.data());
            bb.rewind();
            bb.get();//decl
            Conversations.declare(player, iVars, sVars, bb, new OutputStream() {
                @Override
                public void print(String string, Color color) {
                }

                @Override
                public void close() {
                }

                @Override
                public void setPortrait(int npc) {
                }
            });
        }

    }

    public static void main1(String[] args) throws Exception {

        byte[] test = DatatypeConverter.parseHexBinary("A100B2D30186A7B05F0B0000A2");
        ByteTokenizer testbt = new ByteTokenizer(test,
                new byte[]{U6OP.IF.code(), U6OP.ELSE.code(), U6OP.ENDIF.code()},
                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code()}
        );
        int c = testbt.countTokens();
        byte[] first = testbt.nexToken();

        GZIPInputStream is = new GZIPInputStream(new FileInputStream("src\\main\\resources\\data\\conversations"));
        byte[] tmp = IOUtils.toByteArray(is);
        is.close();

        Conversations convs = new Conversations();

        ByteBuffer bba = ByteBuffer.wrap(tmp);
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

        List<ByteTokenizer> conditions = new ArrayList<>();

        Iterator<Conversation> iter = convs.iter();
        while (iter.hasNext()) {

            Conversation conv = iter.next();

            ByteBuffer bb = conv.data();

            while (bb.position() < bb.limit()) {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    if (op == U6OP.IF) {
                        int start = bb.position();
                        Conversations.seek(bb, U6OP.ENDIF);
                        int end = bb.position();
                        ByteTokenizer t = new ByteTokenizer(bb, start, end - start + 1,
                                new byte[]{U6OP.IF.code(), U6OP.ELSE.code(), U6OP.ENDIF.code()},
                                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code()}
                        );
                        conditions.add(t);
                    } else {
                        bb.get();
                    }
                } else {
                    bb.get();
                }
            }

        }

        for (ByteTokenizer t : conditions) {
            System.out.println("");

            if (t.countTokens() == 0 || t.countTokens() > 2) {
                //System.out.println(t.countTokens());
                //System.out.println(DatatypeConverter.printHexBinary(t.data()));
                continue;
            }

            byte[] expr = t.nexToken();
            System.out.println(debug(expr));

            if (t.hasMoreTokens()) {
                byte[] elseExpr = t.nexToken();
                System.out.println(debug(elseExpr));
            }
        }

    }

    private static String debug(byte[] data) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer bb = ByteBuffer.wrap(data);
        while (bb.position() < bb.limit()) {
            U6OP op = U6OP.get(bb);
            if (op != null) {
                sb.append(String.format("[%s]", op));
            } else {
                boolean ascii = false;//CharUtils.isAsciiPrintable((char) bb.get(bb.position()));
                sb.append(ascii ? "." : String.format("[%02X]", bb.get(bb.position())));
            }
            bb.get();
        }
        return sb.toString();
    }

}
