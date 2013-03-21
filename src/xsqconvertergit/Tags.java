/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;


import xsqconvertergit.interfaces.CSFastQEntryInterface;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.h5.H5ScalarDS;
import ncsa.hdf.object.Dataset;

/**
 *Object containing a XSQ tag node and a function to process it.
 * @author Wim Spee
 */
public class Tags {

    private static Map<String, String> validTags = null;
    
    Group tagGroup = null;   
    Integer numColorBase = 0;
        
    
    public Tags(Group fragmentGroup, Integer numColorBase){
        this.tagGroup = fragmentGroup;
        this.numColorBase = numColorBase;          
    }  
    
    
    /**
     * Process a tag , read the byte array, decode the colorspace and qual value from each byte and add it to a fastqEntry which is written to disk. 
     * Every 50 bytes a new fastq entry is created.     
     * @param fastQWriter the fastQWriter to use to write the fast entries to disk. 
     * 
     */
    public long processTag(OutPutWriter outPutWriter)
    {                
        outPutWriter.setCurrentTagName(tagGroup.getName());
        
        H5ScalarDS readsAndQuals = (H5ScalarDS)tagGroup.getMemberList().get(0);     
             
        
        byte[] byteArray = getByteArrayFromFragment(readsAndQuals);        
        
        int byteArrayPos = 0;
        long readNr = 1;
        
        CSFastQEntryInterface fastQEntry = outPutWriter.getNewEntry(); 
        
       
        
        //loop over all bytes
        for (byte byteVQ :byteArray )
        {
            //if the previous byteArray position was numColorBase print the fastqEntry
            if(byteArrayPos % numColorBase == 0 && byteArrayPos != 0)
            {
               outPutWriter.writeFastQEntry(fastQEntry);
               readNr++;               
               
               fastQEntry = outPutWriter.getNewEntry();               
                         
            }            
                        
            //unpack the byte 
            int unsignedByte = 0xff & byteVQ;       //cast it to a larger primitive bit type to make it hold the unsigned value 
            int csInt = unsignedByte & 0x03;        //take the right most 2 bits that encode for the colorspace call
            int qInt = unsignedByte >> 2;           //shift all other bits to the right that encode for the quality value
            
            fastQEntry.addFastQEntryPosition(csInt, qInt);
            
            byteArrayPos++;
        }        
        
       outPutWriter.writeFastQEntry(fastQEntry);       
       return readNr;
        
    }
    
    
    
    
    /**
     * Reads the byte array from the dataset under this tag
     * @param fragment the tag to read the dataset from
     * @return the byte array
     */
    private static byte[] getByteArrayFromFragment(H5ScalarDS readsAndQuals) {
                        
        byte[] byteArray = null;
        try {
             byteArray = readsAndQuals.readBytes();
        } catch (HDF5Exception ex) {
            Logger.getLogger(XSQConverterGit.class.getName()).log(Level.SEVERE, null, ex);
        }
        return byteArray;
    }    
    
    
   
    
   
    
    
    
}
