package in.acode;

import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class VarHandleSampleTests {

    //Class to hold a variable and a corresponding var-handle
    static class VarHandleContainer {
        public int x;
        public int y;
        public static final VarHandle X;

        static {
            try {
                X = MethodHandles.lookup().findVarHandle(VarHandleContainer.class, "x", int.class);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Test(timeout = 1000)
    public void shouldOpaqueModeDetectInterthreadChangesToSameVar() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        VarHandleContainer varHandleContainer = new VarHandleContainer();

        //Set x to 1 after sometime
        executeAfterDelay(() -> VarHandleContainer.X.setOpaque(varHandleContainer, 1),
                500, TimeUnit.MILLISECONDS);


        //Start a thread which would exit only after the changed value of x is visible
        Thread pollingThread = new Thread(() -> {
            while (true) {
                if ((int) VarHandleContainer.X.getOpaque(varHandleContainer) == 1) {
                    System.out.println("Successfully read the value written by another thread in opaque mode!");
                    break;
                }
            }
        });

        pollingThread.start();
        pollingThread.join();
    }

    @Test(timeout = 1000)
    public void shouldRAModeDetectInterthreadChangesToAllVars() throws NoSuchFieldException, IllegalAccessException, InterruptedException {
        VarHandleContainer varHandleContainer = new VarHandleContainer();

        //Set variable values after some time
        executeAfterDelay(() -> {
                    varHandleContainer.y = 1;
                    VarHandleContainer.X.setRelease(varHandleContainer, 1);
                },
                500, TimeUnit.MILLISECONDS);


        //Start a thread which would exit only after the changed values of all variables are visible
        Thread pollingThread = new Thread(() -> {
            while (true) {
                if ((int) VarHandleContainer.X.getAcquire(varHandleContainer) == 1) {
                    assertEquals(1, varHandleContainer.y);
                    System.out.println("Successfully read the values written by another thread in RA mode!");
                    break;
                }
            }
        });

        pollingThread.start();
        pollingThread.join();
    }

    private void executeAfterDelay(Runnable r, long delay, TimeUnit timeUnit) {
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.schedule(r, delay, timeUnit);
    }

}
