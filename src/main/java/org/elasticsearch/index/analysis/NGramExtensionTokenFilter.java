package org.elasticsearch.index.analysis;

import java.io.IOException;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;

/**
 * Tokenizes the input into n-grams of the given size(s).
 */
public final class NGramExtensionTokenFilter extends TokenFilter {
  public static final int DEFAULT_MIN_NGRAM_SIZE = 1;
  public static final int DEFAULT_MAX_NGRAM_SIZE = 2;

  private int minGram, maxGram;
  
  private char[] curTermBuffer;
  private int curTermLength;
  private int curGramSize;
  private int curPos;
  private int tokStart;
  private int tokEnd; // only used if the length changed before this filter
  private boolean hasIllegalOffsets; // only if the length changed before this filter
  
  private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
  private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);

  /**
   * Creates NGramTokenFilter with given min and max n-grams.
   * @param input {@link TokenStream} holding the input to be tokenized
   * @param minGram the smallest n-gram to generate
   * @param maxGram the largest n-gram to generate
   */
  public NGramExtensionTokenFilter(TokenStream input, int minGram, int maxGram) {
    super(input);
    if (minGram < 1) {
      throw new IllegalArgumentException("minGram must be greater than zero");
    }
    if (minGram > maxGram) {
      throw new IllegalArgumentException("minGram must not be greater than maxGram");
    }
    this.minGram = minGram;
    this.maxGram = maxGram;
  }

  /**
   * Creates NGramTokenFilter with default min and max n-grams.
   * @param input {@link TokenStream} holding the input to be tokenized
   */
  public NGramExtensionTokenFilter(TokenStream input) {
    this(input, DEFAULT_MIN_NGRAM_SIZE, DEFAULT_MAX_NGRAM_SIZE);
  }

  /** Returns the next token in the stream, or null at EOS. */
  @Override
  public final boolean incrementToken() throws IOException {
    while (true) {
      if (curTermBuffer == null) {
        if (!input.incrementToken()) {
          return false;
        } else {
          curTermBuffer = termAtt.buffer().clone();
          curTermLength = termAtt.length();
          curGramSize = minGram;
          curPos = 0;
          tokStart = offsetAtt.startOffset();
          tokEnd = offsetAtt.endOffset();
          // if length by start + end offsets doesn't match the term text then assume
          // this is a synonym and don't adjust the offsets.
          hasIllegalOffsets = (tokStart + curTermLength) != tokEnd;
        }
      }
      while (curGramSize <= maxGram) {
        while (curPos+curGramSize <= curTermLength) {     // while there is input
          // clearAttributes();
          termAtt.copyBuffer(curTermBuffer, curPos, curGramSize);
          if (hasIllegalOffsets) {
            offsetAtt.setOffset(tokStart, tokEnd);
          } else {
            offsetAtt.setOffset(tokStart + curPos, tokStart + curPos + curGramSize);
          }
          curPos++;
          return true;
        }
        curGramSize++;                         // increase n-gram size
        curPos = 0;
      }
      curTermBuffer = null;
    }
  }

  @Override
  public void reset() throws IOException {
    super.reset();
    curTermBuffer = null;
  }
}
