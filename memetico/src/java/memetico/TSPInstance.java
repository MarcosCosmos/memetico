package memetico;/*imports for IO*/

import java.io.StreamTokenizer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;


public class TSPInstance extends GraphInstance {

    public TSPInstance() {
        super.graphType = super.TSP_TYPE;
    }
    /* ------------------------------------ readTSPInstance ------------------------------------*/

    /**
     * The readTSPInstance() reads the filehead of the FileName sent.
     * After, the readDatas() is called to read the Datas to matrix distance.
     * The information about StreamTokenizer was get from http://java.sun.com/products/jdk/1.0.2/api/
     *
     * @param FileName FileName has the name of the TSP file.
     */
    public void readInstance(String FileName) throws Exception {
        int token;
   /*If the file is stored as a matrix file, the Wformat has the information
     about wthat type of matrix it is.*/
        String WFormat = new String("");
   /* WType WType informs what type of file it is: a matrix or a coordinates file.
      If it is a coordinate file, WType informas what the type of distance should
      be calculated.*/
        String WType = new String("");

        try {
            InputStream input = new FileInputStream(FileName);
            StreamTokenizer stokTSP = new StreamTokenizer(input);

            /*It Resets the syntax*/
            stokTSP.resetSyntax();
            /*It sets all the chars as "word chars"*/
            stokTSP.wordChars(" ".charAt(0), "z".charAt(0));
            /*It sets the char " " as the only tokens separator*/
            stokTSP.whitespaceChars(" ".charAt(0), " ".charAt(0));
            /*It sets the end of line as a word separtor too*/
            stokTSP.eolIsSignificant(true)
            /*It reads a number with point and less sinal*/;
            stokTSP.parseNumbers();

            token = stokTSP.nextToken();            /*It gets the next token...*/

            do {
                /* The loop if read the TSPhead.*/
                if (token == stokTSP.TT_WORD) {
                    if (stokTSP.sval.equals("DIMENSION") || stokTSP.sval.equals("DIMENSION:")) {
                        while (token != stokTSP.TT_NUMBER) token = stokTSP.nextToken();
                        super.setDimension((int) stokTSP.nval);
                    } else if (stokTSP.sval.equals("EDGE_WEIGHT_TYPE") ||
                            stokTSP.sval.equals("EDGE_WEIGHT_TYPE:")) {

                        //put out the two points
                        if (stokTSP.sval.equals("EDGE_WEIGHT_TYPE"))
                            token = stokTSP.nextToken();

                        token = stokTSP.nextToken();
                        WType = stokTSP.sval;
                    } else if (stokTSP.sval.equals("EDGE_WEIGHT_FORMAT") ||
                            stokTSP.sval.equals("EDGE_WEIGHT_FORMAT:")) {

                        if (stokTSP.sval.equals("EDGE_WEIGHT_FORMAT"))
                            token = stokTSP.nextToken(); //put out the two points
                        token = stokTSP.nextToken();
                        WFormat = stokTSP.sval;
                    }

                    while (token != stokTSP.TT_EOL) token = stokTSP.nextToken();

                    token = stokTSP.nextToken(); /*gets the first token from the new line*/
                }
            } while (token != stokTSP.TT_NUMBER);

            /*Alloc the MatrixInstance.Mat lenght and sent to read the Datas from the TSP file.*/

            readDatas(WType, WFormat, stokTSP);
            input.close();
        } catch (IOException e) {
            throw new Exception("File not properly opened" + e.toString());
        }
    }


    /* ------------------------------------ readDatas ------------------------------------*/

    /**
     * readDatas() method identify if the file is stored as a coordenates or
     * a matrix file.
     * If it is a Matrix file, the method readMatrix() is called; if it is a
     * Coordinate file, the methods readCoord() and calculateDistance() are called.
     * This method is auxiliar to readTSPInstance() method.
     *
     * @param WType   WType informs what type of file it is: a matrix or a
     *                coordinates file.
     *                If it is a coordinate file, WType informas what the type of distance
     *                should be calculated.
     * @param WFormat If the file is stored as a matrix file, the Wformat has the
     *                information about what type of matrix it is.
     * @param stokTSP stokTSP has the reference to TSP file.
     */
    private void readDatas(String WType, String WFormat, StreamTokenizer stokTSP) throws Exception {
        int NodeCoorType = 0;

        if (WType.equals("EUC_2D") || WType.equals("MAX_2D") || WType.equals("MAN_2D")
                || WType.equals("CEIL_2D") || WType.equals("GEO") || WType.equals("ATT")) {
            NodeCoorType = 2;
        } else if (WType.equals("EUC_3D") || WType.equals("MAX_3D") ||
                WType.equals("MAN_3D")) {
            NodeCoorType = 3;
        }

        if (NodeCoorType != 0) {
            double Position[][] = new double[super.dimension][NodeCoorType];

            readCoord(NodeCoorType, stokTSP, Position);
            calculateDistance(WType, Position);
        } else if (WType.equals("EXPLICIT") || WType.equals("")) {
            readMatrix(WFormat, stokTSP);
        }
    }


