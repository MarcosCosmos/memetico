package memetico;

public class MatrixNeigh {

    final static int SUCCESSOR = 0;
    final static int PREDECESSOR = 1;

    int nrNeighbors = 5,    /* length of a candidate list used for local search */
    /* this should be automatically set from the extension of the file. */
    neighbors[][][];    /*0 -> sucessores e 1->antecessores*/

    /* -------------------------------- Construtor -----------------------------*/
    public MatrixNeigh(GraphInstance inst) {

        if (inst.graphType == inst.ATSP_TYPE) {
            neighbors = new int[inst.dimension][2][nrNeighbors];
        } else if (inst.graphType == inst.TSP_TYPE)
            neighbors = new int[inst.dimension][1][nrNeighbors];

        createNeighMatrix(inst);
    }

    /* ------------------------------------ bubble_sort ------------------------------------*/
    private void sortNeighbors(GraphInstance inst) {
        int i, j, k, aux;

        for (i = 0; i < inst.dimension; i++) {
            //Successor
            for (k = 0; k < nrNeighbors - 1; k++) {
                for (j = k + 1; j < nrNeighbors; j++) {
                    if (inst.matDist[i][neighbors[i][SUCCESSOR][k]] > inst.matDist[i][neighbors[i][SUCCESSOR][j]]) {
                        aux = neighbors[i][SUCCESSOR][k];
                        neighbors[i][SUCCESSOR][k] = neighbors[i][SUCCESSOR][j];
                        neighbors[i][SUCCESSOR][j] = aux;
                    }
                }
                //Predecessor
                if (inst.graphType == inst.ATSP_TYPE) {
                    for (j = k + 1; j < nrNeighbors; j++) {
                        if (inst.matDist[neighbors[i][PREDECESSOR][k]][i] > inst.matDist[neighbors[i][PREDECESSOR][j]][i]) {
                            aux = neighbors[i][PREDECESSOR][k];
                            neighbors[i][PREDECESSOR][k] = neighbors[i][PREDECESSOR][j];
                            neighbors[i][PREDECESSOR][j] = aux;
                        }
                    }
                }
            }
        }
    }

