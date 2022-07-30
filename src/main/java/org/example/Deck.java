package org.example;
import java.util.*;

public class Deck {
    List<Card> cardList;

    public Deck() {
        this.cardList = new ArrayList<>();

        cardList.add(new Card(Cardtype.ACE,11));
        cardList.add(new Card(Cardtype.TWO,2));
        cardList.add(new Card(Cardtype.THREE,3));
        cardList.add(new Card(Cardtype.FOUR,4));
        cardList.add(new Card(Cardtype.FIVE,5));
        cardList.add(new Card(Cardtype.SIX,6));
        cardList.add(new Card(Cardtype.SEVEN,7));
        cardList.add(new Card(Cardtype.EIGHT,8));
        cardList.add(new Card(Cardtype.NINE,9));
        cardList.add(new Card(Cardtype.TEN,10));
        cardList.add(new Card(Cardtype.JACK,2));
        cardList.add(new Card(Cardtype.QUEEN,3));
        cardList.add(new Card(Cardtype.KING,4));

        Collections.shuffle(cardList);
    }
}
