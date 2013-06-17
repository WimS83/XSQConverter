/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit.bcmp_test;

import xsqconvertergit.bc_frag_test.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import xsqconvertergit.XSQConverterGit;

/**
 *
 * @author root
 */
public class BcMPTest {
    
    private static final File tmpDir = new File(System.getProperty("java.io.tmpdir"));
    
    private static File testBaseOutputDir;     
    private static File testXSQOutputDir;    
    private static File testConversionMetricsFile;    
    
    private static List<String> tags;
    private static List<String> libraries;        
        
    public BcMPTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
        
        Assert.assertTrue("Unable to create " + tmpDir.getAbsolutePath(), tmpDir.exists() || tmpDir.mkdirs());
        
        String XSQbaseName = "WKZ1_20121113_MixedMPRun14Run47_Marc_minimalTest_01";
        
        testBaseOutputDir = new File(tmpDir, XSQbaseName + "_fastq");        
        testXSQOutputDir = new File(testBaseOutputDir, XSQbaseName);        
        testConversionMetricsFile = new File(testXSQOutputDir, "conversionMetrics.txt");
        
        tags = Arrays.asList(new String[]{"R3","F3"}); 
        libraries = Arrays.asList(new String[]{"1","2","3","4","5","6","7","8","9","10","11","12","13","14","multipleBarcodeMatch","noBarcodeMatch"});           
        
        String testXSQFilePath = BcMPTest.class.getResource(XSQbaseName+".xsq").getFile();
        String bcmpFilePath = BcMPTest.class.getResource("BC_setIA_2.txt").getFile();
          
        List<String> arguments = new ArrayList<String>();
        arguments.add("-i");
        arguments.add(testXSQFilePath);
        arguments.add("-o");
        arguments.add(testBaseOutputDir.getPath());
        arguments.add("-f");
        arguments.add("BWA");
        arguments.add("-m");
        arguments.add(bcmpFilePath);        
        
          
        XSQConverterGit.main(arguments.toArray(new String[0]));
        
    }
    
    @AfterClass
    public static void tearDownClass() {
        try {
            //remove the base output dir and all its content
            FileUtils.deleteDirectory(testBaseOutputDir);
        } catch (IOException ex) {
            Logger.getLogger(BcMPTest.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        
    }
    
    @Before
    public void setUp() {    }
    
    @After
    public void tearDown() {   }

       
    @Test
    public void testBaseOutputDir() {
        
        Assert.assertTrue( "Base output dir " + testBaseOutputDir.getAbsolutePath() + " does not exist", testBaseOutputDir.exists());
    }
    
    @Test
    public void testXSQOutputDir() {
        Assert.assertTrue( "XSQ output dir " + testXSQOutputDir.getAbsolutePath() + " does not exist", testXSQOutputDir.exists());
    }
    
    @Test
    public void testLibraryTagOutputDir() {
        
        for(String library : libraries)
        {
            for(String tag: tags)
            {
                File libraryAndtagDir = new File(testXSQOutputDir, library+"_"+tag);
                Assert.assertTrue( "Library and tag output dir " + libraryAndtagDir.getAbsolutePath() + " does not exist", libraryAndtagDir.exists());
            }
        }       
    }
    
    @Test
    public void testLibraryTagReadsOutputDir() {
        
        for(String library : libraries)
        {
            for(String tag: tags)
            {
                File libraryAndtagDir = new File(testXSQOutputDir, library+"_"+tag);
                File libraryAndtagAndReadsDir = new File(libraryAndtagDir, "reads");                
                Assert.assertTrue( "Library and tag reads output dir " + libraryAndtagAndReadsDir.getAbsolutePath() + " does not exist", libraryAndtagAndReadsDir.exists());
            }
        } 
    }
    
    @Test
    public void testConversionMetricsFile() {
        Assert.assertTrue( "Conversion metric file " + testConversionMetricsFile.getAbsolutePath() + "does not exist", testConversionMetricsFile.exists());
        
        File expectedConversionMetricsFile  = new File(getClass().getResource("conversionMetrics.txt").getFile());        
       
        junitx.framework.FileAssert.assertEquals("Conversion metrics files is not like expected", testConversionMetricsFile, expectedConversionMetricsFile);                 
        
    }    
    
    @Test
    public void testFastQFiles() {
        
        for(String library : libraries)
        {
            for(String tag: tags)
            {
                File libraryAndtagDir = new File(testXSQOutputDir, library+"_"+tag);
                File libraryAndtagAndReadsDir = new File(libraryAndtagDir, "reads");
                
                File testFastQFile = new File(libraryAndtagAndReadsDir, "p1."+library+"_"+tag+".fastq");
                Assert.assertTrue( "FastQ file " + testFastQFile.getAbsolutePath() + " does not exist", testFastQFile.exists());
                
                File expectedFastQFile  = new File(getClass().getResource(testFastQFile.getName()).getFile()); 
                 
                junitx.framework.FileAssert.assertEquals("Fastq file "+testFastQFile.getPath()+" is not like expected", testFastQFile, expectedFastQFile);  
            }
        }         
    }     
    
}
