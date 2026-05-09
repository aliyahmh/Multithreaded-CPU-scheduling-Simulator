

package cpu.scheduler;
	public class Process {
	    
	    // ─── Basic Info ───────────────────────────────
	    private int processId;          
	    private int burstTime;          // total CPU time needed (never changes)
	    private int remainingTime;      // remaining CPU time (decreases as process runs)
	    
	    // ─── Priority ─────────────────────────────────
	    private int priority;           // current priority (changes due to aging)
	    private int originalPriority;   // original priority from job.txt (never changes)
	    
	    // ─── Memory & Order ───────────────────────────
	    private int memoryRequired;     // memory needed in MB
	    private int arrivalOrder;       // order read from job.txt
	    
	    // ─── State ────────────────────────────────────
	    private String state;           // "NEW" → "READY" → "RUNNING" → "TERMINATED"
	    
	    // ─── Timing ───────────────────────────────────
	    private int startTime = -1;         // first time process gets CPU
	    private int terminationTime = -1;   // time process finishes
	    private int waitingTime = 0;        // total time waiting in ready queue
	    private int turnaroundTime = 0;     // terminationTime - arrivalTime
	    
	    // ─── Starvation & Aging ───────────────────────
	    private boolean starved = false;        // true if process suffered starvation
	    private int starvationStartTime = -1;   // time starvation was first detected
	    private int lastAgedTime = 0;           // last time aging was applied
	    private int lastReadyTime = 0;          // last time process entered ready queue
	    
	    // ─── Constructor ──────────────────────────────
	    public Process(int processId, int burstTime, int priority,
	                   int memoryRequired, int arrivalOrder) {
	        this.processId        = processId;
	        this.burstTime        = burstTime;
	        this.remainingTime    = burstTime;
	        this.priority         = priority;
	        this.originalPriority = priority;
	        this.memoryRequired   = memoryRequired;
	        this.arrivalOrder     = arrivalOrder;
	        this.state            = "NEW";
	    }
	    
	    // ─── Getters ──────────────────────────────────
	    public int getProcessId()           { return processId; }
	    public int getBurstTime()           { return burstTime; }
	    public int getRemainingTime()       { return remainingTime; }
	    public int getPriority()            { return priority; }
	    public int getOriginalPriority()    { return originalPriority; }
	    public int getMemoryRequired()      { return memoryRequired; }
	    public int getArrivalOrder()        { return arrivalOrder; }
	    public String getState()            { return state; }
	    public int getStartTime()           { return startTime; }
	    public int getTerminationTime()     { return terminationTime; }
	    public int getWaitingTime()         { return waitingTime; }
	    public int getTurnaroundTime()      { return turnaroundTime; }
	    public boolean isStarved()          { return starved; }
	    public int getStarvationStartTime() { return starvationStartTime; }
	    public int getLastAgedTime()        { return lastAgedTime; }
	    public int getLastReadyTime()       { return lastReadyTime; }
	    
	    // ─── Setters ──────────────────────────────────
	    public void setRemainingTime(int remainingTime)         { this.remainingTime = remainingTime; }
	    public void setPriority(int priority)                   { this.priority = priority; }
	    public void setState(String state)                      { this.state = state; }
	    public void setStartTime(int startTime)                 { this.startTime = startTime; }
	    public void setTerminationTime(int terminationTime)     { this.terminationTime = terminationTime; }
	    public void setWaitingTime(int waitingTime)             { this.waitingTime = waitingTime; }
	    public void setTurnaroundTime(int turnaroundTime)       { this.turnaroundTime = turnaroundTime; }
	    public void setStarved(boolean starved)                 { this.starved = starved; }
	    public void setStarvationStartTime(int time)            { this.starvationStartTime = time; }
	    public void setLastAgedTime(int lastAgedTime)           { this.lastAgedTime = lastAgedTime; }
	    public void setLastReadyTime(int lastReadyTime)         { this.lastReadyTime = lastReadyTime; }
	    
	  
	    }