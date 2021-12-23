package main.utilities;

import java.util.concurrent.atomic.AtomicInteger;

public class IDGenerator {

  private static final AtomicInteger prevUsedID = new AtomicInteger(0);

  public static boolean init(int restoredVal){
    prevUsedID.set(restoredVal);
    return true;
  }

  public static int generateNewID(){
    if (prevUsedID.get() == Integer.MAX_VALUE){ //overflow check
      prevUsedID.set(0);
    }
    return prevUsedID.incrementAndGet();
  }

  /* Only to be used when saving the generator state to disk*/
  public static int saveState(){
    return prevUsedID.get();
  }
}
