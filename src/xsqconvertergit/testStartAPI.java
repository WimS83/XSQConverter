/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author root
 */
public class testStartAPI {
    
    
    
    
    
    
    
    public static void main(String[] args) 
    {
        XSQFile xSQFile = new XSQFile("/home/wim/Analysis/xsq_converter_test_sets/bcfrag_case/inputFiles/HU03_20121219_MaartjeChip2R_Nico_minimalTest_L02.xsq");
               
        ProcessingOptions processingOptions = new ProcessingOptions();
        processingOptions.setFastQDialect(FastQDialect.tophat);
        processingOptions.setChunkSize(new Long(1000000000));
        processingOptions.setMatePairBarcodeRun(false);    
        processingOptions.setReadLenghtOutputCutoff(1000000);
        
        
        File baseOutputDir = new File("/tmp/HU03_20121219_MaartjeChip2R_Nico_minimalTest_L02");
        baseOutputDir.mkdirs();
        
        processingOptions.setOutputDir(baseOutputDir);
        try {
            List<Library> processedLibraries = xSQFile.processXSQFile(processingOptions);
             String blaat = "blaat";
        } catch (Exception ex) {
            Logger.getLogger(testStartAPI.class.getName()).log(Level.SEVERE, null, ex);
        }
        
       
    }
    
}
