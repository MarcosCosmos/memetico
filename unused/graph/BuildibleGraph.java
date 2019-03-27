package graph;


/**
 * Buildible here refers to nodes, i.e. A graph for which nodes can be added on-the-fly. Ths may mean variable memory usage over the life of the graph.
 * @param <V>
 * @param <D>
 * @param <E>
 */
public interface BuildibleGraph<V extends Graph.Vertex, D extends Number, E extends Graph.Edge<V, D>> extends Graph<V,D,E>{

    /**
     * Sets the vertex in this graph uniquely associated with the supplied {@code vertex}'s id to the supplied {@code vertex}
     * @param vertex
     * @return true if the graph was changed as a result of adding the vertex
     */
    boolean set(V vertex);
}
