package memetico;

public class LocalSearch3opt extends DiCycleLocalSearchOperator {


    public void runLocalSearch(SolutionStructure solution, Instance inst) {
        DiCycle tIns = (DiCycle) solution;
        int i, j, r, initialCity;
        double delta1, delta2;

        initialCity = (int) (Math.random() * inst.dimension);
        tIns.cost = tIns.calculateCost(inst);

        i = initialCity;
        while (tIns.arcArray[i].tip != initialCity) {
            j = tIns.arcArray[i].tip;
            while (tIns.arcArray[j].tip != initialCity) {
                r = tIns.arcArray[j].tip;
                delta1 = calculateDelta1(tIns, i, j, (GraphInstance) inst);
                while (r != initialCity) {
                    delta2 = calculateDelta2(tIns, i, j, r, (GraphInstance) inst);
                    if ((delta1 + delta2) < 0) {
                        opt3(tIns, i, j, r);
                        tIns.cost += (delta1 + delta2);
//		  tIns.printDiCycle();
                        initialCity = i;
                        j = r = tIns.arcArray[i].tip;
                        delta1 = calculateDelta1(tIns, i, j, (GraphInstance) inst);
                    }
                    r = tIns.arcArray[r].tip;
                }
                j = tIns.arcArray[j].tip;
            }
            i = tIns.arcArray[i].tip;
        }
    }


    /* ------------------------------------ Recursive base.Arc Insertion ----------------------------------*/
    private double calculateDelta1(DiCycle tIns, int i, int j, GraphInstance inst) {
        return (+inst.matDist[i][tIns.arcArray[j].tip]
                - inst.matDist[i][tIns.arcArray[i].tip]
                - inst.matDist[j][tIns.arcArray[j].tip]);
    }


    /* ------------------------------------ calculateDelta2 ------------------------------------*/
    private double calculateDelta2(DiCycle tIns, int i, int j, int r, GraphInstance inst) {
        return (+inst.matDist[j][tIns.arcArray[r].tip]
                + inst.matDist[r][tIns.arcArray[i].tip]
                - inst.matDist[r][tIns.arcArray[r].tip]);
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
