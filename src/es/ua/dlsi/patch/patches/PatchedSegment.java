/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.patches;

import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import es.ua.dlsi.segmentation.Word;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 *
 * @author mespla
 */
public class PatchedSegment extends TokenizedSegment{

    private Set<PatchOperator> patches;
    
    public PatchedSegment(TokenizedSegment tok_sentence, Set<PatchOperator> patches) {
        super(tok_sentence);
        this.patches=patches;
    }
    
    public PatchedSegment(List<Word> tok_sentence, Set<PatchOperator> patches) {
        super(tok_sentence);
        this.patches=patches;
    }
    
    public PatchedSegment(String sentence, List<SegmentToken> tokens, List<Word> tok_sentence, Set<PatchOperator> patches) {
        super(sentence,tokens,tok_sentence);
        this.patches=patches;
    }

    public Set<PatchOperator> getPatches() {
        return patches;
    }

    public void setPatches(Set<PatchOperator> patches) {
        this.patches = patches;
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
        if (!Objects.equals(this.original_text, other.getOriginal_text())) {
            return false;
        }
        return true;
    }
}
