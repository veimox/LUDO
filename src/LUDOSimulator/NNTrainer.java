package LUDOSimulator;

public class NNTrainer implements LUDOPlayer
{
	
	LUDOBoard BOARD;

	static int inputsnr = 4;  // amount of input nodes
	static int outputsnr = 4;  // amount of input nodes
	static double outputVector[] = new double[outputsnr];
	static double inputVector[] = new double[inputsnr];

	static double inputs[] = new double[inputsnr];
	
	public NNTrainer(LUDOBoard board)
	{
		BOARD = board;
		outputVector[0]=-1;
		outputVector[1]=-1;
		outputVector[2]=-1;
		outputVector[3]=-1;
	}

	public double[] training()
	{
		//BOARD.rollDice();
		
		//Get input vector from board analysis
		for(int i=0;i<4;i++){
			inputVector[i] = analyzeBrickSituation(i); 	
		}
		
		//If there are two or more optimal possibilities (two bricks can hit an opponent home, ex), only 
		//one is taken, and the rest are neglected
		double max =-1;
		int bestIndex = -1;
		for(int i=0;i<4;i++){
			if(inputVector[i]>=max && inputVector[i]>0) {
				bestIndex = i;
				max = inputVector[i];
			}
		}
		if(bestIndex!=-1) outputVector[bestIndex]=1;
		
		return outputVector;
	}
	
	public void play()
	{
		BOARD.rollDice();
			
		//Get input vector from board analysis
		for(int i=0;i<4;i++){
			inputVector[i] = analyzeBrickSituation(i); 	
		}
		
		double max =-1;
		int bestIndex = -1;
		for(int i=0;i<4;i++){
			if(inputVector[i]>max&&inputVector[i]>0) {
				bestIndex = i;
				max = inputVector[i];
			}
		}
		if(bestIndex!=-1){
			outputVector[bestIndex]=1;
			BOARD.moveBrick(bestIndex);
		}
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
