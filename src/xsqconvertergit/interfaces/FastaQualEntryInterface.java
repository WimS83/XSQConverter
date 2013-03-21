/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit.interfaces;

/**
 *
 * @author Wim Spee
 */
public interface FastaQualEntryInterface extends CSFastQEntryInterface{

    void addFastQEntryPosition(int csInt, int qInt);

    void setSeqName(String seqName);
    
    void setReadStartPosition(int readStartPosition);
    
    void setReadLengthCutoff(int readStartPosition);
    
    String getSeqName();
    
    String getSeq();

    @Override
    String toString();
    
    String toQualString();
}
