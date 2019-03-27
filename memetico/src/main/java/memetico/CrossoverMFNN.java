package memetico;

class CrossoverMFNN extends DiCycleCrossover {
    /* ------------------------------------ CrossoverDiana ------------------------------------*/
    public void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) {
        GraphInstance graphInst = (GraphInstance) inst;
        int i, stringNumber = 0, nextA, nextB, arcNext,
                arcPrev, countArc = 0, aux, nString = 0;
        int mark1[] = new int[graphInst.dimension],
                mark2[] = new int[graphInst.dimension], sorted[];
        double distArc[] = new double[2 * graphInst.dimension];
        Arc listArc[] = new Arc[2 * graphInst.dimension];
        int endPoint[][] = new int[graphInst.dimension][2];              /* just in case ?? */
        DiCycle childDiCycle = (DiCycle) child;
        DiCycle parentADiCycle = (DiCycle) parentA;
        DiCycle parentBDiCycle = (DiCycle) parentB;

        for (i = 0; i < graphInst.dimension; i++)
            childDiCycle.arcArray[i].tip = childDiCycle.arcArray[i].from = mark1[i] = mark2[i] = -1;

        /*Faz A inter B*/
        for (i = 0; i < graphInst.dimension; i++) {
            nextA = parentADiCycle.arcArray[i].tip;
            nextB = parentBDiCycle.arcArray[i].tip;
            if (nextA == nextB)
                childDiCycle.insertInChild(nextA, i, 1);
            else {
                distArc[countArc] = graphInst.matDist[i][nextA];
                listArc[countArc] = new Arc();
                listArc[countArc].from = i;
                listArc[countArc++].tip = nextA;

                distArc[countArc] = graphInst.matDist[i][nextB];
                listArc[countArc] = new Arc();
                listArc[countArc].from = i;
                listArc[countArc++].tip = nextB;
            }
        }
        sorted = new int[countArc];
        ordena(countArc, distArc, sorted);


        stringNumber = childDiCycle.fillEndPoint(endPoint, stringNumber);

        /****** ver se tirar isso ou nao ******/
/*   for (i=0; i<StringNumber; i++){
	  ChildDiCycle.dontlook[EndPoint[i][0]]=false;
	  ChildDiCycle.dontlook[EndPoint[i][1]]=false;
   }
*/
        for (i = 0; i < stringNumber; i++) {
            mark1[endPoint[i][1]] = endPoint[i][0];
            mark2[endPoint[i][0]] = endPoint[i][1];
        }

        nString = stringNumber;

        for (i = 0; i < countArc; i++) {
            arcPrev = listArc[sorted[i]].from;
            arcNext = listArc[sorted[i]].tip;
            if (mark1[arcNext] != -1 && mark2[arcPrev] != -1 && mark2[arcPrev] != arcNext) {
                childDiCycle.insertInChild(arcNext, arcPrev, 1);
                aux = mark2[mark1[arcNext]] = mark2[arcPrev];
                mark1[mark2[arcPrev]] = mark1[arcNext];
                mark2[arcPrev] = mark1[arcNext] = -1;
                if (--nString == 1) {
                    i = countArc;//para cair fora do for
                    childDiCycle.insertInChild(aux, mark1[aux], 1);
                }
            }
        }

        if (nString > 1) {
            nString = 0;
            for (i = 0; i < stringNumber; i++) {
                aux = endPoint[i][0];
                if (mark2[aux] != -1) {
                    endPoint[nString][0] = aux;
                    endPoint[nString++][1] = mark2[aux];
                    childDiCycle.insertInChild(mark2[aux], aux, 1);
                }
            }
            stringNumber = nString;

            for (i = 0; i < stringNumber; i++) {
                childDiCycle.dontlook[endPoint[i][0]] = false;
                childDiCycle.dontlook[endPoint[i][1]] = false;
            }
            childDiCycle.nextNeighborPatching(stringNumber, endPoint, graphInst);
        }
    }


    /* ------------------------------------ ordena ------------------------------------*/
    private void ordena(int countArc, double distArc[], int sorted[]) {
        int j, i, auxIndex;
        double auxDist;

        for (j = 0; j < countArc; j++)
            sorted[j] = j;
        for (i = countArc; i > 1; i--) {
            for (j = 1; j < i; j++) {
                if (distArc[j - 1] > distArc[j]) {
                    auxDist = distArc[j - 1];
                    distArc[j - 1] = distArc[j];
                    distArc[j] = auxDist;

                    auxIndex = sorted[j - 1];
                    sorted[j - 1] = sorted[j];
                    sorted[j] = auxIndex;
                }
            }
        }
    }

}
