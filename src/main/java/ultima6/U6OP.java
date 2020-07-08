package ultima6;

import java.nio.ByteBuffer;

public enum U6OP {
    GT((byte) 0x81, 2),
    GE((byte) 0x82, 2),
    LT((byte) 0x83, 2),
    LE((byte) 0x84, 2),
    NE((byte) 0x85, 2),
    EQ((byte) 0x86, 2),
    ADD((byte) 0x90, 2),
    SUB((byte) 0x91, 2),
    MUL((byte) 0x92, 2),
    DIV((byte) 0x93, 2),
    LOR((byte) 0x94, 2),
    LAND((byte) 0x95, 2),
    CANCARRY((byte) 0x9a, 1),
    WEIGHT((byte) 0x9b, 2),
    HORSED((byte) 0x9d, 1),
    RAND((byte) 0xa0, 2),
    EVAL((byte) 0xa7, 0),
    FLAG((byte) 0xab, 2),
    VAR((byte) 0xb2, 1),
    SVAR((byte) 0xb3, 1),
    DATA((byte) 0xb4, 2),
    OBJCOUNT((byte) 0xbb, 2),
    NPCINPARTY((byte) 0xc6, 1),
    OBJINPARTY((byte) 0xc7, 2),
    JOIN((byte) 0xca, 1),
    LEAVE((byte) 0xcc, 1),
    ONE_BYTE((byte) 0xd3, 1),
    TWO_BYTE((byte) 0xd4, 2),
    FOUR_BYTE((byte) 0xd2, 4),
    NPCNEARBY((byte) 0xd7, 1),
    WOUNDED((byte) 0xda, 1),
    POISONED((byte) 0xdc, 1),
    NPC((byte) 0xdd, 2),
    EXP((byte) 0xe0, 2),
    LVL((byte) 0xe1, 2),
    STR((byte) 0xe2, 2),
    INT((byte) 0xe3, 2),
    DEX((byte) 0xe4, 2),
    HORSE((byte) 0x9c, 1),
    IF((byte) 0xa1, 0),
    ENDIF((byte) 0xa2, 0),
    ELSE((byte) 0xa3, 0),
    SETF((byte) 0xa4, 2),
    CLEARF((byte) 0xa5, 2),
    DECL((byte) 0xa6, 0),
    ASSIGN((byte) 0xa8, 0),
    JUMP((byte) 0xb0, 1),
    DPRINT((byte) 0xb5, 2),
    BYE((byte) 0xb6, 0),
    NEW((byte) 0xb9, 4),
    DELETE((byte) 0xba, 4),
    INVENTORY((byte) 0xbe, 1),
    PORTRAIT((byte) 0xbf, 1),
    ADDKARMA((byte) 0xc4, 2),
    SUBKARMA((byte) 0xc5, 2),
    GIVE((byte) 0xc9, 4),
    WAIT((byte) 0xcb, 0),
    WORKTYPE((byte) 0xcd, 2),
    SETNAME((byte) 0xd8, 1),
    HEAL((byte) 0xd9, 1),
    CURE((byte) 0xdb, 1),
    ENDANSWERS((byte) 0xee, 0),
    KEYWORDS((byte) 0xef, 0),
    SLOOK((byte) 0xf1, 0),
    SCONVERSE((byte) 0xf2, 0),
    SPREFIX((byte) 0xf3, 0),
    ANSWER((byte) 0xf6, 0),
    ASK((byte) 0xf7, 0),
    ASKC((byte) 0xf8, 0),
    INPUT((byte) 0xfb, 2),
    INPUTNUM((byte) 0xfc, 2),
    SIDENT((byte) 0xff, 0),
    SLEEP((byte) 0x9e, 0),
    HASOBJ((byte) 0x9F, 3),
    RESURRECT((byte) 0xD6, 1),
    INPUTSTR((byte) 0xF9, 2),
    INDEXOF((byte) 0xB7, 2),
    ENDOFLIST((byte) 0xB8, 0);

    private byte code;
    private int argCount;

    private U6OP(byte code, int argCount) {
        this.code = code;
        this.argCount = argCount;
    }

    public byte code() {
        return code;
    }

    public int argCount() {
        return argCount;
    }

    public static U6OP find(byte b) {
        for (U6OP op : U6OP.values()) {
            if (op.code == b) {
                return op;
            }
        }
        return null;
    }

    public static U6OP get(ByteBuffer bb) {
        byte b = bb.get(bb.position());
        Byte prev = bb.position() >= 1 ? bb.get(bb.position() - 1) : null;
        for (U6OP op : U6OP.values()) {
            if (op.code == b) {
                if (prev != null && (prev == JUMP.code() || prev == ONE_BYTE.code() || prev == TWO_BYTE.code() || prev == FOUR_BYTE.code())) {
                    return null;
                }
                return op;
            }
        }
        return null;
    }

}
