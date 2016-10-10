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
        Set<Integer> intersection1=new HashSet<>(sigmas.MissmatchingPositionsPhrase1());
        intersection1.retainAll(po.sigmas.MissmatchingPositionsPhrase1());
        Set<Integer> intersection2=new HashSet<>(sigmas.MissmatchingPositionsPhrase2());
        intersection2.retainAll(po.sigmas.MissmatchingPositionsPhrase2());
        
        return (intersection1.isEmpty() && intersection2.isEmpty());
    }
    
    public boolean CompatibleTau(PatchOperator po){
        Set<Integer> intersection1=new HashSet<>(taus.MissmatchingPositionsPhrase1());
        intersection1.retainAll(po.taus.MissmatchingPositionsPhrase1());
        Set<Integer> intersection2=new HashSet<>(taus.MissmatchingPositionsPhrase2());
        intersection2.retainAll(po.taus.MissmatchingPositionsPhrase2());
        
        return (intersection1.isEmpty() && intersection2.isEmpty());
    }
}
