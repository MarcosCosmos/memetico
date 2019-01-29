package memetico;

public class RestartInsertion extends DiCycleRestartOperators {

    /*  VARIAVEIS CONSTANTES  */
    private int CURRENT_TO_POCKET = 30; /* Maximum number of times that a Pocket solution can be without being updated    */
    private ConstructionAlgorithms ca;

    /* ------------------------------------ setConstAlg ------------------------------------*/
    public void setConstAlg(ConstructionAlgorithms ConstAlg) {
        ca = ConstAlg;
    }

    /* ------------------------------------ Restart ------------------------------------*/
    public void runRestart(int GenNum, Instance inst, Population memePop, int maxGenNum) throws Exception {
        PocCurAgent pocCurPop[] = (PocCurAgent[]) memePop.pop; //?
        GraphInstance graphInst = (GraphInstance) inst;
        int City, Neigh, j;
//   FileOutputStream dataOut = new FileOutputStream("restartdebug.txt");
//   DataOutputStream afileOut = new DataOutputStream (dataOut);


//    afileOut.writeBytes(String.valueOf("Restart : "+'\n'));

        //muta o current raiz
        if (pocCurPop[0].noChangeCounter % CURRENT_TO_POCKET == 0) {
//      afileOut.writeBytes(String.valueOf("Raiz Restart Cur_Pocket: "+'\n'));

            City = (int) (Math.random() * graphInst.dimension);
            Neigh = (int) (Math.random() * graphInst.dimension);
            while (Neigh == City || ((DiCycle) pocCurPop[0].current).arcArray[City].from == Neigh ||
                    ((DiCycle) pocCurPop[0].current).arcArray[City].tip == Neigh) {
                Neigh++;
                if (Neigh == graphInst.dimension)
                    Neigh = 0;
            }
//      afileOut.writeBytes(String.valueOf("Before RS : "));
//      ((base.DiCycle)pocCurPop[0].current).writeDiCycle(afileOut);

            ((DiCycle) pocCurPop[0].current).makeCityInsertion(City, Neigh);
            pocCurPop[0].current.calculateCost(inst);

//      afileOut.writeBytes(String.valueOf("After RS : "));
//      ((base.DiCycle)pocCurPop[0].current).writeDiCycle(afileOut);


        }

        //muta os pockets da sub-populacao i
        for (int i = 1; i < memePop.nrParents; i++) {
            if (pocCurPop[i].noChangeCounter % CURRENT_TO_POCKET == 0) {
//         afileOut.writeBytes(String.valueOf("Outros Restart Cur_Pocket: "+'\n'));
                City = (int) (Math.random() * graphInst.dimension);
                for (j = 0; j < 5; j++) {
                    Neigh = (int) (Math.random() * graphInst.dimension);
                    while (Neigh == City || ((DiCycle) pocCurPop[i].pocket).arcArray[City].from == Neigh
                            || ((DiCycle) pocCurPop[i].pocket).arcArray[City].tip == Neigh) {
                        Neigh++;
                        if (Neigh == graphInst.dimension)
                            Neigh = 0;
                    }
//            afileOut.writeBytes(String.valueOf("Before RS : "));
//           ((base.DiCycle)pocCurPop[i].pocket).writeDiCycle(afileOut);

                    ((DiCycle) pocCurPop[i].pocket).makeCityInsertion(City, Neigh);
                    pocCurPop[i].pocket.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After RS : "));
//            ((base.DiCycle)pocCurPop[i].pocket).writeDiCycle(afileOut);


                    Neigh = (int) (Math.random() * graphInst.dimension);
                    //A cidade a ser inserida apos rand tem que ser diferente de rand e
                    //nao pode ser a sucessora de rand em T
                    while (Neigh == City || ((DiCycle) pocCurPop[i * 3 + 1].pocket).arcArray[City].from == Neigh ||
                            ((DiCycle) pocCurPop[i * 3 + 1].pocket).arcArray[City].tip == Neigh) {
                        Neigh++;
                        if (Neigh == graphInst.dimension) Neigh = 0;
                    }
//            afileOut.writeBytes(String.valueOf("Before RS : "));
//           ((base.DiCycle)pocCurPop[i*3+1].pocket).writeDiCycle(afileOut);

                    ((DiCycle) pocCurPop[i * 3 + 1].pocket).makeCityInsertion(City, Neigh);
                    pocCurPop[i * 3 + 1].pocket.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After RS : "));
//           ((base.DiCycle)pocCurPop[i*3+1].pocket).writeDiCycle(afileOut);

                    Neigh = (int) (Math.random() * graphInst.dimension);
                    //A cidade a ser inserida apos rand tem que ser diferente de rand e
                    //nao pode ser a sucessora de rand em T
                    while (Neigh == City || ((DiCycle) pocCurPop[i * 3 + 2].pocket).arcArray[City].from == Neigh ||
                            ((DiCycle) pocCurPop[i * 3 + 2].pocket).arcArray[City].tip == Neigh) {
                        Neigh++;
                        if (Neigh == graphInst.dimension)
                            Neigh = 0;
                    }
//            afileOut.writeBytes(String.valueOf("Before RS : "));
//           ((base.DiCycle)pocCurPop[i*3+2].pocket).writeDiCycle(afileOut);

                    ((DiCycle) pocCurPop[i * 3 + 2].pocket).makeCityInsertion(City, Neigh);
                    pocCurPop[i * 3 + 2].pocket.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After RS : "));
//           ((base.DiCycle)pocCurPop[i*3+2].pocket).writeDiCycle(afileOut);

                    Neigh = (int) (Math.random() * graphInst.dimension);
                    //A cidade a ser inserida apos rand tem que ser diferente de rand e
                    //nao pode ser a sucessora de rand em T
                    while (Neigh == City || ((DiCycle) pocCurPop[i * 3 + 3].pocket).arcArray[City].from == Neigh ||
                            ((DiCycle) pocCurPop[i * 3 + 3].pocket).arcArray[City].tip == Neigh) {
                        Neigh++;
                        if (Neigh == graphInst.dimension)
                            Neigh = 0;
                    }
//            afileOut.writeBytes(String.valueOf("Before RS : "));
//           ((base.DiCycle)pocCurPop[i*3+3].pocket).writeDiCycle(afileOut);

                    ((DiCycle) pocCurPop[i * 3 + 3].pocket).makeCityInsertion(City, Neigh);
                    pocCurPop[i * 3 + 3].pocket.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After RS : "));
//           ((base.DiCycle)pocCurPop[i*3+3].pocket).writeDiCycle(afileOut);

                    City++;
                    if (City >= graphInst.dimension)
                        City -= graphInst.dimension;
                }

                //retira os tres pockets do meio
                if (GenNum % 300 == 0) {
//            afileOut.writeBytes(String.valueOf("Restart 300 iter: "+'\n'));

                    int last3[] = new int[3], city = (int) (Math.random() * graphInst.dimension);

                    last3 = ca.runConstrAlg(pocCurPop[1].pocket, city, inst);
                    pocCurPop[1].pocket.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After Construction P1 "+city+" : "));
//            ((base.DiCycle)pocCurPop[1].pocket).writeDiCycle(afileOut);

                    last3 = ca.runConstrAlg(pocCurPop[2].current, last3[0], inst);
                    pocCurPop[2].current.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After Construction C2 "+last3[0]+" : "));
//            ((base.DiCycle)pocCurPop[2].current).writeDiCycle(afileOut);

                    last3 = ca.runConstrAlg(pocCurPop[3].current, last3[1], inst);
                    pocCurPop[3].current.calculateCost(inst);

//            afileOut.writeBytes(String.valueOf("After Construction C3 "+last3[1]+ " : "));
//            ((base.DiCycle)pocCurPop[3].current).writeDiCycle(afileOut);
                }
            }
        }
//   afileOut.close();
    }

}//fim da classe
