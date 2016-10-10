/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.tokenisation;

import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.segmentation.Word;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

/**
 *
 * @author mespla
 */
public class TokenizedSegment extends Segment{
    
    protected String original_text;

    protected List<SegmentToken> tokens;
    
    public TokenizedSegment(TokenizedSegment tok_sentence) {
        super(tok_sentence);
        this.tokens = tok_sentence.tokens;
        this.original_text = tok_sentence.original_text;
    }
    
    
    public TokenizedSegment(String sentence, List<SegmentToken> tokens, String tok_sentence) {
        super(tok_sentence);
        this.tokens = tokens;
        this.original_text=sentence;
    }
    
    public TokenizedSegment(String sentence, List<SegmentToken> tokens, List<Word> tok_sentence) {
        super(tok_sentence);
        this.tokens = tokens;
        this.original_text=sentence;
    }
    
    public TokenizedSegment(String sentence, List<SegmentToken> tokens, String tok_sentence, boolean keepcapitals) {
        super(tok_sentence, keepcapitals);
        this.tokens = tokens;
        this.original_text=sentence;
    }
    
    public TokenizedSegment(List<Word> words) {
        super(words);
        String tok_sentence = (new Segment(words)).toString();
        int word_init_pos = 0;
        List<SegmentToken> tokens= new LinkedList<>();
        boolean inword = false;
        for(int pos = 0; pos < tok_sentence.length(); pos++){
            if(inword){
                if(Character.isSpaceChar(tok_sentence.charAt(pos))){
                    tokens.add(new SegmentToken(word_init_pos, pos-word_init_pos));
                    inword = false;
                }
            }
            else{
                if(!Character.isSpaceChar(tok_sentence.charAt(pos))){
                    word_init_pos = pos;
                    inword = true;
                }
            }
        }
        if(inword){
            tokens.add(new SegmentToken(word_init_pos, tok_sentence.length()-1-word_init_pos));
        }
        this.tokens = tokens;
        this.original_text=tok_sentence;
    }
    
    public TokenizedSegment(String tok_sentence, boolean keepcapitals) {
        super(tok_sentence, keepcapitals);

        int word_init_pos = 0;
        List<SegmentToken> tokens= new LinkedList<>();
        boolean inword = false;
        for(int pos = 0; pos < tok_sentence.length(); pos++){
            if(inword){
                if(Character.isSpaceChar(tok_sentence.charAt(pos))){
                    tokens.add(new SegmentToken(word_init_pos, pos-word_init_pos));
                    inword = false;
                }
            }
            else{
                if(!Character.isSpaceChar(tok_sentence.charAt(pos))){
                    word_init_pos = pos;
                    inword = true;
                }
            }
        }
        if(inword){
            tokens.add(new SegmentToken(word_init_pos, tok_sentence.length()-1-word_init_pos));
        }
        this.tokens = tokens;
        this.original_text=tok_sentence;
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
    
    public TokenizedSubSegment getTokenizedSubSegment(int indexinit, int indexend){
        List<SegmentToken> toklist=this.tokens.subList(indexinit, indexend);
        String substring=SegmentToken.UntokenizedSegmentPiece(this.original_text, toklist);
        return new TokenizedSubSegment(substring, toklist,
                this.sentence.subList(indexinit, indexend), indexinit, indexend-indexinit);
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 41 * hash + Objects.hashCode(this.original_text);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TokenizedSegment other = (TokenizedSegment) obj;
        if (!Objects.equals(this.original_text, other.original_text)) {
            return false;
        }
        return true;
    }
    
}
