package algos._memetico;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import memetico.*;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.jorlib.io.tspLibReader.TSPLibTour;
import org.jorlib.io.tspLibReader.graph.DistanceTable;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Note importantly that the logview is currently only updated by this main controller; this works well but only under the assumption that all accesses to the log view share a thread (which they under standard javafx which uses a single thread at time of writing)
 */
public class TestVisualisationController implements Initializable {

    //todo: possibly make this a parameter instead
    /**
     * Measured as checks per second
     */
    public static double LOG_POLL_RATE = 60;

    @FXML
    private VBox root;
    @FXML
    private Pane frameContent;
    @FXML
    private Pane playBackControls;

    private transient PCLogger logger;
    private transient BasicLogger<PCAlgorithmState>.View view;
    private transient IntegerProperty numberOfFrames = new SimpleIntegerProperty(0);
    private transient IntegerProperty selectedFrameIndex;
    private transient ObjectProperty<PCAlgorithmState> currentState;
    private final transient Timeline updateCheckingTimeline = new Timeline();

    private Map<String, TSPLibInstance> baseInstances = new HashMap<>();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //todo: perhaps make the type of the logger and frame content to use generic/parameters given to this controller.?
        //set the frame content controller to a new
        try {
            logger = new PCLogger(10);

//            PrintStream originalStdout = System.out;
//            System.setOut(new PrintStream(new OutputStream() {
//                public void write(int b) {
//                    //DO NOTHING
//                }
//            }));

            Thread theThread = new Thread(() -> {
                String MetodoConstrutivo = "Nearest Neighbour";
                String BuscaLocal = "Recursive base.Arc Insertion";//Recursive base.Arc Insertion";
                String SingleorDouble = "Double";
                String OPCrossover = "Strategic base.Arc Crossover - SAX",
                        OPReStart = "base.RestartInsertion",
                        OPMutacao = "base.MutationInsertion";
                String structSol = "base.DiCycle";
                String structPop = "Ternary Tree";
                long MaxTime = 100, MaxGenNum;
                int PopSize = 13, mutationRate = 5;


                try {
                    File probFile = new File(getClass().getClassLoader().getResource("att532.tsp").getFile());

                    TSPLibInstance tspLibInstance = new TSPLibInstance(probFile);

                    baseInstances.put(tspLibInstance.getName(), tspLibInstance);

                    Instance memeticoInstance;

                    switch (tspLibInstance.getDataType()) {
//                        case TSP:
//                            memeticoInstance = new TSPInstance();
//                            break;
                        case ATSP:
                        default:
                            //atsp should be safe (ish) even if it is in fact tsp
                            memeticoInstance = new ATSPInstance();
                    }

                    //give the memeticoInstance the required data
                    memeticoInstance.setDimension(tspLibInstance.getDimension());
                    {
                        DistanceTable distanceTable = tspLibInstance.getDistanceTable();
                        double[][] memeticoMat = ((GraphInstance) memeticoInstance).getMatDist();
                        for(int i=0; i<memeticoInstance.getDimension(); ++i) {
                            for(int k=0; k<memeticoInstance.getDimension(); ++k) {
                                memeticoMat[i][k] = distanceTable.getDistanceBetween(i,k);
                            }
                        }
                    }

                    MaxGenNum = (int) (5 * 13 * Math.log(13) * Math.sqrt(((GraphInstance) memeticoInstance).getDimension()));

                    FileOutputStream dataOut = null;
                    dataOut = new FileOutputStream("result.txt");
                    DataOutputStream fileOut = new DataOutputStream(dataOut);

                    FileOutputStream compact_dataOut = new FileOutputStream("result_fim.txt");
                    DataOutputStream compact_fileOut = new DataOutputStream(compact_dataOut);

                    long targetCost; //for letting the solver know when it's found the optimal (if known)
                    //a280: this one has a tour
//                    {
//                        File tourFile = new File(getClass().getClassLoader().getResource("a280.opt.tour").getFile());
//                        tspLibInstance.addTour(tourFile);
//                        targetCost = (long) tspLibInstance.getTours().get(0).distance(tspLibInstance);
////                        tspLibInstance.getTours().clear();
//                    }
                    //att532: this one just has known cost
                    targetCost = 27686;

                    Memetico meme = new Memetico(logger, memeticoInstance, structSol, structPop, MetodoConstrutivo,
                            PopSize, mutationRate, BuscaLocal, OPCrossover, OPReStart, OPMutacao,
                            MaxTime, MaxGenNum, tspLibInstance.getName(), targetCost, fileOut,
                            compact_fileOut);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            theThread.start();

            view = logger.newView();

            currentState = new SimpleObjectProperty<>();
            //setup the playback controls, giving them an observable reference to the total frame count.
            FXMLLoader loader = new FXMLLoader(getClass().getResource("test_playback_controls.fxml"));
            Pane playbackControls = loader.load();
            root.getChildren().add(playbackControls);
            TestPlaybackController playbackController = loader.<TestPlaybackController>getController();
            playbackController.setup(numberOfFrames);

            //bind the current state to the selected index given by the playback controller.
            selectedFrameIndex = playbackController.frameIndexProperty();
            currentState.bind(
                    Bindings.createObjectBinding(
                            () -> view.isEmpty() ? null : view.get(selectedFrameIndex.get()),
                            selectedFrameIndex
                    )
            );


            //set up the content display with an observable reference to the current state to display.
            loader = new FXMLLoader(
                    getClass().getResource(
                            "test_pc_frame_content.fxml"
                    )
            );
            Pane content = loader.load();
            root.getChildren().add(0, content);
            VBox.setVgrow(content, Priority.ALWAYS);
            loader.<TestPCFrameController>getController().setup(baseInstances, currentState);

            //setup a timeline to poll for log updates, and update the number of frames accordingly
            updateCheckingTimeline.getKeyFrames().add(
                    new KeyFrame(Duration.millis(100/LOG_POLL_RATE), (event) -> {
                        try {
                            view.update();
                            numberOfFrames.set(view.size());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    })
            );
            updateCheckingTimeline.setCycleCount(Animation.INDEFINITE);
            updateCheckingTimeline.play();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
