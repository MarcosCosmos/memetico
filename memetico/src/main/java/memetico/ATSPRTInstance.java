package memetico;

import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;


public class ATSPRTInstance extends ATSPInstance {
    //Constants section
    final protected static String READY_TIME = "READY_TIME_SECTION";

    //Variables section
    double readyTime[];


    public ATSPRTInstance() {

        super.subproblemType = super.ATSP_RT_TYPE;

    }
    /* ------------------------------------ readTSPInstance ------------------------------------*/

    /**
     * The readInstance() reads the filehead of the FileName sent.
     * After, the readDatas() is called to read the Datas to matrix distance.
     * The information about StreamTokenizer was get from http://java.sun.com/products/jdk/1.0.2/api/
     *
     * @param FileName FileName has the name of the ATSP RT file.
     */
    /* ------------------------------------ readATSPInstance ------------------------------------*/
    public void readInstance(String FileName) throws Exception {
        int token;
        int i, j;

        try {
            InputStream input = new FileInputStream(FileName);
            StreamTokenizer stok = new StreamTokenizer(input);

            /*It sets all the chars as "word chars"*/
            stok.wordChars(" ".charAt(0), 'z');
            /*It sets the char " " as the only tokens separator*/
            stok.whitespaceChars(" ".charAt(0), " ".charAt(0));
            stok.ordinaryChar(':');
            stok.eolIsSignificant(true);            /*It sets the end of line as a word separtor too*/
            stok.parseNumbers();                /*It reads a number with point and less sinal*/

            token = stok.nextToken();            /*It gets the next token...*/
            //Finds the dimension keyword
            while (true) {
                if (token == stok.TT_WORD && stok.sval.startsWith(DIMENSION))
                    break;
                token = stok.nextToken();
            }

            //Gets the dimension
            if (stok.sval.startsWith(DIMENSION)) {
                while (token != stok.TT_NUMBER) token = stok.nextToken();
                super.setDimension((int) stok.nval);
            }

            //Finds the arc cost section
            while (true) {
                if (token == stok.TT_WORD && stok.sval.startsWith(MATRIX))
                    break;
                token = stok.nextToken();
            }

            //Gets the cost for each arc
            while (token != stok.TT_NUMBER) token = stok.nextToken();

            for (i = 0; i < super.dimension; i++)
                for (j = 0; j < super.dimension; j++) {
                    super.matDist[i][j] = stok.nval;
                    token = stok.nextToken();

                    if (token == stok.TT_EOL)
                        token = stok.nextToken();
                }

            //Finds the ready time section
            while (true) {
                if (token == stok.TT_WORD && stok.sval.startsWith(READY_TIME))
                    break;
                token = stok.nextToken();
            }

            //Gets the ready time for each city
            readyTime = new double[super.dimension];
            while (token != stok.TT_NUMBER) token = stok.nextToken();

            for (i = 0; i < super.dimension; i++) {
                readyTime[i] = (double) stok.nval;
                token = stok.nextToken();

                if (token == stok.TT_EOL)
                    token = stok.nextToken();
            }

            input.close();
        } catch (IOException e) {
            throw new Exception("File not opened properly" + e.toString());
        }
    }


}/*end of Instance class*/