/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

    

/**
 *
 * @author root
 */
public class OutPutWriter {
    
    
    Boolean bwaSpecific;    
    
    String currentLibraryName;
    String currentTileName;
    String currentTagName;
            
    List<String> libraryNames;
    Map<String, Integer> tagNameLengthMap;
    
    long chunkSize;
    
    File outputDir;
    
    Map<String, FastQWriter> libraryAndTagSpecificWriters;
    Map<String, FastQWriter> matePairBarCodeSpecificWriters;
    
    //mate pair specific code
    Boolean matePairBarCode = false;
    
    Long f3Counter = new Long(1);
    Long r3Counter = new Long(1);
    Map<String, String> matePairBarCodeMap;
    
    Map<Long, String> r3ReadNrBarCodeMap;
    
    Map<Character, Character> BWAToCSMap;
    Integer barcodeSize = null;
    
    Integer MPBCMismatchesAllowed;
    
    

    public OutPutWriter(Boolean bwaSpecific, Map<String, Integer> tagNameLengthMap, List<Library> libraries, File outputDir, Long chunkSize, Map<String, String> matePairBarCodeMap, Integer MPBCMismatchesAllowed) {
        
        
         this.bwaSpecific = bwaSpecific;
         this.tagNameLengthMap = tagNameLengthMap;
         this.MPBCMismatchesAllowed = MPBCMismatchesAllowed;
         
         libraryNames = new ArrayList<String>();
         
         for(Library libarary : libraries)
         {
             libraryNames.add(libarary.getName());
         }    
         
         this.outputDir = outputDir;
         this.chunkSize = chunkSize;
         
         if(matePairBarCodeMap.isEmpty())
         {
             createLibraryAndTagSpecificWriters();   
         }
         else
         {
             matePairBarCode = true;
             this.matePairBarCodeMap = matePairBarCodeMap;
             barcodeSize = matePairBarCodeMap.keySet().iterator().next().length();
             createMatePairBarcodeSpecificWriters(matePairBarCodeMap);
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
        if(bwaSpecific)
        {
            cSFastQEntry = new BWACSFastQEntry();
        }
        else
        {
            cSFastQEntry= new CSFastQEntry();
        }
        
        if(matePairBarCode)
        {
            cSFastQEntry.setReadStartPosition(1+barcodeSize);
        
        }
        else
        {
            cSFastQEntry.setReadStartPosition(1);
        
        }
        
        return cSFastQEntry;
    }
    
     public void writeFastQEntry(CSFastQEntryInterface fastQEntry) {
        
        fastQEntry.setSeqName(formatLibraryTagName(currentLibraryName, currentTagName)+ "_"+currentTileName);
         
        FastQWriter fastQWriter;
        
        if(matePairBarCode)
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
        
        if(bwaSpecific)
        {
            seq = convertBWAToCS(seq);
        }
        
        List<String> barcodeNamesMatched = new ArrayList<String>();
        String barcodeName = "";
        for(String barcode : matePairBarCodeMap.keySet())
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
               
               if(mismatches > MPBCMismatchesAllowed)
               {
                   match = false;
                   break;            
               }
            }
            
            if(match)
            {
                barcodeNamesMatched.add(matePairBarCodeMap.get(barcode));
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
    
    
    public void setCurrentLibraryName(String currentLibraryName) {
        this.currentLibraryName = currentLibraryName;
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
        
        if(matePairBarCode)
        {
            for(FastQWriter fastQWriter : matePairBarCodeSpecificWriters.values())
            {
                fastQWriter.printNrReadsWritten();
                fastQWriter.closeWriter();
            }       
        }
        else
        {
            for(FastQWriter fastQWriter : libraryAndTagSpecificWriters.values())
            {
                fastQWriter.printNrReadsWritten();
                fastQWriter.closeWriter();
            }         
        }
        
       
    }

    private void createLibraryAndTagSpecificWriters() {
        
        libraryAndTagSpecificWriters = new HashMap<String, FastQWriter>();
        
        for(String libraryName : libraryNames)
        {
            for(String tagName : tagNameLengthMap.keySet())
            {
                String libAndTagName = formatLibraryTagName(libraryName, tagName);
                FastQWriter fastQWriter = new FastQWriter(libAndTagName, outputDir, chunkSize);
                libraryAndTagSpecificWriters.put(libAndTagName,fastQWriter );            
            }
        }
        
    }
    
    
    private String formatLibraryTagName(String libraryName, String tagName)
    {
        String[] libraryNameSplit = libraryName.split("_");
        libraryName = libraryNameSplit[0];
        
        String libAndTagName =  libraryName +"_"+ tagName; 
        return libAndTagName;
        
    }

    private FastQWriter getCurrentFastQWriter() {
        
        return libraryAndTagSpecificWriters.get(formatLibraryTagName(currentLibraryName, currentTagName));             
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
                FastQWriter f3FastQWriter = new FastQWriter(barcodeName+"F3", outputDir, chunkSize);
                matePairBarCodeSpecificWriters.put(barcodeName+"F3", f3FastQWriter);
                
                FastQWriter r3FastQWriter = new FastQWriter(barcodeName+"R3", outputDir, chunkSize);
                matePairBarCodeSpecificWriters.put(barcodeName+"R3", r3FastQWriter);
            }
        }
        
        FastQWriter f3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatchF3", outputDir, chunkSize);
        matePairBarCodeSpecificWriters.put("noBarcodeMatchF3", f3NoBarcodeMatchFastQWriter);

        FastQWriter r3NoBarcodeMatchFastQWriter = new FastQWriter("noBarcodeMatchR3", outputDir, chunkSize);
        matePairBarCodeSpecificWriters.put("noBarcodeMatchR3", r3NoBarcodeMatchFastQWriter);
        
        FastQWriter f3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatchF3", outputDir, chunkSize);
        matePairBarCodeSpecificWriters.put("multipleBarcodeMatchF3", f3MultipleBarcodeMatchFastQWriter);

        FastQWriter r3MultipleBarcodeMatchFastQWriter = new FastQWriter("multipleBarcodeMatchR3", outputDir, chunkSize);
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

   

   
    
    
    
     
     
    
    
    
    
    
    
    
    
}
