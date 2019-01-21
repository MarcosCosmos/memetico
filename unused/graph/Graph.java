package graph;

import com.sun.istack.internal.NotNull;

import java.util.Collection;
import java.util.List;

/**
 * Can be matrix or adjacency-list based internally; (Note that matrix based graphs cannot, generally, be BuildableGraphs
 * Generalises node and edge should allow for variety of metric spaces and directedness, etc - Nodes and edges define their comparators, etc, thus defining directedness?
 * Note that edges are not (by this interface) garaunteed to be unique per node pair
 * @param <V> - vertice type
 * @param <E> - e.g. if edges are immutable and sort the nodes they use as keys, then they will be
 * @param <D> - distance type
 */
public interface Graph<V extends Graph.Vertex, D extends Number, E extends Graph.Edge<V, D>> {
    interface Vertex {
        public int getId();
    }
    interface Edge<_V extends Graph.Vertex, D extends Number> {
        _V getFrom();
        _V getTo();
        D length();
    }

    /**
     * Should have linear or constant complexity (see selected implementation)
     * @return an unmodifiable collection
     */
    Collection<V> getVertices();

    /**
     * May have polynomial complexity (e.g. if implemented internally via an adjacency matrix)
     * @return
     */
    Collection<E> getEdges();

    /**
     * Gets the vertex with the supplied {@code id}, if any
     * @param id an id to search against
     * @return the vertex associated with the given id if one exists, otherwise null.
     */
    Vertex getVertex(int id);

    /**
     * Tests whether or not a particular vertex exists
     * @param id an id to search against
     * @return true if there is a vertex {@code v} such that {@code v.getId() == id}
     */
    boolean containsVertex(int id);

    /**
     * Tests whether or not a particular vertex exists
     * @param vertex a vertex to test for (tested on id basis)
     * @return true if there is a vertex {@code v} such that {@code v.getId() == id}
     */
    default boolean contains(V vertex) {
        return containsVertex(vertex.getId());
    }

    /**
     * Gets the edge between two nodes, if it exists
     * @param from
     * @param to
     * @return the edge from {@code from} to {@code to} if it exists, otherwise null.
     */
    Edge<V, D> getEdge(@NotNull V from, @NotNull V to);

    /**
     * Tests whether or not an edge connecting from {@code from} to {@code to}.
     * @param from
     * @param to
     * @return true if there is an edge from {@code from} to {@code to}, otherwise null.
     */
    default boolean hasConnection(@NotNull V from, @NotNull V to) {
        return getEdge(from, to) != null;
    }

    /**
     * Tests whether or not a particular edge exists
     * @param edge an edge to test for
     * @return true if the graph contains some edge {@code other} for which {@code other.equals(edge)} returns true
     */
    boolean contains( @NotNull E edge);

    /**
     * @param edge
     * Sets the edge in this uniquely matching the supplied {@code edge} to the supplied {@code edge}
     * @return whether or not the graph was modified as a result of this action
     */
    boolean set(@NotNull E edge);

    /**
     * Removes the edge that uniques matches the supplied {@code edge} from the graph, if any exist
     * @param edge
     * @return true if the graph was modified as a result of this action (i.e. true if the edge was present), otherwise false.
     */
    boolean remove(@NotNull E edge);
}
