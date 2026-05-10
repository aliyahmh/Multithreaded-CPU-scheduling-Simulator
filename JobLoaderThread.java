//package CSC227project;

package cpu.scheduler;

	import java.util.ArrayList;
	import java.util.List;
	import java.util.concurrent.BlockingQueue;
	import java.util.concurrent.TimeUnit;

	/**
	 * Thread 2 - Job Loader
	 * Moves processes from the Job Queue to the Ready Queue.
	 * Checks available memory before admitting each process.
	 * Also releases memory when a process finishes execution.
	 *
	 
	 * Shares jobQueue and readyQueue with other threads — synchronized
	 * access handled by BlockingQueue 
	 *
	 * Terminates after all processes have been admitted AND completed.
	 */
	public class JobLoaderThread extends Thread {

	    private final BlockingQueue<Process> jobQueue;
	    private final BlockingQueue<Process> readyQueue;
	    private final MemoryManager memoryManager;
	    private final int totalProcesses;

	    // Tracks how many processes have fully finished (updated by scheduler)
	    private volatile int completedProcesses = 0;

	    /**
	     * Constructor
	     * @param jobQueue        shared queue from Thread 1
	     * @param readyQueue      shared queue to the scheduler (Main Thread)
	     * @param memoryManager   shared memory tracker
	     * @param totalProcesses  total number of processes in the system
	     */
	    public JobLoaderThread(BlockingQueue<Process> jobQueue,
	                             BlockingQueue<Process> readyQueue,
	                             MemoryManager memoryManager,
	                             int totalProcesses) {
	        this.jobQueue       = jobQueue;
	        this.readyQueue     = readyQueue;
	        this.memoryManager  = memoryManager;
	        this.totalProcesses = totalProcesses;
	    }

	    /**
	     * Thread 2 execution logic.
	     * Continuously pulls from the Job Queue and admits processes
	     * to the Ready Queue if enough memory is available.
	     */
	    @Override
	    public void run() {

	        // Use a waiting list for processes that don't fit in memory yet
	        List<Process> waitingList = new ArrayList<>();
	        int admittedCount = 0;

	        while (admittedCount < totalProcesses) {
	            try {
	                // Pull from job queue if available
	                Process process = jobQueue.poll(100, TimeUnit.MILLISECONDS);

	                if (process != null) {
	                    waitingList.add(process);
	                }

	                // Try to admit everything in the waiting list
	                List<Process> toRemove = new ArrayList<>();

	                for (Process p : waitingList) {
	                    if (memoryManager.allocate(p.getMemoryRequired())) {
	                        p.setState("ready");
	                        readyQueue.put(p);
	                        toRemove.add(p);
	                        admittedCount++;
	                        System.out.println("[Thread 2 - JobLoader] Process "
	                            + p.getProcessId()
	                            + " admitted to Ready Queue. ("
	                            + admittedCount + "/" + totalProcesses + ")");
	                    } else {
	                        System.out.println("[Thread 2 - JobLoader] Process "
	                            + p.getProcessId()
	                            + " waiting for memory ("
	                            + p.getMemoryRequired() + " MB needed, "
	                            + memoryManager.getAvailableMemory() + " MB available)");
	                    }
	                }

	                waitingList.removeAll(toRemove);

	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                System.err.println("[Thread 2 - JobLoader] Interrupted.");
	                break;
	            }
	        }

	        // Wait until all processes finish execution
	        while (completedProcesses < totalProcesses) {
	            try {
	                // Retry waiting list — memory may have freed up
	                List<Process> toRemove = new ArrayList<>();
	                for (Process p : waitingList) {
	                    if (memoryManager.allocate(p.getMemoryRequired())) {
	                        p.setState("ready");
	                        readyQueue.put(p);
	                        toRemove.add(p);
	                        System.out.println("[Thread 2 - JobLoader] Process "
	                            + p.getProcessId() + " finally admitted.");
	                    }
	                }
	                waitingList.removeAll(toRemove);
	                Thread.sleep(10);
	            } catch (InterruptedException e) {
	                Thread.currentThread().interrupt();
	                break;
	            }
	        }
	    }

	    /**
	     * Called by the Main Thread (scheduler) when a process finishes.
	     * Releases its memory and increments the completion counter.
	     *
	     */
	    public synchronized void notifyProcessCompleted(Process process) {
	    
	    	    memoryManager.release(process.getMemoryRequired());
	    	    completedProcesses++;
	    
	       
	    }

	    // Getter so Main can pass this thread to schedulers
	    public int getTotalProcesses() {
	        return totalProcesses;
	    }
	

}
