package cpu.scheduler;


import java.util.*;
	import java.util.concurrent.BlockingQueue;


	public class Schedule {

	    private static final int TIME_QUANTUM = 5; // ms - Round Robin

	    private BlockingQueue<Process> readyQueue;
	    private JobLoaderThread jobLoader;

	    private List<GanttEntry> gantt;
	    private int currentTime;


	    public Schedule(BlockingQueue<Process> readyQueue, JobLoaderThread jobLoader) {
	        this.readyQueue  = readyQueue;
	        this.jobLoader   = jobLoader;
	        this.gantt       = new ArrayList<>();
	        this.currentTime = 0;
	    }

	    // Shortest Job First (Non-Preemptive)
	     
	    public ScheduleResult shortestJobFirst() {
    gantt.clear();
    currentTime = 0;

    List<Process> queue = new ArrayList<>();
    List<Process> allProcesses = new ArrayList<>();
    int completed = 0;
    int total = jobLoader.getTotalProcesses();

    System.out.println("\n Starting The Shortest Job First (SJF) ");

    while (completed < total) {

        // collect newly admitted processes
        readyQueue.drainTo(queue);

        if (queue.isEmpty()) {
            try {
                Process p = readyQueue.take(); // wait until loader admits one
                queue.add(p);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        // select shortest burst time, tie by arrival order
        Process toRun = queue.get(0);
        for (Process p : queue) {
            if (p.getBurstTime() < toRun.getBurstTime() ||
                (p.getBurstTime() == toRun.getBurstTime()
                 && p.getArrivalOrder() < toRun.getArrivalOrder())) {
                toRun = p;
            }
        }

        queue.remove(toRun);

        if (!allProcesses.contains(toRun)) {
            allProcesses.add(toRun);
        }

        toRun.setStartTime(currentTime);
        toRun.setState("running");

        GanttEntry entry = new GanttEntry(
                toRun.getProcessId(), currentTime, toRun.getRemainingTime());

        while (toRun.getRemainingTime() > 0) {
            for (Process p : queue) {
                p.setWaitingTime(p.getWaitingTime() + 1);
            }

            // also add any newly admitted processes while this one is running
            readyQueue.drainTo(queue);

            toRun.setRemainingTime(toRun.getRemainingTime() - 1);
            currentTime++;
        }

        toRun.setState("terminated");
        toRun.setTerminationTime(currentTime);
        toRun.setTurnaroundTime(currentTime);

        entry.setEndTime(currentTime);
        entry.setBurstAtEnd(0);
        gantt.add(entry);

        jobLoader.notifyProcessCompleted(toRun);
        completed++;
    }

    double avgWT = 0, avgTAT = 0;
    for (Process p : allProcesses) {
        avgWT += p.getWaitingTime();
        avgTAT += p.getTurnaroundTime();
    }

    if (!allProcesses.isEmpty()) {
        avgWT /= allProcesses.size();
        avgTAT /= allProcesses.size();
    }

    return new ScheduleResult(allProcesses, gantt, avgWT, avgTAT, new ArrayList<>());
}

	      

	    //Round Robin (q = 5 ms)

	    public ScheduleResult roundRobin() {
			gantt.clear();
			currentTime = 0;
		
			Queue<Process> rrQueue = new LinkedList<>();
			List<Process> allProcesses = new ArrayList<>();
		
			int completed = 0;
			int total = jobLoader.getTotalProcesses();
		
			System.out.println("\nStarting Round Robin (RR) (q=" + TIME_QUANTUM + " ms)");
		
			while (completed < total) {
		
				readyQueue.drainTo(rrQueue);
		
				if (rrQueue.isEmpty()) {
					try {
						Process p = readyQueue.take();
						rrQueue.add(p);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
		
				Process toRun = rrQueue.poll();
		
				if (!allProcesses.contains(toRun)) {
					allProcesses.add(toRun);
				}
		
				toRun.setState("running");
		
				if (toRun.getStartTime() == -1) {
					toRun.setStartTime(currentTime);
				}
		
				int slice = Math.min(TIME_QUANTUM, toRun.getRemainingTime());
				int burstAtStart = toRun.getRemainingTime();
		
				System.out.printf("[RR] Dispatching P%d | Remaining: %d ms | Slice: %d ms | t=%d%n",
						toRun.getProcessId(), burstAtStart, slice, currentTime);
		
				GanttEntry entry = new GanttEntry(
						toRun.getProcessId(), currentTime, burstAtStart);
		
				for (int tick = 0; tick < slice; tick++) {
					for (Process p : rrQueue) {
						p.setWaitingTime(p.getWaitingTime() + 1);
					}
		
					readyQueue.drainTo(rrQueue);
		
					toRun.setRemainingTime(toRun.getRemainingTime() - 1);
					currentTime++;
				}
		
				entry.setEndTime(currentTime);
				entry.setBurstAtEnd(toRun.getRemainingTime());
				gantt.add(entry);
		
				if (toRun.getRemainingTime() == 0) {
					toRun.setState("terminated");
					toRun.setTerminationTime(currentTime);
					toRun.setTurnaroundTime(currentTime);
		
					jobLoader.notifyProcessCompleted(toRun);
					completed++;
		
					System.out.printf("[RR] P%d done | WT: %d ms | TAT: %d ms%n",
							toRun.getProcessId(), toRun.getWaitingTime(), toRun.getTurnaroundTime());
				} else {
					toRun.setState("ready");
					rrQueue.add(toRun);
				}
			}
		
			double avgWT = 0, avgTAT = 0;
		
			for (Process p : allProcesses) {
				avgWT += p.getWaitingTime();
				avgTAT += p.getTurnaroundTime();
			}
		
			if (!allProcesses.isEmpty()) {
				avgWT /= allProcesses.size();
				avgTAT /= allProcesses.size();
			}
		
			return new ScheduleResult(allProcesses, gantt, avgWT, avgTAT, new ArrayList<>());
		}



	    /* Priority Scheduling (Non-Preemptive). */
		public ScheduleResult priorityScheduling() {
			gantt.clear();
			currentTime = 0;
		
			List<Process> queue = new ArrayList<>();
			List<Process> allProcesses = new ArrayList<>();
			List<Process> starvedProcesses = new ArrayList<>();
		
			int completed = 0;
			int total = jobLoader.getTotalProcesses();
		
			System.out.println("\n Starting Priority Scheduling (Non-Preemptive) ");
		
			while (completed < total) {
		
				List<Process> newlyAdmitted = new ArrayList<>();
				readyQueue.drainTo(newlyAdmitted);
		
				for (Process p : newlyAdmitted) {
					p.setLastReadyTime(currentTime);
					p.setLastAgedTime(currentTime);
					queue.add(p);
		
					if (!allProcesses.contains(p)) {
						allProcesses.add(p);
					}
				}
		
				if (queue.isEmpty()) {
					try {
						Process p = readyQueue.take();
						p.setLastReadyTime(currentTime);
						p.setLastAgedTime(currentTime);
						queue.add(p);
		
						if (!allProcesses.contains(p)) {
							allProcesses.add(p);
						}
		
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						break;
					}
				}
		
				checkStarvationAndAge(queue, starvedProcesses);
		
				Process toRun = queue.get(0);
		
				for (Process p : queue) {
					if (p.getPriority() < toRun.getPriority()) {
						toRun = p;
					} else if (p.getPriority() == toRun.getPriority()) {
						if (p.getArrivalOrder() < toRun.getArrivalOrder()) {
							toRun = p;
						}
					}
				}
		
				queue.remove(toRun);
				toRun.setState("running");
		
				if (toRun.getStartTime() == -1) {
					toRun.setStartTime(currentTime);
				}
		
				System.out.printf("[Priority] Dispatching P%d | Priority: %d (original: %d) "
								+ "| Burst: %d ms | t=%d%n",
						toRun.getProcessId(), toRun.getPriority(),
						toRun.getOriginalPriority(), toRun.getRemainingTime(), currentTime);
		
				GanttEntry entry = new GanttEntry(
						toRun.getProcessId(), currentTime, toRun.getRemainingTime());
		
				while (toRun.getRemainingTime() > 0) {
		
					for (Process p : queue) {
						p.setWaitingTime(p.getWaitingTime() + 1);
					}
		
					List<Process> newDuringRun = new ArrayList<>();
					readyQueue.drainTo(newDuringRun);
		
					for (Process p : newDuringRun) {
						p.setLastReadyTime(currentTime);
						p.setLastAgedTime(currentTime);
						queue.add(p);
		
						if (!allProcesses.contains(p)) {
							allProcesses.add(p);
						}
					}
		
					checkStarvationAndAge(queue, starvedProcesses);
		
					toRun.setRemainingTime(toRun.getRemainingTime() - 1);
					currentTime++;
				}
		
				toRun.setState("terminated");
				toRun.setTerminationTime(currentTime);
				toRun.setTurnaroundTime(currentTime);
		
				entry.setEndTime(currentTime);
				entry.setBurstAtEnd(0);
				gantt.add(entry);
		
				jobLoader.notifyProcessCompleted(toRun);
				completed++;
		
				System.out.printf("[Priority] P%d done | WT: %d ms | TAT: %d ms | Starved: %s%n",
						toRun.getProcessId(),
						toRun.getWaitingTime(),
						toRun.getTurnaroundTime(),
						toRun.isStarved() ? "YES" : "NO");
			}
		
			double avgWT = 0, avgTAT = 0;
		
			for (Process p : allProcesses) {
				avgWT += p.getWaitingTime();
				avgTAT += p.getTurnaroundTime();
			}
		
			if (!allProcesses.isEmpty()) {
				avgWT /= allProcesses.size();
				avgTAT /= allProcesses.size();
			}
		
			return new ScheduleResult(allProcesses, gantt, avgWT, avgTAT, starvedProcesses);
		}

	    /* Priority helper method: Checks each process in the queue for starvation and applies aging. */
	    private void checkStarvationAndAge(List<Process> queue, List<Process> starvedProcesses) {
	        int n = queue.size();
	        if (n == 0) return;

	        int starvationThreshold = n * 5; // N × 5 ms

	        for (Process p : queue) {

	            int waitingSince = currentTime - p.getLastReadyTime();

	            // Check starvation
	            if (waitingSince > starvationThreshold) {

	                // Mark as starved (first time detection)
	                if (!p.isStarved()) {
	                    p.setStarved(true);
	                    p.setStarvationStartTime(currentTime);
	                    starvedProcesses.add(p);
	                    System.out.printf("[Priority] P%d is STARVED (waited %d ms > threshold %d ms) at t=%d%n",
	                            p.getProcessId(), waitingSince, starvationThreshold, currentTime);
	                }

	                // Apply aging every 4 ms
	                if (currentTime - p.getLastAgedTime() >= 4) {
	                    if (p.getPriority() > 1) { 
	                        p.setPriority(p.getPriority() - 1);
	                        p.setLastAgedTime(currentTime);
	                        System.out.printf("[Priority] AGING P%d: priority %d -> %d at t=%d%n",
	                                p.getProcessId(), p.getPriority() + 1,
	                                p.getPriority(), currentTime);
	                    }
	                }
	            }
	        }
	    }


	    public List<GanttEntry> getGantt() { 
	        return gantt; 
	    }
	    public int getCurrentTime() { 
	        return currentTime; 
	    }
	}


