/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.suggestions;

import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import java.util.Comparator;

/**
 *
 * @author mespla
 */
public class ScoredSuggestion extends TokenizedSegment{
    private double score;
    
    int[] alignment;
    
    public ScoredSuggestion(double score, TokenizedSegment segment){
        super(segment);
        this.score = score;
        this.alignment = null;
    }

    public int[] getAlignment() {
        return alignment;
    }

    public void setAlignment(int[] alignment) {
        this.alignment = alignment;
    }
    
    public ScoredSuggestion(double score){
        super(new TokenizedSegment("", null, ""));
        this.score = score;
    }

    public double getScore() {
        return score;
    }

    public void setScore(double position) {
        this.score = position;
    }
    
    class HighestComparator implements Comparator<ScoredSuggestion>{

        @Override
        public int compare(ScoredSuggestion o1, ScoredSuggestion o2) {
            if(o1==o2){
                return 0;
            } else if (o1==null){
                return -1;
            } else if (o2==null){
                return 1;
            } if (o1.getScore() >= o2.getScore()){
                return 1;
            } else
                return -1;
        }
        
    }
    
    class LowestComparator implements Comparator<ScoredSuggestion>{

        @Override
        public int compare(ScoredSuggestion o1, ScoredSuggestion o2) {
            if(o1==o2){
                return 0;
            } else if (o2==null){
                return -1;
            } else if (o1==null){
                return 1;
            } else if (o1.getScore() <= o2.getScore()){
                return 1;
            } else
                return -1;
        }
        
    }
}
