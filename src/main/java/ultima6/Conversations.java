package ultima6;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class Conversations {

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

    public void put(int id, String name, byte[] data) {
        this.conversations.put(id, new Conversation(id, name, data));
    }

    public Iterator<Conversation> iter() {
        return this.conversations.values().iterator();
    }

    public static class Conversation {

        private final int id;
        private final String name;
        private final byte[] data;
        private String description;
        private final Map<String, String> topics = new HashMap<>();

        public Conversation(int id, String name, byte[] data) {
            this.id = id;
            this.name = name;
            this.data = data;

            List<Tuple> ops = new ArrayList<>();
            int lastlen = 0;
            for (int i = 0; i < data.length; i++) {
                U6OP op = U6OP.find(data[i]);
                if (op != null) {
                    ops.add(new Tuple(op, i, lastlen));
                    lastlen = 0;
                } else {
                    lastlen++;
                }
            }

            for (int i = 0; i < ops.size(); i++) {
                Tuple t = ops.get(i);
                if ((t.op == U6OP.ANSWER || t.op == U6OP.KEYWORDS || t.op == U6OP.SLOOK) && (i + 1 < ops.size())) {
                    Tuple t2 = ops.get(i + 1);
                    t.val = normalize(new String(data, t.idx + 1, t2.lastlen));
                }
            }

            for (int i = 0; i < ops.size(); i++) {
                Tuple t = ops.get(i);
                if (t.val != null && t.val.length() > 0) {
                    if (t.op == U6OP.KEYWORDS) {
                        topics.put(t.val, ops.get(i + 1).val);
                    }
                    if (t.op == U6OP.SLOOK) {
                        this.description = t.val;
                    }
                }
            }

        }

        private String normalize(String sb) {
            return sb.replace("\\", "").replace("\"", "").replace("\'", "'").replace("\n", "").replace("*", " ").trim();
        }

        public int getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return "Conversation{" + "id=" + id + ", name=" + name + ", description=" + description + ", topics=" + topics + '}';
        }

    }

    private static class Tuple {

        U6OP op;
        int idx;
        int lastlen;
        String val;

        Tuple(U6OP op, int idx, int lastlen) {
            this.op = op;
            this.idx = idx;
            this.lastlen = lastlen;
        }
    }

}
