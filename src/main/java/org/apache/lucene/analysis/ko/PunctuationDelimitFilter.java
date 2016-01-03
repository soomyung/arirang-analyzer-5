package org.apache.lucene.analysis.ko;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.analysis.TokenFilter;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.KeywordAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;

/**
 * Created by SooMyung(soomyung.lee@gmail.com) on 2014. 7. 30.
 */

public final class PunctuationDelimitFilter extends TokenFilter {

    private final LinkedList<Token> outQueue = new LinkedList<Token>();

    private State currentState = null;

    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final PositionIncrementAttribute posIncrAtt = addAttribute(PositionIncrementAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final KeywordAttribute keywordAtt = addAttribute(KeywordAttribute.class);

    /**
     * Construct a token stream filtering the given input.
     *
     * @param input
     */
    protected PunctuationDelimitFilter(TokenStream input) {
        super(input);
    }

    @Override
    public boolean incrementToken() throws IOException {

        if (!outQueue.isEmpty()) {
            restoreState(currentState);
            setAttributesFromQueue(false);
            return true;
        }

        while (input.incrementToken()) {
        	if (keywordAtt.isKeyword()) return true;
            if(!containPunctuation(termAtt.toString())) return true;

            splitByPunctuation(termAtt.toString());

            if (!outQueue.isEmpty()) {
                setAttributesFromQueue(true);
                return true;
            }
        }

        return false;
    }

    private void setAttributesFromQueue(boolean isFirst) {
        final Token iw = outQueue.removeFirst();

        if (isFirst && !outQueue.isEmpty()) {
            termAtt.setEmpty();
            currentState = captureState();
        }

        termAtt.setEmpty().append(iw.getTerm());
        offsetAtt.setOffset(iw.getOffset(), iw.getOffset() + iw.term.length());
        posIncrAtt.setPositionIncrement(iw.getIncrement());
    }

    private void splitByPunctuation(String term) {
        if(term.length()<2) return;
        StringBuffer sb = new StringBuffer();
        int startOffset = offsetAtt.startOffset();

        List<String> array = new ArrayList<String>();
        for(int i=0;i<term.length();i++) {
            if(isPunctuation(term.charAt(i))) {
                if(sb.length()>0) {
                    array.add(sb.toString());
                    sb = new StringBuffer();
                }
                continue;
            }
            sb.append(term.charAt(i));
        }
        
        if(sb.length()>0) 
        	array.add(sb.toString());
    
        if(array.size()==0) return;

        // add original text
        outQueue.add(new Token(termAtt.toString(),startOffset, posIncrAtt.getPositionIncrement()));

        sb = new StringBuffer();
        for(String str : array) {
            sb.append(str);
        }

        // add a token with punctuation removed.
        outQueue.add(new Token(sb.toString(),startOffset, 0));
        if(sb.length()==1) return;
        
        // add tokens splited by punctuation.
        int offset = 0;
        for(int i=0; i<array.size();i++) {
            int inc = i==0 ? 0 : 1;
            outQueue.add(new Token(array.get(i),startOffset+offset, inc));
            offset += array.get(i).length();
        }

    }

    private boolean containPunctuation(String term) {
        for(int i=0;i<term.length()-1;i++) {
            if(isPunctuation(term.charAt(i))) return true;
        }
        return false;
    }

    private static boolean isPunctuation(char ch) {
        switch(Character.getType(ch)) {
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
            case Character.CONTROL:
            case Character.FORMAT:
            case Character.DASH_PUNCTUATION:
            case Character.START_PUNCTUATION:
            case Character.END_PUNCTUATION:
            case Character.CONNECTOR_PUNCTUATION:
            case Character.OTHER_PUNCTUATION:
            case Character.MATH_SYMBOL:
            case Character.CURRENCY_SYMBOL:
            case Character.MODIFIER_SYMBOL:
            case Character.OTHER_SYMBOL:
            case Character.INITIAL_QUOTE_PUNCTUATION:
            case Character.FINAL_QUOTE_PUNCTUATION:
                return true;
            default:
                return false;
        }
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        outQueue.clear();
        currentState = null;
    }

    private class Token {
        int offset;

        int increment = 1;

        String term;

        public Token(String term, int offset) {
            this.term = term;
            this.offset= offset;
        }

        public Token(String term, int offset, int inc) {
            this(term,offset);
            this.increment =inc;
        }

        public int getOffset() {
            return offset;
        }

        @SuppressWarnings("unused")
		public void setOffset(int offset) {
            this.offset = offset;
        }

        public int getIncrement() {
            return increment;
        }

        @SuppressWarnings("unused")
		public void setIncrement(int increment) {
            this.increment = increment;
        }

        public String getTerm() {
            return term;
        }

        @SuppressWarnings("unused")
		public void setTerm(String term) {
            this.term = term;
        }
    }

}
