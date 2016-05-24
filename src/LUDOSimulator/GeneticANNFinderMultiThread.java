package LUDOSimulator;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.plot.AbstractPlot;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;

public class GeneticANNFinderMultiThread {

    private static int GENERATION_LIMIT = 50;
    private static int number_of_games = 1000;
    public static boolean only_won = false;

    public static int chromosome_size = ANNLUDOPlayer.chromosome_size;
    private static int number_of_players = 8;
    public double [][] solution = new double[chromosome_size][1];
    private static ArrayList<double[][]> chromosomes = new ArrayList<double[][]> (number_of_players);
    public double [][] the_legend_chromosome = new double[chromosome_size][1];
    private double the_legend_score = 0;
    private volatile static int chromosome_to_use;
    private static double [] ranking = new double[number_of_players];
    private static int [] index = new int[number_of_players];
    private int waitingTimeMs = 0; 
    Thread[] games = new Thread[number_of_players];


    /**
     * Constructs a finder and finds a player
     */
    public GeneticANNFinderMultiThread(){
	findPlayer();
    }

    private static class ParallelGame
    implements Runnable{
	public void run(){
	    //Variables
	    double[] result = new double[4];
	    int thread_number = chromosome_to_use;
	    double [][] chromosome = getChromosome(thread_number);
	    threadIncrement();
	    //System.out.println("Thread " + chromosome_to_use);

	    //Let's create a new game with this 4 players to find the best one
	    LUDOBoard board = new LUDOBoard();
	    //Initialize the board's players
	    ANNLUDOPlayer player_1 = new ANNLUDOPlayer(board, chromosome);
	    SemiSmartLUDOPlayer player_2 = new SemiSmartLUDOPlayer(board);
	    SemiSmartLUDOPlayer player_3 = new SemiSmartLUDOPlayer(board);
	    SemiSmartLUDOPlayer player_4 = new SemiSmartLUDOPlayer(board);
	    //Qlearner player_2 = new Qlearner(board);
	    //Qlearner player_3 = new Qlearner(board);
	    //Qlearner player_4 = new Qlearner(board);

	    board.setPlayer(player_1,LUDOBoard.YELLOW);
	    board.setPlayer(player_2,LUDOBoard.RED);
	    board.setPlayer(player_3,LUDOBoard.BLUE);
	    board.setPlayer(player_4,LUDOBoard.GREEN);

	    //Reset the counters
	    result[0] = 0;
	    result[1] = 0;
	    result[2] = 0;
	    result[3] = 0;
	    double total_points = 0;
	    try {
		//And play 1000 games
		for(int i=0;i<number_of_games;i++) {
		    board.play();
		    board.kill();

/*		    if (only_won){
			if(board.getPoints()[0]==3) ++result[0];
			if(board.getPoints()[1]==3) ++result[1];
			if(board.getPoints()[2]==3) ++result[2];
			if(board.getPoints()[3]==3) ++result[3];
		    } else {
			result[0]+=board.getPoints()[0];
			result[1]+=board.getPoints()[1];
			result[2]+=board.getPoints()[2];
			result[3]+=board.getPoints()[3];
		    }*/
		    
			if (only_won){
			    total_points = number_of_games;
			    if(board.getPoints()[0]==3) result[0] += (double)(100/total_points);
			    if(board.getPoints()[1]==3) result[1] += (double)(100/total_points);
			    if(board.getPoints()[2]==3) result[2] += (double)(100/total_points);
			    if(board.getPoints()[3]==3) result[3] += (double)(100/total_points);
			} else {
			    total_points = 6*number_of_games;
			    result[0]+=(double)board.getPoints()[0]/total_points*100;
			    result[1]+=(double)board.getPoints()[1]/total_points*100;
			    result[2]+=(double)board.getPoints()[2]/total_points*100;
			    result[3]+=(double)board.getPoints()[3]/total_points*100;
			}
			
		    board.reset();

		}
	    } catch (InterruptedException e) {
		e.printStackTrace();
	    }

	    writeResult(result[0], thread_number);
	}
    }

    public static synchronized void threadIncrement(){
	chromosome_to_use++;
    }

    public static synchronized void writeResult(double result, int thread){
	ranking[thread] = result;
	index[thread] = thread;
    }

