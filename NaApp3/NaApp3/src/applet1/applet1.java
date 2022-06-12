package applet1 ;

import javacard.framework.*;
import javacard.security.KeyBuilder;
import javacard.security.*;
import javacardx.crypto.*;
import javacardx.apdu.ExtendedLength;

public class applet1 extends Applet implements ExtendedLength {
	private static byte[] duLieuDaMaHoa;
	private static short soLanNhapSai = 0;
	private static short soLanNhapSaiToiDa = 3;
	private static boolean theBiKhoa = false;
	private static short boiSoAES = 16;
	
	// Dung luong toi da cua data trong buffer
	// Cung la dung luong toi da cua kieu du lieu `short`
	private static final short MAX_LENGTH = (short)(0x7FFF);
	private static byte[] tempExtendData;
	private static short tempExtendLen;
	
	// INS map
	private static final byte INS_KIEM_TRA_DU_LIEU_TON_TAI = (byte)0x13;
    private static final byte INS_TAO_DU_LIEU              = (byte)0x14;
    private static final byte INS_DANG_NHAP                = (byte)0x15;
    private static final byte INS_XOA_DU_LIEU              = (byte)0x16;
    private static final byte INS_CAP_NHAT_DU_LIEU         = (byte)0x17;
    
    // Thu vien RSA
    private static final byte INS_GEN_RSA_KEYPAIR          = (byte)0x30;
    private static final byte INS_GET_RSA_PUBKEY           = (byte)0x31;
    private static final byte INS_RSA_SIGN                 = (byte)0x35;
    private byte[] tempBuffer;
    private byte[] flags;
    private static final short OFF_INS    = (short)0;
    private static final short OFF_P1     = (short)1;
    private static final short OFF_P2     = (short)2;
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
    private Signature rsaSignature;
	private static final short SW_REFERENCE_DATA_NOT_FOUND = (short)0x6A88;
    
    public applet1() {
        // Thu vien RSA
        tempBuffer = JCSystem.makeTransientByteArray((short)256, JCSystem.CLEAR_ON_DESELECT);
        flags = JCSystem.makeTransientByteArray(FLAGS_SIZE, JCSystem.CLEAR_ON_DESELECT);
        rsaPubKey = new byte[(short)(256 + 32)];
        rsaPriKey = new byte[(short)(128 * 5)];
        rsaPubKeyLen = 0;
        rsaPriKeyLen = 0;
        isRSAPriKeyCRT = false;
        rsaSignature = null;
        JCSystem.requestObjectDeletion();
    }

	public static void install(byte[] bArray, short bOffset, byte bLength) {
		new applet1().register(bArray, (short) (bOffset + 1), bArray[bOffset]);
	}

