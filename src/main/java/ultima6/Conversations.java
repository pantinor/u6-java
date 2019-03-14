package ultima6;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.CharUtils;

public class Conversations {

    public static final Random RAND = new Random();

    public static final int GLOBAL_VAR_SEX = 0x10;
    public static final int GLOBAL_VAR_KARMA = 0x14;
    public static final int GLOBAL_VAR_GARGF = 0x15;
    public static final int GLOBAL_VAR_NPC_NAME = 0x17;
    public static final int GLOBAL_VAR_PARTYLIVE = 0x17;
    public static final int GLOBAL_VAR_PARTYALL = 0x18;
    public static final int GLOBAL_VAR_HP = 0x19;
    public static final int GLOBAL_VAR_PLAYER_NAME = 0x19;
    public static final int GLOBAL_VAR_QUESTF = 0x1A;
    public static final int GLOBAL_VAR_WORKTYPE = 0x20;
    public static final int GLOBAL_VAR_YSTRING = 0x22;
    public static final int GLOBAL_VAR_INPUT = 0x23;

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
        private final BitSet flags = new BitSet();
        private final Map<Integer, Integer> iVars = new HashMap<>();
        private final Map<Integer, String> sVars = new HashMap<>();

        public Conversation(int id, String name) {
            this.id = id;
            this.name = name;
            this.data = null;
            this.bb = null;
            this.description = null;
            for (int i = 0; i < 64; i++) {
                this.iVars.put(i, i);
            }
            for (int i = 0; i < 64; i++) {
                this.sVars.put(i, "" + i);
            }
        }

        public Conversation(int id, String name, byte[] data) {

            this.id = id;
            this.name = name;
            this.data = data;

            this.bb = ByteBuffer.wrap(data);
            this.bb.order(ByteOrder.LITTLE_ENDIAN);

            seek(bb, U6OP.SLOOK);
            bb.get();
            this.description = consumeText(bb);
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

        public boolean getFlag(int idx) {
            return this.flags.get(idx);
        }

        public void setFlag(int idx) {
            this.flags.set(idx);
        }

        public void clearFlag(int idx) {
            this.flags.clear(idx);
        }

        public void init(Player player, Party party) {
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

            this.iVars.put(GLOBAL_VAR_SEX, 0);
            this.iVars.put(GLOBAL_VAR_KARMA, player.getKarma());
            this.iVars.put(GLOBAL_VAR_GARGF, 0);
            this.iVars.put(GLOBAL_VAR_PARTYLIVE, 1);
            this.iVars.put(GLOBAL_VAR_PARTYALL, 1);
            this.iVars.put(GLOBAL_VAR_HP, player.getHp());
            this.sVars.put(GLOBAL_VAR_NPC_NAME, this.name);
            this.sVars.put(GLOBAL_VAR_PLAYER_NAME, player.getName());
            this.iVars.put(GLOBAL_VAR_QUESTF, 0);
            this.iVars.put(GLOBAL_VAR_WORKTYPE, 0);
        }

        public Object getVar(Integer idx, boolean strVal) {
            if (strVal) {
                return this.sVars.get(idx);
            } else {
                return this.iVars.get(idx);
            }
        }

        public void process(Player avatar, Party party, String input, OutputStream output) {

            output.print(input, Color.RED);

            while (bb.position() < bb.limit()) {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    bb.get();
                    if (op == U6OP.DECL) {
                        declare(party, this, this.bb, output);
                    }
                    if (op == U6OP.SETF) {
                        Conversations.setFlag(this, party, this.bb, false);
                    }
                    if (op == U6OP.CLEARF) {
                        Conversations.setFlag(this, party, this.bb, true);
                    }
                    if (op == U6OP.IF) {
                        condition(party, this, bb, output);
                    }
                    if (op == U6OP.ASK) {
                        break;
                    }
                    if (op == U6OP.KEYWORDS) {
                        String keywords = consumeText(this, avatar, party, this.bb);
                        boolean matched = inputMatches(input, keywords);
                        //output.print(input + " matching " + keywords, matched ? Color.GREEN : Color.RED);
                        if (!matched) {
                            seek(bb, U6OP.KEYWORDS, U6OP.ENDANSWERS);
                        }
                    }
                    if (op == U6OP.ANSWER) {
                        String answer = consumeText(this, avatar, party, this.bb);
                        if (!answer.isEmpty()) {
                            output.print(answer, null);
                        }
                    }
                    if (op == U6OP.JUMP) {
                        int jump = bb.getInt();
                        bb.position(jump);
                        //output.print("Jumped to " + jump, Color.PINK);
                    }
                    if (op == U6OP.INPUTNUM || op == U6OP.INPUT) {
                        int ind = bb.get() & 0xff;
                        try {
                            iVars.put(ind, Integer.parseInt(input));
                        } catch (Exception e) {
                            sVars.put(ind, input);
                        }
                        bb.get();
                    }
                    if (op == U6OP.INPUTSTR) {
                        int ind = bb.get() & 0xff;
                        sVars.put(ind, input);
                        bb.get();
                    }
                    if (op == U6OP.NEW) {
                        objectMgmt(this, party, this.bb, false);
                    }
                    if (op == U6OP.DELETE) {
                        objectMgmt(this, party, this.bb, true);
                    }
                    if (op == U6OP.PORTRAIT) {
                        Stack<Object> values = new Stack<>();
                        marshall(bb, values, null, this);
                        int npc = (int) values.pop();
                        output.setPortrait(npc == 0xeb ? this.id : npc);
                    }
                    if (op == U6OP.BYE) {
                        output.close();
                        break;
                    }
                } else if (bb.get(bb.position()) == (byte) 0x0A) {
                    bb.get();//skip
                } else {
                    String text = consumeText(this, avatar, party, this.bb);
                    if (!text.isEmpty()) {
                        output.print(text, null);
                        break;
                    }
                }
            }

        }

