import java.io.*;
import java.util.*;

public class Main {
	/*This function gets input from text file and run both FIFO and Banker's algorithm*/
	public static void main(String[] args) throws FileNotFoundException{
		File file=new File("./src/"+args[args.length-1]);
		boolean verbose=false;
		if (args.length==2 && args[args.length-2].equals("--verbose")){
			verbose=true;
		}
		Scanner in=new Scanner(file);
		//Get the task number and use it to create task list (ArrayList of Task objects) for each algorithm
		int taskNum=in.nextInt();
		ArrayList<Task> taskList1=new ArrayList<Task>();
		ArrayList<Task> taskList2=new ArrayList<Task>();
		for (int i=0;i<taskNum;i++){
			taskList1.add(new Task(i+1));
			taskList2.add(new Task(i+1));
		}
		//Get resource number and use it to create an ArrayList of resources for each algorithm
		int resourceNum=in.nextInt();
		ArrayList<Resource> resourceList1=new ArrayList<Resource>();
		ArrayList<Resource> resourceList2=new ArrayList<Resource>();
		for (int j=0;j<resourceNum;j++){
			//Get the total number of resources
			Resource resource1=new Resource(j+1);
			int total=in.nextInt();
			resource1.setTotal(total);
			Resource resource2=new Resource(j+1);
			resource2.setTotal(total);
			resourceList1.add(resource1);
			resourceList2.add(resource2);
		}
		/* Load activities of different tasks and store respectively in corresponding tasks
		 * The activity is represented with an array of length 4
		 * The first element of activity shows the activity type 
		 * 0: initiate
		 * 1: request
		 * 2: release
		 * 3: compute
		 * 4: terminate
		  */
		while (in.hasNext()){
			String actName=in.next();
			int[] actArray=new int[4];
			if (actName.equals("initiate")) actArray[0]=0;
			else if (actName.equals("request")) actArray[0]=1;
			else if (actName.equals("release")) actArray[0]=2;
			else if (actName.equals("compute")) actArray[0]=3;
			if (actName.equals("terminate")) actArray[0]=4;
			actArray[1]=in.nextInt();
			actArray[2]=in.nextInt();
			actArray[3]=in.nextInt();
			taskList1.get(actArray[1]-1).getActivities().add(actArray);
			taskList2.get(actArray[1]-1).getActivities().add(actArray);
		}
		in.close();
		FIFO optimistic=new FIFO(taskList1,resourceList1);
		Banker banker=new Banker(taskList2, resourceList2);
		optimistic.run(verbose);
		banker.run(verbose);
		printResult(taskList1,taskList2);
	}
	
	/*This function is used to display the result of both FIFO and Banker's algorithm. The end time and wait time of every
	task are stored in task objects. The function get these information and used them to calculate the sum and wait ratio*/
	private static void printResult(ArrayList<Task> taskList1, ArrayList<Task> taskList2){
		System.out.println("\tFIFO\t\t\t\t\t\tBanker");
		int endSum=0;
		int waitSum=0;
		int endSum2=0;
		int waitSum2=0;
		for (Task t: taskList1){
			int taskNum=t.getTask_number();
			Task t2=taskList2.get(taskNum-1);
			if (t.isAborted()){
				System.out.print("Task "+taskNum+"\taborted\t\t\t\t\t");	
			}
			else{
				endSum+=t.getEnd_time();
				waitSum+=t.getWait_time();
				System.out.print("Task "+taskNum+"\t"+t.getEnd_time()+"\t"+t.getWait_time()+"\t");
				System.out.print(Math.round((float)t.getWait_time()/(float)t.getEnd_time()*100)+"%\t\t\t");
			}
			if (t2.isAborted()){
				System.out.println("Task "+taskNum+"\taborted");	
			}
			else{
				endSum2+=t2.getEnd_time();
				waitSum2+=t2.getWait_time();
				System.out.print("Task "+taskNum+"\t"+t2.getEnd_time()+"\t"+t2.getWait_time()+"\t");
				System.out.println(Math.round((float)t2.getWait_time()/(float)t2.getEnd_time()*100)+"%");
			}
		}
		System.out.print("total\t"+endSum+"\t"+waitSum+"\t");
		System.out.print(Math.round((float)waitSum/(float)endSum*100)+"%\t\t\t");
		System.out.print("total\t"+endSum2+"\t"+waitSum2+"\t");
		System.out.println(Math.round((float)waitSum2/(float)endSum2*100)+"%");		
	}
}
