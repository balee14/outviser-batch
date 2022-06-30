package com.enliple.outviserbatch.common.util;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;
import javax.crypto.spec.DESedeKeySpec;

public class DesEncryptUtils {
	
	/**
     * 고정키 정보
     * @return
     */
    public static String key() {
        return "OUu4ZgztMgjkmrlk7WaHg51x";
    }
    
    /**
     * 키값
     * 24바이트인 경우 TripleDES 아니면 DES
     * @return
     * @throws Exception
     */
    public static Key getKey() throws Exception {
    	//System.out.println("키의 길이 : "+key().length());
        return (key().length() == 24) ? getKey2(key()) : getKey1(key());
    }

    /**
     * 지정된 비밀키를 가지고 오는 메서드 (DES)
     * require Key Size : 16 bytes
     *
     * @return Key 비밀키 클래스
     * @exception Exception
     */
    public static Key getKey1(String keyValue) throws Exception {
    	//System.out.println("DES");
        DESKeySpec desKeySpec = new DESKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }
    
    /**
     * 지정된 비밀키를 가지고 오는 메서드 (TripleDES)
     * require Key Size : 24 bytes
     * @return
     * @throws Exception
     */
    public static Key getKey2(String keyValue) throws Exception {
    	//System.out.println("Triple DES");
        DESedeKeySpec desKeySpec = new DESedeKeySpec(keyValue.getBytes());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DESede");
        Key key = keyFactory.generateSecret(desKeySpec);
        return key;
    }

    /**
     * 문자열 대칭 암호화
     *
     * @param ID
     *            비밀키 암호화를 희망하는 문자열
     * @return String 암호화된 ID
     * @exception Exception
     */
    public static String encrypt(String ID) {
        if (ID == null || ID.length() == 0)
            return "";
        
        String enc = "";
        
        try {
        	//그냥 Des인지 Triple Des 인지 에 따라 분기   키값이 24비트인경우 트리플 des로 앤크립트
            String instance = (key().length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";
            //System.out.println(instance);
            
            //Cipher  JAVA 암호화 객체  =  AES, DESC, RSA 등 다양항 암호화 방식 지원
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
            
            //init 로 암호화 모드로 설정,  암호화할 키를 인자로 전달
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, getKey());
            String amalgam = ID;

            //평문을 바이트화
            byte[] inputBytes1 = amalgam.getBytes("UTF8");
            
            // doFinal 메소드는 바이트한 평문을 암호화
            byte[] outputBytes1 = cipher.doFinal(inputBytes1);
            
            //바이트를 Text화 = Base64 인코딩
            /*BASE64Encoder encoder = new BASE64Encoder();
            String outputStr1 = encoder.encode(outputBytes1);*/
            
            enc = new String(Base64.getMimeEncoder().encode(outputBytes1), StandardCharsets.UTF_8);
        } catch (Exception e) {        	
        }
        
        return enc;
    }
    
    public static String encrypt(String ID, boolean isUrlEncode) {
    	String enc = "";
    	
    	try {
    		enc = encrypt(ID);
    		
    		// 76바이트에 생기는 개행문자 제거
    		enc = enc.replace(System.getProperty("line.separator").toString(), "");
    		enc = enc.replaceAll("\\R", "");
		} catch (Exception e) {
		}
    	
    	return isUrlEncode ? CommonUtils.urlEncode(enc) : enc;
    }

    /**
     * 문자열 대칭 복호화
     *
     * @param codedID
     *            비밀키 복호화를 희망하는 문자열
     * @return String 복호화된 ID
     * @exception Exception
     */
    public static String decrypt(String codedID) throws Exception {
        if (codedID == null || codedID.length() == 0)
            return "";
        
        String instance = (key().length() == 24) ? "DESede/ECB/PKCS5Padding" : "DES/ECB/PKCS5Padding";
        javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(instance);
        
        //복호화 모드로 Cipher 선언  암호화 했던 키 값 넣어줌
        cipher.init(javax.crypto.Cipher.DECRYPT_MODE, getKey());
        
        /*BASE64Decoder decoder = new BASE64Decoder();
        //base64로 디코더 
        byte[] inputBytes1 = decoder.decodeBuffer(codedID);*/
        
        byte[] inputBytes1 = Base64.getDecoder().decode(codedID);
        
        // 복호화 진행해서 바이트화
        byte[] outputBytes2 = cipher.doFinal(inputBytes1);

        //바이트를 UTF-8로 평문으로 변경
        String strResult = new String(outputBytes2, "UTF8");
        return strResult;
    }
    
    public static String decrypt(String codedID, boolean isUrlDecode) {
    	String dec = "";
    	
    	try {
    		dec = decrypt(codedID);
		} catch (Exception e) {
		}
    	
    	return isUrlDecode ? CommonUtils.urlEncode(dec) : dec;
    }
	
}