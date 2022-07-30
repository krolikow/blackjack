package org.example;
import java.io.*;
import java.net.Socket;
import java.util.Collections;

public class ClientHandler implements Runnable {

    private Socket firstPlayer;
    private Socket secondPlayer;
    private DataInputStream fromPlayer1;
    private DataOutputStream toPlayer1;
    private DataInputStream fromPlayer2;
    private DataOutputStream toPlayer2;
    private final Deck deck = new Deck();
    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;

    boolean firstPlayerPassed = false;
    boolean secondPlayerPassed = false;

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

    boolean check = false;

    public ClientHandler(Socket socket1, Socket socket2) {
        try {
            this.firstPlayer = socket1;
            this.secondPlayer = socket2;
            this.fromPlayer1 = new DataInputStream(firstPlayer.getInputStream());
            this.toPlayer1 = new DataOutputStream(firstPlayer.getOutputStream());
            this.fromPlayer2 = new DataInputStream(secondPlayer.getInputStream());
            this.toPlayer2 = new DataOutputStream(secondPlayer.getOutputStream());

        } catch (IOException e) {
            closeEverything(firstPlayer, fromPlayer1, toPlayer1);
            closeEverything(secondPlayer, fromPlayer2, toPlayer2);
        }
    }

    private void sendMove(DataOutputStream out, int command) throws IOException {
        out.writeInt(command);
    }


    public int getRandomCard() {
        Collections.shuffle(deck.cardList);
        return deck.cardList.get(1).getValue();
    }

    public void checkIfWin(int player, int score) throws IOException {
        //check if player won by playing his move
        if (score == 21) {
            System.out.println("if (secondPlayerScore == 21)");
            if (player == PLAYER2) {
                toPlayer1.writeInt(PLAYER2_WON);
                toPlayer2.writeInt(PLAYER2_WON);
            } else if (player == PLAYER1) {
                toPlayer1.writeInt(PLAYER1_WON);
                toPlayer2.writeInt(PLAYER1_WON);
            }
            return;
        }

        //check if player lose by playing his move
        else if (score > 21) {
            System.out.println("else if (secondPlayerScore > 21)");
            if (player == PLAYER2) {
                toPlayer1.writeInt(PLAYER1_WON);
                toPlayer2.writeInt(PLAYER1_WON);
            } else if (player == PLAYER1) {
                toPlayer1.writeInt(PLAYER2_WON);
                toPlayer2.writeInt(PLAYER2_WON);
            }
            return;
        }

        System.out.println("second else");
        toPlayer1.writeInt(CONTINUE);
    }

    @Override
    public void run() {
        try {
            //notify player1 that player2 has joined
            toPlayer1.writeInt(1);

            while (firstPlayer.isConnected() && secondPlayer.isConnected()) {
                try {
                    System.out.println(firstPlayerPassed);

                    int command = fromPlayer1.readInt();
                    if (command == TAKE) { // T
                        System.out.println("Sending a new card to player 1 ");
                        int randomCard = getRandomCard();
                        toPlayer1.writeInt(randomCard);
                        firstPlayerScore += randomCard;
                        System.out.println("CARD: " + randomCard);
                        toPlayer1.flush();

                        //check if first player won by playing his move
                        if (firstPlayerScore == 21) {
                            System.out.println("if (firstPlayerScore == 21)");
                            toPlayer1.writeInt(PLAYER1_WON);
                            toPlayer2.writeInt(PLAYER1_WON);
                            sendMove(toPlayer2, command);
                            break;
                        }

                        //check if first player lose by playing his move
                        else if (firstPlayerScore > 21) {
                            System.out.println("else if (firstPlayerScore > 21)");
                            toPlayer1.writeInt(PLAYER2_WON);
                            toPlayer2.writeInt(PLAYER2_WON);
                            sendMove(toPlayer2, command);
                            break;
                        } else {
                            System.out.println("first else");
                            toPlayer2.writeInt(CONTINUE);
                            sendMove(toPlayer2, command);
                            // send opponents score
                        }

                    } else if (command == PLAYER1_PASS) {
                        System.out.println("Sending score to player 1");
                        toPlayer1.writeInt(PLAYER1_PASS);
                        toPlayer2.writeInt(PLAYER1_PASS);
                        toPlayer1.writeInt(firstPlayerScore);
                        toPlayer1.flush();
                        sendMove(toPlayer2, command);
                        firstPlayerPassed = true;
                    } else if (command == WAIT) {
                        System.out.println("Player 1 waiting");
                        toPlayer2.writeInt(CONTINUE);
                        sendMove(toPlayer2, command);
                    }

                    System.out.println("W przedprogu");

                    command = fromPlayer2.readInt();
                    System.out.println("Int read");
                    if (command == TAKE) { // T
                        System.out.println("Sending a new card to player 2");
                        int randomCard = getRandomCard();
                        toPlayer2.writeInt(randomCard);
                        secondPlayerScore += randomCard;
                        System.out.println("CARD: " + randomCard);
                        toPlayer2.flush();

                        checkIfWin(PLAYER2, secondPlayerScore);
                        sendMove(toPlayer1, command);

                    } else if (command == PLAYER2_PASS) {
                        System.out.println("Sending score to player 2");
                        toPlayer1.writeInt(PLAYER2_PASS);
                        toPlayer2.writeInt(PLAYER2_PASS);
                        toPlayer2.writeInt(secondPlayerScore);
                        toPlayer2.flush();
                        sendMove(toPlayer1, command);
                        secondPlayerPassed = true;
                    }

                    else if (command == WAIT) {
                        System.out.println("Player 2 waiting");
                        toPlayer1.writeInt(CONTINUE);
                        sendMove(toPlayer1, command);
                    }

                    if (firstPlayerPassed && secondPlayerPassed && (firstPlayerScore < 21) && (secondPlayerScore < 21)) {

                        if (firstPlayerScore < secondPlayerScore) {
                            toPlayer1.writeInt(PLAYER2_WON);
                            toPlayer2.writeInt(PLAYER2_WON);
                        } else if (firstPlayerScore > secondPlayerScore) {
                            toPlayer1.writeInt(PLAYER1_WON);
                            toPlayer2.writeInt(PLAYER1_WON);
                        } else {
                            toPlayer1.writeInt(DRAW);
                            toPlayer2.writeInt(DRAW);
                        }
                    }
                    System.out.println("--------------------");

                } catch (IOException e) {

                    closeEverything(firstPlayer, fromPlayer1, toPlayer1);
                    closeEverything(secondPlayer, fromPlayer2, toPlayer2);
                    break;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
