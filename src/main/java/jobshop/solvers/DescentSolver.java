package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;

import java.util.List;


public class DescentSolver implements Solver {
    @Override
    public Result solve(Instance instance, long deadline) {

        Result best_s = new GreedyEST_LRPTSolver().solve(instance,deadline);
        int best_makespan = best_s.schedule.makespan();

        boolean get_better = true;

        while(get_better && deadline > 0) {

            get_better = false;

            ResourceOrder current_order = new ResourceOrder(best_s.schedule);

            List<Utils.Block> blocksFromCriticalPath = Utils.blocksOfCriticalPath(current_order);

            for (Utils.Block block : blocksFromCriticalPath) {

                List<Utils.Swap> swapsFromBlock = Utils.neighbors(block);

                for (Utils.Swap swap : swapsFromBlock) {

                    ResourceOrder neighbor = current_order.copy();

                    swap.applyOn(neighbor);

                    int current_makespan = neighbor.toSchedule().makespan();

                        if (current_makespan < best_makespan) {
                            get_better = true;
                            best_makespan = current_makespan;
                            current_order = neighbor;
                        }
                }
            }

            if (get_better) {
                best_s = new Result(current_order.instance, current_order.toSchedule(), Result.ExitCause.Blocked);
            } else {
                return best_s;
            }
            deadline--;
        }
        return new Result(best_s.instance, best_s.schedule, Result.ExitCause.Timeout);
    }
}
