/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.util.List;
import java.util.Map;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Group;

/**
 * Object containing a XSQ library node and a function to process it.
 * @author Wim Spee
 */
public class Library {

    Group libraryGroup = null;
    
    String name = null;
    
    private enum LibraryAttributeNames  {IndexID, LibraryName};
    
    public Library(Group libraryGroup ) throws Exception {
        
        this.libraryGroup = libraryGroup;
        
        Attribute libraryNameAttribute = getLibraryAttribute(libraryGroup, LibraryAttributeNames.LibraryName);               
                
        String[] libraryNames = (String[])libraryNameAttribute.getValue();
        name = libraryNames[0];  
                
    }   
    
    
    
   /**
    * Process the library
    * @param fastQWriterPerTag a map containing a fastqWriter per tag that should be in the library. Based on which tag is being processed a fastqWriter is retrieved from this map.
    * @return 
    */
    public long processLibrary(OutPutWriter outPutWriter)
    {
                           
        outPutWriter.setCurrentLibraryName(name);
        
        List tileList = libraryGroup.getMemberList();
        int tileCounter = 0;
        long readCounter = 0;
         
        System.out.println("Processing "+name);
        for(Object tileObject : tileList)
        {
            Group tileGoup = (Group)tileObject;
            Tile tile = new Tile(tileGoup);            
            readCounter += tile.processTile(outPutWriter);
            tileCounter++;                            
        }       
        
        
        System.out.println("\nProcessed "+tileCounter+ " tiles with a total of "+readCounter+ " reads"  );
        return readCounter;
    
    }
    
    /**
     * 
     * @param libraryGroup
     * @return the barcode of the library. -1 if the index id of the library is not set
     * @throws Exception 
     */
    public Integer getBarcode() throws Exception
    {
        Attribute libraryIndexId = getLibraryAttribute(libraryGroup, LibraryAttributeNames.IndexID);
        
        short barcode = -1;
        if(libraryIndexId != null)
        {
            short[] barcodes = (short[])libraryIndexId.getValue();
            barcode = barcodes[0];   
        }        
        
        return new Integer(barcode);
    }
    
    
    public Attribute getLibraryAttribute(Group libraryGroup, LibraryAttributeNames reservedName) throws Exception
    {
        Attribute returnAttribute = null;
        for(Object metadataAttribute : libraryGroup.getMetadata())
        {
            Attribute attribute = (Attribute)metadataAttribute;
            if(attribute.getName().toLowerCase().equals(reservedName.toString().toLowerCase()))
            {
                returnAttribute = attribute;
                break;
            }
        }
        return returnAttribute;    
    }
    
    public String getName() 
    {
        
        return name;    
    }
    
}
