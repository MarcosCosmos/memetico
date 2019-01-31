package algos._memetico;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import memetico.logging.PCAlgorithmState;
import memetico.logging.PCLogger;
import org.marcos.uon.tspaidemo.util.log.BasicLogger;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;
public class TestPCFrameController {
    @FXML
    private BorderPane contentRoot;
    @FXML
    private Text generationText;
    @FXML
    private GridPane agentsGrid;

    private IntegerProperty generationValue = new SimpleIntegerProperty();
    private ObjectProperty<PCAlgorithmState> state;
    public void setup(ObjectProperty<PCAlgorithmState> state) {
        this.state = state;
        generationValue.bind(
                Bindings.createIntegerBinding(
                    () -> {
                        PCAlgorithmState curState = state.get();
                        return curState == null ? 0 : curState.generation;
                    } ,
                    state
                )
        );
        generationText.textProperty()
                .bind(generationValue.asString());

        //use a listener to dynamically create/destroy agent displays
        state.addListener(((observable, oldValue, newValue) -> {
            ObservableList<Node> displayNodes = agentsGrid.getChildren();
            if(newValue.agents.length < displayNodes.size()) {
                //delete unneeded agent displays and states; todo: possibly just hide them for performance?
                displayNodes.subList(newValue.agents.length, displayNodes.size()).clear();
            } else if(newValue.agents.length > displayNodes.size()) {
                //add needed agent displays
                for(int i=displayNodes.size(); i<newValue.agents.length; ++i) {
                    FXMLLoader loader = new FXMLLoader(
                            getClass().getResource(
                                    "test_agent_display.fxml"
                            )
                    );
                    try {
                        //load the node
                        Pane newNode = loader.load();
                        //retrieve the controller
                        AgentDisplayController newController = loader.<AgentDisplayController>getController();
                        //create an observable reference to an agent state
                        ObjectProperty<PCAlgorithmState.AgentState> newState = new SimpleObjectProperty<>();
                        //create an observable property for the id
                        IntegerProperty newId = new SimpleIntegerProperty(i); //just use it's index in the lists as the id for now

                        //bind the agent state to the algorithm state+id since it doubles as the index
                        newState.bind(
                                Bindings.createObjectBinding(
                                        () -> state.get().agents[newId.get()],
                                        state, newId
                                )
                        );

                        //give the state to the controller
                        newController.setup(newId, newState);
                        //add the data to the lists
                        displayNodes.add(newNode);

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            int numParents = (int) Math.floor(newValue.agents.length / newValue.nAry);
            int row = newValue.agents.length-1;
            for (int parent = numParents-1; parent >= 0; parent++) {
                int firstCh = newValue.nAry * parent + 1;
                int lastCh = newValue.nAry * parent + newValue.nAry;
                for (int i = lastCh-1; i>=firstCh; i--) {
                    if(i < newValue.agents.length) {
                        GridPane.setRowIndex(displayNodes.get(i), row--);
                    }
                }
            }
            GridPane.setRowIndex(displayNodes.get(0), row);

            int i, j, firstCh, lastCh, parent;

//            for (parent = nrParents - 1; parent >= 0; parent--) {
//                firstCh = n_ary * parent + 1;
//                lastCh = n_ary * parent + n_ary;
//                for (i = firstCh; i < lastCh; i++) {
//                    for (j = (i + 1); j <= lastCh; j++) {
//                        if (pop[i].cost > pop[j].cost) {
//                            pop[i].exchangeSolutionStructures(pop[j]);
//                        }
//                    }
//                }
//            }

//            //for all agents, ensure they position in the grid is correct for their position in the population
//            for(int i=0; i<displayNodes.size();++i) {
//                //set the row and column indices based on tree position
////                //(for initial test, just row index)
//            }
//0
//        1
//                4
//                5
//                6
//        2
//                7
//                8
//                9
//        3
//                10
//                11
//                12
        }));

    }
}
