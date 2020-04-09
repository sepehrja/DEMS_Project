package FrontEnd;

public interface FEInterface {
    void informRmHasBug(int RmNumber);

    void informRmIsDown(int RmNumber);

    void sendRequestToSequencer(MyRequest myRequest);
}
