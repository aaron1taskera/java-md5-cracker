import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class JobManagement implements Runnable {
	public static volatile ArrayList incomplete = new ArrayList();
	public static volatile ArrayList requests = new ArrayList();
	public static ArrayList toRemove = new ArrayList();
	public static BigInteger startCrackNumber = BigInteger.valueOf(1);
	private Client current;
	private int hashRate;
	private long range;
	private BigInteger[] storage = new BigInteger[2];
	private boolean cracking = false;
	public JobManagement() {

	}
	public void run() {
		while (true) {
			try {
				if (cracking != Startup.cracking) {
					if (!Startup.cracking) {
						requests.clear();
						incomplete.clear();
					}
					cracking = Startup.cracking;
				}
				Thread.sleep(20);
				if (requests.size() != 0) {
					for (Object s : requests) {
						toRemove.add(s);
						current = (Client)s;
						hashRate = current.clientHashRate;
						if (incomplete.size() == 0) {
							if (current.average != -1) {
								hashRate = current.average;
							}
							if (hashRate == 0) {
								hashRate = 350;
							}
							range = 45000L * (long)hashRate;
							current.bounds[0] = startCrackNumber;
							current.bounds[1] = startCrackNumber.add(BigInteger.valueOf(range));
							startCrackNumber = current.bounds[1];
							current.hasBlock = true;
						} else {
							storage = (BigInteger[]) incomplete.get(0);
							current.bounds[0] = storage[0];
							current.bounds[1] = storage[1];
							current.hasBlock = true;
							incomplete.remove(0);
							System.out.println("Recycled rejected block with range of " + storage[1].subtract(storage[0]));
						}
					}
					for (Object s : toRemove) {
						requests.remove(s);
					}
					toRemove.clear();
				}
			} catch (Exception e) { e.printStackTrace(); }
		}
	}
}
