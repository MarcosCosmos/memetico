package org.marcos.uon.tspaidemo.gui.memetico.options;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import memetico.*;
import memetico.lkh.LocalSearchLKH;
import memetico.logging.IPCLogger;
import memetico.logging.PCLogger;
import memetico.util.CrossoverOpName;
import memetico.util.LocalSearchOpName;
import memetico.util.RestartOpName;
import org.jorlib.io.tspLibReader.TSPLibInstance;
import org.marcos.uon.tspaidemo.gui.memetico.MemeticoConfiguration;
import memetico.util.ProblemConfiguration;
import memetico.util.ProblemInstance;
import org.marcos.uon.tspaidemo.util.log.ValidityFlag;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

//TODO: possiblt separate the problem/evolutionary param config from the display options and possibly create a seperate box for them (which isn't subsiduary to the content controller)
public class RunConfigurationController implements Initializable {
    public static final List<ProblemConfiguration> INCLUDED_PROBLEMS;
    public static final MemeticoConfiguration DEFAULT_CONFIG = new MemeticoConfiguration(13, 5, LocalSearchOpName.RAI.toString(), CrossoverOpName.SAX.toString(), RestartOpName.INSERTION.toString());
    public static final int DEFAULT_LOG_INTERVAL = 1;
    public static final Pattern INTEGER_TEST_REGEX = Pattern.compile("\\d*");
    public static final Pattern INTEGER_REPLACE_REGEX = Pattern.compile("[^\\d]");
    public static final Pattern DOUBLE_TEST_REGEX = Pattern.compile("\\d*|\\d+\\,\\d*");

    static {
        Function<String, ProblemConfiguration> newToured = (filePrefix) -> new ProblemConfiguration(
                RunConfigurationController.class.getResource(String.format("/problems/tsp/%s.tsp", filePrefix)),
                RunConfigurationController.class.getResource(String.format("/problems/tsp/%s.opt.tour", filePrefix))
        );
        BiFunction<String, Long, ProblemConfiguration> newCosted = (filePrefix, cost) -> new ProblemConfiguration(
                RunConfigurationController.class.getResource(String.format("/problems/tsp/%s.tsp", filePrefix)),
                cost
        );
        INCLUDED_PROBLEMS = Arrays.asList(
                newToured.apply("tsp225"),
                newToured.apply("a280"),
                newToured.apply("berlin52"),
                newToured.apply("eil51"),
                newToured.apply("eil76"),
                newToured.apply("att532"),
                newToured.apply("mnpeano_mod_o8_1452")
        );
    }

    /**
     * Detects a change to a field and sets the selected template to custom - with trip-switch awareness
     */
    private class TemplateSelectionListener<T> implements ChangeListener<T> {
        private ValidityFlag templateTripSwitch = masterTemplateTripSwitch;
        @Override
        public void changed(ObservableValue<? extends T> observable, T oldValue, T newValue) {
            if(templateTripSwitch.isValid()) {
                if(!newValue.equals(oldValue)) {
                    choiceMemeticoProblemTemplate.setValue("Custom");
                    templateTripSwitch.invalidate(); //prevent any other attempts at changing the template since it's pointless until the user does
                }
            } else {
                templateTripSwitch = masterTemplateTripSwitch;
            }
        }
    }

    private Stage theStage;

    @FXML
    private ScrollPane memeticoConfigurationBoxRoot;
    @FXML
    private Button btnMemeticoSelectProblem, btnMemeticoSelectTour;
    @FXML
    private CheckBox cbMemeticoIncludeLKH;

    @FXML
    private TextField fldMemeticoTourCost, fldMemeticoPopDepth, fldMemeticoMutRate,fldMemeticoMaxGen,fldMemeticoLogInterval,fldMemeticoReignLimit;
    @FXML
    private ChoiceBox<String> choiceMemeticoProblemTemplate, choiceMemeticoSolutionType, choiceMemeticoLocalSearch, choiceMemeticoCrossover, choiceMemeticoRestart;

    @FXML
    private Label lblMemeticoProblemFile, lblMemeticoTourFile, lblMemeticoTourFileDesc, lblMemeticoTourCost, lblMemeticoIncludeLKH;
    @FXML
    private Text txtMemeticoProblemFileError, txtMemeticoTourFileError, txtMemeticoLogFileError;