    /* ------------------------------------ readCoord ------------------------------------*/

    /**
     * readCoord() reads the coordenates from the TSP file.
     * This method is auxiliar to readDatas() method.
     * The information about to read cientific notation in Java was get from
     * http://www2.rpa.net/~tfarnum/getDoubles.java
     *
     * @param NodeCoorType NodeCoorType informs if the file coordenates are a 2D or
     *                     a 3D.
     * @param stokTSP      stokTSP has the reference to TSP file.
     * @param Position[][] Position stores the coordenates from cities.
     */
    private void readCoord(int NodeCoorType, StreamTokenizer stokTSP, double Position[][]) throws Exception {
        int token, j;
        double tempdbl = Double.POSITIVE_INFINITY;

        try {

            stokTSP.resetSyntax();
            stokTSP.whitespaceChars("\t".charAt(0), " ".charAt(0));
            stokTSP.wordChars("-".charAt(0), "d".charAt(0)); //'e' and '+' are special chars
            stokTSP.wordChars("f".charAt(0), "z".charAt(0)); //so we can handle them explicitly
            stokTSP.parseNumbers();

            for (int i = 0; i < super.dimension; i++) {

                token = stokTSP.nextToken(); //put out the indice from the line

                for (j = 0; j < NodeCoorType; j++) {
                    tempdbl = stokTSP.nval;

                    token = stokTSP.nextToken();        //get next token to check for
                    if (token == "e".charAt(0))          //scientific notation
                    {
                        token = stokTSP.nextToken();

                        if (token == "+".charAt(0))
                            token = stokTSP.nextToken(); //number parser doesn't handle plus

                        if (token == StreamTokenizer.TT_NUMBER) {
                            Position[i][j] = tempdbl * Math.pow(10, stokTSP.nval);
                            token = stokTSP.nextToken();

                        } else
                            stokTSP.pushBack();

                    } else {
                        stokTSP.pushBack();
                        Position[i][j] = tempdbl;
                        token = stokTSP.nextToken();
                    }
                }

                while (token != stokTSP.TT_EOL)
                    token = stokTSP.nextToken();

                token = stokTSP.nextToken();
            }
        } catch (IOException e) {
            throw new Exception("File not opened properly" + e.toString());
        }
    }


    /* ------------------------------------ calculateDistance ------------------------------------*/

