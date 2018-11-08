package controllers;

import interfaces.MonitorInterface;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Monitor implements MonitorInterface {

    private static File localFolder;

    private static Monitor instance;

    /**
     * Monitor constructor is kept private to achieve the singleton pattern
     */
    private Monitor() {}

    /**
     * Returns an instance of the Monitor class
     * @return The one and only instance allowed of the monitor class
     */
    public static Monitor getInstance() {

        if (instance == null) {
            instance = new Monitor();
            localFolder = new File("local");

            if (!localFolder.exists()) {
                localFolder.mkdirs();
            }
        }

        return instance;
    }

    @Override
    public String[] getSharedNames(Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos) {

        try {
            oos.writeObject(1);
            return (String[]) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a String array with the names of all files in the client folder
     * @return A String array of the client folder file names
     */
    @Override
    public String[] getClientNames() {
        return localFolder.list((dir, name) -> name.endsWith(".mp3"));
    }

    /**
     * Copies the selected file from the shared folder into the client folder
     * @param name The name of the file which will be copied into the client folder
     */
    @Override
    public byte[] getFile(String name, Socket clientSocket, ObjectInputStream ois, ObjectOutputStream oos) {

        try {
            oos.writeObject(3);
            oos.writeObject(name);

            return (byte[]) ois.readObject();

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Uploads a file to the shared folder
     * @param fileContents The file to upload to the shared folder
     */
    @Override
    public synchronized void uploadFile(String fileName, byte[] fileContents, Socket clientSocket, ObjectOutputStream oos) {
        try {
            oos.writeObject(4);
            oos.writeObject(fileName);
            oos.writeObject(fileContents);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
