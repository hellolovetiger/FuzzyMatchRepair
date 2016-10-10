/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package es.ua.dlsi.patch.translation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringEscapeUtils;

/**
 *
 * @author mespla
 */
public class LocalApertiumTranslator implements GenericTranslator {
    
    private String langCmd;
    
    public LocalApertiumTranslator(String langCmd){
        this.langCmd = langCmd;
    }
        
    public Set<String> getTranslation(final String input) {

        Set<String> output = new HashSet<>();
        String finalline = "";
        // pull from the map if already there
        
        try {
            String[] command = {"apertium", "-u", langCmd};
            ProcessBuilder probuilder = new ProcessBuilder(command);

            Process process = probuilder.start();
            OutputStream stdin = process.getOutputStream();
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
            writer.write(input);
            writer.newLine();
            writer.flush();
            writer.close();

            InputStream is = process.getInputStream();
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line; 
            while ((line = br.readLine()) != null) {
                finalline += line;
            }
            br.close();

        } catch (Exception e) {
            e.printStackTrace(System.err);
            System.exit(-1);
        }
        output.add(finalline);
        return output;
    }
    
    
    public Map<String,Set<String>> getTranslation(final Set<String> inputset) {
        Map<String,Set<String>> dictionary=new HashMap<>();
        if(!inputset.isEmpty()){
            try {
                StringBuilder sb = new StringBuilder();
                List<String> input = new LinkedList<>(inputset);
                for(String s: input){
                    sb.append("<p>");
                    sb.append(s);
                    sb.append("</p>");
                }

                //String[] command = {"apertium", "-u", "-f html", langCmd};

                ProcessBuilder probuilder = new ProcessBuilder("apertium", "-u", "-fhtml", langCmd);

                Process process = probuilder.start();
                OutputStream stdin = process.getOutputStream();
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stdin));
                writer.write(sb.toString());
                writer.flush();
                writer.close();

                InputStream is = process.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line; 
                StringBuilder finalline = new StringBuilder();

                while ((line = br.readLine()) != null) {
                    finalline.append(line);
                }
                br.close();
                String finaltranslation=StringEscapeUtils.unescapeHtml3(finalline.toString().replaceAll("\\s<", "<").replaceAll(">\\s", ">").replaceAll("^<p>", "").replace("</p>", ""));
                List<String> translations=new LinkedList<>(Arrays.asList(finaltranslation.split("<p>")));
                for(int i=0; i < translations.size(); i++){
                    if(dictionary.containsKey(input.get(i))){
                        dictionary.get(input.get(i)).add(translations.get(i));
                    }
                    else{
                        Set<String> trans_set = new HashSet<>();
                        trans_set.add(translations.get(i));
                        dictionary.put(input.get(i), trans_set);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace(System.err);
                System.exit(-1);
            }
        }
        return dictionary;
    }
}
