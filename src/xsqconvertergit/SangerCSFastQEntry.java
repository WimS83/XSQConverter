/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import xsqconvertergit.interfaces.CSFastQEntryInterface;
import java.util.HashMap;

/**
 * BWA specific Color Space FastQ entries
 * @author Wim Spee
 */
public class SangerCSFastQEntry implements CSFastQEntryInterface {

    
    private HashMap<Integer, Character> intToCharMap = null;   
    
    private String seqName = null;
    private String description = "+";
      
    
    private StringBuilder readCSSb = null;
    private StringBuilder readQvalueSb = null;   
    
    private Integer readStartPosition = null;
    private Integer readLengthCutoff = null;
    
    
    public SangerCSFastQEntry()    
    {        
        intToCharMap = new HashMap<Integer,Character>();
        intToCharMap.put(0, '0');
        intToCharMap.put(1, '1');
        intToCharMap.put(2, '2');
        intToCharMap.put(3, '3');  
        
        readCSSb = new StringBuilder(50);
        readQvalueSb = new StringBuilder(50);
    }
    
    @Override
    public void setSeqName(String seqName)
    {
        this.seqName = seqName;
    }  

    @Override
    public String getSeq() {
      return readCSSb.toString();
    }
    
    @Override
    public void setReadStartPosition(int readStartPosition)
    {
        this.readStartPosition = readStartPosition;
    }
    
     @Override
     public void setReadLengthCutoff(int readLengthCutoff) {
        this.readLengthCutoff = readLengthCutoff;
     }
    
    
    
    
    @Override
    public void addFastQEntryPosition(int csInt, int qInt )
    {
        char csValue = convertCSIntToCSChar(csInt);
        char qValue = convertQualToBWAQual(qInt);
        
        if(qInt == 63)
        {
            csValue = '.';
            qValue = '"';        
        } 
        
        if(qInt == 0)
        {
            qValue = '"'; 
        }
        
        
        
        readCSSb.append(csValue);
        readQvalueSb.append(qValue);
    }    
    
    
    private char convertCSIntToCSChar(int csInt)
    {
        return intToCharMap.get(csInt);
    }
    
    private char convertQualToBWAQual(int qInt)
    {
        return (char) (qInt+33);
    }

    @Override
    public String toString() {
        
        int readEndPosition = readCSSb.length();
        if(( readStartPosition + readLengthCutoff) < readEndPosition)
        {
            readEndPosition =  readStartPosition + readLengthCutoff;
        }        
        
        StringBuilder sb = new StringBuilder(105);
        sb.append("@");
        sb.append(seqName);
        sb.append("\n");
        sb.append(readCSSb.substring(readStartPosition, readEndPosition));
        sb.append("\n");
        sb.append(description);
        sb.append("\n");
        sb.append(readQvalueSb.substring(readStartPosition, readEndPosition));
        sb.append("\n");
        
        return sb.toString();    
    }     

    @Override
    public String getSeqName() {
        return seqName;
    }
    
    
    
    
    
}
