package interfaces;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public interface MonitorInterface {

    String[] getSharedNames(Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos);

    String[] getClientNames();

    byte[] getFile(String name, Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos);

    void uploadFile(String fileName, byte[] fileContents, Socket clientSocket, ObjectOutputStream oos);

    boolean checkSharedFilesChanged(Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos);

}