package org.example;

import java.io.Serializable;

public class Card implements Serializable {
    private final Cardtype cardtype;
    private int value;

    public Card(Cardtype cardtype, int value) {
        this.cardtype = cardtype;
        this.value = value;
    }

    public Cardtype getCardtype() {
        return cardtype;
    }

    public int getValue() {
        return value;
    }

    public int setValue(){
        switch (this.getCardtype()){
            case ACE: this.value = 11;
                break;
            case TWO:
            case JACK: this.value = 2;
                break;
            case QUEEN:
            case THREE: this.value = 3;
                break;
            case KING:
            case FOUR: this.value = 4;
                break;
            case FIVE: this.value = 5;
                break;
            case SIX: this.value = 6;
                break;
            case SEVEN: this.value = 7;
                break;
            case EIGHT: this.value = 8;
                break;
            case NINE: this.value = 9;
                break;
            case TEN: this.value = 10;
                break;
            default: return 0;
        }
        return 0;
    }

    @Override
    public String toString(){
        return this.getCardtype().toString();
    }

//    public int setValue(){
//        switch (this.getCardname()){
//            case ACE: return 11;
//            case TWO:
//            case JACK: return 2;
//            case QUEEN:
//            case THREE: return 3;
//            case KING:
//            case FOUR: return 4;
//            case FIVE: return 5;
//            case SIX: return 6;
//            case SEVEN: return 7;
//            case EIGHT: return 8;
//            case NINE: return 9;
//            case TEN: return 10;
//            default: return 0;
//        }
//    }
}
