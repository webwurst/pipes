package com.tinkerpop.pipes.filter;

import com.tinkerpop.pipes.AbstractPipe;
import com.tinkerpop.pipes.Pipe;
import com.tinkerpop.pipes.util.MetaPipe;
import com.tinkerpop.pipes.util.PipeHelper;
import com.tinkerpop.pipes.util.SingleIterator;

import java.util.Arrays;
import java.util.List;

/**
 * The AndFilterPipe takes a collection of pipes, where E is boolean. Each provided pipe is fed the same incoming S object.
 * If all the pipes emit true, then the AndFilterPipe emits the incoming S object. If not, then the incoming S object is not emitted.
 *
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class AndFilterPipe<S> extends AbstractPipe<S, S> implements FilterPipe<S>, MetaPipe {

    private final List<Pipe<S, Boolean>> pipes;

    public AndFilterPipe(final Pipe<S, Boolean>... pipes) {
        this.pipes = Arrays.asList(pipes);
    }

    public AndFilterPipe(final List<Pipe<S, Boolean>> pipes) {
        this.pipes = pipes;
    }

    // Todo: Reset all pipes after break?
    public S processNextStart() {
        while (true) {
            final S s = this.starts.next();
            boolean and = true;
            for (final Pipe<S, Boolean> pipe : this.pipes) {
                pipe.setStarts(new SingleIterator<S>(s));
                if (!pipe.next()) {
                    and = false;
                    break;
                }
            }
            if (and)
                return s;
        }
    }

    public List<Pipe> getPipes() {
        return (List) this.pipes;
    }

    public String toString() {
        return PipeHelper.makePipeString(this, this.pipes);
    }

}