/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


    

/**
 *
 * @author root
 */
public class OutPutWriter {
    
    private ProcessingOptions processingOptions;
    
    private Library currentLibrary;
    private String currentTileName;
    private String currentTagName;
            
    private List<Library> libraries;
    private Map<String, Integer> tagNameLengthMap;    
   
    
    private Long f3Counter = new Long(1);
    private  Long r3Counter = new Long(1);   
    
    private Map<Long, String> r3ReadNrBarCodeMap;
    
    private Map<Character, Character> BWAToCSMap;
     
    
    //writer collections
    private HashMap<String, FastQWriter> matePairBarCodeSpecificWriters;
    private HashMap<String, FastQWriter> libraryAndTagSpecificWriters;   
    
    

    public OutPutWriter(ProcessingOptions processingOptions, Map<String, Integer> tagNameLengthMap, List<Library> libraries) {
        
         this.processingOptions = processingOptions;
         
         this.tagNameLengthMap = tagNameLengthMap;        
         this.libraries = libraries;  
         
         if(processingOptions.getMatePairBarcodeRun())
         {                          
            createMatePairBarcodeSpecificWriters(processingOptions.getMatePairBarcodeMap());             
         }
         else
         {
            createLibraryAndTagSpecificWriters();   
         }  
         
         
        BWAToCSMap = new HashMap<Character, Character>();
        BWAToCSMap.put('A', '0');
        BWAToCSMap.put('C', '1');
        BWAToCSMap.put('G', '2');
        BWAToCSMap.put('T', '3');  
        BWAToCSMap.put('N', '.');  
         
    }    
    
    
    public CSFastQEntryInterface getNewEntry()
    {
        CSFastQEntryInterface cSFastQEntry = null;
        if(processingOptions.getBwaSpecific())
        {
            cSFastQEntry = new BWACSFastQEntry();
        }
        else
        {
            cSFastQEntry= new CSFastQEntry();
        }
        
        if(processingOptions.getMatePairBarcodeRun())
        {
            cSFastQEntry.setReadStartPosition(1+processingOptions.getMatePairBarCodeLength());
        
        }
        else
        {
            cSFastQEntry.setReadStartPosition(1);
        
        }
        
        return cSFastQEntry;
    }
    
     public void writeFastQEntry(CSFastQEntryInterface fastQEntry) {
        
        
        fastQEntry.setSeqName(currentLibrary.getName()+ "_" + currentTagName+ "_"+currentTileName);        
         
        FastQWriter fastQWriter;
        
        if(processingOptions.getMatePairBarcodeRun())
        {
            
            fastQWriter = getFastQwriterBasedOnBarcode(fastQEntry);            
        }
        else
        {
            fastQWriter = getCurrentFastQWriter();       
        }      
                  
        
        fastQWriter.writeFastQEntry(fastQEntry);    
    }

    private String getMPBarcodeName(CSFastQEntryInterface fastQEntry) {
        
        String seq = fastQEntry.getSeq();
        
        if(processingOptions.getBwaSpecific())
        {
            seq = convertBWAToCS(seq);
        }
        
        List<String> barcodeNamesMatched = new ArrayList<String>();
        String barcodeName = "";
        for(String barcode : processingOptions.getMatePairBarcodeMap().keySet())
        {
            Boolean match = true;
            
            int charLocation = 0;
            int mismatches = 0;

            for(char c : barcode.toCharArray())
            {
               if(c != seq.charAt(charLocation++))
               {
                  mismatches++;
               }
               
               if(mismatches > processingOptions.getMPBCMismatchesAllowed())
               {
                   match = false;
                   break;            
               }
            }
            
            if(match)
            {
                barcodeNamesMatched.add(processingOptions.getMatePairBarcodeMap().get(barcode));
            }
            
            
        }
        
        if(barcodeNamesMatched.size() == 1)
        {
            barcodeName = barcodeNamesMatched.get(0);        
        }
        else
        {
            if(barcodeNamesMatched.isEmpty())
            {
                barcodeName = "noBarcodeMatch";          
            }
            if(barcodeNamesMatched.size() > 1)
            {
                barcodeName = "multipleBarcodeMatch";      
            }            
        }
        
        if(barcodeName.isEmpty())
        {
            String blaat = "blaat";
        }
        
        return barcodeName;
    
    }    
    
    
    public void setCurrentLibrary(Library currentLibrary) {
        this.currentLibrary = currentLibrary;
    }

    public void setCurrentTileName(String currentTileName) {
        this.currentTileName = currentTileName;
    }
    
    public void resetTileReadCounters()
    {
        f3Counter = new Long(1);
        r3Counter = new Long (1);
        
        r3ReadNrBarCodeMap = new HashMap<Long, String>();
    }

    public void setCurrentTagName(String currentTagName) {
        this.currentTagName = currentTagName;
    }

    public Set<String> getTagNames()
    {
        return tagNameLengthMap.keySet();
    }
    
    public Integer getTagLength(String tagName)
    {
        return tagNameLengthMap.get(tagName);
    } 
    
    

    public void closeWriters() {
        
        if(processingOptions.getMatePairBarcodeRun())
        {
            for(FastQWriter fastQWriter : matePairBarCodeSpecificWriters.values())
            {                
                fastQWriter.closeWriter();
            }       
        }
        else
        {
            for(FastQWriter fastQWriter : libraryAndTagSpecificWriters.values())
            {
                fastQWriter.closeWriter();
            }         
        }
        
       
    }

