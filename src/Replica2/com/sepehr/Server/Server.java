package Replica2.com.sepehr.Server;

public class Server {
    public static void main(String[] args) throws Exception {
        // start orbd -ORBInitialPort 1050
        Runnable task1 = () -> {
            try {
                ServerInstance SherbrookeServer = new ServerInstance("SHE", args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable task2 = () -> {
            try {
                ServerInstance QuebecServer = new ServerInstance("MTL", args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Runnable task3 = () -> {
            try {
                ServerInstance MontrealServer = new ServerInstance("QUE", args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        Thread thread1 = new Thread(task1);
        thread1.start();
        Thread thread2 = new Thread(task2);
        thread2.start();
        Thread thread3 = new Thread(task3);
        thread3.start();
    }
}