        public ByteBuffer data() {
            return this.bb;
        }

    }

    public static void condition(Party party, Conversation conv, ByteBuffer bb, OutputStream output) {

        Stack<Object> values = new Stack<>();
        Stack<OpWrapper> operations = new Stack<>();

        marshall(bb, values, operations, conv);

        Object finalEval = null;
        if (operations.size() > 0) {
            OpWrapper wrapper = operations.pop();
            finalEval = eval(party, conv, wrapper.op, wrapper.args);
        } else {
            finalEval = (Integer) values.pop() > 0 ? 1 : 0;
        }

        if ((Integer) finalEval > 0) {
            //nothing
        } else {
            seek(bb, U6OP.ELSE, U6OP.ENDIF);
        }
    }

    public static void declare(Party party, Conversation conv, ByteBuffer bb, OutputStream output) {

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
        Stack<OpWrapper> operations = new Stack<>();

        marshall(bb, values, operations, conv);

        Object finalValue = null;
        if (operations.size() > 0) {
            OpWrapper wrapper = operations.pop();
            finalValue = eval(party, conv, wrapper.op, wrapper.args);
        } else {
            finalValue = values.pop();
        }

        if (stringType) {
            conv.sVars.put(var_name, "" + finalValue);
        } else {
            conv.iVars.put(var_name, (Integer) finalValue);
        }

    }

    private static void marshall(ByteBuffer bb, Stack<Object> values, Stack<OpWrapper> operations, Conversation conv) {
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
                if (bb.get(bb.position() + 1) == U6OP.FLAG.code()) {
                    values.push(bb.get() & 0xff);//npc
                }
            } else if (bb.get(bb.position()) == U6OP.TWO_BYTE.code()) {
                bb.get();
                values.push(bb.getShort() & 0xff);
            } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.VAR.code()) {
                int ind = bb.get() & 0xff;
                values.push(conv.iVars.get(ind));
                bb.get();
            } else if (bb.position() + 1 < bb.limit() && bb.get(bb.position() + 1) == U6OP.SVAR.code()) {
                int ind = bb.get() & 0xff;
                values.push(conv.sVars.get(ind));
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
                    System.err.printf("unknown byte in marshall [%s]\n", String.format("[%02X]", bb.get(bb.position())));
                    bb.get();
                }
            }
        }
    }

    private static Object eval(Party party, Conversation conv, U6OP op, List<Object> values) {
        Object result = null;

        Stack<Object> args = new Stack<>();
        for (Object v : values) {
            if (v instanceof OpWrapper) {
                OpWrapper wrapper = (OpWrapper) v;
                args.push(eval(party, conv, wrapper.op, wrapper.args));
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
            if (v2 > v1) {
                result = RAND.nextInt(v2 - v1) + v1;
            } else {
                //due to bug in u6 conversations for RAND op, they have put the low number as val2 at least once I saw
                result = RAND.nextInt(v1 - v2) + v2;
            }
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
            result = conv.getFlag(flag) ? 1 : 0;
        } else if (op == U6OP.NPC) {
            Integer idx = (Integer) args.pop();
            Integer unused = (Integer) args.pop();
            result = party.get(idx).getId();
        } else if (op == U6OP.CANCARRY) {
            Integer npc = (Integer) args.pop();
            result = party.getPlayer(npc).canCarryWeight();
        } else if (op == U6OP.OBJINPARTY) {
            Integer obj = (Integer) args.pop();
            Integer quality = (Integer) args.pop();
            result = 0xFFFF;//todo
            //0xFFFF if object val1 with quality val2 is in party inventory, 0x8001 if not ??
        } else if (op == U6OP.OBJCOUNT) {
            Integer npc = (Integer) args.pop();
            Integer obj = (Integer) args.pop();
            result = party.getPlayer(npc).quantity(obj);
        } else if (op == U6OP.STR) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = party.getPlayer(npc).getStrength() + amt;
        } else if (op == U6OP.INT) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = party.getPlayer(npc).getIntelligence() + amt;
        } else if (op == U6OP.DEX) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = party.getPlayer(npc).getDex() + amt;
        } else if (op == U6OP.LVL) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = party.getPlayer(npc).getLevel() + amt;
        } else if (op == U6OP.EXP) {
            Integer npc = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = party.getPlayer(npc).getExp() + amt;
        } else if (op == U6OP.OBJINACTOR) {
            Integer npc = (Integer) args.pop();
            Integer obj = (Integer) args.pop();
            result = party.getPlayer(npc).hasItem(obj) ? 1 : 0;
        } else if (op == U6OP.WEIGHT) {
            Integer obj = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = Ultima6.OBJ_WEIGHTS[obj] * amt;
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
            throw new IllegalArgumentException(String.format("unknown op in eval function [%s]", op));
        }
        return result;
    }

    private static void setFlag(Conversation conv, Party party, ByteBuffer bb, boolean clear) {
        bb.get();//one byte
        int npc = bb.get() & 0xff;
        bb.get();//eval
        byte b = bb.get();//one byte
        int flag = b == U6OP.ONE_BYTE.code() ? bb.get() : b;
        bb.get();//eval

        if (npc == Party.NPC_SELF) {
            if (clear) {
                conv.setFlag(flag);
            } else {
                conv.clearFlag(flag);
            }
        } else if (clear) {
            party.getPlayer(npc).clearFlag(flag);
        } else {
            party.getPlayer(npc).setFlag(flag);
        }

    }

    private static void objectMgmt(Conversation conv, Party party, ByteBuffer bb, boolean delete) {

        Stack<Object> values = new Stack<>();
        Stack<OpWrapper> operations = new Stack<>();

        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);

        int npc = (int) values.pop();
        int obj = (int) values.pop();
        int quantity = (int) values.pop();
        int quality = (int) values.pop();

        if (delete) {
            party.getPlayer(npc).addItem(obj, quantity, quality);
        } else {
            party.getPlayer(npc).addItem(obj, quantity, quality);
        }
    }

    private static void seek(ByteBuffer bb, U6OP... ops) {
        while (bb.position() < bb.limit()) {
            U6OP tmp = U6OP.get(bb);
            for (U6OP op : ops) {
                if (op == tmp) {
                    return;
                }
            }
            bb.get();
        }
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
        return val;
    }

    private static String consumeText(Conversation conv, Player player, Party party, ByteBuffer bb) {
        U6OP op = U6OP.get(bb);
        if (op != null) {
            return "";
        }
        int start = bb.position();
        do {
            byte next = bb.get();
        } while (bb.position() < bb.limit() && U6OP.find(bb.get(bb.position())) == null);
        String val = new String(bb.array(), start, bb.position() - start);
        
        val = val.replace("$G", "milord");
        val = val.replace("$P", player.getName());
        val = val.replace("$N", conv.getName());
        val = val.replace("$T", "morning");
        
        {
            Pattern p = Pattern.compile("#[0-9]+");
            Matcher m = p.matcher(val);

            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String matchedText = m.group();
                int num = Integer.parseInt(matchedText.substring(1));
                Object var = conv.getVar(num, false);
                m.appendReplacement(sb, "" + var);
            }
            m.appendTail(sb);
            val = sb.toString();
        }
        
        {
            Pattern p = Pattern.compile("$[0-9]+");
            Matcher m = p.matcher(val);

            StringBuffer sb = new StringBuffer();
            while (m.find()) {
                String matchedText = m.group();
                int num = Integer.parseInt(matchedText.substring(1));
                Object var = conv.getVar(num, true);
                m.appendReplacement(sb, "" + var);
            }
            m.appendTail(sb);
            val = sb.toString();
        }
        

        return val;
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

    public interface OutputStream {

        public void print(String string, Color color);

        public void close();

        public void setPortrait(int npc);

    }

    private static class OpWrapper {

        private final List<Object> args = new ArrayList<>();
        U6OP op;
    }

}
