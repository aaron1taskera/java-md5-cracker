import java.math.BigInteger;
import java.util.ArrayList;


public class CrackManager implements Runnable {
	public boolean cracking = false;
	public ArrayList threads = new ArrayList();
	public static CrackThread[] cr;
	public static BigInteger[] bi;
	public static long initiationTime;
	public void run() {
		while (true) {
			if (Main.cracking != cracking) {
				if (Main.cracking) {
					cr = new CrackThread[Main.crackThreads];
					bi = new BigInteger[Main.crackThreads];
					BigInteger current = Main.bounds[0];
					BigInteger upper;
					int increment = (int) Math.ceil(((float)Main.bounds[1].subtract(Main.bounds[0]).intValue() / (float)Main.crackThreads));
					for (int i = 0; i < Main.crackThreads; i ++) {
						upper = current.add(BigInteger.valueOf(increment));
						if (Main.bounds[1].compareTo(upper) == -1) {
							upper = Main.bounds[1];
						}
						if (i == Main.crackThreads - 1) {
							upper = Main.bounds[1];
						}
						cr[i] = new CrackThread(i, upper);
						bi[i] = current;
						Thread t = (new Thread(cr[i]));
						threads.add(t);
						t.start();
						current = upper;
					}
					initiationTime = System.currentTimeMillis();
				} else {
					for (int i = 0; i < threads.size(); i ++) {
						Thread t = (Thread)threads.get(i);
						t.interrupt();
					}
				}
				cracking = Main.cracking;
			}
			if (cracking) {
				boolean requestnew = true;
				for (int i = 0; i < cr.length; i ++) {
					if (!cr[i].finished) {
						requestnew = false;
					}
				}
				Main.requestNew = requestnew;
				if (requestnew) {
					try {
						Main.average = (int) (Main.bounds[1].subtract(Main.bounds[0]).intValue() / (System.currentTimeMillis() - initiationTime));
					} catch (Exception e) {  }
					Main.sendAverage = true;
					Main.out("Completed block in " + (System.currentTimeMillis() - initiationTime) + "ms Speed: " + (Main.bounds[1].subtract(Main.bounds[0]).intValue() / (System.currentTimeMillis() - initiationTime)) + "KH/s");
					Main.cracking = false;
				}
			}
			try { Thread.sleep(20); } catch (Exception e) {  }
		}
	}
}