    public static synchronized double[][] getChromosome(int thread){
	return chromosomes.get(thread);
    }


    /**
     * Creates a new game with 4 ANNs players to find the best one
     */
    public void findPlayer(){
	System.out.println("GA finder for " + GENERATION_LIMIT + 
		" generations and " + number_of_players + " players.");	
	//Variables
	ArrayList<Long> timeArray = new ArrayList<Long>();
	long averageTime = 0;
	int generationCount = 0;

	//Creates randomly the chromosomes of four players
	Random randomGen = new Random();
	for (int player = 0; player < number_of_players; player++) {
	    chromosomes.add(new double[][] { {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, 
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
		    {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()}, {randomGen.nextDouble()},
	    });
	}


	//Show Chromosomes
	System.out.println();
	for (int i = 0; i < number_of_players; i++){
	    System.out.print("	Chromosome " + (i+1) +  ": ");
	    for (int gen = 0; gen < chromosome_size; gen++) 
		System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(i)[gen][0]) + " ");
	    System.out.println();
	}

	/*
	 * Plot
	 */
	//Create the plot
	JavaPlot plot = new JavaPlot();
	//Four sets of data
	PointDataSet<Double> data_set_1 = new PointDataSet<Double>();

	/*
	 * Starts the Genetic Algorithm
	 */
	for (int generation=0; generation<GENERATION_LIMIT; generation++){
	    long startTime = System.currentTimeMillis();
	    System.out.println("	Generation " + generationCount + "/" + GENERATION_LIMIT) ;

	    /*
	     * Creation of games 
	     */
	    //Spawning and start
	    chromosome_to_use=0;
	    for (int game = 0; game < games.length; game++) {
		games[game] = new Thread(new ParallelGame());
		games[game].start();
	    }	    
	    //Wait for them to end
	    for (int game = 0; game < games.length; game++) {
		try {
		    games[game].join(waitingTimeMs);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
	    }

	    /*
	     * Now apply evolutionary algorithm
	     */
	    //Selection 
	    //Find the best players ordering the vector of results
	    double temp = -1;
	    double result_temp[] = new double[number_of_players];
	    //Initialization
	    for (int i = 0; i < number_of_players; i++){
		index[i] = i;
		result_temp[i] = ranking[i];
	    }

	    //Ordering and store the indexes
	    for (int j=0; j<number_of_players; j++) {
		for (int i=0; i<number_of_players-1; i++) {
		    if (result_temp[i+1] > result_temp[i]){
			temp = result_temp[i];
			result_temp[i] = result_temp[i+1];
			result_temp[i+1] = (int)temp;

			temp = index[i];
			index[i] = index[i+1];
			index[i+1] = (int)temp;
		    }
		}
	    }

	    //Check if we have found a new legend 
	    if (ranking[index[0]] > the_legend_score){
		System.out.println("New legend!");
		the_legend_score = result_temp[0];
		for (int gen = 0; gen < chromosome_size; gen++)
		    the_legend_chromosome[gen] = chromosomes.get(index[0])[gen].clone();
	    }

	    //Show Chromosomes
	    System.out.println();
	    System.out.print("	The Legend: " + new DecimalFormat("#0.00").format(the_legend_score) + "	");
	    for (int gen = 0; gen < chromosome_size; gen++)
		System.out.print(new DecimalFormat("#0.00").format(the_legend_chromosome[gen][0]) + " ");
	    System.out.println();
	    for (int i = 0; i < number_of_players; i++){
		System.out.print("	Chromosome " + (index[i]+1) +  ": " + new DecimalFormat("#0.00").format(ranking[index[i]]) + "	");
		for (int gen = 0; gen < chromosome_size; gen++)
		    System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(index[i])[gen][0]) + " ");
		System.out.println();
	    }

	    //And add this to the plot
	    data_set_1.add(new Point<Double>((double)(generation+1),ranking[index[0]]));

