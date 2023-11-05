package gqserver.events;

import org.tinylog.Logger;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class GlobalQuakeServerEventHandler {

    private Queue<GlobalQuakeServerEventListener> eventListeners;

    private ExecutorService executor;

    public GlobalQuakeServerEventHandler runHandler() {
        eventListeners = new ConcurrentLinkedQueue<>();
        executor = Executors.newSingleThreadExecutor();
        return this;
    }

    public void stopHandler(){
        executor.shutdownNow();
        try {
            executor.awaitTermination(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Logger.error(e);
        }
        eventListeners.clear();
    }

    public void registerEventListener(GlobalQuakeServerEventListener eventListener){
        eventListeners.add(eventListener);
    }

    @SuppressWarnings("unused")
    public boolean removeEventListener(GlobalQuakeServerEventListener eventListener){
        return eventListeners.remove(eventListener);
    }

    public void fireEvent(GlobalQuakeServerEvent event){
        executor.submit(() -> {
            for (GlobalQuakeServerEventListener eventListener : eventListeners) {
                try {
                    event.run(eventListener);
                } catch (Exception e) {
                    Logger.error(e);
                }
            }
        });
    }

}