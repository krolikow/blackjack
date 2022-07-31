package org.example;

import jakarta.json.JsonObject;

import javax.websocket.server.ServerEndpoint;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import org.json.JSONObject;


public class Server {
    private final ServerSocket serverSocket;

    public Server(ServerSocket serverSocket) {
        this.serverSocket = serverSocket;
    }

    public static int getRandomNumber() {
        return (int) ((Math.random() * 2)); // jesli 0 to zostawiam ustaloną kolejnosc, jesli 1 to zmieniam
    }

    public void startServer() {
        try {
            while (!serverSocket.isClosed()) {
                int value = getRandomNumber();
                // połączenie z pierwszym graczem
                Socket firstPlayer = serverSocket.accept();
                System.out.println("A new client has connected!");

                // powiadomienie pierwszego gracza, że jest pierwszym graczem
                OutputStreamWriter fristPlayerWriter = new OutputStreamWriter(firstPlayer.getOutputStream());
                JSONObject jsonObject1 = new JSONObject();
                if(value == 0){
                    jsonObject1.put("player", Command.PLAYER1);
                }
                else{
                    jsonObject1.put("player", Command.PLAYER2);
                }

                jsonObject1.put("value", value);
                fristPlayerWriter.write(jsonObject1.toString() + '\n');
                fristPlayerWriter.flush();

                // połączenie z drugim graczem
                Socket secondPlayer = serverSocket.accept();
                System.out.println("A new client has connected!");

                // powiadomienie drugiego gracza, że jest drugim graczem
                OutputStreamWriter secondPlayerWriter = new OutputStreamWriter(secondPlayer.getOutputStream());
                JSONObject jsonObject2 = new JSONObject();
                if(value == 0){
                    jsonObject2.put("player", Command.PLAYER2);
                }
                else{
                    jsonObject2.put("player", Command.PLAYER1);
                }
                jsonObject2.put("value", value);
                secondPlayerWriter.write(jsonObject2.toString() +'\n');
                secondPlayerWriter.flush();

                // rozpoczęcie wątku dla dwóch graczy
                ClientHandler clientHandler;

                if (value == 0){
                    clientHandler = new ClientHandler(firstPlayer, secondPlayer);
                }
                else{
                    clientHandler = new ClientHandler(secondPlayer, firstPlayer);
                }
                Thread thread = new Thread(clientHandler);
                thread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void closeServerSocket() {
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(1234);
        Server server = new Server(serverSocket);
        System.out.println("Starting server...");
        System.out.println("...");
        server.startServer();
    }
}
