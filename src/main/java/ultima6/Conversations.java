package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.commons.lang3.CharUtils;

public class Conversations {

    public static final byte SELF = (byte) 0xeb;
    public static final Random RAND = new Random();

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
        private final String description;
        private TextureRegion portait;
        private final Map<Integer, Integer> iVars = new HashMap<>();
        private final Map<Integer, String> sVars = new HashMap<>();

        public Conversation(int id, String name, byte[] data) {
            this.id = id;
            this.name = name;
            this.data = data;
            this.bb = ByteBuffer.wrap(data);
            this.bb.order(ByteOrder.LITTLE_ENDIAN);

            seek(bb, U6OP.SLOOK);
            this.description = consumeText(bb);

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
            for (int i = 0; i < 64; i++) {
                this.iVars.put(i, 0);
            }
            for (int i = 0; i < 64; i++) {
                this.sVars.put(i, null);
            }
        }

        public void process(Player player, String input, OutputStream output) {

            output.print(input, Color.RED);

            Boolean matchesKeywords = null;

            while (bb.position() < bb.limit()) {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    bb.get();
                    if (op == U6OP.DECL) {
                        declare(player, iVars, sVars, this.bb, output);
                    }
                    if (op == U6OP.SETF) {
                        setFlag(player, this.bb);
                    }
                    if (op == U6OP.IF) {
                        int start = bb.position() - 1;
                        seek(bb, U6OP.ENDIF);
                        int end = bb.position();
                        ByteTokenizer tok = new ByteTokenizer(bb, start, end - start + 1,
                                new byte[]{U6OP.IF.code(), U6OP.ELSE.code(), U6OP.ENDIF.code()},
                                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code()}
                        );
                        Conditional cond = new Conditional(tok);
                        cond.evaluate(player, bb, iVars, sVars, output);
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
                    if (op == U6OP.PORTRAIT) {
                        bb.get();
                        int npc = bb.get() & 0xff;
                        output.setPortrait(npc == 0xeb ? this.id : npc);
                        bb.get();//eval
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

        public ByteBuffer data() {
            return this.bb;
        }

    }

    public static void declare(Player player, Map<Integer, Integer> iVars, Map<Integer, String> sVars, ByteBuffer bb, OutputStream output) {
        
        int var_name = 0;
        int four_bytes = 0;
        
        if (bb.get(bb.position()) == U6OP.FOUR_BYTE.code()) {
            bb.get();
            four_bytes = bb.getInt();
            return;//todo
        } else {
            var_name = bb.get() & 0xff;
        }

        if (bb.get(bb.position()) == U6OP.VAR.code()) {
            bb.get();
        }

        boolean stringType = false;
        if (bb.get(bb.position()) == U6OP.SVAR.code()) {
            bb.get();
            stringType = true;
        }

        boolean db = false;
        if (bb.get(bb.position()) == U6OP.DATA.code()) {
            bb.get();
            db = true;
        }

        if (bb.get(bb.position()) == U6OP.ASSIGN.code()) {
            bb.get();
        }

        Stack<Object> values = new Stack<>();
        Stack<U6OP> operations = new Stack<>();
        while (bb.position() < bb.limit()) {
            if (bb.get(bb.position()) == U6OP.EVAL.code()) {
                bb.get();
                break;
            }
            Object value = null;
            if (bb.get(bb.position()) == U6OP.FOUR_BYTE.code()) {
                bb.get();
                value = bb.getInt();
            } else if (bb.get(bb.position()) == U6OP.ONE_BYTE.code()) {
                bb.get();
                value = bb.get() & 0xff;
            } else if (bb.get(bb.position()) == U6OP.TWO_BYTE.code()) {
                bb.get();
                value = bb.getShort() & 0xff;
            } else if (bb.get(bb.position() + 1) == U6OP.VAR.code() || bb.get(bb.position() + 1) == U6OP.SVAR.code()) {
                int ind = bb.get() & 0xff;
                value = iVars.get(ind);
                bb.get(); //VAR or SVAR
            } else {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    operations.add(op);
                    bb.get();
                } else {
                    System.err.printf("unknown byte in declare [%s]\n", String.format("[%02X]", bb.get(bb.position())));
                    bb.get();
                }
            }
            if (value != null) {
                values.add(value);
            }
        }

        Object result = null;
        while (values.size() > 0) {
            if (values.size() >= 2) {
                Object value2 = values.pop();
                Object value1 = values.pop();
                result = assign(player, operations.pop(), value1, value2, result);
                if (result != null && values.isEmpty()) {
                    if (stringType) {
                        sVars.put(var_name, "" + result);
                    } else {
                        iVars.put(var_name, (Integer) result);
                    }
                } else {

                }
            } else {
                Object value1 = values.pop();
                if (stringType) {
                    sVars.put(var_name, "" + value1);
                } else {
                    iVars.put(var_name, (Integer) value1);
                }
            }
        }

    }

    private static Object assign(Player player, U6OP op, Object value1, Object value2, Object result) {
        if (op == U6OP.RAND) {
            result = randomBetween((Integer) value1, (Integer) value2);
        } else if (op == U6OP.ADD) {
            result = (Integer) value1 + (Integer) value2;
        } else if (op == U6OP.SUB) {
            result = (Integer) value1 - (Integer) value2;
        } else if (op == U6OP.MUL) {
            result = (Integer) value1 * (Integer) value2;
        } else if (op == U6OP.DIV) {
            result = (Integer) value1 / (Integer) value2;
        } else if (op == U6OP.FLAG) {
            result = player.getFlag((Integer) value2) ? 1 : 0; //value1 would be the npc id - ignored
        } else if (op == U6OP.NPC) {
        } else if (op == U6OP.CANCARRY) {
        } else if (op == U6OP.OBJINPARTY) {
        } else if (op == U6OP.OBJCOUNT) {
        } else if (op == U6OP.STR) {
        } else if (op == U6OP.INT) {
        } else if (op == U6OP.DEX) {
        } else if (op == U6OP.LVL) {
        } else if (op == U6OP.EXP) {
        } else if (op == U6OP.OBJINACTOR) {
        } else if (op == U6OP.INDEXOF) {
        } else if (op == U6OP.WEIGHT) {
        } else if (op == U6OP.JOIN) {
        } else if (op == U6OP.LEAVE) {
        } else if (op == U6OP.NPCNEARBY) {
        } else if (op == U6OP.WOUNDED) {
        } else if (op == U6OP.POISONED) {
        } else if (op == U6OP.INPARTY) {
        } else if (op == U6OP.HORSED) {
        } else if (op == U6OP.DATA) {
        } else {
            System.err.printf("unknown op in declare [%s]\n", op);
        }
        return result;
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

    public static void seek(ByteBuffer bb, U6OP op) {
        while (bb.position() < bb.limit()) {
            U6OP tmp = U6OP.get(bb);
            if (op == tmp) {
                break;
            }
            bb.get();
        }
    }

    public interface OutputStream {

        public void print(String string, Color color);

        public void close();

        public void setPortrait(int npc);

    }

    private static String consumeText(ByteBuffer bb) {
        U6OP op = U6OP.get(bb);
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
