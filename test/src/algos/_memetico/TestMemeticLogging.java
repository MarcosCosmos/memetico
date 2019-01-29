package algos._memetico;

import memetico.Memetico;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.ILogger;

import java.io.OutputStream;
import java.io.PrintStream;

public class TestMemeticLogging {
    public static void main(String[] args) throws InterruptedException {
        PCLogger logger = new PCLogger();
        Thread theThread = new Thread(() -> Memetico.main(logger, args));
        theThread.start();
        ILogger.View<PCAlgorithmState> view = logger.newView();
        PrintStream originalStdout = System.out;
        System.setOut(new PrintStream(new OutputStream() {
            public void write(int b) {
                //DO NOTHING
            }
        }));

        int counted = 0;
        while (true) {
            view.update();
            if(counted < view.size()) {
                for(PCAlgorithmState each : view.subList(counted, view.size())) {
                    originalStdout.println(each.generation);
                }
//                counted = view.size();
//                originalStdout.println(counted);
                counted = view.size();
            }
        }
    }
}
