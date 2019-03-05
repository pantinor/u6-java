package ultima6;

import com.badlogic.gdx.graphics.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ultima6.Conversations.Conditional;
import ultima6.Conversations.Conversation;
import ultima6.Conversations.OutputStream;

public class Converse {

    public static void aggregateDecompressedConvs(String[] args) throws Exception {
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

    public static void main2(String[] args) throws Exception {

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
            for (int i = 0; i < 64; i++) {
                iVars.put(i, i);
            }
            Map<Integer, String> sVars = new HashMap<>();
            for (int i = 0; i < 64; i++) {
                sVars.put(i, "" + i);
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

        for (ByteTokenizer tok : conditions) {
            System.out.println(debug(tok.data()));

            Map<Integer, Integer> iVars = new HashMap<>();
            for (int i = 0; i < 64; i++) {
                iVars.put(i, i);
            }
            Map<Integer, String> sVars = new HashMap<>();
            for (int i = 0; i < 64; i++) {
                sVars.put(i, "" + i);
            }

            Conditional cond = new Conditional(tok);

            cond.evaluate(player, iVars, sVars, new OutputStream() {
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

    private static ByteBuffer parse(String text) {

        List<Byte> bytes = new ArrayList<>();

        StringTokenizer t = new StringTokenizer(text, "[]");
        while (t.hasMoreTokens()) {
            String st = t.nextToken();
            try {
                U6OP op = U6OP.valueOf(st);
                bytes.add(op.code());
            } catch (Exception e) {
                bytes.add(DatatypeConverter.parseHexBinary(st)[0]);
            }
        }

        ByteBuffer bb = ByteBuffer.allocate(bytes.size());
        bb.order(ByteOrder.LITTLE_ENDIAN);
        for (Byte b : bytes) {
            bb.put(b);
        }
        bb.flip();
        return bb;
    }

    @DataProvider
    public static Object[][] declares() {
        return new Object[][]{
            //{"[DECL][02][VAR][ASSIGN][ONE_BYTE][01][17][VAR][ONE_BYTE][01][ADD][RAND][EVAL]", 2, 4},
            {"[DECL][01][VAR][ASSIGN][ONE_BYTE][EB][00][FLAG][EVAL]", 1, 0},
            {"[DECL][02][VAR][ASSIGN][01][VAR][CANCARRY][ONE_BYTE][59][ONE_BYTE][01][WEIGHT][DIV][EVAL]", 2, 10},
            {"[DECL][00][VAR][ASSIGN][FOUR_BYTE][9F][0D][00][00][ONE_BYTE][23][INDEXOF][EVAL]", 0, 1},
            {"[DECL][07][VAR][ASSIGN][FOUR_BYTE][B9][12][00][00][06][VAR][DATA][ONE_BYTE][02][DIV][EVAL]", 7, 0},
            {"[DECL][00][SVAR][ASSIGN][22][SVAR][EVAL]", 0, "34"},
            {"[DECL][00][VAR][ASSIGN][01][VAR][ONE_BYTE][0A][MUL][EVAL]", 0, 10},
            {"[DECL][10][VAR][ASSIGN][ONE_BYTE][06][EVAL]", 16, 6},
            {"[DECL][09][VAR][ASSIGN][TWO_BYTE][A6][01][ONE_BYTE][01][WEIGHT][EVAL]", 9, 10},
            {"[DECL][FOUR_BYTE][06][13][00][00][02][VAR][DATA][ASSIGN][00][VAR][EVAL]", 0, 0},
            {"[DECL][08][VAR][ASSIGN][05][VAR][ONE_BYTE][01][SUB][EVAL]", 8, 4},
            {"[DECL][08][VAR][ASSIGN][07][VAR][17][VAR][ONE_BYTE][03][ADD][DIV][EVAL]", 8, 10}, //
        //
        };
    }

    @Test(dataProvider = "declares")
    public void testDeclare(String decl, Integer varName, Object expectedValue) throws Exception {

        Party party = new Party();
        Player player = new Player();
        player.setName("Paul");
        player.setParty(party);
        party.add(player);

        Map<Integer, Integer> iVars = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            iVars.put(i, i);
        }
        Map<Integer, String> sVars = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            sVars.put(i, "" + i);
        }

        ByteBuffer bb = parse(decl);
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

        if (expectedValue instanceof String) {
            assertEquals(sVars.get(varName), expectedValue);
        } else {
            assertEquals(iVars.get(varName), expectedValue);
        }

    }

    @DataProvider
    public static Object[][] conditions() {
        return new Object[][]{
            //{"[IF][07][VAR][EVAL][ENDIF]", true},
            //{"[IF][05][VAR][ONE_BYTE][0B][GT][03][VAR][CANCARRY][LAND][FOUR_BYTE][81][0A][00][00][00][VAR][DATA][ONE_BYTE][01][WEIGHT][GE][EVAL][NEW][03][VAR][EVAL][FOUR_BYTE][81][0A][00][00][00][VAR][DATA][EVAL][ONE_BYTE][00][EVAL][ONE_BYTE][01][EVAL][2C][20][70][6C][75][73][20][6F][6E][65][20][66][6F][72][20][6C][75][63][6B][21][22][ELSE][2C][20][24][59][2E][22][ENDIF]", true},
            //{"[IF][17][VAR][EVAL][73][ENDIF]", true},
            //{"[IF][ONE_BYTE][EB][00][FLAG][ONE_BYTE][00][EQ][EVAL][72][65][2E][22][SETF][ONE_BYTE][EB][EVAL][00][EVAL][ELSE][2E][22][ENDIF]", true},
            //{"[IF][23][SVAR][19][SVAR][EQ][EVAL][22][ELSE][22][ENDIF]", false}, 
            {"[IF][03][VAR][CANCARRY][ONE_BYTE][58][FOUR_BYTE][5D][07][00][00][09][SVAR][DATA][WEIGHT][ADD][ONE_BYTE][95][ONE_BYTE][01][WEIGHT][LT][EVAL][ENDIF]", true}, //{"[IF][00][VAR][17][VAR][LE][EVAL][JUMP][AB][02][00][00][ENDIF]", true}, //
        };
    }

    @Test(dataProvider = "conditions")
    public void testConditions(String text, Object expectedValue) throws Exception {

        Party party = new Party();
        Player player = new Player();
        player.setName("Paul");
        player.setParty(party);
        party.add(player);

        Map<Integer, Integer> iVars = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            iVars.put(i, i);
        }
        Map<Integer, String> sVars = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            sVars.put(i, "" + i);
        }

        ByteBuffer bb = parse(text);
        ByteTokenizer tok = new ByteTokenizer(bb, 0, bb.limit(),
                new byte[]{U6OP.IF.code(), U6OP.ELSE.code(), U6OP.ENDIF.code()},
                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code()}
        );
        Conditional cond = new Conditional(tok);
        boolean eval = cond.evaluate(player, iVars, sVars, new OutputStream() {
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

        assertEquals(eval, expectedValue);

    }

}
