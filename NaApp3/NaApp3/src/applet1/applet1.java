package applet1 ;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.*;
import javacardx.crypto.*;

public class applet1 extends Applet
{
	private static byte[] duLieu;
	private static byte[] pin;
	private static short soLanNhapSai = 0;
	private static short soLanNhapSaiToiDa = 3;
	private static boolean theBiKhoa = false;
	
	// INS map
	private static final byte INS_CHECK_INFO_EXIST         = (byte)0x13;
    private static final byte INS_TAO_DU_LIEU              = (byte)0x14;
    private static final byte INS_DANG_NHAP                = (byte)0x15;
	
	// AES
	private static final byte INS_SET_AES_KEY              = (byte)0x10;
    private static final byte INS_SET_AES_ICV              = (byte)0x11;
    private static final byte INS_DO_AES_CIPHER            = (byte)0x12;
        
    private byte aesKeyLen;
    private byte[] aesKey;
    private byte[] aesICV;

    private Cipher aesEcbCipher;
    private Cipher aesCbcCipher;

    private Key tempAesKey1;
    private Key tempAesKey2;
    private Key tempAesKey3;
    
    // RSA
    private static final byte INS_GEN_RSA_KEYPAIR          = (byte)0x30;
    private static final byte INS_GET_RSA_PUBKEY           = (byte)0x31;
    private static final byte INS_GET_RSA_PRIKEY           = (byte)0x32;
    private static final byte INS_SET_RSA_PUBKEY           = (byte)0x33;
    private static final byte INS_SET_RSA_PRIKEY           = (byte)0x34;
    private static final byte INS_RSA_SIGN                 = (byte)0x35;
    private static final byte INS_RSA_VERIFY               = (byte)0x36;
    private static final byte INS_DO_RSA_CIPHER            = (byte)0x37;

	private byte[] tempBuffer;

    private byte[] flags;
    private static final short OFF_INS    = (short)0;
    private static final short OFF_P1     = (short)1;
    private static final short OFF_P2     = (short)2;
    private static final short OFF_LEN    = (short)3;
    private static final short FLAGS_SIZE = (short)5;

	private static final byte ID_N   = 0;
    private static final byte ID_D   = 1;
    private static final byte ID_P   = 2;
    private static final byte ID_Q   = 3;
    private static final byte ID_PQ  = 4;
    private static final byte ID_DP1 = 5;
    private static final byte ID_DQ1 = 6;
    
	private byte[] rsaPubKey;
    private short rsaPubKeyLen;
    private byte[] rsaPriKey;
    private short rsaPriKeyLen;
    private boolean isRSAPriKeyCRT;
    private Cipher rsaCipher;    
    private Signature rsaSignature;
    
	private static final short SW_REFERENCE_DATA_NOT_FOUND = (short)0x6A88;
    
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

        //Create a transient byte array to store the temporary data
        tempBuffer = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        flags = JCSystem.makeTransientByteArray(FLAGS_SIZE, JCSystem.CLEAR_ON_DESELECT);

        rsaPubKey = new byte[(short)	(256 + 32)];
        rsaPriKey = new byte[(short)(128 * 5)];
        rsaPubKeyLen = 0;
        rsaPriKeyLen = 0;
        isRSAPriKeyCRT = false;
        rsaSignature = null;
        //Create a RSA(not pad) object instance
        rsaCipher = Cipher.getInstance(Cipher.ALG_RSA_NOPAD, false);

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
				byte sendStr[] = {'A', 'B', 'C'};
				short resLen = (short) sendStr.length;
				//Copy character to APDU Buffer.
				Util.arrayCopyNonAtomic(sendStr, (short)0, buf, (short)0, (short)resLen);
				//Send the 'sendStr' string, the hex of JCRE sending data is the ASCII of sendStr.
				apdu.setOutgoingAndSend((short)0, (short)resLen);
				break;
				
