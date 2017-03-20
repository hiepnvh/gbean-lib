package com.bean.util;

import java.util.ArrayList;
import java.util.List;

public class ListUtils {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		List<String> list = new ArrayList<String>();
		list.add("Orange");
		list.add("Apple");
		list.add("Kiwi");
		
		System.out.println(join(list));
	}
	
	public static String join(List<String> list){
		String result = "";
		for (int i = 0; i < list.size(); i++) {
			if(i==0)
				result += list.get(i);
			else
				result +=", " + list.get(i);
		}
		
		return result;
	}

}
