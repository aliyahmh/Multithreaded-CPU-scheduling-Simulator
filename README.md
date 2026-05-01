# Multithreaded-CPU-scheduling-Simulator
Multithreaded CPU Scheduling Simulator (SJF, Round Robin, Priority with Aging) — Java | CSC227 King Saud University


## About
A multithreaded Java simulation of CPU scheduling in a single-CPU system, 
built as part of the CSC 227 Operating Systems course at King Saud University.

The simulator supports three scheduling algorithms:
- **Shortest Job First (SJF)** — non-preemptive, tie-broken by arrival order
- **Round Robin (RR)** — with a time quantum of 5ms
- **Priority Scheduling (Non-Preemptive)** — with starvation detection and aging

Key features:
- Multithreaded design with separate threads for job reading, memory loading, and scheduling
- Memory management with a 2048 MB limit
- Starvation detection and aging (priority boosted every 4ms)
- Gantt chart output with burst tracking
- Performance metrics: average waiting time and average turnaround time


## Team Agreements & Shared Structure

*Process Class Fields:*

int processId;
String state; // "NEW", "READY", "RUNNING", "TERMINATED"

int burstTime;
int remainingTime;

int priority;          // changes due to aging
int originalPriority;  // never changes

int memoryRequired;
int arrivalOrder;      // order in job.txt

int startTime = -1;         // -1 = not started yet
int terminationTime = -1;   // -1 = not terminated yet
int waitingTime = 0;
int turnaroundTime = 0;

boolean starved = false;
int starvationStartTime = -1; // when it first became starved
int lastAgedTime = 0;         // last time aging was applied (for every 4ms rule)
int lastReadyTime = 0;        // when it last entered the ready queue
 ⁠

---

*Queues (defined in Main, passed by reference to everyone):*

// ArrayList for readyQueue — needed for re-sorting when aging changes priorities
public static List<Process> readyQueue = new ArrayList<>();
public static Queue<Process> jobQueue = new LinkedList<>();

// Synchronization
synchronized (jobQueue) { }
synchronized (readyQueue) { }
 ⁠
note: Everyone must reference the SAME object, not copies.

---

*Shared Constants (define in Main):*

static final int QUANTUM = 5;
static final int MEMORY_LIMIT = 2048; // MB
static int currentTime = 0;           // simulation clock
 ⁠

---

*Scheduling Method Signatures:*

ScheduleResult shortestJobFirst(List<Process> processes)
ScheduleResult roundRobin(List<Process> processes, int quantum)
ScheduleResult priorityScheduling(List<Process> processes)
 ⁠

---

*Shared Result Classes:*

class GanttEntry {
    int processId;
    int startTime;
    int endTime;
    int startBurst;
    int stopBurst;
}

class ScheduleResult {
    List<Process> processes;
    List<GanttEntry> ganttChart;
    double averageWaitingTime;
    double averageTurnaroundTime;
    List<Process> starvedProcesses; // used only in Priority, can be empty for others
}
 ⁠

---

*MemoryManager:*

memoryManager.allocate(process);
memoryManager.deallocate(process);
memoryManager.getAvailableMemory(); // returns int
 ⁠