			case INS_SET_AES_KEY:
				setAesKey(apdu, len);
				break;
			case INS_CHECK_INFO_EXIST:
				checkInfoExist(apdu, len);
				break;
			case INS_SET_AES_ICV:
				setAesICV(apdu, len);
				break;
			case INS_DO_AES_CIPHER:
				doAesCipher(apdu, len);
				break;
			case INS_TAO_DU_LIEU:
				taoDuLieu(apdu, len);
				break;
			case INS_GEN_RSA_KEYPAIR:
				genRsaKeyPair(apdu, len);
				break;
			case INS_GET_RSA_PUBKEY:
				getRsaPubKey(apdu, len);
				break;
			case INS_GET_RSA_PRIKEY:
				getRsaPriKey(apdu, len);
				break;
			case INS_SET_RSA_PUBKEY:
				setRsaPubKey(apdu, len);
				break;
			case INS_SET_RSA_PRIKEY:
				setRsaPriKey(apdu, len);
				break;
			case INS_RSA_SIGN:
				rsaSign(apdu, len);
				break;
			case INS_RSA_VERIFY:
				rsaVerify(apdu, len);
				break;
			case INS_DO_RSA_CIPHER:
				doRSACipher(apdu, len);
				break;
			case INS_DANG_NHAP:
				dangNhap(apdu, len);
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
		short _pinLen = (short)buffer[ISO7816.OFFSET_CDATA];
		byte[] _pin = new byte[_pinLen];
		byte[] _duLieu = new byte[len];
        
        Util.arrayCopy(buffer, (short)(ISO7816.OFFSET_CDATA + (short)1), _pin, (short)0, _pinLen);
        Util.arrayCopy(buffer, (byte)(ISO7816.OFFSET_CDATA + (short)1 + _pinLen), _duLieu, (short)0, (short)(len - _pinLen));
        
        byte[] res = {(byte)0x00};
		short resLen = (short)res.length;
		
		if(_duLieu != null && _duLieu.length > 0 && _pin != null && _pin.length > 0) {
			duLieu = _duLieu;
			pin = _pin;
			res[0] = (byte)0x01;
		}

