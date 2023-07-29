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
import java.io.*;

/**
 * Simulation of a Hospital ER
 * 
 * The hospital has a collection of Departments, including the ER department, each of which has
 *  and a treatment room.
 * 
 * When patients arrive at the hospital, they are immediately assessed by the
 *  triage team who determine the priority of the patient and (unrealistically) a sequence of treatments 
 *  that the patient will need.
 *
 * The simulation should move patients through the departments for each of the required treatments,
 * finally discharging patients when they have completed their final treatment.
 *
 *  READ THE ASSIGNMENT PAGE!
 */

public class HospitalERCompl{

    // Copy the code from HospitalERCore and then modify/extend to handle multiple departments

    /*# YOUR CODE HERE */
    // Map of the departments 
    public Map<String, Department> departments; 
    private Queue<Patient> currentWaiting = new ArrayDeque<Patient>();
    private Set<Patient> currentTreatment = new HashSet<Patient>();

    // fields for the statistics
    /*# YOUR CODE HERE */
    public int total_processed = 0; 
    public int total_processed_time = 0; 
    public int priority_processed = 0; 
    public int priority_processed_time = 0; 

    // Fields for the simulation
    private boolean running = false;
    public int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    // fields controlling the probabilities.
    private int arrivalInterval = 5;   // new patient every 5 ticks, on average
    private double probPri1 = 0.1; // 10% priority 1 patients
    private double probPri2 = 0.2; // 20% priority 2 patients
    private Random random = new Random();  // The random number generator.

    /**
     * Construct a new HospitalERCore object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments){
        HospitalERCore er = new HospitalERCore();
        er.setupGUI();
        er.reset(false);   // initialise with an ordinary queue.
    }        

    /**
     * Set up the GUI: buttons to control simulation and sliders for setting parameters
     */
    public void setupGUI(){
        UI.addButton("Reset (Queue)", () -> {this.reset(false); });
        UI.addButton("Reset (Pri Queue)", () -> {this.reset(true);});
        UI.addButton("Start", ()->{if (!running){ run(); }});   //don't start if already running!
        UI.addButton("Pause & Report", ()->{running=false;});
        UI.addSlider("Speed", 1, 400, (401-delay),
            (double val)-> {delay = (int)(401-val);});
        UI.addSlider("Av arrival interval", 1, 50, arrivalInterval,
            (double val)-> {arrivalInterval = (int)val;});
        UI.addSlider("Prob of Pri 1", 1, 100, probPri1*100,
            (double val)-> {probPri1 = val/100;});
        UI.addSlider("Prob of Pri 2", 1, 100, probPri2*100,
            (double val)-> {probPri2 = Math.min(val/100,1-probPri1);});
        UI.addButton("Quit", UI::quit);
        UI.setWindowSize(1000,600);
        UI.setDivider(0.5);
    }
    
        /**
     * Reset the simulation:
     *  stop any running simulation,
     *  reset the waiting and treatment rooms
     *  reset the statistics.
     */
    public void reset(boolean usePriorityQueue){
        running=false;
        UI.sleep(2*delay);  // to make sure that any running simulation has stopped
        time = 0;           // set the "tick" to zero.
        
        // reset the waiting room, the treatment room, and the statistics.
        departments = new HashMap<String, Department>();
        departments.put("ER", new Department("ER", 5, usePriorityQueue)); 
        departments.put("Surgery",new Department("Surgery", 2, usePriorityQueue));
        departments.put("X-ray", new Department("X-ray", 2, usePriorityQueue));
        departments.put("MRI", new Department("MRI", 1, usePriorityQueue));
        departments.put("Ultrasound", new Department("Ultrasound", 3, usePriorityQueue)); 
        
        total_processed = 0; 
        priority_processed = 0; 
        
        UI.clearGraphics();
        UI.clearText();
    }
    
