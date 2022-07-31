package org.example;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import org.json.JSONObject;

public class Client {
    private Socket socket;
    private BufferedReader fromServer;
    private OutputStreamWriter toServer;
    private boolean continueToPlay = true;
    private Command player;

    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;

    private final JSONObject firstPlayerResponse = new JSONObject();
    private final JSONObject secondPlayerResponse = new JSONObject();

    boolean firstPlayerPassed = false;
    boolean secondPlayerPassed = false;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.toServer = new OutputStreamWriter(socket.getOutputStream());

        } catch (IOException e) {
            closeEverything(socket, fromServer, toServer);
        }
    }

    public void take(Command player) throws IOException {
        JSONObject response = firstPlayerResponse;

        if(player == Command.PLAYER2){
            response = secondPlayerResponse;
        }

        response.put("command", Command.TAKE);
        toServer.write(response.toString() + '\n');
        toServer.flush();
        String serverResponseString = fromServer.readLine();
        JSONObject serverResponse = new JSONObject(serverResponseString);
        int card = serverResponse.getInt("card");

        if(player == Command.PLAYER1){
            firstPlayerScore += card;
        }
        if(player == Command.PLAYER2){
            secondPlayerScore += card;
        }
        System.out.println("CARD VALUE: " + card);
    }

    public void run() {
        try {
            String playerString = fromServer.readLine(); // informacja ktorym jestes graczem
            JSONObject playerJSON = new JSONObject(playerString);
            player = playerJSON.getEnum(Command.class, "player");
            int value = playerJSON.getInt("value"); // ta wartosc jest przesylana jedynie po to, zeby sensownie wyswietlic ponizsze komunikaty
            if (player == Command.PLAYER1) {
                if(value==0){
                    System.out.println("You are player 1. You start. Waiting for player 2 to join.");
                }
                else{
                    System.out.println("You are player 1. You start.");
                }
                //powiadomienie, że drugi gracz dołaczył
                fromServer.readLine();
            } else if (player == Command.PLAYER2) {
                if(value == 0){
                    System.out.println("You are player 2. Waiting for player 1 to move.");
                }
                else{
                    System.out.println("You are player 2. Waiting for player 1 to join.");
                }
            }

            Scanner scanner = new Scanner(System.in);

            while (continueToPlay && socket.isConnected()) { // wlasciwa rozgrywka
                if (player == Command.PLAYER1) {
                    if (!firstPlayerPassed) {
                        System.out.print("You have " + firstPlayerScore + ". Your opponent has " + secondPlayerScore +". Enter a command: ");
                        String command = scanner.nextLine();

                        if (command.matches("T")) { //dobiera
                            take(Command.PLAYER1);
                        } else if (command.matches("N")) { // pasuje
                            firstPlayerResponse.put("command", Command.PLAYER1_PASS);
                            toServer.write(firstPlayerResponse.toString() + '\n');
                            toServer.flush();
                        }
                    } else {
                        firstPlayerResponse.put("command", Command.WAIT);
                        toServer.write(firstPlayerResponse.toString() + '\n');
                        toServer.flush();
                    }
                    receiveInfoFromServer();
                    if (!continueToPlay) break;
                } else if (player == Command.PLAYER2) {

                    receiveInfoFromServer();
                    if (!continueToPlay) break;

                    if (!secondPlayerPassed) {
                        System.out.print("You have " + secondPlayerScore + ". Your opponent has " + firstPlayerScore +". Enter a command: ");
                        String command = scanner.nextLine();

                        if (command.matches("T")) { //dobiera
                            take(Command.PLAYER2);
                        }
                        if (command.matches("N")) { // pasuje
                            secondPlayerResponse.put("command", Command.PLAYER2_PASS);
                            toServer.write(secondPlayerResponse.toString() + '\n');
                            toServer.flush();
                        }
                    } else {
                        secondPlayerResponse.put("command", Command.WAIT);
                        toServer.write(secondPlayerResponse.toString() + '\n');
                        toServer.flush();
                    }
                }
            }
        } catch (
                IOException e) {
            closeEverything(socket, fromServer, toServer);
        }

    }

    private void receiveInfoFromServer() throws IOException {
        String resultString = fromServer.readLine();
        JSONObject result = new JSONObject(resultString);
        if (!result.has("result")) return;
        Command status = result.getEnum(Command.class, "result");
        firstPlayerScore = result.getInt("firstPlayerScore");
        secondPlayerScore = result.getInt("secondPlayerScore");
        if (status == Command.PLAYER1_WON) {
            continueToPlay = false;
            if (player == Command.PLAYER1) {
                System.out.println("You won. You have " + firstPlayerScore);
            } else if (player == Command.PLAYER2) {
                System.out.println("You lose. You have " + secondPlayerScore);
            }
        } else if (status == Command.PLAYER2_WON) {
            continueToPlay = false;
            if (player == Command.PLAYER2) {
                System.out.println("You won. You have " + secondPlayerScore);
            } else if (player == Command.PLAYER1) {
                System.out.println("You lose. You have " + firstPlayerScore);
            }
        } else if (status == Command.DRAW) {
            continueToPlay = false;
            if (player == Command.PLAYER2) {
                System.out.println("It's a draw. You have " + secondPlayerScore);
            } else if (player == Command.PLAYER1) {
                System.out.println("It's a draw. You have " + firstPlayerScore);
            }
        } else if (status == Command.PLAYER1_PASS) {
            firstPlayerPassed = true;
            if (player == Command.PLAYER1) {
                System.out.println("You passed. You have " + firstPlayerScore);
            } else if (player == Command.PLAYER2) {
                System.out.println("First player passed.");
            }
        } else if (status == Command.PLAYER2_PASS) {
            secondPlayerPassed = true;
            if (player == Command.PLAYER2) {
                System.out.println("You passed. You have " + secondPlayerScore);
            } else if (player == Command.PLAYER1) {
                System.out.println("Second player passed.");
            }
        }
    }


    public void closeEverything(Socket socket, BufferedReader in, OutputStreamWriter out) {
        try {
            if (in != null) {
                in.close();
            }
            if (out != null) {
                out.close();
            }
            if (socket != null) {
                socket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        Socket socket = new Socket("localhost", 1234);
        Client client = new Client(socket);
        client.run();
    }
}
