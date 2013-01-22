/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import ncsa.hdf.hdf5lib.exceptions.HDF5Exception;
import org.apache.commons.cli.*;
import org.apache.commons.io.FilenameUtils;




/**
 *
 * @author Wim Spee
 */
public class XSQConverterJava {

        
    //private static File outputDir = null;  
       
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        
                
        Options options = new Options();
        options.addOption("i", true, "XSQ input file path. XSQ file name should be $machineName_$dateString_$projectName_$owner_$laneNr.xsq");
        options.addOption("o", true, "output directory path");
        options.addOption("c", true, "output fastq chunksize. Default is 1000000");
        options.addOption("l", true, "library which should be converted. For multiple libraries use this argument multiple times");
        options.addOption("b", true, "barcodes which should be converted. For multiple barcodes use this argument multiple times");
        options.addOption("d", false, "display all libraries names");
        options.addOption("j", false, "display Java path");
        options.addOption("f", true, "fastQ dialect / format. Either BWA or Sanger." );        
        options.addOption("m", true, "file with matepair bacodes. Each line should contains a barcode color space sequence and a barcode name, separated with a tab. All barcodes are required to have the same length. A output file is created for every barcode name.  ");
        options.addOption("n", true, "number of mismatches to be allowed for matching matepair barcodes. Default is 0.");
        options.addOption("h", false, "print this message");
        
