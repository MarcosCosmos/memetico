package memetico;

import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;


public class ATSPInstance extends GraphInstance {

    final static int NONE = -1;
    final static int ATSP_RT_TYPE = 0;

    public int subproblemType = NONE;

    public ATSPInstance() {
        super.graphType = super.ATSP_TYPE;
    }
    /* ------------------------------------ readTSPInstance ------------------------------------*/

    /**
     * The readATSPInstance() reads the filehead of the FileName sent.
     * After, the readDatas() is called to read the Datas to matrix distance.
     * The information about StreamTokenizer was get from http://java.sun.com/products/jdk/1.0.2/api/
     *
     * @param FileName FileName has the name of the ATSP file.
     */
    /* ------------------------------------ readATSPInstance ------------------------------------*/
    public void readInstance(String FileName) throws Exception {
        int token;
        int i, j;
//System.out.println(FileName);
        try {
            InputStream input = new FileInputStream(FileName);
            StreamTokenizer stokATSP = new StreamTokenizer(input);

            stokATSP.eolIsSignificant(true);            /*It sets the end of line as a word separtor too*/
            stokATSP.parseNumbers();                /*It reads a number with point and less sinal*/

            token = stokATSP.nextToken();            /*It gets the next token...*/
            while (token != stokATSP.TT_NUMBER) {
                if (stokATSP.sval.equals("DIMENSION") || stokATSP.sval.equals("DIMENSION:")) {
                    while (token != stokATSP.TT_NUMBER)
                        token = stokATSP.nextToken();

                    super.setDimension((int) stokATSP.nval);
                }
                while (token != stokATSP.TT_EOL)
                    token = stokATSP.nextToken();

                token = stokATSP.nextToken(); /*gets the first token from the new line*/
            }

            /*Reads the Datas from the ATSP file.*/

            for (i = 0; i < super.dimension; i++)
                for (j = 0; j < super.dimension; j++) {
                    matDist[i][j] = stokATSP.nval;
                    token = stokATSP.nextToken();
                    while (token == stokATSP.TT_EOL)
                        token = stokATSP.nextToken();
                }

            input.close();
        } catch (IOException e) {
            throw new Exception("File not opened properly" + e.toString());
        }
    }


}/*end of base.Instance class*/