package moss.mystery.energymonitor.processes;

import org.junit.Test;
import static org.junit.Assert.*;

public class ProcessTest {
    @Test
    public void updateTicks_test(){
        Process proc = new Process(1, 1);

        assertEquals(1, proc.updateTicks(2, 1));
        assertEquals(2, proc.currTicks);
        assertEquals(1, proc.prevTicks);

        proc.updateTicks(1, 1);
        assertEquals(1, proc.currTicks);

        proc.updateTicks(0, 2);
        assertEquals(0, proc.currTicks);
    }
}
