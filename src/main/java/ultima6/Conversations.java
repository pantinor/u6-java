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

public class Conversations {

    public static boolean DEBUG = false;

    public static final Random RAND = new Random();

    public static final int GLOBAL_VAR_SEX = 0x10;// sex of avatar: male=0 female=1
    public static final int GLOBAL_VAR_KARMA = 0x14;// avatar's karma
    public static final int GLOBAL_VAR_GARGF = 0x15;// 1=player knows Gargish
    public static final int GLOBAL_VAR_NPC_NAME = 0x17;
    public static final int GLOBAL_VAR_PARTYLIVE = 0x17;// number of people (living) following avatar
    public static final int GLOBAL_VAR_PARTYALL = 0x18;// number of people (total) following avatar
    public static final int GLOBAL_VAR_HP = 0x19;// avatar's health
    public static final int GLOBAL_VAR_PLAYER_NAME = 0x19;
    public static final int GLOBAL_VAR_QUESTF = 0x1A;// 0="Thou art not upon a sacred quest!"
    public static final int GLOBAL_VAR_WORKTYPE = 0x20;// current activity of npc, from schedule
    public static final int GLOBAL_VAR_YSTRING = 0x22;// value of $Y variable.
    public static final int GLOBAL_VAR_INPUT = 0x23;// previous input from player ($Z)

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

