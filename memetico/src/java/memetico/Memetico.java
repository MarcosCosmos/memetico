package memetico;

import memetico.lkh.LocalSearchLKH;
import memetico.logging.IPCLogger;
import memetico.logging.NullPCLogger;
import memetico.logging.MemeticoSnapshot;
import memetico.util.LocalSearchOpName;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.util.log.ILogger;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;
import memetico.util.ProblemConfiguration;
import memetico.util.ProblemInstance;

import java.text.*;

import java.io.IOException;
import java.util.Vector;

public class Memetico {

    static DecimalFormat prec = new DecimalFormat("#.##");

    Population memePop;
    CrossoverOperators refCrossover = null;
    LocalSearchOperators refLocalSearch = null;
    ConstructionAlgorithms refConstr = null;
    MutationOperators refMut = null;
    DiCycleRestartOperators refRestart = null;
//    static int numReplications = 30;

    private IPCLogger logger;
    private ProblemInstance problem;
    private static double AverTotalTime = 0, AverPopInit = 0,
            AverSolOpt = 0, AverGen = 0, AverQuality = 0;
    private static double initialSolution = 0;




    /* -------------------------------- Construtor -----------------------------*/

    /**
     * @param structSol: struct of the Solution
     * @param structPop: struct of the Population
     * @return Kmax = max value to be add to each distance in the lower diagonal.
     */
    public Memetico(IPCLogger logger, ValidityFlag.ReadOnly continuePermission, ProblemInstance problem, String structSol, String structPop, String ConstAlg, int TamPop, int TxMut, String BuscaLocal, String OPCrossover, String OPRestart, String OPMutation, boolean includeLKH, long MaxTime, long MaxGenNum, long numReplications/*, DataOutputStream fileOut, DataOutputStream compact_fileOut*/) throws Exception {
        //todo: compute the memetico Instance here instead of passing it as a parameter

        this.problem = problem;
        TSPLibInstance tspLibInstance = problem.getTspLibInstance();
        Instance inst;

        //todo: allow for more problem types??
        switch (tspLibInstance.getDataType()) {
            case ATSP:
            default:
                //atsp should be safe (ish) even if it is in fact tsp
                inst = new ATSPInstance();
        }

        //give the memeticoInstance the required data
        inst.setDimension(tspLibInstance.getDimension());
        {
            DistanceTable distanceTable = tspLibInstance.getDistanceTable();
            double[][] memeticoMat = ((GraphInstance) inst).getMatDist();
            for (int i = 0; i < inst.getDimension(); ++i) {
                for (int k = 0; k < inst.getDimension(); ++k) {
                    memeticoMat[i][k] = distanceTable.getDistanceBetween(i, k);
                }
            }
        }

        this.logger = logger;
        int GenNum = 0, i;
//        double TotalTime = 0, bestTime = 0, auxTime, recombineTime;
//        double Aver_time = 0, Aver_Gen = 0,
//                time_vmp, time_init, Quality = 0;
        int cont_OptimalSol = 0, count;
        PocCurAgent pocCurPop[];
//   FileOutputStream dataOut = new FileOutputStream("debug.txt");
//   DataOutputStream afileOut = new DataOutputStream (dataOut);


        // we set up a global upper bound, this should be done differently...
        double best_aux = Double.MAX_VALUE;

//   Agent refAgent = null;

        //System.out.println('\n' + "Opening file " + name);
        for (count = 0; count < numReplications; count++) {
//            recombineTime = time_vmp = time_init = 0;
            GenNum = 0;

//      refAgent 		= selectAgentStruct(refAgent);

            memePop = new Population(inst, TamPop, Agent.POKET_CURRENT, SolutionStructure.DICYCLE_TYPE);
            pocCurPop = (PocCurAgent[]) memePop.pop;

            refConstr = selectConstructionAlgorithm(refConstr, ConstAlg, inst);
            refMut = selectMutationOperator(refMut, OPMutation);
            refRestart = selectRestartOperator(refRestart, OPRestart, refConstr);
            refCrossover = selectCrossoverOperator(refCrossover, OPCrossover);
            refLocalSearch = selectLocalSearchOperator(refLocalSearch, BuscaLocal, inst);
            LocalSearchLKH lkh = new LocalSearchLKH(problem.getConfiguration().problemFile);


//            TotalTime = System.currentTimeMillis();
            logger.reset();
            // we initialize the population of agents
            // with a method based on the nearest neighbour heuristic for the TSP
            NNInicializePop(refConstr, 0, (int) (Math.random() * ((GraphInstance) inst).dimension),
                    (int) (Math.random() * ((GraphInstance) inst).dimension), inst);

            // and we evalutate all the agents and return the best
            memePop.evaluatePop(inst);
//            /*log the initial generation too*/
//            logger.log(problem.getName(), memePop, GenNum);

            initialSolution += memePop.bestAgent.bestCost;
//      //System.out.println("Otima: " +OptimalSol);
            //System.out.println("melhor: " + memePop.bestAgent.bestCost);
            //System.out.println("InitPop Time " + (System.currentTimeMillis() - TotalTime) / 1000);

            refMut.mutationRate = TxMut;


            /* Here we start the Generations loop */
/*          String str = new String();
        for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].current.cost; str += " ";}
        //System.out.println("Currents: "+str);
        str = "";
          for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].pocket.cost; str += " ";}
          //System.out.println("Pockets: "+str);*/


            SolutionStructure lastRootCur = null;

            memePop.updateAgents(inst);

            while (GenNum < MaxGenNum)
            // the stoping criteria used are two in this case.
            // This should be improved in a future version.
            {
                memePop.agentPropagation();


                if (memePop.bestSolution.cost < best_aux) {

                } else memePop.newBestSol++;

//                if(includeLKH) {
//                    //if the current of the root update, try improving it with lkh
//                    if (lastRootCur == null) {
//                        lastRootCur = pocCurPop[0].current;
//                    } else if (lastRootCur != pocCurPop[0].current) {
//                        //run LKH on the root pocket
//                        SolutionStructure tmp = pocCurPop[0].current.deepCopy();
//                        lkh.runLocalSearch(tmp, inst);
//                        tmp.calculateCost(inst);
//                        pocCurPop[0].insertSolutionStructure(tmp);
//                        pocCurPop[0].updateAgent(inst); //note that there is a redundant call to calculateCost since it was already run above/for the pocket still in the agent at the beginning of the call
//                        if (tmp.cost < memePop.bestSolution.cost) {
//                            memePop.bestSolution = tmp.deepCopy();
//                            //note this could be the old pocket but we don't to apply it to that pocket anyway (for now?)
//                        }
//                        lastRootCur = pocCurPop[0].current;
//                    }
//                }




                best_aux = memePop.bestSolution.cost;

//                double originalCost = ((PocCurAgent) memePop.pop[0]).pocket.cost;
//                double newCost = ((PocCurAgent)memePop.pop[0]).pocket.calculateCost(inst);
//                assert newCost == originalCost;

                // if (memePop.bestAgent.bestCost <= OptimalSol)  break;
                if (memePop.bestSolution.cost <= problem.getTargetCost() || !continuePermission.isValid()) {
                    //prevent a double log; but force the log if this is the end of the run
                    logger.log(problem.getName(), memePop, GenNum);
                    break;
                } else {
                    /* This seems to be the correct point at which to log since it's above the break? */
                    logger.tryLog(problem.getName(), memePop, GenNum);
                }

//	  memePop.orderChildren();

                recombinePocketsWithCurrents(memePop, refCrossover, inst, refLocalSearch);

                for (i = 0; i < memePop.popSize; i++) refMut.runMutation(pocCurPop[i].current);
                // if the incumbent hasn't been altered in 30 generations
                // RestartPablo
                if (pocCurPop[0].noChangeCounter % 30 == 0) {
                    int aux_rand = (int) (Math.random() * 3 + 1);
                    //System.out.println("aux_rand: " + aux_rand);
                    NNInicializePop(refConstr, aux_rand, (int) (Math.random() * ((GraphInstance) inst).dimension),
                            (int) (Math.random() * ((GraphInstance) inst).dimension), inst);
//                    NNInicializePop(refConstr, 0, (int) (Math.random() * ((GraphInstance) inst).dimension),
//                            (int) (Math.random() * ((GraphInstance) inst).dimension), inst, false);



                    memePop.evaluatePop(inst);
                    memePop.updateAgents(inst);
                    memePop.agentPropagation();

//                    System.out.println("now at Restart... incumbent cost is:" + pocCurPop[0].pocket.cost +
//                            " GenNum: " + GenNum + " Tempo: " +
//                            bestTime);

                    int mutationRateAux = TxMut;
                    TxMut = 100; // increase to high mutation rate

                    // mutate and optimize the currents
                    for (i = 0; i < memePop.popSize; i++) {
                        refMut.runMutation(pocCurPop[i].current);
                        pocCurPop[i].current.calculateCost(inst);
                        refLocalSearch.runLocalSearch(pocCurPop[i].current, inst);
                    }
                    // and also mutate and optimize all the pockets except the best pocket
                    for (i = 1; i < memePop.popSize; i++) {
                        refMut.runMutation(pocCurPop[i].pocket);
                        pocCurPop[i].pocket.calculateCost(inst);
                        refLocalSearch.runLocalSearch(pocCurPop[i].pocket, inst);
                    }
                    TxMut = mutationRateAux; // return the mutation to its previous value

                    memePop.evaluatePop(inst);
                    memePop.updateAgents(inst);
                    memePop.agentPropagation();
//                    String str = new String();
           /* for(i=0; i < 4; i++) {str += pocCurPop[i].current.cost; str += " ";}
           System.out.println("Currents: "+str);
           str = ""; */
//                    for (i = 0; i < 4; i++) {
//                        str += pocCurPop[i].pocket.cost;
//                        str += " ";
//                    }
                    //System.out.println("Pockets: " + str);

	   /* System.out.println("getting out of Restart... incumbent cost is:" + pocCurPop[0].pocket.cost +
                              " GenNum: " +GenNum + " Tempo: " +
                              bestTime);*/
                }// end of RestartPablo

//	    for(i=0; i < memePop.popSize; i++)  refLocalSearch.runLocalSearch(pocCurPop[i].current, inst);


/*        if(memePop.bestAgent.bestCost == 38707){
          String str = new String();
//        for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].current.cost; str += " ";}
//        System.out.println("Currents: "+str);
//        str = "";
          for(i=0; i < memePop.popSize; i++) {str += pocCurPop[i].pocket.cost; str += " ";}
          System.out.println("Pockets: "+str);
           ((DiCycle)((PocCurAgent)memePop.bestAgent).pocket).printDiCycle();
        }*/

                memePop.updateAgents(inst);

                if(includeLKH) {
                    //apply LKH to all currents
                    for (PocCurAgent agent : pocCurPop) {
                        SolutionStructure tmp = agent.current.deepCopy();
                        lkh.runLocalSearch(tmp, inst);
                        tmp.calculateCost(inst);
                        agent.insertSolutionStructure(tmp);
                        agent.updateAgent(inst); //note that there is a redundant call to calculateCost since it was already run above/for the pocket still in the agent at the beginning of the call
                    }
                }

                //may as well update the generation at the end of the loop, so that gen 0 reflects the state of the initial population reordered etc but before local search/recombination
                GenNum++;

            } // end of while, exiting the generations loop.

//            TotalTime = (System.currentTimeMillis() - TotalTime);

//      if (count==0)                                 //?
//         pocCurPop[0].pocket.saveInOptTour(name);
//            Aver_time += TotalTime / 1000;
//            Aver_Gen += GenNum;

//            if (problem.getTargetCost() == memePop.bestAgent.bestCost)
//                cont_OptimalSol++;

//            Quality += memePop.bestAgent.bestCost;
//
//            try {
//                fileOut.writeBytes(String.valueOf(name + '\t'));                /*nome do arquivo*/
//                fileOut.writeBytes(String.valueOf(prec.format((double) (TotalTime / 1000))) + '\t');    /*tempo total da execucao*/
//                fileOut.writeBytes(String.valueOf((long) memePop.bestAgent.bestCost) + '\t');    /*melhor solucao encontrada*/
//                fileOut.writeBytes(String.valueOf(prec.format((double) bestTime)) + '\t');            /*ultima atualizacao da melhor solucao*/
//                fileOut.writeBytes(String.valueOf((int) GenNum) + '\n');            /*numero total de solucoes*/
//            } catch (IOException e) {
//                throw new Exception("File not properly opened" + e.toString());
//            }
            //System.out.println("Tempo total de execucao: " + (TotalTime / 1000) + '\t');        /*Tempo total de execucao*/
            //System.out.println("Melhor solucao encontrada: " + memePop.bestAgent.bestCost + '\t');    /*Melhor solucao encontrada*/
            //System.out.println("Numero total de Geracoes: " + GenNum + '\n');            /*Numero total de Geracoes*/
        }//for

//   afileOut.close();

//        initialSolution = initialSolution / numReplications;
//        Quality = Quality / numReplications;
//        Quality = (100 * (Quality - problem.getTargetCost()) / problem.getTargetCost());
//        AverQuality += Quality;

//        try {
//            compact_fileOut.writeBytes(name + '\t');                                    /*arquivo*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (initialSolution))) + '\t');                /*solucao inicial*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (100 * (initialSolution - OptimalSol) / OptimalSol))) + '\t');    /*qualidade de solucao inicial*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Quality))) + '\t');                    /*qualidade da solucao final*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Aver_time / numReplications))) + '\t');            /*tempo total medio*/
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (Aver_Gen / numReplications))) + '\t');            /*numero de geracoes*/
//            compact_fileOut.writeBytes(String.valueOf((int) (cont_OptimalSol)) + " (" + String.valueOf((int) (numReplications)) + ")" + '\n');/*numero de solucoes otimas*/
//        } catch (IOException e) {
//            throw new Exception("File not properly opened" + e.toString());
//        }

