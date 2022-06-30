package com.enliple.outviserbatch.common.util;

import java.security.MessageDigest;
import java.security.SecureRandom;

import com.google.common.io.BaseEncoding;
import com.oreilly.servlet.Base64Decoder;
import com.oreilly.servlet.Base64Encoder;

@SuppressWarnings({ "resource", "static-access" })
public class CryptogramUtils {

	/**
	 * md5 encode
	 * @param key
	 * @return
	 * @throws Exception
	 */
	public static String md5Encode(String key) throws Exception{
		
		MessageDigest md = MessageDigest.getInstance("MD5");
	
		md.update(key.getBytes());
		
		byte byteData[] = md.digest();
		StringBuffer sb = new StringBuffer(); 
		for(int i = 0 ; i < byteData.length ; i++){	
			sb.append(Integer.toString((byteData[i]&0xff) + 0x100, 16).substring(1));	
		}
		
		return sb.toString();
	}
	
	/**
	 * base64 encode
	 * @param key
	 * @return
	 */
	public static String base64Encode(String key){
		
		Base64Encoder base64 = new Base64Encoder(null);
		
		return base64.encode(key);
	}
	
	/**
	 * base64 decode
	 * @param key
	 * @return
	 */
	public static String base64Decode(String key) {
		Base64Decoder base64 = new Base64Decoder(null);
		return base64.decode(key);
	}
	
	/**
	 * base64 rendom key gen
	 * @param size
	 * @return
	 */
	public static String randomKey(int size) {
		SecureRandom random = new SecureRandom();
		byte bytes[] = new byte[size];
		random.nextBytes(bytes);
		
		return BaseEncoding.base64Url().omitPadding().encode(bytes);
	}
}