/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.patches;

import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import es.ua.dlsi.patch.tokenisation.TokenizedSubSegment;
import es.ua.dlsi.segmentation.Segment;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @author mespla
 */
public class SubSegmentPair {
    /** First phrase pair of the pair */
    private TokenizedSubSegment phrase1;
    /** Second phrase pair of the pair */
    private TokenizedSubSegment phrase2;
    /** Alignemnt between the two sub-segments */
    private SegmentAlignment alignment;

    @Override
    public String toString() {
        
        return "SubSegmentPair{phrase1=" + phrase1 + " (init=" + String.valueOf(phrase1.getPosition()) + " len=" + String.valueOf(phrase1.getLength()) + "), phrase2=" + phrase2  + " (init=" + String.valueOf(phrase2.getPosition()) + " len=" + String.valueOf(phrase2.getLength()) + '}';
    }

    public SubSegmentPair(TokenizedSubSegment phrase1, TokenizedSubSegment phrase2) {
        this.phrase1 = phrase1;
        this.phrase2 = phrase2;
        this.alignment = new SegmentAlignment(phrase1, phrase2);
        alignment.MoveAlignmentS1S2(phrase1.getPosition());
        alignment.MoveAlignmentS2S1(phrase2.getPosition());
        
    }
    

    public SubSegmentPair(TokenizedSubSegment phrase1, TokenizedSubSegment phrase2, SegmentAlignment alignment) {
        this.phrase1 = phrase1;
        this.phrase2 = phrase2;
        this.alignment = alignment;
    }

    public SubSegmentPair(TokenizedSegment seg1, TokenizedSegment seg2) {
        TokenizedSubSegment p1=new TokenizedSubSegment(seg1.getOriginal_text(), seg1.getTokens(), seg1.getSentence(), 0, seg1.size());
        TokenizedSubSegment p2=new TokenizedSubSegment(seg2.getOriginal_text(), seg2.getTokens(), seg2.getSentence(), 0, seg2.size());
        this.phrase1 = p1;
        this.phrase2 = p2;
        this.alignment = new SegmentAlignment(seg1, seg2);
    }

    public TokenizedSubSegment getPhrase1() {
        return phrase1;
    }

    public void setPhrase1(TokenizedSubSegment phrase1) {
        this.phrase1 = phrase1;
    }

    public TokenizedSubSegment getPhrase2() {
        return phrase2;
    }

    public void setPhrase2(TokenizedSubSegment phrase2) {
        this.phrase2 = phrase2;
    }
    
    public SegmentAlignment getAlignment(){
        return this.alignment;
    }
    
    public String getAlignmentString(){
        StringBuilder sb=new StringBuilder("S1_S2=[");
        for(int i: this.alignment.getAlignmentS1S2()){
            sb.append(i);
            sb.append(" ");
        }
        sb.append("], S2_S1=[");
        for(int i: this.alignment.getAlignmentS2S1()){
            sb.append(i);
            sb.append(" ");
        }
        sb.append("]");
        return sb.toString();
    }
    
    public void setAlignment(SegmentAlignment alignment){
        this.alignment=alignment;
    }
    
    /*static public List<SubSegmentPair> MissMatchingSubSegmentPairs(Segment s1, Segment s2, int maxlen){
        int[] alignment=new int[s2.size()];
        SegmentAlg.EditDistance(s1.getSentenceCodes(), s2.getSentenceCodes(), alignment, null, false);
        return MissMatchingSubSegmentPairs(s1,s2,alignment,maxlen);
    }*/
    
