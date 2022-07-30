package org.example;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private final ServerSocket serverSocket;

    public static int PLAYER1 = 1;
    public static int PLAYER2 = 2;
    public static int PLAYER1_WON = 3;
    public static int PLAYER2_WON = 4;
    public static int PLAYER1_PASS = 5;
    public static int PLAYER2_PASS = 6;
    public static int DRAW = 7;
    public static int CONTINUE = 8;
    public static int TAKE = 9;
    public static int WAIT = 10;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public void startServer(){
        try{
            while (!serverSocket.isClosed()){
                // połączenie z pierwszym graczem
                Socket firstPlayer = serverSocket.accept();
                System.out.println("A new client has connected! (player 1)");
                // powiadomienie pierwszego gracza, że jest pierwszym graczem
                new DataOutputStream(firstPlayer.getOutputStream()).writeInt(PLAYER1);

                // połączenie z drugim graczem
                Socket secondPlayer = serverSocket.accept();
                System.out.println("A new client has connected! (player 2)");
                // powiadomienie drugiego gracza, że jest drugim graczem
                new DataOutputStream(secondPlayer.getOutputStream()).writeInt(PLAYER2);

                // rozpoczęcie wątku dla dwóch graczy
                ClientHandler clientHandler = new ClientHandler(firstPlayer, secondPlayer);
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeServerSocket(){
        try{
            if(serverSocket!=null){
                serverSocket.close();
            }
        }
        catch(IOException e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException{
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        System.out.println("Starting server...");
        System.out.println("...");
        server.startServer();

    }
}
