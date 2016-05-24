package LUDOSimulator;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import org.encog.engine.network.activation.ActivationSigmoid;
import org.encog.ml.data.MLDataSet;
import org.encog.ml.data.basic.BasicMLDataSet;
import org.encog.neural.networks.BasicNetwork;
import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;

/**
 * ANN Player
 * @author Jorge Rodriguez
 * @version 0.1
 */
public class ANNLUDOPlayer implements LUDOPlayer {

    LUDOBoard board_;
    Random rand_;
    BasicNetwork playerNetwork_;
    public static int chromosome_size = 17;
    double[][] playerIdealSolution = new double[chromosome_size][1];
    ArrayList<double[]> situations = new ArrayList<double[]>();

    private int ANN_INPUT_LAYER = 9;
    private int ANN_HIDDEN_LAYER = 3;

    boolean playerFound;

    //-----------------------------------------------------------------------
    //				Constructors
    //-----------------------------------------------------------------------
    public ANNLUDOPlayer(LUDOBoard board)
    {
	this.board_ = board;
	this.playerNetwork_ = new BasicNetwork();	
	this.playerIdealSolution = idealSolution();
	trainNetwork(this.playerNetwork_, this.playerIdealSolution);
	rand_ = new Random();
    }

    public ANNLUDOPlayer(LUDOBoard board, double[][] idealSolution)
    {
	this.board_ = board;	
	this.playerNetwork_ = new BasicNetwork(); 
	this.playerIdealSolution = idealSolution;
	trainNetwork(this.playerNetwork_, idealSolution);
	rand_ = new Random();
    }
    
    public ANNLUDOPlayer(LUDOBoard board, double a, double b, double c, double d, double e, double f, double g,
	    double h, double i, double j, double k, double l, double m, double n, double o, double p, double q)
    {
	this.board_ = board;	
	this.playerNetwork_ = new BasicNetwork(); 
	this.playerIdealSolution[0][0] = a; this.playerIdealSolution[1][0] = b; this.playerIdealSolution[2][0] = c; 
	this.playerIdealSolution[3][0] = d; this.playerIdealSolution[4][0] = e; this.playerIdealSolution[5][0] = f; 
	this.playerIdealSolution[6][0] = g; this.playerIdealSolution[7][0] = h; this.playerIdealSolution[8][0] = i; 
	this.playerIdealSolution[9][0] = j; this.playerIdealSolution[10][0] = k; this.playerIdealSolution[11][0] = l; 
	this.playerIdealSolution[12][0] = m; this.playerIdealSolution[13][0] = n; this.playerIdealSolution[14][0] = o; 
	this.playerIdealSolution[15][0] = p; this.playerIdealSolution[16][0] = q; 
	trainNetwork(this.playerNetwork_, this.playerIdealSolution);
	rand_ = new Random();
    }

    //-----------------------------------------------------------------------
    //				Methods
    //-----------------------------------------------------------------------

    /**
     * Supposing we have a player with its network trained,
     * play one turn.
     */
    public void play() {
	board_.print("ANN player playing");
	//Roll the board's dice
	board_.rollDice();
	//Variables to find the best movement
	double max =-1;
	int bestIndex = -1;
	//So, for all the bricks
	for(int i=0;i<4;i++){
	    //If we can move it
	    if(board_.moveable(i)) {
		//We obtain the network's input
		double[] situation = analyzeBrickSituation(i);
		//And compute the result
		double[] result = {0};
		playerNetwork_.compute(situation, result);
		//System.out.println("Brick " + (i+1) + ": " + result[0]);
		//We store the biggest result
		if(result[0]>max&&result[0]>0) {
		    bestIndex = i;
		    max = result[0];
		}
	    }
	}
	//Move the brick in the board
	if(bestIndex!=-1) board_.moveBrick(bestIndex);
    }

    /**
     * For a given network and its ideal solution, train the network.
     * @param network The network to train
     * @param idealSolution The ideal solution to train with
     */
    public void trainNetwork(BasicNetwork network, double[][] idealSolution){
	//Create a neural network
	network.addLayer(new BasicLayer(null,true,ANN_INPUT_LAYER)); //Input layer
	network.addLayer(new BasicLayer(new ActivationSigmoid(),true,ANN_HIDDEN_LAYER)); //Hidden Layer
	network.addLayer(new BasicLayer(new ActivationSigmoid(),false,1)); //Output
	network.getStructure().finalizeStructure();
	network.reset();
	//Create training data
	trainingInput();
	MLDataSet trainingSet = new BasicMLDataSet(getSituations(), idealSolution);
	//Train the neural network
	final ResilientPropagation train = new ResilientPropagation(network, trainingSet);

	int iteration = 1;
	do {
	    train.iteration();
	    //System.out.println("Epoch #" + iteration + " Error:" + train.getError());
	    iteration++;
	} while (train.getError()>0.001 && iteration<=5000);
	train.finishTraining();
	
	//Test the ANN
/*	double[] result = {0};
	System.out.println();
	for (int situation = 0; situation < situations.size(); situation++) {
	    playerNetwork_.compute(situations.get(situation), result);
	    System.out.println(new DecimalFormat("#0.00").format(result[0]) + "=>" 
		    + new DecimalFormat("#0.00").format(idealSolution[situation][0]));
	}*/
	
	
    }

