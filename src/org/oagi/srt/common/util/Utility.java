package org.oagi.srt.common.util;

import java.util.UUID;

public class Utility {
	public static String generateGUID(){
		return "oagis-id-" + UUID.randomUUID().toString().replaceAll("-", "");
	}
	public static String filepath() {
		String filepath = "C:\\Users\\jnl18\\Documents\\OAG\\Jaehun\\";
		return filepath;
	}
}
