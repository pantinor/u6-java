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
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import org.apache.commons.lang3.CharUtils;

public class Conversations {

    public static final int SELF = 235;
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
                this.sVars.put(i, "");
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
                        cond.evaluate(player, iVars, sVars, output);
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

        private void debugOutput() {
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

    public static class Conditional {

        private final byte[] ifBlock;
        private final byte[] elseBlock;

        public Conditional(ByteTokenizer t) {

            if (t.countTokens() == 0 || t.countTokens() > 2) {
                throw new IllegalArgumentException("Must have if with optional else block. tokens: " + t.countTokens());
            }

            ifBlock = t.nexToken();

            if (t.hasMoreTokens()) {
                elseBlock = t.nexToken();
            } else {
                elseBlock = null;
            }
        }

        public boolean evaluate(Player player, Map<Integer, Integer> iVars, Map<Integer, String> sVars, OutputStream output) {

            ByteBuffer bb = ByteBuffer.wrap(ifBlock);
            bb.order(ByteOrder.LITTLE_ENDIAN);

            Stack<Object> values = new Stack<>();
            Stack<OpWrapper> operations = new Stack<>();
            while (bb.position() < bb.limit()) {
                if (bb.get(bb.position()) == U6OP.EVAL.code()) {
                    bb.get();
                    break;
                }
                if (bb.get(bb.position()) == U6OP.FOUR_BYTE.code()) {
                    bb.get();
                    values.push(bb.getInt());
                } else if (bb.get(bb.position()) == U6OP.ONE_BYTE.code()) {
                    bb.get();
                    int v = bb.get() & 0xff;
                    values.push(v);
                    if (v == SELF && bb.get(bb.position()) == 0) {
                        values.push(0);//flag 0
                    }
                } else if (bb.get(bb.position()) == U6OP.TWO_BYTE.code()) {
                    bb.get();
                    values.push(bb.getShort() & 0xff);
                } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.VAR.code()) {
                    int ind = bb.get() & 0xff;
                    values.push(iVars.get(ind));
                    bb.get();
                } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.SVAR.code()) {
                    int ind = bb.get() & 0xff;
                    values.push(sVars.get(ind));
                    bb.get();
                } else {
                    U6OP op = U6OP.get(bb);
                    if (op != null) {
                        OpWrapper wrapper = new OpWrapper();
                        wrapper.op = op;
                        for (int i = 0; i < op.argCount(); i++) {
                            if (values.size() > 0) {
                                wrapper.args.add(values.pop());
                            } else if (operations.size() > 0) {
                                wrapper.args.add(operations.pop());
                            }
                        }
                        operations.push(wrapper);
                        bb.get();
                    } else {
                        System.err.printf("unknown byte in declare [%s]\n", String.format("[%02X]", bb.get(bb.position())));
                        bb.get();
                    }
                }
            }

            Object finalEval = null;
            if (operations.size() > 0) {
                OpWrapper wrapper = operations.pop();
                finalEval = eval(player, wrapper.op, wrapper.args);
            } else {
                finalEval = (Integer) values.pop() > 0 ? 1 : 0;
            }

            return (Integer) finalEval > 0;
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

        List<Object> values = new ArrayList<>();
        List<U6OP> operations = new ArrayList<>();
        while (bb.position() < bb.limit()) {
            if (bb.get(bb.position()) == U6OP.EVAL.code()) {
                bb.get();
                break;
            }
            if (bb.get(bb.position()) == U6OP.FOUR_BYTE.code()) {
                bb.get();
                values.add(bb.getInt());
            } else if (bb.get(bb.position()) == U6OP.ONE_BYTE.code()) {
                bb.get();
                int v = bb.get() & 0xff;
                values.add(v);
                if (v == SELF && bb.get(bb.position()) == 0) {
                    values.add(0);//flag 0
                }
            } else if (bb.get(bb.position()) == U6OP.TWO_BYTE.code()) {
                bb.get();
                values.add(bb.getShort() & 0xff);
            } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.VAR.code()) {
                int ind = bb.get() & 0xff;
                values.add(iVars.get(ind));
                bb.get();
            } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.SVAR.code()) {
                int ind = bb.get() & 0xff;
                values.add(sVars.get(ind));
                bb.get();
            } else {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    operations.add(op);
                    bb.get();
                } else {
                    bb.get();//ignore
                }
            }
        }

        /**
         * A single argument may be more than one value, mixed with special
         * value (or stack) opcodes. These extended argument lists must be
         * resolved down to one value per argument, before executing the command
         * opcode. This can be done by arranging all data and opcodes (for one
         * argument) onto a stack, executing each opcode with the preceding
         * value as input (removing the values & code from the stack), and
         * pushing the results back onto the stack. The resultant data may
         * become input for another opcode, so it is important that operations
         * are executed from the "inner-most" values - or left to right. The
         * argument is resolved when only one value remains on the stack. Repeat
         * for each.
         */
        while (operations.size() > 0) {
            U6OP op = operations.remove(0);
            Object result = eval(player, op, values);
            values.add(0, result);
        }

        Object finalValue = values.remove(0);
        if (stringType) {
            sVars.put(var_name, "" + finalValue);
        } else {
            iVars.put(var_name, (Integer) finalValue);
        }

    }

    private static Object eval(Player player, U6OP op, List<Object> values) {
        Object result = null;
        
        Stack<Object> args = new Stack<>();
        for (Object v : values) {
            if (v instanceof OpWrapper) {
                OpWrapper wrapper = (OpWrapper) v;
                args.push(eval(player, wrapper.op, wrapper.args));
            } else {
                args.push(v);
            }
        }

        if (op == U6OP.EQ) {
            Object v1 = args.pop();
            Object v2 = args.pop();
            result = Objects.equals(v1, v2) ? 1 : 0;
        } else if (op == U6OP.NE) {
            Object v1 = args.pop();
            Object v2 = args.pop();
            result = !Objects.equals(v1, v2) ? 1 : 0;
        } else if (op == U6OP.GT) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 > v2 ? 1 : 0;
        } else if (op == U6OP.LT) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 < v2 ? 1 : 0;
        } else if (op == U6OP.GE) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 >= v2 ? 1 : 0;
        } else if (op == U6OP.LE) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 <= v2 ? 1 : 0;
        } else if (op == U6OP.LAND) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = (v1 > 0 && v2 > 0) ? 1 : 0;
        } else if (op == U6OP.LOR) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = (v1 > 0 || v2 > 0) ? 1 : 0;
        } else if (op == U6OP.RAND) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = randomBetween(v1, v2);
        } else if (op == U6OP.ADD) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 + v2;
        } else if (op == U6OP.SUB) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 - v2;
        } else if (op == U6OP.MUL) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 * v2;
        } else if (op == U6OP.DIV) {
            Integer v1 = (Integer) args.pop();
            Integer v2 = (Integer) args.pop();
            result = v1 / v2;
        } else if (op == U6OP.FLAG) {
            Integer npc = (Integer) args.pop();
            Integer flag = (Integer) args.pop();
            result = player.getFlag(flag) ? 1 : 0;
        } else if (op == U6OP.NPC) {
            Integer partyIdx = (Integer) args.pop();
            Integer unused = (Integer) args.pop();
            result = 0;//todo
            //The NPC number of party member val1.
        } else if (op == U6OP.CANCARRY) {
            Integer npc = (Integer) args.pop();
            result = 100;//todo
            //weight that npc can carry
        } else if (op == U6OP.OBJINPARTY) {
            Integer obj = (Integer) args.pop();
            Integer quality = (Integer) args.pop();
            result = 0xFFFF;//todo
            //0xFFFF if object val1 with quality val2 is in party inventory, 0x8001 if not ??
        } else if (op == U6OP.OBJCOUNT) {
            Integer npc = (Integer) args.pop();
            Integer obj = (Integer) args.pop();
            result = 1;//todo
            //The total quantity of objects of type val2 in the inventory of NPC val1.
        } else if (op == U6OP.STR) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 10;//todo
            //The sum of NPC val1 Strength plus val2
        } else if (op == U6OP.INT) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 10;
            //The sum of NPC val1 Intelli plus val2
        } else if (op == U6OP.DEX) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 10;//todo
            //The sum of NPC val1 Dexterity plus val2
        } else if (op == U6OP.LVL) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 1;//todo
            //The sum of NPC val1 level plus val2
        } else if (op == U6OP.EXP) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 1000;//todo
            //The sum of NPC val1 experience plus val2
        } else if (op == U6OP.OBJINACTOR) {
            Integer npc = (Integer) args.pop();
            Integer obj = (Integer) args.pop();
            result = 1;
            //is object val2 in npc val1
        } else if (op == U6OP.WEIGHT) {
            Integer obj = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = 10;//todo
            //Weight of object val1, of quantity val2.
        } else if (op == U6OP.JOIN) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //0 if the npc val1 is able and did join the party, 
            //1 if the party is not on land, 2 if the party is too large, 
            //3 if npc is already in the party
        } else if (op == U6OP.LEAVE) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //0 if the npc val1 left the party, 1 if the party is not on land, 2 if npc is not in the party
        } else if (op == U6OP.NPCNEARBY) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 is in proximity to self, 0 if not.
        } else if (op == U6OP.WOUNDED) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 is wounded, 0 if current HP equals maximum HP.
        } else if (op == U6OP.POISONED) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 "poisoned" flag is true, 0 if it is false.
        } else if (op == U6OP.NPCINPARTY) {
            Integer npc = (Integer) args.pop();
            result = 1;//todo
            //1 if NPC val1 is in the Avatar's party, 0 if not.
        } else if (op == U6OP.HORSED) {
            Integer npc = (Integer) args.pop();
            result = 0;//todo
            //1 if npc val1 is riding a horse, 0 if on foot.
        } else if (op == U6OP.DATA) {
            Integer db = (Integer) args.pop();
            Object idx = args.pop();
            result = 1;//todo
            //Data (string or integer) from the DB section at val1, index val2.
        } else if (op == U6OP.INDEXOF) {
            Integer db = (Integer) args.pop();
            Integer idx = (Integer) args.pop();
            result = 1;//todo
            //dereference to a database name
        } else {
            throw new IllegalArgumentException(String.format("unknown op in declare [%s]", op));
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

    public static class OpWrapper {

        private final List<Object> args = new ArrayList<>();
        U6OP op;
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

    private static int randomBetween(int v1, int v2) {
        if (v2 > v1) {
            return RAND.nextInt(v2 - v1) + v1;
        } else {
            //due to bug in u6 conversations for RAND op, they have put the low number as val2 at least once I saw
            return RAND.nextInt(v1 - v2) + v2;
        }
    }

}
