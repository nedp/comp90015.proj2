package com.github.nedp.comp90015.proj2.job.worker.master;

import com.github.nedp.comp90015.proj2.job.Job;
import org.jetbrains.annotations.NotNull;

import javax.net.SocketFactory;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.net.Socket;

/**
 * TODO
 * Created by nedp on 21/05/15.
 */
public class RemoteWorker implements Worker {

  private static final int TEN_SECONDS = 10000;

  private final String hostname;
  private final int port;
  private final int jobPort;
  private final BufferedReader input;
  private long freeMemory;
  private Status status;

  // Set the properties of the certificate at startup.
  // TODO swap to JVM args from hard coding for the SSL key store.
  static {
    final String keyDir = "/src/main/resources/assignment2KeyStr";
    System.setProperty("javax.net.ssl.trustStore", System.getProperty("user.dir") + keyDir);
    System.setProperty("javax.net.ssl.trustStorePassword", "comp90015");
    System.setProperty("javax.net.ssl.keyStore", System.getProperty("user.dir") + keyDir);
    System.setProperty("javax.net.ssl.keyStorePassword", "comp90015");
  }

  public RemoteWorker(@NotNull String hostname, int port) throws IOException {
    this.hostname = hostname;
    this.port = port;
    final SocketFactory factory = SSLSocketFactory.getDefault();
    final Socket workerSocket = factory.createSocket(hostname, port);
    workerSocket.setSoTimeout(TEN_SECONDS);

    // Get the port for the job from the Worker
    this.input = new BufferedReader(new InputStreamReader(workerSocket.getInputStream()));
    jobPort = Integer.parseInt(this.input.readLine().split(":")[1]);
  }

  /**
   * Maintains the connection to the remote host.
   */
  public synchronized void maintain() {
    this.status = Status.RUNNING;
    try {
      this.input.lines().forEachOrdered(
          line -> RemoteWorker.this.freeMemory = Long.parseLong(line.split(":")[1]));
    } catch (UncheckedIOException ignored) {} // Handled by setting status to DOWN.
    this.status = Status.DOWN;
  }

  @NotNull
  @Override
  public Result execute(Job job) {
    if (this.status == Status.DOWN) {
      return Result.DISCONNECTED;
    }

    final SocketFactory factory = SSLSocketFactory.getDefault();
    try {
      final Socket jobSocket = factory.createSocket(hostname, jobPort);
      jobSocket.setSoTimeout(TEN_SECONDS);

      try {
        PrintWriter jobOut = new PrintWriter(jobSocket.getOutputStream());
        String JSONJobString = job.toJSON();
        if (null == JSONJobString) {
          System.out.println("Error Turning Job into JSON");
          return Result.FAILED;
        }
        jobOut.println(JSONJobString);
        jobOut.flush();

        BufferedReader jobIn = new BufferedReader(new InputStreamReader(jobSocket.getInputStream()));

        // Until the result gets sent back to us, check the socket
        // and this RemoteWorker's status each 10 seconds.
        // If the RemoteWorker is ever DOWN, report the Job as DISCONNECTED.
        String resultType;
        while (true) {
          try {
            resultType = jobIn.readLine();
            break;
          } catch (IOException e) {
            if (this.status == Status.DOWN) {
              return Result.DISCONNECTED;
            }
          }
        }

        if (resultType.equals(Job.PARSE_ERROR)) {
          System.out.println("There was an Error in parsing the job via JSON");
          return Result.FAILED;

        } else if (resultType.equals("FAILED")) {
          if (!job.files.log.exists()) {
            job.files.log.createNewFile();
          }

          //read all the lines to the logfile
          PrintWriter pw = new PrintWriter(job.files.log);
          String line;
          while (null != (line = jobIn.readLine())) {
            pw.println(line);
          }
          pw.close();
          return Result.FAILED;

        } else if (resultType.equals("FINISHED")) {
          if (!job.files.out.exists()) {
            job.files.out.createNewFile();
          }

          //read all the lines to the output file
          PrintWriter pw = new PrintWriter(job.files.out);
          String line;
          while (null != (line = jobIn.readLine())) {
            pw.println(line);
          }
          pw.close();
          return Result.FINISHED;
        }
      } finally {
        jobSocket.close();
      }
    } catch (IOException e) {
      System.out.printf("IO exception running Job remotely: %s\n", e.getMessage());
      return Result.DISCONNECTED;
    }

    return Result.DISCONNECTED; // TODO
  }

  @NotNull
  @Override
  public Status status() {
    return this.status; // TODO
  }

  @Override
  public long freeMemory() {
    return freeMemory;
  }

  @NotNull
  @Override
  public String identifier() {
    return String.format("RemoteWorker at %s:%d", this.hostname, this.port);
  }

}
