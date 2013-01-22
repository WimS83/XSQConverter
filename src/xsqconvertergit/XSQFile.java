/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.tree.DefaultMutableTreeNode;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import ncsa.hdf.object.Attribute;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.HObject;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5CompoundDS;


/**
 *Object containing a XSQ file 
 * @author Wim Spee
 */
public class XSQFile {

    
    String XSQFilePath = null;
    H5File xsqFile = null;
    Map<String, Integer> usedTags = null;
    
    List<Library> libraries = null;
    
    File outputDir = null;
    
//    String laneNr = null;
//    String dateString = null;    
    
    Group rootGroup = null;

    StringBuilder metrics = new StringBuilder();    
    
     
     
    private enum ReservedNames { RunMetadata, TagDetails, Indexing, Unassigned, Unclassified, } 
    

    public XSQFile(String XSQFilePath) {
        this.XSQFilePath = XSQFilePath;
        
//        setLaneNrAndDateBasedOnFileName(XSQFilePath);
        openFile();
        try {
            getUsedTags();
            getLibraries(rootGroup);
        } catch (Exception ex) {
           
            Logger.getLogger(XSQFile.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
             System.exit(1);
        }
        
    }   
    
    /**
     * Gets the used tags from runmetadata->tagdetails node. 
     */
    private void getUsedTags() throws HDF5Exception {
        
        usedTags = new HashMap<String, Integer>();
           
        Group runMetadataGroup = getReservedGroup(rootGroup, ReservedNames.RunMetadata);
        Group tagDetailsGroup = getReservedGroup(runMetadataGroup, ReservedNames.TagDetails);  
        
        for (Object tag :tagDetailsGroup.getMemberList() )
        {
            Group tagGroup = (Group) tag;
            H5CompoundDS dataSetEncoding = (H5CompoundDS) tagGroup.getMemberList().get(0);
           
            Vector dataSetEncodingVector =  (Vector)dataSetEncoding.read();
            long[] numBaseCallArray = (long[])dataSetEncodingVector.get(4);
            Integer numCSBaseCalls = (int)numBaseCallArray[0]; 
            
            usedTags.put(tagGroup.toString(), numCSBaseCalls);
        }
        
    }
    
     /**
     * Get the libraries from a xsq root group. 
     * Creates a library object for every xsq root group member that matches the a library name from the metadata library names. 
     * @param rootGroup the root group of the HDF5 tree     * 
     * @throws Exception 
     */
    private  void  getLibraries(Group rootGroup) throws HDF5Exception, Exception 
    {
        Map<String, String> libraryNamesMap = getLibraryNames();
        libraries = new ArrayList<Library>();
        
        for(Object rootMember: rootGroup.getMemberList())
        {
            Group rootMemberGroup = (Group)rootMember;
            Attribute libraryNameAttribute = getGroupAttribute(rootMemberGroup,"LibraryName" );
            if(libraryNameAttribute != null)
            {
                String[] libraryNames = (String[])libraryNameAttribute.getValue();
                String libraryName = libraryNames[0];
                
                if(libraryNamesMap.containsKey(libraryName))
                {
                    libraries.add(new Library(rootMemberGroup));
                }
            } 
        }  
    }   
    
    
    
    
    /**
     * Opens a HDF5 file for reading
     * @param hdf5FilePath the path of the HDF5 file to open
     * @return H5File opened for reading
     */
    private void openFile()
    {
        xsqFile =  new H5File(XSQFilePath, H5File.READ);
        try {
            // open the file and retrieve the root group
           openFile2();
            rootGroup = (Group)(( DefaultMutableTreeNode)xsqFile.getRootNode()).getUserObject(); 
        } catch (Exception ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
            System.exit(1);
        } 
    }
    
    private void openFile2() throws Exception
    {
         xsqFile.open();    
    }    
    
    
    private Map<String, String> getLibraryNames() throws HDF5Exception 
    {
        Group runMetadataGroup = getReservedGroup(rootGroup, ReservedNames.RunMetadata);
        
        H5CompoundDS libraryDetails = (H5CompoundDS) runMetadataGroup.getMemberList().get(0);
        
        Vector libraryDetailsVector =  (Vector)libraryDetails.read();
        String[] libraryNameArray = (String[])libraryDetailsVector.get(0);
        
        Map<String, String> libraryNameMap = new HashMap<String, String>();
        for(String libraryName :libraryNameArray)
        {
            libraryNameMap.put(libraryName, null);
        }
        
        
        return libraryNameMap; 
    }
    
    
    private Group getReservedGroup(Group parentGroup, ReservedNames reservedGroupName )  
    {
        Group reservedGroup = null;
        
        for(Object rootSubGroup :parentGroup.getMemberList())
        {
            if(rootSubGroup.toString().equalsIgnoreCase(reservedGroupName.toString()))
            {
                reservedGroup = (Group)rootSubGroup;
                break;
            }
        }
        
        return reservedGroup;
    }
    
   
    
    /**
     * Start processing the XSQ file.
     * @param outputDir the base output dir
     * @param chunkSize the chunksize to use for splitting the output fastQ files.
     */
    public void processXSQFile(
                                    File outputDir, 
                                    long chunkSize, 
                                    Map<String, String> librariesSubSetMap, 
                                    Map<Integer, Integer> barCodes , 
                                    boolean BWASpecific, 
                                    Map<String, String> matePairBarcode, 
                                    Integer MPBCMismatchesAllowed) 
            throws Exception {
        
        this.outputDir = outputDir;        
        
        List librariesToProcess = null;          
        
        //if barcodes are specified get the list of libraries matching these barcodes
        if(!barCodes.isEmpty())
        {   
            librariesToProcess = getLibrariesByBarCode(libraries, barCodes);
        
        }
        //otherwise process all the libraries specified by name of all the non unassigned and non classified libraries
        else
        {        
            //get the user specified libraries
            if(!librariesSubSetMap.isEmpty())
            {
                librariesToProcess = getLibraryByName(libraries, librariesSubSetMap);           
            }
            //get all the non unassigned and non unclassified libraries
            else
            {
                librariesToProcess = removeUnAssignedAndUnclassifiedLibrary(libraries);
            }
        }
               
        processLibraries(librariesToProcess, outputDir, chunkSize, usedTags, BWASpecific, matePairBarcode, MPBCMismatchesAllowed);        
    } 
    
    private List<Library> getLibraryByName(List<Library> libraries, Map<String, String> librariesSubSetMap) throws Exception
    {
        List<Library> librariesSubSet = new ArrayList<Library>();
        for(Library library :libraries)
        {
            String libraryName = library.getName();            
                              
            if(librariesSubSetMap.containsKey(libraryName))
            {                            
                librariesSubSet.add(library);
            }                        
             
        }
        if(librariesSubSet.isEmpty())
        {
            System.out.println("No libraries found in the xsq file with the name(s): "+ librariesSubSetMap.keySet());
            System.exit(0);
        } 
        return librariesSubSet;        
    }
    
    
    
    private List getLibrariesByBarCode(List<Library> libraries, Map<Integer, Integer> barCodesMap) throws Exception {
       List<Library> librariesSubSet = new ArrayList<Library>();
        
       for(Library library :libraries)
       {
            Integer barcode = library.getBarcode();            
                             
            if(barCodesMap.containsKey(barcode))
            {                            
                librariesSubSet.add(library);
            } 
        }
        if(librariesSubSet.isEmpty())
        {
            System.out.println("No libraries found in the xsq file with the barcode(s): "+ barCodesMap.keySet());
            System.exit(0);
        } 
        return librariesSubSet;  
    }    
    
    
    
    
    private List<Library> removeUnAssignedAndUnclassifiedLibrary(List<Library> libraries) {
        
        List librariesSubSet = new ArrayList<Object>();
        for(Library library :libraries)
        {
            if(library.getName().toLowerCase().contains(ReservedNames.Unassigned.toString().toLowerCase()) ||library.getName().toLowerCase().contains(ReservedNames.Unclassified.toString().toLowerCase()))
            {                
            }
            else
            {
                librariesSubSet.add(library);
            }
        }
        if(librariesSubSet.isEmpty())
        {
            System.out.println("No non unassigned and non unclassified libraries found");
            System.exit(0);
        } 
        return librariesSubSet;       
            
            
    }
    
    
    /**
     * Process a list of libraries 
     * @param libraries the list of libraries to process
     */
    private  void processLibraries(
                                        List<Library> libraries, 
                                        File outputDir,
                                        long chunkSize, 
                                        Map<String,Integer> usedTags, 
                                        boolean bwaSpecific, 
                                        Map<String, String> matePairBarcode, 
                                        Integer MPBCMismatchesAllowed)
    {
        OutPutWriter outPutWriter = new OutPutWriter(bwaSpecific, usedTags, libraries, outputDir, chunkSize, matePairBarcode, MPBCMismatchesAllowed);
        
        long totalReadCounter = 0;
        for(Library library : libraries)
        {             
//           Map<String, FastQWriter> fastQWriterPerTag = new HashMap<String, FastQWriter>();
//           for(String tag : usedTags.keySet())
//           {
////             fastQWriterPerTag.put(tag, new FastQWriter(libraryObject.toString(), outputDir, chunkSize, dateString, laneNr, tag));
//               fastQWriterPerTag.put(tag, new FastQWriter(library.getName(), outputDir, chunkSize, tag, bwaSpecific));
//           }           
           
           
           
           long libraryReadCount = library.processLibrary(outPutWriter);
           totalReadCounter += libraryReadCount;
           
           //for 
//           for(FastQWriter fastQWriter: fastQWriterPerTag.values() )
//           {
//               metrics.append(fastQWriter.getLibraryName());
//               metrics.append("\t");
//               metrics.append(fastQWriter.getReadCounter());
//               metrics.append("\n");
//               System.out.println("Processed tag "+ fastQWriter.getTagName() + "with "+fastQWriter.getReadCounter()+" reads");  
//           }
           
                   
           
        }
        
        outPutWriter.closeWriters();
        
        
        System.out.println("Processed "+libraries.size() + "libraries and a total of "+totalReadCounter+" reads");  
        printMetrics();
        
    }    
    
    
   public Attribute getGroupAttribute(Group group, String attributeName) throws Exception
    {
        Attribute returnAttribute = null;
        for(Object metadataAttribute : group.getMetadata())
        {
            Attribute attribute = (Attribute)metadataAttribute;
            if(attribute.getName().toLowerCase().equals(attributeName.toLowerCase()))
            {
                returnAttribute = attribute;
                break;
            }
        }
        return returnAttribute;    
    }
    
   
    
     /**
     * Check if the groupName contains a reserved group name
     * @param groupName the group name to check
     * @return boolean indicating if the group name contains a reserved group name
     */
    private boolean matchesReservedGroupName(String groupName) {
                
        for( Object reservedGroupName : ReservedNames.values())
        {
            if(groupName.contains(reservedGroupName.toString()))
            {
                return true;
            }
        }        
        return false;  
    }
    
    /**
     * Recursively print a group and its members.
     * @throws Exception
     */
    public void printGroup(Group g, String indent) throws Exception
    {
        if (g == null)
            return;

        java.util.List members = g.getMemberList();

        int n = members.size();
        indent += "    ";
        HObject obj = null;
        for (int i=0; i<n; i++)
        {
            obj = (HObject)members.get(i);
            System.out.println(indent+obj);
            if (obj instanceof Group)
            {
                printGroup((Group)obj, indent);
            }
        }
    } 
    
    /**
     * Print all all the root subgroups
     */
    public void printAllRootSubGroups() throws HDF5Exception
    {
        if (rootGroup == null)
            return;
        
//        System.out.println("Displayed are all sub nodes of the root node. At least nodes containing the word Indexing and RunMetadata are reserved nodes that don´t contain libraries");
//        for (Object library : getLibraries(rootGroup, true) )
//        {
//            System.out.println(library);            
//        } 
        
        for(String libraryName : getLibraryNames().keySet())
        {
            System.out.println(libraryName);     
        }        
        
    }
    
    private void printMetrics() {
        System.out.println(metrics);   
        
        File metricsFile = new File(outputDir,"conversionMetrics.txt" );
        try {
           FileWriter  fstream = new FileWriter(metricsFile);
            fstream.write(metrics.toString());
             fstream.close();
        } catch (IOException ex) {
            Logger.getLogger(XSQFile.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
        
    }
    
    
    
    
    
    
}