    /**
     * calculateDistance() calculates the distance store in the Position vetor.
     * The type of distance is informed for the Type variable.
     * This method is auxiliar to readDatas() method.
     * The kind of distances are: ATT, CEIL_2D, EUC_2D, EUC_3D, GEO, MAN_2D, MAN_3D,
     * MAX_2D and the MAN_3D.
     *
     * @param Type         Type store the type of distance to be calculate.
     * @param Position[][] Position stores the coordenates from cities.
     */
    private void calculateDistance(String Type, double Position[][]) {
        int i, j;
        double delta_x, delta_y, delta_z, rij;
        long tij;

        if (Type.equals("ATT")) /*The distance is calculated as a Att distance.*/ {
            for (i = 0; i < super.dimension; i++) {
                super.matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    delta_x = Position[i][0] - Position[j][0];
                    delta_y = Position[i][1] - Position[j][1];
                    rij = Math.sqrt((delta_x * delta_x + delta_y * delta_y) / 10);
                    tij = (long) rij;

                    if (tij < rij)
                        super.matDist[i][j] = tij + 1;
                    else
                        super.matDist[i][j] = tij;

                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        } else if (Type.equals("CEIL_2D")) {
            for (i = 0; i < super.dimension; i++) {
                super.matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    delta_x = Position[i][0] - Position[j][0];
                    delta_y = Position[i][1] - Position[j][1];
                    super.matDist[i][j] = Math.ceil(Math.sqrt(Math.pow(delta_x, 2) +
                            Math.pow(delta_y, 2)) + 0.5);
                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        } else if (Type.equals("EUC_2D") || Type.equals("EUC_3D")) {//ok
            for (i = 0; i < super.dimension; i++) {
                super.matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    delta_x = Position[i][0] - Position[j][0];
                    delta_y = Position[i][1] - Position[j][1];

                    if (Type.equals("EUC_3D")) {
                        delta_z = Math.abs(Position[i][2] - Position[j][2]);

                        super.matDist[i][j] = Math.sqrt(Math.pow(delta_x, 2) +
                                Math.pow(delta_y, 2) +
                                Math.pow(delta_z, 2)) + 0.5;
                    } else
                        super.matDist[i][j] = Math.sqrt(Math.pow(delta_x, 2) +
                                Math.pow(delta_y, 2)) + 0.5;
                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        } else if (Type.equals("GEO")) {
            double PI = 3.141592, RRR = 6378.388, min, q1, q2, q3;
            long deg;
            double latitude[] = new double[super.dimension];
            double longitude[] = new double[super.dimension];

            for (i = 0; i < super.dimension; i++) {
                deg = (long) Position[i][0];
                min = Position[i][0] - deg;
                latitude[i] = PI * (deg + 5 * min / 3) / 180;
                deg = (long) Position[i][1];
                min = Position[i][1] - deg;
                longitude[i] = PI * (deg + 5 * min / 3) / 180;
            }

            for (i = 0; i < super.dimension; i++) {
                matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    q1 = Math.cos(longitude[i] - longitude[j]);
                    q2 = Math.cos(latitude[i] - latitude[j]);
                    q3 = Math.cos(latitude[i] + latitude[j]);
                    super.matDist[i][j] = RRR * Math.acos(0.5 * ((1 + q1) * q2 - (1 - q1) * q3)) + 1;
                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        } else if (Type.equals("MAN_2D") || Type.equals("MAN_3D")) {
            for (i = 0; i < super.dimension; i++) {
                super.matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    delta_x = Math.abs(Position[i][0] - Position[j][0]);
                    delta_y = Math.abs(Position[i][1] - Position[j][1]);

                    if (Type.equals("MAN_3D")) {
                        delta_z = Math.abs(Position[i][2] - Position[j][2]);
                        super.matDist[i][j] = delta_x + delta_y + delta_z + 0.5;
                    } else
                        super.matDist[i][j] = delta_x + delta_y + 0.5;

                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        } else if (Type.equals("MAX_2D") || Type.equals("MAX_3D")) {
            for (i = 0; i < super.dimension; i++) {
                super.matDist[i][i] = 0;
                for (j = i + 1; j < super.dimension; j++) {
                    delta_x = Math.abs(Position[i][0] - Position[j][0]);
                    delta_y = Math.abs(Position[i][1] - Position[j][1]);

                    if (Type.equals("MAX_3D")) {
                        delta_z = Math.abs(Position[i][2] - Position[j][2]);
                        super.matDist[i][j] = Math.max((long) (delta_x + 0.5),
                                (long) (delta_y + 0.5));
                        super.matDist[i][j] = Math.max(super.matDist[i][j],
                                (long) (delta_z + 0.5));
                    } else
                        super.matDist[i][j] = Math.max((long) (delta_x + 0.5),
                                (long) (delta_y + 0.5));
                    super.matDist[j][i] = super.matDist[i][j];
                }
            }
        }
    }


    /* ------------------------------------ readMatrix ------------------------------------*/

    /**
     * readMatrix() method reads a file stored as a matrix. The matrix can have 9 formats.
     * The kind of matrix are: FULL_MATRIX, UPPER_ROW, LOWER_ROW, UPPER_DIAG_ROW,
     * LOWER_DIAG_ROW, UPPER_COL, LOWER_COL, UPPER_DIAG_COL and the LOWER_DIAG_COL.
     * This method is auxiliar to readDatas() method.
     *
     * @param WFormat If the file is stored as a matrix file, the Wformat has the
     *                information about what type of matrix it is.
     * @param stokTSP stokTSP has the reference to TSP file.
     */
    private void readMatrix(String WFormat, StreamTokenizer stokTSP) throws Exception {
        int i, j, token;

        try {
            if (WFormat.equals("FULL_MATRIX")) {
                for (i = 0; i < super.dimension; i++) {
                    for (j = 0; j < super.dimension; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("UPPER_ROW")) {
                for (i = 0; i < super.dimension - 1; i++) {
                    for (j = (i + 1); j < super.dimension; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("LOWER_ROW")) {
                for (i = 1; i < super.dimension; i++) {
                    for (j = 0; j < i; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("UPPER_DIAG_ROW")) {
                for (i = 0; i < super.dimension; i++) {
                    for (j = i; j < super.dimension; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("LOWER_DIAG_ROW")) {
                for (i = 0; i < super.dimension; i++) {
                    for (j = 0; j <= i; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("UPPER_COL")) {
                for (i = 1; i < super.dimension; i++) {
                    for (j = 0; j < i; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("LOWER_COL")) {
                for (i = 0; i < super.dimension - 1; i++) {
                    for (j = (i + 1); j < super.dimension; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("UPPER_DIAG_COL")) {
                for (i = 0; i < super.dimension; i++) {
                    for (j = 0; j <= i; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            } else if (WFormat.equals("LOWER_DIAG_COL")) {
                for (i = 0; i < super.dimension; i++) {
                    for (j = i; j < super.dimension; j++) {
                        super.matDist[j][i] = stokTSP.nval;
                        super.matDist[i][j] = stokTSP.nval;

                        token = stokTSP.nextToken();

                        if (token == stokTSP.TT_EOL)
                            token = stokTSP.nextToken();
                    }
                }
            }
        } catch (IOException e) {
            throw new Exception("File not opened properly" + e.toString());
        }
    }


}/*end of base.Instance class*/