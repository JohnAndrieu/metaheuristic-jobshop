package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;

public class GreedySRPTSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        ArrayList<Task> TasksToDo = new ArrayList<Task>();
        ArrayList<Task> TasksDone = new ArrayList<Task>();

        ResourceOrder rso = new ResourceOrder(instance);

        int JobDuration[] = new int [instance.numJobs];
        int JobDurationDone[] = new int [instance.numJobs];
        Arrays.fill(JobDurationDone,0);

        for(int job = 0 ; job < instance.numJobs ; job++) {
            Task t = new Task(job,0);
            TasksToDo.add(t);
        }

        for(int j = 0 ; j < instance.numJobs ; j++) {
            for(int t = 0 ; t < instance.numTasks ; t++) {
                JobDuration[j] += instance.duration(j,t);
            }
        }

        while(!TasksToDo.isEmpty()) {

            Task shortest = null;

            for(int i = 0 ; i < TasksToDo.size() ; i++) {

                /*
                Choisir une tâche dans cet ensemble et placer cette tâche sur la ressource qu’elle
                demande (à la première place libre dans la représentation par ordre de passage)
                 */

                Task current = TasksToDo.get(i);

                if (i == 0) {
                    shortest = current;
                } else {
                    int shortestRemainingDuration = JobDuration[shortest.job] - JobDurationDone[shortest.job];
                    int currentRemainingDuration = JobDuration[current.job] - JobDurationDone[current.job];

                    if (shortestRemainingDuration > currentRemainingDuration) {
                        shortest = current;
                    }
                }
            }

            int rsc = instance.machine(shortest);
            int job = shortest.job;
            int task = shortest.task;
            rso.addTask(rsc,job,task);

            /*
            Mettre à jour l’ensemble des tâches réalisables
            */

            TasksToDo.remove(shortest);
            TasksDone.add(shortest);
            JobDurationDone[shortest.job] += instance.duration(shortest);

            if(instance.numTasks-1 > task) {
                TasksToDo.add(new Task(job, task + 1));
            }
        }

        return new Result(instance, rso.toSchedule(), Result.ExitCause.Blocked);
    }
}
