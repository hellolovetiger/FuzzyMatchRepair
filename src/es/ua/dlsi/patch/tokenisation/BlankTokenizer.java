/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.tokenisation;

import es.ua.dlsi.segmentation.Word;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author mespla
 */
public class BlankTokenizer implements FMRTokenizer{
    public List<SegmentToken> Tokenize(String s, List<Word> w){
        List<SegmentToken> tokenlist = new LinkedList<>();

        Pattern blankpattern = Pattern.compile("[^\\s]+");
        Matcher m = blankpattern.matcher(s);
        while (m.find()) {
            if(w!=null){
                w.add(new Word(s.substring(m.start(), m.end())));
            }
            SegmentToken t = new SegmentToken(m.start(), m.start()-m.end());
        }
        
        return tokenlist;
    }
}
