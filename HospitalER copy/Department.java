/* Code for COMP103 - 2021T2, Assignment 3
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

import ecs100.*;
import java.util.*;

/**
 * A treatment Department (Surgery, X-ray room,  ER, Ultrasound, etc)
 * Each department will need
 * - A name,
 * - A maximum number of patients that can be treated at the same time
 * - A Set of Patients that are currently being treated
 * - A Queue of Patients waiting to be treated.
 *    (ordinary queue, or priority queue, depending on argument to constructor)
 */

public class Department{

    private String name;
    private int maxPatients;
    private Queue<Patient> waitingRoom = new ArrayDeque<Patient>();
    private Set<Patient> treatmentRoom = new HashSet<Patient>();
    private boolean priority;
    
    // statistics
    private int total_processed = 0; 
    private int total_processed_time = 0;
    private int priority_processed = 0;
    private int priority_processed_time = 0;
    
    private int temp_wait_time = 0; // stores a temp wait time because patient will clear this data once the method is called

    /** 
     * Construct a new Department object
     */
    public Department(String name, int max, boolean priority){
        this.name = name;
        this.maxPatients = max;
        this.priority = priority;
        treatmentRoom = new HashSet(maxPatients);
        if (this.priority){waitingRoom = new PriorityQueue<Patient>();}
        else{waitingRoom = new ArrayDeque<Patient>();}
    } 
    
    /**
     * Draw the department: the patients being treated and the patients waiting
     * You may need to change the names if your fields had different names
     */
    public void redraw(double y){
        UI.setFontSize(14);
        UI.drawString(name, 0, y-35);
        double x = 10;
        UI.drawRect(x-5, y-30, maxPatients*10, 30);  // box to show max number of patients
        for(Patient p : treatmentRoom){
            p.redraw(x, y);
            x += 10;
        }
        x = 200;
        for(Patient p : waitingRoom){
            p.redraw(x, y);
            x += 10;
        }
    }
    
    /**
     * Adds a patient into the department, if there is room in the treatment room, put them there, else
     * add them to the queue. 
     */
    public void addPatient(Patient p){
        if(treatmentRoom.size() < maxPatients){treatmentRoom.add(p);}
        else{waitingRoom.offer(p);}
    }
    
    /**
     * Returns the name of the department
     */
    public String getName(){
        return this.name;
    }
    
    /**
     * Returns the max patients
     */
    public int getMaxPatients(){
        return this.maxPatients;
    }
    
    /**
     * Returns the waiting room
     */
    public Queue getWaitingRoom(){
        return this.waitingRoom;
    }
    
    /**
     * Returns the treatment room
     */
    public Set getTreatmentRoom(){
        return this.treatmentRoom;
    }
    
    /**
     * If there is space in the treatment room and there is patients in the waiting room,
     * move over someone from the waiting room. 
     */
    public void checkWaitingRoom(){
        while(treatmentRoom.size() < maxPatients && waitingRoom.size() != 0){
            treatmentRoom.add(waitingRoom.poll());
        }
    }
    
    /**
     * Reset/updates the waiting and treatment rooms. 
     */
    public void updateRooms(Queue<Patient> w, Set<Patient> t){
        this.waitingRoom = w;
        this.treatmentRoom = t;
    }
    
    // Statistics ---------------------------
    
    /**
     * Adds one onto the total amount of patients that the department 
     * has processed
     */
    public void addProcessed(Patient p){
        this.total_processed++;
        this.temp_wait_time = p.getCurrWaitTime();
        this.total_processed_time = this.total_processed_time + temp_wait_time; 
    }
    
    /**
     * Adds one onto the total amount of priority patients that the department 
     * has processed
     */
    public void addPriorityProcessed(){
        this.priority_processed++; 
        this.priority_processed_time = this.priority_processed_time + temp_wait_time; 
    }
    
    /**
     * Prints the statistics on the department
     */
    public void reportStatistics(){
        UI.println("------------------------------------------");
        UI.println(name + ":");
        UI.println(); 
        if(total_processed != 0){ // to stop from dividing by zero
            double tot_av = total_processed_time/total_processed;  
            UI.println(name + " has processed " + total_processed + " patients with an average waiting time of " + tot_av); 
        }else{
            UI.println(name + " had processed 0 patients"); 
        }
        
        if(priority_processed != 0){
            double priority_av = priority_processed_time/priority_processed;
            UI.println(name + " has processed " + priority_processed + " priority 1 patients with an average waiting time of " + priority_av); 
        }
    }
    
    /** 
     * Returns total processed time
     */
    public int getTotWaitTime(){
        return total_processed_time; 
    }
}
