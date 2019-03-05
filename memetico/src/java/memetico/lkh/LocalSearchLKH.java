package memetico.lkh;

import com.google.common.io.ByteStreams;
import com.sun.istack.internal.NotNull;
import memetico.*;
import org.apache.commons.lang3.SystemUtils;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;

import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Scanner;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Requires that an LHK executable is available via the PATH env var
 */
public class LocalSearchLKH extends DiCycleLocalSearchOperator {
    //these are /all/ necessary - in case the problem doesn't already exist on disk.
    private File problemFile;
    private File paramFile;
    private File initialTourFile;
    private File resultTourFile;
    private File candidateFile;
    private File piFile;
//    private String baseParams;
//
    public LocalSearchLKH(@NotNull URL problemResource) throws IOException {
        problemFile = File.createTempFile("currentProblem", ".tsp");
        InputStream tmpInput = problemResource.openStream();
        OutputStream tmpOutput = new FileOutputStream(problemFile);
        ByteStreams.copy(tmpInput, tmpOutput);
        tmpInput.close();
        tmpOutput.close();
        paramFile = File.createTempFile("lhkConfig", ".par");
        initialTourFile = File.createTempFile("initialSolution", ".tsp");
        resultTourFile = File.createTempFile("resultSolution", ".tsp");
        candidateFile = new File(String.format("candidates%s.lkhdat", UUID.randomUUID()));
        piFile = new File(String.format("pi%s.lkhdat", UUID.randomUUID()));
//        StringWriter tmpWriter = new StringWriter();
        PrintWriter paramOutlet = new PrintWriter(new FileWriter(paramFile));
        paramOutlet.printf("PROBLEM_FILE = %s%n", problemFile.getPath());
        paramOutlet.printf("INITIAL_TOUR_FILE = %s%n", initialTourFile.getPath());
        paramOutlet.printf("TOUR_FILE = %s%n", resultTourFile.getPath());
        paramOutlet.printf("CANDIDATE_FILE = %s%n", candidateFile.getPath());
        paramOutlet.printf("PI_FILE = %s%n", piFile.getPath());
        paramOutlet.printf("MAX_TRIALS = %s%n", 1);
        paramOutlet.println("RUNS = 1");
//        paramOutlet.println("STOP_AT_OPTIMUM = YES");
//        paramOutlet.print("OPTIMUM = ");
        paramOutlet.close();
//        baseParams = tmpWriter.toString();

        //make sure they are deleted when the jvm closes
        problemFile.deleteOnExit();
        paramFile.deleteOnExit();
        initialTourFile.deleteOnExit();
        resultTourFile.deleteOnExit();
        candidateFile.deleteOnExit();
        piFile.deleteOnExit();
    }

    public void runLocalSearch(SolutionStructure soln, Instance inst) {
        DiCycle destination = (DiCycle) soln;
        try {
            destination.saveInOptTour(initialTourFile);
            Runtime r = Runtime.getRuntime();
            Process p = r.exec(new String[]{"LKH", paramFile.getPath()});
            p.waitFor();
            if(p.exitValue() != 0) {
                System.err.println("Error: LKH Failed - printing stdout and stderr outputs..");
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
                String eachLine = reader.readLine();
                while (eachLine != null) {
                    System.out.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
                reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                eachLine = reader.readLine();
                while (eachLine != null) {
                    System.err.println(eachLine);
                    eachLine = reader.readLine();
                }
                reader.close();
            }

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

    /**
     * Identifies whether or not the LKH executable is available in the PATH (and therefore whether or not this heuristic can be used)
     * Curtesy of https://stackoverflow.com/a/23539220
     * @return
     */
    public static boolean isAvailable() {
        String exec = SystemUtils.IS_OS_WINDOWS ? "LKH.exe" : "LKH";
        return Stream.of(System.getenv("PATH").split(Pattern.quote(File.pathSeparator)))
                .map(Paths::get)
                .anyMatch(path -> Files.exists(path.resolve(exec)));
    }
}