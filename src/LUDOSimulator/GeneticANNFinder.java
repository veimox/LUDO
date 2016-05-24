package LUDOSimulator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

public class GeneticANNFinder {

    private static int GENERATION_LIMIT = 4;
    public int chromosome_size = 17;
    private int number_of_players = 4;
    public double [][] solution = new double[chromosome_size][1];

    /**
     * Constructs a finder and finds the player
     */
    public GeneticANNFinder(){
	findPlayer();
    }
    
    /**
     * Creates a new game with 4 ANNs players to find the best one
     */
    public void findPlayer(){
	System.out.println("GA finder for " + GENERATION_LIMIT + " generations") ;	
	//Variables
	ArrayList<Long> timeArray = new ArrayList<Long>();
	long averageTime = 0;
	int[] result = new int[4];
	int[] index = {0,1,2,3};
	ArrayList<double[][]> chromosomes = new ArrayList<double[][]> (number_of_players);
	int generationCount = 0;

	//Let's create a new game with this 4 players to find the best one
	LUDOBoard board = new LUDOBoard();

	//Creates randomly the chromosomes of four players
	Random randomGen = new Random();
	for (int player = 0; player < number_of_players; player++) {
	    chromosomes.add(new double[][] { {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, 
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
	    });
	}
	
	for (int i = 0; i < number_of_players; i++){
	    System.out.print("	Chromosome " + (i+1) +  ": ");
	    for (int gen = 0; gen < chromosome_size; gen++) 
		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(i)[gen][0]) + " ");
	    System.out.println();
	}


