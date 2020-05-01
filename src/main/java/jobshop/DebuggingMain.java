package jobshop;

import jobshop.encodings.JobNumbers;
import jobshop.encodings.ResourceOrder;
import jobshop.solvers.DescentSolver;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import jobshop.Solver.*;

class Block {
    /** machine on which the block is identified */
    final int machine;
    /** index of the first task of the block */
    final int firstTask;
    /** index of the last task of the block */
    final int lastTask;

    Block(int machine, int firstTask, int lastTask) {
        this.machine = machine;
        this.firstTask = firstTask;
        this.lastTask = lastTask;
    }
}

public class DebuggingMain {

    public static void main(String[] args) {
        /*try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            JobNumbers enc = new JobNumbers(instance);
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 1;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 0;
            enc.jobs[enc.nextToSet++] = 1;

            System.out.println("\nENCODING: " + enc);

            Schedule sched = enc.toSchedule();
            // TODO: make it print something meaningful
            // by implementing the toString() method
            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }*/

        try {
            // load the aaa1 instance
            Instance instance = Instance.fromFile(Paths.get("instances/aaa1"));

            // construit une solution dans la représentation par
            // numéro de jobs : [0 1 1 0 0 1]
            // Note : cette solution a aussi été vue dans les exercices (section 3.3)
            //        mais on commençait à compter à 1 ce qui donnait [1 2 2 1 1 2]
            ResourceOrder resOrder = new ResourceOrder(instance);

            System.out.println("\nNUM JOB: " + instance.numJobs);
            System.out.println("NUM TASK/JOB: " + instance.numTasks);
            System.out.println("NUM MACHINE: " + instance.numMachines);

            resOrder.addTask(0,0,0);
            resOrder.addTask(0,1,1);
            resOrder.addTask(1,0,1);
            resOrder.addTask(1,1,0);
            resOrder.addTask(2,0,2);
            resOrder.addTask(2,1,2);

            System.out.println("\nENCODING:\n " + resOrder);

            Schedule sched = resOrder.toSchedule();

            System.out.println("SCHEDULE: " + sched);
            System.out.println("VALID: " + sched.isValid());
            System.out.println("MAKESPAN: " + sched.makespan());


        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }

    }
}
