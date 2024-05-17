package br.com.danielschiavo.shop;

import java.util.Base64;

public class Base64Utils {

	public static byte[] codificarParaBase64(byte[] bytes) {
		return Base64.getEncoder().encode(bytes);
	}
	
	public static byte[] decodificarDeBase64(byte[] bytesBase64) {
		return Base64.getDecoder().decode(bytesBase64);
	}
}
