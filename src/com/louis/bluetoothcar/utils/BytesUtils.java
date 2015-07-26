package com.louis.bluetoothcar.utils;

public class BytesUtils {

	/**
	 * byte����ת��Ϊ16�����ַ���
	 * @param bytes
	 * @return
	 */
	public static String byteToHexString(byte[] bytes){
		String result="";
		for(int i=0;i<bytes.length;i++){
			String hexString=Integer.toHexString(bytes[i]&0xFF);
			if(hexString.length()==1){
				hexString="0" + hexString;
			}
			result+=hexString;
		}
		return result;
		
	}
	/**
	 * 
	 * @param 16�����ַ���ת��Ϊbyte����
	 * @return
	 */
	public static byte[] hexStringToByte(String hex){
		String hexString=hex.replace(" ","");
		int len = hexString.length() / 2;
        char[] chars = hexString.toCharArray();
        String[] hexStr = new String[len];
        byte[] bytes = new byte[len];
        for (int i = 0, j = 0; j < len; i += 2, j++) {
            hexStr[j] = "" + chars[i] + chars[i + 1];
            bytes[j] = (byte) Integer.parseInt(hexStr[j], 16);
        }
        return bytes;
		
	}
	
}
