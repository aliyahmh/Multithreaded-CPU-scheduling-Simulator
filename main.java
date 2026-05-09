
package cpu.scheduler;


import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class main {
    // System constants: time quantum for Round Robin and total memory limit
    public static final int QUANTUM      = 5;
    public static final int MEMORY_LIMIT = 2048;
    public static int currentTime = 0;
 // Store original processes after reading job.txt 
    private static Process[] originalProcesses;
    public static void main(String[] args) throws InterruptedException {

        System.out.println("+==================================+");
        System.out.println("|    CPU Scheduling Simulator      |");
        System.out.println("+==================================+");
        Scanner input = new Scanner(System.in);
     // Read job.txt 
        BlockingQueue<Process> originalJobQueue = new LinkedBlockingQueue<>();
        JobReaderThread t1 = new JobReaderThread("job.txt", originalJobQueue);
        t1.start();
        t1.join();

        originalProcesses = originalJobQueue.toArray(new Process[0]);

      
     // Main loop: keeps running until user chooses to exit
        while (true) {

            System.out.println("\n+==================================+");
            System.out.println(  "|   Choose Scheduling Algorithm    |");
            System.out.println(  "+----------------------------------+");
            System.out.println(  "|  1 - Shortest Job First (SJF)    |");
            System.out.println(  "|  2 - Round Robin (RR)            |");
            System.out.println(  "|  3 - Priority Scheduling         |");
            System.out.println(  "|  4 - Exit                        |");
            System.out.println(  "+==================================+");
            System.out.print("Your choice: ");

            // Validate user input: must be 1, 2, 3, or 4
            int choice = 0;
            while (choice < 1 || choice > 4) {
                try {
                    choice = Integer.parseInt(input.nextLine().trim());
                    if (choice < 1 || choice > 4)
                        System.out.print("Invalid! Enter 1, 2, 3, or 4: ");
                } catch (NumberFormatException e) {
                    System.out.print("Invalid! Enter 1, 2, 3, or 4: ");
                }
            }// Exit
            if (choice == 4) {
                System.out.println("\n+==================================+");
                System.out.println(  "|        Thank you! Goodbye!       |");
                System.out.println(  "+==================================+");
                input.close();
                return;
            }
            // Prepare fresh scheduler for each run
            Schedule scheduler = prepareScheduler();
            ScheduleResult result;

            switch (choice) {
                case 1: result = scheduler.shortestJobFirst();   
                break;
                case 2: result = scheduler.roundRobin();      
                break;
                case 3: result = scheduler.priorityScheduling(); 
                break;
                default: System.out.println("Invalid choice!"); 
                return;
            }

            // Print results: Gantt chart, process table, and performance metrics
            OutputFormatter.printGanttChart(result.getGanttChart());
            OutputFormatter.printTable(result.getProcesses());
            OutputFormatter.printMetrics(result);
            
            // Print starvation report only for Priority Scheduling
            if (choice == 3)
                OutputFormatter.printStarvation(result.getStarvedProcesses());
        }
    }

  
    private static Schedule prepareScheduler() throws InterruptedException {
        BlockingQueue<Process> jobQueue = new LinkedBlockingQueue<>();
        BlockingQueue<Process> readyQueue = new LinkedBlockingQueue<>();

        for (Process p : originalProcesses) {
            Process fresh = new Process(
                    p.getProcessId(),
                    p.getBurstTime(),
                    p.getOriginalPriority(),
                    p.getMemoryRequired(),
                    p.getArrivalOrder()
            );

            jobQueue.add(fresh);
        }

        MemoryManager memoryManager = new MemoryManager();
        System.out.println("Memory Check Start: " + memoryManager.getAvailableMemory() + " MB");

        int totalProcesses = jobQueue.size();

        JobLoaderThread t2 = new JobLoaderThread(
                jobQueue,
                readyQueue,
                memoryManager,
                totalProcesses
        );

        t2.start();
        
        return new Schedule(readyQueue, t2);
    }
}

        
        
        
        
        
        
        
        
        
        
        
        
