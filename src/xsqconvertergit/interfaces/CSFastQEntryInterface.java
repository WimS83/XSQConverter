/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit.interfaces;

/**
 *
 * @author Wim Spee
 */
public interface CSFastQEntryInterface {

    void addFastQEntryPosition(int csInt, int qInt);

    void setSeqName(String seqName);
    
    void setReadStartPosition(int readStartPosition);
    
    void setReadLengthCutoff(int readStartPosition);
    
    void setLeadingBase(Character leadingBase);
    
    String getSeqName();
    
    String getSeq();

    @Override
    String toString();
    
}
