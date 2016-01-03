package org.apache.lucene.analysis.ko;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.lucene.analysis.Tokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.apache.lucene.analysis.util.CharacterUtils;
import org.apache.lucene.util.AttributeFactory;

public final class KoreanTokenizer extends Tokenizer {

    private int offset = 0, bufferIndex = 0, dataLen = 0, finalOffset = 0;
    private static final int MAX_WORD_LEN = 255;
    private static final int IO_BUFFER_SIZE = 4096;

    private final CharacterUtils charUtils;
    private final CharacterUtils.CharacterBuffer ioBuffer = CharacterUtils.newCharacterBuffer(IO_BUFFER_SIZE);

    private static Map<Integer,Integer> pairmap = new HashMap<Integer,Integer>();

    static {
        pairmap.put(34,34);// ""
        pairmap.put(39,39);// ''
        pairmap.put(40,41);// ()
        pairmap.put(60,62);// <>
        pairmap.put(91,93);// []
        pairmap.put(123,125);// {}
        pairmap.put(65288,65289);// ‘’
        pairmap.put(8216,8217);// ‘’
        pairmap.put(8220,8221);// “”
    }

    private List<Integer> pairstack = new ArrayList<Integer>();

    public static final String TYPE_KOREAN = "korean";
    public static final String TYPE_WORD = "word";

    // this tokenizer generates three attributes:
    // term offset, positionIncrement and type
    private final CharTermAttribute termAtt = addAttribute(CharTermAttribute.class);
    private final OffsetAttribute offsetAtt = addAttribute(OffsetAttribute.class);
    private final TypeAttribute typeAtt = addAttribute(TypeAttribute.class);
    public KoreanTokenizer() {
        charUtils = CharacterUtils.getInstance();
    }

    public KoreanTokenizer(AttributeFactory factory) {
        super(factory);
        charUtils = CharacterUtils.getInstance();
    }

    @Override
    public final boolean incrementToken() throws IOException {

        clearAttributes();
        char[] buffer = termAtt.buffer();

        int length = 0;
        int start = -1; // this variable is always initialized
        int end = -1;

        while (true) {
            if (bufferIndex >= dataLen) {
                offset += dataLen;
                charUtils.fill(ioBuffer, input); // read supplementary char aware with CharacterUtils
                if (ioBuffer.getLength() == 0) {
                    dataLen = 0; // so next offset += dataLen won't decrement offset
                    if (length > 0) {
                        break;
                    } else {
                        finalOffset = correctOffset(offset);
                        return false;
                    }
                }
                dataLen = ioBuffer.getLength();
                bufferIndex = 0;
            }

            // use CharacterUtils here to support < 3.1 UTF-16 code unit behavior if the char based methods are gone
            final int c = charUtils.codePointAt(ioBuffer.getBuffer(), bufferIndex, ioBuffer.getLength());
            final int charCount = Character.charCount(c);
            bufferIndex += charCount;

            if(pairstack.size()>0 && pairstack.get(0)==c) {
                pairstack.remove(0);
                continue;
            }

            int closechar = getPairChar(c);
            if(closechar!=0) {
                if((pairstack.size()==0 || pairstack.get(0)!=closechar) && length>0) {
                    pairstack.add(0,closechar);
                    break;
                } else {
                    pairstack.add(0,closechar);
                    continue;
                }
            }

            if (isTokenChar(c)) {               // if it's a token char
                if (length == 0) {                // start of token
                    assert start == -1;
                    start = offset + bufferIndex - charCount;
                    end = start;
                } else if (length >= buffer.length - 1) { // check if a supplementary could run out of bounds
                    buffer = termAtt.resizeBuffer(2 + length); // make sure a supplementary fits in the buffer
                }
                end += charCount;
                length += Character.toChars(c, buffer, length); // buffer it
                if (length >= MAX_WORD_LEN)
                    break; // buffer overflow! make sure to check for >= surrogate pair could break == test
            } else if (length > 0) {           // at non-Letter w/ chars
                break;
            }// return 'em
        }

        termAtt.setLength(length);
        assert start != -1;
        offsetAtt.setOffset(correctOffset(start), finalOffset = correctOffset(end));
        typeAtt.setType(getType());
        return true;
    }


    private boolean isTokenChar(int c) {
        if(Character.isLetter(c) || Character.isDigit(c) || isPreserveSymbol((char)c)) return true;
        return false;
    }

    private int getPairChar(int c) {
        Integer p = pairmap.get(c);
        return p==null ? 0 : p;
    }

    /**
     * if more than a character exist, the token is considered as a korean token
     * @return
     */
    private String getType() {
        char[] buffer = termAtt.buffer();
        int leng = termAtt.length();
        for(int i=0;i<leng;i++) {
            if(buffer[i]=='\u0000') break;
            if(buffer[i]>='\uAC00' && buffer[i]<='\uD7A3') return TYPE_KOREAN;
        }
        return TYPE_WORD;
    }

    private boolean isPreserveSymbol(char c) {
        return (c=='#' || c=='+' || c=='-' || c=='·' || c == '&');
    }

    @Override
    public final void end() throws IOException {
        super.end();
        // set final offset
        offsetAtt.setOffset(finalOffset, finalOffset);
    }

    @Override
    public void reset() throws IOException {
        super.reset();
        bufferIndex = 0;
        offset = 0;
        dataLen = 0;
        finalOffset = 0;
        ioBuffer.reset(); // make sure to reset the IO buffer!!
    }

}
