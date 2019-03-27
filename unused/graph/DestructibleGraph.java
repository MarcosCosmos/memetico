package graph;


/**
 * Destructible here refers to destruction of nodes, i.e. A graph for which nodes can be added on-the-fly. Ths may mean variable memory usage over the life of the graph.
 * @param <V>
 * @param <D>
 * @param <E>
 */
public interface DestructibleGraph<V extends Graph.Vertex, D extends Number, E extends Graph.Edge<V, D>> extends BuildibleGraph<V,D,E> {

    /**
     * Removes the vertex with the same unique identifier as this one from the graph, if any
     * @param vertex
     * @return
     */
    boolean remove(V vertex);
}
