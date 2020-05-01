package jobshop.solvers;

import jobshop.Instance;
import jobshop.Result;
import jobshop.Schedule;
import jobshop.Solver;
import jobshop.encodings.ResourceOrder;
import jobshop.encodings.Task;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DescentSolver implements Solver {

    /** A block represents a subsequence of the critical path such that all tasks in it execute on the same machine.
     * This class identifies a block in a ResourceOrder representation.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The block with : machine = 1, firstTask= 0 and lastTask = 1
     * Represent the task sequence : [(0,2) (2,1)]
     *
     * */
    static class Block {
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

    /**
     * Represents a swap of two tasks on the same machine in a ResourceOrder encoding.
     *
     * Consider the solution in ResourceOrder representation
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (0,2) (2,1) (1,1)
     * machine 2 : ...
     *
     * The swam with : machine = 1, t1 = 0 and t2 = 1
     * Represent inversion of the two tasks : (0,2) and (2,1)
     * Applying this swap on the above resource order should result in the following one :
     * machine 0 : (0,1) (1,2) (2,2)
     * machine 1 : (2,1) (0,2) (1,1)
     * machine 2 : ...
     */
    static class Swap {
        // machine on which to perform the swap
        final int machine;
        // index of one task to be swapped
        final int t1;
        // index of the other task to be swapped
        final int t2;

        Swap(int machine, int t1, int t2) {
            this.machine = machine;
            this.t1 = t1;
            this.t2 = t2;
        }

        /** Apply this swap on the given resource order, transforming it into a new solution. */
        public void applyOn(ResourceOrder order) {
            Task temp = order.tasksByMachine[this.machine][this.t1];
            order.tasksByMachine[this.machine][this.t1] = order.tasksByMachine[this.machine][this.t2];
            order.tasksByMachine[this.machine][this.t2] = temp;
        }
    }

    @Override
    public Result solve(Instance instance, long deadline) {
        List<Swap> swapsFromBlock = new ArrayList<>();
        List<Block> blocksFromCriticalPath;


        GreedyEST_LRPTSolver greedy = new GreedyEST_LRPTSolver();
        Result resInit = greedy.solve(instance,deadline);
        ResourceOrder initOrder = new ResourceOrder(resInit.schedule);

        boolean get_better = true;
        boolean isInit = false;

        ResourceOrder best_order = initOrder.copy();
        ResourceOrder best_neighbor = null;

        while(get_better && deadline > 0) {
            isInit = false;
            get_better = false;
            blocksFromCriticalPath = blocksOfCriticalPath(best_order);
            for (Block block : blocksFromCriticalPath) {
                swapsFromBlock = neighbors(block);
                for (Swap swap : swapsFromBlock) {
                    ResourceOrder neighbor = best_order.copy();
                    swap.applyOn(neighbor);
                    if (!isInit) {
                        best_neighbor = neighbor.copy();
                        isInit = true;
                    } else {
                        Schedule sch = neighbor.toSchedule();
                        Schedule schBest = best_neighbor.toSchedule();
                        if (sch != null && schBest != null) {
                            if (neighbor.toSchedule().makespan() < best_neighbor.toSchedule().makespan()) {
                                best_neighbor = neighbor.copy();
                            }
                        }
                    }
                }
            }

            if(best_neighbor != null) {
                if (best_neighbor.toSchedule().makespan() < best_order.toSchedule().makespan()) {
                    get_better = true;
                    best_order = best_neighbor.copy();
                }
            }
            deadline = deadline-1;
        }
        return new Result(instance, best_order.toSchedule(), Result.ExitCause.Blocked);
    }

    /** Returns a list of all blocks of the critical path. */
    List<Block> blocksOfCriticalPath(ResourceOrder order) {
       List<Block> blocks = new ArrayList<>();

        List<Task> tasksOfCriticalPath = order.toSchedule().criticalPath();

        int firstTask = 0;
        int lastTask = 0;

        boolean isInit = false;

        int machine = 0;

        for(Task t : tasksOfCriticalPath) {
            if(!isInit) {
                machine = order.instance.machine(t);
                firstTask = Arrays.asList(order.tasksByMachine[machine]).indexOf(t);
                lastTask = firstTask;
                isInit = true;
            } else {
                if (machine == order.instance.machine(t)) {
                    lastTask++;
                }
                else {
                    if(firstTask != lastTask) {
                        blocks.add(new Block(machine,firstTask,lastTask));
                    }
                    machine = order.instance.machine(t);
                    firstTask = Arrays.asList(order.tasksByMachine[machine]).indexOf(t);
                    lastTask = firstTask;
                }
            }
        }

        return blocks;

    }

    /** For a given block, return the possible swaps for the Nowicki and Smutnicki neighborhood */
    List<Swap> neighbors(Block block) {

        List<Swap> swaps = new ArrayList<Swap>();
        int diff = block.lastTask - block.firstTask;

        if (diff >= 2) {
            swaps.add(new Swap(block.machine,block.firstTask,block.firstTask+1));
            swaps.add(new Swap(block.machine,block.lastTask-1,block.lastTask));
        }
        else {
            swaps.add(new Swap(block.machine,block.firstTask,block.lastTask));
        }

        return swaps;
    }

}
