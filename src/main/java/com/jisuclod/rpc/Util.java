package com.jisuclod.rpc;

import java.util.ArrayList;
import java.util.List;

public class Util {

	public static List<String> getPartList(String str){
		List<String> partlist = new ArrayList<String>();
		int end = 512;
		while (true) {
			partlist.add(str.substring(0, end));
			str = str.substring(end, str.length());
			if (str.length() > 512){
				continue;
			}else{
				partlist.add(str);
				break;
			}
		}
		return partlist;
	}
}
