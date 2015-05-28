package com.github.nedp.comp90015.proj2.job.worker;

public class Worker {

	public Worker() {
		// TODO Auto-generated constructor stub
	}
	static final int defaultPort = 80;

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int portnum = defaultPort;
		
		for(int i = 0; i +1 < args.length; i++ ){
			if(args.equals("-p")){
				portnum = Integer.parseInt(args[i+1]);
				
			}
		}
		
		
		
		
		

	}

}
