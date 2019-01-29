package memetico;

class CrossoverSAX extends DiCycleCrossover {

    /* ------------------------------------ base.CrossoverSAX ------------------------------------*/
    public void runCrossover(SolutionStructure parentA, SolutionStructure parentB, SolutionStructure child, Instance inst) throws Exception {
        GraphInstance graphInst = (GraphInstance) inst;
        int stringNumber = 0, i, arcMap[][];         /* data structure useful for the recombination */
        int endPoint[][] = new int[graphInst.dimension][2];         /* just in case ?? */
        DiCycle childDiCycle = (DiCycle) child;

        arcMap = new int[graphInst.dimension][6];

        createArcMap((DiCycle) parentA, (DiCycle) parentB, arcMap, graphInst);

        stringNumber = createStrings(childDiCycle, endPoint, arcMap, graphInst);

        if (stringNumber > 1)
            childDiCycle.nextNeighborPatching(stringNumber, endPoint, graphInst);
    }


    /* ------------------------------------ CreateArcMap ----------------------*/
    private void createArcMap(DiCycle parentA, DiCycle parentB, int arcMap[][], GraphInstance inst) {
        int i, neigh1, neigh2, cont_neighbs;
        boolean is_the_same = false;

/*   if (base.Instance.problem==0) cont_neighs = 4;
   else */
        cont_neighbs = 2;

        for (i = 0; i < inst.dimension; i++)
            for (neigh1 = 0; neigh1 < 6; neigh1++)
                arcMap[i][neigh1] = 0;

        for (i = 0; i < inst.dimension; i++) {
            neigh1 = parentA.arcArray[i].tip;
            neigh2 = parentB.arcArray[i].tip;

            // adds the information regarding neigh1 and neigh2 in the ArcMap
            if (neigh1 == neigh2) {
                arcMap[i][0] = neigh1;
                arcMap[i][cont_neighbs] = 1;
                is_the_same = true;
            } else {
                arcMap[i][0] = neigh1;
                arcMap[i][1] = neigh2;
                arcMap[i][cont_neighbs] = 2;
            }

            //TSP Simetrico
/*      if (base.Instance.problem==0){
	 int neigh3, neigh4;
	 neigh3 = ParentA.ArcArray[i].From;
	 neigh4 = ParentB.ArcArray[i].From;
		 CreateArcMap_STSP(is_the_same, i, neigh3, neigh4, ParentA, ParentB, ArcMap);
	  }*/
            //TSP Assimetrico
            /*else*/
            createArcMapATSP(i, neigh1, neigh2, parentA, parentB, arcMap);
        }//for
    }


    /* ------------------------------------ CreateStrings ------------------------------------*/
    /* Cria n substrings e as coloca todas em ChildDiCycle. Pode ocorrer substring de apenas 1   */
    /* no, apenas um arco, ou maiores	                                                  */
    /* ---------------------------------------------------------------------------------------*/
    private int createStrings(DiCycle childDiCycle, int endPoint[][], int arcMap[][], GraphInstance inst) throws Exception {
        int i, stringNumber = 0, endCity = 0, startCity = 0, citiesRemaining, nextCity;
        int aux[] = new int[inst.dimension];

        citiesRemaining = inst.dimension;
        for (i = 0; i < inst.dimension; i++) aux[i] = i;

        try {
            while (citiesRemaining > 0) {
                startCity = aux[(int) (Math.random() * citiesRemaining)];
                removeCity(aux, citiesRemaining, startCity);
                removeArcMapCity(startCity, arcMap, inst);
                citiesRemaining--;
                stringNumber++;
                endCity = startCity;

                /*Cria o string considerando os sucessores*/
                while (arcMap[endCity][2] > 0) {
                    nextCity = arcMap[endCity][(int) (Math.random() * arcMap[endCity][2])];
                    removeArcMapCity(nextCity, arcMap, inst);
                    removeCity(aux, citiesRemaining, nextCity);
                    citiesRemaining--;
                    assignArc(childDiCycle, endCity, nextCity);
                    endCity = nextCity;
                }/*while*/

                /*Cria o string considerando os antecessores*/
/*        while (arcMap[startCity][5]>0){
           nextCity = arcMap[startCity][3 + (int)(Math.random()*arcMap[startCity][5])];
           removeArcMapCity(nextCity, arcMap, inst);
           removeCity(aux, citiesRemaining, nextCity);
           citiesRemaining--;
           assignArc(childDiCycle, nextCity, startCity);
           startCity = nextCity;
        }*/

                endPoint[stringNumber - 1][0] = endCity;
                endPoint[stringNumber - 1][1] = startCity;
                assignArc(childDiCycle, endCity, startCity);
            }
            /*Marca as cidades que s�o extremos de Strings como false*/

            for (i = 0; i < stringNumber; i++) {
                childDiCycle.dontlook[endPoint[i][0]] = false;
                childDiCycle.dontlook[endPoint[i][1]] = false;
            }
        } catch (Exception e) {
            throw e;
        }

        return (stringNumber);
    }

