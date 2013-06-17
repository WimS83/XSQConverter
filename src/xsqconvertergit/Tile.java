/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.util.Collections;
import java.util.List;
import ncsa.hdf.object.Group;

/**
 * Object containing a XSQ tile node and a function to process it.
 * @author Wim Spee
 */
public class Tile {

    Group tileGroup = null;   
    
    public Tile(Group tileGroup) {
        this.tileGroup = tileGroup;        
       
    }    
    
    
    /**
     * Process the tile.
     * @param fastQWriterPerTag a map containing a fastqWriter per tag that should be in the tile. Based on which tag is being processed a fastqWriter is retrieved from this map.
     * @return 
     */
    public long processTile(OutPutWriter outPutWriter)
    {
        
        outPutWriter.setCurrentTileName(tileGroup.getName());
        outPutWriter.resetTileReadCounters();
        
        List tagList = tileGroup.getMemberList();  
      //  Collections.reverse(tagList);
                        
        long readCounter = 0;       
        
        for(Object tileMember : tagList)
        {
           String tileMemberName = tileMember.toString();
           
            
           Boolean matchUsedtags= false;
           for(String usedTag : outPutWriter.getTagNames())
           {
               if(tileMemberName.equalsIgnoreCase(usedTag))
               {
                   matchUsedtags = true; 
                   break;
               }                   
           }
           
           if(matchUsedtags)
           {
               Group fragmentGroup = (Group)tileMember;
               Tags tag = new Tags(fragmentGroup, outPutWriter.getTagLength(tileMemberName));
               readCounter += tag.processTag(outPutWriter);
           }
        }
        System.out.print("\rProcessed tile "+ tileGroup.getName());         
        return readCounter;
    }
    
    
}