    static private List<SubSegmentPair> OneDirectionMissMatchingSubSegmentPairs(
            TokenizedSegment s1, TokenizedSegment s2, int[] alignment, int[] inverse_alignment,
            int maxlen, boolean reversepair){

        List<SubSegmentPair> exit=new LinkedList<>();

        for(int s2index=0; s2index<alignment.length; s2index++){
            int s1index = alignment[s2index];
            //If a missaligned word is detected in S2
            if(s1index==-1){
                //Start collecting sub-segments with length 2 or more that include this missaligned word
                for(int len=2; len<=maxlen; len++){
                    //Most left-position possible including the missaligned word
                    int pos_min=s2index-(len-1);
                    //If this position is out of the segment, start from the first word
                    if(pos_min<0)
                        pos_min=0;
                    //Keep iterating while: a) the beggining of the sub-segment is lower or equal than the position of the missaligned word;
                    //b) the end of the sub-segment is higher or equal than the position of the missaligned word, and c) neither higher than the end of the segment
                    for(int sub_seg_index=pos_min; sub_seg_index<=s2index &&
                            sub_seg_index+len>=s2index && sub_seg_index+len<=s2.size(); sub_seg_index++){
                        //Building the sub-segment from s2 containing the missaligned word
                        TokenizedSubSegment sub_seg_s2 = s2.getTokenizedSubSegment(sub_seg_index, sub_seg_index+len);
                        
                        //Collection of indexes collected from the counter-segment
                        int[] alg_array=Arrays.copyOfRange(alignment, sub_seg_index, sub_seg_index+len);
                        Set<Integer> index_set=Arrays.stream(alg_array).boxed().collect(Collectors.toSet());
                        //Discarding missalignments
                        index_set.remove(-1);
                        
                        //If the segment is aligned with any word in the other side
                        if(!index_set.isEmpty()){
                            //Adding the range of words aligned
                            int min_index=Collections.min(index_set);
                            int max_index=Collections.max(index_set);
                            TokenizedSubSegment sub_seg_s1=s1.getTokenizedSubSegment(min_index, max_index+1);
                            if (reversepair)
                                exit.add(new SubSegmentPair(sub_seg_s2, sub_seg_s1));
                            else
                                exit.add(new SubSegmentPair(sub_seg_s1, sub_seg_s2));
                            
                            //Now: checking if in the other side there are missaligned words as well
                            int index_moving_left=min_index-1;
                            while(index_moving_left>=0 && inverse_alignment[index_moving_left]==-1){
                                sub_seg_s1=s1.getTokenizedSubSegment(index_moving_left, max_index+1);
                                if (reversepair)
                                    exit.add(new SubSegmentPair(sub_seg_s2, sub_seg_s1));
                                else
                                    exit.add(new SubSegmentPair(sub_seg_s1, sub_seg_s2));
                                index_moving_left--;
                            }
                            
                            int index_moving_right=max_index+1;
                            while(index_moving_right<inverse_alignment.length && inverse_alignment[index_moving_right]==-1){
                                sub_seg_s1=s1.getTokenizedSubSegment(min_index, index_moving_right+1);
                                if (reversepair)
                                    exit.add(new SubSegmentPair(sub_seg_s2, sub_seg_s1));
                                else
                                    exit.add(new SubSegmentPair(sub_seg_s1, sub_seg_s2));
                                index_moving_right++;
                            }
                        }
                    }
                }
            }
        }
        return exit;
    }
    
    static public List<SubSegmentPair> MissMatchingSubSegmentPairs(TokenizedSegment s1, TokenizedSegment s2, int maxlen){
        List<SubSegmentPair> exit=new LinkedList<>();
        
        SegmentAlignment alignment= new SegmentAlignment(s1, s2);
        
        exit.addAll(OneDirectionMissMatchingSubSegmentPairs(s1, s2, alignment.getAlignmentS2S1(), alignment.getAlignmentS1S2(), maxlen, false));
        exit.addAll(OneDirectionMissMatchingSubSegmentPairs(s2, s1, alignment.getAlignmentS1S2(), alignment.getAlignmentS2S1(), maxlen, true));

        return exit;
    }
    
    public Set<Integer> MissmatchingPositionsPhrase1(){
        Set<Integer> exit = new HashSet();
        
        for (int pos=0; pos<alignment.getAlignmentS1S2().length; pos++){
            if(alignment.getAlignmentS1S2()[pos]==-1)
                exit.add(pos+this.phrase1.getPosition());
        }
        return exit;
    }
    
