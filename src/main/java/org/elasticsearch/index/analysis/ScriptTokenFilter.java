package org.elasticsearch.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UCharacterEnums.ECharacterCategory;
import com.ibm.icu.lang.UScript;

public class ScriptTokenFilter extends TokenFilter {

    private static final int basicLatin[] = new int[128];

    private char[] currentTermBuffer;

    private int currentTermLength;

    private int currentPosition;

    private int tokenStart;

    private final CharTermAttribute charTermAttribute = this.addAttribute(CharTermAttribute.class);

    private final OffsetAttribute offsetAttribute = this.addAttribute(OffsetAttribute.class);

    private final ScriptAttribute scriptAttribute = addAttribute(ScriptAttribute.class);

    static {
        for (int i = 0; i < basicLatin.length; i++) {
            basicLatin[i] = UScript.getScript(i);
        }
    }

    private static int getScript(int codepoint) {
        if (0 <= codepoint && codepoint < basicLatin.length) {
            return basicLatin[codepoint];
        } else {
            return UScript.getScript(codepoint);
        }
    }

    private static boolean isSameScript(int scriptOne, int scriptTwo) {
        return scriptOne == scriptTwo;
    }

    public ScriptTokenFilter(TokenStream input) {
        super(input);
    }

    @Override
    public final boolean incrementToken() throws IOException {
        if (this.currentTermBuffer == null) {
            if (!this.input.incrementToken()) {
                return false;
            } else {
                this.currentTermBuffer = this.charTermAttribute.buffer().clone();
                this.currentTermLength = this.charTermAttribute.length();
                this.tokenStart = this.offsetAttribute.startOffset();
                this.currentPosition = 0;

                return true;
            }
        }

        int scriptCode = UScript.INVALID_CODE;
        int endPosition = this.currentPosition;
        while (endPosition <= (this.currentTermLength - 1)) {
            char character = this.currentTermBuffer[endPosition];
            int charScriptCode = getScript(character);

            if (this.currentPosition == endPosition) {
                scriptCode = charScriptCode;
            }

            if (isSameScript(scriptCode, charScriptCode)
                    || UCharacter.getType(character) == ECharacterCategory.NON_SPACING_MARK) {
                endPosition += 1;
            } else {
                break;
            }
        }

        this.charTermAttribute.copyBuffer(this.currentTermBuffer, this.currentPosition, endPosition
                - this.currentPosition);
        this.offsetAttribute.setOffset(this.tokenStart + this.currentPosition, this.tokenStart + endPosition);
        this.scriptAttribute.setCode(scriptCode);

        this.currentPosition = endPosition;
        if (endPosition > (this.currentTermLength - 1)) {
            this.currentTermBuffer = null;
        }

        return true;
    }

    @Override
    public void reset() throws IOException {
        super.reset();

        this.currentTermBuffer = null;
    }
}
