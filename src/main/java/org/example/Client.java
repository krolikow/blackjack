package org.example;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import jakarta.json.Json;
import jakarta.json.JsonWriter;
import jakarta.json.JsonObject;
import com.fasterxml.jackson.databind.ObjectMapper;

public class Client {
    private Socket socket;
    private DataInputStream fromServer;
    private DataOutputStream toServer;
    private boolean continueToPlay = true;
    private int score;

    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;

    boolean firstPlayerPassed = false;
    boolean secondPlayerPassed = false;

    private int myNumber;
    private int otherNumber;

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

    public Client(Socket socket) {
        try {
            this.socket = socket;
            this.fromServer = new DataInputStream(socket.getInputStream());
            this.toServer = new DataOutputStream(socket.getOutputStream());
            this.score = 0;

        } catch (IOException e) {
            closeEverything(socket, fromServer, toServer);
        }
    }

    private String buildJsonData(int player, String message) {
        JsonObject jsonObject = Json.createObjectBuilder().add("message", player +": "+message).build();
        StringWriter stringWriter = new StringWriter();
        try(JsonWriter jsonWriter = Json.createWriter(stringWriter)){jsonWriter.writeObject(jsonObject);}
        return stringWriter.toString();
    }

    public void run() {
        try {
            int player = fromServer.readInt(); // ktory player

            if (player == PLAYER1) {
                System.out.println("You are player 1. Waiting for player 2 to join.");
                myNumber = 1;
                otherNumber = 2;
                //notification that player 2 joined
                fromServer.readInt();
            } else if (player == PLAYER2) {
                System.out.println("You are player 2. Waiting for player 1 to move.");
                myNumber = 2;
                otherNumber = 1;
            }

            Scanner scanner = new Scanner(System.in);
            while (continueToPlay && socket.isConnected()) {

                if (player == PLAYER1) {
                    System.out.println("Im in first");
                    if (!firstPlayerPassed) {
                        System.out.print("You have " + score + ". Enter a command: ");
                        String command = scanner.nextLine();
                        if (command.matches("T")) { //dobiera
                            toServer.writeInt(TAKE);
                            int card = fromServer.readInt();
                            score += card;
                            System.out.println("CARD: " + card);
                        } else if (command.matches("N")) { // pasuje
                            toServer.writeInt(PLAYER1_PASS);
                        }
                        toServer.flush();
                    } else {
                        toServer.writeInt(WAIT);
                    }
                    receiveInfoFromServer();

                    if (!continueToPlay) break;
                } else if (player == PLAYER2) {
                    System.out.println("Im in 2");
                    System.out.println(firstPlayerPassed);

                    receiveInfoFromServer();
                    if (!continueToPlay) break;

                    if(!secondPlayerPassed){
                        System.out.print("You have " + score + ". Enter a command: ");
                        String command = scanner.nextLine();

                        if (command.matches("T")) { //dobiera
                            toServer.writeInt(TAKE);
                            int card = fromServer.readInt();
                            score += card;
                            System.out.println("CARD: " + card);
                        }
                        if (command.matches("N")) { // pasuje
                            toServer.writeInt(PLAYER2_PASS);
                        }
                    }
                    else{
                        toServer.writeInt(WAIT);
                    }
                    toServer.flush();

                }
            }
        } catch (IOException e) {
            closeEverything(socket, fromServer, toServer);
        }
    }

    private void receiveMove() throws IOException {
        int command = fromServer.readInt();
        // score actualize
    }


    private void receiveInfoFromServer() throws IOException {
        int status = fromServer.readInt();
        if (status == PLAYER1_WON) {
            continueToPlay = false;
            if (myNumber == 1) { // zmienic na player
                System.out.println("You won");
            } else if (myNumber == 2) {
                System.out.println("Player 1 has won");
            }
        } else if (status == PLAYER2_WON) {
            continueToPlay = false;
            if (myNumber == 2) {
                System.out.println("You won");
            } else if (myNumber == 1) {
                System.out.println("Player 2 has won");
            }
        } else if (status == DRAW) {
            continueToPlay = false;
            System.out.println("It's a draw");
            if (myNumber == 2) {
                receiveMove();
            }
        } else if (status == PLAYER1_PASS) {
            firstPlayerPassed = true;
            if (myNumber == 1) {
                firstPlayerScore = fromServer.readInt();
                System.out.println("You passed. You have " + firstPlayerScore);
            } else if (myNumber == 2) {
                System.out.println("First player passed");
                receiveMove();
            }
        } else if (status == PLAYER2_PASS) {
            secondPlayerPassed = true;
            if (myNumber == 2) {
                secondPlayerScore = fromServer.readInt();
                System.out.println("You passed. You have " + secondPlayerScore);
            } else if (myNumber == 1) {
                System.out.println("Second player passed");
                receiveMove();
            }
        } else {
            receiveMove();
        }
    }


    public void closeEverything(Socket socket, InputStream in, OutputStream out) {
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
