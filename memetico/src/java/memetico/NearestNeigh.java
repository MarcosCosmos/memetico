package memetico;

class NearestNeigh extends DiCycleConstructAlgorithm {

    public GraphInstance inst;
    public MatrixNeigh matNeighbors;

    /* ------------------------------------setMatrixDouble ------------------------------------*/
    public NearestNeigh(Instance i) {
        inst = (GraphInstance) i;
        matNeighbors = new MatrixNeigh(inst);
    }


    /* ------------------------------------ vmprox ------------------------------------*/
    public int[] runConstrAlg(SolutionStructure child, int startingCity, Instance inst) {
        GraphInstance graphInst = (GraphInstance) inst;
        int last3[] = new int[3];
        int currentCity = 0,
                count = 0,
                nextCity;
        boolean visited[] = new boolean[graphInst.dimension];
        DiCycle newChild = (DiCycle) child;

        /* vector that marks visited cities */
        visited[startingCity] = true;
        currentCity = startingCity;
        newChild.startTime[currentCity] = 0;

        if (graphInst.graphType == GraphInstance.ATSP_TYPE && startingCity != 0) {
            if (((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                newChild.arcArray[0].tip = startingCity;
                newChild.arcArray[startingCity].from = 0;

                newChild.startTime[0] = ((ATSPRTInstance) inst).readyTime[0];

                if (newChild.startTime[0] + graphInst.matDist[0][currentCity] <
                        ((ATSPRTInstance) inst).readyTime[currentCity]) {
                    newChild.startTime[currentCity] = ((ATSPRTInstance) inst).readyTime[currentCity];
                } else
                    newChild.startTime[currentCity] = newChild.startTime[0] + graphInst.matDist[0][currentCity];

                count = 1;
                startingCity = 0;
                visited[startingCity] = true;
            }
        }

        /* all cities are yet unvisited */
        /* while there are unvisited cities */
        while (count < graphInst.dimension) {
            count++;

            // we close the base.DiCycle returning to the first visited city
            if (count == graphInst.dimension)
                nextCity = startingCity;
            else
                nextCity = nearestUnvisitedCity(newChild, currentCity, visited, graphInst);

            // mark as visited
            visited[nextCity] = true;

            // we update the prev/next links
            newChild.arcArray[currentCity].tip = nextCity;
            newChild.arcArray[nextCity].from = currentCity;

            //Calculates the arrival time at city - nextCity
            if (nextCity != startingCity) {
                newChild.startTime[nextCity] = newChild.startTime[currentCity] +
                        graphInst.matDist[currentCity][nextCity];

                if (graphInst.graphType == GraphInstance.ATSP_TYPE &&
                        ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                    if (newChild.startTime[currentCity] + graphInst.matDist[currentCity][nextCity] <
                            ((ATSPRTInstance) inst).readyTime[nextCity]) {
                        newChild.startTime[nextCity] = ((ATSPRTInstance) inst).readyTime[nextCity];
                    }
                }
            }
            currentCity = nextCity;

            // keeps record of the last three cities visited
            if ((graphInst.dimension - count) < 4 && count != graphInst.dimension)
                last3[graphInst.dimension - count - 1] = nextCity;
        }

        return (last3);
    }

    /* ------------------------------------ nearest_unvisited_city -----------------*/
    private int nearestUnvisitedCity(DiCycle child, int city, boolean visited[], GraphInstance inst) {
        int nearest_unvisited_city = -1, i;
        double shortest_distance = Double.MAX_VALUE,
                distance;

        //Update list of neighbors for city if the problem is of type ATSP-RT
        if (inst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
            matNeighbors.fillWithNeighbors(inst, child, city);
        }
        // this gives some priority to the closest N_NEIGH cities
        for (i = 0; i < matNeighbors.nrNeighbors; i++)
            if (!visited[matNeighbors.neighbors[city][MatrixNeigh.SUCCESSOR][i]])
                return (matNeighbors.neighbors[city][MatrixNeigh.SUCCESSOR][i]);
        // it was a greedy decision, we should also check what may happen
        // if we replace with for (i=(base.MatrixNeigh.N_NEIGH-1); i>-1, i--)

        // since the first N_NEIGH cities have been visited...
        // we need to find the nearest yet unvisited one...
        for (i = 0; i < inst.dimension; i++) {
            if (!visited[i] && i != city) {
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                        distance = Math.max(child.startTime[city] + inst.matDist[city][i],
                                ((ATSPRTInstance) inst).readyTime[i]);

                        distance = distance - child.startTime[city];

                        if (distance < shortest_distance) {
                            nearest_unvisited_city = i;
                            shortest_distance = distance;
                        }
                    } else if (inst.matDist[city][i] < shortest_distance) {
                        nearest_unvisited_city = i;
                        shortest_distance = inst.matDist[city][i];
                    }
                } else if (inst.matDist[city][i] < shortest_distance) {
                    nearest_unvisited_city = i;
                    shortest_distance = inst.matDist[city][i];
                }
            }
        }

        return (nearest_unvisited_city);
    }


}//fim da classe
