package org.elasticsearch.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.icu.tokenattributes.ScriptAttribute;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

import com.ibm.icu.lang.UScript;

public class AbbreviatedSpellingTokenFilter extends TokenFilter {

    private char[] currentTermBuffer;

    private int currentTermLength;

    private String[] termAbbreviatedSpellingBuffer;

    private int termAbbreviatedSpellingPosition = -1;

    private final CharTermAttribute charTermAttribute = this.addAttribute(CharTermAttribute.class);

    private final ScriptAttribute scriptAttribute = addAttribute(ScriptAttribute.class);

    /**
     * @param input
     */
    public AbbreviatedSpellingTokenFilter(TokenStream input) {
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
            if (this.termAbbreviatedSpellingBuffer == null || this.termAbbreviatedSpellingBuffer.length == 0) {
                this.termAbbreviatedSpellingBuffer = Spellings.toAbbreviatedSpellings(new String(this.currentTermBuffer,
                        0, this.currentTermLength));
                this.termAbbreviatedSpellingPosition = -1;
            }

            if (this.termAbbreviatedSpellingBuffer == null || this.termAbbreviatedSpellingBuffer.length == 0) {
                this.currentTermBuffer = null;
                this.currentTermLength = 0;
                this.termAbbreviatedSpellingBuffer = null;
                this.termAbbreviatedSpellingPosition = -1;
            } else {
                this.termAbbreviatedSpellingPosition += 1;
                String fullSpelling = this.termAbbreviatedSpellingBuffer[this.termAbbreviatedSpellingPosition];
                this.charTermAttribute.copyBuffer(fullSpelling.toCharArray(), 0, fullSpelling.length());

                if ((this.termAbbreviatedSpellingBuffer.length - 1) == this.termAbbreviatedSpellingPosition) {
                    this.currentTermBuffer = null;
                    this.currentTermLength = 0;
                    this.termAbbreviatedSpellingBuffer = null;
                    this.termAbbreviatedSpellingPosition = -1;
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

        System.out.println("code is : " + this.getAttribute(ScriptAttribute.class).getCode());
//        int scriptCode = this.scriptAttribute.getCode();
//        if (UScript.HAN != scriptCode) {
//            return true;
//        }

        this.currentTermBuffer = this.charTermAttribute.buffer().clone();
        this.currentTermLength = this.charTermAttribute.length();
        return true;
    }

}
