import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.*;

public class BlackJack extends Application {

    private class Card {
        String value;
        String type;

        Card(String value, String type) {
            this.value = value;
            this.type = type;
        }

        public String toString() {
            return value + "-" + type;
        }

        public int getValue() {
            if ("JQK".contains(value)) return 10;
            else if (value.equals("A")) return 11;
            return Integer.parseInt(value);
        }

        public boolean isAce() {
            return value.equals("A");
        }

        public String getImagePath() {
            return "file:cards/" + toString() + ".png";
        }
    }

    private ArrayList<Card> deck;
    private Random random = new Random();

    private Card hiddenCard;
    private ArrayList<Card> dealerHand;
    private ArrayList<Card> playerHand;
    private int dealerSum;
    private int dealerAceCount;
    private int playerSum;
    private int playerAceCount;

    private HBox dealerCards = new HBox(5);
    private HBox playerCards = new HBox(5);
    private Text resultText = new Text();
    private Text playerTotalText = new Text();
    private Text dealerTotalText = new Text();

    private Button hitButton = new Button("Hit");
    private Button stayButton = new Button("Stay");
    private Button playAgainButton = new Button("Play Again");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        startGame();

        VBox root = new VBox(20);
        root.setStyle("-fx-background-color:#1E3A8A; -fx-padding: 20;");

        Font uiFont = Font.font("Arial", FontWeight.BOLD, 20);
        resultText.setFont(uiFont);
        resultText.setFill(Color.WHITE);
        playerTotalText.setFont(uiFont);
        playerTotalText.setFill(Color.WHITE);
        dealerTotalText.setFont(uiFont);
        dealerTotalText.setFill(Color.WHITE);

        hitButton.setOnAction(e -> {
            Card card = deck.remove(deck.size() - 1);
            playerHand.add(card);
            playerSum += card.getValue();
            if (card.isAce()) playerAceCount++;

            playerCards.getChildren().add(new ImageView(new Image(card.getImagePath(), 110, 154, false, true)));
            playerTotalText.setText("Player Total: " + reducePlayerAce());

            if (playerSum > 21 && playerAceCount == 0) {
                hitButton.setDisable(true);
                stayButton.setDisable(true);

                dealerCards.getChildren().set(0, new ImageView(new Image(hiddenCard.getImagePath(), 110, 154, false, true)));
                dealerHand.add(0, hiddenCard);
                dealerSum += hiddenCard.getValue();
                if (hiddenCard.isAce()) dealerAceCount++;

                dealerTotalText.setText("Dealer Total: " + reduceDealerAce());

                displayResult();
            }
        });

        stayButton.setOnAction(e -> {
            hitButton.setDisable(true);
            stayButton.setDisable(true);

            dealerCards.getChildren().set(0, new ImageView(new Image(hiddenCard.getImagePath(), 110, 154, false, true)));
            dealerHand.add(0, hiddenCard);
            dealerSum += hiddenCard.getValue();
            if (hiddenCard.isAce()) dealerAceCount++;

            while (reduceDealerAce() < 17) {
                Card card = deck.remove(deck.size() - 1);
                dealerHand.add(card);
                dealerSum += card.getValue();
                if (card.isAce()) dealerAceCount++;
                dealerCards.getChildren().add(new ImageView(new Image(card.getImagePath(), 110, 154, false, true)));
            }

            dealerTotalText.setText("Dealer Total: " + reduceDealerAce());
            playerTotalText.setText("Player Total: " + reducePlayerAce());

            displayResult();
        });

        playAgainButton.setOnAction(e -> startGame());

        root.getChildren().addAll(
            dealerCards,
            dealerTotalText,
            playerCards,
            playerTotalText,
            resultText,
            new HBox(10, hitButton, stayButton, playAgainButton)
        );

        Scene scene = new Scene(root, 600, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("BlackJack - JavaFX");
        primaryStage.show();
    }

    private void displayResult() {
        int adjustedPlayer = reducePlayerAce();
        int adjustedDealer = reduceDealerAce();

        String message;
        if (adjustedPlayer > 21) {
            message = "You Lose!";
        } else if (adjustedDealer > 21) {
            message = "You Win!";
        } else if (adjustedPlayer > adjustedDealer) {
            message = "You Win!";
        } else if (adjustedPlayer < adjustedDealer) {
            message = "You Lose!";
        } else {
            message = "Tie!";
        }

        resultText.setText(message);
    }

    private void startGame() {
        buildDeck();
        shuffleDeck();

        dealerHand = new ArrayList<>();
        playerHand = new ArrayList<>();
        dealerSum = playerSum = 0;
        dealerAceCount = playerAceCount = 0;

        hiddenCard = deck.remove(deck.size() - 1);
        Card dealerCard = deck.remove(deck.size() - 1);
        dealerHand.add(dealerCard);
        dealerSum += dealerCard.getValue();
        if (dealerCard.isAce()) dealerAceCount++;

        dealerCards.getChildren().clear();
        dealerCards.getChildren().add(new ImageView(new Image("file:cards/BACK.png", 110, 154, false, true)));
        dealerCards.getChildren().add(new ImageView(new Image(dealerCard.getImagePath(), 110, 154, false, true)));

        playerCards.getChildren().clear();
        for (int i = 0; i < 2; i++) {
            Card card = deck.remove(deck.size() - 1);
            playerHand.add(card);
            playerSum += card.getValue();
            if (card.isAce()) playerAceCount++;
            playerCards.getChildren().add(new ImageView(new Image(card.getImagePath(), 110, 154, false, true)));
        }

        resultText.setText("");
        hitButton.setDisable(false);
        stayButton.setDisable(false);

        playerTotalText.setText("Player Total: " + reducePlayerAce());
        dealerTotalText.setText("Dealer Total: ?");

        // this will check for an instant blackjack
        if (reducePlayerAce() == 21 && playerHand.size() == 2) {
            resultText.setText("Blackjack! You Win!");

            dealerCards.getChildren().set(0, new ImageView(new Image(hiddenCard.getImagePath(), 110, 154, false, true)));
            dealerHand.add(0, hiddenCard);
            dealerSum += hiddenCard.getValue();
            if (hiddenCard.isAce()) dealerAceCount++;

            dealerTotalText.setText("Dealer Total: " + reduceDealerAce());

            hitButton.setDisable(true);
            stayButton.setDisable(true);
        }
    }

    private void buildDeck() {
        deck = new ArrayList<>();
        String[] values = {"A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K"};
        String[] types = {"C", "D", "H", "S"};

        for (String type : types) {
            for (String value : values) {
                deck.add(new Card(value, type));
            }
        }
    }

    private void shuffleDeck() {
        for (int i = 0; i < deck.size(); i++) {
            int j = random.nextInt(deck.size());
            Card temp = deck.get(i);
            deck.set(i, deck.get(j));
            deck.set(j, temp);
        }
    }

    private int reducePlayerAce() {
        int total = playerSum;
        int aces = playerAceCount;
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }

    private int reduceDealerAce() {
        int total = dealerSum;
        int aces = dealerAceCount;
        while (total > 21 && aces > 0) {
            total -= 10;
            aces--;
        }
        return total;
    }
}