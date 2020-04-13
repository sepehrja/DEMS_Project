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
    private final FEInterface inter;
    private final List<RmResponse> responses = new ArrayList<>();
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
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:addEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String removeEvent(String managerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("removeEvent", managerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:removeEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String listEventAvailability(String managerID, String eventType) {
        MyRequest myRequest = new MyRequest("listEventAvailability", managerID);
        myRequest.setEventType(eventType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:listEventAvailability>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String bookEvent(String customerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("bookEvent", customerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:bookEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String getBookingSchedule(String customerID) {
        MyRequest myRequest = new MyRequest("getBookingSchedule", customerID);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:getBookingSchedule>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String cancelEvent(String customerID, String eventID, String eventType) {
        MyRequest myRequest = new MyRequest("cancelEvent", customerID);
        myRequest.setEventID(eventID);
        myRequest.setEventType(eventType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:cancelEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public synchronized String swapEvent(String customerID, String newEventID, String newEventType, String oldEventID, String oldEventType) {
        MyRequest myRequest = new MyRequest("swapEvent", customerID);
        myRequest.setEventID(newEventID);
        myRequest.setEventType(newEventType);
        myRequest.setOldEventID(oldEventID);
        myRequest.setOldEventType(oldEventType);
        myRequest.setSequenceNumber(sendUdpUnicastToSequencer(myRequest));
        System.out.println("FE Implementation:swapEvent>>>" + myRequest.toString());
        return validateResponses(myRequest);
    }

    @Override
    public void shutdown() {
        orb.shutdown(false);
    }

    public void waitForResponse() {
        try {
            System.out.println("FE Implementation:waitForResponse>>>ResponsesRemain" + latch.getCount());
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
                resp = "Fail: No response from any server";
                System.out.println(resp);
                if (myRequest.haveRetries()) {
                    myRequest.countRetry();
                    resp = retryRequest(myRequest);
                }
                Rm1NoResponseCount++;
                Rm2NoResponseCount++;
                Rm3NoResponseCount++;
                break;
            default:
                resp = "Fail: " + myRequest.noRequestSendError();
                break;
        }
        System.out.println("FE Implementation:validateResponses>>>Responses received:" + latch.getCount() + " >>>Response to be sent to client " + resp);
        return resp;
    }

    private String findMajorityResponse(MyRequest myRequest) {
        RmResponse res1 = null;
        RmResponse res2 = null;
        RmResponse res3 = null;
        for (RmResponse response :
                responses) {
            if (response.getSequenceID() == myRequest.getSequenceNumber()) {
                switch (response.getRmNumber()) {
                    case 1:
                        res1 = response;
                        break;
                    case 2:
                        res2 = response;
                        break;
                    case 3:
                        res3 = response;
                        break;
                }
            }
        }
        System.out.println("FE Implementation:findMajorityResponse>>>RM1" + ((res1 != null) ? res1.getResponse() : "null"));
        System.out.println("FE Implementation:findMajorityResponse>>>RM2" + ((res2 != null) ? res2.getResponse() : "null"));
        System.out.println("FE Implementation:findMajorityResponse>>>RM3" + ((res3 != null) ? res3.getResponse() : "null"));
        if (res1 == null) {
            rmDown(1);
        } else {
            Rm1NoResponseCount = 0;
            if (res1.equals(res2)) {
                if (!res1.equals(res3)) {
                    rmBugFound(3);
                }
                return res1.getResponse();
            } else if (res1.equals(res3)) {
                if (!res1.equals(res2)) {
                    rmBugFound(2);
                }
                return res1.getResponse();
            } else {
//                if (res2 != null && res2.equals(res3)) {
                rmBugFound(1);
//                    return res2.getResponse();
//                }
            }
        }
        if (res2 == null) {
            rmDown(2);
        } else {
            Rm2NoResponseCount = 0;
            if (res2.equals(res3)) {
                if (!res2.equals(res1)) {
                    rmBugFound(1);
                }
                return res2.getResponse();
            } else if (res2.equals(res1)) {
                if (!res2.equals(res3)) {
                    rmBugFound(3);
                }
                return res2.getResponse();
            } else {
//                if (!res1.equals("null") && res1.equals(res3)) {
                rmBugFound(2);
//                }
//                return res1;
            }
        }
        if (res3 == null) {
            rmDown(3);
        } else {
            Rm3NoResponseCount = 0;
            if (res3.equals(res2)) {
                if (!res3.equals(res1)) {
                    rmBugFound(1);
                }
                return res3.getResponse();
            } else if (res3.equals(res1)) {
                if (!res3.equals(res2)) {
                    rmBugFound(2);
                }
                return res3.getResponse();
            } else {
//                if (!res2.equals("null") && res2.equals(res1)) {
                rmBugFound(3);
//                }
//                return res1;
            }
        }
        return "Fail: majority response not found";
    }

    private void rmBugFound(int rmNumber) {
        System.out.println("FE Implementation:rmDown>>>RM1" + Rm1BugCount);
        System.out.println("FE Implementation:rmDown>>>RM2" + Rm2BugCount);
        System.out.println("FE Implementation:rmDown>>>RM3" + Rm3BugCount);
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
        System.out.println("FE Implementation:rmDown>>>RM1" + Rm1NoResponseCount);
        System.out.println("FE Implementation:rmDown>>>RM2" + Rm2NoResponseCount);
        System.out.println("FE Implementation:rmDown>>>RM3" + Rm3NoResponseCount);
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
        System.out.println("FE Implementation:setDynamicTimout>>>" + DYNAMIC_TIMEOUT);
    }

    private void notifyOKCommandReceived() {
        latch.countDown();
        System.out.println("FE Implementation:notifyOKCommandReceived>>>Response Received: Remaining responses" + latch.getCount());
    }

    public void addReceivedResponse(RmResponse res) {
        long endTime = System.nanoTime();
        responseTime = (endTime - startTime) / 1000000;
        System.out.println("Current Response time is: " + responseTime);
        responses.add(res);
        notifyOKCommandReceived();
    }

    private int sendUdpUnicastToSequencer(MyRequest myRequest) {
        startTime = System.nanoTime();
        int sequenceNumber = inter.sendRequestToSequencer(myRequest);
        myRequest.setSequenceNumber(sequenceNumber);
        latch = new CountDownLatch(3);
        waitForResponse();
        return sequenceNumber;
    }

    private String retryRequest(MyRequest myRequest) {
        System.out.println("FE Implementation:retryRequest>>>" + myRequest.toString());
        startTime = System.nanoTime();
        inter.retryRequest(myRequest);
        latch = new CountDownLatch(3);
        waitForResponse();
        return validateResponses(myRequest);
    }
}
