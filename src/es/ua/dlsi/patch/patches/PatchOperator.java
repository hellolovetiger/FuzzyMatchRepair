/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.patches;

import es.ua.dlsi.segmentation.Segment;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * @author mespla
 */
public class PatchOperator {
    private SubSegmentPair sigmas;
    private SubSegmentPair taus;

    public SubSegmentPair getSigmas() {
        return sigmas;
    }

    public void setSigmas(SubSegmentPair sigmas) {
        this.sigmas = sigmas;
    }

    public SubSegmentPair getTaus() {
        return taus;
    }

    public void setTaus(SubSegmentPair taus) {
        this.taus = taus;
    }
    
    public PatchOperator(SubSegmentPair sigmas, SubSegmentPair taus){
        this.sigmas=sigmas;
        this.taus=taus;
    }
    
    @Override
    public String toString(){
        StringBuilder sb=new StringBuilder(sigmas.toString());
        sb.append("\n");
        sb.append(taus.toString());
        return sb.toString();
    }
    
    
    public boolean isValidOpperator(Segment targetseg){
        return (taus.getPhrase1().Appears(targetseg) != null);
    }
    
    public boolean CompatibleSigma(PatchOperator po){
        /*Set<Integer> intersection1=new HashSet<>(sigmas.MissmatchingPositionsPhrase1());
        intersection1.retainAll(po.sigmas.MissmatchingPositionsPhrase1());
        Set<Integer> intersection2=new HashSet<>(sigmas.MissmatchingPositionsPhrase2());
        intersection2.retainAll(po.sigmas.MissmatchingPositionsPhrase2());*/
        Set<Integer> union1 = new HashSet<>(sigmas.MissmatchingPositionsPhrase1());
        union1.addAll(new HashSet<>(sigmas.MissmatchingPositionsPhrase2()));
        
        Set<Integer> union2 = new HashSet<>(po.sigmas.MissmatchingPositionsPhrase1());
        union1.addAll(new HashSet<>(po.sigmas.MissmatchingPositionsPhrase2()));
        
        Set<Integer> intersection=new HashSet<>(union1);
        intersection.retainAll(union2);
                
        //return (intersection1.isEmpty() && intersection2.isEmpty());
        return (intersection.isEmpty());
    }
    
    
    
    public boolean CompatibleTau(PatchOperator po){
        //Guessing which positions in the first opperator are edited
        Set<Integer> union1 = new HashSet<>(taus.MissmatchingPositionsPhrase1());
        union1.addAll(new HashSet<>(taus.MissmatchingPositionsPhrase2()));
        
        //Guessing which positions in the second opperator are edited
        Set<Integer> union2 = new HashSet<>(po.taus.MissmatchingPositionsPhrase1());
        union2.addAll(new HashSet<>(po.taus.MissmatchingPositionsPhrase2()));
        
        //Augmenting the collection with surrounding positions to avoid opperations
        //for which we are not sure if they could be overlapping; this is done only
        //in one of the collections (the larger one)
        Set<Integer> augmented_union1, augmented_union2;
        if(union1.size() > union2.size()){
            augmented_union1 = new HashSet<>();
            for(int i: union1){
                augmented_union1.add(i);
                augmented_union1.add(i+1);
                augmented_union1.add(i-1);
            }
            augmented_union2=union2;
        }
        else{
            augmented_union1=union1;
            augmented_union2 = new HashSet<>();
            for(int i: union2){
                augmented_union2.add(i);
                augmented_union2.add(i+1);
                augmented_union2.add(i-1);
            }
        }
        
        //Detecting overlapping opperations
        Set<Integer> intersection=new HashSet<>(augmented_union1);
        intersection.retainAll(augmented_union2);
        
        return (intersection.isEmpty());
    }
}
