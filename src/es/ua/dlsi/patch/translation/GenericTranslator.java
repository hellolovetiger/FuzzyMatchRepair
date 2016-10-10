/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.translation;

import java.util.Map;
import java.util.Set;

/**
 *
 * @author mespla
 */
public interface GenericTranslator {
    public abstract Set<String> getTranslation(final String input);
    public abstract Map<String,Set<String>> getTranslation(final Set<String> inputset);
}
