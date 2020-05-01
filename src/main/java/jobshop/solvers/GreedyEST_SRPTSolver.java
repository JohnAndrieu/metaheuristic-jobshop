package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;

public class GreedyEST_SRPTSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        ArrayList<Task> TasksToDo = new ArrayList<Task>();
        ArrayList<Task> TasksDone = new ArrayList<Task>();
        ArrayList<Task> MinimalsTasks = new ArrayList<Task>();

        ResourceOrder rso = new ResourceOrder(instance);

        int JobDuration[] = new int [instance.numJobs];
        int JobDurationDone[] = new int [instance.numJobs];
        Arrays.fill(JobDurationDone,0);

        // for each task, its start time
        int[][] startTimes = new int[instance.numJobs][instance.numTasks];
        for(int i = 0 ; i < instance.numJobs ; i++) {
            for(int j = 0 ; j < instance.numTasks ; j++) {
                startTimes[i][j] = 0;
            }
        }

        // for each machine, earliest time at which the machine can be used
        int[] releaseTimeOfMachine = new int[instance.numMachines];
        Arrays.fill(releaseTimeOfMachine,0);

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
            MinimalsTasks.clear();
            int minStartTime = -1;

            for(int i = 0 ; i < TasksToDo.size() ; i++) {

                Task current = TasksToDo.get(i);

                if (minStartTime == -1) {
                    minStartTime = (current.task == 0) ? 0 : startTimes[current.job][current.task-1] + instance.duration(current.job, current.task-1);
                    minStartTime = Math.max(minStartTime, releaseTimeOfMachine[instance.machine(current)]);
                    MinimalsTasks.add(current);
                }
                else {

                    int estT = (current.task == 0) ? 0 : startTimes[current.job][current.task-1] + instance.duration(current.job, current.task-1);
                    estT = Math.max(estT, releaseTimeOfMachine[instance.machine(current.job,current.task)]);

                    if (estT < minStartTime) {
                        MinimalsTasks.clear();
                        MinimalsTasks.add(current);
                        minStartTime = estT;
                    }
                    else if (estT == minStartTime) {
                        MinimalsTasks.add(current);
                    }
                }
            }

            for(Task t : MinimalsTasks) {
                if(shortest == null){
                    shortest = t;
                }
                int shortestRemainingDuration = JobDuration[shortest.job] - JobDurationDone[shortest.job];
                int currentRemainingDuration = JobDuration[t.job] - JobDurationDone[t.job];

                if (shortestRemainingDuration > currentRemainingDuration) {
                    shortest = t;
                }
            }

            startTimes[shortest.job][shortest.task] = minStartTime;
            releaseTimeOfMachine[instance.machine(shortest)] = minStartTime + instance.duration(shortest);

            int rsc = instance.machine(shortest);
            int job = shortest.job;
            int task = shortest.task;
            rso.addTask(rsc,job,task);

            TasksToDo.remove(shortest);
            TasksDone.add(shortest);

            if(instance.numTasks-1 > task) {
                TasksToDo.add(new Task(job, task + 1));
            }
        }

        return new Result(instance, rso.toSchedule(), Result.ExitCause.Blocked);
    }
}

