/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.suggestions;

import es.ua.dlsi.patch.patches.SegmentAlignment;
import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import java.util.Stack;

/**
 *
 * @author mespla
 */
public class SuggestionWithEditingInfo extends ScoredSuggestion{

    final static public int ED_UNCHANGED = 0; 
    final static public int ED_INSERTED = 1; 
    final static public int ED_DELETED = -1; 
    
    Stack<SegmentToken> words_deleted;
    Stack<SegmentToken> words_inserted;
    String textToShow;

    public Stack<SegmentToken> getWords_deleted() {
        return words_deleted;
    }

    public Stack<SegmentToken> getWords_inserted() {
        return words_inserted;
    }

    public String getTextToShow() {
        return textToShow;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    /*public SuggestionWithEditingInfo(double score, TokenizedSegment orig_segment,
            TokenizedSegment suggestion) {
        super(score, suggestion);
        words_deleted = new Stack<>();
        words_inserted = new Stack<>();
        
        SegmentAlignment bidir_alg = new SegmentAlignment(orig_segment, suggestion);
        
        StringBuilder sb = new StringBuilder();

        int last_edit = ED_UNCHANGED;
        int last_offset = 0;
        int last_orig_val = -1;
        int pos;
        int added_characters = 0;

        for(pos = 0; pos < bidir_alg.getAlignmentS2S1().length; pos++){
            if(bidir_alg.getAlignmentS2S1()[pos] == -1){
                SegmentToken tok;
                if(last_edit == ED_DELETED){
                    tok = words_inserted.pop();
                    tok.length = suggestion.tokens.get(pos).offset - tok.offset + suggestion.tokens.get(pos).length;
                }
                else{
                    tok = new SegmentToken(suggestion.tokens.get(pos).offset + added_characters, suggestion.tokens.get(pos).length);
                }
                this.words_inserted.add(tok);
            }
            else{
                int orig_val = bidir_alg.getAlignmentS2S1()[pos];
                if(orig_val-last_orig_val > 1){
                    sb.append(suggestion.original_text.substring(last_offset, suggestion.tokens.get(pos-1).offset+suggestion.tokens.get(pos-1).length));
                    sb.append(" ");
                    SegmentToken inittok = orig_segment.getTokens().get(last_orig_val+1);
                    SegmentToken lasttok = orig_segment.getTokens().get(bidir_alg.getAlignmentS2S1()[pos]-1);
                    
                    SegmentToken tok = new SegmentToken(sb.length(), lasttok.offset-inittok.offset + lasttok.length+1);
                    this.words_deleted.add(tok);
                    
                    sb.append(orig_segment.original_text.substring(inittok.offset, lasttok.offset+lasttok.length));
                    sb.append(" ");

                    last_offset = suggestion.tokens.get(pos).offset;
                    
                    
                    added_characters+=lasttok.offset-inittok.offset + lasttok.length+1;
                    last_edit = ED_INSERTED;
                }
                else{
                    last_edit = ED_UNCHANGED;
                }
                last_orig_val = orig_val;
            }
        }
        if(suggestion.tokens.get(pos-1).offset > last_offset){
            sb.append(suggestion.original_text.substring(last_offset, suggestion.tokens.get(pos-1).offset+suggestion.tokens.get(pos-1).length));
        }
        if(orig_segment.tokens.size() > last_orig_val+1){
            SegmentToken inittok = orig_segment.getTokens().get(last_orig_val+1);
            SegmentToken lasttok = orig_segment.getTokens().get(orig_segment.getTokens().size()-1);
            sb.append(orig_segment.original_text.substring(inittok.offset, lasttok.offset+lasttok.length));
        }
        this.textToShow = sb.toString();
    }*/
    
    public SuggestionWithEditingInfo(double score, TokenizedSegment orig_segment,
            TokenizedSegment suggestion) {
        super(score, suggestion);
        words_deleted = new Stack<>();
        words_inserted = new Stack<>();
        
        SegmentAlignment bidir_alg = new SegmentAlignment(orig_segment, suggestion, true);
        
        StringBuilder sb = new StringBuilder();

        int last_edit = ED_UNCHANGED;
        int last_offset = 0;
        int last_orig_val = -1;
        int pos;
        int added_characters = 0;

        for(pos = 0; pos < bidir_alg.getAlignmentS1S2().length; pos++){
            if(bidir_alg.getAlignmentS1S2()[pos] == -1){
                SegmentToken tok;
                if(last_edit == ED_DELETED){
                    tok = words_deleted.pop();
                    tok.length = orig_segment.getTokens().get(pos).offset - tok.offset + orig_segment.getTokens().get(pos).length;
                }
                else{
                    tok = new SegmentToken(orig_segment.getTokens().get(pos).offset + added_characters, orig_segment.getTokens().get(pos).length);
                }
                this.words_deleted.add(tok);
            }
            else{
                int orig_val = bidir_alg.getAlignmentS1S2()[pos];
                if(orig_val-last_orig_val > 1){
                    sb.append(orig_segment.getOriginal_text().substring(last_offset, orig_segment.getTokens().get(pos-1).offset+orig_segment.getTokens().get(pos-1).length));
                    sb.append(" ");
                    SegmentToken inittok = suggestion.getTokens().get(last_orig_val+1);
                    SegmentToken lasttok = suggestion.getTokens().get(bidir_alg.getAlignmentS1S2()[pos]-1);
                    
                    SegmentToken tok = new SegmentToken(sb.length(), lasttok.offset-inittok.offset + lasttok.length+1);
                    this.words_inserted.add(tok);
                    
                    sb.append(suggestion.getOriginal_text().substring(inittok.offset, lasttok.offset+lasttok.length));
                    sb.append(" ");

                    last_offset = orig_segment.getTokens().get(pos).offset;
                    
                    
                    added_characters+=lasttok.offset-inittok.offset + lasttok.length+1;
                    last_edit = ED_INSERTED;
                }
                else{
                    last_edit = ED_UNCHANGED;
                }
                last_orig_val = orig_val;
            }
        }
        //if(orig_segment.tokens.get(pos-1).offset >= last_offset){
        if(orig_segment.getTokens().get(pos-1).offset >= last_offset){
            sb.append(orig_segment.getOriginal_text().substring(last_offset, orig_segment.getOriginal_text().length()));
            //sb.append(orig_segment.original_text.substring(last_offset, orig_segment.tokens.get(pos-1).offset+orig_segment.tokens.get(pos-1).length));
        }
        if(suggestion.getTokens().size() > last_orig_val+1){
            SegmentToken inittok = suggestion.getTokens().get(last_orig_val+1);
            //SegmentToken lasttok = suggestion.getTokens().get(suggestion.getTokens().size()-1);
            sb.append(suggestion.getOriginal_text().substring(inittok.offset, suggestion.getOriginal_text().length()-1));
            //sb.append(suggestion.original_text.substring(inittok.offset, lasttok.offset+lasttok.length));
        }
        this.textToShow = sb.toString();
    }
    
}
