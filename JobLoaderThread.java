




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
	      

	        int admittedCount = 0;

	        while (admittedCount < totalProcesses) {

	            try {
	                // Wait up to 100ms for a job to appear in the queue
	                Process process = jobQueue.poll(100, TimeUnit.MILLISECONDS);

	                if (process == null) {
	                    // No job available yet — Thread 1 may still be reading
	                    continue;
	                }

	                // Keep trying until memory is available for this process
	                boolean admitted = false;
	                while (!admitted) {

	                    admitted = memoryManager.allocate(process.getMemoryRequired());

	                    if (!admitted) {
	                        System.out.println("[Thread 2 - JobAdmitter] Waiting for memory... "
	                            + "Need " + process.getMemoryRequired()
	                            + " MB for Process " + process.getProcessId());

	                        // Wait 10ms before retrying (another process may finish)
	                        Thread.sleep(10);
	                    }
	                }

	                // Memory allocated — move process to Ready Queue
	                process.setState("ready");
	                readyQueue.put(process);
	                admittedCount++;

	             

	            } catch (InterruptedException e) {
	                // Deferred cancellation 
	                Thread.currentThread().interrupt();
	                System.err.println("[Thread 2 - JobAdmitter] Interrupted.");
	                break;
	            }
	        }

	        // Wait until all admitted processes finish before this thread terminates
	       

	        while (completedProcesses < totalProcesses) {
	            try {
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
	    	if (completedProcesses < totalProcesses) {
	    	    memoryManager.release(process.getMemoryRequired());
	    	    completedProcesses++;
	    	}
	       
	    }

	    // Getter so Main can pass this thread to schedulers
	    public int getTotalProcesses() {
	        return totalProcesses;
	    }
	

}
