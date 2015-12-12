package ui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import game.Constants;
import game.Game;
import objects.Paddle;

public class GameScreen extends Pane
{
    private final Game game;
    
    private final Rectangle ball = new Rectangle(Constants.BALL_SIZE, Constants.BALL_SIZE);
    private final Rectangle player = new Rectangle(Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT);
    private final Rectangle opponent = new Rectangle(Constants.PADDLE_WIDTH, Constants.PADDLE_HEIGHT);
    
    private final Text playerScore = new Text("0");
    private final Text opponentScore = new Text("0");
    
    public GameScreen(Game game)
    {
        this.game = game;
        
        ball.translateXProperty().bind(game.getBall().xProperty());
        ball.translateYProperty().bind(game.getBall().yProperty());
        ball.getStyleClass().add("ball");
        
        player.translateXProperty().bind(game.getPlayer().xProperty());
        player.translateYProperty().bind(game.getPlayer().yProperty());
        player.getStyleClass().add("paddle");
        
        opponent.translateXProperty().bind(game.getOpponent().xProperty());
        opponent.translateYProperty().bind(game.getOpponent().yProperty());
        opponent.getStyleClass().add("paddle");
        
        playerScore.textProperty().bind(game.getPlayer().scoreProperty().asString());
        playerScore.boundsInLocalProperty().addListener(observable ->
        {
            /*
             * When using CSS, the width and height (with CSS applied) aren't available right away.
             * Therefore, we listen for changes and update the position once the width and height
             * are available.
             */
            playerScore.setTranslateX(Constants.WIDTH / 2 - Constants.SCORE_SPACING / 2 - playerScore.getBoundsInLocal().getWidth());
        });
        playerScore.setTranslateY(Constants.TEXT_MARGIN_TOP_BOTTOM);
        playerScore.getStyleClass().add("score");
        
        opponentScore.textProperty().bind(game.getOpponent().scoreProperty().asString());
        opponentScore.setTranslateX(Constants.WIDTH / 2 + Constants.SCORE_SPACING / 2);
        opponentScore.setTranslateY(Constants.TEXT_MARGIN_TOP_BOTTOM);
        opponentScore.getStyleClass().add("score");
        
        setPrefSize(Constants.WIDTH, Constants.HEIGHT);
        getChildren().addAll(ball, player, opponent, playerScore, opponentScore);
        getStyleClass().add("screen");
        
        setOnKeyPressed(this::keyPressed);
        setOnKeyReleased(this::keyReleased);
    }
    
    private void keyPressed(KeyEvent event)
    {
        if (event.getCode() == KeyCode.P) {
            game.pause();
        } else if (event.getCode() == KeyCode.ESCAPE) {
            game.forfeit();
        } else if (game.getPlayer().getMovement() == Paddle.Movement.NONE && event.getCode() == KeyCode.UP) {
            game.getPlayer().setMovement(Paddle.Movement.UP);
        } else if (game.getPlayer().getMovement() == Paddle.Movement.NONE && event.getCode() == KeyCode.DOWN) {
            game.getPlayer().setMovement(Paddle.Movement.DOWN);
        }
    }

    private void keyReleased(KeyEvent event)
    {
        if (game.getPlayer().getMovement() == Paddle.Movement.UP && event.getCode() == KeyCode.UP) {
            game.getPlayer().setMovement(Paddle.Movement.NONE);
        } else if (game.getPlayer().getMovement() == Paddle.Movement.DOWN && event.getCode() == KeyCode.DOWN) {
            game.getPlayer().setMovement(Paddle.Movement.NONE);
        }
    }
}
