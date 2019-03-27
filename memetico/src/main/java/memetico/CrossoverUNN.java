package memetico;

class CrossoverUNN extends DiCycleCrossover {

    /* ------------------------------------ CrossoverUniforme ------------------------------------*/
    public void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) {
        GraphInstance graphInst = (GraphInstance) inst;
        int stringNumber = 0, i, nextA, nextB, prevA, prevB, index;
        boolean mark[] = new boolean[graphInst.dimension];
        int endPoint[][] = new int[graphInst.dimension][2];    /* just in case ?? */
        DiCycle childDiCycle = (DiCycle) child;
        DiCycle parentADiCycle = (DiCycle) parentA;
        DiCycle parentBDiCycle = (DiCycle) parentB;

        for (i = 0; i < graphInst.dimension; i++)
            childDiCycle.arcArray[i].tip = childDiCycle.arcArray[i].from = -1;

        for (i = 0; i < graphInst.dimension; i++) {
            nextA = parentADiCycle.arcArray[i].tip;
            nextB = parentBDiCycle.arcArray[i].tip;

            if (nextA == nextB && mark[nextA] == false) {
                childDiCycle.insertInChild(nextA, i, 1);
                mark[nextA] = true;
            } else {
                if (Math.random() < 0.5) {
//	 if (Dist[i][NextA] < Dist[i][NextB]){
                    if (mark[nextA] == false) {
                        childDiCycle.insertInChild(nextA, i, 1);
                        mark[nextA] = true;
                    } else if (mark[nextB] == false) {
                        childDiCycle.insertInChild(nextB, i, 1);
                        mark[nextB] = true;
                    }
                } else {
                    if (mark[nextB] == false) {
                        childDiCycle.insertInChild(nextB, i, 1);
                        mark[nextB] = true;
                    } else if (mark[nextA] == false) {
                        childDiCycle.insertInChild(nextA, i, 1);
                        mark[nextA] = true;
                    }
                }//else
            }//else
        }//for


        stringNumber = childDiCycle.fillEndPoint(endPoint, stringNumber);

        for (i = 0; i < stringNumber; i++) {
            childDiCycle.dontlook[endPoint[i][0]] = false;
            childDiCycle.dontlook[endPoint[i][1]] = false;
        }

        if (stringNumber > 1)
            childDiCycle.nextNeighborPatching(stringNumber, endPoint, graphInst);
    }


    /* ------------------------------------ CrossoverUniforme ------------------------------------*/
    private void crossoverUniforme1(int endPoint[][], DiCycle parentA, DiCycle parentB, DiCycle childDiCycle, GraphInstance inst) {
        int stringNumber = 0, i, nextA, nextB, prevA, prevB, index;
        boolean mark[] = new boolean[childDiCycle.size];

        for (i = 0; i < childDiCycle.size; i++)
            childDiCycle.arcArray[i].tip = childDiCycle.arcArray[i].from = -1;

        for (i = 0; i < childDiCycle.size; i++) {
            nextA = parentA.arcArray[i].tip;
            nextB = parentB.arcArray[i].tip;

            if (nextA == nextB && mark[nextA] == false) {
                childDiCycle.insertInChild(nextA, i, 1);
                mark[nextA] = true;
            } else {
                if (Math.random() < 0.5) {
                    if (mark[nextA] == false) {
                        childDiCycle.insertInChild(nextA, i, 1);
                        mark[nextA] = true;
                    } else if (mark[nextB] == false) {
                        childDiCycle.insertInChild(nextB, i, 1);
                        mark[nextB] = true;
                    } else {
                        prevA = parentA.arcArray[i].from;
                        if (mark[prevA] == false) {
                            childDiCycle.insertInChild(prevA, i, 1);
                            mark[prevA] = true;
                        } else {
                            prevB = parentB.arcArray[i].from;
                            if (mark[prevB] == false) {
                                childDiCycle.insertInChild(prevB, i, 1);
                                mark[prevB] = true;
                            }
                        }
                    }
                } else {
                    if (mark[nextB] == false) {
                        childDiCycle.insertInChild(nextB, i, 1);
                        mark[nextB] = true;
                    } else if (mark[nextA] == false) {
                        childDiCycle.insertInChild(nextA, i, 1);
                        mark[nextA] = true;
                    } else {
                        prevB = parentB.arcArray[i].from;
                        if (mark[prevB] == false) {
                            childDiCycle.insertInChild(prevB, i, 1);
                            mark[prevB] = true;
                        } else {
                            prevA = parentA.arcArray[i].from;
                            if (mark[prevA] == false) {
                                childDiCycle.insertInChild(prevA, i, 1);
                                mark[prevA] = true;
                            }
                        }
                    }
                }//else
            }//else
        }//for

        stringNumber = childDiCycle.fillEndPoint(endPoint, stringNumber);

        for (i = 0; i < stringNumber; i++) {
            childDiCycle.dontlook[endPoint[i][0]] = false;
            childDiCycle.dontlook[endPoint[i][1]] = false;
        }

        if (stringNumber > 1)
            childDiCycle.nextNeighborPatching(stringNumber, endPoint, inst);
    }


}//end UNN class
