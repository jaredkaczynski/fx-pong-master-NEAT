package game;

import java.util.Random;
import javafx.animation.AnimationTimer;
import javafx.scene.media.AudioClip;
import ai.DefaultAi;
import ai.PaddleAi;
import objects.Ball;
import objects.Paddle;

public class Game
{
    /* --- Construction and final properties --- */
    
    private static final Random random = new Random();
    
    private final int winningScore;

    double distanceFromBall = 0;
    
    public Game(int maxScore)
    {
        this.winningScore = maxScore;
        loop.start();
    }

    public int getWinningScore()
    {
        return winningScore;
    }
    
    /* --- Game loop --- */
    
    /*
     * This is an implementation of a game loop using variable time steps. See the blog posts on
     * game loops in JavaFX for more information.
     */
    private class GameLoop extends AnimationTimer
    {
        private long previousTime = 0;
        
        @Override
        public void handle(long currentTime)
        {
            /*
             * If this is the first frame, simply record an initial time.
             */
            if (previousTime == 0) {
                previousTime = currentTime;
                return;
            }

            double secondsElapsed = (currentTime - previousTime) / 1_000_000_000.0; /* Convert nanoseconds to seconds. */

            /*
             * Avoid large time steps by imposing an upper bound.
             */
            if (secondsElapsed > 0.0333) {
                secondsElapsed = 0.0333;
            }
            
            updateGame(secondsElapsed);
            
            previousTime = currentTime;
        }
    }

    /*
     * This is an implementation of a game loop using variable time steps. See the blog posts on
     * game loops in JavaFX for more information.
     */
        public double[] step(double[] nnetOutput)
        {
            /*
             * If this is the first frame, simply record an initial time.
             */

            updateGame(.1);
            double[] nnetInput = {ball.getX(),ball.getY(),player.getY(),ball.getSpeed()};
            return(nnetInput);
        }

    public double getfitnessDistance(){
        return distanceFromBall;
    }
    private final GameLoop loop = new GameLoop();
    
    /* --- State --- */
    
    public enum State
    {
        PLAYING, PAUSED, ENDED;
    }
    
    private State state = State.ENDED;
    
    public State getState()
    {
        return state;
    }

    private Runnable onGameEnd = () -> {}; /* Do nothing for now. */
    
    public void setOnGameEnd(Runnable onGameEnd)
    {
        this.onGameEnd = onGameEnd;
    }
    
    public void start()
    {
        player.setX(Constants.MARGIN_LEFT_RIGHT + Constants.GOAL_WIDTH - Constants.PADDLE_WIDTH); /* Aligned with the goal area. */
        player.setY((Constants.HEIGHT - Constants.PADDLE_HEIGHT) / 2); /* Centered. */
        
        opponent.setX(Constants.WIDTH - Constants.MARGIN_LEFT_RIGHT - Constants.GOAL_WIDTH); /* Aligned with the goal area. */
        opponent.setY((Constants.HEIGHT - Constants.PADDLE_HEIGHT) / 2); /* Centered. */
        
        player.setScore(0);
        opponent.setScore(0);
        
        player.setMovement(Paddle.Movement.NONE);
        opponent.setMovement(Paddle.Movement.NONE);
        
        launchBall();
        
        state = State.PLAYING;
    }

    public void pause()
    {
        if (state == State.PAUSED) {
            state = State.PLAYING;
        } else if (state == State.PLAYING) {
            state = State.PAUSED;
        }
    }

    public void forfeit()
    {
        player.setScore(0);
        opponent.setScore(winningScore);
        state = State.ENDED;
        onGameEnd.run();
    }
    
    /* --- Ball --- */
    
    private final Ball ball = new Ball(Constants.BALL_MAX_SPEED);

    public Ball getBall()
    {
        return ball;
    }
    
    public void launchBall()
    {
        boolean towardsOpponent = random.nextBoolean();
        double initialAngle = Constants.PADDLE_SECTION_ANGLES[random.nextInt(2) + 1]; /* We don't use the steepest angle. */
        
        ball.setSpeed(towardsOpponent ? -Constants.BALL_INITIAL_SPEED : Constants.BALL_INITIAL_SPEED);
        ball.setAngle(towardsOpponent ? -initialAngle : initialAngle);
        ball.setX((Constants.WIDTH - Constants.BALL_SIZE) / 2); /* Centered. */
        ball.setY(Constants.MARGIN_TOP_BOTTOM);
    }
    
    /* --- Player --- */
    
    private final Paddle player = new Paddle(Constants.PLAYER_PADDLE_SPEED);
    
    public Paddle getPlayer()
    {
        return player;
    }
    
    /* --- Opponent --- */
    
    private final Paddle opponent = new Paddle(Constants.OPPONENT_PADDLE_SPEED);
    private final PaddleAi ai = new DefaultAi(opponent, this);
    