    /* ------------------------------------ Retira_City_ArcMap ------------------------------------*/
    private void removeArcMapCity(int city, int arcMap[][], GraphInstance inst) throws Exception {
        int i;
   /*Apos ter selecionado a proxima cidade a entrar na rota,
	 retira-se esta da lista dos seus dois vizinhos e do no de partida */
        try {
            if (inst.graphType == inst.TSP_TYPE)/*TSP*/ {
                for (i = 0; i < arcMap[city][4]; i++)
                    extractNeighbour(arcMap[city][i], city, 0, 4, arcMap);

                arcMap[city][4] = 0;
            } else if (inst.graphType == inst.ATSP_TYPE) {
                for (i = 0; i < arcMap[city][5]; i++)
                    extractNeighbour(arcMap[city][i + 3], city, 0, 2, arcMap);

                arcMap[city][5] = 0;

                for (i = 0; i < arcMap[city][2]; i++)
                    extractNeighbour(arcMap[city][i], city, 3, 5, arcMap);
            }
        } catch (Exception e) {
            throw e;
        }
    }


    /* ------------------------------------ AssignArc ------------------------------------*/
    private void assignArc(DiCycle tour, int city1, int city2) {
        /* If one  is not "empty" (0), assumes that the other one is */
        tour.arcArray[city1].tip = city2;
        tour.arcArray[city2].from = city1;
    }


    /* ------------------------------------ Eliminar ------------------------------------*/
    private void removeCity(int[] aux, int remaining, int city) throws Exception {
        int i = 0;

        while (i < remaining && city != aux[i])
            i++;

        if (i < remaining) {
            for (++i; i < remaining; i++)
                aux[i - 1] = aux[i];
        } else {
            throw new Exception("Err - Invalid City to be removed");
        }
    }


    /* ------------------------------------ ExtractNeighbour ------------------------------------*/
    private void extractNeighbour(int city, int neighbour, int first, int last, int arcMap[][]) throws Exception {
        int i = first;
        try {

            while (i < (arcMap[city][last] + first) && neighbour != arcMap[city][i])
                i++;

            for (++i; i < (arcMap[city][last] + first); i++)
                arcMap[city][i - 1] = arcMap[city][i];

            arcMap[city][last]--;

        } catch (Exception e) {
            throw e;
        }
    }


    /* ------------------------------------ CreateArcMap_ATSP ------------------------------------*/
    private void createArcMapATSP(int i, int neigh1, int neigh2, DiCycle parentA, DiCycle parentB, int arcMap[][])
    /*Preenche o ArcMap do problema Assimetrico que e' preenchida diferente do Ssimetrico*/ {
   /*esse if torna o preenchimento mais rapido se neigh1=neigh2, e isto acontece
     com frequencia*/
        if (neigh1 == neigh2) {
            arcMap[neigh1][3] = i;//tirar funciona???
            arcMap[neigh1][5] = 1;
      /*se uma cidade tem os mesmos predecessores e sucessores nos dois tours
        da recombinacao, entao este e marcado para true*/
            if (arcMap[neigh1][0] < 0)
                parentA.dontlook[i] = parentB.dontlook[i] = true;
        } else {

            /*Insere i como antecessor neigh1 i no ArcMap*/
            arcMap[neigh1][arcMap[neigh1][5] + 3] = i;
            arcMap[neigh1][5]++;
            /*Insere i como antecessor neigh2 i no ArcMap*/
            arcMap[neigh2][arcMap[neigh2][5] + 3] = i;
            arcMap[neigh2][5]++;
        }
    }


