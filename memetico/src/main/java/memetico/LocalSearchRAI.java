package memetico;

public class LocalSearchRAI extends DiCycleLocalSearchOperator {

    public MatrixNeigh matNeighbors;

    public LocalSearchRAI(Instance inst) {
        matNeighbors = new MatrixNeigh((GraphInstance) inst);
    }

    /* ------------------------------------ runLocalSearch - ---------------*/
    public void runLocalSearch(SolutionStructure tIns, Instance inst) {
        DiCycle tInsDiCycle = (DiCycle) tIns;

        for (int j = 0; j < ((GraphInstance) inst).dimension; j++) {
            if (tInsDiCycle.dontlook[j] == false) {
                localSearchRAI(tInsDiCycle, j, (GraphInstance) inst);
                tInsDiCycle.dontlook[j] = true;
            }
        }
    }

    /* ------------------------------------ Delta1_Change_1Arc ----------------------------------*/
    private double delta1Change1Arc(DiCycle tIns, int city, int neigh, GraphInstance inst)
    /* +(City->Neigh) - (City->Sucessor_de_City) - (Antecessor_de_Neigh->Neigh)*/ {
        return (inst.matDist[city][neigh] - inst.matDist[city][tIns.arcArray[city].tip]
                - inst.matDist[tIns.arcArray[neigh].from][neigh]);
    }


    /* ------------------------------------ Delta2_Change_1Arc ------------------------------------*/
    private double delta2Change1Arc(Arc arcOut, int next_City, int prev_Neigh, GraphInstance inst)
    /*+(Inicio_ArcOut -> NextCity) +(PrevNeigh -> Fim_ArcOut) -(Inicio_ArcOut -> Fim_ArcOut)*/ {
        return (inst.matDist[arcOut.from][next_City] + inst.matDist[prev_Neigh][arcOut.tip]
                - inst.matDist[arcOut.from][arcOut.tip]);
    }


    /* ------------------------------------ Insert_Arc ------------------------------------*/
    /*Tenta inserir o arco City->Neigh no DiCycle T*/
    private boolean insertArc(DiCycle tIns, int city, int neigh, GraphInstance inst, LinkedList lista) {
        /*ArcOut sera o arco que sera selecionado para ser excluido de T*/
        Arc arcOut = new Arc();

        /*Se o DiCycle diminuira de tamanho, entao faca a insercao do arco Neigh->City e retire o arco ArcOut*/
        if (testArcInsertion(tIns, city, neigh, arcOut, inst)) {
            tIns.changeDiCycle(inst, arcOut, city, neigh);

            /*Reotimiza-se as cidades dos extremos dos 3 arcos que foram inseridos*/
            if (inst.graphType == GraphInstance.TSP_TYPE) {
                recursiveArcInsertion_STSP(tIns, tIns.arcArray[arcOut.from].tip, inst, lista);/*Cidade sucessora a City no T original*/
                recursiveArcInsertion_STSP(tIns, tIns.arcArray[arcOut.tip].from, inst, lista);/*Cidade antecessora a Neigh no T original*/
                recursiveArcInsertion_STSP(tIns, arcOut.tip, inst, lista);/*Cidade final do arco ArcOut*/
                recursiveArcInsertion_STSP(tIns, arcOut.from, inst, lista);/*Cidade inicial do arco ArcOut*/
                recursiveArcInsertion_STSP(tIns, neigh, inst, lista);
            } else if (inst.graphType == inst.ATSP_TYPE) {

/*		 localSearchRAI(tIns, tIns.arcArray[arcOut.from].tip, inst);
		 localSearchRAI(tIns, tIns.arcArray[arcOut.tip].from, inst);
		 localSearchRAI(tIns, arcOut.tip, inst);
		 localSearchRAI(tIns, arcOut.from, inst);
		 localSearchRAI(tIns, neigh, inst);
*/
                lista.append(tIns.arcArray[arcOut.from].tip);
                lista.append(tIns.arcArray[arcOut.tip].from);
                lista.append(arcOut.tip);
                lista.append(arcOut.from);
                lista.append(neigh);
                lista.append(city);

            }

            return (true);
        }

        return (false);
    }


