/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.tokenisation;

import es.ua.dlsi.segmentation.Word;
import java.util.List;

/**
 *
 * @author mespla
 */
public interface FMRTokenizer {
    abstract public List<SegmentToken> Tokenize(String s, List<Word> w);
}
