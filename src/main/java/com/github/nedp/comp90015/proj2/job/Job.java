package com.github.nedp.comp90015.proj2.job;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * Runs a JAR in a separate JVM process.
 * <p/>
 * When {@link #run} is called, a new JVM process will be started,
 * running the specified JAR, passing in the specified input and
 * output files, and using the specified log file for stdout and
 * stderr.
 * The Job can be queried at any time for its current {@link Status}.
 *
 * @author nedp
 */
public class Job implements Runnable {

    public static final int NO_LIMIT = -1;
    public static final int NO_TIMEOUT = -1;

    private static final String JAVA = "java";
    private static final String JAR_FLAG = "-jar";

    @NotNull
    public final Files files;

    private final int memoryLimit; // in MB
    private final int timeout; // in seconds

    private final StatusTracker tracker;


    /**
     * Creates a new Job with no memory limit or timeout.
     * <p/>
     * The Job may be executed as
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param files  not null
     * @param tracker  not null, should be ready to {@link StatusTracker#start}
     */
    public Job(@NotNull Files files, @NotNull StatusTracker tracker) {
        this.files = files;
        this.tracker = tracker;
        this.memoryLimit = NO_LIMIT;
        this.timeout = NO_TIMEOUT;
    }

    /**
     * Creates a new Job which may executed as:
     * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
     *
     * @param files  not null
     * @param tracker  not null, should be ready to {@link StatusTracker#start}
     * @param memoryLimit  maximum number of megabytes of RAM to allocate to the job,
     *                     no limit if non-positive
     * @param timeout  maximum number of seconds to wait for the job to finish,
     *                 no limit if non-positive.
     */
    public Job(@NotNull Files files, @NotNull StatusTracker tracker, int memoryLimit, int timeout) {
        if (memoryLimit < 1) {
            memoryLimit = NO_LIMIT;
        }
        this.memoryLimit = memoryLimit;

        if (timeout < 1) {
            timeout = NO_TIMEOUT;
        }
        this.timeout = timeout;

        this.files = files;
        this.tracker = tracker;
    }

    @Override
    /**
     * Runs the Job, as specified, in a new process.
     * <p/>
     * This method may only be called once.
     * It returns after the job's process terminates,
     * at which point the output may be taken from the
     * job's output and log files.
     */
    final public void run() {
        // Enforce run-once semantics by checking and advancing state.
        this.tracker.start();

        // Run the JVM with the jar and input file.
        // Pipe stderr and stdout to a log file.
        final boolean ok = this.runJVM();

        // Advance the state based on whether everything is ok.
        this.tracker.finish(ok);
    }

    /**
     * Runs the job process.
     * @return whether the job completed successfully or not.
     */
    private boolean runJVM() {
        // Begin with $ java ...
        final ProcessBuilder pb = new ProcessBuilder(JAVA);

        // Use the memory limit as a heap size limit (in MB),
        // if one was provided.
        // ... -Xmx<memLimit>M ...
        if (this.memoryLimit != NO_LIMIT) {
            pb.command().add(String.format("-Xmx%dM", this.memoryLimit));
        }
        // ... -jar job_name.jar ...
        Collections.addAll(pb.command(), JAR_FLAG, this.files.jar.toString());

        // ... job_name.in job_name.out ...
        Collections.addAll(pb.command(), this.files.in.toString(), this.files.out.toString());

        // ... &> job_name.log
        pb.redirectErrorStream(true);
        pb.redirectOutput(this.files.log);

        // Run the process; zero exit codes indicate success.
        // If we have a timeout value, use it; running out of
        // time counts as a failure.
        try {
            final Process p = pb.start();
            if (this.timeout == NO_TIMEOUT) {
                return p.waitFor() == 0;
            } else {
                final boolean didFinish = p.waitFor(this.timeout, TimeUnit.SECONDS);
                p.destroyForcibly().waitFor();
                return didFinish && (p.exitValue() == 0);
            }
        } catch (IOException | InterruptedException e) {
            // TODO log this nicely
            System.err.printf("Job#run:  %s caught:  %s\n", e.getClass(), e.getMessage());

            return false; // If there was an exception, the Job failed.
        }
    }

    /**
     * Queries the injected {@link StatusTracker} for the current status.
     *
     * @return the status
     */
    @NotNull
    public Status currentStatus() {
        return this.tracker.current();
    }

    /**
     * Retrieves the name of this Job, which is determined by its jar file.
     * <p/>
     * A Job's name is a short, but possibly ambiguous identifier.
     * It should not be used to uniquely identify the Job.
     *
     * @return a string containing the Job's name.
     */
    @NotNull
    public String name() {
        return this.files.jar.getName();
    }

    /**
     * A record to store the files associated with a {@link Job}.
     */
    @SuppressWarnings("unused")
    public static final class Files {
        @NotNull public final File jar;
        @NotNull public final File in;
        @NotNull public final File out;
        @NotNull public final File log;

        /**
         * Creates a record by appending suffixes to the base name.
         * <p/>
         * eg: {@code new Files("name")} returns
         * {@code Files{"name.jar", "name.in", "name.out", "name.log"}}
         *
         * @param basename  not null, not empty
         *                  "$basename$.jar" and "$basename$.in should exist"
         */
        public Files(@NotNull String basename) {
            this.jar = new File(String.format("%s.jar", basename));
            this.in = new File(String.format("%s.in", basename));
            this.out = new File(String.format("%s.out", basename));
            this.log = new File(String.format("%s.log", basename));
        }

        /**
         * Creates a record with the specified files.
         */
        public Files(@NotNull File jar, @NotNull File in, @NotNull File out, @NotNull File log) {
            this.jar = jar;
            this.in = in;
            this.out = out;
            this.log = log;
        }
    }

    public static Job fromJSON(BufferedReader input){
		JSONParser parser = new JSONParser();
		JSONObject obj = new JSONObject();
		
			try {
				obj = (JSONObject) parser.parse(input);
				
			} catch (IOException | ParseException e) {
				System.out.printf("couldn't parse the JSON string: %s\n", e.getMessage());
				
				return null;
			}
			
			int memoryLimit = Integer.parseInt((String) obj.get("MemoryLimit"));
			int timeout = Integer.parseInt((String) obj.get("Timeout"));
			
			String basename = (String) obj.get("Name");
			basename = basename.substring(0, basename.lastIndexOf((int)'.'));
			
			File dir = new File(basename);
			if(dir.exists()){
				int i = 0;
				while(dir.exists()){
					i++;
					dir = new File(basename + i);
					
				}
				basename = basename + i;
			}
			
			if(!dir.mkdir()){
				System.out.printf("couldn't create the new directory: %s\n", dir.getName());
				return null;
			}
			
			//write all the files to Disk
			Job.Files jobFiles = new Job.Files(dir.getName()+ File.pathSeparator + basename);
			BufferedWriter output;
			try{
				
				jobFiles.jar.createNewFile();
				output = new BufferedWriter(new OutputStreamWriter( 
						new FileOutputStream(jobFiles.jar), 
						StandardCharsets.UTF_8 ));
				
				output.write((String) obj.get("JarFile"));
				output.close();
			}catch(IOException e){
				System.out.printf("couldn't write to new jar file: %s\n",e.getMessage());
				return null;
			}try{
				
				jobFiles.in.createNewFile();
				output = new BufferedWriter(new OutputStreamWriter( 
						new FileOutputStream(jobFiles.in), 
						StandardCharsets.UTF_8 ));
				output.write((String) obj.get("InFile"));
				output.close();
			}catch(IOException e){
				System.out.printf("couldn't write to new input file: %s\n",e.getMessage());
				return null;
			}try{
				
				jobFiles.out.createNewFile();
				output = new BufferedWriter(new OutputStreamWriter( 
						new FileOutputStream(jobFiles.out), 
						StandardCharsets.UTF_8 ));
				output.write((String) obj.get("OutFile"));
				output.close();
				
			}catch(IOException e){
				System.out.printf("couldn't write to new output file: %s\n",e.getMessage());
				return null;
			}try{
				
				jobFiles.log.createNewFile();
				output = new BufferedWriter(new OutputStreamWriter( 
						new FileOutputStream(jobFiles.log), 
						StandardCharsets.UTF_8 ));
				output.write((String) obj.get("LogFile"));
				output.close();
			
			}catch(IOException e){
				System.out.printf("couldn't write to new log file: %s\n",e.getMessage());
				return null;
			}
			
			
		return new Job(jobFiles, new StatusTracker(), memoryLimit, timeout );
	}
    /**
     * 
     * @param job
     * @return null if error but otherwise a JSONstring that can be sent over the network
     */
    @SuppressWarnings("unchecked")
	public  String toJSON(Job job){
		JSONObject obj = new JSONObject();
		obj.put("Type", "Job");
		obj.put("Name", this.name());
		obj.put("MemoryLimit", this.memoryLimit);
		obj.put("Timeout", this.timeout);

		//write the jar file
		String text = "";
		try {
			text = new String(java.nio.file.Files.readAllBytes( files.jar.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("couldn't write jar file to JSON");
			e.printStackTrace();
			return null;
		}
		obj.put("JarFile", text);
		
		
		// the in file
		text = "";
		try {
			text = new String(java.nio.file.Files.readAllBytes( files.in.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("couldn't write input file to JSON");
			e.printStackTrace();
			return null;
		}
		obj.put("InFile", text);
		
		//the out file
		text = "";
		try {
			text = new String(java.nio.file.Files.readAllBytes( files.out.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("couldn't write output file to JSON");
			e.printStackTrace();
			return null;
		}
		obj.put("OutFile", text);
		
		// the log file
		
		text = "";
		try {
			text = new String(java.nio.file.Files.readAllBytes( files.log.toPath()), StandardCharsets.UTF_8);
		} catch (IOException e) {
			System.out.println("couldn't write log file to JSON");
			e.printStackTrace();
			return null;
		}
		obj.put("LogFile", text);
		
		
		
		return obj.toJSONString();
	}
}
