package Replica2.com.sepehr.Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {
    public static final int LOG_TYPE_SERVER = 1;

    public static void serverLog(String serverID, String clientID, String requestType, String requestParams, String serverResponse) throws IOException {

        if (clientID.equals("null")) {
            clientID = "Event Manager";
        }
        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " ClientID: " + clientID + " | RequestType: " + requestType + " | RequestParameters: " + requestParams + " | ServerResponse: " + serverResponse);

        printWriter.close();
    }

    public static void serverLog(String serverID, String msg) throws IOException {

        FileWriter fileWriter = new FileWriter(getFileName(serverID, LOG_TYPE_SERVER), true);
        PrintWriter printWriter = new PrintWriter(fileWriter);
        printWriter.println("DATE: " + getFormattedDate() + " " + msg);

        printWriter.close();
    }

    private static String getFileName(String ID, int logType) {
        final String dir = System.getProperty("user.dir");
        String fileName = dir;
        if (logType == LOG_TYPE_SERVER) {
            if (ID.equalsIgnoreCase("MTL")) {
                fileName = dir + "\\src\\Replica2\\com\\sepehr\\Logs\\Server\\Montreal.txt";
            } else if (ID.equalsIgnoreCase("SHE")) {
                fileName = dir + "\\src\\Replica2\\com\\sepehr\\Logs\\Server\\Sherbrooke.txt";
            } else if (ID.equalsIgnoreCase("QUE")) {
                fileName = dir + "\\src\\Replica2\\com\\sepehr\\Logs\\Server\\Quebec.txt";
            }
        }
        return fileName;
    }

    private static String getFormattedDate() {
        Date date = new Date();

        String strDateFormat = "yyyy-MM-dd hh:mm:ss a";

        DateFormat dateFormat = new SimpleDateFormat(strDateFormat);

        return dateFormat.format(date);
    }

}
