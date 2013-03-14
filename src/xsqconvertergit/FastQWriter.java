/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 * Writes FastQentries to an output file. 
 * Chunks the output file if the amount of entries is larger than the chunksize. 
 * Also sets the seqName of the entry just before writing it to the output file. 
 * @author Wim Spee
 */
public class FastQWriter {
    
    private long readCounter = new Long(0);
    private Long chunkCounter = new Long(0);
    private BufferedWriter out = null;   
    
    private long chunkSize;   
    
    
    private File outputDirReads;
    
    private String writerId;

    /**
     * Writes FastQentries to an output file.
     * @param libraryName name of the library for which reads are being converted. 
     * @param outputDir output directory where the fastq file should be written
     * @param chunkSize amount or reads after which a new fastq chunk file should be opened.  
     * @param dateString dateString that should be used in the name of the fastq files
     * @param laneNr laneNr that should be used in the name of the fastq files
     * @param tagName tag name that should be used in the name of the fastq files
     */
    public FastQWriter(String writerId, File baseOutputDir, long chunkSize) {
        
        this.chunkSize = chunkSize;
        this.writerId = writerId;
        
       
        createOutputDir(baseOutputDir, writerId);   
        
    }    
    
    /**
     * Creates a output directory for the library being processed
     * @param outputDir the base output directory
     * @param libraryName the library name being processed
     */
    private void createOutputDir(File baseOutputDir, String writerId ) {
       
        File outputDir = new File(baseOutputDir,writerId );
        //create the outputDir if it doesn't exist 
        if(!outputDir.exists())
        {
            outputDir.mkdir();
        }
        
        outputDirReads = new File(outputDir, "reads" );        
        if(!outputDirReads.exists())
        {
            outputDirReads.mkdir();
        } 
        
        
        
    }
    
    public boolean checkForExistingOutput() {
        
        //check if there are already fastq files in the outputDir and it's subdirectories
        String[] extensions = new String[]{"fastq"};
        List<File> fastqFiles = (List<File>) FileUtils.listFiles(outputDirReads, extensions, true);
        
        return !fastqFiles.isEmpty();  
    }
    
    
    
    /**
     * Set the output buffer to write to a next file (chunk)
     */
    public void setWriterToNextChunk()
    {
        chunkCounter++;
        StringBuilder chunkFileName = new StringBuilder();
        chunkFileName.append("p");
        chunkFileName.append(chunkCounter);
        chunkFileName.append('.');
        chunkFileName.append(writerId);

        chunkFileName.append(".fastq");   
        
        
        File outPutChunk = new File(outputDirReads, chunkFileName.toString());
        FileWriter fstream;
        try {
            
            fstream = new FileWriter(outPutChunk);
            out = new BufferedWriter(fstream);            
            
        } catch (IOException ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
        }  
    }

    
    /**
     * Write a fastq entry to the output buffer. Adds seqName to the fastq entry. Calls setWriterToNextChunk if read counter % chunksize = 0 .
     * @param fastQEntry the fastq entry to write
     */
    public void writeFastQEntry(CSFastQEntryInterface fastQEntry) {
        try {
            
            if(readCounter==0){setWriterToNextChunk();}
            
            fastQEntry.setSeqName(fastQEntry.getSeqName()+"_"+readCounter );            

            if(readCounter % chunkSize == 0 && readCounter != 0)
            {
                closeWriter();
                setWriterToNextChunk();
            }            
            out.write(fastQEntry.toString());
            readCounter++;
            
                        
        } catch (IOException ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    /**
     * Close and flush the current output buffer to file
     */
    public void closeWriter()
    {
        try {
            if(out != null)
            {
                out.close();
            }
            
        } catch (IOException ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void printNrReadsWritten()
    {
        System.out.println(writerId+"\t"+readCounter);           
    }

    public String getWriterId() {
        return writerId;
    }

    public long getReadCounter() {
        return readCounter;
    }
    
    
    
    

    
    
    
    
    
    
    
    
    

    
    
    
    
    
    


    
    

    
    
    

    
    
    
    
    
    
    
    
}
