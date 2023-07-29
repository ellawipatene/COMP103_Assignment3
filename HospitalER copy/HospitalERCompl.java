/* Code for COMP103 - 2021T2, Assignment 3
 * Name: Ella Wipatene
 * Username: wipateella
 * ID: 300558005
 */

import ecs100.*;
import java.util.*;
import java.io.*;
import java.awt.*;

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

    // Fields for recording the patients waiting in the waiting room and being treated in the treatment room
    private Map<String, Department> departments = new HashMap<String, Department>();
    
    // fields for the statistics - these are for the hospital as a whole
    private int total_processed = 0;
    private int total_processed_time = 0;
    private int priority_processed = 0;
    private int priority_processed_time = 0;

    // Fields for the simulation
    private boolean running = false;
    private int time = 0; // The simulated time - the current "tick"
    private int delay = 300;  // milliseconds of real time for each tick

    // fields controlling the probabilities.
    private int arrivalInterval = 5;   // new patient every 5 ticks, on average
    private double probPri1 = 0.1; // 10% priority 1 patients
    private double probPri2 = 0.2; // 20% priority 2 patients
    private Random random = new Random();  // The random number generator.

    /**
     * Construct a new HospitalERComp object, setting up the GUI, and resetting
     */
    public static void main(String[] arguments){
        HospitalERCompl er = new HospitalERCompl();
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

        // reset the fields
        departments = new HashMap<String, Department>();
        departments.put("ER", new Department("ER", 8, usePriorityQueue));
        departments.put("MRI", new Department("MRI", 1, usePriorityQueue));
        departments.put("Surgery", new Department("Surgery", 2, usePriorityQueue));
        departments.put("X-ray", new Department("X-ray", 2, usePriorityQueue));
        departments.put("Ultrasound", new Department("Ultrasound", 3, usePriorityQueue));

        UI.clearGraphics();
        UI.clearText();
    }

    /**
     * Main loop of the simulation
     */
    public void run(){
        if (running) { return; }
        running = true;
        while (running){       
            time++;
            
            Collection<Department> allDepartments = departments.values(); // Collection of the department values
            for (Department d: allDepartments) {
                ArrayList<Patient> removePatient = new ArrayList<Patient>(); // Patients that have finished their treatment
                Queue<Patient> tempWaiting = d.getWaitingRoom(); // To temp hold the waiting room from one department
                Set<Patient> tempTreatment = d.getTreatmentRoom();  // To temp hold the treatment room from one depatment
                
                for (Patient p : tempTreatment) { // For each patient in the department
                    if (p.completedCurrentTreatment()) { 
                        removePatient.add(p); // Remove if they have completed treatment 
                        d.addProcessed(p); 
                        if(p.getPriority() == 1){d.addPriorityProcessed();}
                        
                        p.incrementTreatmentNumber();
                        if (!p.noMoreTreatments()) {  // if they have more treatments
                            UI.println(time + ": Moved Departments: " + p); 
                            departments.get(p.getCurrentTreatment()).addPatient(p);  // move them to that department
                        }else{
                            // statistics 
                            UI.println(time + ": Discharge: " + p);
                            total_processed_time = total_processed_time + p.getWaitingTime();
                            total_processed++; 
                            if(p.getPriority() == 1){
                                priority_processed++;
                                priority_processed_time = priority_processed_time + p.getWaitingTime();
                            }
                        }
                    } else {
                        p.advanceTreatmentByTick(); // for all patinets in the treatment room, advance by a tick
                    }
                }
                
                // Removes the patients down here so that it does not mess up the for loop 
                for (Patient item : removePatient) {
                    tempTreatment.remove(item);
                } 
                // Advances everyone in the waiting room by one tick
                for (Patient w : tempWaiting) {
                    w.waitForATick();
                }                   

                d.updateRooms(tempWaiting, tempTreatment); // update the departments waiting rooms
                d.checkWaitingRoom(); // if there is now room in the treatment room, move a patient from the waiting room
            }
            
            // get new patient and add them to the correct department
            if (time==1 || Math.random()<1.0/arrivalInterval){
                Patient newPatient = new Patient(time, randomPriority());
                UI.println(time+ ": Arrived: "+newPatient);
                if (departments.containsKey(newPatient.getCurrentTreatment())){
                    departments.get(newPatient.getCurrentTreatment()).addPatient(newPatient); 
                }
            }
            redraw();
            UI.sleep(delay);
        }
        
        reportStatistics();
    }

    // Additional methods used by run() (You can define more of your own)

    /**
     * Report summary statistics about all the patients that have been discharged.
     * (Doesn't include information about the patients currently waiting or being treated)
     * The run method should have been recording various statistics during the simulation.
     */
    public void reportStatistics(){
        UI.println();
        UI.println("Overall Hospital Statistics:");
        if(total_processed != 0){ // to stop from dividing by zero
            double tot_av = total_processed_time/total_processed;  
            UI.println("Processed " + total_processed + " patients with an average waiting time of " + tot_av); 
        }else{
            UI.println("Processed 0 patients"); 
        }
        
        if(priority_processed != 0){
            double priority_av = priority_processed_time/priority_processed;
            UI.println("Processed " + priority_processed + " priority 1 patients with an average waiting time of " + priority_av); 
        }
        
        Collection<Department> allDepartments = departments.values();
        for(Department d: allDepartments){
            d.reportStatistics(); 
            UI.println();
        }
        
        drawGraph();
    }
    
    
    /**
     * Print a graph to display the statistics - NOT COMPLETE!!!!
     */
    public void drawGraph(){
        Collection<Department> allDepartments = departments.values();
        UI.setColor(Color.black);
        UI.drawLine(50, 650, 650, 650);
        UI.drawLine(50, 500, 50, 650);
    
        int max = findMax(); 
        double max_val = (double)max/3; 
        // The y axis
        for(int i = 0; i < 4; i++){
            if (i == 3){
                UI.drawString(String.valueOf(max), 10, 655  - (i*50));
            } else {
                UI.drawString(String.valueOf((int)max_val * i ), 10, 655  - (i*50));
            }
            UI.drawLine(45, 500 + (i*50), 50, 500 + (i*50));
            
        }
        
        // The x axis
        for(int i = 0; i < allDepartments.size(); i++){UI.drawLine(50 + (100*i), 650, 50 + (100*i), 655);}

        
        int text_coord = 75; 
        int graph_coord = 0;
        for(Department d: allDepartments){
            UI.drawString(d.getName(), text_coord, 675); 
            
            UI.setColor(Color.green);
            UI.fillRect(graph_coord, 650 - (150 * ((double)d.getTotWaitTime()/(double)max)), 70 + graph_coord, (150 * ((double)d.getTotWaitTime()/(double)max))); 
            
            text_coord = text_coord + 100;
            graph_coord = graph_coord + 75;
            
            UI.setColor(Color.black); 
            UI.drawString(String.valueOf(d.getTotWaitTime()), 135, 600);             
        }
    
    }
    
    /**
     * Returns the department with the largest wait time
     */
    public int findMax(){
        int max = -9999999; 
        Collection<Department> allDepartments = departments.values();
        for(Department d: allDepartments){
            if(d.getTotWaitTime() > max){max = d.getTotWaitTime();}
        }
        return max; 
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
        
        int y = 80;
        Collection<Department> allDepartments = departments.values();
        for (Department d: allDepartments){ 
            // Makes sure ER is drawn at the top 
            if(d.getName().equals("ER")){
                d.redraw(y);
                UI.drawLine(0,y+2,400, y+2);
                y = y+60;
            }
        }
        for (Department d: allDepartments){
            if(!d.getName().equals("ER")){
                d.redraw(y);
                UI.drawLine(0,y+2,400, y+2);
                y = y+60;
            }
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