		Util.arrayCopyNonAtomic(res, (short)0, buffer, (short)0, (short)resLen);
		apdu.setOutgoingAndSend((short)0, (short)resLen);
	}
	
	private void dangNhap(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		byte[] _pin = new byte[len];
		
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, _pin, (short)0, len);
		
		byte[] res = {(byte)0x00};
		short resLen = (short)res.length;
		
		if(soLanNhapSai >= soLanNhapSaiToiDa) {
			res[0] = 0x02;
		}
		else if(len == (short)pin.length) {
			if(Util.arrayCompare(pin, (short)0, _pin, (short)0, len) == (byte)0x00) {
				res = duLieu;
				resLen = (short)res.length;
			}
		}
        
        if(resLen == 1 && res[0] == (byte)0x00) {
	        soLanNhapSai++;
        }
        
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

	//RSA algorithm encrypt and decrypt
	private void doRSACipher(APDU apdu, short len)
    {
    	byte[] buffer = apdu.getBuffer();
        byte p1Tmp = buffer[ISO7816.OFFSET_P1];
        boolean hasMoreCmd = (p1Tmp & 0x80) != 0;
        boolean isEncrypt = (p1Tmp & 0x01) != 1;
    	short keyLen = (p1Tmp & 0x08) == (byte)0x00 ? KeyBuilder.LENGTH_RSA_1024 : KeyBuilder.LENGTH_RSA_2048;
    	short offset = (p1Tmp & 0x08) == (byte)0x00 ? (short)128 : (short)256;
    	
        if (len <= 0)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        //RSA encrypt, Public Key will be used
        if (isEncrypt)
		{
			//Create uninitialized public key for signature and cipher algorithms.
			RSAPublicKey pubKey = (RSAPublicKey)KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, keyLen, false);
			pubKey.setModulus(rsaPubKey, (short)0, offset);
			pubKey.setExponent(rsaPubKey, offset, (short)3);
			if (buffer[ISO7816.OFFSET_P2] == 0x00)
			{
				//In multiple-part encryption/decryption operations, only the fist APDU command will be used.
				rsaCipher.init(pubKey, Cipher.MODE_ENCRYPT); 
			}
			
			if (hasMoreCmd)
			{
				//This method is intended for multiple-part encryption/decryption operations.
				rsaCipher.update(buffer, ISO7816.OFFSET_CDATA, len, tempBuffer, (short)0);
			}
			else
			{
				//Generates encrypted output from all input data.
				short outlen = rsaCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
				apdu.setOutgoingAndSend((short)0, outlen);	
			}
		}
		else//RSA decrypt, Private Key will be used
		{
			if (!isRSAPriKeyCRT)
            {
            	//RSA Alogrithm, create uninitialized private key for decypt
            	RSAPrivateKey priKey = (RSAPrivateKey)KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, keyLen, false);
            	//Set the modulus value of the key.
				priKey.setModulus(rsaPriKey, (short)0, offset);
				//Sets the private exponent value of the key
				priKey.setExponent(rsaPriKey, offset, offset);
				if (buffer[ISO7816.OFFSET_P2] == 0x00)
				{
					//In multiple-part encryption/decryption operations, only the fist APDU command will be used.
					rsaCipher.init(priKey, Cipher.MODE_DECRYPT);
				}
				if (hasMoreCmd)
				{
					//This method is intended for multiple-part encryption/decryption operations.
					rsaCipher.update(buffer, ISO7816.OFFSET_CDATA, len, tempBuffer, (short)0);
				}
				else
				{
					short outlen = rsaCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
					apdu.setOutgoingAndSend((short)0, outlen);	
				}
            }
            else 
            {
            	//RSA CRT Algorithm, need to create uninitialized private key and set the value of some parameters, such as P Q PQ DP DQ.
            	RSAPrivateCrtKey priCrtKey = (RSAPrivateCrtKey)KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_CRT_PRIVATE, keyLen, false);
                priCrtKey.setP(rsaPriKey, (short)0, (short)(offset / 2));
                priCrtKey.setQ(rsaPriKey, (short)(offset / 2), (short)(offset / 2));
                priCrtKey.setPQ(rsaPriKey, (short)offset, (short)(offset / 2));
                priCrtKey.setDP1(rsaPriKey, (short)(offset + offset / 2), (short)(offset / 2));
                priCrtKey.setDQ1(rsaPriKey, (short)(offset * 2), (short)(offset / 2));
                
                if (buffer[ISO7816.OFFSET_P2] == 0x00)
				{
					//Initializes the Cipher object with the appropriate Key. 
					//In multiple-part encryption/decryption operations, only the fist APDU command will be used.
					rsaCipher.init(priCrtKey, Cipher.MODE_DECRYPT);
				}
				if (hasMoreCmd)
				{
					//This method is intended for multiple-part encryption/decryption operations.
					rsaCipher.update(buffer, ISO7816.OFFSET_CDATA, len, tempBuffer, (short)0);
				}
				else
				{
					//Generates decrypted output from all input data.
					short outlen = rsaCipher.doFinal(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
					apdu.setOutgoingAndSend((short)0, outlen);	
				}
            }
		}        
    }
    
    //Get the value of RSA Public Key from the global variable 'rsaPubKey' 
    private void getRsaPubKey(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if (rsaPubKeyLen == 0)
        {
            ISOException.throwIt(SW_REFERENCE_DATA_NOT_FOUND);
        }

        short modLen = rsaPubKeyLen <= (128 + 32) ? (short)128 : (short)256;
        switch (buffer[ISO7816.OFFSET_P1])
        {
        case 0:
            Util.arrayCopyNonAtomic(rsaPubKey,(short)0,buffer,(short)0,modLen);
            apdu.setOutgoingAndSend((short)0,modLen);
            break;
        case 1:
            //get public key E
            short eLen = (short)(rsaPubKeyLen - modLen);
            Util.arrayCopyNonAtomic(rsaPubKey,modLen,buffer,(short)0,eLen);
            apdu.setOutgoingAndSend((short)0,eLen);
            break;
        default:
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            break;
        }

    }
	//According to the different ID, returns the value/length of RSA Private component
    private short getRsaPriKeyComponent(byte id, byte[] outBuff, short outOff)
    {
        if (rsaPriKeyLen == 0)
        {
            return (short)0;
        }
        short modLen;
        if (isRSAPriKeyCRT)
        {
            if (rsaPriKeyLen == 64 * 5)
            {
                modLen = (short)128;
            }
            else
            {
                modLen = (short)256;
            }
        }
        else
        {
            if (rsaPriKeyLen == 128 * 2)
            {
                modLen = (short)128;
            }
            else
            {
                modLen = (short)256;
            }
        }
        short readOff;
        short readLen;

        switch (id)
        {
        case ID_N:
            //RSA private key N
            if (isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)0;
            readLen = modLen;
            break;
        case ID_D:
            if (isRSAPriKeyCRT)
            {
                return (short)0;
            }
            //RSA private key D
            readOff = modLen;
            readLen = modLen;
            break;
        case ID_P:
            if (!isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)0;
            readLen = (short)(modLen / 2);
            break;
        case ID_Q:
            if (!isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)(modLen / 2);
            readLen = (short)(modLen / 2);
            break;
        case ID_PQ:
            if (!isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)(modLen);
            readLen = (short)(modLen / 2);
            break;
        case ID_DP1:
            if (!isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)(modLen / 2 * 3);
            readLen = (short)(modLen / 2);
            break;
        case ID_DQ1:
            if (!isRSAPriKeyCRT)
            {
                return (short)0;
            }
            readOff = (short)(modLen * 2);
            readLen = (short)(modLen / 2);
            break;
        default:
            return 0;
        }
        Util.arrayCopyNonAtomic(rsaPriKey, readOff, outBuff, outOff, readLen);
        return readLen;
    }

	//Get the value of RSA Private Key
    private void getRsaPriKey(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if ((buffer[ISO7816.OFFSET_P1] & 0xff) > 6)
        {
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
        }
        short ret = getRsaPriKeyComponent(buffer[ISO7816.OFFSET_P1], buffer, (short)0);
        if (ret == 0)
        {
            ISOException.throwIt(SW_REFERENCE_DATA_NOT_FOUND);
        }
        apdu.setOutgoingAndSend((short)0, ret);
    }

	//Set the value of RSA public key
    private void setRsaPubKey(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if (buffer[ISO7816.OFFSET_P2] == 0) // first block
        {
            rsaPubKeyLen = (short)0;
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_INS, flags, OFF_INS, (short)3);
            Util.setShort(flags, OFF_LEN, (short)0);
        }
        else
        {
            if (flags[OFF_INS] != buffer[ISO7816.OFFSET_INS]
                    || (flags[OFF_P1] & 0x7f) != (buffer[ISO7816.OFFSET_P1] & 0x7f)
                    || (short)(flags[OFF_P2] & 0xff) != (short)((buffer[ISO7816.OFFSET_P2] & 0xff) - 1))
            {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }

            flags[OFF_P2] ++;
        }
        short loadedLen = Util.getShort(flags, OFF_LEN);
        if (loadedLen + len > rsaPubKey.length)
        {
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
		//Copy the value of RSA public key  to the global variable 'rsaPubKey'. 
        Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, rsaPubKey, loadedLen, len);
        loadedLen += len;

        if ((buffer[ISO7816.OFFSET_P1] & 0x80) == 0) //last block
        {
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            short modLen = (buffer[ISO7816.OFFSET_P1] & 0x01) == 0 ? (short)128 : (short)256;
            if (loadedLen < modLen + 3 || loadedLen > modLen + 32)
            {
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            }

            rsaPubKeyLen = loadedLen;
        }
        else
        {
            Util.setShort(flags, OFF_LEN, loadedLen);
        }

    }

	//Set the value of RSA private key
    private void setRsaPriKey(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if (buffer[ISO7816.OFFSET_P2] == 0) // first block
        {
            rsaPriKeyLen = (short)0;
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_INS, flags, OFF_INS, (short)3);
            Util.setShort(flags, OFF_LEN, (short)0);
        }
        else
        {
            if (flags[OFF_INS] != buffer[ISO7816.OFFSET_INS]
                    || (flags[OFF_P1] & 0x7f) != (buffer[ISO7816.OFFSET_P1] & 0x7f)
                    || (short)(flags[OFF_P2] & 0xff) != (short)((buffer[ISO7816.OFFSET_P2] & 0xff) - 1))
            {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }

            flags[OFF_P2] ++;
        }
        short loadedLen = Util.getShort(flags, OFF_LEN);
        if (loadedLen + len > rsaPriKey.length)
        {
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            ISOException.throwIt(ISO7816.SW_WRONG_DATA);
        }
		//Copy the value of RSA private key  to the global variable 'rsaPriKey'.
		Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, rsaPriKey, loadedLen, len);
        loadedLen += len;

        if ((buffer[ISO7816.OFFSET_P1] & 0x80) == 0) //last block
        {
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            short modLen = (buffer[ISO7816.OFFSET_P1] & 0x01) == 0 ? (short)128 : (short)256;
            boolean isCRT = (buffer[ISO7816.OFFSET_P1] & 0x40) != 0;
            if ((isCRT && (loadedLen != modLen / 2 * 5)) || (!isCRT && (loadedLen != modLen * 2)))
            {
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            }

            isRSAPriKeyCRT = isCRT;
            rsaPriKeyLen = loadedLen;
        }
        else
        {
            Util.setShort(flags, OFF_LEN, loadedLen);
        }
    }
    //
	private void genRsaKeyPair(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        short keyLen = buffer[ISO7816.OFFSET_P1] == 0 ? (short)1024 : (short)2048;
        byte alg = buffer[ISO7816.OFFSET_P2] == 0 ? KeyPair.ALG_RSA : KeyPair.ALG_RSA_CRT;
        KeyPair keyPair = new KeyPair(alg, keyLen);
        if (len > 32)
        {
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
        }
        if (len > 0)
        {
            ((RSAPublicKey)keyPair.getPublic()).setExponent(buffer, ISO7816.OFFSET_CDATA, len);
        }
        //(Re)Initializes the key objects encapsulated in this KeyPair instance with new key values.
        keyPair.genKeyPair();
        JCSystem.beginTransaction();
        rsaPubKeyLen = 0;
        rsaPriKeyLen = 0;
        JCSystem.commitTransaction();
        //Get a reference to the public key component of this 'keyPair' object.
        RSAPublicKey pubKey = (RSAPublicKey)keyPair.getPublic();
        short pubKeyLen = 0;
        //Store the RSA public key value in the global variable 'rsaPubKey', the public key contains modulo N and Exponent E
        pubKeyLen += pubKey.getModulus(rsaPubKey, pubKeyLen);
        pubKeyLen += pubKey.getExponent(rsaPubKey, pubKeyLen);

        short priKeyLen = 0;
        if (alg == KeyPair.ALG_RSA)
        {
        	isRSAPriKeyCRT = false;
        	//Returns a reference to the private key component of this KeyPair object.
            RSAPrivateKey priKey = (RSAPrivateKey)keyPair.getPrivate();
            //RSA Algorithm,  the Private Key contains N and D, and store these parameters value in global variable 'rsaPriKey'.
            priKeyLen += priKey.getModulus(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getExponent(rsaPriKey, priKeyLen);
        }
        else //RSA CRT
        {
        	isRSAPriKeyCRT =  true;
        	//The RSAPrivateCrtKey interface is used to sign data using the RSA algorithm in its Chinese Remainder Theorem form.
            RSAPrivateCrtKey priKey = (RSAPrivateCrtKey)keyPair.getPrivate();
            //RSA CRT Algorithm,  the Private Key contains P Q PQ DP and DQ, and store these parameters value in global variable 'rsaPriKey'.
            priKeyLen += priKey.getP(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getQ(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getPQ(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getDP1(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getDQ1(rsaPriKey, priKeyLen);
        }

        JCSystem.beginTransaction();
        rsaPubKeyLen = pubKeyLen;
        rsaPriKeyLen = priKeyLen;
        JCSystem.commitTransaction();

        JCSystem.requestObjectDeletion();
    }
    //RSA Signature
    private void rsaSign(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if (rsaPriKeyLen == 0)
        {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        boolean hasMoreCmd = (buffer[ISO7816.OFFSET_P1] & 0x80) != 0;
        short resultLen = 0;
        if (buffer[ISO7816.OFFSET_P2] == 0) //first block
        {
            Key key;
            if (!isRSAPriKeyCRT)
            {
                short ret;
                //Creates uninitialized private keys for signature algorithms.
                key = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, (short)(rsaPriKeyLen / 2 * 8), false);
                ret = getRsaPriKeyComponent(ID_N, tempBuffer, (short)0);
                ((RSAPrivateKey)key).setModulus(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_D, tempBuffer, (short)0);
                ((RSAPrivateKey)key).setExponent(tempBuffer, (short)0, ret);
            }
            else
            {
                short ret;
                //Creates uninitialized private keys for signature algorithms.
                key = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_CRT_PRIVATE, (short)(rsaPriKeyLen / 5 * 16), false);
                ret = getRsaPriKeyComponent(ID_P, tempBuffer, (short)0);
                ((RSAPrivateCrtKey)key).setP(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_Q, tempBuffer, (short)0);
                ((RSAPrivateCrtKey)key).setQ(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_DP1, tempBuffer, (short)0);
                ((RSAPrivateCrtKey)key).setDP1(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_DQ1, tempBuffer, (short)0);
                ((RSAPrivateCrtKey)key).setDQ1(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_PQ, tempBuffer, (short)0);
                ((RSAPrivateCrtKey)key).setPQ(tempBuffer, (short)0, ret);
            }
			// Creates a Signature object instance of the ALG_RSA_SHA_PKCS1 algorithm.
            rsaSignature = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
            JCSystem.requestObjectDeletion();
			//Initializ the Signature object.
            rsaSignature.init(key, Signature.MODE_SIGN);

            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_INS, flags, OFF_INS, (short)3);
            JCSystem.requestObjectDeletion();
        }
        else
        {
            if (flags[OFF_INS] != buffer[ISO7816.OFFSET_INS]
                    || (flags[OFF_P1] & 0x7f) != (buffer[ISO7816.OFFSET_P1] & 0x7f)
                    || (short)(flags[OFF_P2] & 0xff) != (short)((buffer[ISO7816.OFFSET_P2] & 0xff) - 1))
            {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }

            flags[OFF_P2] ++;
        }

        if (hasMoreCmd)
        {
        	// Accumulates a signature of the input data. 
            rsaSignature.update(buffer, ISO7816.OFFSET_CDATA, len);
        }
        else
        {
        	//Generates the signature of all input data.
            short ret = rsaSignature.sign(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            apdu.setOutgoingAndSend((short)0, ret);
        }
    }
    //RSA Signature and Verify
    private void rsaVerify(APDU apdu, short len)
    {
        byte[] buffer = apdu.getBuffer();
        if (rsaPubKeyLen == 0)
        {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        boolean hasMoreCmd = (buffer[ISO7816.OFFSET_P1] & 0x80) != 0;
        short resultLen = 0;
        short offset = ISO7816.OFFSET_CDATA;
        short modLen = rsaPubKeyLen > 256 ? (short)256 : (short)128;
        if (buffer[ISO7816.OFFSET_P2] == 0) //first block
        {
            Key key;
            // Create uninitialized public keys for signature  algorithms.
            key = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PUBLIC, (short)(modLen * 8), false);
            //Sets the modulus value of the key. 
            ((RSAPublicKey)key).setModulus(rsaPubKey, (short)0, modLen);
            //Sets the public exponent value of the key.
            ((RSAPublicKey)key).setExponent(rsaPubKey, modLen, (short)(rsaPubKeyLen - modLen));

			//Create a ALG_RSA_SHA_PKCS1 object instance.
            rsaSignature = Signature.getInstance(Signature.ALG_RSA_SHA_PKCS1, false);
            JCSystem.requestObjectDeletion();
            //Initializes the Signature object with the appropriate Key. 
            rsaSignature.init(key, Signature.MODE_VERIFY);
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_INS, flags, OFF_INS, (short)3);
            Util.setShort(flags, OFF_LEN, (short)0);
            JCSystem.requestObjectDeletion();
        }
        else
        {
            if (flags[OFF_INS] != buffer[ISO7816.OFFSET_INS]
                    || (flags[OFF_P1] & 0x7f) != (buffer[ISO7816.OFFSET_P1] & 0x7f)
                    || (short)(flags[OFF_P2] & 0xff) != (short)((buffer[ISO7816.OFFSET_P2] & 0xff) - 1))
            {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }

            flags[OFF_P2] ++;
        }

        short sigLen = Util.getShort(flags, OFF_LEN);
        if (sigLen < modLen)
        {
            short readLen = (short)(modLen - sigLen);
            if (readLen > len)
            {
                readLen = len;
            }
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, tempBuffer, sigLen, readLen);
            sigLen += readLen;
            len -= readLen;
            Util.setShort(flags, OFF_LEN, sigLen);
            offset += readLen;
        }
        if (hasMoreCmd)
        {
            if (len > 0)
            {
            	//Accumulates a signature of the input data. 
                rsaSignature.update(buffer, offset, len);
            }
        }
        else
        {
            if (sigLen != modLen)
            {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_WRONG_DATA);
            }
            //Verify the signature of all/last input data against the passed in signature.
            boolean ret = rsaSignature.verify(buffer, offset, len, tempBuffer, (short)0, sigLen);
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            buffer[(short)0] = ret ? (byte)1 : (byte)0;
            apdu.setOutgoingAndSend((short)0, (short)1);
        }
    }
}
