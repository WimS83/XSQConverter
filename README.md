<script type="text/javascript" src="https://ajax.googleapis.com/ajax/libs/jquery/1.8.0/jquery.min.js"></script>
<script type="text/javascript" src="https://blockchain.info//Resources/wallet/pay-now-button.js"></script>

<div style="font-size:16px;margin:0 auto;width:300px" class="blockchain-btn"
     data-address="1BXRZaHEeLsRvDT9BqvLLHpYKajja2PZCo"
     data-shared="false">
    <div class="blockchain stage-begin">
        <img src="https://blockchain.info//Resources/buttons/donate_64.png"/>
    </div>
    <div class="blockchain stage-loading" style="text-align:center">
        <img src="https://blockchain.info//Resources/loading-large.gif"/>
    </div>
    <div class="blockchain stage-ready">
         <p align="center">Please Donate To Bitcoin Address: <b>[[data-address]]</b></p>
         <p align="center" class="qr-code"></p>
    </div>
    <div class="blockchain stage-paid">
         Donation of <b>[[value]] BTC</b> Received. Thank You.
    </div>
    <div class="blockchain stage-error">
        <font color="red">[[error]]</font>
    </div>
</div>




XSQConverter
============

The XSQConverter tool converts SOLiD XSQ files to normal Sanger colorspace fastq or to BWA 0.5.9 specific (double encoded) colorspace fastq files.
There is also an option to output CSFasta and qual files. 

This tool is able to convert XSQ files comparatively fast by using the native HDF5 C++ libraries, OO wrapped by HDF5 Java library plus some java code written by myself to process the XSQ file. 

The output is a directory structure with output files in the specified format for every combination of library(lib_sample1, lib_sample2) and tag (F3, F5 ,R3 etc). 

I have added a zip file with the HDF5 native c++ libraries and the java wrappers to the source because I have had troubles myself downloading the correct libraries again from the HDFGroup website.  
The libraries are from http://www.hdfgroup.org/HDF5/

Other libraries used are from apache commons and a junit-addon. 

More information about the XSQ format is here:
https://www.lifetechnologies.com/content/dam/LifeTech/Documents/PDFs/software-downloads/XSQ_file_format_specifications_v1.0.1.pdf


The command line interface:

usage: XSQ converter version v10:03-07-2013
           . By default converts all XSQ libraries into CSFastQ files
           except the Unassigned_* and the Unclassified library. Options
           -i and -o are required.
 
 -b,--barcode <arg>                       barcodes which should be
                                          converted. For multiple barcodes
                                          use this argument multiple times
 
 -c,--chunk <arg>                         output fastq chunksize. Default
                                          is 1000000
                                          
 -d,--display                             display all libraries names and
                                          quit without processing
                                          
 -f,--fastq-dialect <arg>                 fastQ dialect / format. Either
                                          BWA, Sanger or csfasta.
                                          
 -h,--help                                print this message
 
 -i,--input <arg>                         XSQ input file path.
 
 -j,--javapath                            display Java path
 
 -l,--library <arg>                       library which should be
                                          converted. For multiple
                                          libraries use this argument
                                          multiple times
                                          
 -m,--matepair-barcode-file <arg>         file with matepair bacodes. Each
                                          line should contains a barcode
                                          color space sequence and a
                                          barcode name, separated with a
                                          tab. All barcodes are required
                                          to have the same length. A
                                          output file is created for every
                                          barcode name and tag
                                          combination.
                                          
 -mpbl <arg>                              The location of the matepair
                                          barcode sequence. Either F3 or
                                          R3. Default is F3
                                          
 -n,--matepair-barcode-mismatches <arg>   number of mismatches to be
                                          allowed for matching matepair
                                          barcodes. Default is 0.
                                          
 -o,--output <arg>                        output directory path
 
 -t                                       Add the leading base and color
                                          call. BWA and Bowtie do not use
                                          these but other mappers do. Not
                                          yet available for mate pair
                                          barcode runs. Can only be used
                                          for output in Sanger or csfasta
                                          format.
                                          
 -u,--use-barcode-name                    use barcode in the output names.
                                          Should always be used when
                                          processing multiple unassigned
                                          libraries by barcode because
                                          they have the same name.
                                          
 -w,--overwrite                           overwrite existing output. By
                                          default libraries for which
                                          existing output is present are
                                          skipped.
                                          
 -x,--read-lenght-cutoff <arg>            Only output reads untill this
                                          cutoff. Works on all tags.


