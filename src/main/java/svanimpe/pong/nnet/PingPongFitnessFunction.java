package svanimpe.pong.nnet;

/**
 * Created by razrs on 12-Dec-15.
 */

import com.anji.imaging.IdentifyImageFitnessFunction;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;
import com.anji.util.Configurable;
import com.anji.util.Properties;
import com.anji.util.Randomizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

package nnet;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

import PingPong.*;
import com.anji.util.Configurable;
import com.anji.util.Properties;
import com.anji.util.Randomizer;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.jgap.BulkFitnessFunction;
import org.jgap.Chromosome;

import com.anji.imaging.IdentifyImageFitnessFunction;
import com.anji.integration.Activator;
import com.anji.integration.ActivatorTranscriber;


/**
 * Created by Jared on 06-Dec-15.
 */
public class PingPongFitnessFunction implements BulkFitnessFunction,Configurable {


    private final static double DEFAULT_TRACK_LENGTH = 4.8;

    private double trackLength = DEFAULT_TRACK_LENGTH;

    private double trackLengthHalfed;

    private final static int DEFAULT_TIMESTEPS = 10000;

    private int maxTimesteps = DEFAULT_TIMESTEPS;

    private final static int DEFAULT_NUM_TRIALS = 1;

    private int numTrials = DEFAULT_NUM_TRIALS;




    private final static Logger logger = Logger.getLogger( PingPongFitnessFunction.class );

    private ActivatorTranscriber factory;

    private Random rand;

    private void setTrackLength( double aTrackLength ) {
        trackLength = aTrackLength;
        trackLengthHalfed = trackLength / 2;
    }

    /**
     * @see com.anji.util.Configurable#init(com.anji.util.Properties)
     */

    public void init( Properties props ) throws Exception {
        try {
            BasicConfigurator.configure();
            factory = (ActivatorTranscriber) props.singletonObjectProperty( ActivatorTranscriber.class );
            Randomizer randomizer = (Randomizer) props.singletonObjectProperty( Randomizer.class );
            rand = randomizer.getRand();
        }
        catch ( Exception e ) {
            throw new IllegalArgumentException( "invalid properties: " + e.getClass().toString() + ": "
                    + e.getMessage() );
        }
    }

    /**
     * @see org.jgap.BulkFitnessFunction#evaluate(java.util.List)
     * @see IdentifyImageFitnessFunction#evaluate(Chromosome)
     */
    public void evaluate( List genotypes ) {

        // evaluate each chromosome
        Iterator it = genotypes.iterator();
        while ( it.hasNext() ) {
            Chromosome c = (Chromosome) it.next();
            evaluate( c );
        }
    }

    /**
     * Evaluate chromosome and set getfitness.
     * @param c
     */
    public void evaluate( Chromosome c ) {
        try {
            Activator activator = factory.newActivator( c );

            // calculate getfitness, sum of multiple trials
            int fitness = 0;
            for ( int i = 0; i < numTrials; i++ ) {
                fitness += singleTrial(activator);
            }
            c.setFitnessValue(fitness );
            //System.out.println(fitness + " getfitness evaluate " + c.getFitnessValue());
        }
        catch ( Throwable e ) {
            logger.warn( "error evaluating chromosome " + c.toString(), e );
            c.setFitnessValue( 0 );
        }
    }