	public void process(APDU apdu) {
		if (selectingApplet()) {
			return;
		}

		// Get the apdu buffer datas
        byte[] buf = apdu.getBuffer();
        byte cla = buf[ISO7816.OFFSET_CLA];
        byte ins = buf[ISO7816.OFFSET_INS];
        // byte p1 = buf[ISO7816.OFFSET_P1];
        // byte p2 = buf[ISO7816.OFFSET_P2];
        // short p1p2 = Util.makeShort(p1, p2);
        // short lc = apdu.getIncomingLength();
        short len = apdu.setIncomingAndReceive();
        
        // Chi chap nhan CLA 0x80
        if (cla != (byte)0x80) ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        
        boolean hasRouted = false;
        
        // INS khong bi anh huong boi dieu kien
        switch(ins) {
	        // Test
			case (byte)0x00:
				// Define a byte string
				byte sendStr[] = {'A', 'B', 'C'};
				short resLen = (short) sendStr.length;
				// Copy character to APDU Buffer.
				Util.arrayCopyNonAtomic(sendStr, (short)0, buf, (short)0, (short)resLen);
				// Send the 'sendStr' string, the hex of JCRE sending data is the ASCII of sendStr.
				apdu.setOutgoingAndSend((short)0, (short)resLen);
				hasRouted = true;
				break;
			case INS_KIEM_TRA_DU_LIEU_TON_TAI:
				kiemTraDuLieuTonTai(apdu, len);
				hasRouted = true;
				break;
			case INS_GET_RSA_PUBKEY:
				getRsaPubKey(apdu, len);
				hasRouted = true;
				break;
        }
        
        if(hasRouted) return;
        
        // Neu the bi khoa se khong truy cap duoc cac INS ben duoi
        if(theBiKhoa) ISOException.throwIt(ISO7816.SW_LOGICAL_CHANNEL_NOT_SUPPORTED);
        
        // Neu the co du lieu chi co the truy cap cac INS ben trong nay
        if(coDuLieuTrongThe()) {
	        switch (ins) {
				case INS_DANG_NHAP:
					dangNhap(apdu, len);
					break;
				case INS_XOA_DU_LIEU:
					xoaDuLieu(apdu, len);
					break;
				case INS_RSA_SIGN:
					rsaSign(apdu, len);
					break;
				case INS_CAP_NHAT_DU_LIEU:
					capNhatDuLieu(apdu, len);
					break;
				default:
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
		// Neu the khong co du lieu chi co the truy cap cac INS ben trong nay
		else {
			switch (ins) {
				case INS_GEN_RSA_KEYPAIR:
					genRsaKeyPair(apdu, len);
					break;
				case INS_TAO_DU_LIEU:
					taoDuLieu(apdu, len);
					break;
				case INS_GET_RSA_PUBKEY:
					getRsaPubKey(apdu, len);
					break;
				default:
					ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
			}
		}
	}
	
	private boolean coDuLieuTrongThe() {
		return duLieuDaMaHoa != null && duLieuDaMaHoa.length > 0;
	}
	
	private void kiemTraDuLieuTonTai(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		
		byte[] res = {coDuLieuTrongThe() ? (byte)0x01 : (byte)0x00};
		short resLen = (short)res.length;
        
		Util.arrayCopyNonAtomic(res, (short)0, buffer, (short)0, (short)resLen);
		apdu.setOutgoingAndSend((short)0, (short)resLen);
	}
	
	private void taoDuLieu(APDU apdu, short len) {
		layDuLieuTuBuffer(apdu, len);
		
		// Lay ra PIN va du lieu tu buffer
		short _pinLen = boiSoAES; // Do dai PIN sau khi pad = boiSoAES => _pin.length <= boiSoAES (bat buoc)
		byte[] _pin = new byte[_pinLen];
		byte[] _duLieu = new byte[tempExtendLen];
        Util.arrayCopy(tempExtendData, (short)0, _pin, (short)0, _pinLen);
        Util.arrayCopy(tempExtendData, (short)0, _duLieu, (short)0, tempExtendLen);
        
        // Kiem tra dau vao
        kiemTraBoiSo(_pin, boiSoAES);
        kiemTraBoiSo(_duLieu, boiSoAES);
		
		// Ma hoa du lieu
		duLieuDaMaHoa = doAesCipherComponent(_duLieu, _pin, (byte)0);
	}
	
	private void dangNhap(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		byte[] _pin = new byte[len];
		
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, _pin, (short)0, len);
        
        kiemTraBoiSo(_pin, boiSoAES);
		
		// Gia dinh ma PIN sai va tang so lan nhap sai
		soLanNhapSai++;
		if(soLanNhapSai >= soLanNhapSaiToiDa)
        	theBiKhoa = true;
        	
		xacThucPIN(_pin);
		
		// Neu code chay toi doan nay thi da xac thuc dung
		// => reset so lan nhap sai & trang thai khoa the
		soLanNhapSai = 0;
		theBiKhoa = false;
		
		byte[] _duLieuBanRo = doAesCipherComponent(duLieuDaMaHoa, _pin, (byte)0x01);
		byte[] res = _duLieuBanRo;
		short resLen = (short)res.length;
		
		apdu.setOutgoing();
		apdu.setOutgoingLength(resLen);
		apdu.sendBytesLong(res, (short)0, resLen);
	}
	
	private void capNhatDuLieu(APDU apdu, short len) {
		layDuLieuTuBuffer(apdu, len);
		
		// Lay ra PIN va du lieu tu buffer
		short _pinLen = boiSoAES; // Do dai PIN sau khi pad = boiSoAES => _pin.length <= boiSoAES (bat buoc)
		short _oldPinLen = boiSoAES;
		byte[] _pin = new byte[_pinLen];
		byte[] _oldPin = new byte[_oldPinLen];
		short _duLieuLen = (short)(tempExtendLen - _oldPinLen);
		byte[] _duLieu = new byte[_duLieuLen];
        Util.arrayCopy(tempExtendData, (short)0, _oldPin, (short)0, _oldPinLen);
        Util.arrayCopy(tempExtendData, _oldPinLen, _pin, (short)0, _pinLen);
        Util.arrayCopy(tempExtendData, _oldPinLen, _duLieu, (short)0, _duLieuLen);
        
        // Kiem tra du lieu & xac thuc ma PIN
        kiemTraBoiSo(_oldPin, boiSoAES);
        kiemTraBoiSo(_pin, boiSoAES);
        kiemTraBoiSo(_duLieu, boiSoAES);
        xacThucPIN(_oldPin);
		
		duLieuDaMaHoa = doAesCipherComponent(_duLieu, _pin, (byte)0x00);
		JCSystem.requestObjectDeletion();
	}
	
	private void xoaDuLieu(APDU apdu, short len) {
		byte[] buffer = apdu.getBuffer();
		
		// Lay ra PIN tu buffer
		byte[] _pin = new byte[len];
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, _pin, (short)0, len);
        
        kiemTraBoiSo(_pin, boiSoAES);
        xacThucPIN(_pin);
        
        duLieuDaMaHoa = null;
        JCSystem.requestObjectDeletion();
	}
    
