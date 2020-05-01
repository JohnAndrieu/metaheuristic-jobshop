package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.HashSet;

public class GreedyLPTSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        HashSet<Task> TasksToDo = new HashSet<Task>();
        ResourceOrder rso = new ResourceOrder(instance);

        for(int job = 0 ; job < instance.numJobs ; job++) {
            Task t = new Task(job,0);
            TasksToDo.add(t);
        }

        while(!TasksToDo.isEmpty()) {
            Task shortest = null;

            for(Task t : TasksToDo) {
                if(shortest == null){
                    shortest=t;
                }
                if (instance.duration(t) > instance.duration(shortest)) {
                    shortest=t;
                }
            }

            rso.addTask(instance.machine(shortest),shortest.job,shortest.task);
            TasksToDo.remove(shortest);

            if(instance.numTasks-1 > shortest.task) {
                TasksToDo.add(new Task(shortest.job, shortest.task + 1));
            }
        }

        return new Result(instance, rso.toSchedule(), Result.ExitCause.Blocked);
    }
}