//        AverTotalTime += (Aver_time / numReplications);
//        AverPopInit += initialSolution;
//        AverSolOpt += cont_OptimalSol;
//        AverGen += (Aver_Gen / numReplications);
    }


    /* ------------------------------------ MAIN ------------------------------------*/
    public static void main(IPCLogger logger, String args[]) {
        long MaxTime = 100, MaxGenNum;
        int PopSize = 13, mutationRate = 5, count;
        int numReplications = 30;
        Instance inst = null;
        Vector matRT[];
        Reduction reduction = null;
        // counter to check the number of instances, 27 in the assymetric case

        // names of the instances of the TSP which are going to be used for the
        // experiments. We are assuming they are located in the same directory.
        // A GUI would be needed here.
        String MetodoConstrutivo = "Nearest Neighbour";
        String BuscaLocal = "Recursive Arc Insertion";//Recursive Arc Insertion";
        String SingleorDouble = "Double";
        String OPCrossover = "Strategic Arc Crossover - SAX",
                OPReStart = "Insertion",
                OPMutacao = "MutationInsertion";
        String structSol = "DiCycle";
        String structPop = "Ternary Tree";
//        String Names[] = {
//                "br17.atsp", "ftv33.atsp", "ftv35.atsp", "ftv38.atsp", "p43.atsp", "ftv44.atsp",
//                "ftv47.atsp", "ry48p.atsp", "ft53.atsp", "ftv55.atsp", "ftv64.atsp", "ft70.atsp",
//                "ftv70.atsp", "ftv90.atsp", "kro124p.atsp", "ftv100.atsp", "ftv110.atsp", "ftv120.atsp",
//                "ftv130.atsp", "ftv140.atsp", "ftv150.atsp", "ftv160.atsp", "ftv170.atsp",
//                "rbg323.atsp", "rbg358.atsp", "rbg403.atsp", "rbg443.atsp",
//                "tsp_tree64.txt", "tsp_tree128.txt", "tsp_tree8.txt", "tsp_tree16.txt", "tsp_tree20.txt", "tsp_tree30.txt", "ulysses16.atsp",
//                "alb1000.hcp", "alb2000.hcp", "alb3000.hcp", "alb3000a.hcp", "alb3000b.hcp",
//                "alb3000c.hcp", "alb3000d.hcp", "alb3000e.hcp", "alb4000.hcp", "alb5000.hcp",
//
//                "ulysses16.tsp", "ulysses22.tsp", "gr24.tsp", "fri26.tsp", "bayg29.tsp",
//                "bays29.tsp", "att48.tsp", "gr48.tsp", "eil51.tsp", "berlin52.tsp",
//                "st70.tsp", "eil76.tsp", "pr76.tsp", "gr96.tsp", "kroa100.tsp",
//                "kroc100.tsp", "krod100.tsp", "rd100.tsp", "eil101.tsp", "lin105.tsp",
//                "gr120.tsp", "ch130.tsp", "ch150.tsp", "brg180.tsp", "gr202.tsp",
//                "tsp225.tsp", "a280.tsp", "pcb442.tsp", "pa561.tsp", "gr666.tsp",
//                "pr1002.tsp", "pr2392.tsp"
//        };

        // the cost of the optimal solution for each of the preceeding instances.
        // Same order.
//        long OptimalSol[] = {
//                39, 1286, 1473, 1530, 5620, 1613, 1776, 14422, 6905, 1608,
//                1839, 38673, 1950, 1579, 36230, 1788, 1958, 2166, 2307,
//                2420, 2611, 2683, 2755, 1326, 1163, 2465, 2720,
//                2563, 4979, 178, 270, 336, 408, 6859,
//                1000, 2000, 3000, 3000, 3000,
//                3000, 3000, 3000, 4000, 5000,
//
//                6859, 7013, 1272, 937, 1610,
//                2020, 10628, 5046, 426, 7542,
//                675, 538, 108159, 55209, 21282,
//                20749, 21294, 7910, 629, 14379,
//                6942, 6110, 6528, 1950, 40160,
//                3916, 2579, 50778, 2763, 294358,
//                259045, 378032
//        };

        String Names[] = {
                "tsp225.tsp"
        };
        long OptimalSol[] = {
                7542
        };


        int          countNames=Names.length;



        try {
//            FileOutputStream dataOut = new FileOutputStream("result.txt");
//            DataOutputStream fileOut = new DataOutputStream(dataOut);
//
//            FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
//            DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);
//            try {
//                compact_fileOut.writeBytes("file" + '\t' + "ini" + '\t' + "qual_ini" + '\t' + "qual" + '\t' + "time" + '\t' + "lsTime" + '\t' + "gen" + '\t' + "opt" + '\n');
//            } catch (IOException e) {
//                throw new Exception("File not properly opened" + e.toString());
//            }

//      compact_fileOut.writeBytes("HCP com SAX - 10/11.");

            for (count = 0; count < countNames; count++) {
//                switch (Instance.GRAPH_TYPE) {
//                    case Instance.GRAPH_TYPE:
//                        switch (GraphInstance.ATSP_TYPE) {
//                            case GraphInstance.ATSP_TYPE:
//                                switch (ATSPInstance.NONE) {
//                                    case ATSPInstance.NONE:
//                                        ATSPInstance instATSP = new ATSPInstance();
//                                        inst = instATSP;
//                                        break;
//                                    case ATSPInstance.ATSP_RT_TYPE:
//                                        ATSPRTInstance instATSPRT = new ATSPRTInstance();
//                                        inst = instATSPRT;
//                                        break;
//                                }
//                                break;
//
//                            case GraphInstance.TSP_TYPE: {
//                                TSPInstance instTSP = new TSPInstance();
//                                inst = instTSP;
//                                break;
//                            }
//                            case GraphInstance.HCP_TYPE: {
//                                HCPInstance instHCP = new HCPInstance();
//                                inst = instHCP;
//                                break;
//                            }
//                            case GraphInstance.DHCP_TYPE: {
//                                DHCPInstance instDHCP = new DHCPInstance();
//                                inst = instDHCP;
//                                break;
//                            }
//                            default: {
//                                System.err.println("Invalid Graph Type");
//                                System.exit(1);
//                                break;
//                            }
//                        }
//                        break;
//
//                    default: {
//                        System.err.println("Invalid Graph Type");
//                        System.exit(1);
//                        break;
//                    }
//
//                }


//                switch (Reduction.NONE) {
//                    case (Reduction.NONE):
//                        break;
//                    case (Reduction.HCP_To_ATSP): {
//                        ReductionHCPtoATSP redHCPtoATSP = new ReductionHCPtoATSP();
//                        reduction = redHCPtoATSP;
//                        break;
//                    }
//                    case (Reduction.DHCP_TO_ATSP): {
//                        ReductionDHCPtoATSP redDHCPtoATSP = new ReductionDHCPtoATSP();
//                        reduction = redDHCPtoATSP;
//                        break;
//                    }
//                    default: {
//                        System.err.println("Invalid Graph Type");
//                        System.exit(1);
//                        break;
//                    }
//                }
//                if (Reduction.NONE != Reduction.NONE)
//                    inst = reduction.runReduction(inst);

                ProblemInstance problem = new ProblemInstance(new ProblemConfiguration(Memetico.class.getResource("/problems/tsp/tsp225.tsp"), Memetico.class.getResource("/problems/tsp/tsp225.opt.tour")));
                MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(problem.getTspLibInstance().getDimension()));
//          if(MaxGenNum < 200) MaxGenNum = 200;

                Memetico meme = new Memetico(logger, () -> true, problem, structSol, structPop, MetodoConstrutivo,
                        PopSize, mutationRate, BuscaLocal, OPCrossover, OPReStart, OPMutacao, false, //default to false even though the lkh performance is good; just an example configuration
                        MaxTime, MaxGenNum, numReplications);
            }//for

//            compact_fileOut.writeBytes('\t' + String.valueOf(prec.format((double) (AverPopInit / countNames))) + '\t' + '\t');
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (AverQuality / countNames))) + '\t');
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (AverTotalTime / countNames))) + '\t');
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (AverGen / countNames))) + '\t');
//            compact_fileOut.writeBytes(String.valueOf(prec.format((double) (AverSolOpt / countNames))) + "/" + String.valueOf((int) (numReplications)) + '\n');
//
//            compact_fileOut.close();
//            fileOut.close();
        } catch (IOException e) {
            System.err.println("File not opened properly" + e.toString());
//      System.exit(1);
        } catch (Exception e) {
            System.out.println(e.toString());
            e.printStackTrace();
//      System.exit(1);
        }

