package algos._memetico;

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Vector;
import memetico.Memetico;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.ILogger;

public class TestMemeticLogging {
    public static void main(String[] args) throws InterruptedException {
        PCLogger logger = new PCLogger();
        ILogger.View<PCAlgorithmState> view = logger.newView();
        Thread theThread = new Thread(() -> Memetico.main(logger, args));
        theThread.start();
        int counted = 0;
        while (true) {
            view.update();
            if(counted < view.size()) {
                counted = view.size();
                System.out.println(counted);
            }
        }
    }
}
