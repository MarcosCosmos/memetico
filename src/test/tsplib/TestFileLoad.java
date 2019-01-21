package tsplib;

import org.jorlib.io.tspLibReader.TSPInstance;

import java.io.File;
import java.io.IOException;

public class TestFileLoad {
    public static void main(String[] args) throws IOException {
        TSPInstance instance = new TSPInstance();
        instance.load(new File("/home/marcos/Documents/git/ma4astp_base/resources/p43.atsp"));

        TSPInstance a280 = new TSPInstance();
        a280.load(new File("/home/marcos/Downloads/ALL_tsp/a280.tsp"));
        a280.addTour(new File("/home/marcos/Downloads/ALL_tsp/a280.opt.tour"));
        int done = 0;
    }
}
