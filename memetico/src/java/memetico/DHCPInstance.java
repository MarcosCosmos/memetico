package memetico;/*imports for IO*/

import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;


public class DHCPInstance extends GraphInstance {

    public DHCPInstance() {
        super.graphType = super.DHCP_TYPE;
    }

    /* ------------------------------------ readInstance ------------------------------------*/
    public void readInstance(String FileName) throws Exception {
        int token;
        int i = 0, position;

        try {
            InputStream input = new FileInputStream(FileName);
            StreamTokenizer stokHCP = new StreamTokenizer(input);

            stokHCP.eolIsSignificant(true);            /*It sets the end of line as a word separtor too*/
            stokHCP.parseNumbers();                /*It reads a number with point and less sinal*/

            token = stokHCP.nextToken();            /*It gets the next token...*/
            while (token != stokHCP.TT_NUMBER) {
                if (stokHCP.sval.equals("DIMENSION") || stokHCP.sval.equals("DIMENSION:")) {
                    while (token != stokHCP.TT_NUMBER)
                        token = stokHCP.nextToken();

                    super.setDimension((int) stokHCP.nval);
                }
                while (token != stokHCP.TT_EOL) token = stokHCP.nextToken();
                token = stokHCP.nextToken(); /*gets the first token from the new line*/
            }


            /*Reads the Datas from the HCP file.*/

            do {
//         if (i==base.Instance.Dimension-1) System.out.println((int) stokHCP.nval);
                position = (int) stokHCP.nval - 1;
                token = stokHCP.nextToken();

                super.matDist[position][(int) stokHCP.nval - 1] = 1;
                token = stokHCP.nextToken();
                while (token == stokHCP.TT_EOL)
                    token = stokHCP.nextToken();
                i++;
            } while (stokHCP.nval != -1);

//      System.out.println("I: "+i);
        } catch (IOException e) {
            throw new Exception("File not properly opened" + e.toString());
        }

    }


}/*end of base.HCPInstance class*/