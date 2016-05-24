package LUDOSimulator;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

import com.panayotis.gnuplot.JavaPlot;
import com.panayotis.gnuplot.dataset.Point;
import com.panayotis.gnuplot.dataset.PointDataSet;
import com.panayotis.gnuplot.style.NamedPlotColor;
import com.panayotis.gnuplot.style.PlotStyle;
import com.panayotis.gnuplot.style.Style;
import com.panayotis.gnuplot.terminal.PostscriptTerminal;
import com.panayotis.gnuplot.plot.AbstractPlot;
/**
 * Main class the LUDO simulator - "controls" the game.
 * This is where you decide how many games to play and if 
 * the graphical interface should be visible.
 * 
 * @author David Johan Christensen
 * 
 * @version 0.9
 */
public class LUDO extends Frame implements ActionListener
{
    private static final long serialVersionUID = 1L;

    static LUDOBoard board;
    public LUDO() 
    {  
	super("LUDO Simulator");
	setBackground(Color.white);
	board = new LUDOBoard();
	add(board, BorderLayout.CENTER);

	Menu optionsMenu = new Menu("Options", true);  
	optionsMenu.add("Reset Game");
	optionsMenu.addActionListener(this);

	MenuBar mbar = new MenuBar();  
	mbar.add(optionsMenu);

	setMenuBar(mbar); // Add the menu bar to the frame.
	setBounds(30,50,1000,800);  // Set size and position of window.

	setResizable(false);
	addWindowListener(
		new WindowAdapter() {
		    public void windowClosing(WindowEvent evt) {
			LUDO.this.dispose();
			System.exit(0);

		    }
		}
		);
	setVisible(visual);
	play();
    }
    public void actionPerformed(ActionEvent event) 
    {
	if(event.getActionCommand()=="Reset Game") {
	    board.kill();
	}
    }
    public static boolean visual = true;
    /**
     * Plays a number of games, which are useful when all players are automatic.
     * Remember to set the "visual" field to speed up the simulation time.
     */
    public void play() {
	System.out.println("Playing Ludo");

	//Find with GA the ANN player
	//GeneticANNFinderMultiThread GAfinderAnnFinder = new GeneticANNFinderMultiThread();
	//ANNLUDOPlayer ANNGAPlayer = new ANNLUDOPlayer(board, GAfinderAnnFinder.solution);
	
	//ANNLUDOPlayer ANNGAPlayer = new ANNLUDOPlayer(board, 1.00, 0.64, 0.00, 0.99, 1.00, 0.41, 0.00, 0.51, 0.90, 1.00, 0.80, 0.00, 0.00, 0.33, 0.66, 0.31, 0.34); //Legend: 48
	ANNLUDOPlayer ANNGAPlayer = new ANNLUDOPlayer(board, 0.45, 1.00, 0.00, 0.63, 0.48, 0.10, 0.00, 0.94, 0.74, 0.66, 0.93, 0.15, 0.00, 0.95, 0.40, 0.11, 0.30);
	//ANNLUDOPlayer ANNGAPlayer = new ANNLUDOPlayer(board);

	double[] result = new double[4];
	int won = 0;

	//Initialize the board's players
	ANNLUDOPlayer player_1 = ANNGAPlayer;
	SemiSmartLUDOPlayer player_2 = new SemiSmartLUDOPlayer(board);
	//SemiSmartLUDOPlayer player_3 = new SemiSmartLUDOPlayer(board);
	//SemiSmartLUDOPlayer player_4 = new SemiSmartLUDOPlayer(board);


	//ANNLUDOPlayer player_2 = ANNGAPlayer;
	//ANNLUDOPlayer player_3 = ANNGAPlayer;
	//ANNLUDOPlayer player_4 = ANNGAPlayer;
	
	//NNSmartPlayer player_2 = new NNSmartPlayer(board, 0);
	NNSmartPlayer player_3 = new NNSmartPlayer(board, 0);
	//NNSmartPlayer player_4 = new NNSmartPlayer(board, 0);
	

	//Qlearner player_2 = new Qlearner(board);
	//Qlearner player_3 = new Qlearner(board);
	Qlearner player_4 = new Qlearner(board, 0);

	board.setPlayer(player_1, LUDOBoard.YELLOW);
	board.setPlayer(player_2, LUDOBoard.RED);
	board.setPlayer(player_3, LUDOBoard.BLUE);
	board.setPlayer(player_4, LUDOBoard.GREEN);

	//Create the plot
	JavaPlot plot = new JavaPlot();
	//Four sets of data
	PointDataSet<Double> data_set_1 = new PointDataSet<Double>();
	PointDataSet<Double> data_set_2 = new PointDataSet<Double>();
	PointDataSet<Double> data_set_3 = new PointDataSet<Double>();
	PointDataSet<Double> data_set_4 = new PointDataSet<Double>();
	//ArrayList<Integer> results_player_1 = new ArrayList<Integer>();
	
	int number_of_games = 10000;
	int game_window = 100;
	double total_points = 0;
	try {
	    for(int i=0;i<number_of_games;i++) {
		board.play();
		board.kill();

		if (true){
		    total_points = 3*number_of_games;
		    if(board.getPoints()[0]==3) ++result[0];
		    if(board.getPoints()[1]==3) ++result[1];
		    if(board.getPoints()[2]==3) ++result[2];
		    if(board.getPoints()[3]==3) ++result[3];
		} else {
		    total_points = 6*number_of_games;
		    result[0]+=(double)board.getPoints()[0]/total_points*100;
		    result[1]+=(double)board.getPoints()[1]/total_points*100;
		    result[2]+=(double)board.getPoints()[2]/total_points*100;
		    result[3]+=(double)board.getPoints()[3]/total_points*100;
		}
		//results_player_1.add(result[0]);
		if (i%number_of_games/game_window == 0 && i!=0) data_set_1.add(new Point<Double>((double)i*game_window,(double)result[0]*game_window));
		if (i%number_of_games/game_window == 0 && i!=0) data_set_2.add(new Point<Double>((double)i*game_window,(double)result[1]*game_window));
		if (i%number_of_games/game_window == 0 && i!=0) data_set_3.add(new Point<Double>((double)i*game_window,(double)result[2]*game_window));
		if (i%number_of_games/game_window == 0 && i!=0) data_set_4.add(new Point<Double>((double)i*game_window,(double)result[3]*game_window));

		if (board.getPoints()[0] == 3) won++;

		board.reset();
		
		board.setPlayer(new Qlearner(board, i), LUDOBoard.BLUE);
		board.setPlayer(new Qlearner(board, i), LUDOBoard.GREEN);
		if((i%100)==0) System.out.print(".");
	    }
	} catch (InterruptedException e) {
	    e.printStackTrace();
	}

	//Calculates the sample standard deviation
/*	double main_player_1 = (double)result[0]/(double)number_of_games;
	double main_temp = 0.0;
	double sample_standard_deviation_player_1 = 0.0;
	int samples = 1;
	for (int i = 0; i < results_player_1.size()/samples; i++) {
	    for (int j = 0; j < samples; j++) {
		main_temp+=results_player_1.get(i*samples+j);
	    }
	    main_temp/=(i*samples*samples);
	    sample_standard_deviation_player_1 = main_temp - main_player_1;
	    //sample_standard_deviation_player_1 = Math.sqrt(sample_standard_deviation_player_1/samples);
	    data_set_1.add(new Point<Double>((double)i*samples,sample_standard_deviation_player_1));
	    sample_standard_deviation_player_1=0;
	    main_temp=0;
	}*/
	
	System.out.println();
	System.out.println("RESULT:");
	System.out.println("	ANNGA  Player: " + (double)result[0]);
	System.out.println("	RED    Player: " + (double)result[1]);
	System.out.println("	BLUE   Player: " + (double)result[2]);
	System.out.println("	GREEN  Player: " + (double)result[3]);
	System.out.println("	Won: " + won);

	/*
	 * Plots!
	 */
	//Add the data to the plot
	plot.addPlot(data_set_1); 
	plot.addPlot(data_set_2); 
	plot.addPlot(data_set_3); 
	plot.addPlot(data_set_4);

	//Put the name of the player in the plot
	((AbstractPlot) plot.getPlots().get(0)).setTitle(
		player_1.toString().substring(
			player_1.toString().indexOf(".")+1,
			player_1.toString().indexOf("@")
		)
	);
	((AbstractPlot) plot.getPlots().get(1)).setTitle(
		player_2.toString().substring(
			player_2.toString().indexOf(".")+1,
			player_2.toString().indexOf("@")
		)
	);
	((AbstractPlot) plot.getPlots().get(2)).setTitle(
		player_3.toString().substring(
			player_3.toString().indexOf(".")+1,
			player_3.toString().indexOf("@")
		)
	);
	((AbstractPlot) plot.getPlots().get(3)).setTitle(
		player_4.toString().substring(
			player_4.toString().indexOf(".")+1,
			player_4.toString().indexOf("@")
		)
	);
	
	//Axis's name
	plot.getAxis("x").setLabel("Game");
	plot.getAxis("y").setLabel("Points/Total points [%]");
	//Lets print an eps file
	PostscriptTerminal eps_file = new PostscriptTerminal("/mnt/Free/Drive/Robot Systems/Artificial Intelligence 2/LUDO/report/figures"
		+ System.getProperty("file.separator") + "output.eps");
	eps_file.setColor(true);	
	plot.setTerminal(eps_file);

	//This is to define the style 
	PlotStyle style_1 = ((AbstractPlot) plot.getPlots().get(0)).getPlotStyle();
	style_1.setStyle(Style.LINESPOINTS);
	style_1.setLineType(NamedPlotColor.GOLDENROD);
	style_1.setPointType(1);       

	PlotStyle style_2 = ((AbstractPlot) plot.getPlots().get(1)).getPlotStyle();
	style_2.setStyle(Style.LINESPOINTS);
	style_2.setLineType(NamedPlotColor.CYAN);
	style_2.setPointType(2);  

	PlotStyle style_3 = ((AbstractPlot) plot.getPlots().get(2)).getPlotStyle();
	style_3.setStyle(Style.LINESPOINTS);
	style_3.setLineType(NamedPlotColor.GREEN);
	style_3.setPointType(3);  

	PlotStyle style_4 = ((AbstractPlot) plot.getPlots().get(3)).getPlotStyle();
	style_4.setStyle(Style.LINESPOINTS);
	style_4.setLineType(NamedPlotColor.MAGENTA);
	style_4.setPointType(4);  

	plot.plot();
    }

    public static void main(String[] args)
    {
	new LUDO();
    }
}