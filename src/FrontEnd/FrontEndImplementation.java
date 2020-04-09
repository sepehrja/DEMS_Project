package FrontEnd;

import FrontEnd.ServerObjectInterfaceApp.ServerObjectInterfacePOA;
import org.omg.CORBA.ORB;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class FrontEndImplementation extends ServerObjectInterfacePOA {
    private static long DYNAMIC_TIMEOUT = 10000;
    private static int Rm1BugCount = 0;
    private static int Rm2BugCount = 0;
    private static int Rm3BugCount = 0;
    private static int Rm1NoResponseCount = 0;
    private static int Rm2NoResponseCount = 0;
    private static int Rm3NoResponseCount = 0;
    private long responseTime = DYNAMIC_TIMEOUT;
    private long startTime;
    private CountDownLatch latch;
    private FEInterface inter;
    private List<String> responses = new ArrayList<>();
    private ORB orb;

    public FrontEndImplementation(FEInterface inter) {
        super();
        this.inter = inter;
    }

    public void setORB(ORB orb_val) {
        orb = orb_val;
    }


    @Override
    public synchronized String addEvent(String managerID, String eventID, String eventType, int bookingCapacity) {
        MyRequest myRequest = new MyRequest("addEvent", managerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        myRequest.setBookingCapacity(bookingCapacity);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String removeEvent(String managerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("removeEvent", managerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String listEventAvailability(String managerID, String eventType) {
        MyRequest myRequest = new MyRequest("listEventAvailability", managerID);
        myRequest.setEventType(eventType);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String bookEvent(String customerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("bookEvent", customerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String getBookingSchedule(String customerID) {
        MyRequest myRequest = new MyRequest("getBookingSchedule", customerID);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String cancelEvent(String customerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("cancelEvent", customerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) {
        MyRequest myRequest = new MyRequest("swapEvent", customerID);
        myRequest.setEventID(oldEventID);
        myRequest.setEventType(oldEventType);
        myRequest.setNewEventID(newEventID);
        myRequest.setNewEventType(newEventType);
        sendUdpUnicastToSequencer(myRequest);
        return validateResponses(myRequest);
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }

    public void waitForResponse() {
        try {
            boolean timeoutReached = latch.await(DYNAMIC_TIMEOUT, TimeUnit.MILLISECONDS);
            if (!timeoutReached) {
                setDynamicTimout();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
//            inter.sendRequestToSequencer(myRequest);
        }
//         check result and react correspondingly
    }

    private String validateResponses(MyRequest myRequest) {
        String resp;
        switch ((int) latch.getCount()) {
            case 0:
            case 1:
            case 2:
                resp = findMajorityResponse(myRequest);
                break;
            case 3:
                resp = "Failed: No response from any server";
                Rm1NoResponseCount++;
                Rm2NoResponseCount++;
                Rm3NoResponseCount++;
                break;
            default:
                resp = "Failed: " + myRequest.noRequestSendError();
                break;
        }
        return resp;
    }

    private String findMajorityResponse(MyRequest myRequest) {
        String res1 = "null";
        String res2 = "null";
        String res3 = "null";
        for (String response :
                responses) {
            //TODO: check only responses that match myRequest
            if (true) {
                //TODO: add each rm response to its corresponding response
            }
        }
        if (res1.equals("null")) {
            rmDown(1);
        } else {
            if (res1.equals(res2)) {
                if (!res3.equals(res1)) {
                    rmBugFound(3);
                }
                return res1;
            } else if (res1.equals(res3)) {
                if (!res2.equals(res1)) {
                    rmBugFound(2);
                }
                return res1;
            } else {
                if (!res2.equals("null") && res2.equals(res3)) {
                    rmBugFound(1);
                }
                return res2;
            }
        }
        if (res2.equals("null")) {
            rmDown(2);
        } else {
            if (res2.equals(res3)) {
                if (!res1.equals(res2)) {
                    rmBugFound(1);
                }
                return res2;
            } else if (res2.equals(res1)) {
                if (!res3.equals(res2)) {
                    rmBugFound(3);
                }
                return res2;
            } else {
                if (!res1.equals("null") && res1.equals(res3)) {
                    rmBugFound(2);
                }
                return res1;
            }
        }
        if (res3.equals("null")) {
            rmDown(3);
        } else {
            if (res3.equals(res2)) {
                if (!res1.equals(res3)) {
                    rmBugFound(1);
                }
                return res3;
            } else if (res3.equals(res1)) {
                if (!res2.equals(res3)) {
                    rmBugFound(2);
                }
                return res3;
            } else {
                if (!res2.equals("null") && res2.equals(res1)) {
                    rmBugFound(3);
                }
                return res1;
            }
        }
        return "Failed: majority response not found";
    }

    private void rmBugFound(int rmNumber) {
        switch (rmNumber) {
            case 1:
                Rm1BugCount++;
                if (Rm1BugCount == 3) {
                    Rm1BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;
            case 2:
                Rm2BugCount++;
                if (Rm2BugCount == 3) {
                    Rm2BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;

            case 3:
                Rm3BugCount++;
                if (Rm3BugCount == 3) {
                    Rm3BugCount = 0;
                    inter.informRmHasBug(rmNumber);
                }
                break;
        }
    }

    private void rmDown(int rmNumber) {
        switch (rmNumber) {
            case 1:
                Rm1NoResponseCount++;
                if (Rm1NoResponseCount == 3) {
                    Rm1NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;
            case 2:
                Rm2NoResponseCount++;
                if (Rm2NoResponseCount == 3) {
                    Rm2NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;

            case 3:
                Rm3NoResponseCount++;
                if (Rm3NoResponseCount == 3) {
                    Rm3NoResponseCount = 0;
                    inter.informRmIsDown(rmNumber);
                }
                break;
        }
    }

    private void setDynamicTimout() {
        DYNAMIC_TIMEOUT = responseTime * 2;
    }

    private void notifyOKCommandReceived() {
        latch.countDown();
    }

    public void addReceivedResponse(String res) {
        long endTime = System.nanoTime();
        responseTime = (endTime - startTime) / 1000000;
        System.out.println("Current Response time is: " + responseTime);
        responses.add(res);
        notifyOKCommandReceived();
    }

    private void sendUdpUnicastToSequencer(MyRequest myRequest) {
        startTime = System.nanoTime();
        inter.sendRequestToSequencer(myRequest);
        latch = new CountDownLatch(3);
        waitForResponse();
    }
}
