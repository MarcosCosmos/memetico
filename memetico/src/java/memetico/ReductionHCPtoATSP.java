package memetico;

public class ReductionHCPtoATSP extends Reduction {

    /* ------------------------------------ runReduction ------------------------------------*/
    public Instance runReduction(Instance inst) {
        int i, j;
        ATSPInstance temp = new ATSPInstance();
        HCPInstance hcpInst = (HCPInstance) inst;

        temp.problemType = inst.GRAPH_TYPE;
        temp.graphType = GraphInstance.ATSP_TYPE;
        temp.setDimension(hcpInst.dimension);


/*   for(int i=0; i<base.Instance.Dimension; i++){
      for (j=0; j<base.Instance.Dimension; j++){
	 if (i == j) matDist.setValue(i, j, 0);
	 else if(matDist.getValue(i, j) == 1) matDist.setValue(j, i, 1);

      }
   }
*/
        for (i = 0; i < hcpInst.dimension; i++) {
            for (j = 0; j < hcpInst.dimension; j++) {
                if (hcpInst.matDist[i][j] == 0 && i != j) {
                    temp.matDist[i][j] = 2;
                    temp.matDist[j][i] = 2;
                } else {
                    temp.matDist[i][j] = hcpInst.matDist[i][j];
                }
            }
        }

        return temp;
    }


}