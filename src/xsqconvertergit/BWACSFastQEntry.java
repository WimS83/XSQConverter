/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.util.HashMap;

/**
 * BWA specific Color Space FastQ entries
 * @author Wim Spee
 */
public class BWACSFastQEntry implements CSFastQEntryInterface {

    
    private HashMap<Integer, Character> csToBWACSMap = null;   
    
    private String seqName = null;
    private String description = "+";
      
    
    private StringBuilder readCSSb = null;
    private StringBuilder readQvalueSb = null;   
    
    private Integer readStartPosition = null;
    
    
    public BWACSFastQEntry()    
    {        
        
        
        csToBWACSMap = new HashMap<Integer,Character>();
        csToBWACSMap.put(0, 'A');
        csToBWACSMap.put(1, 'C');
        csToBWACSMap.put(2, 'G');
        csToBWACSMap.put(3, 'T');  
        
        readCSSb = new StringBuilder(50);
        readQvalueSb = new StringBuilder(50);
    }
    
    @Override
    public void setSeqName(String seqName)
    {
        this.seqName = seqName;
    }
    
    @Override
    public void setReadStartPosition(int readStartPosition)
    {
        this.readStartPosition = readStartPosition;
    }
    
    
    @Override
    public void addFastQEntryPosition(int csInt, int qInt )
    {
        char csValue = convertCStoBWACS(csInt);
        char qValue = convertQualToBWAQual(qInt);
        
        if(qInt == 63)
        {
            csValue = 'N';
            qValue = '"';        
        } 
        
        if(qInt == 0)
        {
            qValue = '"'; 
        }
        
        
        
        readCSSb.append(csValue);
        readQvalueSb.append(qValue);
    }    
    
    
    private char convertCStoBWACS(int csInt)
    {
        return csToBWACSMap.get(csInt);
    }
    
    private char convertQualToBWAQual(int qInt)
    {
        return (char) (qInt+33);
    }

    @Override
    public String toString() {
        
                
        StringBuilder sb = new StringBuilder(105);
        sb.append("@");
        sb.append(seqName);
        sb.append("\n");
        sb.append(readCSSb.substring(readStartPosition));
        sb.append("\n");
        sb.append(description);
        sb.append("\n");
        sb.append(readQvalueSb.substring(readStartPosition));
        sb.append("\n");
        
        return sb.toString();    
    } 
    
    @Override
    public String getSeqName() {
        return seqName;
    }
    
    @Override
    public String getSeq() {
      return readCSSb.toString();
    }
    
    
    
    
}