	/*
	 * Starts the Genetic Algorithm
	 */
	for (int generation=0; generation<GENERATION_LIMIT; generation++){
	    long startTime = System.currentTimeMillis();
	    System.out.println("	Generation " + generationCount + "/" + GENERATION_LIMIT) ;
	    
//	    System.out.print("	Chromosome 1: ");
//	    for (int gen = 0; gen < chromosome_size; gen++)
//		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(0)[gen][0]) + " ");
//	    System.out.println("");
//	    System.out.print("	Chromosome 2: ");
//	    for (int gen = 0; gen < chromosome_size; gen++)
//		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(1)[gen][0]) + " ");
//	    System.out.println("");
//	    System.out.print("	Chromosome 3: ");
//	    for (int gen = 0; gen < chromosome_size; gen++)
//		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(2)[gen][0]) + " ");
//	    System.out.println("");
//	    System.out.print("	Chromosome 4: ");
//	    for (int gen = 0; gen < chromosome_size; gen++)
//		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(3)[gen][0]) + " ");
//	    System.out.println("");

	    //Initialize the board's players
	    ANNLUDOPlayer player_1 = new ANNLUDOPlayer(board, chromosomes.get(0));
	    ANNLUDOPlayer player_2 = new ANNLUDOPlayer(board, chromosomes.get(1));
	    ANNLUDOPlayer player_3 = new ANNLUDOPlayer(board, chromosomes.get(2));
	    ANNLUDOPlayer player_4 = new ANNLUDOPlayer(board, chromosomes.get(3));

	    board.setPlayer(player_1,LUDOBoard.YELLOW);
	    board.setPlayer(player_2,LUDOBoard.RED);
	    board.setPlayer(player_3,LUDOBoard.BLUE);
	    board.setPlayer(player_4,LUDOBoard.GREEN);

	    //Reset the counters
	    result[0] = 0;
	    result[1] = 0;
	    result[2] = 0;
	    result[3] = 0;

	    try {
		//And play 1000 games
		for(int i=0;i<1000;i++) {
		    board.play();
		    board.kill();

		    result[0]+=board.getPoints()[0];
		    result[1]+=board.getPoints()[1];
		    result[2]+=board.getPoints()[2];
		    result[3]+=board.getPoints()[3];

		    board.reset();
		    board.setPlayer(player_1,LUDOBoard.YELLOW);
		    board.setPlayer(player_2,LUDOBoard.RED);
		    board.setPlayer(player_3,LUDOBoard.BLUE);
		    board.setPlayer(player_4,LUDOBoard.GREEN);

		    if((i%500)==0) System.out.print(".");
		}
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }


	    /*
	     * Now apply evolutionary algorithm
	     */
	    //Selection 
	    //Find the best player (index[0])
	    int temp = -1;
	    index[0]=0; index[1]=1; index[2]=2; index[3]=3;
	    int result_temp[] = {0,0,0,0};
	    //Make a copy for ordering
	    for (int i=0; i<number_of_players; i++) 
		result_temp[i] = result[i];
	    //Ordering and store the indexes
	    for (int j=0; j<number_of_players; j++) {
		for (int i=0; i<number_of_players-1; i++) {
		    if (result_temp[i+1] > result_temp[i]){
			temp = result_temp[i];
			result_temp[i] = result_temp[i+1];
			result_temp[i+1] = temp;

			temp = index[i];
			index[i] = index[i+1];
			index[i+1] = temp;
		    }
		}
	    }

	    //	    System.out.println();
	    //	    System.out.println("	Chromosome " + (index[0]+1) + ": " + result[index[0]]);
	    //	    System.out.println("	Chromosome " + (index[1]+1) + ": " + result[index[1]]);
	    //	    System.out.println("	Chromosome " + (index[2]+1) + ": " + result[index[2]]);
	    //	    System.out.println("	Chromosome " + (index[3]+1) + ": " + result[index[3]]);

	    //Crossover.
	    //The father and the mother (index[0] and index[1]) stay equal
	    //The children are crossover from them randomly
	    Random random = new Random();
	    for (int children = number_of_players-2; children < number_of_players; children++) {
		int crossover = random.nextInt(chromosome_size);
		//Father
		for (int gen = 0; gen < crossover; gen++) {
		    chromosomes.get(index[children])[gen][0] = chromosomes.get(index[0])[gen][0];
		}
		//Mother
		for (int gen = chromosome_size-1; gen >= chromosome_size-crossover; gen--) {
		    chromosomes.get(index[children])[gen][0] = chromosomes.get(index[1])[gen][0];
		}
	    }

	    //Mutation
	    //And we apply mutation randomly to their genes
	    double rangeMin = -0.4;
	    double rangeMax = 0.4;
	    for (int children = number_of_players-2; children < number_of_players; children++) {
		for (int gen = 0; gen < chromosome_size; gen++) {
		    double mutation = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
		    chromosomes.get(index[children])[gen][0] = chromosomes.get(index[children])[gen][0]+mutation;
		    //Check Limits
		    if (chromosomes.get(index[children])[gen][0] < 0.0) chromosomes.get(index[children])[gen][0] = 0;
		    if (chromosomes.get(index[children])[gen][0] > 1.0) chromosomes.get(index[children])[gen][0] = 1.0;
		}
	    }

	    generationCount++;
	    
	    //Calculate ETA
	    timeArray.add(System.currentTimeMillis() - startTime);
	    averageTime = 0;
	    for (int j = 0; j < timeArray.size(); j++) 
		averageTime += timeArray.get(j);
	    averageTime = averageTime/timeArray.size();
	    long ETA_sec = averageTime*(GENERATION_LIMIT-generationCount)/1000;
	    if (ETA_sec > 60) System.out.println("ETA: " + ETA_sec/60 + "min");
	    else System.out.println("ETA: " + ETA_sec + "sec");
	}
	

	//Copy the result to the solution
	System.out.println();
	System.out.println();
	System.out.print("Final chromosome: ");
	for (int gen = 0; gen < chromosome_size; gen++) {
	    solution[gen][0] = chromosomes.get(index[0])[gen][0];
	    System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(index[0])[gen][0]) + " ");
	}
	System.out.println();

    }
}
