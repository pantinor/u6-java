package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.StringTokenizer;
import org.apache.commons.lang3.CharUtils;

public class Conversations {

    public static final byte SELF = (byte) 0xeb;
    public static final Random RAND = new Random();

    public static enum U6OP {
        GT((byte) 0x81),
        GE((byte) 0x82),
        LT((byte) 0x83),
        LE((byte) 0x84),
        NE((byte) 0x85),
        EQ((byte) 0x86),
        ADD((byte) 0x90),
        SUB((byte) 0x91),
        MUL((byte) 0x92),
        LOR((byte) 0x94),
        LAND((byte) 0x95),
        CANCARRY((byte) 0x9a),
        WEIGHT((byte) 0x9b),
        HORSED((byte) 0x9d),
        RAND((byte) 0xa0),
        EVAL((byte) 0xa7),
        FLAG((byte) 0xab),
        VAR((byte) 0xb2),
        SVAR((byte) 0xb3),
        DATA((byte) 0xb4),
        OBJCOUNT((byte) 0xbb),
        INPARTY((byte) 0xc6),
        OBJINPARTY((byte) 0xc7),
        JOIN((byte) 0xca),
        LEAVE((byte) 0xcc),
        ONE_BYTE((byte) 0xd3),
        TWO_BYTE((byte) 0xd4),
        FOUR_BYTE((byte) 0xd2),
        NPCNEARBY((byte) 0xd7),
        WOUNDED((byte) 0xda),
        POISONED((byte) 0xdc),
        NPC((byte) 0xdd),
        EXP((byte) 0xe0),
        LVL((byte) 0xe1),
        STR((byte) 0xe2),
        INT((byte) 0xe3),
        DEX((byte) 0xe4),
        HORSE((byte) 0x9c),
        IF((byte) 0xa1),
        ENDIF((byte) 0xa2),
        ELSE((byte) 0xa3),
        SETF((byte) 0xa4),
        CLEARF((byte) 0xa5),
        DECL((byte) 0xa6),
        ASSIGN((byte) 0xa8),
        JUMP((byte) 0xb0),
        DPRINT((byte) 0xb5),
        BYE((byte) 0xb6),
        NEW((byte) 0xb9),
        DELETE((byte) 0xba),
        INVENTORY((byte) 0xbe),
        PORTRAIT((byte) 0xbf),
        ADDKARMA((byte) 0xc4),
        SUBKARMA((byte) 0xc5),
        GIVE((byte) 0xc9),
        WAIT((byte) 0xcb),
        WORKTYPE((byte) 0xcd),
        SETNAME((byte) 0xd8),
        HEAL((byte) 0xd9),
        CURE((byte) 0xdb),
        ENDANSWERS((byte) 0xee),
        KEYWORDS((byte) 0xef),
        SLOOK((byte) 0xf1),
        SCONVERSE((byte) 0xf2),
        SPREFIX((byte) 0xf3),
        ANSWER((byte) 0xf6),
        ASK((byte) 0xf7),
        ASKC((byte) 0xf8),
        INPUT((byte) 0xfb),
        INPUTNUM((byte) 0xfc),
        SIDENT((byte) 0xff),
        SLEEP((byte) 0x9e),
        OBJINACTOR((byte) 0x9F),
        RESURRECT((byte) 0xD6),
        INPUTSTR((byte) 0xF9),
        DIV((byte) 0x93),
        INDEXOF((byte) 0xB7),
        ENDOFLIST((byte) 0xB8);

        private byte code;

        private U6OP(byte code) {
            this.code = code;
        }

        public byte code() {
            return code;
        }

        public static U6OP find(byte b) {
            for (U6OP op : U6OP.values()) {
                if (op.code == b) {
                    return op;
                }
            }
            return null;
        }

    }

    private final Map<Integer, Conversation> conversations = new HashMap<>();

    public Conversation get(int id) {
        return this.conversations.get(id);
    }

    public Conversation put(int id, String name, byte[] data) {
        Conversation c = new Conversation(id, name, data);
        this.conversations.put(id, c);
        return c;
    }

    public Iterator<Conversation> iter() {
        return this.conversations.values().iterator();
    }

    public static class Conversation {

        private final int id;
        private final String name;
        private final byte[] data;
        private final ByteBuffer bb;
        private String description;
        private TextureRegion portait;
        private final Map<Integer, Integer> variables = new HashMap<>();

