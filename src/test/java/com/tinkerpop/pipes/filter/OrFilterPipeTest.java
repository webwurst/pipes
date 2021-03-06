package com.tinkerpop.pipes.filter;

import com.tinkerpop.blueprints.pgm.Edge;
import com.tinkerpop.blueprints.pgm.Graph;
import com.tinkerpop.blueprints.pgm.Vertex;
import com.tinkerpop.blueprints.pgm.impls.tg.TinkerGraphFactory;
import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.transform.HasNextPipe;
import com.tinkerpop.pipes.transform.InVertexPipe;
import com.tinkerpop.pipes.transform.OutEdgesPipe;
import com.tinkerpop.pipes.transform.PropertyPipe;
import com.tinkerpop.pipes.util.Pipeline;
import junit.framework.TestCase;

import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OrFilterPipeTest extends TestCase {

    public void testOrPipeBasic() {
        List<String> names = Arrays.asList("marko", "povel", "peter", "povel", "marko");
        ObjectFilterPipe<String> pipe1 = new ObjectFilterPipe<String>("marko", FilterPipe.Filter.EQUAL);
        ObjectFilterPipe<String> pipe2 = new ObjectFilterPipe<String>("povel", FilterPipe.Filter.EQUAL);
        OrFilterPipe<String> orFilterPipe = new OrFilterPipe<String>(new HasNextPipe<String>(pipe1), new HasNextPipe<String>(pipe2));
        orFilterPipe.setStarts(names);
        int counter = 0;
        while (orFilterPipe.hasNext()) {
            String name = orFilterPipe.next();
            assertTrue(name.equals("marko") || name.equals("povel"));
            counter++;
        }
        assertEquals(counter, 4);
    }

    public void testOrPipeGraph() {
        // ./outE[@label='created' or @weight > 0.5]

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Vertex peter = graph.getVertex("6");
        OutEdgesPipe pipe0 = new OutEdgesPipe();
        LabelFilterPipe pipe1 = new LabelFilterPipe("created", FilterPipe.Filter.EQUAL);
        PropertyFilterPipe<Edge, Float> pipe2 = new PropertyFilterPipe<Edge, Float>("weight", 0.5f, FilterPipe.Filter.GREATER_THAN);
        OrFilterPipe<Edge> orFilterPipe = new OrFilterPipe<Edge>(new HasNextPipe<Edge>(pipe1), new HasNextPipe<Edge>(pipe2));
        Pipeline<Vertex, Edge> pipeline = new Pipeline<Vertex, Edge>(pipe0, orFilterPipe);
        pipeline.setStarts(Arrays.asList(marko, peter, marko));
        int counter = 0;
        while (pipeline.hasNext()) {
            Edge edge = pipeline.next();
            assertTrue(edge.getId().equals("8") || edge.getId().equals("9") || edge.getId().equals("12"));
            assertTrue((Float) edge.getProperty("weight") > 0.5f || edge.getLabel().equals("created"));
            counter++;
        }
        assertEquals(counter, 5);

    }

    public void testAndOrPipeGraph() {
        // ./outE[@label='created' or (@label='knows' and @weight > 0.5)]

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");
        Pipe<Vertex, Edge> pipe1 = new OutEdgesPipe();
        FilterPipe<Edge> pipeA = new LabelFilterPipe("created", FilterPipe.Filter.EQUAL);
        FilterPipe<Edge> pipeB = new LabelFilterPipe("knows", FilterPipe.Filter.EQUAL);
        FilterPipe<Edge> pipeC = new PropertyFilterPipe<Edge, Float>("weight", 0.5f, FilterPipe.Filter.GREATER_THAN);
        FilterPipe<Edge> pipeD = new AndFilterPipe<Edge>(new HasNextPipe<Edge>(pipeB), new HasNextPipe<Edge>(pipeC));
        FilterPipe<Edge> pipe2 = new OrFilterPipe<Edge>(new HasNextPipe<Edge>(pipeA), new HasNextPipe<Edge>(pipeD));
        Pipeline<Vertex, Edge> pipeline = new Pipeline<Vertex, Edge>(pipe1, pipe2);
        pipeline.setStarts(Arrays.asList(marko));
        int counter = 0;
        while (pipeline.hasNext()) {
            Edge edge = pipeline.next();
            assertTrue(edge.getId().equals("8") || edge.getId().equals("9"));
            assertTrue(edge.getLabel().equals("created") || ((Float) edge.getProperty("weight") > 0.5f && edge.getLabel().equals("knows")));
            counter++;
        }
        assertEquals(counter, 2);
    }

    public void testFutureFilter() {
        List<String> names = Arrays.asList("marko", "peter", "josh", "marko", "jake", "marko", "marko");
        Pipe<String, Integer> pipeA = new CharacterCountPipe();
        Pipe<Integer, Integer> pipeB = new ObjectFilterPipe<Integer>(4, FilterPipe.Filter.NOT_EQUAL);
        Pipe<String, String> pipe1 = new OrFilterPipe<String>(new HasNextPipe<String>(new Pipeline<String, Integer>(pipeA, pipeB)));
        Pipeline<String, String> pipeline = new Pipeline<String, String>(pipe1);
        pipeline.setStarts(names);
        int counter = 0;
        while (pipeline.hasNext()) {
            String name = pipeline.next();
            //System.out.println(name);
            counter++;
            assertTrue((name.equals("marko") || name.equals("peter")) && !name.equals("josh") && !name.equals("jake"));
        }
        assertEquals(counter, 5);
    }

    public void testFutureFilterGraph() {
        // ./outE[@label='created']/inV[@name='lop']/../../@name

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");

        Pipe<Vertex, Edge> pipeA = new OutEdgesPipe();
        Pipe<Edge, Edge> pipeB = new LabelFilterPipe("created", FilterPipe.Filter.EQUAL);
        Pipe<Edge, Vertex> pipeC = new InVertexPipe();
        Pipe<Vertex, Vertex> pipeD = new PropertyFilterPipe<Vertex, String>("name", "lop", FilterPipe.Filter.EQUAL);
        Pipe<Vertex, Vertex> pipe1 = new AndFilterPipe<Vertex>(new HasNextPipe<Vertex>(new Pipeline<Vertex, Vertex>(pipeA, pipeB, pipeC, pipeD)));
        Pipe<Vertex, String> pipe2 = new PropertyPipe<Vertex, String>("name");
        Pipeline<Vertex, String> pipeline = new Pipeline<Vertex, String>(pipe1, pipe2);
        pipeline.setStarts(Arrays.asList(marko));
        int counter = 0;
        while (pipeline.hasNext()) {
            String name = pipeline.next();
            assertEquals(name, "marko");
            counter++;
        }
        assertEquals(counter, 1);
    }

    public void testComplexFutureFilterGraph() {
        // ./outE[@weight > 0.5]/inV/../../outE/inV/@name

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");

        Pipe<Vertex, Edge> pipeA = new OutEdgesPipe();
        Pipe<Edge, Edge> pipeB = new PropertyFilterPipe<Edge, Float>("weight", 0.5f, FilterPipe.Filter.GREATER_THAN_EQUAL);
        Pipe<Edge, Vertex> pipeC = new InVertexPipe();
        Pipe<Vertex, Vertex> pipe1 = new AndFilterPipe<Vertex>(new HasNextPipe<Vertex>(new Pipeline<Vertex, Vertex>(pipeA, pipeB, pipeC)));
        Pipe<Vertex, Edge> pipe2 = new OutEdgesPipe();
        Pipe<Edge, Vertex> pipe3 = new InVertexPipe();
        Pipe<Vertex, String> pipe4 = new PropertyPipe<Vertex, String>("name");
        Pipeline<Vertex, String> pipeline = new Pipeline<Vertex, String>(pipe1, pipe2, pipe3, pipe4);
        pipeline.setStarts(Arrays.asList(marko));
        int counter = 0;
        while (pipeline.hasNext()) {
            String name = pipeline.next();
            assertTrue(name.equals("vadas") || name.equals("lop") || name.equals("josh"));
            counter++;
        }
        assertEquals(counter, 3);

    }

    public void testComplexTwoFutureFilterGraph() {
        // ./outE/inV/../../outE/../outE/inV/@name

        Graph graph = TinkerGraphFactory.createTinkerGraph();
        Vertex marko = graph.getVertex("1");

        Pipe<Vertex, Edge> pipeA = new OutEdgesPipe();
        Pipe<Edge, Vertex> pipeB = new InVertexPipe();
        Pipe<Vertex, Vertex> pipe1 = new OrFilterPipe<Vertex>(new HasNextPipe<Vertex>(new Pipeline<Vertex, Vertex>(pipeA, pipeB)));
        Pipe<Vertex, Edge> pipeC = new OutEdgesPipe();
        Pipe<Vertex, Vertex> pipe2 = new OrFilterPipe<Vertex>(new HasNextPipe<Vertex>(pipeC));
        Pipe<Vertex, Edge> pipe3 = new OutEdgesPipe();
        Pipe<Edge, Vertex> pipe4 = new InVertexPipe();
        Pipe<Vertex, String> pipe5 = new PropertyPipe<Vertex, String>("name");
        Pipeline<Vertex, String> pipeline = new Pipeline<Vertex, String>(pipe1, pipe2, pipe3, pipe4, pipe5);
        pipeline.setStarts(Arrays.asList(marko));
        int counter = 0;
        while (pipeline.hasNext()) {
            String name = pipeline.next();
            assertTrue(name.equals("vadas") || name.equals("lop") || name.equals("josh"));
            counter++;
        }
        assertEquals(counter, 3);

    }

    private class CharacterCountPipe extends AbstractPipe<String, Integer> {
        protected Integer processNextStart() {
            return this.starts.next().length();
        }
    }
}
