import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Main {
	public static BigInteger[] bounds = new BigInteger[2];
	public static BigInteger lowestNumber = BigInteger.valueOf(0);
	public InetSocketAddress connectionAddress;
	public Socket connectionSocket;
	public volatile DataOutputStream dos;
	public volatile DataInputStream dis;
	public static boolean requestNew = false;
	public static boolean cracking = false;
	public static boolean upperCase = false;
	public static boolean numbers = false;
	public static boolean special = false;
	public static byte[] comparisonHash;
	public static long hashesThisSecond = 0;
	public static long lastKeepAlive = 0L;
	public ListenerThread lt;
	public static String hostIp;
	public static int hostPort;
	public static int maxThreads;
	public static int crackThreads = 0;
	public static boolean connected = false;
	public static boolean sendAverage = false;
	public static int average = -1;
	public static String solution = "";
	public static boolean foundSolution = false;

	public static void main (String args[]) {
		new Main();
	}
	public Main() {
		initiate();
	}
	public void initiate() {
		maxThreads = Runtime.getRuntime().availableProcessors();
		try {
			hostIp = getFileString("ip.dat");
			hostPort = Integer.parseInt(getFileString("port.dat"));
		} catch (Exception e) { out("[FATAL]Failed to load settings"); System.exit(1); }
		(new Thread(new Runnable() {
			public void run() {
				while (true) {
					try {
						int total = 0;
						BigInteger storage = BigInteger.valueOf(0);
						for (int i = 0; i < CrackManager.cr.length; i ++) {
							if (storage.compareTo(BigInteger.valueOf(0)) == 0) {
								storage = CrackManager.bi[i];
							}
							if (CrackManager.bi[i].compareTo(storage) == -1) {
								storage = CrackManager.bi[i];
							}
							total += CrackManager.cr[i].hashesPerSecond;
						}
						lowestNumber = storage;
						hashesThisSecond = total;
					} catch (Exception e) {  }
					try { Thread.sleep(1000); } catch (Exception e) {  }
				}
			}
		})).start();
		(new Thread(new CrackManager())).start();
		Runtime.getRuntime().addShutdownHook((new Thread(new Runnable() {
			public void run() {
				try {
					dos.writeByte(0x01);
					dos.flush();
				} catch (Exception e) {  }
			}
		})));
		while (true) {
			try {
				connectionSocket = new Socket();
				connectionAddress = new InetSocketAddress(hostIp, hostPort);
				connectionSocket.setSoTimeout(3000);
				connectionSocket.connect(connectionAddress);
				dos = new DataOutputStream(connectionSocket.getOutputStream());
				dis = new DataInputStream(connectionSocket.getInputStream());
				dis.read();
				dos.writeByte(0x00);
				dos.flush();
				System.out.println("Connected to server");
				lt = new ListenerThread(dis);
				(new Thread(lt)).start();
				connected = true;
				(new Thread(new Runnable() {
					public void run() {
						while (connected) {
							try {
								if (System.currentTimeMillis() - lastKeepAlive > 1000) {
									dos.writeByte(0x00);
									dos.flush();
									if (cracking) {
										dos.writeByte(0x02);
										dos.flush();
										dos.writeInt(Math.round(hashesThisSecond/1000F));
										dos.flush();
										dos.writeByte(0x03);
										dos.flush();
										writeBigInteger(lowestNumber, dos);
										dos.flush();
									}
									lastKeepAlive = System.currentTimeMillis();
								}
								if (foundSolution) {
									dos.write(0x04);
									dos.flush();
									writeString(solution, dos);
									dos.flush();
									foundSolution = false;
								}
								if (sendAverage) {
									sendAverage = false;
									dos.write(0x05);
									dos.flush();
									dos.writeInt(average);
									dos.flush();
								}
								if (requestNew) {
									requestNew = false;
									dos.write(0x06);
									dos.flush();
								}
								Thread.sleep(20);
							} catch (Exception e) {  }
						}
					}
				})).start();
				while (connected) {
					Thread.sleep(200);
				}
			} catch (Exception e) { connected = false; }
			try { connected = false; Thread.sleep(10000); } catch (Exception e) {  }
		}
	}
	public String getFileString(String filename) throws Exception {
		String s13 = "";
		InputStream in = getClass().getClassLoader().getResourceAsStream(filename);
		DataInputStream datainputstream = new DataInputStream(in);
		BufferedReader bufferedreader = new BufferedReader(new InputStreamReader(datainputstream));
		String line;
		while ((line = bufferedreader.readLine()) != null) {
			s13 += line;
		}
		bufferedreader.close();
		datainputstream.close();
		in.close();
		return s13;
	}
	public static BigInteger power (int number, int exponent) {
		BigInteger num = BigInteger.valueOf(1);
		for (int i = 0; i < exponent; i ++) {
			num = num.multiply(BigInteger.valueOf((long)(number)));
		}
		return num;
	}
	public static void out(String output) {
		System.out.println(output);
	}
	public void writeString(String par0Str, DataOutputStream par1DataOutputStream) throws IOException {
		if (par0Str.length() > 32767) {
			throw new IOException("String too big");
		} else {
			par1DataOutputStream.writeShort(par0Str.length());
			par1DataOutputStream.writeChars(par0Str);
			return;
		}
	}
	public void writeBigInteger(BigInteger big, DataOutputStream par1DataOutputStream) throws IOException {
		byte[] data = big.toByteArray();
		int length = data.length;
		par1DataOutputStream.writeInt(length);
		for (int i = 0; i < length; i++) {
			par1DataOutputStream.writeByte(data[i]);
		}
		return;
	}
}