    /**
     * Based on the board's information, translate the brick's situation to 
     * the network's input format.
     * @param i The brick to find the situation for
     * @return The vector that defines the situation in the network's input format
     */
    public double[] analyzeBrickSituation(int i) {
	double at_home = 0;
	double hit_opponent = 0;
	double hit_my_self = 0;
	double in_star = 0;
	double in_globe = 0;
	double move_out = 0;
	double close_to_enemy = 0;
	double hit_safe_area = 0;
	double almost_home = 0;

	if(board_.moveable(i)) {
	    int[][] current_board = board_.getBoardState();
	    int[][] new_board = board_.getNewBoardState(i, board_.getMyColor(), board_.getDice());

	    if (board_.atHome(new_board[board_.getMyColor()][i], board_.getMyColor()))
		at_home = 1.0;
	    if (hitOpponentHome(current_board,new_board))
		hit_opponent = 1.0;
	    if (hitMySelfHome(current_board,new_board))
		hit_my_self = 1.0;
	    if (board_.isStar(new_board[board_.getMyColor()][i]))
		in_star = 1.0;
	    if (board_.isGlobe(new_board[board_.getMyColor()][i]))
		in_globe = 1.0;
	    if (moveOut(current_board,new_board))
		move_out = 1.0;
	    if (closeToEnemy(new_board, i))
		close_to_enemy = 1.0;
	    if (hitSafeArea(current_board, new_board))
		hit_safe_area = 1.0;
	    if (board_.almostHome(new_board[board_.getMyColor()][i], board_.getMyColor()))
		almost_home = 1.0;	    
	}
	else {
	    //Illegal movement
	    double ANN_input[]={0,0,0,0,0,0,0,0,0};
	    return ANN_input;
	}

	double ANN_input[]={at_home,hit_opponent,hit_my_self,in_star,in_globe,move_out,close_to_enemy,hit_safe_area,almost_home};

	boolean situationExits = false;
	for (int j = 0; j < situations.size(); j++) {
	    int coincidences = 0;
	    for (int j2 = 0; j2 < ANN_input.length; j2++) {
		if (ANN_input[j2] == situations.get(j)[j2]){
		    coincidences++;
		}
	    }
	    if (coincidences == ANN_input.length){
		situationExits = true;
	    }

	}
	//If the situations doesn't exist
	if (!situationExits){
	    //Check if it is all zeros
	    int empty = 0;
	    for (int j2 = 0; j2 < ANN_INPUT_LAYER; j2++) {
		if (ANN_input[j2] == 0.0){
		    empty++;
		}
	    }
	    //If not, we have found a new situation and we can add it
	    if (empty != ANN_INPUT_LAYER){
		System.out.print("New situation: ");
		for (int j1 = 0; j1 < ANN_input.length; j1++) {
		    System.out.print(ANN_input[j1] + " ");	    
		}
		System.out.println();
		situations.add(ANN_input);
	    }
	}

	return ANN_input;
    }


    //-----------------------------------------------------------------------
    //				Situations
    //-----------------------------------------------------------------------
    /*
     * Defined in the board class we can obtain:
     * 		1. inStart
     * 		2. atHome
     * 		3. almostHome
     * 		4. atField
     * 		5. isGlobe
     * 		6. isStar
     * And also we can get this situations
     */
    /**
     * Method to transform the Array of situations to a double[][]. This is
     * necessary for train the network
     * @return A double[][] with the possible situations
     */
    private double[][] getSituations(){
	double[][] situations_temp = new double[situations.size()][ANN_INPUT_LAYER] ;

	for (int situation = 0; situation < situations.size(); situation++) {
	    for (int gen = 0; gen < ANN_INPUT_LAYER; gen++) {
		situations_temp[situation][gen] = situations.get(situation)[gen];
	    }
	}

	return situations_temp;
    }
    
    /**
     * Returns the size of the chromosome -This is the possible states-
     * @return The size of the chromosome
     */
    public int chromosomeSize(){
	return situations.size();
    }

