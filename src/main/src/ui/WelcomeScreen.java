package ui;

import javafx.application.Platform;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import game.Constants;

public class WelcomeScreen extends Pane
{
    private Runnable onStart = () -> {}; /* Do nothing for now. */
    
    public void setOnStart(Runnable onStart)
    {
        this.onStart = onStart;
    }
    
    public WelcomeScreen()
    {
        Text header = new Text("pong");
        header.boundsInLocalProperty().addListener(observable ->
        {
            /*
             * When using CSS, the width and height (with CSS applied) aren't available right away.
             * Therefore, we listen for changes and update the position once the width and height
             * are available.
             */
            header.setTranslateX((Constants.WIDTH - header.getBoundsInLocal().getWidth()) / 2); /* Centered. */
            header.setTranslateY(Constants.TEXT_MARGIN_TOP_BOTTOM);
        });
        header.getStyleClass().add("header");
        
        Text info = new Text("use the arrow keys to move\npress p to pause\n\npress enter to start\npress escape to quit");
        info.boundsInLocalProperty().addListener(observable ->
        {
            /*
             * When using CSS, the width and height (with CSS applied) aren't available right away.
             * Therefore, we listen for changes and update the position once the width and height
             * are available.
             */
            info.setTranslateX((Constants.WIDTH - info.getBoundsInLocal().getWidth()) / 2); /* Centered. */
            info.setTranslateY(Constants.HEIGHT - Constants.TEXT_MARGIN_TOP_BOTTOM - info.getBoundsInLocal().getHeight());
        });
        info.getStyleClass().add("info");
        
        setPrefSize(Constants.WIDTH, Constants.HEIGHT);
        getChildren().addAll(header, info);
        getStyleClass().add("screen");
        
        setOnKeyPressed(event ->
        {
            if (event.getCode() == KeyCode.ENTER) {
                onStart.run();
            } else if (event.getCode() == KeyCode.ESCAPE) {
                Platform.exit();
            }
        });
    }
}