    private static void trace(String format, Object... args) {
        if (DEBUG) {
            System.out.printf(format, args);
        }
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

        public void init(Player player, Party party, OutputStream output) {
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

            this.iVars.put(GLOBAL_VAR_SEX, player.getGender());
            this.iVars.put(GLOBAL_VAR_KARMA, player.getKarma());
            this.iVars.put(GLOBAL_VAR_GARGF, player.getGargishf());
            this.iVars.put(GLOBAL_VAR_PARTYLIVE, party.getPlayers().size());
            this.iVars.put(GLOBAL_VAR_PARTYALL, party.getPlayers().size());
            this.iVars.put(GLOBAL_VAR_HP, player.getHp());
            this.iVars.put(GLOBAL_VAR_QUESTF, player.getQuestf());
            this.iVars.put(GLOBAL_VAR_WORKTYPE, 0);

            this.sVars.put(GLOBAL_VAR_NPC_NAME, this.name);
            this.sVars.put(GLOBAL_VAR_PLAYER_NAME, player.getName());
            this.sVars.put(GLOBAL_VAR_YSTRING, "");
            this.sVars.put(GLOBAL_VAR_INPUT, "");

            output.print("You see: " + this.description, Color.BLUE);

        }

        public Object getVar(Integer idx, boolean strVal) {
            if (strVal) {
                return this.sVars.get(idx);
            } else {
                return this.iVars.get(idx);
            }
        }

        public void setVar(Integer idx, Object value, boolean strVal) {
            if (strVal) {
                this.sVars.put(idx, (String) value);
            } else {
                this.iVars.put(idx, (Integer) value);
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
                    } else if (op == U6OP.SETF) {
                        Conversations.setFlag(this, party, this.bb, false);
                    } else if (op == U6OP.CLEARF) {
                        Conversations.setFlag(this, party, this.bb, true);
                    } else if (op == U6OP.IF) {
                        condition(party, this, bb, output);
                    } else if (op == U6OP.ENDIF) {

                    } else if (op == U6OP.ASK) {
                        break;
                    } else if (op == U6OP.KEYWORDS) {
                        String keywords = consumeText(this, avatar, party, this.bb);
                        boolean matched = inputMatches(input, keywords);
                        trace("Keywords [%s] with input [%s] matched [%s]\n", keywords, input, matched);
                        if (!matched) {
                            seek(bb, U6OP.KEYWORDS, U6OP.ENDANSWERS);
                        }
                    } else if (op == U6OP.ANSWER) {
                        String answer = consumeText(this, avatar, party, this.bb);
                        if (!answer.isEmpty()) {
                            output.print(answer, null);
                        }
                    } else if (op == U6OP.JUMP) {
                        int jump = bb.getInt();
                        bb.position(jump);
                        trace("Jumped to %d\n", jump);
                    } else if (op == U6OP.INPUTNUM || op == U6OP.INPUT) {
                        int ind = bb.get() & 0xff;
                        try {
                            iVars.put(ind, Integer.parseInt(input));
                        } catch (Exception e) {
                            sVars.put(ind, input);
                        }
                        bb.get();
                    } else if (op == U6OP.INPUTSTR) {
                        int ind = bb.get() & 0xff;
                        sVars.put(ind, input);
                        bb.get();
                    } else if (op == U6OP.HEAL) {

                    } else if (op == U6OP.CURE) {

                    } else if (op == U6OP.WORKTYPE) {

                    } else if (op == U6OP.RESURRECT) {

                    } else if (op == U6OP.GIVE) {

                    } else if (op == U6OP.SUBKARMA) {

                    } else if (op == U6OP.ADDKARMA) {

                    } else if (op == U6OP.WAIT) {

                    } else if (op == U6OP.NEW) {
                        objectMgmt(this, party, this.bb, false);
                    } else if (op == U6OP.DELETE) {
                        objectMgmt(this, party, this.bb, true);
                    } else if (op == U6OP.PORTRAIT) {
                        Stack<Object> values = new Stack<>();
                        marshall(bb, values, null, this);
                        int npc = (int) values.pop();
                        output.setPortrait(npc == 0xeb ? this.id : npc);
                    } else if (op == U6OP.BYE) {
                        output.close();
                        break;
                    } else {
                        System.err.printf("Unhandled OP [%s] at line %d\n", op, bb.position());
                    }

                } else if (bb.get(bb.position()) == (byte) 0x0A) {
                    bb.get();//skip
                } else {
                    String text = consumeText(this, avatar, party, this.bb);
                    if (!text.isEmpty()) {
                        output.print(text, null);

                        //advance it
                        U6OP peek = U6OP.get(bb);
                        if (peek == U6OP.ASK) {
                            bb.get();//skip
                        }

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

        boolean condition = (Integer) finalEval > 0;

        trace("Condition met: %s\n", condition);

        if (condition) {
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

        trace("Declared %s %s as value %s\n", stringType ? "SVAR" : "IVAR", var_name, finalValue + "");

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
            } else if (bb.get(bb.position() + 1) == U6OP.FLAG.code()) {
                values.push(bb.get() & 0xff);//npc
            } else if (bb.get(bb.position()) == U6OP.NPC.code()) {
                bb.get();
                values.pop();//remove the unused extra byte 0
            } else {
                U6OP op = U6OP.get(bb);
                if (op != null) {
                    OpWrapper wrapper = new OpWrapper();
                    wrapper.op = op;
                    for (int i = 0; i < op.argCount(); i++) {
                        if (values.size() > 0) {
                            wrapper.args.add(values.remove(0));
                        } else if (operations.size() > 0) {
                            wrapper.args.add(operations.remove(0));
                        }
                    }
                    operations.push(wrapper);
                    bb.get();

                    trace("At line [%d] Marshalled [%s]\n", bb.position(), operations);

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

        trace("\tEvaluating [%s]\n", op, args);

        if (op == U6OP.EQ) {
            Object v1 = args.get(0);
            Object v2 = args.get(1);
            result = Objects.equals(v1, v2) ? 1 : 0;
        } else if (op == U6OP.NE) {
            Object v1 = args.get(0);
            Object v2 = args.get(1);
            result = !Objects.equals(v1, v2) ? 1 : 0;
        } else if (op == U6OP.GT) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 > v2 ? 1 : 0;
        } else if (op == U6OP.LT) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 < v2 ? 1 : 0;
        } else if (op == U6OP.GE) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 >= v2 ? 1 : 0;
        } else if (op == U6OP.LE) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 <= v2 ? 1 : 0;
        } else if (op == U6OP.LAND) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = (v1 > 0 && v2 > 0) ? 1 : 0;
        } else if (op == U6OP.LOR) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = (v1 > 0 || v2 > 0) ? 1 : 0;
        } else if (op == U6OP.RAND) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            if (v2 > v1) {
                result = RAND.nextInt(v2 - v1) + v1;
            } else {
                //due to bug in u6 conversations for RAND op, they have put the low number as val2 at least once I saw
                result = RAND.nextInt(v1 - v2) + v2;
            }
        } else if (op == U6OP.ADD) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 + v2;
        } else if (op == U6OP.SUB) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 - v2;
        } else if (op == U6OP.MUL) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 * v2;
        } else if (op == U6OP.DIV) {
            Integer v1 = (Integer) args.get(0);
            Integer v2 = (Integer) args.get(1);
            result = v1 / v2;
        } else if (op == U6OP.FLAG) {
            Integer npc = (Integer) args.get(0);
            Integer flag = (Integer) args.get(1);
            if (npc <= 7) {
                result = party.getPlayer(npc).getTalkFlag(flag) ? 1 : 0;
            } else {
                result = conv.getFlag(flag) ? 1 : 0;
            }
        } else if (op == U6OP.NPC) {
            Integer idx = (Integer) args.get(0);
            Integer unused = args.size() > 1 ? (Integer) args.get(1) : 0;
            result = party.get(idx).getId();
        } else if (op == U6OP.CANCARRY) {
            Integer idx = (Integer) args.get(0);
            Player p = party.get(idx);
            int val = p.getMaxInventoryWeight() - p.getInventoryWeight() * 10;
            result = val;
        } else if (op == U6OP.OBJINPARTY) {
            Integer obj = (Integer) args.get(0);
            Integer quality = (Integer) args.get(1);
            result = party.isObjectInParty(ultima6.Objects.Object.get(obj), quality);
        } else if (op == U6OP.OBJCOUNT) {
            Integer idx = (Integer) args.get(0);
            Integer obj = (Integer) args.get(1);
            Player p = party.get(idx);
            result = p.quantity(ultima6.Objects.Object.get(obj));
        } else if (op == U6OP.STR) {
            Integer idx = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.getStrength() + amt;
            p.setStrength((Integer) result);
        } else if (op == U6OP.INT) {
            Integer idx = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.getIntelligence() + amt;
            p.setIntelligence((Integer) result);
        } else if (op == U6OP.DEX) {
            Integer idx = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.getDex() + amt;
            p.setDex((Integer) result);
        } else if (op == U6OP.LVL) {
            Integer idx = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.getLevel() + amt;
            p.setLevel((Integer) result);
        } else if (op == U6OP.EXP) {
            Integer idx = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.getExp() + amt;
            p.setExp((Integer) result);
        } else if (op == U6OP.OBJINACTOR) {
            Integer idx = (Integer) args.pop();
            Integer obj = (Integer) args.pop();
            Player p = party.get(idx);
            result = p.hasItem(ultima6.Objects.Object.get(obj)) ? 1 : 0;
        } else if (op == U6OP.WEIGHT) {
            Integer obj = (Integer) args.pop();
            Integer amt = (Integer) args.pop();
            result = Ultima6.OBJ_WEIGHTS[obj] & 0xff * amt;
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
            Integer idx = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 is in proximity to self, 0 if not.
        } else if (op == U6OP.WOUNDED) {
            Integer idx = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 is wounded, 0 if current HP equals maximum HP.
        } else if (op == U6OP.POISONED) {
            Integer idx = (Integer) args.pop();
            result = 0;//todo
            //1 if NPC val1 "poisoned" flag is true, 0 if it is false.
        } else if (op == U6OP.NPCINPARTY) {
            Integer npc = (Integer) args.pop();
            result = 1;//todo
            //1 if NPC val1 is in the Avatar's party, 0 if not.
        } else if (op == U6OP.HORSED) {
            Integer idx = (Integer) args.pop();
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

        trace("\t\tEvaluated with result [%s]\n", result);

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
                conv.clearFlag(flag);
            } else {
                conv.setFlag(flag);
            }
        } else if (clear) {
            party.getPlayer(npc).clearTalkFlag(flag);
        } else {
            party.getPlayer(npc).setTalkFlag(flag);
        }

        trace("Set Flag on NPC [%d] %s Flag [%d]\n", npc, clear ? "Cleared" : "Set", flag);

    }

    private static void objectMgmt(Conversation conv, Party party, ByteBuffer bb, boolean delete) {

        Stack<Object> values = new Stack<>();
        Stack<OpWrapper> operations = new Stack<>();

        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);
        marshall(bb, values, operations, conv);

        int idx = (int) values.get(0);
        int obj = (int) values.get(1);
        int quality = (int) values.get(2);
        int quantity = (int) values.get(3);

        Player player = party.get(idx);

        trace("Object MGMT on NPC [%d] player found? [%s] %s [%s] quantity %d quality %d\n",
                idx, player != null, delete ? "Removed Item" : "Added Item", ultima6.Objects.Object.get(obj), quantity, quality);

        if (delete) {
            player.removeItem(ultima6.Objects.Object.get(obj), quantity, quality);
        } else {
            player.addItem(ultima6.Objects.Object.get(obj), quantity, quality);
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
        val = val.replace("*", "");
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

        val = val.replace("*", "");

        val = val.replace("$G", "milord");
        val = val.replace("$P", player.getName());
        val = val.replace("$N", conv.getName());
        val = val.replace("$T", "morning");

        val = val.replace("$Y", (String) conv.getVar(GLOBAL_VAR_YSTRING, true));
        val = val.replace("$Z", (String) conv.getVar(GLOBAL_VAR_INPUT, true));

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

        private U6OP op;
        private final List<Object> args = new ArrayList<>();

        @Override
        public String toString() {
            return "OpWrapper{" + "op=" + op + ", args=" + args + '}';
        }

    }
}
