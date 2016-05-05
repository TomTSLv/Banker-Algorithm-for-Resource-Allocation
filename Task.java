import java.util.*;

public class Task {
	//Task Number
	private int task_number;
	
	//This HashMap maps resource type to the initial claim of this resource of this task
	private HashMap<Integer, Integer> initial_claim;
	
	//This HashMap maps resource type to the how many resources of this kind is occupied by this task
	private HashMap<Integer, Integer> occupied_resource;
	
	//ArrayList which stores the activities of this task in order
	private ArrayList<int[]> activities;
	
	//Wait time (time when the task is blocked)
	private int wait_time=0;
	
	//End time (when the task is terminated)
	private int end_time;
	
	//whether the task is aborted or not
	private boolean aborted;
	
	public Task(int task_number){
		this.task_number=task_number;
		this.activities=new ArrayList<int[]>();
		this.initial_claim=new HashMap<Integer, Integer>();
		this.occupied_resource=new HashMap<Integer, Integer>();
		this.aborted=false;
	}
	
	public int getTask_number() {
		return task_number;
	}

	public void setTask_number(int task_number) {
		this.task_number = task_number;
	}

	public HashMap<Integer, Integer> getInitial_claim() {
		return initial_claim;
	}

	public void setInitial_claim(HashMap<Integer, Integer> initial_claim) {
		this.initial_claim = initial_claim;
	}

	public HashMap<Integer, Integer> getOccupied_resource() {
		return occupied_resource;
	}

	public void setOccupied_resource(HashMap<Integer, Integer> occupied_resource) {
		this.occupied_resource = occupied_resource;
	}

	public ArrayList<int[]> getActivities() {
		return activities;
	}

	public void setActivities(ArrayList<int[]> activities) {
		this.activities = activities;
	}

	public int getWait_time() {
		return wait_time;
	}

	public void setWait_time(int wait_time) {
		this.wait_time = wait_time;
	}

	public int getEnd_time() {
		return end_time;
	}

	public void setEnd_time(int end_time) {
		this.end_time = end_time;
	}

	public boolean isAborted() {
		return aborted;
	}

	public void setAborted(boolean aborted) {
		this.aborted = aborted;
	}

}
