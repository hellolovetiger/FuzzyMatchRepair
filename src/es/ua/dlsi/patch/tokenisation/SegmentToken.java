/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.tokenisation;

import java.util.List;

/**
 *
 * @author mespla
 */

public class SegmentToken{
    public int offset;
    
    public int length;
    
    public SegmentToken(int offset, int length){
        this.offset=offset;
        this.length=length;
    }
    
    static String UntokenizedSegmentPiece(String segment, List<SegmentToken> toklist){
        if(!toklist.isEmpty()){
            SegmentToken firsttok=toklist.get(0);
            SegmentToken lasttok=toklist.get(toklist.size()-1);
            int minoffset = firsttok.offset;
            int maxoffset = lasttok.offset+lasttok.length;
            return segment.substring(minoffset, maxoffset);
        }
        else
            return "";
    }
}