    private byte[] doAesCipherComponent(byte[] message, byte[] _key, byte _mode) {
        kiemTraBoiSo(_key, boiSoAES);
        
    	short len = (short)message.length;
    	byte[] result = new byte[len];
    	
        AESKey key = (AESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
        key.setKey(_key, (short)0);
        
        byte mode = _mode == (byte)0x00 ? Cipher.MODE_ENCRYPT : Cipher.MODE_DECRYPT;
        
        Cipher aesCipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_ECB_NOPAD, false);
        aesCipher.init(key, mode);
        aesCipher.doFinal(message, (short)0, len, result, (short)0);
        
        return result;
    }
    
    private void kiemTraBoiSo(byte[] _pin, short boiSo) {
    	if(_pin == null || _pin.length == 0)
	    	ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    	else if ((short)_pin.length == 0 || (short)_pin.length % boiSo != 0)
            ISOException.throwIt(ISO7816.SW_WRONG_LENGTH);
    }
    
    private void xacThucPIN(byte[] _pin) {
	    byte[] _duLieuBanRo = doAesCipherComponent(duLieuDaMaHoa, _pin, (byte)0x01);
		if((short)_duLieuBanRo.length > (short)_pin.length)
			if(Util.arrayCompare(_duLieuBanRo, (short)0, _pin, (short) 0, (short)_pin.length) == 0)
				return;
				
		ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
    }
    
    private void layDuLieuTuBuffer(APDU apdu, short recvLen) {
    	byte[] buffer = apdu.getBuffer();
	    tempExtendLen = 0;
		tempExtendData = new byte[MAX_LENGTH];
		short offData = apdu.getOffsetCdata();
		// Recvieve data and put them in a byte array 'tmp_memory'
		while (recvLen > (short) 0) {
			Util.arrayCopy(buffer, offData, tempExtendData, tempExtendLen, recvLen);
			tempExtendLen += recvLen;
			//Gets as many data bytes as will fit without APDU buffer overflow
			recvLen = apdu.receiveBytes(offData);
		}
    }





	// THU VIEN RSA
    
