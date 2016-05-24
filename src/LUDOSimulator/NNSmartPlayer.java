package LUDOSimulator;

public class NNSmartPlayer implements LUDOPlayer
{
	LUDOBoard BOARD;
	
	//hola hola

	static int NUMGAMES;
	static int neuronsPerLayer[] = {25, 4};
	static int inputsnr = 4;  // amount of input nodes
	static int outputsnr = 4;  // amount of input nodes
	static NeuralNetwork NN = new NeuralNetwork(neuronsPerLayer, inputsnr);
	static double outputVector[] = new double[outputsnr];
	static double desiredOutVector[] = new double [outputsnr];
	static double inputVector[] = new double[inputsnr];
	static boolean debuggin = false;

	public NNSmartPlayer(LUDOBoard board, int nungames)
	{
		BOARD = board;
		NUMGAMES = nungames;
	}
	
	public void play()
	{
		BOARD.rollDice();

		//Get input vector from board analysis
		for(int i=0;i<4;i++){
			inputVector[i] = (analyzeBrickSituation(i)*(14/6))-7.0; 	//Mapping to [-7,7] input values
		}
		
		//Activate NN with the input of the board
		double results[] = NN.activate(inputVector);
		for(int j=0;j<4;j++){
			outputVector[j] = results[j];
		}
		int brick = BrickToMove();
		
		//Training process
		if(outputVector[0]==outputVector[1] && outputVector[1]==outputVector[2] 
		   && outputVector[2]==outputVector[3] && outputVector[3]==outputVector[0]){
			desiredOutVector[0]=-1;
			desiredOutVector[1]=-1;
			desiredOutVector[2]=-1; 
			desiredOutVector[3]=-1;
			desiredOutVector[brick]=1;
		}
		else{
			NNTrainer Trainer = new NNTrainer(BOARD);
			desiredOutVector= Trainer.training();
		}
			
		//if(Error(outputVector, desiredOutVector)>0.0001){
		if(NUMGAMES<1000){
			//double desiredoutputs[] = desiredOutVector;
			NN.backpropagate(outputVector, desiredOutVector);
		}
		//}
		/*else {
			NN.backpropagate(desiredOutVector, desiredOutVector);
		}*/ 
		
		if(debuggin){
			System.out.println("--------------------------");
			System.out.println("Dice: "+ BOARD.getDice());
			System.out.println("inputVector:" +inputVector[0]+","+inputVector[1]+","+inputVector[2]+","+inputVector[3]);
			System.out.println("outputVector:" +outputVector[0]+","+outputVector[1]+","+outputVector[2]+","+outputVector[3]);
			System.out.println("desiredOutVector:" +desiredOutVector[0]+","+desiredOutVector[1]+","+desiredOutVector[2]+","+desiredOutVector[3]);
		}
		
		//NN.saveWeights();
		
		if(!BOARD.nothingToDo()){
			if(debuggin) System.out.println("Brick to move:" +brick);
			BOARD.moveBrick(brick);
		}
		else
			if(debuggin) System.out.println("Nothing to do");
	}
	
	public double analyzeBrickSituation(int i) {
		if(BOARD.moveable(i)) {
			int[][] current_board = BOARD.getBoardState();
			int[][] new_board = BOARD.getNewBoardState(i, BOARD.getMyColor(), BOARD.getDice());
			
			if(BOARD.atHome(new_board[BOARD.getMyColor()][i],BOARD.getMyColor())) {
				return 6.0;
			}
			else if(hitOpponentHome(current_board,new_board)) {
				return 5.0;
			}
			else if(BOARD.isStar(new_board[BOARD.getMyColor()][i])) {
				return 4.0;
			}
			else if(moveOut(current_board,new_board)) {
				return 3.0;
			}
			else if(hitMySelfHome(current_board,new_board)) {
				return 1.0;
			}
			else {
				return 2.0;	//Nothing special to do
			}
		}
		else {
			return 0.0;	//Brick cannot move	
		}
	}
		
	protected double Error(double [] outputVec, double [] desOutVec){
		double norm1=0;
		double norm2=0;
		double result;
		for(int mem=0; mem<4; mem++){
			norm1=norm1+Math.pow(outputVec[mem], 2);
			norm2=norm2+Math.pow(desOutVec[mem], 2);
		}
		result=Math.abs(Math.sqrt(norm1)-Math.sqrt(norm2));
		//System.out.println("Error:" +result);
		return result;
	}
	
	protected int BrickToMove(){
		int bricktomove = 0;
		double bestscore = -1;
		//int counterFails = 0;
		for (int num = 0; num < 4; num++){
			if (outputVector[num] >= bestscore){
				//counterFails++;
				bestscore=outputVector[num];
				bricktomove = num;
			}
		}
		return bricktomove;
	}
		
	
	// these methods are like the SemiSmartLUDOPlayers
	private boolean moveOut(int[][] current_board, int[][] new_board){
		for (int i = 0; i < 4; i++){
			if (BOARD.inStartArea(current_board[BOARD.getMyColor()][i], BOARD.getMyColor()) && !BOARD.inStartArea(new_board[BOARD.getMyColor()][i], BOARD.getMyColor())){
				return true;
			}
		}
		return false;
	}

	private boolean hitOpponentHome(int[][] current_board, int[][] new_board){
		for (int i = 0; i < 4; i++){
			for (int j = 0; j < 4; j++){
				if (BOARD.getMyColor() != i){
					if (BOARD.atField(current_board[i][j]) && !BOARD.atField(new_board[i][j])){
						return true;
					}
				}
			}
		}
		return false;
	}

	private boolean hitMySelfHome(int[][] current_board, int[][] new_board){
		for (int i = 0; i < 4; i++){
			if (!BOARD.inStartArea(current_board[BOARD.getMyColor()][i], BOARD.getMyColor()) && BOARD.inStartArea(new_board[BOARD.getMyColor()][i], BOARD.getMyColor())){
				return true;
			}
		}
		return false;
	}
	
}
