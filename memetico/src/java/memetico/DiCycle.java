package memetico;

import java.io.*;


public class DiCycle extends SolutionStructure {
    /* DiCycle = a Double-Linked Cyclic Permutation of {1..N} integers */
/* it also has a boolean array of ``don't look bits'' which is
   generally useful for some local search implementations         */

    public Arc arcArray[];
    boolean dontlook[];
    double startTime[];

    //may as well be final until we might need to change it, since it's public
    public final int startingCity = 0;


    public DiCycle() {
        super.solutionStructureType = super.DICYCLE_TYPE;
    }

    /* ------------------------------------ Construtor ------------------------------------*/
    public DiCycle(int nCities) {
        dontlook = new boolean[nCities];
        arcArray = new Arc[nCities];
        startTime = new double[nCities];

        for (int i = 0; i < nCities; i++) {
            arcArray[i] = new Arc();
            dontlook[i] = true;
        }
        size = nCities;
        super.solutionStructureType = super.DICYCLE_TYPE;
    }

    /**
     * Copy Constructor for DiCycle.
     *
     * @param solution:DiCycle Solution to be copied to this instance.
     */
    public DiCycle(DiCycle solution) {
        size = solution.size;

        dontlook = new boolean[size];
        arcArray = new Arc[size];
        startTime = new double[size];

        for (int i = 0; i < size; i++) {
            arcArray[i] = new Arc(solution.arcArray[i]);
            dontlook[i] = solution.dontlook[i];
        }

        super.solutionStructureType = super.DICYCLE_TYPE;
    }

    /* ------------------------------------ saveInOptTour ------------------------------------*/
    public void saveInOptTour(File destination) throws Exception {
        int city = 0;

        try {
            FileOutputStream dataOut = new FileOutputStream(destination);
            DataOutputStream fileOut = new DataOutputStream(dataOut);

            fileOut.writeBytes(String.valueOf("NAME: " + destination.getName() + '\n'));
            fileOut.writeBytes("TYPE: TOUR" + '\n');
            fileOut.writeBytes("COMMENT: Optimal solution for " + destination.getName() + '\n');
            fileOut.writeBytes("DIMENSION: " + String.valueOf(size) + '\n');
            fileOut.writeBytes("TOUR_SECTION" + '\n');

            for (int i = 0; i < size; i++) {
                fileOut.writeBytes(String.valueOf(city + 1));
                if (i > 0 && i % 14 == 0 && size != i)
                    fileOut.writeBytes(String.valueOf((char) '\n'));
                else
                    fileOut.writeBytes(" ");

                city = arcArray[city].tip;
            }
            fileOut.writeBytes('\n' + "-1" + '\n' + "EOF" + '\n');
        } catch (IOException e) {
            throw new Exception("File not properly opened" + e.toString());
        }
    }


    public boolean isSameValues(int values[][]) {
        for (int i = 0; i < values.length; i++) {
            if (arcArray[values[i][0]].from != values[i][1]) return false;
            if (arcArray[values[i][0]].tip != values[i][2]) return false;
        }
        return true;
    }


    /* ------------------------------------ CalculateCost ------------------------------------*/
    public double calculateCost(Instance inst) {
        int nextCity, city = startingCity, i;
        GraphInstance gInst = (GraphInstance) inst;

        if (gInst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) gInst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
            cost = startTime[city] = ((ATSPRTInstance) inst).readyTime[city];
        } else
            cost = startTime[city] = 0;

        for (i = 0; i < gInst.dimension; i++) {
            nextCity = arcArray[city].tip;

            cost = startTime[city] + gInst.matDist[city][nextCity];

            if (nextCity != startingCity) {
                if (gInst.graphType == GraphInstance.ATSP_TYPE &&
                        ((ATSPInstance) gInst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                    startTime[nextCity] = Math.max(cost, ((ATSPRTInstance) inst).readyTime[nextCity]);
                } else {
                    startTime[nextCity] = cost;
                }
            } else
                break;

            city = nextCity;
        }

        return (cost);
    }

