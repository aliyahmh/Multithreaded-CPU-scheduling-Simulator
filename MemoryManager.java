package cpu.scheduler;



	/**
	 * Manages the main memory of the system.
	 * Total memory = 2048 MB as specified in the project.
	 * 
	 * Shared between:
	 *   - JobLoaderThread (allocates memory when admitting a job)
	 *   - Main Thread / Scheduler (releases memory when a process finishes)
	 *
	 * All methods are synchronized to prevent race conditions
	 * as taught in Chapter 4 (shared data must be explicitly arranged).
	 */
	public class MemoryManager {

	    
	    private int availableMemory;

	    public MemoryManager() {
	        this.availableMemory = Main.MEMORY_LIMIT;
	    }

	    /**
	     * Tries to allocate memory for a process.
	     * Returns true if successful, false if not enough memory available.
	     */
	    public synchronized boolean allocate(int memoryRequired) {
	        if (memoryRequired <= availableMemory) {
	            availableMemory -= memoryRequired;
	            
	            return true;
	        }
	       
	        return false;
	    }

	    /**
	     * Releases memory back to the system when a process finishes.
	     */
	    public synchronized void release(int memoryRequired) {
	        availableMemory += memoryRequired;
	       
	    }

	    public synchronized int getAvailableMemory() {
	        return availableMemory;
	    }
	}

