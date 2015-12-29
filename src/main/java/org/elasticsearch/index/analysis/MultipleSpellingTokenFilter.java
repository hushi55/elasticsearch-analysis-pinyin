package org.elasticsearch.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.ibm.icu.lang.UScript;

public class MultipleSpellingTokenFilter extends TokenFilter {

    private char[] currentTermBuffer;

    private int currentTermLength;

    private State stateCache;

    private String[] termMultipleSpellingBuffer;

    private int termMultipleSpellingPosition = -1;

    private final CharTermAttribute charTermAttribute = this.addAttribute(CharTermAttribute.class);

    private final ScriptAttribute scriptAttribute = addAttribute(ScriptAttribute.class);

    /**
     * @param input
     */
    public MultipleSpellingTokenFilter(TokenStream input) {
        super(input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    public final boolean incrementToken() throws IOException {
        if (this.currentTermBuffer != null) {
            if (this.termMultipleSpellingBuffer == null || this.termMultipleSpellingBuffer.length == 0) {
                this.termMultipleSpellingBuffer = Spellings.toMultipleSpellings(new String(this.currentTermBuffer, 0,
                        this.currentTermLength));
                this.termMultipleSpellingPosition = -1;
            }

            if (this.termMultipleSpellingBuffer == null || this.termMultipleSpellingBuffer.length == 0) {
                this.currentTermBuffer = null;
                this.currentTermLength = 0;
                this.stateCache = null;
                this.termMultipleSpellingBuffer = null;
                this.termMultipleSpellingPosition = -1;
            } else {
                this.termMultipleSpellingPosition += 1;
                String fullSpelling = this.termMultipleSpellingBuffer[this.termMultipleSpellingPosition];
                this.restoreState(this.stateCache);
                this.charTermAttribute.copyBuffer(fullSpelling.toCharArray(), 0, fullSpelling.length());

                if ((this.termMultipleSpellingBuffer.length - 1) == this.termMultipleSpellingPosition) {
                    this.currentTermBuffer = null;
                    this.currentTermLength = 0;
                    this.stateCache = null;
                    this.termMultipleSpellingBuffer = null;
                    this.termMultipleSpellingPosition = -1;
                }

                return true;
            }
        }

        if (!this.input.incrementToken()) {
            return false;
        }

        if (this.scriptAttribute == null) {
            return true;
        }

//        System.out.println("code is : " + this.getAttribute(CharTermAttribute.class));
//        System.out.println("code is : " + this.getAttribute(CharTermAttribute.class).charAt(0));
//        if (this.getAttribute(CharTermAttribute.class).length() == 1) {
//        	System.out.println(Spellings.isChinese(this.getAttribute(CharTermAttribute.class).charAt(0)));
//        	if (!Spellings.isChinese(this.getAttribute(CharTermAttribute.class).charAt(0))) {
//        		return true;
//        	}
//        }
//        int scriptCode = this.scriptAttribute.getCode();
//        if (UScript.HAN != scriptCode) {
//            return true;
//        }

        this.currentTermBuffer = this.charTermAttribute.buffer().clone();
        this.currentTermLength = this.charTermAttribute.length();
        this.stateCache = this.captureState();
        return true;
    }
}
