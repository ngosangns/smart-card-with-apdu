/**
 * @file  Storage.java
 * @brief The Class of JCSystem Sample Code in JavaCard API Specification
 * @comment The purpose of this example is only used to show the usage of API functions and there is no practical significance.
 * @copyright Copyright(C) 2016 JavaCardOS Technologies Co., Ltd. All rights reserved.
 */
 
package applet3 ;

 
import javacard.framework.*;
 
public class applet3  extends Applet
{
 
    public static final byte INS_GET_MEMORY_EEPROM   = 0x01;
    public static final byte INS_GET_MEMORY_COR     = 0x02;
    public static final byte INS_GET_MEMORY_COD     = 0x03;
    public static final byte INS_MAKE_TRANSIENT     = 0x04;
 
    public static void install(byte[] bArray, short bOffset, byte bLength) 
    {
        new applet3().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }
 
    public void process(APDU apdu)
    {
        if (selectingApplet())
        {
            return;
        }
 
        byte[] buf = apdu.getBuffer();
        boolean isDeletionSupported = false;
        switch (buf[ISO7816.OFFSET_INS])
        {
        case INS_GET_MEMORY_EEPROM:
            int memsize_All = 0;
            short memSize_EEP = 0;
            //If the number of available bytes is greater than 32767, then this method returns 32767.
            //It's necessary to apply memory, to allow the remaining the available bytes is less than 32767
            try{
                while(true){
                    //Creates a persistent memory byte array with the specified array length
                    byte[] temp = new byte[(short)0x7FFF];
                    memsize_All += (short)0x7FFF;
                            }
            }catch(Exception e)
            {
                //Obtains the amount of memory of the persistent type that is available to the applet.
                memSize_EEP = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT);  
            }
            memsize_All += memSize_EEP;
            //Splits the int types data 'memsize_All' into 2 short types
            short Tmp1 = (short)((memsize_All >> 16) & 0xFFFF);
            short Tmp2 = (short)(memsize_All & 0xFFFF);
            //Deposits the short value as two successive bytes in the byte array.
            Util.setShort(buf, (short)0, Tmp1);  
            Util.setShort(buf, (short)2, Tmp2);  
            //Request the object deletion service of the Java Card runtime environment.
            apdu.setOutgoingAndSend((short)0, (short)4);
            //Determine if the implementation for the Java Card platform supports the object deletion mechanism
            isDeletionSupported = JCSystem.isObjectDeletionSupported();
            if (isDeletionSupported)
            {
                ////if the Java Card platform supports the object deletion mechanism, request Object Deletion
                JCSystem.requestObjectDeletion();
            }
            break;
        case INS_GET_MEMORY_COR:
            //Obtains the amount of memory of the CLEAR_ON_RESET type that is available to the applet.
            short memSize_COR = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_RESET);  
            //Deposits the short value 'memSize_COR' as two successive bytes in the byte array 'buf'.
            Util.setShort(buf, (short)0, memSize_COR);   
            apdu.setOutgoingAndSend((short)0, (short)2);          
            break;
        case INS_GET_MEMORY_COD:
            //Obtains the amount of memory of the CLEAR_ON_DESELECT type that is available to the applet.
            short memSize_COD = JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_TRANSIENT_DESELECT);   
            //Deposits the short value 'memSize_COD' as two successive bytes in the byte array 'buf'.
            Util.setShort(buf, (short)0, memSize_COD);   
            apdu.setOutgoingAndSend((short)0, (short)2);      
            break;
        case INS_MAKE_TRANSIENT:
            apdu.setIncomingAndReceive();
            short lc = apdu.getIncomingLength();
            if (lc == 0x00)
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            //Creates a transient byte array with the specified array length
            byte[] tmpMemory = JCSystem.makeTransientByteArray((short)100, JCSystem.CLEAR_ON_DESELECT);
            //Begins an atomic transaction, to ensure the integrity of data
            JCSystem.beginTransaction();
            //Copies an array from the 'buf' source array, beginning at the ISO7816.OFFSET_CDATA position(non-atomically).
            Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, tmpMemory, (short)0, lc);
            //Copies an array from the 'tmpMemory' source array, beginning at the 0 position
            Util.arrayCopy(tmpMemory, (short)0, buf, (short)0, lc);
            //Commits an atomic transaction. The contents of commit buffer is atomically committed. 
            JCSystem.commitTransaction();
            apdu.setOutgoingAndSend((short)0, (short)lc);
            //Determine if the implementation for the Java Card platform supports the object deletion mechanism
            isDeletionSupported = JCSystem.isObjectDeletionSupported();
            if (isDeletionSupported)
            {
                //if the Java Card platform supports the object deletion mechanism, request Object Deletion
                JCSystem.requestObjectDeletion();
            }
            break;
 
        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
}