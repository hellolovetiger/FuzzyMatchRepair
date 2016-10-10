/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.suggestions;

import es.ua.dlsi.patch.patches.PatchOperator;
import es.ua.dlsi.patch.patches.PatchedSegment;
import es.ua.dlsi.patch.patches.SegmentAlignment;
import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import es.ua.dlsi.patch.tokenisation.TokenizedSubSegment;
import es.ua.dlsi.segmentation.Segment;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author mespla
 */
public class RankingSuggestions {
    
    public static SortedSet<ScoredSuggestion> LengthRanker(TokenizedSegment sourceseg, Set<PatchedSegment> suggestions){
        double sourcelen=(double)sourceseg.size();
        
        ScoredSuggestion emptysuggestion=new ScoredSuggestion(0.0);
        Comparator<ScoredSuggestion> comparator = emptysuggestion.new HighestComparator();
        
        SortedSet<ScoredSuggestion> scoredsuggestions=new TreeSet<>(comparator);
        
        for(TokenizedSegment s: suggestions){
            int suggestionlen=s.size();
            
            double score=Math.abs(1.0-(sourcelen/suggestionlen));
            scoredsuggestions.add(new ScoredSuggestion(score, s));
        }
        
        return scoredsuggestions;
    }
    
    public static SortedSet<ScoredSuggestion> EditedWordsRanker(TokenizedSegment sourceseg,
            TokenizedSegment tu_source, TokenizedSegment tu_target, Set<PatchedSegment> suggestions){
        double sdistance = Segment.EditDistance(tu_source.getSentenceCodes(), sourceseg.getSentenceCodes(), null, false)+1;

        ScoredSuggestion emptysuggestion=new ScoredSuggestion(0.0);
        Comparator<ScoredSuggestion> comparator = emptysuggestion.new HighestComparator();

        SortedSet<ScoredSuggestion> scoredsuggestions=new TreeSet<>(comparator);

        for(TokenizedSegment s: suggestions){
            int[] alignment=new int[s.getSentenceCodes().size()];
            int suggestiondistance=SegmentAlignment.EditDistance(tu_target.getSentenceCodes(), s.getSentenceCodes(), alignment, null, false)+1;

            double score=Math.abs(1.0-(sdistance/suggestiondistance));
            ScoredSuggestion suggestion=new ScoredSuggestion(score, s);
            suggestion.setAlignment(alignment);
            scoredsuggestions.add(new ScoredSuggestion(score, suggestion));
        }

        return scoredsuggestions;
    }
    
    public static SortedSet<ScoredSuggestion> OverlappingRanker(TokenizedSegment sourceseg,
            TokenizedSegment tu_source, TokenizedSegment tu_target, Set<PatchedSegment> suggestions){

        ScoredSuggestion emptysuggestion=new ScoredSuggestion(0.0);
        Comparator<ScoredSuggestion> comparator = emptysuggestion.new HighestComparator();

        SortedSet<ScoredSuggestion> scoredsuggestions=new TreeSet<>(comparator);
        
        for(PatchedSegment s: suggestions){
            if(!s.getPatches().isEmpty()){
                double score = 0.0;
                double opperators_length = 0.0;
                double num_missaligned_words = 0.0;
                for(PatchOperator po: s.getPatches()){
                
                    TokenizedSubSegment subseg = tu_target.getTokenizedSubSegment(po.getTaus().getPhrase2().getPosition(), po.getTaus().getPhrase2().getPosition()+po.getTaus().getPhrase2().getLength());
                    int[] alignment_array = new int[po.getTaus().getPhrase2().size()];
                    SegmentAlignment.EditDistance(subseg.getSentenceCodes(), po.getTaus().getPhrase2().getSentenceCodes(), alignment_array, null, false);
                    
                    //List<Integer> missaligned_words = Arrays.stream(alignment_array).boxed().collect(Collectors.toList());
                    //double num_missaligned_words = (double)Collections.frequency(missaligned_words, -1);
                    for(int num: alignment_array)
                        if(num == -1)
                            num_missaligned_words++;
                    opperators_length += (double)(po.getTaus().getAlignment().getAlignmentS2S1().length);
                }
                if(opperators_length != 0)
                    score += num_missaligned_words/opperators_length;
                //score /= s.getPatches().size();

                ScoredSuggestion suggestion=new ScoredSuggestion(score, s);
                scoredsuggestions.add(new ScoredSuggestion(score, suggestion));
            }
        }
        return scoredsuggestions;
    }
    
