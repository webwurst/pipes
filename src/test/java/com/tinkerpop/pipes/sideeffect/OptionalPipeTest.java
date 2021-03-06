package com.tinkerpop.pipes.sideeffect;

import com.tinkerpop.pipes.filter.FilterPipe;
import com.tinkerpop.pipes.filter.ObjectFilterPipe;
import com.tinkerpop.pipes.transform.IdentityPipe;
import com.tinkerpop.pipes.util.Pipeline;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class OptionalPipeTest extends TestCase {

    public void testOptionalBasic() {
        List<String> names = Arrays.asList("marko", "povel", "peter", "josh");
        OptionalPipe<String> pipe1 = new OptionalPipe<String>(new IdentityPipe<String>());
        pipe1.setStarts(names);
        int counter = 0;
        while (pipe1.hasNext()) {
            counter++;
            assertTrue(names.contains(pipe1.next()));
        }
        assertEquals(counter, 4);
    }

    public void testOptionalSideEffect() {
        List<String> names = Arrays.asList("marko", "povel", "peter", "josh");
        List<String> results = new ArrayList<String>();
        OptionalPipe<String> pipe1 = new OptionalPipe<String>(new AggregatorPipe<String>(results));
        pipe1.setStarts(names);
        int counter = 0;
        while (pipe1.hasNext()) {
            counter++;
            assertTrue(names.contains(pipe1.next()));

        }
        assertEquals(counter, 4);
        for (int i = 0; i < names.size(); i++) {
            assertEquals(names.get(i), results.get(i));
        }
    }

    public void testOptionalSideEffectWithFilter() {
        List<String> names = Arrays.asList("marko", "povel", "peter", "josh");
        List<String> results = new ArrayList<String>();
        OptionalPipe<String> pipe1 = new OptionalPipe<String>(new Pipeline(new AggregatorPipe<String>(results), new ObjectFilterPipe<String>("marko", FilterPipe.Filter.EQUAL)));
        pipe1.setStarts(names);
        int counter = 0;
        while (pipe1.hasNext()) {
            counter++;
            assertTrue(names.contains(pipe1.next()));

        }
        assertEquals(counter, 4);
        for (int i = 0; i < names.size(); i++) {
            assertEquals(names.get(i), results.get(i));
        }
    }


}
