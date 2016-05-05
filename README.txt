#################################
#                               #
# OS Lab 3 Running Instructions # 
#                               #
#################################

Name: Tianshu Lyu
NetID: tl1443

This project runs both the FIFO and Banker's algorithm for resource allocation. 
FIFO just allocates the resource in order of requests. When there is a deadlock when all the unterminated and unaborted resources are blocked, we abort tasks in the order of task number until the remaining resource can fulfill the task's request.
Banker's algorithm, however, tests if it's a safe state after allocation in each request. Safe state is determined when there is a way where all the resources can terminate after having all the initially claimed resources (and release them after termination, of course).

The following is the instruction for compiling and running the program:

1. Download the OSLab3 directory from access.cims.nyu.edu or from NYU Classes.
2. Open the terminal and use cd command to enter the directory which contains all java files. (e.g.: cd /Desktop/OSLab3)
3. Compile the java project with command: javac Main.java
4. Run the program with command:
if you want the simple output: 
java Main input-01.txt 

if you want the detailed result with process status:
java Main --verbose input-01.txt

(input-01.txt can also be input-02.txt, input-03.txt,... to input-13.txt)

5. Then the program will print out the proper result:
if it's a simple output:
The program displays just the statistical result of FIFO and Banker's algorithm, i.e. the wait time, end time and the portion of wait time in end time for every task and the sum of them.

if it's a detailed result (with --verbose):
The program will first display the detailed cycle by cycle results, first FIFO and then Banker's.
Following that is the statistical results for two algorithms.