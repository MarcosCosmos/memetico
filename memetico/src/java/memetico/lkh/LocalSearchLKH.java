package memetico.lkh;

import com.google.common.io.ByteStreams;
import com.sun.istack.internal.NotNull;
import memetico.*;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

/**
 * Requires that an LHK executable is available via the PATH env var
 */
public class LocalSearchLKH extends DiCycleLocalSearchOperator {
    //these are /all/ necessary - in case the problem doesn't already exist on disk.
    private File problemFile;
    private File paramFile;
    private File initialTourFile;
    private File resultTourFile;

    public LocalSearchLKH(@NotNull URL problemResource) throws IOException {
        problemFile = File.createTempFile("currentProblem", ".tsp");
        ByteStreams.copy(problemResource.openStream(), new FileOutputStream(problemFile));
        paramFile = File.createTempFile("lhkConfig", ".par");
        initialTourFile = File.createTempFile("initialSolution", ".tsp");
        resultTourFile = File.createTempFile("resultSolution", ".tsp");
        PrintWriter paramOutlet = new PrintWriter(new FileOutputStream(paramFile));
        paramOutlet.printf("PROBLEM_FILE = %s%n", problemFile.getPath());
        paramOutlet.printf("INITIAL_TOUR_FILE = %s%n", initialTourFile.getPath());
        paramOutlet.printf("OUTPUT_TOUR_FILE = %s%n", resultTourFile.getPath());
        paramOutlet.println("RUNS = 1");
        paramOutlet.close();

        //make sure they are deleted when the jvm closes
        problemFile.deleteOnExit();
        paramFile.deleteOnExit();
        initialTourFile.deleteOnExit();
        resultTourFile.deleteOnExit();

    }

    public void runLocalSearch(SolutionStructure soln, Instance inst) {
        DiCycle destination = (DiCycle) soln;
        try {
            destination.saveInOptTour(initialTourFile);
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(new String[]{"LKH", paramFile.getPath()});
            p.waitFor();

            TSPLibInstance tmpInst = new TSPLibInstance(resultTourFile);
            TSPLibTour theTour = tmpInst.getTours().get(0);
            for (int i = 0; i < theTour.size(); i++) {
                int cur = theTour.get(i);
                int next = theTour.get((i + 1) % theTour.size()); //this % means that at the end, "next" will return to the start of the tour
                destination.arcArray[cur].tip = next;
                destination.arcArray[next].from = cur;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}