/**
 * 
 */
package com.github.nedp.comp90015.proj2.job.worker;

/**
 * A class to collect the current worker usage
 * 
 * @author Charles Talbot
 *
 */
public class WorkerStatus {
	
	private long memoryMax;
	private long memoryFree;
	private Runtime runtime;
	
	/**
	 * Gets the maximum memory of the Worker
	 * @return maximum memory
	 */
	public long getMaxMemory(){
		runtime = Runtime.getRuntime();
		memoryMax = runtime.maxMemory();
		return memoryMax;
	}
	
	/**
	 * Gets the current memory used by the Worker
	 * @return current memory
	 */
	public long getCurrentMemory(){
		runtime = Runtime.getRuntime();
		memoryMax = runtime.maxMemory();
		memoryFree = runtime.freeMemory();
		return memoryMax - memoryFree;
	}
	
	/**
	 * Gets the current free memory of the Worker
	 * @return free memory
	 */
	public long getFreeMemory(){
		runtime = Runtime.getRuntime();
		memoryFree = runtime.freeMemory();
		return memoryFree;
	}
	
}
