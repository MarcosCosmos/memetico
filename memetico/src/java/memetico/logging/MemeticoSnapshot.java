package memetico.logging;

import com.google.gson.*;
import memetico.Arc;
import memetico.DiCycle;
import memetico.PocCurAgent;
import memetico.Population;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Takes log-safe information from data provided by memetico;
 * It is important to note that it is only truely safe in so far as no attempts are made to interiorly modify pocket/current.
 */
public class MemeticoSnapshot {
    public static class LightTour {
        /**
         * Read-only/unmodifiable
         */
        public final List<Integer> tour;
        public final double cost;
        public LightTour(DiCycle src) {
            Gson gson = new Gson();
            List<Integer> tmp = new ArrayList<>(src.arcArray.length);
            int city = 0;
            int i=0;
            do {
                tmp.add(city);
                city = src.arcArray[city].tip;
            } while (city != 0);
            this.cost = src.cost;
            this.tour = Collections.unmodifiableList(tmp);
        }

        public LightTour(List<Integer> tour, double cost) {
            this.tour = tour;
            this.cost = cost;
        }

        public static class Deserializer implements JsonDeserializer<LightTour> {

            @Override
            public LightTour deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                double cost = jsonObject.get("cost").getAsDouble();
                List<Integer> tour = Arrays.asList(new Gson().fromJson(jsonObject.get("tour").getAsJsonArray(), Integer[].class));
                return new LightTour(tour, cost);
            }
        }
    }

    public static class AgentSnapshot {
        public final int id;
        //todo: consider possibly using a thin solutionstructure here like for agent and algorithm?
        public final LightTour pocket;          /* The "Pocket"  SolutionStructure    */
        public final LightTour current;         /* The "Current" SolutionStructure    */
        //todo: possibly consider
        public AgentSnapshot(int id, DiCycle pocket, DiCycle current) {
            this(id, new LightTour(pocket), new LightTour(current));
        }
        public AgentSnapshot(int id, LightTour pocket, LightTour current) {
            this.id = id;
            this.pocket = pocket;
            this.current = current;
        }

        public static class Deserializer implements JsonDeserializer<AgentSnapshot> {
            @Override
            public AgentSnapshot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                return new AgentSnapshot(jsonObject.get("id").getAsInt(), (LightTour) context.deserialize(jsonObject.get("pocket"), LightTour.class), context.deserialize(jsonObject.get("current"), LightTour.class));
            }
        }
    }

    public final String instanceName;
    public final int generation;
    public final int nAry;
    /**
     * Measured in nanoseconds
     */
    public final long logTime;
    public final LightTour bestSolution;
    public final List<AgentSnapshot> agents;

    /**
     * Note: the cost value (in the SolutionStructure class) is expected to be already computed for all srcs
     * @param src
     */
    public MemeticoSnapshot(String instanceName, Population src, int generation, long logTime) {
        this(instanceName, generation, src.n_ary, logTime, new LightTour((DiCycle)src.bestSolution),
                Collections.unmodifiableList(
                        IntStream.range(0,src.popSize).mapToObj(i -> {
                                    PocCurAgent each = (PocCurAgent)src.pop[i];
                                return new AgentSnapshot(i, (DiCycle)each.pocket, (DiCycle)each.current);
                            }
                            ).collect(Collectors.toList())
                        )
        );
    }

    public MemeticoSnapshot(String instanceName, int generation, int nAry, long logTime, LightTour bestSolution, List<AgentSnapshot> agents) {
        this.instanceName = instanceName;
        this.generation = generation;
        this.nAry = nAry;
        this.logTime = logTime;
        this.bestSolution = bestSolution;
        this.agents = agents;
    }

    public static class Deserializer implements JsonDeserializer<MemeticoSnapshot> {
        @Override
        public MemeticoSnapshot deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            return new MemeticoSnapshot(jsonObject.get("instanceName").getAsString(), jsonObject.get("generation").getAsInt(), jsonObject.get("nAry").getAsInt(), jsonObject.get("logTime").getAsLong(), context.deserialize(jsonObject.get("pocket"), LightTour.class), context.deserialize(jsonObject.get("current"), LightTour.class));
        }
    }
}
