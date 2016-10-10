/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.tokenisation;

import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.segmentation.SubSegment;
import es.ua.dlsi.segmentation.Word;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author mespla
 */
public class TokenizedSubSegment extends SubSegment{
    private String original_text;

    private List<SegmentToken> tokens;

    public TokenizedSubSegment(String segment, int position, int length) {
        super(segment, position, length);
        
        int word_init_pos = 0;
        List<SegmentToken> tokens= new LinkedList<>();
        boolean inword = false;
        for(int pos = 0; pos < segment.length(); pos++){
            if(inword){
                if(Character.isWhitespace(segment.charAt(pos))){
                    tokens.add(new SegmentToken(word_init_pos, pos-word_init_pos));
                    inword = false;
                }
            }
            else{
                if(!Character.isWhitespace(segment.charAt(pos))){
                    word_init_pos = pos;
                    inword = true;
                }
            }
        }
        if(inword){
            tokens.add(new SegmentToken(word_init_pos, segment.length()-1-word_init_pos));
        }
        
        this.original_text=segment;
        this.tokens=tokens;
    }
    
    public TokenizedSubSegment(TokenizedSegment seg, int position, int length) {
        super(((Segment)seg).getSentence(), position, length);
        this.original_text=seg.getOriginal_text();
        this.tokens=seg.getTokens();
    }
    
    /*public TokenizedSubSegment(String original_text, List<SegmentToken> tokens, String segment, int position, int length) {
        super(segment, position, length);
        this.original_text=original_text;
        this.tokens=tokens;
    }*/

    public TokenizedSubSegment(String original_text, List<SegmentToken> tokens, List<Word> segment, int position, int length) {
        super(segment, position, length);
        this.original_text=original_text;
        this.tokens=tokens;
    }

    public String getOriginal_text() {
        return original_text;
    }

    public void setOriginal_text(String original_text) {
        this.original_text = original_text;
    }

    public List<SegmentToken> getTokens() {
        return tokens;
    }

    public void setTokens(List<SegmentToken> tokens) {
        this.tokens = tokens;
    }
    
}
