package ultima6;

import java.nio.ByteBuffer;
import java.util.Map;
import javax.xml.bind.DatatypeConverter;
import ultima6.Conversations.OutputStream;

public class Conditional {

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

    public void evaluate(Player player, ByteBuffer bb, Map<Integer, Integer> iVars, Map<Integer, String> sVars, OutputStream output) {

        ByteTokenizer evalTokens = new ByteTokenizer(ifBlock,
                new byte[]{U6OP.EVAL.code()},
                new byte[]{U6OP.JUMP.code(), U6OP.ONE_BYTE.code(), U6OP.TWO_BYTE.code(), U6OP.FOUR_BYTE.code()}
        );
    }

    @Override
    public String toString() {
        if (elseBlock != null) {
            return DatatypeConverter.printHexBinary(ifBlock) + "\n" + DatatypeConverter.printHexBinary(elseBlock);
        } else {
            return DatatypeConverter.printHexBinary(ifBlock);
        }
    }

}
