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

        GreedyEST_LRPTSolver glouton = new GreedyEST_LRPTSolver();
        Result s = glouton.solve(instance,deadline);
        //on s_local = meilleur solution pour l'itération
        Result s_local = s;
        int best = s.schedule.makespan();
        int ListeTaboo [][] = new int[instance.numJobs*instance.numMachines][instance.numJobs*instance.numMachines];
        //k permet de compter les itérations
        int k = 0;
        int maxIter=100;
        int dureeTabou=10;
        //tant que le nombre d'itération max n'est pas atteinte et que la deadline n'est pas atteinte
        while (k < maxIter && deadline - System.currentTimeMillis() > 1) {
            k++;
            //l'order qui correspond au meilleur schedule (s)
            ResourceOrder order = new ResourceOrder(s.schedule);
            //l'order qui correspond au meilleur schedule de l'itération (s_local)
            ResourceOrder order_local = new ResourceOrder(s_local.schedule);
            //la liste des Block du chemin critique
            List<DescentSolver.Block> blocksList = blocksOfCriticalPath(order_local);
            //variables pour stocker les meilleurs résultats locaux
            DescentSolver.Swap bestSwap = null;
            int best_local = -1;
            for (DescentSolver.Block block : blocksList) {
                //la liste des Swap pour le Block
                List<DescentSolver.Swap> swapList = neighbors(block);
                for (DescentSolver.Swap swap : swapList) {
                    //avant de tester le swap, on vérifie qu'il est autorisé
                    if (k > ListeTaboo[swap.t1][swap.t2]) {
                        //on copie l'ordre de s et on applique le swap
                        ResourceOrder copy = order_local.copy();
                        swap.applyOn(copy);
                        int makespan = copy.toSchedule().makespan();
                        //si le swap retourne un meilleur résultat que le résultat local on actualise s_local
                        if (best_local == -1 || makespan < best_local) {
                            bestSwap = swap;
                            best_local = makespan;
                            order_local = copy;
                            //si le swap est également meilleur que s, on actulise s
                            if (makespan < best) {
                                best = makespan;
                                order = copy;
                            }
                        }
                    }
                }
            }
            //si un swap est meilleur que la solution locale on l'ajoute à la structure
            if (bestSwap != null) {
                ListeTaboo[bestSwap.t1][bestSwap.t2] = k + dureeTabou;
            }
            //on actualise s et s_local
            s_local = new Result(order_local.instance, order_local.toSchedule(), Result.ExitCause.Blocked);
            s = new Result(order.instance, order.toSchedule(), Result.ExitCause.Blocked);
        }
        //en fonction de si maxIter a été atteint ou si la deadline a été atteinte
        //on ne retourne pas la même raison de sortie
        if (k == maxIter) return s;
        return new Result(s.instance, s.schedule, Result.ExitCause.Timeout);
    }

    /** Returns a list of all blocks of the critical path. */
    List<DescentSolver.Block> blocksOfCriticalPath(ResourceOrder order) {
        List<DescentSolver.Block> blocks = new ArrayList<>();

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
                        blocks.add(new DescentSolver.Block(machine,firstTask,lastTask));
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
    List<DescentSolver.Swap> neighbors(DescentSolver.Block block) {

        List<DescentSolver.Swap> swaps = new ArrayList<DescentSolver.Swap>();
        int diff = block.lastTask - block.firstTask;

        if (diff >= 2) {
            swaps.add(new DescentSolver.Swap(block.machine,block.firstTask,block.firstTask+1));
            swaps.add(new DescentSolver.Swap(block.machine,block.lastTask-1,block.lastTask));
        }
        else {
            swaps.add(new DescentSolver.Swap(block.machine,block.firstTask,block.lastTask));
        }

        return swaps;
    }

}
