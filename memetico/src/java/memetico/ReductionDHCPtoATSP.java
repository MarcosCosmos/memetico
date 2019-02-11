package memetico;

public class ReductionDHCPtoATSP extends Reduction {

    /* ------------------------------------ runReduction ------------------------------------*/
    public Instance runReduction(Instance inst) {
        int i, j;
        ATSPInstance temp = new ATSPInstance();
        DHCPInstance dhcpInst = (DHCPInstance) inst;

        temp.problemType = Instance.GRAPH_TYPE;
        temp.graphType = GraphInstance.ATSP_TYPE;
        temp.setDimension(dhcpInst.dimension);

        for (i = 0; i < dhcpInst.dimension; i++) {
            for (j = 0; j < dhcpInst.dimension; j++) {
                if (dhcpInst.matDist[i][j] == 0 && i != j) {
                    temp.matDist[i][j] = 2;
                    temp.matDist[j][i] = 2;
                } else {
                    temp.matDist[i][j] = dhcpInst.matDist[i][j];
                }
            }
        }

        return temp;
    }


}/*end of Instance class*/