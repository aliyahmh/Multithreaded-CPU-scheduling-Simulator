//package CSC227project;




	import java.io.BufferedReader;
	import java.io.FileNotFoundException;
	import java.io.FileReader;
	import java.io.IOException;
	import java.util.concurrent.BlockingQueue;

	/**
	 * Thread 1 - Job Reader
	 * 
	 * Reads process information from job.txt, creates Process objects,
	 * and adds them to the Job Queue.
	 * 
	 * 
	 * 
	 * Terminates automatically after all jobs are read from the file.
	 */
	public class JobReaderThread extends Thread {

	    private final String filePath;
	    private final BlockingQueue<Process> jobQueue;

	    
	    public JobReaderThread(String filePath, BlockingQueue<Process> jobQueue) {
	        this.filePath = filePath;
	        this.jobQueue = jobQueue;
	    }

	    /**
	     * Thread 1 execution logic.
	     * Reads job.txt line by line, parses each line into a Process,
	     * and puts it into the job queue.
	     */
	    @Override
	    public void run() {
	        System.out.println("[Thread 1 - JobReader] Started.");

	        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {

	            String line;
	            int arrivalOrder = 0; // used for tie-breaking in scheduling

	            while ((line = reader.readLine()) != null) {
	                line = line.trim();

	                // Skip empty lines
	                if (line.isEmpty()) continue;

	                Process process = parseLine(line, arrivalOrder);

	                if (process != null) {
	                    jobQueue.put(process); // blocks if queue full (thread-safe)
	                    System.out.println("[Thread 1 - JobReader] Added to Job Queue: "
	                        + "PID=" + process.getProcessId()
	                        + " | Burst=" + process.getBurstTime()
	                        + " | Priority=" + process.getPriority()
	                        + " | Memory=" + process.getMemoryRequired() + "MB");
	                    arrivalOrder++;
	                }
	            }

	        } catch (FileNotFoundException e) {
	            System.err.println("[Thread 1 - JobReader] ERROR: File not found -> " + filePath);
	        } catch (IOException e) {
	            System.err.println("[Thread 1 - JobReader] ERROR reading file: " + e.getMessage());
	        } catch (InterruptedException e) {
	            // Deferred cancellation - as taught in Chapter 4 slide 48
	            Thread.currentThread().interrupt();
	            System.err.println("[Thread 1 - JobReader] Interrupted while adding to queue.");
	        }

	        System.out.println("[Thread 1 - JobReader] Finished. All jobs added to Job Queue.");
	    }

	    
	    private Process parseLine(String line, int arrivalOrder) {
	        try {
	            // Split on ";" to separate memory from the rest
	            String[] mainParts = line.split(";");
	            if (mainParts.length != 2) {
	                System.err.println("[Thread 1 - JobReader] Bad format (missing ';'): " + line);
	                return null;
	            }

	            int memoryRequired = Integer.parseInt(mainParts[1].trim());

	            // Split the left side on ":" to get ID, burst, priority
	            String[] coreParts = mainParts[0].split(":");
	            if (coreParts.length != 3) {
	                System.err.println("[Thread 1 - JobReader] Bad format (missing ':'): " + line);
	                return null;
	            }

	            int processID = Integer.parseInt(coreParts[0].trim());
	            int burstTime = Integer.parseInt(coreParts[1].trim());
	            int priority  = Integer.parseInt(coreParts[2].trim());

	            // Validate priority range: must be 1-30 as per project spec
	            if (priority < 1 || priority > 30) {
	                System.err.println("[Thread 1 - JobReader] Priority out of range (1-30): " + line);
	                return null;
	            }

	            // Validate memory: single process cannot exceed total system memory
	            if (memoryRequired > 2048) {
	                System.err.println("[Thread 1 - JobReader] Process " + processID
	                    + " requires more than total memory (2048 MB). Skipping.");
	                return null;
	            }

	            return new Process(processID, burstTime, priority, memoryRequired, arrivalOrder);

	        } catch (NumberFormatException e) {
	            System.err.println("[Thread 1 - JobReader] Parse error on line: " + line);
	            return null;
	        }
	    }
	}


