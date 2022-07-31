package org.example;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;
import java.util.Collections;

public class ClientHandler implements Runnable {

    private Socket firstPlayer;
    private Socket secondPlayer;
    private BufferedReader fromPlayer1;
    private OutputStreamWriter toPlayer1;
    private BufferedReader fromPlayer2;
    private OutputStreamWriter toPlayer2;
    private final Deck deck = new Deck();
    private int firstPlayerScore = 0;
    private int secondPlayerScore = 0;

    private JSONObject firstPlayerResponse = new JSONObject();
    private JSONObject secondPlayerResponse = new JSONObject();

    boolean firstPlayerPassed = false;
    boolean secondPlayerPassed = false;

    public ClientHandler(Socket socket1, Socket socket2) {
        try {
            this.firstPlayer = socket1;
            this.secondPlayer = socket2;
            this.fromPlayer1 = new BufferedReader(new InputStreamReader(firstPlayer.getInputStream()));
            this.toPlayer1 = new OutputStreamWriter(firstPlayer.getOutputStream());
            this.fromPlayer2 = new BufferedReader(new InputStreamReader(secondPlayer.getInputStream()));
            this.toPlayer2 = new OutputStreamWriter(secondPlayer.getOutputStream());

        } catch (IOException e) {
            closeEverything(firstPlayer, fromPlayer1, toPlayer1);
            closeEverything(secondPlayer, fromPlayer2, toPlayer2);
        }
    }

    public Card getRandomCard() {
        Collections.shuffle(deck.cardList);
        return deck.cardList.get(1);
    }

    public void checkIfWin(JSONObject data, int score, Command player) throws IOException {
        //sprawdzenie czy gracz wygrał
        if (score == 21) {
            if (player == Command.PLAYER2) {
                sendResponse(Command.PLAYER2_WON);

            } else if (player == Command.PLAYER1) {
                sendResponse(Command.PLAYER1_WON);
            }
            return;
        }

        //sprawdzenie czy gracz przegrał
        else if (score > 21) {
            if (player == Command.PLAYER2) {
                secondPlayerScore = 0;
                sendResponse(Command.PLAYER1_WON);
            } else if (player == Command.PLAYER1) {
                firstPlayerScore = 0;
                sendResponse(Command.PLAYER2_WON);
            }
            return;
        }

        if (player == Command.PLAYER1) {
            secondPlayerContinue();
        } else {
            firstPlayerContinue();
        }

    }

    public void sendResponse(Command result) throws IOException {
        firstPlayerResponse.put("result", result);
        firstPlayerResponse.put("firstPlayerScore", firstPlayerScore);
        firstPlayerResponse.put("secondPlayerScore", secondPlayerScore);
        toPlayer1.write(firstPlayerResponse.toString() + '\n');
        toPlayer1.flush();

        secondPlayerResponse.put("result", result);
        secondPlayerResponse.put("secondPlayerScore", secondPlayerScore);
        secondPlayerResponse.put("firstPlayerScore", firstPlayerScore);
        toPlayer2.write(secondPlayerResponse.toString() + '\n');
        toPlayer2.flush();
    }

    public void firstPlayerContinue() throws IOException {
        firstPlayerResponse.put("result", Command.CONTINUE);
        firstPlayerResponse.put("firstPlayerScore", firstPlayerScore);
        firstPlayerResponse.put("secondPlayerScore", secondPlayerScore);
        toPlayer1.write(firstPlayerResponse.toString() + '\n');
        toPlayer1.flush();
    }

    public void secondPlayerContinue() throws IOException {
        secondPlayerResponse.put("result", Command.CONTINUE);
        secondPlayerResponse.put("firstPlayerScore", firstPlayerScore);
        secondPlayerResponse.put("secondPlayerScore", secondPlayerScore);
        toPlayer2.write(secondPlayerResponse.toString() + '\n');
        toPlayer2.flush();
    }

    public void sendCard(Command player) throws IOException {
        JSONObject response = firstPlayerResponse;
        OutputStreamWriter toPlayer = toPlayer1;

        if (player == Command.PLAYER2){
            response = secondPlayerResponse;
            toPlayer = toPlayer2;
        }

        Card randomCard = getRandomCard();
        response.put("card", randomCard.getValue());
        toPlayer.write(response.toString() + '\n');
        toPlayer.flush();

        if (player == Command.PLAYER1){firstPlayerScore += randomCard.getValue();}
        if (player == Command.PLAYER2){secondPlayerScore += randomCard.getValue();}

        System.out.println("CARD: " + randomCard.getCardtype() + ", VALUE: " + randomCard.getValue());
    }
    @Override
    public void run() {
        try {
            // powiadomienie, że drugi gracz dołączył
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("secondPlayer", true);
            toPlayer1.write(jsonObject.toString() + '\n');
            toPlayer1.flush();

            while (firstPlayer.isConnected() && secondPlayer.isConnected()) {
                try {
                    String dataString = fromPlayer1.readLine();
                    JSONObject data = new JSONObject(dataString);

                    Command command = data.getEnum(Command.class, "command");
                    if (command == Command.TAKE) {

                        System.out.println("Sending a new card to player 1 ");
                        sendCard(Command.PLAYER1);
                        checkIfWin(firstPlayerResponse, firstPlayerScore, Command.PLAYER1);

                    } else if (command == Command.PLAYER1_PASS) {

                        System.out.println("Player 1 passed");
                        sendResponse(Command.PLAYER1_PASS);
                        firstPlayerPassed = true;

                    } else if (command == Command.WAIT) {
                        secondPlayerContinue();
                    }

                    dataString = fromPlayer2.readLine();
                    data = new JSONObject(dataString);
                    firstPlayerResponse = new JSONObject();
                    secondPlayerResponse = new JSONObject();

                    command = data.getEnum(Command.class, "command");
                    if (command == Command.TAKE) {

                        System.out.println("Sending a new card to player 2");
                        sendCard(Command.PLAYER2);
                        checkIfWin(secondPlayerResponse, secondPlayerScore, Command.PLAYER2);

                    } else if (command == Command.PLAYER2_PASS) {

                        System.out.println("Player 2 passed");
                        sendResponse(Command.PLAYER2_PASS);
                        secondPlayerPassed = true;

                    } else if (command == Command.WAIT) {
                        firstPlayerContinue();
                    }

                    if (firstPlayerPassed && secondPlayerPassed && (firstPlayerScore < 21) && (secondPlayerScore < 21)) {

                        if (firstPlayerScore < secondPlayerScore) {
                            sendResponse(Command.PLAYER2_WON);
                        } else if (firstPlayerScore > secondPlayerScore) {
                            sendResponse(Command.PLAYER1_WON);
                        } else {
                            sendResponse(Command.DRAW);
                        }
                    }

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
}