        public Conversation(int id, String name, byte[] data) {
            this.id = id;
            this.name = name;
            this.data = data;
            this.bb = ByteBuffer.wrap(data);
            this.bb.order(ByteOrder.LITTLE_ENDIAN);

            reset();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getDescription() {
            return description;
        }

        public TextureRegion getPortait() {
            return portait;
        }

        public void setPortait(TextureRegion portait) {
            this.portait = portait;
        }

        public final void reset() {
            this.bb.rewind();
            while (this.bb.position() < this.bb.limit()) {
                U6OP op = U6OP.find(this.bb.get());
                if (op == U6OP.SCONVERSE) {
                    break;
                }
            }
            for (int i = 0; i < 10; i++) {
                this.variables.put(i, 0);
            }
        }

        public void process(Player player, String input, OutputStream output) {

            output.print(input, Color.RED);

            Boolean matchesKeywords = null;

            while (bb.position() < bb.limit()) {
                U6OP op = U6OP.find(bb.get(bb.position()));
                if (op != null) {
                    bb.get();
                    if (op == U6OP.DECL) {
                        declare(variables, this.bb, output);
                    }
                    if (op == U6OP.SETF) {
                        setFlag(player, this.bb);
                    }
                    if (op == U6OP.IF) {
                        condition(variables, player, this.bb, output);
                    }
                    if (op == U6OP.ASK) {
                        matchesKeywords = null;
                    }
                    if (op == U6OP.ENDANSWERS) {
                        matchesKeywords = null;
                        break;
                    }
                    if (op == U6OP.KEYWORDS) {
                        String keywords = consumeText(this.bb);
                        if (inputMatches(input, keywords)) {
                            matchesKeywords = true;
                        } else {
                            matchesKeywords = false;
                        }
                    }
                    if (op == U6OP.ANSWER) {
                        String answer = consumeText(this.bb);
                        if (answer.length() < 1) {
                            //ignore
                        } else if (matchesKeywords) {
                            output.print(answer, null);
                        }
                    }
                    if (op == U6OP.WAIT) {
                        if (matchesKeywords == null) {
                            output.print("...", Color.YELLOW);
                            break;
                        } else if (matchesKeywords) {
                            output.print("...", Color.YELLOW);
                            break;
                        } else {
                            //ignore
                        }
                    }
                    if (op == U6OP.JUMP) {
                        int jump = bb.getInt();
                        if (matchesKeywords == null) {
                            bb.position(jump);
                            //output.print("jumped to " + jump, Color.PINK);
                        } else if (matchesKeywords) {
                            bb.position(jump);
                            //output.print("jumped to " + jump, Color.PINK);
                            break;
                        } else {
                            //ignore
                        }
                        matchesKeywords = null;
                    }
                    if (op == U6OP.NEW) {
                        newObject(player, this.bb);
                    }
                    if (op == U6OP.BYE) {
                        reset();
                        output.close();
                        break;
                    }
                } else if (bb.get(bb.position()) == (byte) 0x0A) {
                    bb.get();//skip
                } else {
                    String text = consumeText(this.bb);
                    if (text.length() < 1) {
                        //ignore
                    } else if (matchesKeywords == null) {
                        output.print(text, null);
                        break;
                    } else if (matchesKeywords) {
                        output.print(text, null);
                        break;
                    } else {
                        //ignore
                    }
                }
            }

        }

        public void debugOutput() {
            bb.rewind();
            while (bb.position() < bb.limit()) {
                byte b = bb.get();
                U6OP op = U6OP.find(b);
                if (op != null) {
                    if (op == U6OP.IF || op == U6OP.ASK || op == U6OP.DECL) {
                        System.out.println("");
                    }
                    if (op == U6OP.JUMP) {
                        System.out.printf("\n%d:%s[%s] --> [%d]", bb.position(), op, String.format("%02X", b), bb.getInt());
                    } else {
                        System.out.printf("\n%d:%s[%s]", bb.position(), op, String.format("%02X", b));
                    }

                } else {
                    boolean ascii = CharUtils.isAsciiPrintable((char) b);
                    System.out.print(ascii ? (char) b : String.format("[%02X]", b));
                    //System.out.print(String.format("%02X", b));
                }
            }

        }

    }

    private static void declare(Map<Integer, Integer> variables, ByteBuffer bb, OutputStream output) {

        int var_name = bb.get() & 0xff;
        byte var_type = bb.get();

        bb.get();//assign

        int val1 = 0;
        if (bb.get(bb.position()) == U6OP.ONE_BYTE.code()) {
            bb.get();
            val1 = bb.get() & 0xff;
        } else {
            val1 = variables.get(bb.get() & 0xff);
        }

        Integer val2 = null;
        if (bb.get(bb.position()) == U6OP.ONE_BYTE.code()) {
            bb.get();
            val2 = bb.get() & 0xff;
        }

        Integer rand = null;
        if (bb.get(bb.position()) == U6OP.RAND.code()) {
            bb.get();
            rand = randomBetween(val1, val2);
        }

        if (bb.get(bb.position()) == U6OP.VAR.code()) {
            bb.get();
        }

        if (var_type == U6OP.VAR.code()) {
            if (rand != null) {
                //output.print("set VAR " + var_name + " to " + rand, Color.NAVY);
                variables.put(var_name, rand);
            } else {
                //output.print("set VAR " + var_name + " to " + val1, Color.BLUE);
                variables.put(var_name, val1);
            }
        } else {
            //output.print("set VAR " + var_name + " to " + val1, Color.BLUE);
            variables.put(var_name, val1);
        }

        bb.get();//eval

    }

    private static void setFlag(Player player, ByteBuffer bb) {
        bb.get();//one byte
        bb.get();//should be self
        bb.get();//eval
        byte b = bb.get();//one byte
        int flagIndex = b == U6OP.ONE_BYTE.code() ? bb.get() : b;
        bb.get();//eval
        player.setFlag(flagIndex);
    }