    public static SortedSet<ScoredSuggestion> MeanLengthRanker(TokenizedSegment sourceseg,
            TokenizedSegment tu_source, TokenizedSegment tu_target, Set<PatchedSegment> suggestions){

        ScoredSuggestion emptysuggestion=new ScoredSuggestion(0.0);
        Comparator<ScoredSuggestion> comparator = emptysuggestion.new LowestComparator();

        SortedSet<ScoredSuggestion> scoredsuggestions=new TreeSet<>(comparator);
        
        for(PatchedSegment s: suggestions){
            if(!s.getPatches().isEmpty()){
                double score = 0.0;
                for(PatchOperator po: s.getPatches()){
                    score+=(double)po.getTaus().getPhrase2().size();
                }
                score /= s.getPatches().size();
                //score /= s.getPatches().size();

                ScoredSuggestion suggestion=new ScoredSuggestion(score, s);
                scoredsuggestions.add(new ScoredSuggestion(score, suggestion));
            }
        }

        return scoredsuggestions;
    }
    
    public static SortedSet<ScoredSuggestion> OverlappingFirstLengthSecondRanker(TokenizedSegment sourceseg,
            TokenizedSegment tu_source, TokenizedSegment tu_target, Set<PatchedSegment> suggestions){

        ScoredSuggestion emptysuggestion=new ScoredSuggestion(0.0);
        Comparator<ScoredSuggestion> comparator = emptysuggestion.new HighestComparator();

        SortedSet<ScoredSuggestion> scoredsuggestions=new TreeSet<>(comparator);
        
        for(PatchedSegment s: suggestions){
            if(!s.getPatches().isEmpty()){
                double primary_score = 0.0;
                double secondary_score = 0.0;
                double opperators_length = 0.0;
                double num_missaligned_words = 0.0;
                for(PatchOperator po: s.getPatches()){
                
                    TokenizedSubSegment subseg = tu_target.getTokenizedSubSegment(po.getTaus().getPhrase2().getPosition(), po.getTaus().getPhrase2().getPosition()+po.getTaus().getPhrase2().getLength());
                    int[] alignment_array = new int[po.getTaus().getPhrase2().size()];
                    SegmentAlignment.EditDistance(subseg.getSentenceCodes(), po.getTaus().getPhrase2().getSentenceCodes(), alignment_array, null, false);
                    
                    //List<Integer> missaligned_words = Arrays.stream(alignment_array).boxed().collect(Collectors.toList());
                    //double num_missaligned_words = (double)Collections.frequency(missaligned_words, -1);
                    for(int num: alignment_array)
                        if(num == -1)
                            num_missaligned_words++;
                    opperators_length += (double)(po.getTaus().getAlignment().getAlignmentS2S1().length);
                    
                    secondary_score+=(double)po.getTaus().getPhrase2().size();

                }
                if(opperators_length != 0)
                    primary_score += num_missaligned_words/opperators_length;
                secondary_score /= s.getPatches().size();
                //score /= s.getPatches().size();

                secondary_score = (1.0/secondary_score)*0.1;
                ScoredSuggestion suggestion=new ScoredSuggestion(primary_score+secondary_score, s);
                scoredsuggestions.add(suggestion);
            }
        }
        return scoredsuggestions;
    }
}