//   System.exit(0);
    }

    public static void main(String[] args) {
        main(new NullPCLogger(), args);
    }


    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
/*public Agent selectSolutionStruct(Agent refAgent, String StructSol)
{
   return(refAgent);
}
*/

    /* ------------------------------------ selectSolutionStruct -----------------------------------*/
/*public SolutionStruct selectSolutionStruct (String SolutionStr)
{
   if (SolutionStr.equals("DiCycle")){
      DiCycle dc = new DiCycle(Instance.Dimension);
   }
   return();
}
*/
    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
    public CrossoverOperators selectCrossoverOperator(CrossoverOperators refCrossover, String OPCrossover) {
        if (OPCrossover.equals("Strategic Arc Crossover - SAX")) {
            CrossoverSAX sax = new CrossoverSAX();
            refCrossover = sax;
        } else if (OPCrossover.equals("Distance Preserving Crossover - DPX")) {
            CrossoverDPX dpx = new CrossoverDPX();
            refCrossover = dpx;
        } else if (OPCrossover.equals("Uniform")) {
            CrossoverUNN unn = new CrossoverUNN();
            refCrossover = unn;
        } else /*if (OPCrossover.equals("Multiple Fragment - NNRER"))*/ {
            CrossoverMFNN mfnn = new CrossoverMFNN();
            refCrossover = mfnn;
        }
        return (refCrossover);
    }


    /* ------------------------------------ selectRestartOperator ------------------------------------*/
    public DiCycleRestartOperators selectRestartOperator(DiCycleRestartOperators refRestart, String OPRestart, ConstructionAlgorithms refConstr) {
        if (OPRestart.equals("Insertion")) {
            RestartInsertion mi = new RestartInsertion();
            mi.setConstAlg(refConstr);
            refRestart = mi;
        } else if (OPRestart.equals("Cut")) {
            RestartCut mi = new RestartCut();
            mi.setConstAlg(refConstr);
            refRestart = mi;
        }
        return (refRestart);

    }


    /* ------------------------------------ selectMutationOperator ------------------------------------*/
    public MutationOperators selectMutationOperator(MutationOperators refMut, String OPMutation) {
//   if(OPMutation.equals("Insertion")){
        MutationInsertion mi = new MutationInsertion();
        refMut = mi;
//   }
        return (refMut);

    }


    /* ------------------------------------ selectConstructionAlgorithm ------------------------------------*/
    public ConstructionAlgorithms selectConstructionAlgorithm(ConstructionAlgorithms refConstr, String Constr, Instance inst) {
        NearestNeigh nn = new NearestNeigh(inst);
        refConstr = nn;

        return (refConstr);
    }


    /* ------------------------------------ selectCrossoverOperator ------------------------------------*/
    public LocalSearchOperators selectLocalSearchOperator(LocalSearchOperators refLocalSearch, String OPLocalSearch, Instance inst) throws IOException {
        if (OPLocalSearch.equals("Recursive Arc Insertion")) {
            LocalSearchRAI rai = new LocalSearchRAI(inst);
            refLocalSearch = rai;
        } else if (OPLocalSearch.equals("3opt")) {
            LocalSearch3opt opt3 = new LocalSearch3opt();
            refLocalSearch = opt3;
        } else if(OPLocalSearch.equals(LocalSearchOpName.LKH.toString())) {
            refLocalSearch = new LocalSearchLKH(problem.getConfiguration().problemFile);
        } else /*if (OPLocalSearch.equals("3opt"))*/ {
            LocalSearchJohnson lsj = new LocalSearchJohnson(inst);
            refLocalSearch = lsj;
        }

        return (refLocalSearch);
    }


    /* ------------------------------------ Recombine_Pockets_with_Currents ------------------------------------*/
    private void recombinePocketsWithCurrents(Population memePop, CrossoverOperators refCrossover, Instance inst, LocalSearchOperators refLocalSearch) throws Exception {
        GraphInstance graphInst = (GraphInstance) (inst);
        int endPoint[][] = new int[graphInst.dimension][2];              /* just in case ?? */
        SolutionStructure parentA, parentB;
        DiCycle child = new DiCycle(graphInst.dimension);

        parentA = ((PocCurAgent) memePop.pop[1]).pocket;
        parentB = ((PocCurAgent) memePop.pop[2]).current;

//   System.out.println("Crossover");
        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[0].testValues(getSameValues(child)))
            memePop.pop[0].insertSolutionStructure(child);
        else for (int i = 0; i < graphInst.dimension; i++)
            child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        for (int i = 1; i < memePop.nrParents; i++)
            Crossover(refCrossover, memePop, endPoint, i, inst, refLocalSearch);
    }


    /* ------------------------------------ Crossover ------------------------------------*/
    private void Crossover(CrossoverOperators refCrossover, Population memePop, int EndPoint[][], int parent, Instance inst, LocalSearchOperators refLocalSearch) throws Exception {
        GraphInstance graphInst = (GraphInstance) inst;   //?
        PocCurAgent pocCurPop[] = (PocCurAgent[]) memePop.pop;
        int child1, child2, child3, i;
        SolutionStructure parentA, parentB;
        DiCycle child;

        child1 = parent * 3 + (int) (Math.random() * 3);
        child2 = child1 + 1;

        if (child2 == parent * 3 + 4)
            child2 -= 3;

        child3 = child2 + 1;

        if (child3 == parent * 3 + 4)
            child3 -= 3;


        parentA = pocCurPop[child3].pocket;
        parentB = pocCurPop[child2].pocket;

        child = new DiCycle(graphInst.dimension);

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[parent].testValues(getSameValues(child)))
            memePop.pop[parent].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

