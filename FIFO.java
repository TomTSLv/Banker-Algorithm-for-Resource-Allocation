import java.util.*;

public class FIFO {
	private ArrayList<Task> taskList;
	
	private ArrayList<Resource> resourceList;
	
	//blockList is used to store index of blocked task in taskList in order in order to check blocked tasks first in each cycle
	private ArrayList<Integer> blockList;
	
	/*Stores the index of the activity we are currently dealing with in the activity list for each task
	 * index of taskStatusArray corresponds with index of the task in the taskList
	 */
	int[] taskStatusArray;
	
	//Stores the status of each task
	String[] statusArray;
	
	//Task which has an index<abortNum is either aborted or terminated. We will not deal with them after.
	int abortNum=0;
	
	public FIFO(ArrayList<Task> taskList, ArrayList<Resource> resourceList){
		this.taskList=taskList;
		this.resourceList=resourceList;
		this.blockList=new ArrayList<Integer>();
		this.taskStatusArray=new int[this.taskList.size()];
		this.statusArray=new String[this.taskList.size()];
	}
	
	//Run the FIFO algorithm to allocate resources
	public void run(boolean verbose){
		int cycle=0;
		//All tasks are running at the very beginning
		for (int i=0;i<taskList.size();i++){
			statusArray[i]="running";
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
			//When all tasks are either terminated or aborted, the FIFO algorithm is done
			boolean terminate=true;
			for (int i=abortNum;i<taskList.size();i++){
				if ((!statusArray[i].equals("terminated")) && (!taskList.get(i).isAborted())) terminate=false;
			}
			if (terminate) break;
			
			//Update initial remaining resources for this cycle
			if (verbose) System.out.println("During "+cycle+"-"+(cycle+1));
			for (int j=0;j<resourceList.size();j++){
				tempRemaining[j]=resourceList.get(j).getRemaining();
			}
			
			//First check blocked task in the order of when it's blocked
			boolean first=true;
			for (int j=0;j<blockList.size();j++){
				if (verbose) {
					if (first) System.out.println("First check blocked tasks: ");
				}
				first=false;
				int i=blockList.get(j);
				/*If the task isn't aborted and the remaining resource is enough for allocation, 
				 * allocate the resource. But the task cannot move forward this cycle it need to wait for one more cycle.
				 */
				if (statusArray[i].equals("blocked") && (!taskList.get(i).isAborted())){
					activity=taskList.get(i).getActivities().get(taskStatusArray[i]);
					if (activity[3]<=resourceList.get(activity[2]-1).getRemaining()){
						taskList.get(i).getOccupied_resource().put(activity[2], taskList.get(i).getOccupied_resource().get(activity[2])+activity[3]);
						resourceList.get(activity[2]-1).setRemaining(resourceList.get(activity[2]-1).getRemaining()-activity[3]);
						tempRemaining[activity[2]-1]-=activity[3];
						taskStatusArray[i]+=1;
						if (verbose) System.out.println("grant task "+ (i+1)+"'s request, ");
						statusArray[i]=("waitOneCycle");
						blockList.remove(j);
						j--;
					}
					else{
						if (verbose) System.out.println("task "+ (i+1)+" is still blocked");
					}
				}
			}
			
			//Deal with activities of unaborted resources
			for (int i=abortNum;i<taskList.size();i++){
				if (taskList.get(i).isAborted()) continue;
				Task task=taskList.get(i);
				//If the task is running, deal with the next activity
				if (statusArray[i].equals("running")){
					activity=task.getActivities().get(taskStatusArray[i]);
					//Initialization: set initial claim, set occupied resource to 0 at first
					if (activity[0]==0){
						task.getInitial_claim().put(activity[2], activity[3]);
						task.getOccupied_resource().put(activity[2], 0);
						taskStatusArray[i]+=1;
						if (verbose) System.out.println("Task "+task.getTask_number()+" does initialization.");
					}
					/*
					 * Request: test whether the remaining resource is enough for allocation
					 * If yes, allocate the resource and move to the next activity
					 * If not, block the task and add it to blockList
					 */
					else if (activity[0]==1){
						if (activity[3]<=resourceList.get(activity[2]-1).getRemaining()){
							task.getOccupied_resource().put(activity[2], task.getOccupied_resource().get(activity[2])+activity[3]);
							resourceList.get(activity[2]-1).setRemaining(resourceList.get(activity[2]-1).getRemaining()-activity[3]);
							tempRemaining[activity[2]-1]-=activity[3];
							taskStatusArray[i]+=1;
							if (verbose) {
								System.out.print("Task "+task.getTask_number()+" completes its request (i.e. the request is granted).");
								System.out.println(" (resource["+(activity[2]-1)+"]; requested="+activity[3]+", remaining="+tempRemaining[activity[2]-1]+")");
							}
						}
						else{
							statusArray[i]="blocked";
							blockList.add(i);
							if (verbose) System.out.println("Task "+task.getTask_number()+"'s request cannot be granted.");
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
							System.out.print(" (resource["+(activity[2]-1)+"]; released="+activity[3]+", available next cycle="+tempRemaining[activity[2]-1]+")");
						}
						if (task.getActivities().get(taskStatusArray[i])[0]==4){
							task.setEnd_time(cycle+1);
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
						taskStatusArray[i]+=1;
					}
					//Terminate: set the task status to terminate
					else{
						taskList.get(i).setEnd_time(cycle);
						statusArray[i]="terminated";
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
						if (task.getActivities().get(taskStatusArray[i])[0]==4){
							task.setEnd_time(cycle+1);
							if (verbose) System.out.println(" and is finished (at "+task.getEnd_time()+").");
						}
						else{
							if (verbose) System.out.println();
						}
					}
					else{
						if (verbose) System.out.println();
					}
				}
				if (statusArray[i].equals("waitOneCycle")) statusArray[i]="running";
			}
			//Update remaining resources to resource objects
			for (int j=0;j<resourceList.size();j++){
				resourceList.get(j).setRemaining(tempRemaining[j]);
			}
			resolveDeadlock(verbose,cycle);
			if (verbose) {
				System.out.print("Available Resources: ");
				for (int j=0;j<resourceList.size();j++){
					if (j==0) System.out.println(resourceList.get(j).getRemaining());
					else System.out.println(", "+resourceList.get(j).getRemaining());
				}
			}
			cycle+=1;
			if (verbose) {
				if (cycle!=0) System.out.println();
			}
		}
	}
	
	/*
	 * This function is used to resolve potential deadlock in each cycle.
	 * First we determine if there is a deadlock happening now.
	 * If there is, we aborted the first unaborted or unterminated task,
	 * release all its occupied resources and give them to corresponding resource object
	 * after release if there is a blocked task able to run, run it and the deadlock is solved
	 * If not, we determine the deadlock and follow the same procedure again until one blocked task is able to run.
	 */
	private void resolveDeadlock(boolean verbose, int cycle){
		boolean deadLockFirst=false;
		ArrayList<Integer> abortList=new ArrayList<Integer>();
		while (abortNum!=taskList.size()){
			if (detectDeadlock()){
				if (statusArray[abortNum].equals("terminated")) {
					abortNum+=1;
					continue;
				}
				deadLockFirst=true;
				Task task=taskList.get(abortNum);
				task.setAborted(true);
				abortList.add(task.getTask_number());
				blockList.remove(Integer.valueOf(abortNum));
				int[] activity=task.getActivities().get(taskStatusArray[abortNum]);
				resourceList.get(activity[2]-1).setRemaining(resourceList.get(activity[2]-1).getRemaining()+task.getOccupied_resource().get(activity[2]));
				task.getOccupied_resource().put(activity[2], 0);
				abortNum+=1;
				for (int i=abortNum;i<taskList.size();i++){
					if (taskList.get(i).isAborted()) continue;
					if (resourceList.get(activity[2]-1).getRemaining()>=activity[3]){
						statusArray[i]="running";
						blockList.remove(Integer.valueOf(i));
					}
				}
			}
			else{
				break;
			}
		}
		if (verbose && deadLockFirst){
			System.out.print("\nAccording to the spec, tasks ");
			for (int i=0;i<abortList.size();i++){
				if (i==0) System.out.print(abortList.get(i));
				else System.out.print(", "+abortList.get(i));
			}
			System.out.println(" are aborted now and their resources are available next cycle ("+(cycle+1)+"-"+(cycle+2)+").");
		}
	}
	
	/*
	 * Determine if there is a deadlock happening:
	 * determine despite the aborted and terminated tasks, if all the other tasks are blocked
	 * If yes, deadlocked
	 * If no, not deadlocked
	 */
	private boolean detectDeadlock(){
		if (blockList.size()==1) return false;
		for (int i=abortNum;i<taskList.size();i++){
			if (taskList.get(i).isAborted() || statusArray[i].equals("terminated")) continue;
			if (!statusArray[i].equals("blocked")) return false;
		}
		return true;
	}
}
