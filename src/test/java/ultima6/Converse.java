package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.google.common.io.LittleEndianDataInputStream;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import javax.xml.bind.DatatypeConverter;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.CharUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ultima6.Conversations.Conversation;
import ultima6.Conversations.OutputStream;

public class Converse {

    private static final OutputStream OUTPUT = new OutputStream() {
        @Override
        public void print(String string, Color color) {
        }

        @Override
        public void close() {
        }

        @Override
        public void setPortrait(int npc) {
        }
    };

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

    public static void main(String[] args) throws Exception {

        Party party = new Party();

        Player player = new Player(0, "avatar");
        party.add(player);

        Player player2 = new Player(2, "dupre");
        party.add(player2);

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

        Iterator<Conversation> iter = convs.iter();
        while (iter.hasNext()) {

            Conversation conv = iter.next();

            System.out.println("\n**********************\n" + conv.getName());

            if (!conv.getName().equals("Iolo")) {
                //continue;
            }

            ByteBuffer bb = conv.data();

            boolean printCriteria = true;

//            while (bb.position() < bb.limit()) {
//                U6OP op = U6OP.get(bb);
//                if (op != null) {
//                    if (op == U6OP.WORKTYPE) {
//                        printCriteria = true;
//                    }
//                }
//                bb.get();
//            }
            if (printCriteria) {
                //debugOutput(bb);
                System.out.println(debug(conv.data().array()));
            }
        }

    }

    private static String debug(byte[] data) {
        StringBuilder sb = new StringBuilder();
        ByteBuffer bb = ByteBuffer.wrap(data);
        boolean printing = false;
        while (bb.position() < bb.limit()) {
            U6OP op = U6OP.get(bb);
            if (op != null) {

                if (op == U6OP.IF) {
                    printing = true;
                }

                if (printing) {
                    sb.append(String.format("[%s]", op));
                    if (op == U6OP.ENDANSWERS || op == U6OP.ENDIF || op == U6OP.ASK) {
                        sb.append("\n");
                    }
                }

                if (op == U6OP.ENDIF) {
                    printing = false;
                }
            } else if (bb.get(bb.position() - 1) == U6OP.IF.code()) {
                sb.append(String.format("[%02x]", bb.get(bb.position())));
            } else {
                if (printing) {
                    boolean ascii = CharUtils.isAsciiPrintable((char) bb.get(bb.position()));
                    sb.append(ascii ? (char) bb.get(bb.position()) : String.format("[%02x]", bb.get(bb.position())));
                    //sb.append(String.format("[%02x]", bb.get(bb.position())));
                }
            }
            bb.get();
        }
        return sb.toString();
    }

