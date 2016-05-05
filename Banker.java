import java.util.*;

public class Banker {
	private ArrayList<Task> taskList;
	
	private ArrayList<Resource> resourceList;
	
	//blockList is used to store index of blocked task in taskList in order in order to check blocked tasks first in each cycle
	private ArrayList<Integer> blockList;
	
	/*Stores the index of the activity we are currently dealing with in the activity list for each task
	 * index of taskStatusArray corresponds with index of the task in the taskList
	 */
	int[] taskStatusArray;
	
	//Represents how many tasks are running
	int runningNum;
	
	//Stores the status of each task
	String[] statusArray;
	
	//use to change the status of each task temporarily when determine safe states
	String[] testStatusArray;
	
	public Banker(ArrayList<Task> taskList, ArrayList<Resource> resourceList){
		this.taskList=taskList;
		this.resourceList=resourceList;
		this.blockList=new ArrayList<Integer>();
		this.taskStatusArray=new int[this.taskList.size()];
		this.statusArray=new String[this.taskList.size()];
		this.testStatusArray=new String[this.taskList.size()];
		this.runningNum=this.taskList.size();
	}
	
	//Run the Banker's algorithm to allocate resources
	public void run(boolean verbose){
		if (verbose) System.out.println("Banker Algorithm Begins.\n");
		int cycle=0;
		//All tasks are running at the very beginning
		for (int i=0;i<taskList.size();i++){
			statusArray[i]="running";
			testStatusArray[i]="running";
		}
		//how much time we have spent in a single compute
		int[] computeArray=new int[taskList.size()];
		//how much time in total this single compute needs
		int[] originalComputeArray=new int[taskList.size()];
		int[] activity;
		/*
		 * tempRemaining temporarily shows the remaining number of each resource
		 * (the resource released this cycle is available in the next cycle)
		 */
		int[] tempRemaining=new int[resourceList.size()];
		while (true){
			
			//When all tasks are either terminated or aborted, the Banker's algorithm is done
			boolean terminate=true;
			for (int i=0;i<taskList.size();i++){
				if ((!statusArray[i].equals("terminated")) && (!taskList.get(i).isAborted())) terminate=false;
			}
			if (terminate) break;
			
			//Update initial remaining resources for this cycle
			if (verbose) System.out.println("During "+cycle+"-"+(cycle+1));
			for (int j=0;j<resourceList.size();j++){
				tempRemaining[j]=resourceList.get(j).getRemaining();
			}
			
			//First check blocked task in the order of when it's blocked
			for (int j=0;j<blockList.size();j++){
				if (verbose) {
					if (j==0) System.out.println("First check blocked tasks: ");
				}
				int i=blockList.get(j);
				/*If the task isn't aborted,
				 * if all the requested (not released) resources exceeds initial claim, abort the task
				 * if the request is unsafe, task stay blocked,
				 * but if it is safe and the remaining resource is enough for allocation, allocate the resource
				 * but the task shall wait for this cycle to run.
				 */
				activity=taskList.get(i).getActivities().get(taskStatusArray[i]);
				if (activity[3]<=resourceList.get(activity[2]-1).getRemaining()){
					taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])+activity[3]);
					if (taskList.get(i).getOccupied_resource().get(activity[2])>taskList.get(i).getInitial_claim().get(activity[2])){
						statusArray[i]="aborted";
						testStatusArray[i]="aborted";
						taskList.get(i).setAborted(true);
						if (verbose) System.out.println("Task "+taskList.get(i).getTask_number()+"'s request exceeds its claim; aborted;");
						blockList.remove(j);
						j--;
						taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])-activity[3]);
						tempRemaining[activity[2]-1]+=taskList.get(i).getOccupied_resource().get(activity[2]);
						continue;
					}
					taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])-activity[3]);
					statusArray[i]="running";
					testStatusArray[i]="running";
					runningNum+=1;
					if (!detectSafeRecur(taskList.get(i),activity)){
						statusArray[i]="blocked";
						testStatusArray[i]="blocked";
						runningNum-=1;
						if (verbose) System.out.println("\tcannot grant task"+taskList.get(i).getTask_number()+"'s request (not safe)");
						continue;
					}
					runningNum-=1;
					taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])+activity[3]);
					resourceList.get(activity[2]-1).setRemaining(resourceList.get(activity[2]-1).getRemaining()-activity[3]);
					tempRemaining[activity[2]-1]-=activity[3];
					taskStatusArray[i]+=1;
					if (verbose) System.out.println("\tcan grant task "+ (i+1)+"'s request");
					statusArray[i]="waitOneCycle";
					testStatusArray[i]="waitOneCycle";
					blockList.remove(j);
					j--;
				}
				else{
					if (verbose) System.out.println("\ttask "+ (i+1)+" is still blocked");
				}
			}
			
			//Deal with activities of unaborted and unterminated resources
			for (int i=0;i<taskList.size();i++){
				Task task=taskList.get(i);
				if (task.isAborted()) continue;
				//If the task is running, deal with the next activity
				if (statusArray[i].equals("running")){
					activity=task.getActivities().get(taskStatusArray[i]);
					//Initialization: set initial claim, set occupied resource to 0 at first
					if (activity[0]==0){
						if (activity[3]>resourceList.get(activity[2]-1).getTotal()){
							statusArray[i]="aborted";
							testStatusArray[i]="aborted";
							runningNum-=1;
							task.setAborted(true);
							System.out.println("Banker's abort task "+task.getTask_number()+" before run begins:");
							System.out.println("\tclaim for resource "+activity[2]+" ("+activity[3]+") exceeds number of units present ("+resourceList.get(activity[2]-1).getTotal()+")\n");
							if (verbose) System.out.println("Task "+task.getTask_number()+"'s request exceeds its claim; aborted;");
							continue;
						}
						task.getInitial_claim().put(activity[2], activity[3]);
						task.getOccupied_resource().put(activity[2], 0);
						if (verbose) System.out.println("Task "+task.getTask_number()+" does initialization.");
						taskStatusArray[i]+=1;
					}
					/*
					 * Request: first, if all the requested (not released) resources exceeds initial claim, abort the task
					 * second, if the allocation is unsafe, blocked the task
					 * third, if the remaining resources is not enough for allocation, block the task
					 * If no these three conditions, complete the request.
					 */
					else if (activity[0]==1){
						if (activity[3]<=resourceList.get(activity[2]-1).getRemaining()){
							task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])+activity[3]);
							if (task.getOccupied_resource().get(activity[2])>task.getInitial_claim().get(activity[2])){
								statusArray[i]="aborted";
								testStatusArray[i]="aborted";
								runningNum-=1;
								task.setAborted(true);
								System.out.println("During cycle "+cycle+"-"+(cycle+1)+" of Banker's algorithms");
								System.out.print("\tTask "+task.getTask_number()+"'s request exceeds its claim; aborted; ");
								task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])-activity[3]);
								tempRemaining[activity[2]-1]+=task.getOccupied_resource().get(activity[2]);
								System.out.println(task.getOccupied_resource().get(activity[2])+" units available next cycle\n");
								continue;
							}
							taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])-activity[3]);
							if (!detectSafeRecur(task,activity)){
								statusArray[i]="blocked";
								testStatusArray[i]="blocked";
								blockList.add(i);
								runningNum-=1;
								if (verbose) System.out.println("Task "+task.getTask_number()+"'s request cannot be granted (not safe)");
								task.setWait_time(taskList.get(i).getWait_time()+1);
								continue;
							}
							taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])+activity[3]);
							resourceList.get(activity[2]-1).setRemaining(resourceList.get(activity[2]-1).getRemaining()-activity[3]);
							tempRemaining[activity[2]-1]-=activity[3];
							taskStatusArray[i]+=1;
							if (verbose) {
								System.out.print("Task "+task.getTask_number()+" completes its request (i.e. the request is granted)");
								System.out.println(" (resource "+(activity[2])+"; requested="+activity[3]+", remaining="+tempRemaining[activity[2]-1]+")");
								}
						}
						else{
							statusArray[i]="blocked";
							testStatusArray[i]="blocked";
							blockList.add(i);
							runningNum-=1;
							if (verbose) System.out.println("Task "+task.getTask_number()+"'s request cannot be granted");
						}
					}
					
					/*
					 * Release: release the resource to tempRemaining
					 * be ready to update it to resource object when the cycle is over
					 * If the task is terminated after release, set end time and print its termination message
					 */
					else if (activity[0]==2){
						task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])-activity[3]);
						tempRemaining[activity[2]-1]+=activity[3];
						taskStatusArray[i]+=1;
						if (verbose) {
							System.out.print("Task "+task.getTask_number()+" releases");
							System.out.print(" (resource "+(activity[2])+"; released="+activity[3]+", available next cycle="+tempRemaining[activity[2]-1]+")");
						}
						if (task.getActivities().get(taskStatusArray[i])[0]==4){
							taskList.get(i).setEnd_time(cycle+1);
							statusArray[i]="terminated";
							testStatusArray[i]="terminated";
							runningNum-=1;
							if (verbose) System.out.println(" and is finished (at "+task.getEnd_time()+").");
						}
						else{
							if (verbose) System.out.println();
						}
					}
					//Compute: set the total time of this compute and start the compute
					else if (activity[0]==3){
						computeArray[i]=0;
						originalComputeArray[i]=activity[2];
						statusArray[i]="compute";
						testStatusArray[i]="compute";
						taskStatusArray[i]+=1;
						runningNum-=1;
					}
					//Terminate: set the task status to terminate
					else{
						taskList.get(i).setEnd_time(cycle);
						statusArray[i]="terminated";
						testStatusArray[i]="terminated";
						runningNum-=1;
					}
				}
				//If the task is blocked, increase wait time by 1
				if (statusArray[i].equals("blocked")){
					task.setWait_time(taskList.get(i).getWait_time()+1);
				}
				/*
				 * If the task is computing,
				 * increase the computed time by one.
				 * If it equals the total time of this computation, the task is set to running
				 * If it terminates after this compute, set end time and print termination message
				 */
				if (statusArray[i].equals("compute")){
					computeArray[i]+=1;
					if (verbose) System.out.print("Task "+task.getTask_number()+" computes ("+computeArray[i]+" of "+originalComputeArray[i]+")");
					if (computeArray[i]==originalComputeArray[i]){
						statusArray[i]="running";
						testStatusArray[i]="running";
						runningNum+=1;
						if (task.getActivities().get(taskStatusArray[i])[0]==4){
							taskList.get(i).setEnd_time(cycle+1);
							statusArray[i]="terminated";
							testStatusArray[i]="terminated";
							if (verbose) System.out.println(" and is finished (at "+task.getEnd_time()+").");
							runningNum-=1;
						}
						else {
							if (verbose) System.out.println();
						}
					}
					else {
						if (verbose) System.out.println();
					}
				}
				if (statusArray[i].equals("waitOneCycle")) {
					statusArray[i]="running";
					testStatusArray[i]="running";
					runningNum+=1;
				}
			}
			//Update remaining resources to resource objects
			if (verbose) System.out.print("Available Resources: ");
			for (int j=0;j<resourceList.size();j++){
				if (verbose){
					if (j==0) System.out.println(tempRemaining[j]);
					else System.out.println(", "+tempRemaining[j]);
				}
				resourceList.get(j).setRemaining(tempRemaining[j]);
			}
			cycle+=1;
			if (verbose) System.out.println();
		}
	}
	
	/*After determine the safe state, we should restore the testStatusArray to before the test. 
	 * Because all the status change in the test will not really happen.
	 */
	private void restoreTestArray(){
		for (int i=0;i<testStatusArray.length;i++){
			testStatusArray[i]=statusArray[i];
		}
	}
	
	//This function first allocate the request to the task, and start recursively determine if it's a safe state
	private boolean detectSafeRecur(Task task, int[] activity){
		HashMap<Integer, Integer> tempRemainingMap=new HashMap<Integer, Integer>();
		for (Resource r:resourceList){
			tempRemainingMap.put(r.getResource_type(), r.getRemaining());
		}
		task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])+activity[3]); 
		tempRemainingMap.put(activity[2],tempRemainingMap.get(activity[2])-activity[3]);
		boolean safe=detectRecur(tempRemainingMap);
		task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])-activity[3]);
		restoreTestArray();
		return safe;
	}
	
	/*
	 * In each level, we find all the task that its initial claims can be fulfilled by remaining resources
	 * For each task, we allocate the resource and terminate the task and release all its occupied resources and 
	 * enter the next level and recursively do the same check.
	 * The process continues if we find a level when all the tasks are terminated, we return safe=true to the first level
	 * then we can determine that the state is safe.
	 */
	private boolean detectRecur(HashMap<Integer, Integer> tempRemainingMap){
		boolean canSatisfy;
		boolean terminate=true;
		for (Task t:taskList){
			if (testStatusArray[t.getTask_number()-1].equals("running")){
				terminate=false;
				break;
			}
		}
		if (terminate) return true;
		for (Task t:taskList){
			canSatisfy=true;
			if (!testStatusArray[t.getTask_number()-1].equals("running")) continue;
			for (Resource r:resourceList){
				if (t.getInitial_claim().get(r.getResource_type())-t.getOccupied_resource().get(r.getResource_type())>tempRemainingMap.get(r.getResource_type())){
					canSatisfy=false;
				}
			}
			if (canSatisfy){
				for (Resource r:resourceList){
					tempRemainingMap.put(r.getResource_type(), tempRemainingMap.get(r.getResource_type())+t.getOccupied_resource().get(r.getResource_type()));
				}
				testStatusArray[t.getTask_number()-1]="terminated";
				return detectRecur(tempRemainingMap);
			}
		}
		return false;
	}
}