    // Get the value of RSA Public Key from the global variable 'rsaPubKey' 
    private void getRsaPubKey(APDU apdu, short len) {
        byte[] buffer = apdu.getBuffer();
        if (rsaPubKeyLen == 0) {
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
            // get public key E
            short eLen = (short)(rsaPubKeyLen - modLen);
            Util.arrayCopyNonAtomic(rsaPubKey,modLen,buffer,(short)0,eLen);
            apdu.setOutgoingAndSend((short)0,eLen);
            break;
        default:
            ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            break;
        }
    }

    // Tao RSA key pair va ma hoa private key bang ma PIN (dung AES)
	private void genRsaKeyPair(APDU apdu, short len) {
        byte[] buffer = apdu.getBuffer();
        
        // Lay ra pin tu buffer
        byte[] _pin = new byte[len];
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, _pin, (short)0, len);
        
        kiemTraBoiSo(_pin, boiSoAES);
        
        short keyLen = buffer[ISO7816.OFFSET_P1] == 0 ? (short)1024 : (short)2048;
        byte alg = buffer[ISO7816.OFFSET_P2] == 0 ? KeyPair.ALG_RSA : KeyPair.ALG_RSA_CRT;
        KeyPair keyPair = new KeyPair(alg, keyLen);
        
        //(Re)Initializes the key objects encapsulated in this KeyPair instance with new key values.
        keyPair.genKeyPair();
        JCSystem.beginTransaction();
        rsaPubKeyLen = 0;
        rsaPriKeyLen = 0;
        JCSystem.commitTransaction();
        
        // Get a reference to the public key component of this 'keyPair' object.
        RSAPublicKey pubKey = (RSAPublicKey)keyPair.getPublic();
        short pubKeyLen = 0;
        
        // Store the RSA public key value in the global variable 'rsaPubKey', the public key contains modulo N and Exponent E
        pubKeyLen += pubKey.getModulus(rsaPubKey, pubKeyLen);
        pubKeyLen += pubKey.getExponent(rsaPubKey, pubKeyLen);