//verificar ultima linha...

        parentA = pocCurPop[parent].pocket;
        parentB = pocCurPop[child2].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child1].testValues(getSameValues(child)))
            memePop.pop[child1].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

        parentA = pocCurPop[child1].pocket;
        parentB = pocCurPop[child3].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child2].testValues(getSameValues(child)))
            memePop.pop[child2].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();

        child = new DiCycle(graphInst.dimension);

        parentA = pocCurPop[child2].pocket;
        parentB = pocCurPop[child1].current;

        refCrossover.runCrossover(parentA, parentB, child, inst);
        refLocalSearch.runLocalSearch(child, inst);

        if (memePop.isNewSolutionStructure(child.calculateCost(inst)) || memePop.pop[child3].testValues(getSameValues(child)))
            memePop.pop[child3].insertSolutionStructure(child);
        else
            for (i = 0; i < graphInst.dimension; i++)
                child.dontlook[i] = true;

//   System.out.print("Parent 1 : ");
//   ((DiCycle)parentA).printDiCycle();
//   System.out.print("Parent 2 : ");
//   ((DiCycle)parentB).printDiCycle();
//   child.printDiCycle();
    }

    /* ------------------------------------ NNInicializaPop --------------------*/
    private int[][] getSameValues(DiCycle child) {
        int values[][] = new int[10][3], city;

        for (int i = 0; i < 10; i++) {
            city = (int) (Math.random()) * child.size;
            values[i][0] = city;
            values[i][1] = child.arcArray[city].from;
            values[i][2] = child.arcArray[city].tip;
        }
        return (values);
    }


    /* ------------------------------------ NNInicializaPop --------------------*/
    private void NNInicializePop(ConstructionAlgorithms refConstr, int ind, int startcity1, int startcity2, Instance inst, boolean includeSubPop) {
        int last3Poc[], last3Cur[];       //?
        GraphInstance graphInst = (GraphInstance) inst;

        if (graphInst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
            if (startcity1 == 0)
                startcity1++;
            if (startcity2 == 0)
                startcity2++;
        }
        // we run a Nearest Neighbour algorithm to initialize the Pop[ind].Pocket
        // solution starting from city startcity1. We keep record of the last
        // three cities added to the path.
        last3Poc = refConstr.runConstrAlg(((PocCurAgent) memePop.pop[ind]).pocket, startcity1, inst);

        // we analogously create the current solution, now using startcity2 as
        // the starting city
//   for (i=0; i<3; i++) last3[i][1] = last3[i][0];
        last3Cur = refConstr.runConstrAlg(((PocCurAgent) memePop.pop[ind]).current, startcity2, inst);
        if(includeSubPop) {
            if (last3Poc[0] == last3Cur[0] || last3Poc[0] == last3Cur[1] || last3Poc[0] == last3Cur[2] ||
                    last3Poc[0] == last3Poc[1] || last3Poc[0] == last3Poc[2])
                last3Poc[0] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Poc[1] == last3Cur[0] || last3Poc[1] == last3Cur[1] || last3Poc[1] == last3Cur[2] ||
                    last3Poc[1] == last3Poc[0] || last3Poc[1] == last3Poc[2])
                last3Poc[1] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Poc[2] == last3Cur[0] || last3Poc[2] == last3Cur[1] || last3Poc[2] == last3Cur[2] ||
                    last3Poc[2] == last3Poc[0] || last3Poc[2] == last3Poc[1])
                last3Poc[2] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[0] == last3Cur[1] || last3Cur[0] == last3Cur[2] || last3Cur[0] == last3Poc[0] ||
                    last3Cur[0] == last3Poc[1] || last3Cur[0] == last3Poc[2])
                last3Cur[0] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[1] == last3Cur[0] || last3Cur[1] == last3Cur[2] || last3Cur[1] == last3Poc[0] ||
                    last3Cur[1] == last3Poc[1] || last3Cur[1] == last3Poc[2])
                last3Cur[1] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (last3Cur[2] == last3Cur[0] || last3Cur[2] == last3Cur[1] || last3Cur[2] == last3Poc[0] ||
                    last3Cur[2] == last3Poc[1] || last3Cur[2] == last3Poc[2])
                last3Cur[2] = (int) (Math.random() * ((GraphInstance) inst).dimension);

            if (ind < memePop.nrParents) {
                // we recursively initialze the solutions using the NNAlgorithm
                // seeding it with the last visited cities of the leader agent
                NNInicializePop(refConstr, ind * 3 + 1, last3Poc[0], last3Cur[0], inst);
                NNInicializePop(refConstr, ind * 3 + 2, last3Poc[1], last3Cur[1], inst);
                NNInicializePop(refConstr, ind * 3 + 3, last3Poc[2], last3Cur[2], inst);
            }
        }
    }

    private void NNInicializePop(ConstructionAlgorithms refConstr, int ind, int startcity1, int startcity2, Instance inst) {
        NNInicializePop(refConstr, ind, startcity1, startcity2, inst, true);
    }


    public ILogger<MemeticoSnapshot> getLogger() {
        return logger;
    }
}//fim da classe
