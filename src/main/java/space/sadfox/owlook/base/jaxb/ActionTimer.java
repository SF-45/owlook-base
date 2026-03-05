package space.sadfox.owlook.base.jaxb;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActionTimer {
  private final AtomicInteger duration = new AtomicInteger(0);
  private final AtomicInteger counter = new AtomicInteger(1);
  private Thread timerThread;
  private final Runnable timer;
  private static final Logger log = LoggerFactory.getLogger(ActionTimer.class);
  private final ReentrantLock lock = new ReentrantLock();

  public ActionTimer(int initDuration, Runnable action) {
    this.duration.set(initDuration);
    timer = () -> {
      while (counter.getAndIncrement() < duration.get()) {
        try {
          Thread.sleep(1000L);
        } catch (Exception e) {
          log.error("Action timer sleep error", e);
        }
      }
      lock.lock();
      counter.set(1);
      action.run();
      timerThread = null;
      lock.unlock();
    };
  }

  public ActionTimer(Runnable action) {
    this(0, action);
  }

  public void runOrResetTimer() {
    lock.lock();
    if (timerThread == null || !timerThread.isAlive()) {
      timerThread = new Thread(timer);
      timerThread.start();
    } else {
      counter.set(1);
    }
    lock.unlock();
  }

  public void setDuration(int duration) {
    this.duration.set(duration);
  }
}
