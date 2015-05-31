package com.github.nedp.comp90015.proj2.job;

public class JobFactory {

	
	public static Job fromJSON(BufferedReader input){
		
		
		return new Job(null, new StatusTracker());
	}
	
	public static String toJSON(Job job){
		
		
		return "";
}
