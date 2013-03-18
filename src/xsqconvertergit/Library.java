/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.File;
import java.util.HashMap;
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
    short barcode;
    
    Map<String, List<File>> tagFastQFileMap = new HashMap<String, List<File>>();

    
    
    private enum LibraryAttributeNames  {IndexID, LibraryName};
    
    public Library(Group libraryGroup ) throws Exception {
        
        this.libraryGroup = libraryGroup;
        
        //get the libraryName and set it as the name
        Attribute libraryNameAttribute = getLibraryAttribute(libraryGroup, LibraryAttributeNames.LibraryName);               
        String[] libraryNames = (String[])libraryNameAttribute.getValue();
        name = libraryNames[0];  
        
        //get the barcodeId and set it as the barcodeId
        Attribute libraryIndexId = getLibraryAttribute(libraryGroup, LibraryAttributeNames.IndexID);
        barcode = -1;
        if(libraryIndexId != null)
        {
            short[] barcodes = (short[])libraryIndexId.getValue();
            barcode = barcodes[0];   
        }        
                
    }   
    
    
    
   /**
    * Process the library
    * @param fastQWriterPerTag a map containing a fastqWriter per tag that should be in the library. Based on which tag is being processed a fastqWriter is retrieved from this map.
    * @return 
    */
    public long processLibrary(OutPutWriter outPutWriter)
    {
        
        outPutWriter.getTagNames();
        
        outPutWriter.setCurrentLibrary(this);
        
        
        
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

    public Integer getBarcode() {
        return new Integer(barcode);
    }
    
    public String getNameAndBarCode()
    {
        return name + "_" + getBarcode().toString();
    }
    
    public void setWrittenFiles(String tag, List<File> fastqFiles) {
        tagFastQFileMap.put(tag, fastqFiles);
    }
    
    
    
    
    
    
}
