/**
 * @file  Communication.java
 * @brief Communication Sample Code in JavaCard API Specification
 * @comment The purpose of this example is only used to show the usage of API functions and there is no practical significance.
 * @copyright Copyright(C) 2016 JavaCardOS Technologies Co., Ltd. All rights reserved.
 */
package applet2 ;
 
import javacard.framework.*;
import javacardx.apdu.ExtendedLength;
 
public class applet2  extends Applet implements ExtendedLength 
{
    public static final byte INS_SEND_RECV_APDU_1   = 0x01;
    public static final byte INS_SEND_RECV_APDU_2   = 0x02;
    public static final byte INS_RECV_EXTEND_APDU   = 0x03;
    public static final byte INS_SEND_EXTEND_APDU   = 0x04;
 
    private byte[] tmp_memory;
    public static final short MAX_LENGTH = (short)(0x7FFF);
 
    public static void install(byte[] bArray, short bOffset, byte bLength) 
    {
        new applet2 ().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
    }
 
    public void process(APDU apdu)
    {
        if (selectingApplet())
        {
            return;
        }
        //Get the apdu buffer datas 
        byte[] buf = apdu.getBuffer();
        byte cla= buf[ISO7816.OFFSET_CLA];
        byte ins = buf[ISO7816.OFFSET_INS];
        byte p1 = buf[ISO7816.OFFSET_P1];
        byte p2 = buf[ISO7816.OFFSET_P2];
        short p1p2 = Util.makeShort(p1, p2);
 
        if (cla != (byte)0x80)
        {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
 
        if (p1p2 != 0x00)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }
 
        //Calling this method indicates that this APDU has incoming data. 
        short recvLen = apdu.setIncomingAndReceive();
 
        //Get the incoming data length(Lc).
        short lc = apdu.getIncomingLength();
 
        if (lc == 0x00)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
 
        switch (ins)
        {
        case (byte)INS_SEND_RECV_APDU_1:
            //Define a byte string
            byte sendStr[] = {'A','P','D','U', ',', 'C','l','a','s','s', ',', 'D','e','m','o'};
            short len = (short) sendStr.length;
 
            //Copy character to APDU Buffer.
            Util.arrayCopyNonAtomic(sendStr, (short)0, buf, (short)0, (short)len);
 
            //Send the 'sendStr' string, the hex of JCRE sending data is the ASCII of sendStr.
            apdu.setOutgoingAndSend((short)0, (short)len);
            break;
 
        case (byte)INS_SEND_RECV_APDU_2:
            //Set the length of received bytes into the 'buf' array,  the offset is 0.
            Util.setShort(buf, (short)0, (short)lc);
 
            //Set the data transfer direction to outbound.
            short le = apdu.setOutgoing();
 
            //Set the expected(le) length of response data.
            apdu.setOutgoingLength((short)le);
 
            //Sends the data of APDU buffer 'buf', the length is 'le' bytes,  the offset is 0.
            apdu.sendBytes((short) 0, (short)le);
            break;
 
        case (byte) INS_RECV_EXTEND_APDU: //
            short pointer = 0;
            tmp_memory = new byte[MAX_LENGTH];
            short offData = apdu.getOffsetCdata();
            // recvieve data and put them in a byte array 'tmp_memory'
            while (recvLen > (short) 0) 
            {
                Util.arrayCopy(buf, offData, tmp_memory, pointer, recvLen);
                pointer += recvLen;
                //Gets as many data bytes as will fit without APDU buffer overflow
                recvLen = apdu.receiveBytes(offData);
            }
 
            // send the lc length
            apdu.setOutgoing();
            apdu.setOutgoingLength((short)2);
            Util.setShort(buf, (short)0, lc);
            apdu.sendBytesLong(buf, (short) 0, (short)2);
            break;
 
        case (byte) INS_SEND_EXTEND_APDU: //
            if(buf[ISO7816.OFFSET_LC] != 0x02)
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            short sendLen = Util.getShort(buf, ISO7816.OFFSET_CDATA);
            // send the specified length data in byte array 'tmp_memory'
            apdu.setOutgoing();
            apdu.setOutgoingLength(sendLen);
            apdu.sendBytesLong(tmp_memory, (short)0, sendLen);
            //request Object Deletion
            tmp_memory = null;
            JCSystem.requestObjectDeletion();
            break;
 
        default:
            ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }
 
}