    /* ------------------------------------ CreateArcMap_STSP ------------------------------------*/
    /*Preenche o ArcMap do problema Simetrico que e' preenchida diferente do Assimetrico*/
    private void createArcMapSTSP(boolean is_the_same, int i, int neigh3, int neigh4, DiCycle parentA, DiCycle parentB, int arcMap[][]) {
        int place;                       /*primeira posicao de i vazia no ArcMap*/

        if (is_the_same)
            place = 1;
        else
            place = 2;

        if (neigh3 == neigh4) {
            if (is_the_same)
                parentA.dontlook[i] = parentB.dontlook[i] = true;
            arcMap[i][place] = neigh3;
            arcMap[i][4]++;
        } else {
            arcMap[i][place] = neigh3;
            arcMap[i][place + 1] = neigh4;
            arcMap[i][4] += 2;
        }
    }


    /* ------------------------------------ Select_City ------------------------------------*/
    private int selectCity(boolean which, int currentCity, int arcMap[][], GraphInstance inst) {
        if (inst.graphType == inst.TSP_TYPE)//TSP
        {
            if (arcMap[currentCity][4] == 2) {

                if (which)
                    return (arcMap[currentCity][0]);
                else
                    return (arcMap[currentCity][1]);

            } else if (arcMap[currentCity][4] == 4) {

                if (which)
                    return (arcMap[currentCity][(int) (Math.random() * 2)]);
                else
                    return (arcMap[currentCity][(int) (Math.random() * 2) + 2]);

            } else {

                if (arcMap[currentCity][0] < 0)
                    return (arcMap[currentCity][0]);
                else
                    return (arcMap[currentCity][2]);

            }
        } else if (inst.graphType == inst.ATSP_TYPE) {

            if (arcMap[currentCity][2] == 1)
                return (arcMap[currentCity][0]);
            else {
                if (which)
                    return (arcMap[currentCity][0]);
                else
                    return (arcMap[currentCity][1]);
            }
        } else {
            return -1;
        }

    }


    /* ------------------------------------ Select_NextCity ------------------------------------*/
    private int selectNextCity(int currentCity, int arcMap[][], GraphInstance inst) {
        int min = 5;               /* There are at most 4 neighbours for each city */
        int valor_aux = 0, numMinus, i;
        int candList[] = new int[4], numElemCandList = 0;

   /*Seleciona-se os nos que aparecem na lista de vizinhos de CurrentCity,
     em pelo menos uma das rotas pais*/
        for (i = 0; i < arcMap[currentCity][4]; i++) {
            valor_aux = arcMap[currentCity][i];
            if (arcMap[valor_aux][4] == min) {
                candList[numElemCandList] = valor_aux;
                numElemCandList++;
            } else {
                if (arcMap[valor_aux][4] < min) {
                    numElemCandList = 1;
                    candList[0] = valor_aux;
                    min = arcMap[valor_aux][4];
                }
            }
        } /*for*/
   /*Seleciona-se aleatoriamente um dos nos que aparece na lista de
	 vizinhos de CurrentCity dos dois pais. Se nenhum no aparece*/
   /*na lista de vizinhos dos dois pais, seleciona-se um entre os nos
     que sao vizinhos em pelo menos um pai*/
        /* CandList is ready to be used. It has "posible" neighbours */
        if (inst.graphType == inst.TSP_TYPE) {
            if (numElemCandList > 1) {
                numMinus = 0;
       /*Se tiver pelo menos um no' com valor negativo, ou seja, aparece como vizinho
         nas duas rotas pais, o proximo a entrar sera selecionado entre os nos negativos*/
                for (i = 0; i < numElemCandList; i++) {
                    /*Testa-se o ArcMap e nao CandList, por causa da troca do NCities pelo 0*/
                    if (arcMap[currentCity][i] < 0) {
                        candList[numMinus] = candList[i];
                        numMinus++;
                    }
                }
                if (numMinus > 0)
                    numElemCandList = numMinus;
            }/*if*/
        }
        return (candList[(int) (Math.random() * numElemCandList)]);
    }

}//end of SAX class