        CommandLineParser parser = new PosixParser();
        CommandLine cmd = null;
        try {
            cmd = parser.parse( options, args);
        } catch (ParseException ex) {
            Logger.getLogger(XSQConverterJava.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        if(cmd.hasOption("j")){ printJavaPath(); System.exit(0); }
        if(cmd.hasOption("h")){ printHelp(options); }
        
        if(cmd.hasOption("d"))
        { 
            if(!cmd.hasOption("i")){printHelp(options);}
            String xsqFilePath = cmd.getOptionValue("i"); 
            XSQFile xSQFile = new XSQFile(xsqFilePath); 
            try {
                xSQFile.printAllRootSubGroups();
            } catch (HDF5Exception ex) {
                ex.printStackTrace();
            }            
            
            System.exit(0);
        }             
        
        
        if(!requiredOptionsAreSet(cmd))
        {
            System.out.println("Not all required options are set");
            printHelp(options);           
        }  
        
        
        Map<String, String> matePairBarCodeMap = new HashMap<String, String>();
        Integer MPBCMismatchesAllowed = 0;
        if(cmd.hasOption("m"))
        {
            matePairBarCodeMap = readMatePairBarCodes(cmd);
            MPBCMismatchesAllowed = new Integer(cmd.getOptionValue("n", "0"));
        }
        
        
        Boolean bwaSpecific = false;
        if(cmd.getOptionValue("f").equalsIgnoreCase("bwa"))
        {
            bwaSpecific = true;
        };
          
        
        String xsqFilePath = cmd.getOptionValue("i"); 
        String outputPath = cmd.getOptionValue("o");
        long chunkSize =  new Long(cmd.getOptionValue("c", "1000000"));   

        Map<String, String> librariesSubSet = getSpecifiedLibrarySubSet(cmd);
        Map<Integer, Integer> barcodes = getSpecifiedBarcodeSubSet(cmd);        
        
        System.out.println("Input file = " + xsqFilePath);
        System.out.println("Output directory = " + outputPath);
         
         
        File outputDir = createOutputDir(outputPath,xsqFilePath);
        XSQFile xSQFile = new XSQFile(xsqFilePath);
        try {
            xSQFile.processXSQFile(outputDir, chunkSize, librariesSubSet,barcodes,  bwaSpecific, matePairBarCodeMap, MPBCMismatchesAllowed);
        } catch (Exception ex) {
           ex.printStackTrace();
        }
                
        
    }
    
    /**
     * print the command line help options
     * @param options 
     */
    private static void printHelp(Options options)
    {
        HelpFormatter formatter = new HelpFormatter();
        formatter.printHelp( "XSQ converter. By default converts all XSQ libraries into CSFastQ files except the Unassigned_* and the Unclassified library. Options -i and -o are required.", options );
        System.exit(1);
    }
    
    /**
     * check if all the required options where set at the command line
     * @param cmd
     * @return 
     */
    private static Boolean requiredOptionsAreSet(CommandLine cmd)
    {
        Boolean requiredOptionsAreSet = true;
        
        if(!cmd.hasOption("i")){ requiredOptionsAreSet = false; }
        if(!cmd.hasOption("o")){ requiredOptionsAreSet = false; }  
        if(!cmd.hasOption("f")){ requiredOptionsAreSet = false; }
        else
        {
            if(!fastQformatIsSpecified(cmd)){ requiredOptionsAreSet = false;}
        }  
        
        return requiredOptionsAreSet;
        
    }
    
    private static boolean fastQformatIsSpecified(CommandLine cmd) {
        Boolean fastQFormatIsSet = false;
        
                if(cmd.getOptionValue("f").equalsIgnoreCase("bwa")){fastQFormatIsSet = true;}
        if(cmd.getOptionValue("f").equalsIgnoreCase("sanger")){fastQFormatIsSet = true;}
        return fastQFormatIsSet;
        
    }
    
    /**
     * Validate the output directory path and if it is valid path create the output directory. 
     * @param outputPath The path to create. 
     */
    private static File createOutputDir(String outputPath, String xsqFilePath) {
        
        File xsqFile = new File(xsqFilePath);
        if(!xsqFile.canRead())
        {
            System.out.println("XSQ file "+ xsqFilePath + " could not be read.");
            System.exit(1);  
        }      
        
        
        File baseOutputDir = new File(outputPath);  
        
        if(!baseOutputDir.exists())
        {
            if(!baseOutputDir.mkdirs())
            {
                System.out.println("Output directory "+ baseOutputDir + " could not be created.");
                System.exit(1);                
            }
           
        }
        else
        {
            if(!baseOutputDir.isDirectory())
            {
                System.out.println("Output directory "+ baseOutputDir + " is not a directory.");
                System.exit(1);   
            } 
        }
        
        String fileNameWithExt = xsqFile.getName();
                
        String xsqName = FilenameUtils.removeExtension(fileNameWithExt);       
        
        File outputDir = new File(baseOutputDir, xsqName);
        outputDir.mkdir();
        return outputDir;        
        
        
        
    }
    
    /**
     * print the Java Path. The native libraries should be placed in one of the directories listed. 
     */
    private static void printJavaPath()
    {
        String path = System.getProperty("java.library.path");
        
        System.out.println("The Java Path = ");      
        System.out.println(path);
    }

    private static Map<String, String> getSpecifiedLibrarySubSet(CommandLine cmd) {        
        
        Map<String, String> librariesSubSet = new HashMap<String, String>();
        if(cmd.hasOption("l"))
        {
            for(Object object :cmd.getOptionValues("l"))
            {
                librariesSubSet.put(object.toString(), null);                
            } 
        }
        return librariesSubSet;
    }

    private static Map<Integer, Integer> getSpecifiedBarcodeSubSet(CommandLine cmd) {
        
        Map<Integer, Integer> barcodes = new HashMap<Integer, Integer>();
        if(cmd.hasOption("b"))
        {
            for(Object object :cmd.getOptionValues("b"))
            {
                barcodes.put(new Integer(object.toString()), null);                
            } 
        }         
        return barcodes;
    }

    private static Map<String, String> readMatePairBarCodes(CommandLine cmd) {
        File matePairBarCodeFile = new File(cmd.getOptionValue("m"));
        
         Map<String, String> matePairBarCodeMap = new HashMap<String, String>();
        
        if(!matePairBarCodeFile.canRead())
        {
            System.out.println("Can't read matePairBarCode file "+ matePairBarCodeFile.getPath());
            System.exit(1);                    
        }
        try {
            BufferedReader in = new BufferedReader(new FileReader(matePairBarCodeFile));
            
            System.out.println ("Reading barcodes");
            System.out.println ("Barcode\tbarcodeName");
            
            String line;
            while ((line = in.readLine()) != null)   
            {
                String[] splitLine = line.split("\t");
                String barCodeSequence =  splitLine[0];
                String barCodeName =  splitLine[1];
                
                matePairBarCodeMap.put(barCodeSequence, barCodeName);
                
                
                // Print the content on the console
                System.out.println (line);
            }
            
        } catch (Exception ex) {
            System.out.println("Can't read matePairBarCode file "+ matePairBarCodeFile.getPath());
            System.exit(1);       
        }
        
        if(matePairBarCodeMap.isEmpty())
        {
            System.out.println("No 'barcode TAB barcodeName' entries found in  "+ matePairBarCodeFile.getPath());
            System.exit(1);     
        }
        
        return matePairBarCodeMap;        
        
    }

    
    
  
   
}
