package memetico;

public class MutationInsertion extends DiCycleMutationOperators {

    /* ------------------------------------ base.MutationInsertion -----------------*/
    public void runMutation(SolutionStructure child) {
        int i, j, rCity1, rCity2 = 0, aux = 0;
        DiCycle childDiCycle = (DiCycle) child;


        /*Cada Current tem 5% de chances de ser mutada*/
        if (Math.random() < (mutationRate / 100)) {
            rCity1 = (int) (Math.random() * childDiCycle.size);

            /* log(childDiCycle.size)cities will be mutated*/
            for (i = 0; i < Math.log(childDiCycle.size); i++) {
                rCity2 = (int) (Math.random() * childDiCycle.size);
                while (rCity2 == rCity1 || childDiCycle.arcArray[rCity1].from == rCity2 ||
                        childDiCycle.arcArray[rCity1].tip == rCity2) {
                    // A cidade a ser inserida apos rand tem que ser
                    // diferente de rand e nao pode ser a sucessora de rand em T
                    rCity2++;
                    if (rCity2 == childDiCycle.size)
                        rCity2 = 0;
                }
                childDiCycle.dontlook[childDiCycle.arcArray[rCity2].from] = false;
                childDiCycle.dontlook[childDiCycle.arcArray[rCity2].tip] = false;
                childDiCycle.makeCityInsertion(rCity1, rCity2);
                childDiCycle.dontlook[childDiCycle.arcArray[rCity2].from] = false;
                childDiCycle.dontlook[childDiCycle.arcArray[rCity2].tip] = false;
                childDiCycle.dontlook[rCity2] = false;

                rCity1++;
                if (rCity1 >= childDiCycle.size)
                    rCity1 -= childDiCycle.size;
            }
        }
    }


}//fim da classe