    private double singleTrial( Activator activator ) {
        double energyUsed = 0;
        double f2 = 0.0;
        double fitness = 0;
        //System.out.println("trial");
        // Run the pole-balancing simulation.
        int currentTimestep = 0;
        Board trial = new Board();
        double[] neuralnetworkdataFROMPong = {198,300,175};
        double[] networkOutput = activator.next(neuralnetworkdataFROMPong);
        //System.out.println(neuralnetworkdataFROMPong);
        while(true) {
            // Network activation values

            // Activate the network.
            //neuralnetworkdataFROMPong = trial.screen.step(neuralnetworkdataFROMPong);
            //System.out.println(activator.getInputDimension()+" This is the dimension");
            if(trial.getRealTime()) {
                //System.out.println(neuralnetworkdataFROMPong[0] + " network input" + neuralnetworkdataFROMPong[1] + " ");
            }
            networkOutput = activator.next(neuralnetworkdataFROMPong);
            //System.out.println(networkOutput.length);
            if(trial.getRealTime()) {
                //System.out.println(networkOutput[0] + " network output" + networkOutput[1]);
            }
            /*if(networkOutput>0) {
                System.out.println(networkOutput + " what the network will do");
            }*/
            neuralnetworkdataFROMPong = trial.screen.step(networkOutput);
            /*
            performAction(networkOutput, state);
            if (display != null) {
                // display.setStatus( Arrays.toString( state ) );
                display.step();
            }
            */
            //SimulateTimestep(network.getOutputSignal(0)>0.5);
            if(trial.getfinished()){
                break;
            }
        }
        fitness = trial.getfitnessDistance();
        if(fitness>2250)
            System.out.println(fitness + " getfitness" +  trial.getfitnessDistance());
        trial.delete();
        return fitness;
    }

    /*private double singleTrial2( Activator activator ) {
        double energyUsed = 0;
        double f2 = 0.0;
        double fitness = 0;

        //variables

        //size of the board
        final int WIDTH = 600;
        final int HEIGHT = 400;

        //starting location of the players
        //player 1
        final int P1_X = 30;
        final int P1_Y = 175;
        //player 2
        final int P2_X = 550;
        final int P2_Y = 175;

        //speed of the players
        final int P1_SPEED = 1;
        final int P2_SPEED = 1;

        //speed of the ball
        final int B_SPEED = 2;


        //create players
        Player p1 = new Player(P1_X, P1_Y, P1_SPEED);
        Player p2 = new Player(P2_X, P2_Y, P2_SPEED);

        //create ball
        Ball b = new Ball(B_SPEED);

        //create the scoreboard
        ScoreBoard sb = new ScoreBoard();

        //System.out.println("trial");
        // Run the pole-balancing simulation.
        int currentTimestep = 0;
        //Board trial = new Board();
        PingPongNoGui trial = new PingPongNoGui(p1, p2, b, sb);
        double[] neuralnetworkdataFROMPong = {198,300,175};
        double[] networkOutput = activator.next(neuralnetworkdataFROMPong);
        //System.out.println(neuralnetworkdataFROMPong);
        while(true) {
            // Network activation values

            // Activate the network.
            //neuralnetworkdataFROMPong = trial.screen.step(neuralnetworkdataFROMPong);
            //System.out.println(activator.getInputDimension()+" This is the dimension");
            if(trial.getRealTime()) {
                //System.out.println(neuralnetworkdataFROMPong[0] + " network input" + neuralnetworkdataFROMPong[1] + " ");
            }
            networkOutput = activator.next(neuralnetworkdataFROMPong);
            //System.out.println(networkOutput.length);
            if(trial.getRealTime()) {
                //System.out.println(networkOutput[0] + " network output" + networkOutput[1]);
            }
            /*if(networkOutput>0) {
                System.out.println(networkOutput + " what the network will do");
            }
            neuralnetworkdataFROMPong = trial.step(networkOutput);

            performAction(networkOutput, state);
            if (display != null) {
                // display.setStatus( Arrays.toString( state ) );
                display.step();
            }

            //SimulateTimestep(network.getOutputSignal(0)>0.5);
            if(trial.getfinished()){
                break;
            }
        }
        fitness = trial.getfitnessDistance();
        //if(fitness>0)
        //System.out.println(fitness + " getfitness");
        //trial.delete();
        return fitness;
    }*/

    /**
     * @see org.jgap.BulkFitnessFunction#getMaxFitnessValue()
     */
    public int getMaxFitnessValue() {
        return ( 1 );
    }

    /**
     * enable GUI display of pole balancing
     */
    /*
    public void enableDisplay() {
        display = new PoleBalanceDisplay( trackLength, new double[] { poleLength1, poleLength2 },
                maxTimesteps );
        display.setVisible( true );
    }*/

}

