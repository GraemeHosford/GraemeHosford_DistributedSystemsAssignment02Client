package threads;

import controllers.Monitor;
import interfaces.SonglistChangedListener;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientSideCheckForChange extends Thread {

    private Socket clientSocket;
    private SonglistChangedListener songChangedListener;
    private ObjectInputStream ois;
    private ObjectOutputStream oos;

    public ClientSideCheckForChange(Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos, SonglistChangedListener songChangedListener) {
        this.clientSocket = clientSocket;
        this.ois = ois;
        this.oos = oos;
        this.songChangedListener = songChangedListener;
    }

    @Override
    public void run() {
        super.run();

        try {

            while (true) {

                boolean songChanged = Monitor.getInstance().checkSharedFilesChanged(clientSocket, ois, oos);

                if (songChanged) {
                    songChangedListener.onSonglistChnaged();
                }

                Thread.sleep(20000);

            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