    /**
     * <p> Sort the neighbors for a given city.</p>
     *
     * @param inst The problem instance
     * @param solution       Solution structure that contains the cities.
     * @param city               The city for each the neighbors matrix will sorted.
     */
    private void sortNeighbors(GraphInstance inst, DiCycle solution, int city) {
        int k, j, aux;
        double distance1,
                distance2;

        for (k = 0; k < nrNeighbors - 1; k++) {
            //Sort Successor
            for (j = k + 1; j < nrNeighbors; j++) {
                //Successor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance1 = Math.max(((ATSPRTInstance) inst).readyTime[neighbors[city][SUCCESSOR][k]] -
                                        solution.startTime[city],
                                inst.matDist[city][neighbors[city][SUCCESSOR][k]]);
                        distance2 = Math.max(((ATSPRTInstance) inst).readyTime[neighbors[city][SUCCESSOR][j]] -
                                        solution.startTime[city],
                                inst.matDist[city][neighbors[city][SUCCESSOR][j]]);
                    } else {
                        distance1 = inst.matDist[city][neighbors[city][SUCCESSOR][k]];
                        distance2 = inst.matDist[city][neighbors[city][SUCCESSOR][j]];
                    }
                } else {
                    distance1 = inst.matDist[city][neighbors[city][SUCCESSOR][k]];
                    distance2 = inst.matDist[city][neighbors[city][SUCCESSOR][j]];
                }
                if (distance1 > distance2) {
                    aux = neighbors[city][SUCCESSOR][k];
                    neighbors[city][SUCCESSOR][k] = neighbors[city][SUCCESSOR][j];
                    neighbors[city][SUCCESSOR][j] = aux;
                }
            }
            //Sort Predecessor
            if (inst.graphType == inst.ATSP_TYPE) {
                for (j = k + 1; j < nrNeighbors; j++) {
                    //Predecessor distance
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance1 = Math.max(((ATSPRTInstance) inst).readyTime[neighbors[city][PREDECESSOR][k]] +
                                        inst.matDist[neighbors[city][PREDECESSOR][k]][city] -
                                        ((ATSPRTInstance) inst).readyTime[city],
                                inst.matDist[neighbors[city][PREDECESSOR][k]][city]);
                        distance2 = Math.max(((ATSPRTInstance) inst).readyTime[neighbors[city][PREDECESSOR][j]] +
                                        inst.matDist[neighbors[city][PREDECESSOR][j]][city] -
                                        ((ATSPRTInstance) inst).readyTime[city],
                                inst.matDist[neighbors[city][PREDECESSOR][j]][city]);
                    } else {
                        distance1 = inst.matDist[neighbors[city][PREDECESSOR][k]][city];
                        distance2 = inst.matDist[neighbors[city][PREDECESSOR][j]][city];
                    }

                    if (distance1 > distance2) {
                        aux = neighbors[city][PREDECESSOR][k];
                        neighbors[city][PREDECESSOR][k] = neighbors[city][PREDECESSOR][j];
                        neighbors[city][PREDECESSOR][j] = aux;
                    }
                }
            }
        }
    }

    /**
     * <p> Find the nearest neighbors for a given city in a given solution.</p>
     * <p> It fills the neighbors matrix for that city.</p>
     *
     * @param inst The problem instance
     * @param solution       Solution structure that contains the cities
     * @param city               The city for each the neighbors matrix will be filled with.
     */
    public void fillWithNeighbors(GraphInstance inst, DiCycle solution, int city) {
        int i, j, cont_suc, cont_pred = 0;
        int VizSuc[] = new int[inst.dimension], VizPred[] = null;
        double maior_suc, maior_pred = 0, distance;

        if (inst.graphType == inst.ATSP_TYPE) {
            VizPred = new int[inst.dimension];
        }

        cont_suc = 0;

        for (j = 0; j <= nrNeighbors; j++) {
            if (city != j) {
                VizSuc[cont_suc] = j;
                if (inst.graphType == inst.ATSP_TYPE)
                    VizPred[cont_suc] = j;
                cont_suc++;
            }
        }
        if (inst.graphType == inst.ATSP_TYPE) {
            cont_pred = cont_suc;
            maior_pred = maxNeighbor(PREDECESSOR, solution, city, VizPred, inst);
        }
        maior_suc = maxNeighbor(SUCCESSOR, solution, city, VizSuc, inst);

        for (j = nrNeighbors + 1; j < VizSuc.length; j++) {
            if (city != j) {
                if (inst.graphType == inst.ATSP_TYPE) {
                    //Predecessor distance
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE)
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[j] +
                                        inst.matDist[j][city] -
                                        ((ATSPRTInstance) inst).readyTime[city],
                                inst.matDist[j][city]);
                    else
                        distance = inst.matDist[j][city];

                    if (distance == maior_pred) {
                        VizPred[cont_pred] = j;
                        cont_pred++;
                    } else if (distance < maior_pred) {
                        removeNeighbor(PREDECESSOR, solution, j, city, cont_pred, maior_pred, VizPred, inst);
                        cont_pred = nrNeighbors;
                        maior_pred = maxNeighbor(PREDECESSOR, solution, city, VizPred, inst);
                    }
                }
                //Successor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE)
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[j] -
                                        solution.startTime[city],
                                inst.matDist[city][j]);
                    else
                        distance = inst.matDist[city][j];
                } else
                    distance = inst.matDist[city][j];

                if (distance == maior_suc) {
                    VizSuc[cont_suc] = j;
                    cont_suc++;
                } else if (distance < maior_suc) {
                    removeNeighbor(SUCCESSOR, solution, city, j, cont_suc, maior_suc, VizSuc, inst);
                    cont_suc = nrNeighbors;
                    maior_suc = maxNeighbor(SUCCESSOR, solution, city, VizSuc, inst);
                }
            }//if
        }//for
        if (inst.graphType == inst.ATSP_TYPE) {
            removeNeighbor(PREDECESSOR, solution, -1, city, cont_pred, maior_pred, VizPred, inst);
            for (j = 0; j < nrNeighbors; j++)
                neighbors[city][PREDECESSOR][j] = VizPred[j];
        }
        removeNeighbor(SUCCESSOR, solution, city, -1, cont_suc, maior_suc, VizSuc, inst);
        for (j = 0; j < nrNeighbors; j++)
            neighbors[city][SUCCESSOR][j] = VizSuc[j];

        sortNeighbors(inst, solution, city);
    }

    /* ------------------------------------ Create_neigh_matrix ------------------------------------*/
    private void createNeighMatrix(GraphInstance inst) {
        int i, j, cont_suc, cont_pred = 0;
        int VizSuc[] = new int[inst.dimension], VizPred[] = null;
        double maior_suc, maior_pred = 0;

        if (inst.graphType == inst.ATSP_TYPE) {
            VizPred = new int[inst.dimension];
        }

        for (i = 0; i < inst.dimension; i++) {
            cont_suc = 0;
            for (j = 0; j <= nrNeighbors; j++) {
                if (i != j) {
                    VizSuc[cont_suc] = j;
                    if (inst.graphType == inst.ATSP_TYPE)
                        VizPred[cont_suc] = j;
                    cont_suc++;
                }
            }
            if (inst.graphType == inst.ATSP_TYPE) {
                cont_pred = cont_suc;
                maior_pred = maxNeighbor(PREDECESSOR, i, VizPred, inst);
            }
            maior_suc = maxNeighbor(SUCCESSOR, i, VizSuc, inst);

            for (j = nrNeighbors + 1; j < VizSuc.length; j++) {
                if (i != j) {
                    if (inst.graphType == inst.ATSP_TYPE) {
                        if (inst.matDist[j][i] == maior_pred) {
                            VizPred[cont_pred] = j;
                            cont_pred++;
                        } else if (inst.matDist[j][i] < maior_pred) {
                            removeNeighbor(PREDECESSOR, j, i, cont_pred, maior_pred, VizPred, inst);
                            cont_pred = nrNeighbors;
                            maior_pred = maxNeighbor(PREDECESSOR, i, VizPred, inst);
                        }
                    }

                    if (inst.matDist[i][j] == maior_suc) {
                        VizSuc[cont_suc] = j;
                        cont_suc++;
                    } else if (inst.matDist[i][j] < maior_suc) {
                        removeNeighbor(SUCCESSOR, i, j, cont_suc, maior_suc, VizSuc, inst);
                        cont_suc = nrNeighbors;
                        maior_suc = maxNeighbor(SUCCESSOR, i, VizSuc, inst);
                    }
                }//if
            }//for
            if (inst.graphType == inst.ATSP_TYPE) {
                removeNeighbor(PREDECESSOR, -1, i, cont_pred, maior_pred, VizPred, inst);
                for (j = 0; j < nrNeighbors; j++)
                    neighbors[i][PREDECESSOR][j] = VizPred[j];
            }
            removeNeighbor(SUCCESSOR, i, -1, cont_suc, maior_suc, VizSuc, inst);
            for (j = 0; j < nrNeighbors; j++)
                neighbors[i][SUCCESSOR][j] = VizSuc[j];
        }//for
        sortNeighbors(inst);
    }


    /* ------------------------------------ Max_neigh ------------------------------------*/
    private double maxNeighbor(int Pred_or_Suc, int i, int Vetor[], GraphInstance inst) {
        int j;
        double max = 0;

        if (Pred_or_Suc == SUCCESSOR) {
            for (j = 0; j < nrNeighbors; j++) {
                if (max < inst.matDist[i][Vetor[j]])
                    max = inst.matDist[i][Vetor[j]];
            }
        } else {
            for (j = 0; j < nrNeighbors; j++)
                if (max < inst.matDist[Vetor[j]][i])
                    max = inst.matDist[Vetor[j]][i];
        }

        return (max);
    }

    /**
     * <p> Returns the greatest neighbor in vector.</p>
     *
     * @param Pred_or_Suc           it is as sucessor or predecessor vector.
     * @param solution      Solution structure that contains the cities.
     * @param i            The city that will be used to get the neighbor.
     * @param vector          List of neighbors
     * @param inst The problem instance
     */
    private double maxNeighbor(int Pred_or_Suc, DiCycle solution, int i, int vector[], GraphInstance inst) {
        int j;
        double max = 0;
        double distance;

        if (Pred_or_Suc == SUCCESSOR) {
            for (j = 0; j < nrNeighbors; j++) {
                //Successor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[vector[j]] -
                                        solution.startTime[i],
                                inst.matDist[i][vector[j]]);
                    } else {
                        distance = inst.matDist[i][vector[j]];
                    }
                } else {
                    distance = inst.matDist[i][vector[j]];
                }

                if (max < distance)
                    max = distance;
            }
        } else {
            for (j = 0; j < nrNeighbors; j++) {
                //Predecessor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[vector[j]] +
                                        inst.matDist[vector[j]][i] -
                                        ((ATSPRTInstance) inst).readyTime[i],
                                inst.matDist[vector[j]][i]);
                    } else {
                        distance = inst.matDist[vector[j]][i];
                    }
                } else {
                    distance = inst.matDist[vector[j]][i];
                }

                if (max < distance)
                    max = distance;
            }
        }

        return (max);
    }

    /* ------------------------------------ Retira ------------------------------------*/
    private void removeNeighbor(int Pre_or_Suc, int City_aux1, int City_aux2, int cont, double maior, int Vetor[], GraphInstance inst) {
        int i, cont_aux = cont, aux, aux1, City2 = City_aux2, City1 = City_aux1;

        cont--;
   /*o proximo loop poe todos indices com maior distancia no fim do vetor e
     posiciona cont na posicao anterior ao primeiro maior e i na posicao do primeiro maior*/
        i = 0;
        while (i <= cont) {
            if (Pre_or_Suc == PREDECESSOR)
                City1 = Vetor[i];
            else
                City2 = Vetor[i];

            if (inst.matDist[City1][City2] == maior) {
                aux = Vetor[i];
                Vetor[i] = Vetor[cont];
                Vetor[cont] = aux;
                cont--;
                i--;
            }
            i++;
        }

        cont++;
        while (cont < nrNeighbors) {
            aux1 = (int) (Math.random() * (cont_aux - cont)) + cont;
            aux = Vetor[cont];
            Vetor[cont] = Vetor[aux1];
            Vetor[aux1] = aux;
            cont++;
        }

        if (City_aux2 != -1 && City_aux1 != -1) {
            if (Pre_or_Suc == PREDECESSOR)
                Vetor[nrNeighbors - 1] = City_aux1;
            else
                Vetor[nrNeighbors - 1] = City_aux2;
        }
    }

    /**
     * <p> Remove the greatest cost neighbor in vector as given by cost.</p>
     *
     * @param Pre_or_Suc          it is as sucessor or predecessor vector.
     * @param solution       Solution structure that contains the cities.
     * @param vector         List of neighbors
     * @param inst The problem instance
     */
    private void removeNeighbor(int Pre_or_Suc, DiCycle solution, int City_aux1, int City_aux2, int count, double cost, int vector[], GraphInstance inst) {
        int i, cont_aux = count, aux, aux1, City2 = City_aux2, City1 = City_aux1;
        double distance;

        count--;
   /*o proximo loop poe todos indices com maior distancia no fim do vetor e
     posiciona cont na posicao anterior ao primeiro maior e i na posicao do primeiro maior*/
        i = 0;
        while (i <= count) {
            if (Pre_or_Suc == PREDECESSOR) {
                City1 = vector[i];
                //Predecessor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[City1] +
                                        inst.matDist[City1][City2] -
                                        ((ATSPRTInstance) inst).readyTime[City2],
                                inst.matDist[City1][City2]);
                    } else {
                        distance = inst.matDist[City1][City2];
                    }
                } else {
                    distance = inst.matDist[City1][City2];
                }
            } else {
                City2 = vector[i];
                //Successor distance
                if (inst.graphType == inst.ATSP_TYPE) {
                    if (((ATSPRTInstance) inst).subproblemType == ATSPRTInstance.ATSP_RT_TYPE) {
                        distance = Math.max(((ATSPRTInstance) inst).readyTime[City2] -
                                        solution.startTime[City1],
                                inst.matDist[City1][City2]);
                    } else {
                        distance = inst.matDist[City1][City2];
                    }
                } else {
                    distance = inst.matDist[City1][City2];
                }
            }

            if (distance == cost) {
                aux = vector[i];
                vector[i] = vector[count];
                vector[count] = aux;
                count--;
                i--;
            }
            i++;
        }

        count++;
        while (count < nrNeighbors) {
            aux1 = (int) (Math.random() * (cont_aux - count)) + count;
            aux = vector[count];
            vector[count] = vector[aux1];
            vector[aux1] = aux;
            count++;
        }

        if (City_aux2 != -1 && City_aux1 != -1) {
            if (Pre_or_Suc == PREDECESSOR)
                vector[nrNeighbors - 1] = City_aux1;
            else
                vector[nrNeighbors - 1] = City_aux2;
        }
    }


}//fim da classe