    private transient final ReadOnlyObjectWrapper<ProblemInstance> chosenProblemInstance = new ReadOnlyObjectWrapper<>();

    private transient final ReadOnlyObjectWrapper<MemeticoConfiguration> chosenMemeticoConfiguration = new ReadOnlyObjectWrapper<>();

    /**
     * Acts in combination with {@code TemplateSelectionListener}, synonymously to a circuit breaker
     * Used to allow user changes to update the chosen template to custom - but never as a result of changing the template to something other than custom
     * Note that the current master switch could be invalid
     * @see TemplateSelectionListener
     */
    private ValidityFlag masterTemplateTripSwitch = new ValidityFlag();

    private ValidityFlag.Synchronised currentMemeticoContinuePermission;
    private Thread memeticoThread = null;


    //todo: possibly make a static map of the base instances which the non-static map does an addAll() from on init; this would facilitate multiple simultaneous runs/tabs/viewpanes, whatever
    private Map<String, ProblemInstance> instances = new HashMap<>();


    private PCLogger logger = new PCLogger(1);

    //called when the user wants to apply their selected configuration
    private Runnable applyConfigFunc = () -> {};

    public void openProblemSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Problem File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            lblMemeticoProblemFile.setText(selection.getPath());
            //reset the solution information
            lblMemeticoTourFile.setText("");
            fldMemeticoTourCost.setText("");
        }
    }

    public void openTourSelectionDialog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Tour File");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            lblMemeticoTourFile.setText(selection.getPath());
        }
    }

    public void saveLog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Log destination file");
        File selection = fileChooser.showSaveDialog(new Stage());
        if (selection != null) {
            try {
                Writer writer = new FileWriter(selection);
                Gson gson = new Gson();
                JsonObject data = logger.newView().jsonify();
                //note that the logger will ignore any additional fields it doesn't need/recognise in the json so we can add the problem and configuration settings
                data.add("problem", gson.toJsonTree(chosenProblemInstance.get().getConfiguration()));
                data.add("settings", gson.toJsonTree(chosenMemeticoConfiguration.get()));
                gson.toJson(data, writer);
                writer.close();
                txtMemeticoLogFileError.setText("");
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
                txtMemeticoLogFileError.setText("Notice: failed to save the log.");
            }
        }
    }

    public void loadLog() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Log source file");
        File selection = fileChooser.showOpenDialog(new Stage());
        if (selection != null) {
            try {
                ProblemInstance intendedInstance;
                Gson gson = new Gson();
                Reader reader = new FileReader(selection);
                JsonParser parser = new JsonParser();
                JsonObject data = parser.parse(reader).getAsJsonObject();
                currentMemeticoContinuePermission.invalidate();
                logger.loadJson(data);
                if (data.has("problem")) {
                    ProblemConfiguration intendedConfig = gson.fromJson(data.get("problem"), ProblemConfiguration.class);
                    URL problemFile = intendedConfig.problemFile;
                    String problemText = problemFile.getPath();
                    if (problemText.contains("!")) {
                        problemFile = getClass().getResource(problemText.split("!")[1]);
                    } else {
                        problemFile = new File(problemText).toURI().toURL();
                    }
                    if (intendedConfig.solutionType == ProblemConfiguration.SolutionType.TOUR) {
                        URL tourFile;
                        String tourText = intendedConfig.tourFile.getPath();
                        if (tourText.contains("!")) {
                            tourFile = getClass().getResource(tourText.split("!")[1]);
                        } else {
                            tourFile = new File(tourText).toURI().toURL();
                        }
                        intendedConfig = new ProblemConfiguration(problemFile, tourFile);
                    } else {
                        intendedConfig = new ProblemConfiguration(problemFile, intendedConfig.targetCost);
                    }
                    intendedInstance = ProblemInstance.create(intendedConfig);
                    ;
                } else {
                    intendedInstance = chosenProblemInstance.get();
                }
                if (intendedInstance != null) {
                    if (intendedInstance.getTspLibInstance() == null) {
                        //bad instance file; complain
                        txtMemeticoProblemFileError.setText("Notice: could not read the problem file.");
                        txtMemeticoTourFileError.setText("");
                    } else {
                        txtMemeticoProblemFileError.setText("");
                        if (intendedInstance.getTargetCost() < 0) {
                            //bad problem file; complain
                            txtMemeticoTourFileError.setText("Notice: could not read the tour file.");
                        } else {
                            txtMemeticoTourFileError.setText("");
                            chosenProblemInstance.set(intendedInstance);
                            instances.put(chosenProblemInstance.get().getName(), chosenProblemInstance.get());
                        }
                    }
                }
                if (data.has("settings")) {
                    chosenMemeticoConfiguration.set(gson.fromJson(data.get("settings"), MemeticoConfiguration.class));
                }
                txtMemeticoLogFileError.setText("");
            } catch (InterruptedException | FileNotFoundException | MalformedURLException e) {
                e.printStackTrace();
                txtMemeticoLogFileError.setText("Notice: failed to save the tour.");
            }
        }
    }

    public void saveTour() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Tour destination file");
        File selection = fileChooser.showSaveDialog(new Stage());
        if (selection != null) {
            try {
                IPCLogger.View theView = logger.newView();
                List<Integer> tour = theView.get(theView.size()-1).bestSolution.tour;
                PrintWriter writer = new PrintWriter(new FileWriter(selection));
                writer.printf("NAME : %s%n", selection.getName());
                writer.println("TYPE : TOUR");
                writer.printf("DIMENSION : %d%n", tour.size());
                writer.println("TOUR_SECTION");
                for (Integer each : tour) {
                    writer.println(each+1);
                }
                writer.println("-1");
                writer.println("EOF");
                writer.close();
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public ProblemInstance getChosenProblemInstance() {
        return chosenProblemInstance.get();
    }

    public ReadOnlyObjectProperty<ProblemInstance> chosenProblemInstanceProperty() {
        return chosenProblemInstance.getReadOnlyProperty();
    }

//    public void setChosenProblemInstance(ProblemInstance chosenProblemInstance) {
//        this.chosenProblemInstance.set(chosenProblemInstance);
//    }

    public MemeticoConfiguration getChosenMemeticoConfiguration() {
        return chosenMemeticoConfiguration.get();
    }

    public ReadOnlyObjectProperty<MemeticoConfiguration> chosenMemeticoConfigurationProperty() {
        return chosenMemeticoConfiguration;
    }

//    public void setChosenMemeticoConfiguration(MemeticoConfiguration config) {
//        this.chosenMemeticoConfiguration.set(config);
//    }


    private static void attachIntegerFieldFixer(TextField target, long min, long max) {
        target.setTextFormatter(new TextFormatter((UnaryOperator<TextFormatter.Change>) change -> INTEGER_TEST_REGEX.matcher(change.getControlNewText()).matches() ? change : null));
        ChangeListener<String> fixer = (observable, oldValue, newValue) -> {
            long candidateNumber;
            if (!newValue.isEmpty()) {
                candidateNumber = Long.parseLong(newValue);
            } else {
                candidateNumber = min;
            }

            target.setText(String.valueOf(Math.max(min, Math.min(max, candidateNumber))));
        };
        target.textProperty().addListener(fixer);
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
//        memeticoConfigurationBoxRoot.setFitToWidth(true);

        //todo: add an option for changing the log frequency through the UI/clean up save/load buttons
        memeticoConfigurationBoxRoot.getStylesheets().addAll(
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/memetico/options/options_box.css").toExternalForm(),
                getClass().getResource("/fxml/org/marcos/uon/tspaidemo/gui/main/common.css").toExternalForm()
        );

        //update the content of all fields to match the template if one is selected
        choiceMemeticoProblemTemplate.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    masterTemplateTripSwitch.invalidate(); //trip the current switch so that no attempts to change
                    switch (newValue) {
                        case "Custom":
                            break;
                        default:
                            masterTemplateTripSwitch = new ValidityFlag();

                            //now populate the fields
                            ProblemInstance targetInstance = instances.get(newValue);
                            lblMemeticoProblemFile.textProperty().set(targetInstance.getConfiguration().problemFile.getPath());
                            choiceMemeticoSolutionType.valueProperty().set(targetInstance.getConfiguration().solutionType.toString());
                            switch (targetInstance.getConfiguration().solutionType) {
                                case TOUR:
                                    lblMemeticoTourFile.textProperty().set(targetInstance.getConfiguration().tourFile.getPath());
                                    break;
                                case COST:
                                    lblMemeticoTourFile.textProperty().set("");
                                    break;
                            }
                            fldMemeticoTourCost.setText(String.valueOf(targetInstance.getTargetCost()));
                    }
                }
        );

        choiceMemeticoSolutionType.valueProperty().addListener(
                (observable, oldValue, newValue) -> {
                    switch (newValue){
                        case "Tour":
                            lblMemeticoTourFileDesc.setVisible(true);
                            lblMemeticoTourFile.setVisible(true);
                            btnMemeticoSelectTour.setVisible(true);
                            lblMemeticoTourCost.setVisible(false);
                            fldMemeticoTourCost.setVisible(false);
                            break;
                        case "Cost":
                            lblMemeticoTourFileDesc.setVisible(false);
                            lblMemeticoTourFile.setVisible(false);
                            btnMemeticoSelectTour.setVisible(false);
                            lblMemeticoTourCost.setVisible(true);
                            fldMemeticoTourCost.setVisible(true);
                            break;
                    }
                }
        );

        //add the template selection listeners
        lblMemeticoProblemFile.textProperty().addListener(new TemplateSelectionListener<>());
        lblMemeticoTourFile.textProperty().addListener(new TemplateSelectionListener<>());
        fldMemeticoTourCost.textProperty().addListener(new TemplateSelectionListener<>());

        attachIntegerFieldFixer(fldMemeticoTourCost, 0, Long.MAX_VALUE);
        attachIntegerFieldFixer(fldMemeticoPopDepth, 1, Long.MAX_VALUE);
        attachIntegerFieldFixer(fldMemeticoMutRate, 0, 100);
        attachIntegerFieldFixer(fldMemeticoMaxGen, 0, Long.MAX_VALUE);
        attachIntegerFieldFixer(fldMemeticoReignLimit, 0, Long.MAX_VALUE);
        attachIntegerFieldFixer(fldMemeticoLogInterval, 1, Long.MAX_VALUE);



//        choiceMemeticoLocalSearch.getItems().addAll(
//                Arrays.stream(LocalSearchOpName.values())
//                        .map(Object::toString)
//                        .collect(Collectors.toList())
//        );
        List<String> localSearchOptions = choiceMemeticoLocalSearch.getItems();
        localSearchOptions.add(LocalSearchOpName.RAI.toString());
        localSearchOptions.add(LocalSearchOpName.THREE_OPT.toString());
        if(LocalSearchLKH.isAvailable()) {
            lblMemeticoIncludeLKH.setVisible(true);
            cbMemeticoIncludeLKH.setVisible(true);
        } else {
            lblMemeticoIncludeLKH.setVisible(false);
            cbMemeticoIncludeLKH.setVisible(false);
            System.err.println("Warning: LKH Executable not found, the option will be hidden");
        }

        choiceMemeticoCrossover.getItems().addAll(
                Arrays.stream(CrossoverOpName.values())
                        .map(Object::toString)
                        .collect(Collectors.toList())
        );

        choiceMemeticoRestart.getItems().addAll(
                Arrays.stream(RestartOpName.values())
                    .map(Object::toString)
                    .collect(Collectors.toList())
        );

        //bind the displayed fields etc to the actual problem via listeners
        chosenProblemInstance.addListener(
                (observable, oldValue, newValue) -> {
                    lblMemeticoProblemFile.setText(newValue.getConfiguration().problemFile.getPath());
                    choiceMemeticoSolutionType.setValue(newValue.getConfiguration().solutionType.toString());
                    switch (newValue.getConfiguration().solutionType) {
                        case TOUR:
                            lblMemeticoTourFile.setText(newValue.getConfiguration().tourFile.getPath());
                            break;
                        case COST:
                            fldMemeticoTourCost.setText(String.valueOf(newValue.getTargetCost()));
                            break;
                    }
                }
        );

        chosenMemeticoConfiguration.addListener((observable, oldValue, newValue) -> {
            int popSize = newValue.populationSize;
            int popDepth = (int)Math.ceil(
                    (Math.log(
                        ( (Population.DEFAULT_N_ARY-1) * popSize ) + 1
                    ) / Math.log(Population.DEFAULT_N_ARY)) - 1
            );
            fldMemeticoPopDepth.setText(String.valueOf(popDepth));
            fldMemeticoMutRate.setText(String.valueOf(newValue.mutationRate));
            fldMemeticoMaxGen.setText(String.valueOf(newValue.maxGenerations));
            fldMemeticoReignLimit.setText(String.valueOf(newValue.reignLimit));
            choiceMemeticoLocalSearch.setValue(newValue.localSearchOp);
            choiceMemeticoCrossover.setValue(newValue.crossoverOp);
            choiceMemeticoRestart.setValue(newValue.restartOp);
        });


        //apply the default configuration and display it on screen
        chosenMemeticoConfiguration.set(DEFAULT_CONFIG);
//        int popSize = DEFAULT_CONFIG.populationSize;
//        int popDepth = (int)Math.ceil(
//                (Math.log(
//                        ( (Population.DEFAULT_N_ARY-1) * popSize ) + 1
//                ) / Math.log(Population.DEFAULT_N_ARY)) - 1
//        );
//        fldMemeticoPopDepth.setText(String.valueOf(popDepth));
//        fldMemeticoMutRate.setText(String.valueOf(DEFAULT_CONFIG.mutationRate));
//        fldMemeticoMaxGen.setText(String.valueOf(DEFAULT_CONFIG.maxGenerations));
//        choiceMemeticoLocalSearch.setValue(DEFAULT_CONFIG.localSearchOp);
//        choiceMemeticoCrossover.setValue(DEFAULT_CONFIG.crossoverOp);
//        choiceMemeticoRestart.setValue(DEFAULT_CONFIG.restartOp);
//
        fldMemeticoLogInterval.setText(String.valueOf(DEFAULT_LOG_INTERVAL));

        txtMemeticoProblemFileError.visibleProperty().bind(txtMemeticoProblemFileError.textProperty().isNotEmpty());
        txtMemeticoTourFileError.visibleProperty().bind(txtMemeticoTourFileError.textProperty().isNotEmpty());
        txtMemeticoLogFileError.visibleProperty().bind(txtMemeticoLogFileError.textProperty().isNotEmpty());

        //setup template selection
        choiceMemeticoProblemTemplate.getItems().add("Custom");
        //load all the base instances into the map and template list for the options boz
        for (ProblemConfiguration eachProblem : RunConfigurationController.INCLUDED_PROBLEMS) {
            ProblemInstance theInstance = ProblemInstance.create(eachProblem);
            instances.put(theInstance.getName(), theInstance);
            choiceMemeticoProblemTemplate.getItems().add(theInstance.getName());
        }
        //now select the first of our included instances and memetico will be ready to run whenever it may be needed
        choiceMemeticoProblemTemplate.getSelectionModel().select(1);//since "Custom" is first,  we'll want to select the second entry

        theStage = new Stage();
        Scene newScane = new Scene(memeticoConfigurationBoxRoot, 300, 200);
        theStage.setScene(newScane);
    }

    public PCLogger getLogger() {
        return logger;
    }

    public void applyConfiguration() {
        try {
            ProblemInstance targetInstance;
            if ("Custom".equals(choiceMemeticoProblemTemplate.getValue())) {
                URL problemFile;
                String problemText = lblMemeticoProblemFile.getText();
                if (problemText.contains("!")) {
                    problemFile = getClass().getResource(problemText.split("!")[1]);
                } else {
                    problemFile = new File(problemText).toURI().toURL();
                }
                ProblemConfiguration configuration;
                switch (choiceMemeticoSolutionType.getValue()) {
                    case "Tour":
                        URL tourFile;
                        String tourText = lblMemeticoTourFile.getText();
                        if (tourText.contains("!")) {
                            tourFile = getClass().getResource(tourText.split("!")[1]);
                        } else {
                            tourFile = new File(lblMemeticoTourFile.getText()).toURI().toURL();
                        }
                        configuration = new ProblemConfiguration(problemFile, tourFile);
                        break;
                    case "Cost":
                        int targetCost = Integer.parseInt(fldMemeticoTourCost.getText());
                        configuration = new ProblemConfiguration(problemFile, targetCost);
                        break;
                    default:
                        configuration = new ProblemConfiguration(problemFile, 0);
                }

                targetInstance = ProblemInstance.create(configuration);
                //if the raw from-file name is taken - add custom to it
                if (instances.containsKey(targetInstance.getName())) {
                    targetInstance.setName(targetInstance.getName() + " (Custom)");
                }
                instances.put(targetInstance.getName(), targetInstance);
            } else {
                targetInstance = new ProblemInstance(instances.get(choiceMemeticoProblemTemplate.getValue())); //create a clone to prevent external modification of the original
            }
            if(targetInstance.getTspLibInstance() == null) {
                //bad instance file; complain
                txtMemeticoProblemFileError.setText("Notice: failed to read the problem file.");
                txtMemeticoTourFileError.setText("");
            } else {
                txtMemeticoProblemFileError.setText("");
                if(targetInstance.getTargetCost() < 0) {
                    //bad problem file; complain
                    txtMemeticoTourFileError.setText("Notice: failed to read the tour file.");
                } else {
                    txtMemeticoTourFileError.setText("");
                    //now we know the data is correct we can safely apply it
                    chosenProblemInstance.set(targetInstance);
                    int populationHeight = Integer.parseInt(fldMemeticoPopDepth.textProperty().get());
                    int populationSize = (int) ((Math.pow(3, populationHeight + 1) - 1) / 2.0);
                    int mutationRate = Integer.parseInt(fldMemeticoMutRate.textProperty().get());
                    int maxGenerations = Integer.parseInt(fldMemeticoMaxGen.textProperty().get());
                    int reignLimit = Integer.parseInt(fldMemeticoReignLimit.textProperty().get());
                    chosenMemeticoConfiguration.set(new MemeticoConfiguration(populationSize, mutationRate, choiceMemeticoLocalSearch.getValue(), choiceMemeticoCrossover.getValue(), choiceMemeticoRestart.getValue(), maxGenerations, reignLimit));
                    launchMemetico();
                }
            }
            txtMemeticoLogFileError.setText("");
        } catch (IOException e) {
            e.printStackTrace();
            txtMemeticoLogFileError.setText("Notice: failed to read the log file.");
        }
    }

    private void launchMemetico() {
        if(memeticoThread != null && memeticoThread.isAlive()) {
            //tell memetico to stop, then wait for that to happen safely
            currentMemeticoContinuePermission.invalidate();
        }
        try {
            logger.reset();
            logger.setLogFrequency(Integer.parseInt(fldMemeticoLogInterval.getText()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        currentMemeticoContinuePermission = new ValidityFlag.Synchronised();
        final ProblemInstance finalizedProblem = chosenProblemInstance.get();
        final MemeticoConfiguration finalizedConfig = chosenMemeticoConfiguration.get();
        final ValidityFlag.ReadOnly finalizedContinuePermission = currentMemeticoContinuePermission.getReadOnly();
        try {
            TSPLibInstance tspLibInstance = finalizedProblem.getTspLibInstance();
            long maxGenerations = finalizedConfig.maxGenerations != 0 ? finalizedConfig.maxGenerations : (int) (5 * 13 * Math.log(13) * Math.sqrt(tspLibInstance.getDimension()));
            boolean finalizedLKHInclusion = cbMemeticoIncludeLKH.isSelected();

            final Thread oldMemeticoThread = memeticoThread;
            //launch memetico
            memeticoThread = new Thread(() -> {
                try {
                    try {
                        if(oldMemeticoThread != null) {
                            oldMemeticoThread.join(); //wait for the old thread to die; the the new thread will eventually reset it's logger
                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace(); //we don't mind if that thread was interrupted, as long as it's dead
                    }
                    Memetico meme = new Memetico(logger, finalizedContinuePermission, finalizedProblem, finalizedConfig.solutionStructure, finalizedConfig.populationStructure, finalizedConfig.constructionAlgorithm,
                            finalizedConfig.populationSize, finalizedConfig.mutationRate, finalizedConfig.localSearchOp, finalizedConfig.crossoverOp, finalizedConfig.restartOp, finalizedConfig.mutationOp, finalizedLKHInclusion,
                            finalizedConfig.maxTime, maxGenerations, finalizedConfig.reignLimit, finalizedConfig.numReplications);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                currentMemeticoContinuePermission.invalidate();
            });
            memeticoThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void open() {
        theStage.show();
    }

    public void close() {
        theStage.close();
    }

    public Map<String, ProblemInstance> getInstances() {
        return instances;
    }
}
