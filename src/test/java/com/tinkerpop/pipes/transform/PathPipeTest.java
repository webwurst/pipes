package com.tinkerpop.pipes.transform;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.Pipeline;
import com.tinkerpop.pipes.util.SingleIterator;
import junit.framework.TestCase;

import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class PathPipeTest extends TestCase {

    public void testPipeBasic() {
        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Pipe<Vertex, Edge> pipe1 = new OutEdgesPipe();
        Pipe<Edge, Vertex> pipe2 = new InVertexPipe();
        Pipe<Vertex, List> pipe3 = new PathPipe<Vertex>();
        Pipe<Vertex, List> pipeline = new Pipeline<Vertex, List>(pipe1, pipe2, pipe3);
        pipeline.setStarts(new SingleIterator<Vertex>(marko));
        for (List path : pipeline) {
            assertEquals(path.get(0), marko);
            assertTrue(path.get(1) instanceof Edge);
            assertTrue(path.get(2) instanceof Vertex);
        }
    }
}
