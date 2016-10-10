/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.patches;

import es.ua.dlsi.segmentation.Segment;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author mespla
 */
public class SegmentAlignment {
    private int[] alignment_s2_s1;
    private int[] alignment_s1_s2;
    
    public SegmentAlignment(Segment s1, Segment s2){
        alignment_s2_s1=new int[s2.size()];
        alignment_s1_s2=new int[s1.size()];
        
        EditDistance(new Segment(s1.toString()).getSentenceCodes(), new Segment(s2.toString()).getSentenceCodes(), alignment_s2_s1, null, false);
        InverseAlignment(alignment_s2_s1, alignment_s1_s2);
    }
    
    public SegmentAlignment(Segment s1, Segment s2, boolean keepcapitals){
        alignment_s2_s1=new int[s2.size()];
        alignment_s1_s2=new int[s1.size()];
        
        if(keepcapitals)
            EditDistance(new Segment(s1.toString(), true).getSentenceCodes(), new Segment(s2.toString(), true).getSentenceCodes(), alignment_s2_s1, null, false);
        else
            EditDistance(new Segment(s1.toString()).getSentenceCodes(), new Segment(s2.toString()).getSentenceCodes(), alignment_s2_s1, null, false);
        InverseAlignment(alignment_s2_s1, alignment_s1_s2);
    }
    
    public void MoveAlignmentS2S1(int movement){
        for(int pos = 0; pos<this.alignment_s2_s1.length; pos++){
            if(this.alignment_s2_s1[pos]!=-1)
                this.alignment_s2_s1[pos]+=movement;
        }
    }
    
    public void MoveAlignmentS1S2(int movement){
        for(int pos = 0; pos<this.alignment_s1_s2.length; pos++){
            if(this.alignment_s1_s2[pos]!=-1)
                this.alignment_s1_s2[pos]+=movement;
        }
    }
    
    public int[] getAlignmentS2S1(){
        return alignment_s2_s1;
    }
    
    public int[] getAlignmentS1S2(){
        return alignment_s1_s2;
    }
    
    static public int EditDistance(List<Integer> s1, List<Integer> s2, int[] alignment, Integer maxval, boolean debug){
        int[][] d=new int[s1.size()+1][s2.size()+1];
        int i, j;

        //int maxi, maxj;
        if(maxval==null)
            maxval=s1.size()+s2.size();

        for(i=0; i<s1.size()+1 && i<=maxval;i++){
            d[i][0]=i;
        }

        for(i=0; i<s2.size()+1 && i<=maxval;i++){
            d[0][i]=i;
        }

        for(j=1;j<s2.size()+1;j++){
            int initi;
            int minval;
            if(j>maxval){
                initi=(j-maxval);
                if(s1.get(initi-1).equals(s2.get(j-1)))
                    d[initi][j]=d[initi-1][j-1];
                else{
                    if(d[initi-1][j-1]<d[initi][j-1])
                        d[initi][j]=d[initi-1][j-1]+1;
                    else
                        d[initi][j]=d[initi][j-1]+1;
                }
                minval=d[initi][j];
                initi++;
            }else{
                minval=Integer.MAX_VALUE;
                initi=1;
            }
            for(i=initi;i<s1.size()+1 && i<j+maxval;i++){
                if(s1.get(i-1).equals(s2.get(j-1)))
                    d[i][j]=d[i-1][j-1];
                else{
                    if(d[i-1][j]<d[i][j-1]){
                        if(d[i-1][j]<d[i-1][j-1])
                            d[i][j]=d[i-1][j]+1;
                        else
                            d[i][j]=d[i-1][j-1]+1;
                    }
                    else{
                        if(d[i][j-1]<d[i-1][j-1])
                            d[i][j]=d[i][j-1]+1;
                        else
                            d[i][j]=d[i-1][j-1]+1;
                    }
                }
                if(d[i][j]<minval)
                    minval=d[i][j];
            }
            if(i<s1.size()+1){
                if(s1.get(i-1).equals(s2.get(j-1)))
                    d[i][j]=d[i-1][j-1];
                else{
                    if(d[i-1][j-1]<d[i-1][j])
                        d[i][j]=d[i-1][j-1]+1;
                    else
                        d[i][j]=d[i-1][j]+1;
                }
                if(d[i][j]<minval)
                    minval=d[i][j];
            }
            if(minval>=maxval+1){
                if(debug){
                    for(j=0;j<s2.size()+1;j++){
                        for(i=0;i<s1.size();i++){
                            System.out.print(d[i][j]);
                            System.out.print("\t");
                        }
                        System.out.println(d[i][j]);
                    }
                }
                return maxval+1;
            }
        }
        if(alignment!=null){
            Arrays.fill(alignment, -1);
            i=s1.size();
            j=s2.size();
            
            
            while(j>0){
                if(i==0){
                    alignment[j-1]=-1;
                    j--;
                }
                else{
                    if(s1.get(i-1).equals(s2.get(j-1))){
                        alignment[j-1]=i-1;
                        i--;
                        j--;
                    }
                    else{
                        if(d[i-1][j-1]<d[i-1][j]){
                            if(d[i-1][j-1]<d[i][j-1]){
                                i--;
                                j--;
                            }
                            else{
                                j--;
                            }
                        }
                        else{
                            if(d[i-1][j]<d[i][j-1]){
                                if(s1.get(i-1).equals(s2.get(j-1))){
                                    alignment[j-1]=i-1;
                                }
                                i--;
                            }
                            else{
                                j--;
                            }
                        }
                    }
                }
            }
        }

        if(debug){
            for(j=0;j<s2.size()+1;j++){
                for(i=0;i<s1.size();i++){
                    System.out.print(d[i][j]);
                    System.out.print("\t");
                }
                System.out.println(d[i][j]);
            }
        }

        return d[s1.size()][s2.size()];
    }
    
    private static int[] InverseAlignment(int[] alignemnt, int[] new_alignment){
        Arrays.fill(new_alignment, -1);
        
        for(int oldindex=0; oldindex<alignemnt.length; oldindex++){
            if(alignemnt[oldindex]!=-1 && alignemnt[oldindex] < new_alignment.length){
                new_alignment[alignemnt[oldindex]]=oldindex;
            }
        }
        return new_alignment;
    }
}
