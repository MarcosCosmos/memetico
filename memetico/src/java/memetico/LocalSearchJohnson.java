package memetico;

public class LocalSearchJohnson extends DiCycleLocalSearchOperator {

    public MatrixNeigh matNeigh;


    /* ----------------------- construtor ---------------------*/
    public LocalSearchJohnson(Instance inst) {
        matNeigh = new MatrixNeigh((GraphInstance) inst);
    }


    /* --------------------- runLocalSearch --------------------*/
    public void runLocalSearch(SolutionStructure solution, Instance inst) {
        int t1, t2, t3, t4, t5;
        double d1, d2, d3, d4, diff;
        DiCycle tIns = (DiCycle) solution;
        LinkedList lista = new LinkedList();

        for (int j = 0; j < ((GraphInstance) inst).dimension; j++) lista.append(j);
        tIns.cost = tIns.calculateCost(inst);

        while (!lista.isEmpty()) {

            t1 = lista.getFirst();
            lista.getHead().extract();
            t2 = tIns.arcArray[t1].tip;
            d1 = ((GraphInstance) inst).matDist[t1][t2];

            t3 = t3Get(t1, t2, d1, tIns, (GraphInstance) inst);
            if (t3 != -1) {
                t4 = tIns.arcArray[t3].tip;
                d2 = ((GraphInstance) inst).matDist[t2][t3];
                t5 = t5Get(t1, t3, t4, d1, d2, tIns, (GraphInstance) inst);
                if (t5 != -1) {
//	       System.out.println(t1 + " " + t3 + " "+t5);
                    d3 = d1 + ((GraphInstance) inst).matDist[t4][t5];
                    d4 = d2 + ((GraphInstance) inst).matDist[t3][t4];
                    diff = delta(t1, t2, t3, t4, t5, tIns, (GraphInstance) inst);
                    if (diff < 0) {
//	       tIns.printDiCycle();
                        opt3(tIns, t1, t3, t5);
                        tIns.cost += diff;
//	       tIns.printDiCycle();
                        lista.append(t1);
                    }
                }//if (t5 != -1)
            }//if (t3 != -1)
        }//while
    }


    /* ------------------------------------ Recursive base.Arc Insertion ----------------------------------*/
    private int t3Get(int t1, int t2, double d1, DiCycle tIns, GraphInstance inst) {
        int t3;
        double d2;

        for (int i = 0; i < matNeigh.nrNeighbors; i++) {
            t3 = matNeigh.neighbors[t2][MatrixNeigh.SUCCESSOR][i];
            d2 = inst.matDist[t2][t3];
            if (d2 < d1 && t3 != t1 && tIns.arcArray[t3].tip != t1) return t3;
        }
        return (-1);
    }


    /* ------------------------------------ Recursive base.Arc Insertion ----------------------------------*/
    private int t5Get(int t1, int t3, int t4, double d1, double d2, DiCycle tIns, GraphInstance inst) {
        int t5;
        double d3, d4;

        for (int i = 0; i < matNeigh.nrNeighbors; i++) {
            t5 = matNeigh.neighbors[t4][MatrixNeigh.SUCCESSOR][i];
            if (t5 == t4) System.out.println("Cidades : " + t4 + " " + t5);
//       System.out.println(t5);
            d3 = inst.matDist[t3][t4];
            d4 = inst.matDist[t4][t5];
            if ((d2 + d4) > (d1 + d3)) {
                int t = t4;
                while (t != t5 && t != t1) t = tIns.arcArray[t].tip;
                if (t != t1) return t5;
            }
        }
        return (-1);
    }


    /* ------------------------------------ Recursive base.Arc Insertion ----------------------------------*/
    private double delta(int t1, int t2, int t3, int t4, int t5, DiCycle tIns, GraphInstance inst) {
        return (-inst.matDist[t1][t2]
                - inst.matDist[t3][t4]
                - inst.matDist[t5][tIns.arcArray[t5].tip]

                + inst.matDist[t1][t4]
                + inst.matDist[t5][t2]
                + inst.matDist[t3][tIns.arcArray[t5].tip]
        );
    }


    /* ------------------------------------ opt3 ------------------------------------*/
    private void opt3(DiCycle tIns, int i, int j, int r) {
        int iTip = tIns.arcArray[i].tip;
        int jTip = tIns.arcArray[j].tip;
        int rTip = tIns.arcArray[r].tip;

        tIns.arcArray[i].tip = jTip;
        tIns.arcArray[jTip].from = i;

        tIns.arcArray[j].tip = rTip;
        tIns.arcArray[rTip].from = j;

        tIns.arcArray[r].tip = iTip;
        tIns.arcArray[iTip].from = r;
    }


}//fim da classe