    /**
     * Get out of home
     */
    private boolean moveOut(int[][] current_board, int[][] new_board) {
	for(int i=0;i<4;i++) {
	    if(board_.inStartArea(current_board[board_.getMyColor()][i],board_.getMyColor())&&!board_.inStartArea(new_board[board_.getMyColor()][i],board_.getMyColor())) {
		return true;
	    }
	}
	return false;
    }
    /**
     * Hit opponent and send it to home
     */
    private boolean hitOpponentHome(int[][] current_board, int[][] new_board) {
	for(int i=0;i<4;i++) {
	    for(int j=0;j<4;j++) {
		if(board_.getMyColor()!=i) {
		    if(board_.atField(current_board[i][j])&&!board_.atField(new_board[i][j])) {
			return true;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * Hit my self to my own home
     */
    private boolean hitMySelfHome(int[][] current_board, int[][] new_board) {
	for(int i=0;i<4;i++) {
	    if(!board_.inStartArea(current_board[board_.getMyColor()][i],board_.getMyColor())&&board_.inStartArea(new_board[board_.getMyColor()][i],board_.getMyColor())) {
		return true;
	    }
	}
	return false;
    }

    /*
     * Check if it lands close to an enemy
     */
    private boolean closeToEnemy(int[][] new_board, int brickToCheck){
	//For all the the other colors bricks
	for(int color=0; color<4; color++){
	    for (int brick=0; brick<4; brick++){
		if (color!=board_.getMyColor()){
		    if( (new_board[color][brick] - new_board[board_.getMyColor()][brickToCheck]) < 6 &&
			    (new_board[color][brick] - new_board[board_.getMyColor()][brickToCheck]) > 0)
			return true;
		}
	    }
	}
	return false;
    }

    /*
     * Check if it lands in the safe area
     */
    private boolean hitSafeArea(int[][] current_board, int[][] new_board) {
	for(int i=0;i<4;i++) {
	    if(board_.atField(current_board[board_.getMyColor()][i]) && 
		    board_.almostHome(new_board[board_.getMyColor()][i], board_.getMyColor()) ) {
		return true;
	    }
	}
	return false;
    }

    //-----------------------------------------------------------------------
    //				ANN Stuff
    //-----------------------------------------------------------------------

    /**
     * Array that represents the possible combinations of inputs. The inputs are:
     * 1. The brick can get home
     * 2. The brick is able to hit an opponent home
     * 3. If the brick moves, it will be hit home
     * 4. The brick will land on a star
     * 5. The brick will land on a globe
     * 6. The brick can get out of the start area
     * 7. If the brick moves, it will have an enemy near
     * 8. If the brick moves, it will get into the safe area
     * 9. The brick is in the safe are
     * @return The array of possible inputs
     */
    private double[][] trainingInput(){
	double training_input[][] = {
		{1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, // + atHome
		{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, // + hitOpponentHome
		{0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0}, // + hitMySelfHome
		{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, // + isStar
		{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0}, // + isGlobe

		//{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0}, // - moveOut -> Not possible!
		{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0}, // + closeToEnemy
		//{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0}, // - hitSafeArea -> Not possible!
		{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0}, // + almostHome
		{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0}, // + hitOpponentsHome + isStar

		{0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0}, // + hitOpponentsHome + moveOut + isGlobe
		{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0}, // + hitOpponentsHome + closeToEnemy
		{0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0}, // + isStar + closeToEnemy
		{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0, 0.0}, // + isGlobe + moveOut
		{0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0}, // + isGlobe + closeToEnemy

		//{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0, 0.0}, // - moveOut + closeToEnemy
		{0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0, 0.0, 0.0}, // + hitOpponentsHome + isStar + closeToEnemy
		{0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0}, // - hitOpponentsHome + moveOut + isGlobe + closeToEnemy
		//{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 0.0}, // - hitSafeArea + closeToEnemy

		//{0.0, 1.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0},  // - hitOpponentHome + isGlobe + moveOut + closeToEnemy
		{0.0, 0.0, 0.0, 0.0, 1.0, 1.0, 1.0, 0.0, 0.0}, // + isGlobe + moveOut + closeToEnemy
		{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 1.0}, // + closeToEnemy + hitSafeArea
	};

	for (int situation = 0; situation < training_input.length; situation++) {
	    situations.add(training_input[situation]);
	}

	return training_input;
    }

    /**
     * Example of ideal solution for an ANN player
     * @return
     */
    private double[][] idealSolution(){
	double training_output[][] = {
		{1.00},
		{0.78},
		{0.04},
		{0.21},
		{0.47},

		{0.74},
		{0.02},
		{0.66},
		{0.00},
		{0.34},

		{0.70},
		{0.11},
		{0.37},
		{0.39},
		{0.06},

		{0.11},
		{1.00},
	};
	return training_output;
    }
}
