import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Scanner;

public class Startup {
	public static ArrayList clients = new ArrayList();
	public static Listener l;
	public static Client c;
	public static ArrayList listeners = new ArrayList();
	public static ArrayList listenerClasses = new ArrayList();
	public static long initiateTime = System.currentTimeMillis();
	public static boolean cracking = false;
	public static String currentHashString = "";
	public static boolean crackNumbers = false;
	public static boolean crackUpperCase = false;
	public static boolean crackSpecial = false;
	public static int threads = 0;
	public static char[] array;
	public static JobManagement jb;
	public static String genSequenceFromBigInt(BigInteger num) {
		StringBuilder sb = new StringBuilder();
		while(num.compareTo(BigInteger.ZERO) == 1) {
			int tempNum = num.mod(BigInteger.valueOf(array.length)).intValue();
			if (tempNum == 0) {
				tempNum += 1;
			}
			sb.append(array[tempNum]);
			num = num.divide(BigInteger.valueOf(array.length));
		}
		return sb.reverse().toString();
	}
	public static String getLowest() {
		BigInteger lowest = BigInteger.valueOf(-1);
		for (int i = 0; i < clients.size(); i++) {
			c = (Client)clients.get(i);
			if (c.isConnected()) {
				if (c.clientLast.compareTo(BigInteger.valueOf(0)) == 0) {
					continue;
				}
				if (lowest.compareTo(BigInteger.valueOf(-1)) == 0) {
					lowest = c.clientLast;
				}
				if (c.clientLast.compareTo(lowest) == -1) {
					lowest = c.clientLast;
				}
			}
		}
		return genSequenceFromBigInt(lowest);
	}
	public static byte[] dehexify(String hexString) {
		if (hexString.length()%2 == 1)
			throw new IllegalArgumentException("Invalid length");       
		int len = hexString.length()/2;
		byte[] bytes = new byte[len];
		for (int i=0; i<len; i++) {
			int index = i*2;
			bytes[i] = (byte)Integer.parseInt(hexString.substring(index, index+2), 16);
		}
		return bytes;
	}
	public static void main (String[] args) {
		out("Type help for information");
		out("");
		int number;
		jb = new JobManagement();
		(new Thread(jb)).start();
		String command;
		String split[];
		(new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						if (cracking) {
							int total = 0;
							for (int i = 0; i < clients.size(); i ++) {
								c = (Client)clients.get(i);
								if (c.isConnected() && c.clientHashRate != 250) {
									total += c.clientHashRate;
								}
							}
							if (total != 0) {
								System.out.println("Speed: " + total + "KH/s Sequence: " + getLowest());
							}
						}
					} catch (Exception e) {  }
					try { Thread.sleep(1000); } catch (Exception e) {  }
				}
			}
		})).start();
		while (true) {
			Scanner in = new Scanner(System.in);
			command = in.nextLine();
			split = command.split(" ");
			if (split[0].equals("list")) {
				if (getConnectedNumbers() == 0) {
					out("There are no connected clients");
				} else {
					for (int i = 0; i < clients.size(); i ++) {
						c = (Client)clients.get(i);
						if (c.isConnected()) {
							int rate = c.clientHashRate;
							if (rate == 250) {
								rate = 0;
							}
							out("Client: " + c.cs.getInetAddress().getHostAddress() + " " + rate + "KH/s Sequence: " + genSequenceFromBigInt(c.clientLast));
						}
					}
				}
			}
			else if (split[0].equals("exit")) {
				break;
			} else if (split[0].equals("listener")) {
				split = command.split(" ");
				try {
					if (split[1].equals("add")) {
						try {
							number = Integer.parseInt(split[2]);
							if (!listeners.contains(number)) {
								listeners.add(number);
								out("Added listener " + number);
								addListener(number);
							} else {
								out("Listener " + number + " already existed");
							}
						} catch (Exception e) { out("Please use a number as an argument"); }
					} else if (split[1].equals("del")) {
						try {
							number = Integer.parseInt(split[2]);
							if (!listeners.contains(number)) {
								out("Listener " + number + " already existed");
							} else {
								listeners.remove((Object)number);
								out("Deleted listener " + number);
								delListener(number);
							}
						} catch (Exception e) { out("Please use a number as an argument"); }
					} else if (split[1].equals("list")) {
						if (listeners.size() == 0) {
							System.out.println("There are no listeners");
						} else {
							for (int i = 0; i < listeners.size(); i ++) {
								out(listeners.get(i) + "");
							}
						}
					} else {
						out("Accepted arguments for listener (add|del|list) number");
					}
				} catch (Exception e) { out("Accepted arguments for listener (add|del|list) number"); }
			} else if (split[0].equals("rate")) {
				out(getHashRate() + "KH/s");
			} else if (split[0].equals("stop")) {
				stopCracking();
			} else if (split[0].equals("crack")) {
				try {
					if (!cracking) {
						String arguments = command.replaceAll(split[1], "");
						currentHashString = split[1];
						try {
							dehexify(currentHashString);
						} catch (Exception e) { System.out.println("Not a valid md5 hash"); continue; }
						crackNumbers = arguments.contains("numbers");
						crackSpecial = arguments.contains("special");
						crackUpperCase = arguments.contains("upper");
						threads = Integer.parseInt(split[3]);
						int crackNumber = Integer.valueOf(split[2]);
						decideArray();
						jb.startCrackNumber = BigInteger.valueOf(array.length - 1).pow(crackNumber - 1).add(BigInteger.valueOf(1));
						if (jb.startCrackNumber.compareTo(BigInteger.valueOf(2)) == 0) {
							jb.startCrackNumber = BigInteger.valueOf(1);
						}
					}
					startCracking();
				} catch (Exception e) {
					e.printStackTrace();
					out("crack (hash) (start length) (threads) [numbers] [special] [upper] - starts cracking");
				}
			}
			else if (split[0].equals("status")) {
				if (cracking) {
					out("Solving " + currentHashString + " @" + getHashRate() + "KH/s for " + (Float.valueOf(System.currentTimeMillis() - initiateTime)/1000F) + "ms");
				} else {
					out("Not solving a hash");
				}
			} else if (split[0].equals("hash")) {
				try {
					System.out.println(toHex(MessageDigest.getInstance("MD5").digest(split[1].getBytes())));
				} catch (Exception e) { out("hash (value) - md5 hashes a value"); }
			}
			else if (split[0].equals("help")) {
				out("listener (add|del|list) number - adds a port on which to listen");
				out("help - outputs the help");
				out("rate - retrieves hash rate (kh/s)");
				out("crack (hash) (start length) (threads) [numbers] [special] [upper] - starts cracking");
				out("list - lists connected clients");
				out("stop - stops cracking");
				out("status - gives a status on the current crack");
				out("hash (value) - md5 hashes a value");
			} else {
				out("Unknown command. Try help");
			}
		}
		out("Thankyou for using this tool");
		System.exit(0);
	}
	public static int getConnectedNumbers() {
		int num = 0;
		for (int i = 0; i < clients.size(); i ++) {
			c = (Client)clients.get(i);
			if (c.isConnected()) {
				num += 1;
			}
		}
		return num;
	}
	public static void stopCracking() {
		if (cracking) {
			cracking = false;
			out("Stopped cracking");
		} else {
			out("The hash solver is not cracking");
		}
	}
	public static void startCracking() {
		if (cracking) {
			out("Already solving a hash");
		} else {
			initiateTime = System.currentTimeMillis();
			cracking = true;
			out("Attempting to solve hash " + currentHashString);
		}
	}
	public static float getHashRate() {
		float i = 0;
		for (int i2 = 0; i2 < clients.size(); i2 ++) {
			c = (Client)clients.get(i2);
			if (c.connected) {
				i += c.clientHashRate;
			}
		}
		return i;
	}
	public static void delListener(int number) {
		try {
			for (int i = 0; i < listenerClasses.size(); i ++) {
				l = (Listener)listenerClasses.get(i);
				if (l.port == number) {
					l.stop();
					l.ss.close();
					listenerClasses.remove(i);
					for (int i2 = 0; i2 < listeners.size(); i2 ++) {
						if (l.port == (Integer)listeners.get(i2)) {
							listeners.remove(i2);
							break;
						}
					}
					break;
				}
			}
		} catch (Exception e) {  }
	}
	public static void success(String hash, String ip) {
		out("Solved hash: " + currentHashString + " as " + hash + " in " + (System.currentTimeMillis() - initiateTime) + "ms On server " + ip);
		stopCracking();
	}
	public static void addListener(int number) {
		l = new Listener(number);
		listenerClasses.add(l);
		Thread t = new Thread(l);
		t.start();
	}
	public static void out(String output) {
		System.out.println(output);
	}
	public static void decideArray() {
		if (!crackNumbers && !crackSpecial && !crackUpperCase) {
			array = comb0;
		}
		if (crackNumbers && crackSpecial && crackUpperCase) {
			array = comb1;
		}
		if (!crackNumbers && !crackSpecial && crackUpperCase) {
			array = comb2;
		}
		if (crackNumbers && !crackSpecial && !crackUpperCase) {
			array = comb3;
		}
		if (!crackNumbers && crackSpecial && !crackUpperCase) {
			array = comb4;
		}
		if (crackNumbers && !crackSpecial && crackUpperCase) {
			array = comb5;
		}
		if (!crackNumbers && crackSpecial && crackUpperCase) {
			array = comb6;
		}
		if (crackNumbers && crackSpecial && !crackUpperCase) {
			array = comb7;
		}
	}
	public static String toHex(byte[] b) {
		String result = "";
		for (int i = 0; i < b.length; i++) {
			result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
		}
		return result;
	}
	public static char[] comb0 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z'};
	public static char[] comb1 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0','!','£','$','%',',','.','?','@','#'};
	public static char[] comb2 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z'};
	public static char[] comb3 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2','3','4','5','6','7','8','9','0'};
	public static char[] comb4 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','!','£','$','%',',','.','?','@','#'};
	public static char[] comb5 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','1','2','3','4','5','6','7','8','9','0'};
	public static char[] comb6 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','A','B','C','D','E','F','G','H','I','J','K','L','M','N','O','P','Q','R','S','T','U','V','W','X','Y','Z','!','£','$','%',',','.','?','@','#'};
	public static char[] comb7 = new char[] {'a','a','b','c','d','e','f','g','h','i','j','k','l','m','n','o','p','q','r','s','t','u','v','w','x','y','z','1','2','3','4','5','6','7','8','9','0','!','£','$','%',',','.','?','@','#'};
}