    public double updateCost(Instance inst, int from) {
        int nextCity, city = from, i;
        GraphInstance gInst = (GraphInstance) inst;

        cost = startTime[city];

        for (i = 0; i < gInst.dimension; i++) {
            nextCity = arcArray[city].tip;

            cost = startTime[city] + gInst.matDist[city][nextCity];

            if (nextCity != startingCity) {
                if (gInst.graphType == GraphInstance.ATSP_TYPE &&
                        ((ATSPInstance) gInst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
                    startTime[nextCity] = Math.max(cost, ((ATSPRTInstance) inst).readyTime[nextCity]);
                } else {
                    startTime[nextCity] = cost;
                }
            } else
                break;

            city = nextCity;
        }

        return (cost);
    }

    /* ------------------------------------ NN_patching ------------------------------------*/
    public void nextNeighborPatching(int NStrings, int EndPoint[][], GraphInstance inst) {
        //endpoint as in the endpoints of any disjoint strings; (so the index of the actual cities which are endpoints of strings are stored in initial and terminal
        int currentcity = 0,
                count, Neigh,
                startcity,
                //these variable names appear to need flipping
                initial[] = new int[NStrings], //arc destination
                terminal[] = new int[NStrings], //arc start node
                proximo, prox_index;                 /*proxima cidade a ser visitada*/ //next city to be visited


        for (count = 0; count < NStrings; count++) {
            initial[count] = EndPoint[count][1];//arc dest
            terminal[count] = EndPoint[count][0];//arc start
        }

        prox_index = (int) (Math.random() * NStrings);
        currentcity = terminal[prox_index];
        startcity = initial[prox_index];
        initial[prox_index] = -1;

        count = 0;
        while (count < NStrings)        /* while there are still unvisited cities */ {
            count++;
            if (count == NStrings)
                proximo = startcity;
            else {
                prox_index = acha_no_patch(NStrings, terminal[prox_index], initial, inst); //
                proximo = initial[prox_index];
                initial[prox_index] = -1;/*marca cidade visitada*/
            }
            arcArray[currentcity].tip = proximo;
            arcArray[proximo].from = currentcity;
            currentcity = terminal[prox_index];
        }
    }


    /* ------------------------------------ acha_no ------------------------------------*/
    /* finds the nearest unvisited/permitted neighbour */
    //note that there is no tiebreaking
    private int acha_no_patch(int NStrings, int city, int initial[], GraphInstance inst) {
        int index_neigh = 0, i;
        double menor = Double.MAX_VALUE;

        for (i = 0; i < NStrings; i++) /*percorre a matrix*/ {
            if (initial[i] >= 0)    /*se cidade ainda nao visitada*/ {
                if (inst.matDist[city][initial[i]] < menor) {
                    index_neigh = i;
                    menor = inst.matDist[city][initial[i]];
                }
            }
        }

        return (index_neigh);/*retorna vizinho mais proximo ao parametro recebido ->partida*/
    }


    /* ------------------------------------ Insert_in_Child ------------------------------------*/
    public void insertInChild(int city, int index, int xyPosition) {
        if (xyPosition == 0)//insere City no ChildDiCycle[index].From
        {
            arcArray[index].from = city;
            arcArray[city].tip = index;
        } else { //insere City no ChildDiCycle[index].Tip
            arcArray[index].tip = city;
            arcArray[city].from = index;
        }
    }


    /* ------------------------------------ Fill_EndPoint ------------------------------------*/

    /**
     * Seems to be a (possibly greedy) infill? (still not sure what motivated it being called "endpoint"
     * @param endPoint
     * @param stringNumber
     * @return
     */
    public int fillEndPoint(int endPoint[][], int stringNumber) {
        int i, j, index;
        boolean mark[] = new boolean[size];

        //for each unmarked city without an incoming, find the first unmarked city without an outgoing and connect them; mark both, then find the next pair (if any) that meet those conditions, until none remain.
        for (i = 0; i < size; i++) {
            //only consider unmakred cities (those that haven't been modified yet in this function)
            if (mark[i] == false) {
                //if this city has no incoming?
                if (arcArray[i].from == -1) {
                    mark[i] = true;
                    //set this as the destination of a new arc?
                    endPoint[stringNumber][1] = i;

                    //find the first city with no outgoing
                    j = i;
                    while (arcArray[j].tip != -1) {
                        j = arcArray[j].tip;
                        mark[j] = true;
                        //make this the from of the new arc?
                    }
                    endPoint[stringNumber][0] = j;
                    stringNumber++;
                    arcArray[i].from = j;
                    arcArray[j].tip = i;
                }
            }
        }

        /*o trecho abaixo identifica os subtour formados sem serem strings!*/
        //in other words, this adds all the cities which were already connected (before the above loop) to the endpoint.
        for (i = 0; i < size; i++) {
            if (mark[i] == false) {
                index = i;
                mark[i] = true;
                do {
                    index = arcArray[index].tip;
                    mark[index] = true;
                } while (arcArray[index].tip != i);
                endPoint[stringNumber][0] = index;
                endPoint[stringNumber][1] = i;
                stringNumber++;
            }
        }

        return (stringNumber);
    }


    /**
     * It gives a new neighbor for a city and does the appropriate patching to eliminate strings.
     *
     * @param city:int  City that will get a neighbor.
     * @param neigh:int Neighbor of a city.
     */
    public void makeCityInsertion(int city, int neigh) {
        int prevNeigh, nextNeigh, nextCity;

        prevNeigh = arcArray[neigh].from;
        nextNeigh = arcArray[neigh].tip;
        nextCity = arcArray[city].tip;

        //Creates an arc from city to neigh

        arcArray[city].tip = neigh;
        arcArray[neigh].from = city;

        //Does the appropriate patching to eliminate strings
        arcArray[neigh].tip = nextCity;
        arcArray[nextCity].from = neigh;

        arcArray[prevNeigh].tip = nextNeigh;
        arcArray[nextNeigh].from = prevNeigh;
    }

    /**
     * <p>    It emulates an arc insertion from city to neigh, an arc deletion,
     * and the appropriate patching.</p>
     * <p>    It returns the new cost if it is smaller than current cost;
     * otherwise, it returns the current cost.</p>
     *
     * @param inst:ATSPRTInstance Instance of the problem so that the cost can be calculated
     * @param arcOut:Arc          Arc to be removed.
     * @param city:int            New arc�s starting point.
     * @param neigh:int           New arc�s ending point.
     */
//Works only for ATSPRT because it uses ready Time. It needs modification for other instances
    public double emulateChange(ATSPRTInstance inst, Arc arcOut, int city, int neigh) {
        double stTime;   //Auxiliary start time for cities
        double newCost;
        int prevCity, nextCity;


//   prevCity = city;
//   nextCity = neigh;

        //Checks if the neighbor is the starting city.  If it is then it needs to calculate
        //all the dicycle
//   if(neigh == startingCity)
//   {
        nextCity = prevCity = startingCity;

        if (nextCity == arcOut.from)
            nextCity = arcArray[city].tip;
        else if (arcArray[nextCity].tip == neigh)
            nextCity = arcOut.tip;
        else if (nextCity == city)
            nextCity = neigh;
        else
            nextCity = arcArray[nextCity].tip;

//      nextCity = arcArray[startingCity].tip;
//   }else if(arcOut.from == startingCity){
//     prevCity = startingCity;
//     nextCity = arcArray[city].tip;
//   }

        newCost = startTime[prevCity];

        //Calculates the cost from neigh to startCity or all the dicycle
        while (newCost < cost) {
            newCost = Math.max(newCost + inst.matDist[prevCity][nextCity], inst.readyTime[nextCity]);

            if (nextCity == startingCity)
                break;

            prevCity = nextCity;

            if (nextCity == arcOut.from)
                nextCity = arcArray[city].tip;
            else if (arcArray[nextCity].tip == neigh)
                nextCity = arcOut.tip;
            else if (nextCity == city)
                nextCity = neigh;
            else
                nextCity = arcArray[nextCity].tip;
        }

        return Math.min(newCost, cost);
    }

    /**
     * It inserts an arc from city to neigh, removes an arc a1, and do the appropriate patching.
     *
     * @param inst:GraphInstance Instance of the problem.
     * @param arcOut:Arc         Arc to be removed.
     * @param city:int           New arc�s starting point.
     * @param neigh:int          New arc�s ending point.
     */
    public void changeDiCycle(GraphInstance inst, Arc arcOut, int city, int neigh) {
        /*Arco.From aponta para City.Tip*/
        arcArray[arcOut.from].tip = arcArray[city].tip;
        arcArray[arcArray[city].tip].from = arcOut.from;

        /*Neigh.From aponta para Arco.Tip*/
        arcArray[arcArray[neigh].from].tip = arcOut.tip;
        arcArray[arcOut.tip].from = arcArray[neigh].from;

        /*City aponta para Neigh*/
        arcArray[city].tip = neigh;
        arcArray[neigh].from = city;

        if (inst.graphType == GraphInstance.ATSP_TYPE &&
                ((ATSPInstance) inst).subproblemType == ATSPInstance.ATSP_RT_TYPE) {
//       if(arcOut.from == startingCity || neigh == startingCity)
            calculateCost(inst);
//       else
//         updateCost(inst,city);
        }
    }

    public void printDiCycle() {
        int nextCity = startingCity;

        do {
            System.out.print(nextCity + " ");
            nextCity = arcArray[nextCity].tip;
        } while (nextCity != startingCity);
        System.out.println("= " + cost);
    }

    public void writeDiCycle(DataOutputStream fileOut) throws Exception {
        int nextCity = startingCity;

        do {
            fileOut.writeBytes(String.valueOf(nextCity + " "));
            nextCity = arcArray[nextCity].tip;
        } while (nextCity != startingCity);
        fileOut.writeBytes(String.valueOf("= " + cost + '\n'));
    }

}