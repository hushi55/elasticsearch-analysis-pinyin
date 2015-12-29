package org.elasticsearch.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.ibm.icu.lang.UScript;

public class FullSpellingTokenFilter extends TokenFilter {

    private char[] currentTermBuffer;

    private int currentTermLength;

    private String[] termFullSpellingBuffer;

    private int termFullSpellingPosition = -1;

    private final CharTermAttribute charTermAttribute = this.addAttribute(CharTermAttribute.class);

    private final ScriptAttribute scriptAttribute = addAttribute(ScriptAttribute.class);

    /**
     * @param input
     */
    public FullSpellingTokenFilter(TokenStream input) {
        super(input);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.apache.lucene.analysis.TokenStream#incrementToken()
     */
    @Override
    public boolean incrementToken() throws IOException {
        if (this.currentTermBuffer != null) {
            if (this.termFullSpellingBuffer == null || this.termFullSpellingBuffer.length == 0) {
                this.termFullSpellingBuffer = Spellings.toFullSpellings(new String(this.currentTermBuffer, 0,
                        this.currentTermLength));
                this.termFullSpellingPosition = -1;
            }

            if (this.termFullSpellingBuffer == null || this.termFullSpellingBuffer.length == 0) {
                this.currentTermBuffer = null;
                this.currentTermLength = 0;
                this.termFullSpellingBuffer = null;
                this.termFullSpellingPosition = -1;
            } else {
                this.termFullSpellingPosition += 1;
                String fullSpelling = this.termFullSpellingBuffer[this.termFullSpellingPosition];
                this.charTermAttribute.copyBuffer(fullSpelling.toCharArray(), 0, fullSpelling.length());

                if ((this.termFullSpellingBuffer.length - 1) == this.termFullSpellingPosition) {
                    this.currentTermBuffer = null;
                    this.currentTermLength = 0;
                    this.termFullSpellingBuffer = null;
                    this.termFullSpellingPosition = -1;
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

//        int scriptCode = this.scriptAttribute.getCode();
//        if (UScript.HAN != scriptCode) {
//            return true;
//        }

        this.currentTermBuffer = this.charTermAttribute.buffer().clone();
        this.currentTermLength = this.charTermAttribute.length();
        return true;
    }

}
