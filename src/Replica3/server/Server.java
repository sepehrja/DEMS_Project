package Replica3.server;

public class Server {
	public static void main(String[] args) {
		new Thread() {
			@Override 
			public void run() {
				try {
					Montreal.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		new Thread() {
			@Override 
			public void run() {
				try {
					Quebec.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		
		new Thread() {
			@Override 
			public void run() {
				try {
					Sherbrook.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();
		
/*		new Thread() {
			@Override 
			public void run() {
				try {
					Client1.main(args);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}.start();*/
	}
}