    private void createLibraryAndTagSpecificWriters() {
        
        libraryAndTagSpecificWriters = new HashMap<String, FastQWriter>();
        
        for(Library library : libraries)
        {
            for(String tagName : tagNameLengthMap.keySet())
            {
                String writerID = library.getNameAndBarCode()+ "_" +tagName;
                String writerName;
                
                if(processingOptions.getUseBarcodeInOutputName())
                {
                    writerName = library.getNameAndBarCode() + "_" + tagName;
                }
                else
                {
                     writerName = library.getName() + "_" + tagName;
                }
                
                FastQWriter fastQWriter = new FastQWriter(writerName, processingOptions.getOutputDir(), processingOptions.getChunkSize());
                libraryAndTagSpecificWriters.put(writerID,fastQWriter );            
            }
        }
        
    }
    
    
//    private String formatLibraryTagName(String libraryName, String tagName)
//    {
//        String[] libraryNameSplit = libraryName.split("_");
//        libraryName = libraryNameSplit[0];
//        
//        String libAndTagName =  libraryName +"_"+ tagName; 
//        return libAndTagName;
//        
//    }

    private FastQWriter getCurrentFastQWriter() {
        
        String writerID = currentLibrary.getNameAndBarCode() + "_"+ currentTagName;
        return libraryAndTagSpecificWriters.get(writerID);             
    }
    
     private FastQWriter getFastQwriterBasedOnBarcode(CSFastQEntryInterface fastQEntry) {
        
         FastQWriter fastQWriter;
         
         //if the tag is R3, look up the writer based on the barcode in the read
         if(currentTagName.equalsIgnoreCase("R3"))
         {

            String barcodeName = getMPBarcodeName(fastQEntry);                

            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"R3");
            r3ReadNrBarCodeMap.put(r3Counter, barcodeName);
            r3Counter++;

         }
         //if the tag is F3, look up the writer based on where the R3 mate read was written
         else
         {
            String barcodeName = r3ReadNrBarCodeMap.get(f3Counter);
            fastQWriter = matePairBarCodeSpecificWriters.get(barcodeName+"F3");
            f3Counter++;
         }
         return fastQWriter;
    }

    private void createMatePairBarcodeSpecificWriters(Map<String, String> matePairBarCodeMap) {
        
        matePairBarCodeSpecificWriters = new HashMap<String, FastQWriter>();
        
        for(String barcodeName : matePairBarCodeMap.values())
        {
            if(!matePairBarCodeSpecificWriters.containsKey(barcodeName+"F3"))
            {
                FastQWriter f3FastQWriter = new FastQWriter(barcodeName+"F3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
                matePairBarCodeSpecificWriters.put(barcodeName+"F3", f3FastQWriter);
                
                FastQWriter r3FastQWriter = new FastQWriter(barcodeName+"R3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
                matePairBarCodeSpecificWriters.put(barcodeName+"R3", r3FastQWriter);
            }
        }
        
        FastQWriter f3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatchF3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
        matePairBarCodeSpecificWriters.put("noBarcodeMatchF3", f3NoBarcodeMatchFastQWriter);

        FastQWriter r3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatchR3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
        matePairBarCodeSpecificWriters.put("noBarcodeMatchR3", r3NoBarcodeMatchFastQWriter);
        
        FastQWriter f3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatchF3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
        matePairBarCodeSpecificWriters.put("multipleBarcodeMatchF3", f3MultipleBarcodeMatchFastQWriter);

        FastQWriter r3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatchR3", processingOptions.getOutputDir(), processingOptions.getChunkSize());
        matePairBarCodeSpecificWriters.put("multipleBarcodeMatchR3", r3MultipleBarcodeMatchFastQWriter);
        
        
        
    }

    private String convertBWAToCS(String seq) {
       
        StringBuilder CSSeq  = new StringBuilder(seq.length());
        
        for (int i = 0; i < seq.length(); i++)
        {
            char c = seq.charAt(i);        
            CSSeq.append(BWAToCSMap.get(c));
            
        }
        
        return CSSeq.toString();
        
    }

    public Boolean checkExistingOutput(Library library) {
        
        Boolean existingOutput = false;
        
        if(processingOptions.getMatePairBarcodeRun())
        {                          
            if(matePairBarCodeSpecificWriters.values().iterator().next().checkForExistingOutput())
            {
                existingOutput = true;
            }       
        }
        else
        {
            for(String tagName : tagNameLengthMap.keySet())
            {
                String writerID = library.getNameAndBarCode() + "_"+ tagName;
                if(libraryAndTagSpecificWriters.get(writerID).checkForExistingOutput())
                {
                    existingOutput = true;
                }
                
            }
        } 
        
        return existingOutput;
        
    }

    public Collection<FastQWriter> getFastQWriters() {
        
        Collection<FastQWriter> fastQWriters = null;
        if(processingOptions.getMatePairBarcodeRun())
        {
            fastQWriters = matePairBarCodeSpecificWriters.values();
        }
        else
        {
            fastQWriters = libraryAndTagSpecificWriters.values();
        }
        
        return fastQWriters;
        
        
        
    }

    public void removeExistingOutput() {
        
        for(File exitingOutputFileOrDir : processingOptions.getOutputDir().listFiles())
        {
            exitingOutputFileOrDir.delete();
        }
    }

   

   
    
    
    
     
     
    
    
    
    
    
    
    
    
}