    public Paddle getOpponent()
    {
        return opponent;
    }
    
    /* --- Update --- */
    
    private void updateGame(double deltaTime)
    {
        if (state == State.PAUSED || state == State.ENDED) {
            return; /* This is necessary because the loop keeps running even when the game is paused or stopped. */
        }
        
        player.update(deltaTime);
        opponent.update(deltaTime);
        
        keepPaddleInBounds(player);
        keepPaddleInBounds(opponent);
        
        ball.update(deltaTime);
        
        checkWallCollision();
        checkPaddleOrEdgeCollision(player);
        checkPaddleOrEdgeCollision(opponent);
        
        ai.update(deltaTime);
    }
    
    /* --- Collision detection --- */
    
    private void keepPaddleInBounds(Paddle paddle)
    {
        if (paddle.getY() < Constants.MARGIN_TOP_BOTTOM) {
            paddle.setY(Constants.MARGIN_TOP_BOTTOM);
        } else if (paddle.getY() + Constants.PADDLE_HEIGHT > Constants.HEIGHT - Constants.MARGIN_TOP_BOTTOM) {
            paddle.setY(Constants.HEIGHT - Constants.MARGIN_TOP_BOTTOM - Constants.PADDLE_HEIGHT);
        }
    }
    
    private void checkWallCollision()
    {
        boolean ballHitTopWall = ball.getY() < Constants.MARGIN_TOP_BOTTOM;
        boolean ballHitBottomWall = ball.getY() + Constants.BALL_SIZE > Constants.HEIGHT - Constants.MARGIN_TOP_BOTTOM;
        
        if (ballHitTopWall || ballHitBottomWall) {
            ball.setAngle(ball.getAngle() * -1);
            new AudioClip(Sounds.HIT_WALL).play();
        }
        
        if (ballHitTopWall) {
            ball.setY(Constants.MARGIN_TOP_BOTTOM);
        } else if (ballHitBottomWall) {
            ball.setY(Constants.HEIGHT - Constants.MARGIN_TOP_BOTTOM - Constants.BALL_SIZE);
        }
    }
    
    private void checkPaddleOrEdgeCollision(Paddle paddle)
    {
        boolean ballHitEdge;
        if (paddle == player) {
            ballHitEdge = ball.getX() < Constants.MARGIN_LEFT_RIGHT + Constants.GOAL_WIDTH;
        } else {
            ballHitEdge = ball.getX() + Constants.BALL_SIZE > Constants.WIDTH - Constants.MARGIN_LEFT_RIGHT - Constants.GOAL_WIDTH;
        }
        if (!ballHitEdge) {
            return;
        }
        
        boolean ballHitPaddle = ball.getY() + Constants.BALL_SIZE > paddle.getY() && ball.getY() < paddle.getY() + Constants.PADDLE_HEIGHT;
        if (ballHitPaddle) {
            
            /*
             * Find out what section of the paddle was hit.
             */
            for (int i = 0; i < Constants.PADDLE_SECTIONS; i++) {
                boolean ballHitCurrentSection = ball.getY() < paddle.getY() + (i + 0.5) * Constants.PADDLE_SECTION_HEIGHT;
                if (ballHitCurrentSection) {
                    ball.setAngle(Constants.PADDLE_SECTION_ANGLES[i] * (paddle == opponent ? -1 : 1));
                    break; /* Found our match. */
                } else if (i == Constants.PADDLE_SECTIONS - 1) { /* If we haven't found our match by now, it must be the last section. */
                    ball.setAngle(Constants.PADDLE_SECTION_ANGLES[i] * (paddle == opponent ? -1 : 1));
                }
            }
            
            /*
             * Update and reposition the ball.
             */
            ball.setSpeed(ball.getSpeed() * Constants.BALL_SPEED_INCREASE);
            if (paddle == player) {
                ball.setX(Constants.MARGIN_LEFT_RIGHT + Constants.GOAL_WIDTH);
            } else {
                ball.setX(Constants.WIDTH - Constants.MARGIN_LEFT_RIGHT - Constants.GOAL_WIDTH - Constants.BALL_SIZE);
            }
            new AudioClip(Sounds.HIT_PADDLE).play();
            
        } else {
            
            /*
             * Update the score.
             */
            if (paddle == opponent) {
                player.setScore(player.getScore() + 1);
                new AudioClip(Sounds.SCORE_PLAYER).play();
            } else {
                opponent.setScore(opponent.getScore() + 1);
                //Add miss point and track distance of miss
                distanceFromBall = ball.getY()+paddle.getY();
                new AudioClip(Sounds.SCORE_OPPONENT).play();
            }
            
            /*
             * Check if the game has ended. If not, play another round.
             */
            if (player.getScore() == winningScore || opponent.getScore() == winningScore) {
                state = State.ENDED;
                onGameEnd.run();
            } else {
                launchBall();
            }
        }
    }
}