	    //Crossover.
	    //The father and the mother (index[0] and index[1]) stay equal
	    //The children are crossover from them randomly
	    Random random = new Random();
	    for (int children = 2; children < number_of_players; children++) {
		int rand = random.nextInt(chromosome_size);
		//Single point
		/*
 		int crossover_point = random.nextInt(chromosome_size);
		//Father
		for (int gen = 0; gen < crossover_point; gen++)
		    chromosomes.get(index[children])[gen] = chromosomes.get(index[0])[gen].clone();
		//Mother
		for (int gen = chromosome_size-1; gen >= chromosome_size-crossover_point; gen--)
		    chromosomes.get(index[children])[gen] = chromosomes.get(index[1])[gen].clone();
		*/
		//Uniform Crossover
		//Creates a mask. 0=father and 1=mother. Density in the second for!
		int[] mask = new int[chromosome_size];
		for (int gen = 0; gen < chromosome_size; gen++)
		    mask[gen] = 0;
		for (int gen = 0; gen < chromosome_size/2; gen++)
		    mask[rand] = 1;
		//Copy the gens based on the mask
		for (int gen = 0; gen < chromosome_size; gen++){
		    //Father
		    if (mask[gen]==0)
			chromosomes.get(index[children])[gen] = chromosomes.get(index[0])[gen].clone();
		    //Mother
		    else
			chromosomes.get(index[children])[gen] = chromosomes.get(index[1])[gen].clone();
		}
	    }

	    //Mutation
	    //And we apply mutation randomly to their genes
	    double rangeMin = -0.2;
	    double rangeMax = 0.2;
	    for (int children = 2; children < number_of_players; children++) {
		for (int gen = 0; gen < chromosome_size; gen++) {
		    double mutation = rangeMin + (rangeMax - rangeMin) * random.nextDouble();
		    if (random.nextBoolean()) chromosomes.get(index[children])[gen][0] = chromosomes.get(index[children])[gen][0]+mutation;
		    //Check Limits
		    if (chromosomes.get(index[children])[gen][0] < 0.0) chromosomes.get(index[children])[gen][0] = 0;
		    if (chromosomes.get(index[children])[gen][0] > 1.0) chromosomes.get(index[children])[gen][0] = 1.0;
		}
	    }
	    //Increase the generation counter
	    generationCount++;
	    //Calculate ETA
	    timeArray.add(System.currentTimeMillis() - startTime);
	    averageTime = 0;
	    for (int j = 0; j < timeArray.size(); j++) 
		averageTime += timeArray.get(j);
	    averageTime = averageTime/timeArray.size();
	    long ETA_sec = averageTime*(GENERATION_LIMIT-generationCount)/1000;
	    if (ETA_sec > 60) System.out.println("ETA: " + ETA_sec/60 + " minutes");
	    else System.out.println("ETA: " + ETA_sec + " seconds");
	    
	    if (generationCount!=0 && generationCount%(GENERATION_LIMIT/10) == 0){
		/*
		 * Plots!
		 */
		//Add the data to the plot
		plot.getPlots().clear();
		plot.addPlot(data_set_1); 
		((AbstractPlot) plot.getPlots().get(0)).setTitle("ANNGAPlayer");
		//Axis's name
		plot.getAxis("x").setLabel("Generation");
		plot.getAxis("y").setLabel("Points/Total points [%]");
		//Lets print an eps file
		PostscriptTerminal eps_file = new PostscriptTerminal("/mnt/Free/Drive/Robot Systems/Artificial Intelligence 2/LUDO/report/figures"
			+ System.getProperty("file.separator") + "evolution.eps");
		eps_file.setColor(true);	
		plot.setTerminal(eps_file);
		
		//This is to define the style 
		PlotStyle style_1 = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
		style_1.setStyle(Style.LINESPOINTS);
		style_1.setLineType(NamedPlotColor.VIOLET);
		style_1.setPointType(1);       
		
		plot.plot();
	    }
	}



	//Copy the result to the solution
	System.out.println();
	System.out.println();
	System.out.print("Final chromosome: ");
	for (int gen = 0; gen < chromosome_size; gen++) {	    
	    solution[gen][0] = chromosomes.get(index[0])[gen][0];
	    System.out.print(new DecimalFormat("#0.00").format(chromosomes.get(index[0])[gen][0]) + ", ");
	}
	System.out.println();
	System.out.print("The Legend: ");
	for (int gen = 0; gen < chromosome_size; gen++) 	    
	    System.out.print(new DecimalFormat("#0.00").format(the_legend_chromosome[gen][0]) + ", ");
	System.out.println();

    }
}