        short priKeyLen = 0;
        if (alg == KeyPair.ALG_RSA) {
        	isRSAPriKeyCRT = false;
        	// Returns a reference to the private key component of this KeyPair object.
            RSAPrivateKey priKey = (RSAPrivateKey)keyPair.getPrivate();
            // RSA Algorithm,  the Private Key contains N and D, and store these parameters value in global variable 'rsaPriKey'.
            priKeyLen += priKey.getModulus(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getExponent(rsaPriKey, priKeyLen);
        }
        else // RSA CRT
        {
        	isRSAPriKeyCRT =  true;
        	// The RSAPrivateCrtKey interface is used to sign data using the RSA algorithm in its Chinese Remainder Theorem form.
            RSAPrivateCrtKey priKey = (RSAPrivateCrtKey)keyPair.getPrivate();
            // RSA CRT Algorithm,  the Private Key contains P Q PQ DP and DQ, and store these parameters value in global variable 'rsaPriKey'.
            priKeyLen += priKey.getP(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getQ(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getPQ(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getDP1(rsaPriKey, priKeyLen);
            priKeyLen += priKey.getDQ1(rsaPriKey, priKeyLen);
        }

		// rsaPriKey = doAesCipherComponent(rsaPriKey, _pin, (byte)0x00);

        JCSystem.beginTransaction();
        rsaPubKeyLen = pubKeyLen;
        rsaPriKeyLen = priKeyLen;
        JCSystem.commitTransaction();

        JCSystem.requestObjectDeletion();
    }
    
    // According to the different ID, returns the value/length of RSA Private component
    private short getRsaPriKeyComponent(byte id, byte[] outBuff, short outOff) {
        if (rsaPriKeyLen == 0) {
            return (short)0;
        }
        short modLen;
        if (isRSAPriKeyCRT) {
            if (rsaPriKeyLen == 64 * 5) {
                modLen = (short)128;
            }
            else  {
                modLen = (short)256;
            }
        }
        else {
            if (rsaPriKeyLen == 128 * 2) {
                modLen = (short)128;
            }
            else {
                modLen = (short)256;
            }
        }
        short readOff;
        short readLen;

        switch (id)
        {
        case ID_N:
            // RSA private key N
            if (isRSAPriKeyCRT) {
                return (short)0;
            }
            readOff = (short)0;
            readLen = modLen;
            break;
        case ID_D:
            if (isRSAPriKeyCRT) {
                return (short)0;
            }
            // RSA private key D
            readOff = modLen;
            readLen = modLen;
            break;
        case ID_P:
            if (!isRSAPriKeyCRT) {
                return (short)0;
            }
            readOff = (short)0;
            readLen = (short)(modLen / 2);
            break;
        case ID_Q:
            if (!isRSAPriKeyCRT) {
                return (short)0;
            }
            readOff = (short)(modLen / 2);
            readLen = (short)(modLen / 2);
            break;
        case ID_PQ:
            if (!isRSAPriKeyCRT) {
                return (short)0;
            }
            readOff = (short)(modLen);
            readLen = (short)(modLen / 2);
            break;
        case ID_DP1:
            if (!isRSAPriKeyCRT) {
                return (short)0;
            }
            readOff = (short)(modLen / 2 * 3);
            readLen = (short)(modLen / 2);
            break;
        case ID_DQ1:
            if (!isRSAPriKeyCRT) {
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
    
    // RSA Signature
    private void rsaSign(APDU apdu, short len) {
        byte[] buffer = apdu.getBuffer();
        
        // Lay ra pin tu buffer
        byte[] _pin = new byte[len];
        Util.arrayCopy(buffer, ISO7816.OFFSET_CDATA, _pin, (short)0, len);
        
        if (rsaPriKeyLen == 0) {
            ISOException.throwIt(ISO7816.SW_CONDITIONS_NOT_SATISFIED);
        }
        
        boolean hasMoreCmd = (buffer[ISO7816.OFFSET_P1] & 0x80) != 0;
        if (buffer[ISO7816.OFFSET_P2] == 0) //first block
        {
            Key key;
            if (!isRSAPriKeyCRT) {
                short ret;
                // Creates uninitialized private keys for signature algorithms.
                key = KeyBuilder.buildKey(KeyBuilder.TYPE_RSA_PRIVATE, (short)(rsaPriKeyLen / 2 * 8), false);
                ret = getRsaPriKeyComponent(ID_N, tempBuffer, (short)0);
                ((RSAPrivateKey)key).setModulus(tempBuffer, (short)0, ret);
                ret = getRsaPriKeyComponent(ID_D, tempBuffer, (short)0);
                ((RSAPrivateKey)key).setExponent(tempBuffer, (short)0, ret);
            }
            else {
                short ret;
                // Creates uninitialized private keys for signature algorithms.
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
			// Initializ the Signature object.
            rsaSignature.init(key, Signature.MODE_SIGN);

            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_INS, flags, OFF_INS, (short)3);
            JCSystem.requestObjectDeletion();
        }
        else {
            if (flags[OFF_INS] != buffer[ISO7816.OFFSET_INS]
                    || (flags[OFF_P1] & 0x7f) != (buffer[ISO7816.OFFSET_P1] & 0x7f)
                    || (short)(flags[OFF_P2] & 0xff) != (short)((buffer[ISO7816.OFFSET_P2] & 0xff) - 1)) {
                Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
                ISOException.throwIt(ISO7816.SW_INCORRECT_P1P2);
            }
            flags[OFF_P2] ++;
        }

        if (hasMoreCmd) {
        	// Accumulates a signature of the input data. 
            rsaSignature.update(buffer, ISO7816.OFFSET_CDATA, len);
        }
        else {
        	// Generates the signature of all input data.
            short ret = rsaSignature.sign(buffer, ISO7816.OFFSET_CDATA, len, buffer, (short)0);
            Util.arrayFillNonAtomic(flags, (short)0, (short)flags.length, (byte)0);
            apdu.setOutgoingAndSend((short)0, ret);
        }
    }
}
