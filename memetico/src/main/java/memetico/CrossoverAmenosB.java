package memetico;

class CrossoverAmenosB extends DiCycleCrossover {

    /* ------------------------------------ CrossoverAmenosB ------------------------------------*/
    public void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) {
        GraphInstance graphInst = (GraphInstance) inst;
        int stringNumber = 0, i, nextA, nextB, prevA, prevB;
        int endPoint[][] = new int[graphInst.dimension][2];      /* just in case ?? */
        DiCycle childDiCycle = (DiCycle) child;
        DiCycle parentADiCycle = (DiCycle) parentA;
        DiCycle parentBDiCycle = (DiCycle) parentB;

        for (i = 0; i < graphInst.dimension; i++) {
            childDiCycle.arcArray[i].tip = parentADiCycle.arcArray[i].tip;
            childDiCycle.arcArray[i].from = parentADiCycle.arcArray[i].from;
        }

        for (i = 0; i < graphInst.dimension; i++) {
            nextA = parentADiCycle.arcArray[i].tip;
            nextB = parentBDiCycle.arcArray[i].tip;
            if (nextA == nextB)
                childDiCycle.arcArray[nextA].from = childDiCycle.arcArray[i].tip = -1;
        }

        stringNumber = childDiCycle.fillEndPoint(endPoint, stringNumber);

        for (i = 0; i < stringNumber; i++) {
            childDiCycle.dontlook[endPoint[i][0]] = false;
            childDiCycle.dontlook[endPoint[i][1]] = false;
        }

        if (stringNumber > 1)
            childDiCycle.nextNeighborPatching(stringNumber, endPoint, graphInst);
    }

}
