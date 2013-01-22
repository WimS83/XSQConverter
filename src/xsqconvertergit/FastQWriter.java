/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes FastQentries to an output file. 
 * Chunks the output file if the amount of entries is larger than the chunksize. 
 * Also sets the seqName of the entry just before writing it to the output file. 
 * @author Wim Spee
 */
public class FastQWriter {
    
    long readCounter = new Long(0);
    Long chunkCounter = new Long(0);
    BufferedWriter out = null;   
    
    private long chunkSize;
    
    File outputDirReads;
    
    String outputName;

    /**
     * Writes FastQentries to an output file.
     * @param libraryName name of the library for which reads are being converted. 
     * @param outputDir output directory where the fastq file should be written
     * @param chunkSize amount or reads after which a new fastq chunk file should be opened.  
     * @param dateString dateString that should be used in the name of the fastq files
     * @param laneNr laneNr that should be used in the name of the fastq files
     * @param tagName tag name that should be used in the name of the fastq files
     */
    public FastQWriter(String outputName, File baseOutputDir, long chunkSize) {
        
        this.chunkSize = chunkSize;
        this.outputName = outputName;
       
        createOutputDir(baseOutputDir, outputName);
        
        setWriterToNextChunk();          
        
    }    
    
    /**
     * Creates a output directory for the library being processed
     * @param outputDir the base output directory
     * @param libraryName the library name being processed
     */
    private void createOutputDir(File baseOutputDir, String outputName ) {
       
        File outputDir = new File(baseOutputDir,outputName );
        outputDir.mkdir();
        outputDirReads = new File(outputDir, "reads" );        
        outputDirReads.mkdir();
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
        chunkFileName.append(outputName);

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
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void printNrReadsWritten()
    {
        System.out.println(outputName+"\t"+readCounter);           
    }
    
    
    
    
    
    
    
    

    
    
    
    
    
    


    
    

    
    
    

    
    
    
    
    
    
    
    
}
