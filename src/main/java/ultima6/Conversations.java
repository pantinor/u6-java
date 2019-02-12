package ultima6;

import com.badlogic.gdx.graphics.g2d.TextureRegion;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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

        public Conversation(int id, String name, byte[] data) {
            this.id = id;
            this.name = name;
            this.data = data;
            this.bb = ByteBuffer.wrap(data);
        }

        public String process(String input) {
            String keywords = null, answer = null;
            while (bb.position() < bb.limit()) {
                byte b = bb.get();
                U6OP op = U6OP.find(b);
                if (op != null) {
                    switch (op) {
                        case ENDANSWERS:
                            break;
                        case KEYWORDS:
                            keywords = getFormattedText();
                            break;
                        case SLOOK:
                            this.description = getFormattedText();
                            break;
                        case SCONVERSE:
                            break;
                        case SPREFIX:
                            break;
                        case ANSWER:
                            answer = getFormattedText();
                            if (matchKeywords(input, keywords)) {
                                return answer;
                            }
                            break;
                        case ASK:
                            break;
                        case ASKC:
                            break;
                        case INPUT:
                            break;
                        case GT:
                            break;
                        case GE:
                            break;
                        case LT:
                            break;
                        case LE:
                            break;
                        case NE:
                            break;
                        case EQ:
                            break;
                        case ADD:
                            break;
                        case SUB:
                            break;
                        case MUL:
                            break;
                        case LOR:
                            break;
                        case LAND:
                            break;
                        case CANCARRY:
                            break;
                        case WEIGHT:
                            break;
                        case HORSED:
                            break;
                        case RAND:
                            break;
                        case EVAL:
                            break;
                        case FLAG:
                            break;
                        case VAR:
                            break;
                        case SVAR:
                            break;
                        case DATA:
                            break;
                        case OBJCOUNT:
                            break;
                        case INPARTY:
                            break;
                        case OBJINPARTY:
                            break;
                        case JOIN:
                            break;
                        case LEAVE:
                            break;
                        case ONE_BYTE:
                            break;
                        case TWO_BYTE:
                            break;
                        case FOUR_BYTE:
                            break;
                        case NPCNEARBY:
                            break;
                        case WOUNDED:
                            break;
                        case POISONED:
                            break;
                        case NPC:
                            break;
                        case EXP:
                            break;
                        case LVL:
                            break;
                        case STR:
                            break;
                        case INT:
                            break;
                        case DEX:
                            break;
                        case HORSE:
                            break;
                        case IF:
                            break;
                        case ENDIF:
                            break;
                        case ELSE:
                            break;
                        case SETF:
                            break;
                        case CLEARF:
                            break;
                        case DECL:
                            break;
                        case ASSIGN:
                            break;
                        case JUMP:
                            break;
                        case DPRINT:
                            break;
                        case BYE:
                            break;
                        case NEW:
                            break;
                        case DELETE:
                            break;
                        case INVENTORY:
                            break;
                        case PORTRAIT:
                            break;
                        case ADDKARMA:
                            break;
                        case SUBKARMA:
                            break;
                        case GIVE:
                            break;
                        case WAIT:
                            break;
                        case WORKTYPE:
                            break;
                        case SETNAME:
                            break;
                        case HEAL:
                            break;
                        case CURE:
                            break;
                        case INPUTNUM:
                            break;
                        case SIDENT:
                            break;
                        case SLEEP:
                            break;
                        case OBJINACTOR:
                            break;
                        case RESURRECT:
                            break;
                        case INPUTSTR:
                            break;
                        case DIV:
                            break;
                        case INDEXOF:
                            break;
                        case ENDOFLIST:
                            break;
                        default:
                            break;

                    }
                }
            }

            return "I cannot help thee with that.";

        }

        private boolean matchKeywords(String input, String keywords) {
            StringTokenizer st = new StringTokenizer(keywords, ",");
            while (st.hasMoreTokens()) {
                String tok = st.nextToken().trim().toLowerCase();
                if (input.toLowerCase().contains(tok)) {
                    return true;
                }
            }
            return false;
        }

        private String getFormattedText() {
            int start = bb.position();
            do {
                byte next = bb.get();
            } while (bb.position() < bb.limit() && U6OP.find(bb.get(bb.position())) == null);
            String val = new String(bb.array(), start, bb.position() - start);
            return prepare(val);
        }

        private String prepare(String s) {
            s = s.replace("$N", this.name);
            return s.replace("\\", "").replace("\"", "").replace("\'", "'").replace("\n", "").replace("*", " ").trim();
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

    }

}
