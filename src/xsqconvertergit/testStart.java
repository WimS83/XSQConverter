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
//        arguments.add("-i");
//        arguments.add("/home/wim/Analysis/xsq_converter_test_sets/./bcfrag_case/inputFiles/HU03_20121219_MaartjeChip2R_Nico_L02.xsq");
//        arguments.add("-o");
//        arguments.add("/home/wim/Analysis/xsq_converter_test_sets/bcfrag_case/outputVersionMarch2013");
//        arguments.add("-f");
//        arguments.add("BWA");  
                arguments.add("-j");
        
        
             
        
                
        
        
        

        
        XSQConverterGit.main(arguments.toArray(new String[0]));
    }
    
}
