/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package xsqconvertergit;

/**
 *
 * @author Wim Spee
 */
public interface CSFastQEntryInterface {

    void addFastQEntryPosition(int csInt, int qInt);

    void setSeqName(String seqName);
    
    void setReadStartPosition(int readStartPosition);
    
    String getSeqName();
    
    String getSeq();

    @Override
    String toString();
    
}