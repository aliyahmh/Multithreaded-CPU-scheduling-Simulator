//package CSC227project;

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

    private static final int TOTAL_MEMORY = 2048; // MB
    private int availableMemory;

    public MemoryManager() {
        this.availableMemory = TOTAL_MEMORY;
        System.out.println("[MemoryManager] Initialized with " 
            + TOTAL_MEMORY + " MB total memory.");
    }

    /**
     * Tries to allocate memory for a process.
     * Returns true if successful, false if not enough memory available.
     */
    public synchronized boolean allocate(int memoryRequired) {
        if (memoryRequired <= availableMemory) {
            availableMemory -= memoryRequired;
            System.out.println("[MemoryManager] Allocated " + memoryRequired
                + " MB. Remaining: " + availableMemory + " MB");
            return true;
        }
        System.out.println("[MemoryManager] Cannot allocate " + memoryRequired
            + " MB. Only " + availableMemory + " MB available.");
        return false;
    }

    /**
     * Releases memory back to the system when a process finishes.
     */
    public synchronized void release(int memoryRequired) {
        availableMemory += memoryRequired;
        System.out.println("[MemoryManager] Released " + memoryRequired
            + " MB. Available: " + availableMemory + " MB");
    }

    public synchronized int getAvailableMemory() {
        return availableMemory;
    }
}
