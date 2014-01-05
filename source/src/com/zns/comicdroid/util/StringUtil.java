package com.zns.comicdroid.util;

public final class StringUtil {
	public static boolean nullOrEmpty(String val)
	{
		return val == null || val.trim().equals("");
	}
}