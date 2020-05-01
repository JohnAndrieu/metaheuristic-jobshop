package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

public class TabooSolver implements Solver {

    @Override
    public Result solve(Instance instance, long deadline) {

        Result best_s = new GreedyEST_LRPTSolver().solve(instance,deadline);
        Result local_s = best_s;

        int best_makespan = best_s.schedule.makespan();

        int tabou [][] = new int [instance.numJobs*instance.numMachines][instance.numJobs*instance.numMachines];

        int k = 0;
        int maxIter = 100;
        int tabouTime = 10;

        while(k < maxIter && deadline > 0) {

            k++;

            ResourceOrder current_order = new ResourceOrder(best_s.schedule);

            ResourceOrder current_local_order = new ResourceOrder(local_s.schedule);

            List<Utils.Block> blocksFromCriticalPath = Utils.blocksOfCriticalPath(current_local_order);

            Utils.Swap best_swap = null;

            int best_local_makespan = -1;

            for (Utils.Block block : blocksFromCriticalPath) {

                List<Utils.Swap> swapsFromBlock = Utils.neighbors(block);

                for (Utils.Swap swap : swapsFromBlock) {

                    if(k > tabou[swap.t1][swap.t2]) {

                        ResourceOrder neighbor = current_local_order.copy();

                        swap.applyOn(neighbor);

                        int current_makespan = neighbor.toSchedule().makespan();

                        if(best_local_makespan == -1 || current_makespan < best_local_makespan) {
                            best_swap = swap;
                            best_local_makespan = current_makespan;
                            current_local_order = neighbor;

                            if(current_makespan < best_makespan) {
                                best_makespan = current_makespan;
                                current_order = neighbor;
                            }

                        }
                    }
                }
            }

            if(best_swap != null) {
                tabou[best_swap.t1][best_swap.t2] = k + tabouTime;
            }

            local_s = new Result(current_local_order.instance, current_local_order.toSchedule(), Result.ExitCause.Blocked);
            best_s = new Result(current_order.instance, current_order.toSchedule(), Result.ExitCause.Blocked);

            deadline--;
        }
        if (k == maxIter) return best_s;
        return new Result(best_s.instance, best_s.schedule, Result.ExitCause.Timeout);
    }

}


