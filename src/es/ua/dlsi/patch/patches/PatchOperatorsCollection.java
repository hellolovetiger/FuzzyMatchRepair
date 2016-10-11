/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.patches;

import es.ua.dlsi.patch.tokenisation.SegmentToken;
import es.ua.dlsi.patch.tokenisation.TokenizedSegment;
import es.ua.dlsi.patch.tokenisation.TokenizedSubSegment;
import es.ua.dlsi.patch.tokenisation.FMRTokenizer;
import es.ua.dlsi.patch.translation.GenericTranslator;
import es.ua.dlsi.segmentation.Segment;
import es.ua.dlsi.segmentation.Word;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

/**
 *
 * @author mespla
 */
public class PatchOperatorsCollection {
    
    final private TokenizedSegment translation;
    final private Set<PatchOperator> patch_operators;
    final private FMRTokenizer tokenizer;
    private boolean interrupt;
    
    public void Interrupt(){
        this.interrupt = true;
    }
    
    public PatchOperatorsCollection(TokenizedSegment segment, TokenizedSegment tu_source, TokenizedSegment tu_target,
            int maxlen, boolean grounded_bothsides, FMRTokenizer tokenizer, GenericTranslator translator ){
        this.interrupt = false;
        this.translation=tu_target;
        this.patch_operators=new HashSet<PatchOperator>();
        this.tokenizer = tokenizer;
        if(segment != null && tu_source!= null && tu_target!=null){
            List<SubSegmentPair> missmatching_source_subsegments=
                    SubSegmentPair.MissMatchingSubSegmentPairs(segment, tu_source, maxlen);
            Set<String> sources= new HashSet<String>();
            for(SubSegmentPair ssp: missmatching_source_subsegments){
                sources.add(ssp.getPhrase1().getOriginal_text());
                sources.add(ssp.getPhrase2().getOriginal_text());
            }

            Map<String,Set<String>> dictionary= translator.getTranslation(sources);

            Map<String, Set<TokenizedSegment>> tokdictionary = new HashMap<String, Set<TokenizedSegment>>();

            for (Map.Entry<String,Set<String>> entry: dictionary.entrySet()){
                //Token[] tokens = tokenizer.tokenizeWords(match.source, StemmingMode.MATCHING);
                for(String trans: entry.getValue()){
                    List<Word> words = new LinkedList<>();
                    List<SegmentToken> tokenlist = tokenizer.Tokenize(trans, words);
                    TokenizedSegment tprova = new TokenizedSegment(trans, tokenlist, words);
                    /*System.out.println(tprova);
                    System.out.println(trans);
                    for(SegmentToken stok: tprova.getTokens()){
                        System.out.print("\t'"+tprova.getOriginal_text().subSequence(stok.offset, stok.offset+stok.length)+"'");
                        System.out.print("\t");
                        System.out.print(stok.offset);
                        System.out.print("\t");
                        System.out.print(stok.offset+stok.length);
                        System.out.println();
                    }*/
                    if(tokdictionary.containsKey(entry.getKey()))
                        tokdictionary.get(entry.getKey()).add(new TokenizedSegment(trans, tokenlist, words));
                    else{
                        Set<TokenizedSegment> translations = new HashSet<>();
                        translations.add(new TokenizedSegment(trans, tokenlist, words));
                        tokdictionary.put(entry.getKey(), translations);
                    }
                }
            }


            for(SubSegmentPair ssp: missmatching_source_subsegments){
                Set<TokenizedSegment> taus2 = tokdictionary.get(ssp.getPhrase2().getOriginal_text());
                if(taus2 !=null){
                    for(TokenizedSegment tau2: taus2){
                        Segment lower_tau2 = new Segment(tau2.getOriginal_text());
                        Set<Integer> ta2_in_tutarget=lower_tau2.Appears(new Segment(tu_target.getOriginal_text()));
                        if(ta2_in_tutarget != null){
                            Set<TokenizedSegment> taus1 = tokdictionary.get(ssp.getPhrase1().getOriginal_text());
                            if(taus1 != null){
                                for(TokenizedSegment tau1: taus1){

                                    for(int init_pos: ta2_in_tutarget){
                                        TokenizedSubSegment sub_tau2=new TokenizedSubSegment(tau2, init_pos, tau2.size());
                                        TokenizedSubSegment sub_tau1=new TokenizedSubSegment(tau1, init_pos, tau1.size());
                                        SubSegmentPair subseg_pair = new SubSegmentPair(sub_tau1, sub_tau2);
                                        if(grounded_bothsides){
                                            TokenizedSubSegment subseg = tu_target.getTokenizedSubSegment(
                                                    sub_tau2.getPosition(), sub_tau2.getPosition()+sub_tau2.getLength());
                                            int[] alignment_array = new int[sub_tau2.size()];
                                            SegmentAlignment.EditDistance(subseg.getSentenceCodes(),
                                                    sub_tau2.getSentenceCodes(), alignment_array, null, false);
                                            if(alignment_array.length >= 2 && alignment_array[0] > -1 &&
                                                    alignment_array[alignment_array.length-1] > -1)
                                                this.patch_operators.add(new PatchOperator(ssp, subseg_pair));
                                        }
                                        else
                                            this.patch_operators.add(new PatchOperator(ssp, subseg_pair));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    public Set<Integer> Appears(Segment subsegment, Segment segment){
        Segment subseg = new Segment(subsegment);
        Segment seg = new Segment(segment);
        
        Set<Integer> exit=new LinkedHashSet<Integer>();
        int i;

        if(subseg.size()>0 && seg.size()>0){
            for(i=0;i<=seg.size()-subseg.size();i++){
                if(subseg.getSentence().equals(seg.getSentence().subList(i, i+subseg.size()))){
                    exit.add(i);
                }
            }
            if(exit.isEmpty())
                return null;
            else
                return exit;
        }
        else
            return null;
    }
    
    public PatchedSegment ApplyPatches(Set<PatchOperator> patches){
        PatchedSegment translation = new PatchedSegment(this.translation, patches);
        Map<Integer, Map<Integer, TokenizedSubSegment>> matrix_of_editions=
                new HashMap<Integer, Map<Integer, TokenizedSubSegment>>();
        
        for (PatchOperator po: patches){
            int patch_init_pos=po.getTaus().getPhrase2().getPosition();
            int patch_end_pos=po.getTaus().getPhrase2().getPosition()+po.getTaus().getPhrase2().getLength();
            
            if(!matrix_of_editions.containsKey(patch_init_pos)){
                Map<Integer, TokenizedSubSegment> tau_end_position=new HashMap<Integer, TokenizedSubSegment>();
                tau_end_position.put(patch_end_pos, po.getTaus().getPhrase1());
                matrix_of_editions.put(patch_init_pos, tau_end_position);
                /*System.err.println(translation);
                System.err.println(translation.size());
                System.err.println(patch_init_pos);
                System.err.println(patch_end_pos);
                System.err.println(po.getTaus().getPhrase1());
                System.err.println();*/
            }
            else{
                matrix_of_editions.get(patch_init_pos).put(patch_end_pos, po.getTaus().getPhrase1());
                /*System.err.println(translation);
                System.err.println(translation.size());
                System.err.println(patch_init_pos);
                System.err.println(patch_end_pos);
                System.err.println(po.getTaus().getPhrase1());
                System.err.println();*/
            }
        }
        List<Integer> edit_positions=new LinkedList<Integer>(matrix_of_editions.keySet());
        Collections.sort(edit_positions);
        Collections.reverse(edit_positions);
        
        for(int init_pos: edit_positions){
            for(int end_pos: matrix_of_editions.get(init_pos).keySet()){
                TokenizedSubSegment prefix_subsegment;
                if(init_pos>0)
                    prefix_subsegment=translation.getTokenizedSubSegment(0, init_pos);
                else
                    prefix_subsegment= new TokenizedSubSegment("", 0, 0);
                //TokenizedSubSegment prefix_word_codes=translation.getSentence().subList(0, init_pos);
                TokenizedSubSegment postfix_subsegment=translation.getTokenizedSubSegment(end_pos,translation.size());
                //TokenizedSubSegment postfix_word_codes=translation.getSentence().subList(end_pos,translation.size());
                TokenizedSubSegment center_subsegment = matrix_of_editions.get(init_pos).get(end_pos);
                List<Word> new_word_list=new LinkedList<Word>(((Segment)prefix_subsegment).getSentence());
                new_word_list.addAll(center_subsegment.getSentence());
                new_word_list.addAll(postfix_subsegment.getSentence());
                
                List<SegmentToken> st = new LinkedList<SegmentToken>(prefix_subsegment.getTokens());
                st.addAll(center_subsegment.getTokens());
                st.addAll(postfix_subsegment.getTokens());
                
                StringBuilder sb = new StringBuilder(prefix_subsegment.getOriginal_text());
                sb.append(" ");
                sb.append(center_subsegment.getOriginal_text());
                sb.append(" ");
                sb.append(postfix_subsegment.getOriginal_text());
                
                //Token[] tokens = tokenizer.tokenizeVerbatim(sb.toString());
                //Token[] tokens = tokenizer.tokenizeWords(match.source, StemmingMode.MATCHING);

                List<Word> words = new LinkedList<>();
                List<SegmentToken> tokenlist=tokenizer.Tokenize(sb.toString(), words);

                translation = new PatchedSegment(sb.toString(), tokenlist, words, patches);
            }
        }
        return translation;
    }
    
    private Set<Set<PatchOperator>> BuildValidPatchCollections(Stack<PatchOperator> remaining_patch_operators,
        PatchOperator next_operator, Set<Set<PatchOperator>> accepted_patch_operators_collections){
        if(!interrupt){
            Set<Set<PatchOperator>> collection_valid_po=new HashSet<Set<PatchOperator>>(accepted_patch_operators_collections);
            for(Set<PatchOperator> po_collection: accepted_patch_operators_collections){
                boolean compatible=true;
                for(PatchOperator po: po_collection){
                    if(!po.CompatibleSigma(next_operator) || !po.CompatibleTau(next_operator)){
                        compatible=false;
                        break;
                    }
                }
                if(compatible){
                    Set<PatchOperator> new_set=new HashSet<PatchOperator>(po_collection);
                    new_set.add(next_operator);
                    collection_valid_po.add(new_set);
                }
            }
            if(remaining_patch_operators.isEmpty())
                return collection_valid_po;
            else{
                PatchOperator patchop= remaining_patch_operators.pop();
                return BuildValidPatchCollections(remaining_patch_operators,patchop,collection_valid_po);
            }
        }
        else
            return new HashSet<Set<PatchOperator>>();
    }
    
    public Set<PatchedSegment> BuildAllPatchedTranslations(){
        Stack<PatchOperator> remaining_patch_operators=new Stack<PatchOperator>();
        remaining_patch_operators.addAll(this.patch_operators);
        Set<PatchedSegment> newsegments=new HashSet<PatchedSegment>();
        
        this.interrupt = false;

        if(!remaining_patch_operators.isEmpty()){
            PatchOperator nextOperator=remaining_patch_operators.pop();

            Set<Set<PatchOperator>> accepted_patch_operators=new HashSet<Set<PatchOperator>>();
            Set<PatchOperator> initial_operator_collection=new HashSet<PatchOperator>();
            accepted_patch_operators.add(initial_operator_collection);
            accepted_patch_operators=BuildValidPatchCollections(remaining_patch_operators, nextOperator, accepted_patch_operators);

            for(Set<PatchOperator> po_collection: accepted_patch_operators){
                PatchedSegment newsegment=ApplyPatches(po_collection);
                newsegments.add(newsegment);
            }
        }
        return newsegments;
    }
}
