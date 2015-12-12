package ai;

import game.Constants;
import game.Game;
import objects.Paddle;

public class DefaultAi extends ai.PaddleAi
{
    /* --- Construction and final properties --- */
    
    private static final double timeBetweenUpdates = 0.2;
    
    public DefaultAi(Paddle paddle, Game game)
    {
        super(paddle, game);
    }

    /* --- Update --- */
    
    private double timeSinceLastUpdate = 0;
    
    @Override
    public void update(double deltaTime)
    {
        timeSinceLastUpdate += deltaTime;
        
        if (timeSinceLastUpdate < timeBetweenUpdates) {
            return; /* Wait a while longer. */
        }
        
        timeSinceLastUpdate -= timeBetweenUpdates;
        
        double distanceFromPaddle = getPaddle().getX() - getGame().getBall().getX();
        
        /*
         * Do nothing if the ball is not moving towards us.
         */
        if (Math.signum(distanceFromPaddle) != Math.signum(getGame().getBall().getSpeed())) {
            getPaddle().setMovement(Paddle.Movement.NONE);
            return;
        }

        /*
         * Find out where the ball is heading for and move in that direction (this does not look
         * ahead past collisions).
         */
        double targetY = getGame().getBall().getY() + distanceFromPaddle * Math.tan(getGame().getBall().getAngle());
        boolean paddleOnTarget = targetY >= getPaddle().getY() && targetY + Constants.BALL_SIZE <= getPaddle().getY() + Constants.PADDLE_HEIGHT;
        if (paddleOnTarget) {
            getPaddle().setMovement(Paddle.Movement.NONE);
        } else if (targetY < getPaddle().getY()) {
            getPaddle().setMovement(Paddle.Movement.UP);
        } else if (targetY > getPaddle().getY()) {
            getPaddle().setMovement(Paddle.Movement.DOWN);
        }
    }
}
