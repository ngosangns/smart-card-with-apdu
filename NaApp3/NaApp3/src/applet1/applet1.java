package applet1 ;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.*;
import javacardx.crypto.*;

public class applet1 extends Applet
{
	private byte[] duLieu;
	
	private static final byte INS_SET_AES_KEY              = (byte)0x10;
    private static final byte INS_SET_AES_ICV              = (byte)0x11;
    private static final byte INS_DO_AES_CIPHER            = (byte)0x12;
    private static final byte CHECK_INFO_EXIST             = (byte)0x13;
    private static final byte TAO_DU_LIEU                  = (byte)0x14;
        
    private byte aesKeyLen;
    private byte[] aesKey;
    private byte[] aesICV;

    private Cipher aesEcbCipher;
    private Cipher aesCbcCipher;

    private Key tempAesKey1;
    private Key tempAesKey2;
    private Key tempAesKey3;
    
    public applet1()
    {
        aesKey = new byte[32];
        aesICV = new byte[16];
        aesKeyLen = 0;
        //Create a AES ECB/CBS object instance of the AES algorithm.
        aesEcbCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
        aesCbcCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
		//Create uninitialized cryptographic keys for AES algorithms
        tempAesKey1 = KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_AES_128, false);
        tempAesKey2 = KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_AES_192, false);
        tempAesKey3 = KeyBuilder.buildKey(KeyBuilder.TYPE_AES_TRANSIENT_DESELECT, KeyBuilder.LENGTH_AES_256, false);

        JCSystem.requestObjectDeletion();
    }

	public static void install(byte[] bArray, short bOffset, byte bLength) 
	{
		new applet1().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
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
        
        //Calling this method indicates that this APDU has incoming data. 
        short len = apdu.setIncomingAndReceive();
        
        //Get the incoming data length(Lc).
        short lc = apdu.getIncomingLength();
        
        if (cla != (byte)0x80) {
            ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        }
 
        if (p1p2 != 0x00) {
            ISOException.throwIt(ISO7816.SW_WRONG_P1P2);
        }
        
		switch (ins) {
			//Test
			case (byte)0x00:
				//Define a byte string
				byte sendStr[] = {'S', 'E', 'N', 'D', 'N', 'U', 'D', 'E', 'S'};
				short resLen = (short) sendStr.length;
				//Copy character to APDU Buffer.
				Util.arrayCopyNonAtomic(sendStr, (short)0, buf, (short)0, (short)resLen);
				//Send the 'sendStr' string, the hex of JCRE sending data is the ASCII of sendStr.
				apdu.setOutgoingAndSend((short)0, (short)resLen);
				break;
				
			case INS_SET_AES_KEY:
				setAesKey(apdu, len);
				break;
			case CHECK_INFO_EXIST:
				checkInfoExist(apdu, len);
				break;
			case INS_SET_AES_ICV:
				setAesICV(apdu, len);
				break;
			case INS_DO_AES_CIPHER:
				doAesCipher(apdu, len);
				break;
			case TAO_DU_LIEU:
				taoDuLieu(apdu, len);
				break;
			default:
				ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
		}
	}
	
	private void checkInfoExist(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		
		byte[] res = {(byte)0x00};
		short resLen = (short)res.length;
		
        if(duLieu != null && duLieu.length > 0) res[0] = (byte)0x01;
        
		Util.arrayCopyNonAtomic(res, (short)0, buffer, (short)0, (short)resLen);
		apdu.setOutgoingAndSend((short)0, (short)resLen);
	}
	
	private void taoDuLieu(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		byte[] duLieuMoi = new byte[len];
        
        JCSystem.beginTransaction();
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, duLieuMoi, (short)0, len);
        duLieu = duLieuMoi;
        JCSystem.commitTransaction();
        
        byte[] res = {(byte)0x00};
		short resLen = (short)res.length;
		
        if(duLieuMoi != null && duLieuMoi.length == len) res[0] = (byte)0x01;
        
		Util.arrayCopyNonAtomic(res, (short)0, buffer, (short)0, (short)resLen);
		apdu.setOutgoingAndSend((short)0, (short)resLen);
	}
	
	//Set the key of AES Encrypt/Decrypt
    private void setAesKey(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        byte keyLen = 0;
        switch (buffer[ISO7816.OFFSET_P1])
        {
        case (byte)0x01:
            if (len != 16) // The length of key is 16 bytes
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            keyLen = (byte)16;
            break;
        case (byte)0x02:
            if (len != 24) //The length of key is 24 bytes
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            keyLen = (byte)24;
            break;
        case (byte)0x03:
            if (len != 32) //The length of key is 32 bytes
            {
                ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
            }
            keyLen = (byte)32;
            break;
        default:
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            break;
        }

        JCSystem.beginTransaction();
        //Copy the incoming AES Key value to the global variable 'aesKey'
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, aesKey, (short)0, len);
        aesKeyLen = keyLen;
        JCSystem.commitTransaction();
    }

	//Set AES ICV, ICV is the initial vector
    private void setAesICV(APDU apdu, short len)
    {
        if (len != 16)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        //Copy the incoming ICV value to the global variable 'aesICV'
        Util.arrayCopy(apdu.getBuffer(), ISO7816.OFFSET_CDATA, aesICV, (short)0, (short)16);
    }

	//Sets the Key data, and return the AESKey object. The plaintext length of input key data is 16/24/32 bytes.
    private Key getAesKey()
    {
        Key tempAesKey = null;
        switch (aesKeyLen)
        {
        case (byte)16:
            tempAesKey = tempAesKey1;
            break;
        case (byte)24:
            tempAesKey = tempAesKey2;
            break;
        case (byte)32:
            tempAesKey = tempAesKey3;
            break;
        default:
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
            break;
        }
		//Set the 'aesKey' key data value into the internal representation
        ((AESKey)tempAesKey).setKey(aesKey, (short)0);
        return tempAesKey;
    }
   
   //AES algorithm encrypt and decrypt
    private void doAesCipher(APDU apdu, short len)
    {
    	//The byte length to be encrypted/decrypted must be a multiple of 16
        if (len <= 0 || len % 16 != 0)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }

        byte[] buffer = apdu.getBuffer();
        Key key = getAesKey();
        byte mode = buffer[ISO7816.OFFSET_P1] == (byte)0x00 ? Cipher.MODE_ENCRYPT : Cipher.MODE_DECRYPT;
        Cipher cipher = buffer[ISO7816.OFFSET_P2] == (byte)0x00 ? aesEcbCipher : aesCbcCipher;
        //Initializes the 'cipher' object with the appropriate Key and algorithm specific parameters.
        //AES algorithms in CBC mode expect a 16-byte parameter value for the initial vector(IV)
        if (cipher == aesCbcCipher)
        {
            cipher.init(key, mode, aesICV, (short)0, (short)16);
        }
        else
        {
            cipher.init(key, mode);
        }
        //This method must be invoked to complete a cipher operation. Generates encrypted/decrypted output from all/last input data. 
        cipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
        apdu.setOutgoingAndSend((short)0, len);
    }
}
