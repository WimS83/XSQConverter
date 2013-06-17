/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author root
 */
public class testStart {
    
    
        
    
    
    public static void main(String[] args) 
    {
        
        List<String> arguments = new ArrayList<String>();
        arguments.add("-i");
        arguments.add("/home/wim/Analysis/BCMP/HU01_20130529_WigardMPP1BCPool1_Nico/L02/result/HU01_20130529_WigardMPP1BCPool1_Nico_L02.xsq");
        arguments.add("-o");
        arguments.add("/home/wim/Analysis/BCMP/output");
        arguments.add("-f");
        arguments.add("BWA");  
        arguments.add("-m"); 
        arguments.add("/home/wim/Analysis/BCMP/barcodes.txt"); 
        arguments.add("-n"); 
        arguments.add("1"); 
          //      arguments.add("-j");
        
        

        
        XSQConverterGit.main(arguments.toArray(new String[0]));
    }
    
}