    public Set<Integer> MissmatchingPositionsPhrase2(){
        Set<Integer> exit = new HashSet();
        
        for (int pos=0; pos<alignment.getAlignmentS2S1().length; pos++){
            if(alignment.getAlignmentS2S1()[pos]==-1)
                exit.add(pos+this.phrase2.getPosition());
        }
        return exit;
    }
    
    /*static public List<SubSegmentPair> MatchingSubSegmentPairs(Segment s1, Segment s2, int[] matches){
        
        List<SubSegmentPair> exit=new LinkedList<>();
        int init_s1_subseg=0;
        int init_s2_subseg=0;
        int end_s1_subseg=-1;
        int end_s2_subseg=-1;
        
        int position;
        for(position=0; position<matches.length; position++){
            
            //If unaligned position found (jump in segment1)
            if(matches[position]==-1){
                if(end_s1_subseg>=init_s1_subseg && end_s2_subseg>=init_s2_subseg){
                    SubSegment ss1=new SubSegment(s1.getSentence().subList(init_s1_subseg, end_s1_subseg+1),
                            init_s1_subseg, end_s1_subseg-init_s1_subseg);
                    SubSegment ss2=new SubSegment(s2.getSentence().subList(init_s2_subseg, end_s2_subseg+1),
                            init_s2_subseg, end_s2_subseg-init_s2_subseg);
                    exit.add(new SubSegmentPair(ss1, ss2));
                }
                
                while(position<matches.length && matches[position]==-1){
                    position++;
                }
                if(position<matches.length)
                    end_s1_subseg=init_s1_subseg=matches[position];
                end_s2_subseg=init_s2_subseg=position;

            }
            else{
                //If unaligned position found (jump in segment2)
                if(matches[position]-end_s1_subseg>1){
                    SubSegment ss1=new SubSegment(s1.getSentence().subList(init_s1_subseg, end_s1_subseg+1),
                            init_s1_subseg, end_s1_subseg-init_s1_subseg);
                    SubSegment ss2=new SubSegment(s2.getSentence().subList(init_s2_subseg, end_s2_subseg+1),
                            init_s2_subseg, end_s2_subseg-init_s2_subseg);
                    exit.add(new SubSegmentPair(ss1, ss2));

                    end_s1_subseg=init_s1_subseg=matches[position];
                    end_s2_subseg=init_s2_subseg=position;
                }
                else{
                    end_s1_subseg=matches[position];
                    end_s2_subseg=position;
                }
            }
        }
        
        if(end_s1_subseg>=init_s1_subseg && end_s2_subseg>=init_s2_subseg && end_s2_subseg<matches.length){
            SubSegment ss1=new SubSegment(s1.getSentence().subList(init_s1_subseg, end_s1_subseg+1),
                    init_s1_subseg, end_s1_subseg-init_s1_subseg);
            SubSegment ss2=new SubSegment(s2.getSentence().subList(init_s2_subseg, end_s2_subseg+1),
                    init_s2_subseg, end_s2_subseg-init_s2_subseg);
            exit.add(new SubSegmentPair(ss1, ss2));
        }
        
        return exit;
    }*/
    
    public static void main(String[] args) {
        /*Segment s2=new Segment("a a a b b");
        Segment s1=new Segment("a a b b");*/
        /*Segment s2=new Segment("a a a");
        Segment s1=new Segment("a a b b");*/
        /*Segment s1=new Segment("b b b");
        Segment s2=new Segment("a a b b");*/
        Segment s2=new Segment("a b a b");
        Segment s1=new Segment("a a b b");
        SegmentAlignment alignment=new SegmentAlignment(s1,s2);
        
        for (int a: alignment.getAlignmentS2S1()){
            System.out.print(a);
            System.out.print(" ");
        }
        System.out.println();
        
        /*List<SubSegmentPair> phrase_pairs=MatchingSubSegmentPairs(s1, s2, alignment);
        for (SubSegmentPair p: phrase_pairs){
            System.out.println(p);
        }*/
    }
}