    private static void newObject(Player player, ByteBuffer bb) {
        bb.get();
        int npc = bb.get() & 0xff;
        bb.get();

        bb.get();
        int obj = bb.get() & 0xff;
        bb.get();

        bb.get();
        int quality = bb.get() & 0xff;
        bb.get();

        bb.get();
        int quantity = bb.get() & 0xff;
        bb.get();

        player.addItem(obj, quantity, quality);
    }

    private static void condition(Map<Integer, Integer> variables, Player player, ByteBuffer bb, OutputStream output) {

        byte b1 = bb.get(bb.position());
        byte b2 = bb.get(bb.position() + 1);
        byte b3 = bb.get(bb.position() + 2);
        byte b4 = bb.get(bb.position() + 3);
        byte b5 = bb.get(bb.position() + 4);

        if (b2 == SELF && b5 == U6OP.FLAG.code()) {
            bb.get();//one byte
            bb.get();//self
            bb.get();//one byte
            int flagIndex = bb.get();
            bb.get();//flag
            bb.get();//eval
            bb.get();//jump
            int jump = bb.getInt();
            if (player.getFlag(flagIndex)) {
                bb.position(jump);
            }
        }

        if (b2 == U6OP.VAR.code()) {
            Integer var = variables.get((int) bb.get());
            Integer evalVar = null;
            if (b3 == U6OP.ONE_BYTE.code()) {
                bb.get();//var
                bb.get();//one byte
                evalVar = (int) bb.get();
            }
            if (b4 == U6OP.VAR.code()) {
                bb.get();//var
                evalVar = variables.get((int) bb.get());
                bb.get();//var
            }
            U6OP comparator = U6OP.find(bb.get());

            boolean eval = false;
            if (comparator == U6OP.EQ) {
                eval = var == evalVar;
            }
            if (comparator == U6OP.NE) {
                eval = var != evalVar;
            }
            if (comparator == U6OP.GE) {
                eval = var >= evalVar;
            }
            if (comparator == U6OP.LE) {
                eval = var <= evalVar;
            }
            if (comparator == U6OP.GT) {
                eval = var > evalVar;
            }
            if (comparator == U6OP.LT) {
                eval = var < evalVar;
            }

            bb.get();//eval
            bb.get();//jump
            int jump = bb.getInt();
            if (eval) {
                bb.position(jump);
            }
        }

        if (b1 == U6OP.ONE_BYTE.code() && b3 == U6OP.ONE_BYTE.code() && b5 == U6OP.OBJINPARTY.code()) {
            bb.get();
            int obj = (int) bb.get() & 0xff;
            bb.get();
            int quality = (int) bb.get() & 0xff;
            bb.get();
            bb.get();//two_byte
            bb.getShort();//value 2 bytes
            U6OP comparator = U6OP.find(bb.get());
            boolean eval = player.hasItem(obj, 1, quality);
            bb.get();//eval
            bb.get();//jump
            int jump = bb.getInt();
            if (eval) {
                bb.position(jump);
            }
        }

        if (b1 == U6OP.ONE_BYTE.code() && b3 == U6OP.INPARTY.code()) {
            bb.get();
            int npc = (int) bb.get() & 0xff;
            bb.get();
            bb.get();//eval
            String msg = consumeText(bb);
            bb.get();//jump
            int jump = bb.getInt();
            if (player.getParty().isInParty(npc)) {
                output.print(msg, null);
                bb.position(jump);
            }
        }

    }

    public interface OutputStream {

        public void print(String string, Color color);

        public void close();

    }

    private static String consumeText(ByteBuffer bb) {
        U6OP op = U6OP.find(bb.get(bb.position()));
        if (op != null) {
            return "";
        }
        int start = bb.position();
        do {
            byte next = bb.get();
        } while (bb.position() < bb.limit() && U6OP.find(bb.get(bb.position())) == null);
        String val = new String(bb.array(), start, bb.position() - start);
        return prepareText(val);
    }

    private static boolean inputMatches(String input, String keywords) {
        if ("*".equals(keywords)) {
            return true;
        }
        StringTokenizer st = new StringTokenizer(keywords, ",");
        while (st.hasMoreTokens()) {
            String tok = st.nextToken().trim().toLowerCase();
            if (input.toLowerCase().contains(tok)) {
                return true;
            }
        }
        return false;
    }

    private static String prepareText(String s) {

        return s.replace("\\", "").replace("\"", "").replace("\'", "'").replace("\n", "").trim();
    }

    private static int randomBetween(int low, int high) {
        return RAND.nextInt(high - low) + low;
    }

    private static String toCamelCase(String s) {
        if (s == null) {
            return null;
        }

        s = s.replace("_", " ");

        final StringBuilder ret = new StringBuilder(s.length());

        for (String word : s.split(" ")) {
            if (!word.isEmpty()) {
                ret.append(word.substring(0, 1).toUpperCase());
                ret.append(word.substring(1).toLowerCase());
            }
        }

        return ret.toString();
    }
}