    private static void debugOutput(ByteBuffer bb) {
        bb.rewind();
        while (bb.position() < bb.limit()) {
            U6OP op = U6OP.get(bb);
            if (op != null) {

                bb.get();

                if (op == U6OP.IF || op == U6OP.ASK || op == U6OP.DECL || op == U6OP.KEYWORDS || op == U6OP.ASKC || op == U6OP.ENDANSWERS) {
                    System.out.println("" + bb.position());
                }

                if (op == U6OP.JUMP) {
                    System.out.printf("[%s to %d]", op, bb.getInt());
                } else {
                    System.out.printf("[%s]", op);
                }

                if (op == U6OP.ONE_BYTE) {
                    System.out.printf("[%02x]", bb.get());
                }

                if (op == U6OP.ENDANSWERS || op == U6OP.ENDIF || op == U6OP.ASK) {
                    System.out.println("");
                }

            } else {
                boolean ascii = CharUtils.isAsciiPrintable((char) bb.get(bb.position()));
                System.out.print(ascii ? (char) bb.get(bb.position()) : String.format("[%02x]", bb.get(bb.position())));
                bb.get();
            }
        }

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
    public static Object[][] declares() throws Exception {
        FileInputStream is = new FileInputStream("target/classes/data/TILEFLAG");
        LittleEndianDataInputStream dis = new LittleEndianDataInputStream(is);

        byte[] f1 = new byte[2048];
        byte[] f2 = new byte[2048];
        byte[] none = new byte[1024];
        byte[] f3 = new byte[2048];

        dis.read(f1);
        dis.read(f2);
        dis.read(Ultima6.OBJ_WEIGHTS);
        dis.read(none);
        dis.read(f3);

        return new Object[][]{
            {"[DECL][05][VAR][ASSIGN][ONE_BYTE][01][CANCARRY][EVAL]", 5, 0}, /*
            {"[DECL][02][VAR][ASSIGN][ONE_BYTE][01][17][VAR][ONE_BYTE][01][ADD][RAND][EVAL]", 2, 4},
            {"[DECL][01][VAR][ASSIGN][ONE_BYTE][EB][00][FLAG][EVAL]", 1, 0},
            {"[DECL][02][VAR][ASSIGN][01][VAR][CANCARRY][ONE_BYTE][59][ONE_BYTE][01][WEIGHT][DIV][EVAL]", 2, 36},
            {"[DECL][00][VAR][ASSIGN][FOUR_BYTE][9F][0D][00][00][ONE_BYTE][23][INDEXOF][EVAL]", 0, 1},
            {"[DECL][07][VAR][ASSIGN][FOUR_BYTE][B9][12][00][00][06][VAR][DATA][ONE_BYTE][02][DIV][EVAL]", 7, 0},
            {"[DECL][00][SVAR][ASSIGN][22][SVAR][EVAL]", 0, "34"},
            {"[DECL][00][VAR][ASSIGN][18][VAR][ONE_BYTE][01][ADD][ONE_BYTE][08][MUL][EVAL]", 0, 200},
            {"[DECL][00][VAR][ASSIGN][01][VAR][ONE_BYTE][0A][MUL][EVAL]", 0, 10},
            {"[DECL][10][VAR][ASSIGN][ONE_BYTE][06][EVAL][00]", 16, 6},
            {"[DECL][09][VAR][ASSIGN][TWO_BYTE][A6][01][ONE_BYTE][01][WEIGHT][EVAL]", 9, 120},
            {"[DECL][FOUR_BYTE][06][13][00][00][02][VAR][DATA][ASSIGN][00][VAR][EVAL]", 0, 0},
            {"[DECL][08][VAR][ASSIGN][05][VAR][ONE_BYTE][01][SUB][EVAL]", 8, 4},
            {"[DECL][08][VAR][ASSIGN][02][VAR][17][VAR][ONE_BYTE][03][ADD][DIV][EVAL]", 8, 13}, //
         */};
    }

    @Test(dataProvider = "declares")
    public void testDeclare(String decl, Integer varName, Object expectedValue) throws Exception {

        Party party = new Party();

        Player player = new Player(0, "avatar");
        party.add(player);
        player.setStrength(18);
        player.addItem(Objects.Object.KEY, 1, 0);
        player.addItem(Objects.Object.GOLD_COIN, 100, 0);

        Player player2 = new Player(2, "npc");
        party.add(player2);
        player2.setStrength(18);
        player2.addItem(Objects.Object.GOLD_COIN, 100, 0);

        ByteBuffer bb = parse(decl);
        bb.get();//decl

        Conversation conv = new Conversation(1, "test");

        Conversations.declare(party, conv, bb, OUTPUT);

        assertEquals(conv.getVar(varName, expectedValue instanceof String), expectedValue);

    }

    @DataProvider
    public static Object[][] conditions() {
        return new Object[][]{
            /*
            {"[IF][01][VAR][17][VAR][LE][EVAL][JUMP][85][07][00][00][ENDIF]"},
            {"[IF][ONE_BYTE][EB][00][FLAG][ONE_BYTE][EB][01][FLAG][ONE_BYTE][00][EQ][LAND][EVAL][ENDIF]"},
            {"[IF][ONE_BYTE][EB][00][FLAG][EVAL][ENDIF]"},
            {"[IF][07][VAR][EVAL][ENDIF]"},
            {"[IF][05][VAR][ONE_BYTE][0B][GT][03][VAR][CANCARRY][LAND][FOUR_BYTE][81][0A][00][00][00][VAR][DATA][ONE_BYTE][01][WEIGHT][GE][EVAL][NEW][03][VAR][EVAL][FOUR_BYTE][81][0A][00][00][00][VAR][DATA][EVAL][ONE_BYTE][00][EVAL][ONE_BYTE][01][EVAL][22][ELSE][22][ENDIF]"},
            {"[IF][17][VAR][EVAL][73][ENDIF]"},
            {"[IF][ONE_BYTE][EB][00][FLAG][ONE_BYTE][00][EQ][EVAL][72][65][2E][22][SETF][ONE_BYTE][EB][EVAL][00][EVAL][ELSE][2E][22][ENDIF]"},
             */
            {"[IF][05][VAR][ONE_BYTE][01][CANCARRY][GT][EVAL][DECL][05][VAR][ASSIGN][ONE_BYTE][01][CANCARRY][EVAL][ENDIF]"}, /*
            {"[IF][03][VAR][CANCARRY][ONE_BYTE][58][FOUR_BYTE][5D][07][00][00][09][SVAR][DATA][WEIGHT][ADD][ONE_BYTE][95][ONE_BYTE][01][WEIGHT][LT][EVAL][ENDIF]"},
            {"[IF][00][VAR][03][VAR][FLAG][EVAL][CLEARF][00][VAR][EVAL][03][VAR][EVAL][ELSE][SETF][00][VAR][EVAL][03][VAR][EVAL][ENDIF]"},
            {"[IF][ONE_BYTE][eb][00][FLAG][EVAL][JUMP][64][02][00][00][ENDIF]"},
            {"[IF][ONE_BYTE][04][NPCINPARTY][EVAL][22][49][6f][6c][6f][21][ELSE][JUMP][07][02][00][00][ENDIF]"},
            {"[IF][FOUR_BYTE][c1][12][00][00][00][VAR][DATA][EVAL][JUMP][41][0d][00][00][ENDIF]"},
         */};
    }

    @Test(dataProvider = "conditions")
    public void testConditions(String text) throws Exception {

        Party party = new Party();

        Player player = new Player(0, "avatar");
        party.add(player);
        player.setStrength(18);
        player.addItem(Objects.Object.KEY, 1, 0);
        player.addItem(Objects.Object.GOLD_COIN, 100, 0);

        Player player2 = new Player(2, "npc");
        party.add(player2);
        player2.setStrength(18);
        player2.addItem(Objects.Object.GOLD_COIN, 100, 0);

        ByteBuffer bb = parse(text);
        bb.get();//if

        Conversation conv = new Conversation(1, "test");

        conv.setVar(35, "test", true);
        conv.setVar(25, "test", true);

        Conversations.condition(party, conv, bb, OUTPUT);

    }

    @Test
    public void testMatcher() {

        Map<Integer, String> vars = new HashMap<>();
        for (int i = 0; i < 64; i++) {
            vars.put(i, "test-" + i);
        }

        Pattern p = Pattern.compile("\\$[0-9]+");
        Matcher m = p.matcher("\"Enjoy your $1.\"");

        StringBuffer sb = new StringBuffer();
        while (m.find()) {
            String matchedText = m.group();
            int num = Integer.parseInt(matchedText.substring(1));
            String var = vars.get(num);
            m.appendReplacement(sb, "" + var);
        }
        m.appendTail(sb);
        System.out.println(sb.toString());
        assertEquals(sb.toString(), "some text test-5 was here after test-63.");
    }

}
