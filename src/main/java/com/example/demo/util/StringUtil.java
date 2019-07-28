package com.example.demo.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;



public class StringUtil {


	public static boolean isEmpty(String str){
		if(str==null||"".equals(str.trim())){
			return true;
		}else{
			return false;
		}
	}
	

	public static boolean isNotEmpty(String str){
		if((str!=null)&&!"".equals(str.trim())){
			return true;
		}else{
			return false;
		}
	}

}
