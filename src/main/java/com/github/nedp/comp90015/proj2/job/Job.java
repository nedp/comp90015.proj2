package com.github.nedp.comp90015.proj2.job;

import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.File;
import java.io.IOException;
import java.util.Base64;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.nio.file.Files.readAllBytes;

/**
 * Runs a JAR in a separate JVM process.
 * <p>
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

  public static final String PARSE_ERROR = "Bad Job";

  private static final String JAVA = "java";
  private static final String JAR_FLAG = "-jar";

  @NotNull
  public final Files files;

  private final int memoryLimit; // in MB
  private final int timeout; // in seconds

  private final StatusTracker tracker;

  /**
   * Creates a new Job with no memory limit or timeout.
   * <p>
   * The Job may be executed as
   * {@code java -jar jarFile inFile outFile >logFile 2>&1}.
   *
   * @param files not null
   * @param tracker not null, should be ready to {@link StatusTracker#start}
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
   * @param files not null
   * @param tracker not null, should be ready to {@link StatusTracker#start}
   * @param memoryLimit maximum number of megabytes of RAM to allocate to the job,
   * no limit if non-positive
   * @param timeout maximum number of seconds to wait for the job to finish,
   * no limit if non-positive.
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
   *
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
   * <p>
   * A Job's name is a short, but possibly ambiguous identifier.
   * It should not be used to uniquely identify the Job.
   *
   * @return a string containing the Job's name.
   */
  @NotNull
  public String name() {
    return this.files.jar.getName();
  }

  public static Job fromJSON(String input) throws ParseException, IOException {
    final JSONParser parser = new JSONParser();
    final JSONObject obj = (JSONObject) parser.parse(input);

    final int memoryLimit = ((Long) obj.get("MemoryLimit")).intValue();
    final int timeout = ((Long) obj.get("Timeout")).intValue();

    final File jobDir;
    final String fullName = (String) obj.get("Name");
    final String baseName = fullName.substring(0, fullName.lastIndexOf('.'));
    {
      File dir = new File(baseName);
      // Resolve name conflicts by linear probing.
      if (dir.exists()) {
        int i = 0;
        while (dir.exists()) {
          i++;
          dir = new File(baseName + i);
        }
      }
      jobDir = dir;
    }

    if (!jobDir.mkdir()) {
      throw new IOException(String.format("Couldn't create directory: %s", jobDir.getName()));
    }
    jobDir.deleteOnExit();
    final Base64.Decoder decoder = Base64.getDecoder();

    // Write the jar and input files to disk.
    // Note: the job itself is responsible for creating the log and output files.
    final Job.Files jobFiles = new Job.Files(jobDir.getName() + File.separator + baseName);
    final boolean ok =
        newFile(jobFiles.jar, (String) obj.get("JarFile"), decoder)
        && newFile(jobFiles.in, (String) obj.get("InFile"), decoder);
    assert (ok);

    return new Job(jobFiles, new StatusTracker(), memoryLimit, timeout);
  }

  private static boolean newFile(File file, String content, Base64.Decoder decoder)
      throws IOException {
    final boolean ok = file.createNewFile();
    file.deleteOnExit();
    java.nio.file.Files.write(file.toPath(), decoder.decode(content));
    return ok;
  }

  /**
   * Encodes this Job as a JSON String to be sent over the network.
   *
   * @return the JSON String.
   */
  @SuppressWarnings("unchecked")
  public String toJSON() {
    final JSONObject obj = new JSONObject();
    obj.put("Type", "Job");
    obj.put("Name", this.name());
    obj.put("MemoryLimit", this.memoryLimit);
    obj.put("Timeout", this.timeout);

    Base64.Encoder b64encoder = Base64.getEncoder();

    // Encode the files.
    putFile(obj, files.jar, "JarFile", b64encoder);
    putFile(obj, files.in, "InFile", b64encoder);
    putFile(obj, files.out, "OutFile", b64encoder);
    putFile(obj, files.log, "LogFile", b64encoder);

    return obj.toJSONString();
  }

  @SuppressWarnings("unchecked")
  private static void putFile(JSONObject obj, File file, String fieldName,
                              Base64.Encoder b64encoder) {
    String text = "";
    try {
      text = b64encoder.encodeToString(readAllBytes(file.toPath()));
    } catch (Exception e) {
      System.out.printf("Job file not found during encoding: %s\n", e.getMessage());
    }
    obj.put(fieldName, text);
  }

  /**
   * A record to store the files associated with a {@link Job}.
   */
  public static final class Files {
    @NotNull
    public final File jar;
    @NotNull
    public final File in;
    @NotNull
    public final File out;
    @NotNull
    public final File log;

    /**
     * Creates a record by appending suffixes to the base name.
     * <p>
     * eg: {@code new Files("name")} returns
     * {@code Files{"name.jar", "name.in", "name.out", "name.log"}}
     *
     * @param basename not null, not empty,
     * "$basename$.jar" and "$basename$.in should exist"
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
}