    /* ------------------------------------ LocalSearchRAI - ---------------*/
    /*T: DiCycle a ser otimizado   City: cidade onde tenta-se inserir arco antecessor e sucessor a City*/
    public void localSearchRAI(DiCycle tIns, int initialCity, GraphInstance inst) {
        int neigh, indice;
        LinkedList lista = new LinkedList();
        lista.append(initialCity);

        while (!lista.isEmpty()) {

            initialCity = lista.getLast();
            lista.getTail().extract();

/*      initialCity = lista.getFirst();
      lista.getHead().extract();
*/
            /*Em primeiro lugar, tenta-se inserir uma cidade antecessora a City*/
            indice = selectRand();

            //Update list of neighbors for initial city if problem is of instance ATSP-RT
            if (inst.graphType == GraphInstance.ATSP_TYPE &&
                    ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                matNeighbors.fillWithNeighbors(inst, tIns, initialCity);
            }

            neigh = matNeighbors.neighbors[initialCity][MatrixNeigh.PREDECESSOR][indice];
            /*Se N_NEIGH esta setado para 1 e esta ja antecessora de InitialCity no DiCycle, entao nao ha mudanca*/
            if (tIns.arcArray[initialCity].from != neigh)
                insertArc(tIns, neigh, initialCity, inst, lista);

            /*Em segundo lugar, tenta-se inserir uma cidade sucessora a InitialCity*/
            indice = selectRand();
            /*Nao aceita que a cidade a ser inserida ja seja sucessora de InitialCity em T*/
            neigh = matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice];
            /*Se N_Neigh esta setado para 1 e este ja sucessora de InitialCity no DiCycle, entao nao ha mudanca*/
            if (tIns.arcArray[initialCity].tip != neigh)
                insertArc(tIns, initialCity, neigh, inst, lista);
        }
    }


    /* ------------------------------------ RecursiveArcInsertion_STSP ------------------------------------*/
    /*T: DiCycle a ser otimizado   InitialCity: cidade onde tenta-se inserir arco antecessor e sucessor a InitialCity*/
    public void recursiveArcInsertion_STSP(DiCycle tIns, int initialCity, GraphInstance inst, LinkedList lista) {
        int neigh, indice; /*Ponto final (seta) do arco que esta sendo inserido*/

        /*Em primeiro lugar, tenta-se inserir uma cidade antecessora a InitialCity*/
        indice = selectRand();
        //Update list of neighbors for initial city if problem is of instance ATSP-RT
        if (inst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
            matNeighbors.fillWithNeighbors(inst, tIns, initialCity);
        }
        /*Nao aceita que a cidade a ser inserida ja seja antecessora de InitialCity em T*/
        while ((tIns.arcArray[initialCity].from == matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice]
                || tIns.arcArray[initialCity].tip == matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice])
                && matNeighbors.nrNeighbors > 2) {
            indice--;
            if (indice == -1) indice = 2;
        }
        neigh = matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice];
        /*Se N_NEIGH esta setado para 1 e esta ja  antecessora de InitialCity no DiCycle, entao nao ha mudanca*/
        if (tIns.arcArray[initialCity].from != neigh && tIns.arcArray[initialCity].tip != neigh)
            insertArc(tIns, neigh, initialCity, inst, lista);


        /*Em segundo lugar, tenta-se inserir uma cidade sucessora a InitialCity*/
        indice = selectRand();
        /*Nao aceita que a cidade a ser inserida ja seja sucessora de InitialCity em T*/
        while ((tIns.arcArray[initialCity].from == matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice]
                || tIns.arcArray[initialCity].tip == matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice])
                && matNeighbors.nrNeighbors > 2) {
            indice--;
            if (indice == -1) indice = 2;
        }
        neigh = matNeighbors.neighbors[initialCity][MatrixNeigh.SUCCESSOR][indice];
        /*Se N_Neigh esta setado para 1 e este ja  sucessora de InitialCity no DiCycle, entao nao ha mudanca*/
        if (tIns.arcArray[initialCity].tip != neigh && tIns.arcArray[initialCity].from != neigh)
            insertArc(tIns, initialCity, neigh, inst, lista);
    }


    /* ------------------------------------ Select_Rand ------------------------------------*/
    private int selectRand() {
        double rand = Math.random();

        if (rand < 0.40) return (0);
        else if (rand < 0.70) return (1);
        else if (rand < 0.85) return (2);
        else if (rand < 0.95) return (3);
        else return (4);


    }


    /* ------------------------------------ Test_Insert_Arc ------------------------------------*/
    /*Verifica o valor que sera acrescido a T com insercao do arco City->Neigh e remocao do arco ArcOut*/
    private boolean testArcInsertion(DiCycle solution, int city, int neigh, Arc arcOut, GraphInstance inst) {
        /*delta2 = custo de factibilizacao do DiCycle se insere arco (City->Neigh) e retira ArcOut*/
        double delta1, delta2 = Double.MAX_VALUE, cost = Double.MAX_VALUE;
        int nextCity, fromNeigh;

        if (inst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {

            arcOut.from = neigh;
            arcOut.tip = solution.arcArray[neigh].tip;

            /*Looks for an arc that decreases the dicycle objective function*/
            while (arcOut.from != city && cost >= solution.cost) {
                cost = solution.emulateChange((ATSPRTInstance) inst, arcOut, city, neigh);
                if (cost >= solution.cost) {
                    arcOut.from = arcOut.tip;
                    arcOut.tip = solution.arcArray[arcOut.tip].tip;
                } else
                    return true;
            }
        } else {
            /*delta1 contem o delta dos arcos fixos*/
            delta1 = delta1Change1Arc(solution, city, neigh, inst);

            /*Inicia a busca do de ArcOut pelo arco (Neigh->Sucessor_de_Neigh)*/
            arcOut.from = neigh;
            arcOut.tip = solution.arcArray[neigh].tip;

            nextCity = solution.arcArray[city].tip;
            fromNeigh = solution.arcArray[neigh].from;

            /*Procura um arco que diminua o DiCycle. Procura de Neigh ate City, ou ate encontralo*/
            while (arcOut.from != city && (delta1 + delta2) >= 0) {

                delta2 = delta2Change1Arc(arcOut, nextCity, fromNeigh, inst);

                if ((delta1 + delta2) >= 0)//Gets the following arc in a directed cycle.
                {
                    arcOut.from = arcOut.tip;
                    arcOut.tip = solution.arcArray[arcOut.tip].tip;
                } else
                    return (true);

            }
        }
        return (false);
    }


}//fim da classe
