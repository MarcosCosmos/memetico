package memetico;

public class RestartCut extends DiCycleRestartOperators {

    /*  VARIAVEIS CONSTANTES  */
//private int CURRENT_TO_POCKET = 10;
    private ConstructionAlgorithms ca;

    static int maxRestart = 0;

    /* ------------------------------------ setConstAlg ------------------------------------*/
    public void setConstAlg(ConstructionAlgorithms ConstAlg) {
        ca = ConstAlg;
    }

    /* ------------------------------------ Restart ------------------------------------*/
    public void runRestart(int GenNum, Instance inst, Population memePop, int maxGen) throws Exception {
        PocCurAgent pocCurPop[] = (PocCurAgent[]) memePop.pop; //?
        if (memePop.newBestSol % 20 == 0 && maxRestart < 10) {
            memePop.newBestSol = 1;
            maxRestart++;
//System.out.println("Restart na geracao " +GenNum +" " +memePop.newBestSol);

            /* Troca os pockets*/
            ca.runConstrAlg(pocCurPop[1].pocket, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[1].pocket.calculateCost(inst);

            ca.runConstrAlg(pocCurPop[2].pocket, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[2].pocket.calculateCost(inst);

            ca.runConstrAlg(pocCurPop[3].pocket, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[3].pocket.calculateCost(inst);

            /* Troca os currents*/

            ca.runConstrAlg(pocCurPop[1].current, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[1].current.calculateCost(inst);

            ca.runConstrAlg(pocCurPop[2].current, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[2].current.calculateCost(inst);

            ca.runConstrAlg(pocCurPop[3].current, (int) (Math.random() * inst.dimension), inst);
            pocCurPop[3].current.calculateCost(inst);

            memePop.pop[1].calculateCost(inst);
            memePop.pop[2].calculateCost(inst);
            memePop.pop[3].calculateCost(inst);


        }
    }

}//fim da classe