        /**
     * Main loop of the simulation
     */
    public void run(){
        if (running) { return; } // don't start simulation if already running one!
        running = true;
        while (running){         // each time step, check whether the simulation should pause
            time++;  // advance the sim by one tick
            
            List<Patient> movedDepartment = new ArrayList<Patient>(); // stores the patients that need to move departments
            Collection<Department> allDepartments = departments.values();
            for (Department d: allDepartments) {
                List<Patient> removeDepartment = new ArrayList<Patient>();
                currentTreatment = d.getTreatmentRoom();
                currentWaiting = d.getWaitingRoom();
                for (Patient p : currentTreatment) {
                    if (p.completedCurrentTreatment()) {
                        removeDepartment.add(p);      // they can be removed from the department
                        if (!(p.noMoreTreatments())) {  // if still more treatments
                            p.incrementTreatmentNumber();
                            movedDepartment.add(p);   // they will move departments
                        }
                    } else {
                        p.advanceTreatmentByTick(); // if they have not finished their treatment, they will advance treatment
                    }
                }
                for (Patient item : removeDepartment) {
                    currentTreatment.remove(item);
                } // removes patients
                for (Patient w : currentWaiting) {
                    w.waitForATick();
                }                   // everyone in the waiting room waits one tick

                // resets the current treatment and waiting room, and moves patients up if there is space
                d.updateRooms(currentTreatment, currentWaiting);
                departments.get(d.getName()).updateRooms(currentTreatment, currentWaiting); // same thing, idk which one is better to use
                currentTreatment = new HashSet<Patient>();
                currentWaiting = new ArrayDeque<Patient>();
                d.movePatient(); // same thing, idk whuch one is better to use
                departments.get(d.getName()).movePatient();
            }
            
            for(Patient m: movedDepartment){
                if (!(m.noMoreTreatments())){
                    departments.get(m.getCurrentTreatment()).addPatient(m); 
                }
                else{
                    UI.println(time + ": Discharge: " + m);
                    //totWaitTime = totWaitTime + m.getWaitingTime();
                    //totPatients++;   
                    if(m.getPriority() == 1){
                        //pri1Patients++;
                        //pri1WaitTime = pri1WaitTime + m.getWaitingTime();
                    }
                }
            }
            

            // Get any new patient that has arrived and add them to the waiting room
            if (time==1 || Math.random()<1.0/arrivalInterval){
                Patient newPatient = new Patient(time, randomPriority());
                UI.println(time+ ": Arrived: "+newPatient);
                departments.get("ER").addPatient(newPatient); // adds the patient to the waiting room ER queue
            }
            redraw();
            UI.sleep(delay);
        }
        // paused, so report current statistics
        reportStatistics();
    }
    
    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     * (Doesn't include information about the patients currently waiting or being treated)
     * The run method should have been recording various statistics during the simulation.
     */
    public void reportStatistics(){
        if(total_processed != 0){
            double tot_av = total_processed_time/total_processed; 
            UI.println("Processed " + total_processed + " patients with an average waiting time of " + tot_av); 
        }
        
        if(priority_processed != 0){
            double priority_av = priority_processed_time/priority_processed;
            UI.println("Processed " + priority_processed + " priority 1 patients with an average waiting time of " + priority_av); 
        }
    }


    // HELPER METHODS FOR THE SIMULATION AND VISUALISATION
    /**
     * Redraws all the departments
     */
    public void redraw(){
        UI.clearGraphics();
        UI.setFontSize(14);
        UI.drawString("Treating Patients", 5, 15);
        UI.drawString("Waiting Queues", 200, 15);
        UI.drawLine(0,32,400, 32);

        // Draw the treatment room and the waiting room:
        int y = 80;
        Collection<Department> allDepartments = departments.values();
        for (Department d: allDepartments){
            d.redraw(y);
            UI.drawLine(0,y+2,400, y+2);
            y = y+60;
        }
    }
    
    /** 
     * Returns a random priority 1 - 3
     * Probability of a priority 1 patient should be probPri1
     * Probability of a priority 2 patient should be probPri2
     * Probability of a priority 3 patient should be (1-probPri1-probPri2)
     */
    private int randomPriority(){
        double rnd = random.nextDouble();
        if (rnd < probPri1) {return 1;}
        if (rnd < (probPri1 + probPri2) ) {return 2;}
        return 3;
    }

}

