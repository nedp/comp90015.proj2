package com.github.nedp.comp90015.proj2.job.worker;

import com.github.nedp.comp90015.proj2.job.Job;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class JobHandlerThread implements Runnable {

  private Socket socket;

  public JobHandlerThread(Socket s, RemoteMaster m) {
    this.socket = s;
  }

  @Override
  public void run() {
    // Establish the socket streams.
    final PrintWriter socketOut;
    try {
      socketOut = new PrintWriter(socket.getOutputStream());
    } catch (IOException e) {
      System.out.printf("IOException creating PrintWriter running Job: %s\n", e.getMessage());
      this.close(null, null);
      return;
    }
    final BufferedReader socketIn;
    try {
      socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream()));
    } catch (IOException e) {
      System.out.printf("could not get streamReader on socket: %s\n", e.getMessage());
      this.close(null, socketOut);
      return;
    }

    // Close the sockets in `finally` block.
    try {
      // Parse and run the job.
      final String JSONStringJob;
      try {
        JSONStringJob = socketIn.readLine();
      } catch (IOException e) {
        System.out.printf("IO Error reading from job socket: %s\n", e.getMessage());
        return;
      }

      // Parse the job.
      final Job job;
      try {
        job = Job.fromJSON(JSONStringJob);
      } catch (ParseException | IOException e) {
        System.out.printf("%s parsing Job from JSON: %s", e.getClass().getName(), e.getMessage());
        return;
      }

      // Run it, handle the result, then clean it up.
      job.run();
      try {
        this.handleJobResult(job, socketOut);
      } catch (FileNotFoundException e) {
        System.out.printf("Required file not found when handling job result: %s", e.getMessage());
        return;
      } finally {
        if (!this.cleanup(job)) {
          System.out.printf("Error cleaning up Job: %s\n", job.name());
        }
      }
    } finally {
      this.close(socketIn, socketOut);
    }
  }

  private void handleJobResult(Job job, PrintWriter socketOut)
      throws FileNotFoundException {
    final BufferedReader jobOutput;
    switch (job.currentStatus()) {
      case FINISHED:
        socketOut.println("FINISHED");
        jobOutput = new BufferedReader(new FileReader(job.files.out));
        break;

      case FAILED:
        socketOut.println("FAILED");
        jobOutput = new BufferedReader(new FileReader(job.files.log));
        break;

      case WAITING: // fallthrough
      case RUNNING:
      default:
        throw new RuntimeException("Job#run returned before the Job terminated.");
    }

    // write the whole result back to the master
    try {
      String line;
      while (null != (line = jobOutput.readLine())) {
        socketOut.println(line);
      }
    } catch (IOException e) {
      System.out.printf("Couldn't read output back to the master: %s\n", e.getMessage());
    }
  }

  private void close(BufferedReader socketIn, PrintWriter socketOut) {
    socketOut.close();
    try {
      socketIn.close();
    } catch (IOException e) {
      System.out.printf("IOException closing socket: %s\n", e.getMessage());
    }
    try {
      socket.close();
    } catch (IOException e) {
      System.out.printf("IOException closing socket: %s\n", e.getMessage());
    }
  }

  private boolean cleanup(Job job) {
    boolean ok = true;
    final File parentDir = job.files.jar.getParentFile();
    ok &= job.files.jar.delete();
    ok &= job.files.in.delete();
    ok &= job.files.out.delete();
    ok &= job.files.log.delete();
    ok &= parentDir.delete();
    return ok;
  }
}
