package memetico.lkh;

import memetico.*;
import org.jorlib.io.tspLibReader.TSPLibTour;

import java.io.*;
import java.net.URL;

/**
 * Requires that an LHK executable is available via the PATH env var
 */
public class LocalSearchLKH extends DiCycleLocalSearchOperator {
	//these are /all/ necessary - in case the problem doesn't already exist on disk.
	private File problemFile;
	private File paramFile;
	private File initialTourFile;
	private File resultTourFile;
	public void LocalSearchLHK(URL problemResource) throws IOException {
		problemFile = File.createTempFile("currentProblem", "tsp");
		//todo: use guava's bytestreams.copy to transfer the problem data to the temp file
		paramFile = File.createTempFile("lhkConfig", "par");
		initialTourFile = File.createTempFile("initialSolution", "tsp");
		resultTourFile = File.createTempFile("resultSolution", "tsp");

		//make sure they are deleted when the jvm closes
		problemFile.deleteOnExit();
		paramFile.deleteOnExit();
		initialTourFile.deleteOnExit();
		resultTourFile.deleteOnExit();

	}
	public void runLocalSearch(SolutionStructure soln, Instance inst) {
		DiCycle destination = (DiCycle) soln;
		try {
			destination.saveInOptTour(initialTourFile.getPath());
			PrintWriter paramOutlet = new PrintWriter(new FileOutputStream(paramFile));
			paramOutlet.printf("PROBLEM_FILE = %s%n", problemFile.getPath());
			paramOutlet.printf("INITIAL_TOUR_FILE = %s%n", initialTourFile.getPath());
			paramOutlet.printf("OUTPUT_TOUR_FILE = %s%n", resultTourFile.getPath());

			Runtime r = Runtime.getRuntime();
			Process p = r.exec(new String[]{"LKH", paramFile.getPath()});
			p.waitFor();

			TSPLibTour tmp = new TSPLibTour();
			tmp.load(new BufferedReader(new FileReader(resultTourFile)));
			for (int i = 0; i < tmp.size(); i++) {
				int cur=tmp.get(i);
				int next=tmp.get((i+1)%tmp.size()); //this % means that at the end, "next" will return to the start of the tour
				destination.arcArray[cur].tip = next;
				destination.arcArray[next].from = cur;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}