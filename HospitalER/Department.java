// This program is copyright VUW.
// You are granted permission to use it to construct your answer to a COMP103 assignment.
// You may not distribute it in any other way without permission.

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

    /*# YOUR CODE HERE */
    public Set<Patient> treatmentRoom; 
    public Queue<Patient> waitingRoom;
    
    /**
     * This creates a new Department object 
     */
    public Department(String name, int max, boolean priority){
        this.maxPatients = max; 
        this.name = name;
        this.treatmentRoom = new HashSet<Patient>(); 
        
        if(priority){
            this.waitingRoom = new PriorityQueue<Patient>();
        }else{
            this.waitingRoom = new ArrayDeque<Patient>();
        }
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
    
    public Queue<Patient> getWaitingRoom(){
        return waitingRoom; 
    }
    
    public Set<Patient> getTreatmentRoom(){
        return treatmentRoom; 
    }
    
    public int getMaxPatients(){
        return maxPatients; 
    }
    
    public String getName(){
        return this.name;
    }
    
    /**
     * Add a new patient into the treatment room if there is enough room
     * Else they will be added to the waiting room 
     */
    
    public void addPatient(Patient p){
        if(treatmentRoom.size() < maxPatients){
            treatmentRoom.add(p);
        }
        else{
            waitingRoom.offer(p); 
        }
    }
    
    /**
     * If the treatmentRoom is not full, get a patient from the front of the waiting room
     * queue and put them into the treatment room. 
     */
    public void movePatient(){
        if(treatmentRoom.size() < maxPatients && waitingRoom.size() != 0){
            Patient temp = waitingRoom.remove();
            treatmentRoom.add(temp); 
        } 
    }
    
    public void updateRooms(Set treatment_room, Queue waiting_room){
        this.treatmentRoom = treatment_room;
        this.waitingRoom = waiting_room; 
    }
    
    
}
