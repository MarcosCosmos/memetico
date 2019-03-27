package memetico;

class CrossoverDPX extends DiCycleCrossover {

    /* ------------------------------------ CrossoverMerz ------------------------------------*/
    public void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) {
        GraphInstance graphInst = (GraphInstance) inst;
        int stringNumber = 0, i, nextA, nextB, prevA, prevB;
        int endPoint[][] = new int[graphInst.dimension][2];     /* just in case ?? */
        DiCycle childDiCycle = (DiCycle) child;
        DiCycle parentADiCycle = (DiCycle) parentA;
        DiCycle parentBDiCycle = (DiCycle) parentB;

        for (i = 0; i < graphInst.dimension; i++)
            childDiCycle.arcArray[i].tip = childDiCycle.arcArray[i].from = -1;

        for (i = 0; i < graphInst.dimension; i++) {
            nextA = parentADiCycle.arcArray[i].tip;
            nextB = parentBDiCycle.arcArray[i].tip;

            if (nextA == nextB)
                childDiCycle.insertInChild(nextA, i, 1);
        }

        stringNumber = childDiCycle.fillEndPoint(endPoint, stringNumber);

        for (i = 0; i < stringNumber; i++) {
            childDiCycle.dontlook[endPoint[i][0]] = false;
            childDiCycle.dontlook[endPoint[i][1]] = false;
        }

        if (stringNumber > 1)
            vmprox_merz(childDiCycle, stringNumber, endPoint, parentADiCycle, parentBDiCycle, graphInst);
    }

    /* ------------------------------------ vmprox_merz ------------------------------------*/
    public void vmprox_merz(DiCycle solution, int nStrings, int endPoint[][], DiCycle parentA, DiCycle parentB, GraphInstance inst) {
        int atual = 0, /*cidade atual*/
                count, neigh,
                startcity,
                initial[] = new int[nStrings],
                terminal[] = new int[nStrings],
                proximo, prox_index; /*proxima cidade a ser visitada*/


        for (count = 0; count < nStrings; count++) {
            initial[count] = endPoint[count][1];
            terminal[count] = endPoint[count][0];
        }

        prox_index = (int) (Math.random() * nStrings);
        atual = terminal[prox_index];
        startcity = initial[prox_index];
        initial[prox_index] = -1;

        count = 0;
        while (count < nStrings) /*enquanto todas as cidades nao forem visitadas*/ {
            count++;
            if (count == nStrings) {
                proximo = startcity;
            } else {
                prox_index = acha_no_merz(nStrings, terminal[prox_index], initial,
                        parentA.arcArray[terminal[prox_index]].tip,
                        parentB.arcArray[terminal[prox_index]].tip, inst);
                proximo = initial[prox_index];
                initial[prox_index] = -1;/*marca cidade visitada*/
            }
            solution.arcArray[atual].tip = proximo;
            solution.arcArray[proximo].from = atual;
            atual = terminal[prox_index];
        }
    }


    /* ------------------------------------ acha_no ------------------------------------*/
    private int acha_no_merz(int NStrings, int city, int initial[], int NextA, int NextB, GraphInstance inst) {
        int index_neigh = -1, count = 0, Aux[] = new int[inst.dimension];
        double menor = 100000000;

        for (int i = 0; i < NStrings; i++) /*percorre a matrix*/ {
            if (initial[i] >= 0)    /*se cidade ainda nao visitada*/ {
                if (initial[i] != NextA && initial[i] != NextB) {
                    if (inst.matDist[city][initial[i]] < menor) {
                        index_neigh = i;
                        menor = inst.matDist[city][initial[i]];
                    }
                } else
                    Aux[count++] = i;
            }
        }

        /*retorna vizinho mais proximo ao parametro recebido ->partida*/
        if (index_neigh != -1)
            return (index_neigh);
        else
            return (Aux[(int) (Math.random() * count)]);
    }

}//end of DPX class
