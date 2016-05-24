package LUDOSimulator;
import java.util.Arrays;
import java.util.Random;
/**
 * Example of automatic LUDO player
 * @author David Johan Christensen
 * 
 * @version 0.9
 *
 */
public class Qplayer implements LUDOPlayer {
	
	LUDOBoard board;
	Random rand;
	int[] state;
	int[] new_state;
	int[] action;
	int[] new_action;
	public Qplayer(LUDOBoard board)
	{
		this.board = board;
		rand = new Random();
		state= new int[4];
		new_state= new int[4];
		action= new int[4];
		new_action= new int[4];
		/*for(int i=0; i<7; i++){
			for(int j=0; j<9; j++){
				board.Qtable[i][j]=0;
			}
		}*/
	}
	
	public void play() {
		board.print("My Player playing");
		//System.out.println("My Player playing");
		int[] myBricksValue = new int[4];  
		
		board.rollDice();
		double max =-1;
		int bestIndex = -1;
		//printTable();
		//System.out.println(" ");
		for(int i=0;i<4;i++)
		{
			double value = analyzeBrickSituation(i); 
			if(value>max) {
				bestIndex = i;
				max = value;
			}
		}
		
		/*if(rand.nextFloat()>0.9 && max!=-1){
			//System.out.println("Random");
			bestIndex=rand.nextInt(4);
			while(!board.moveable(bestIndex)){
				bestIndex=rand.nextInt(4);
			}
		}*/
		//System.out.println(bestIndex);
		//System.out.println("------");
		
		if(bestIndex!=-1 && state[bestIndex]>=0 && action[bestIndex]>=0){
			//System.out.println(new_state[bestIndex] + " " + action[bestIndex] + " " + reward(new_state[bestIndex], new_action[bestIndex], state[bestIndex]));
			//System.out.println(reward(new_state[bestIndex], new_action[bestIndex], state[bestIndex]));
			
			/*double dQ=0.9*(reward(state[bestIndex], action[bestIndex], new_state[bestIndex])+0.05*board.Qtable[new_state[bestIndex]][new_action[bestIndex]]-board.Qtable[state[bestIndex]][action[bestIndex]]);
			board.Qtable[state[bestIndex]][action[bestIndex]]+=dQ;*/
			
			board.moveBrick(bestIndex);
		}
		else{
			board.nothingToDo();
		}
		//printTable();
	}
	public double analyzeBrickSituation(int i) {
		//System.out.print(board.moveable(i) + " ");
		if(board.moveable(i)) {
			int[][] current_board = board.getBoardState();
			int[][] new_board = board.getNewBoardState(i, board.getMyColor(), board.getDice());
		
			state[i]=checkState(current_board, i);
			action[i]=checkAction(current_board, new_board);
			new_state[i]=checkState(new_board, i);
			//System.out.println(state[i] + " " + action[i] + " " + new_state[i] + " " + reward(state[i], action[i], new_state[i]));
			double max=0;
			for(int j=0; j<9; j++){
				if(board.Qtable[new_state[i]][j]>max){
					max=board.Qtable[new_state[i]][j];
					new_action[i]=j;
				}
			}
			
			return board.Qtable[state[i]][action[i]];
		}
		return -1;
	}
	
	private void printTable(){
		for(int i=0; i<7; i++){
			for(int j=0; j<9; j++){
				System.out.print(board.Qtable[i][j] + " ");
			}
			System.out.print("\n");
		}
	}
	
	private int reward(int state, int action, int new_state){
		if(state==0 && action==0 && new_state==3){
			return 9;
		}
		if(action==1 && new_state==0){
			return -10;
		}
		if(action==2){
			return 7;
		}
		if(action==3 && new_state==2){
			return 5;
		}
		if(action==4 && new_state==3){
			return 4;
		}
		if(state!=4 && action==5 && new_state==4){
			return 6;
		}
		if(state==4 && action==6 && new_state==5){
			return 2;
		}
		if(action==6 && new_state==5){
			return 8;
		}
		if(action==7 && new_state==5){
			return 10;
		}
		if(action!=7 && action!=6 && action!= 5 && new_state==6){
			return 3;
		}
		return 0;
	}
	
	private int checkAction(int[][] current_board, int[][] new_board){
		/*if(hitBlock(current_board, new_board)){
			return 8;
		}*/
		if(moveOut(current_board, new_board)){
			return 0;
		}
		if(hitMySelfHome(current_board, new_board)){
			return 1;
		}
		if(hitOpponentHome(current_board, new_board)){
			return 2;
		}
		if(hitStar(current_board, new_board)){
			return 3;
		}
		if(hitGlobe(current_board, new_board)){
			return 4;
		}
		if(hitAlmostHome(current_board, new_board)){
			return 5;
		}
		if(win(new_board)){
			return 7;
		}
		if(hitHome(current_board, new_board)){
			return 6;
		}
		
		return 8;
	}
	
	private int checkState(int[][] current_board, int i){
		if(board.inStartArea(current_board[board.getMyColor()][i], board.getMyColor())){
			return 0;
		}
		if(blocking(current_board, i)){
			return 6;
		}
		if(board.isStar(current_board[board.getMyColor()][i])){
			return 2;
		}
		if(board.isGlobe(current_board[board.getMyColor()][i])){
			return 3;
		}
		if(board.almostHome(current_board[board.getMyColor()][i], board.getMyColor())){
			return 4;
		}
		if(board.atHome(current_board[board.getMyColor()][i], board.getMyColor())){
			return 5;
		}
		return 1;
	}
	
	
	private boolean win(int[][] new_board){
		for(int i=0; i<4; i++){
			if(!board.atHome(new_board[board.getMyColor()][i], board.getMyColor())){
				return false;
			}
		}
		return true;
	}
	
	private boolean moveOut(int[][] current_board, int[][] new_board) {
		for(int i=0;i<4;i++) {
			if(board.inStartArea(current_board[board.getMyColor()][i],board.getMyColor())&&!board.inStartArea(new_board[board.getMyColor()][i],board.getMyColor())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hitMySelfHome(int[][] current_board, int[][] new_board) {
		for(int i=0;i<4;i++) {
			if(!board.inStartArea(current_board[board.getMyColor()][i],board.getMyColor())&&board.inStartArea(new_board[board.getMyColor()][i],board.getMyColor())) {
				return true;
			}
		}
		return false;
	}
	
	private boolean hitOpponentHome(int[][] current_board, int[][] new_board) {
		for(int i=0;i<4;i++) {
			for(int j=0;j<4;j++) {
				if(board.getMyColor()!=i) {
					if(board.atField(current_board[i][j])&&!board.atField(new_board[i][j])) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private boolean hitStar(int[][] current_board, int[][] new_board){
		for(int i=0; i<4; i++){
			if(!board.isStar(current_board[board.getMyColor()][i])&&board.isStar(new_board[board.getMyColor()][i])){
				return true;
			}
		}
		return false;
	}
	
	private boolean hitGlobe(int[][] current_board, int[][] new_board){
		for(int i=0; i<4; i++){
			if(!board.isGlobe(current_board[board.getMyColor()][i])&&board.isGlobe(new_board[board.getMyColor()][i])){
				return true;
			}
		}
		return false;
	}
	
	private boolean hitAlmostHome(int[][] current_board, int[][] new_board){
		for(int i=0; i<4; i++){
			if(!board.almostHome(current_board[board.getMyColor()][i], board.getMyColor())&&board.almostHome(new_board[board.getMyColor()][i], board.getMyColor())){
				return true;
			}
		}
		return false;
	}
	
	private boolean hitHome(int[][] current_board, int[][] new_board){
		for(int i=0; i<4; i++){
			if(!board.atHome(current_board[board.getMyColor()][i], board.getMyColor())&&board.atHome(new_board[board.getMyColor()][i], board.getMyColor())){
				return true;
			}
		}
		return false;
	}
	
	private boolean hitBlock(int[][] current_board, int[][] new_board){
		boolean prev=true;
		for(int i=0; i<4; i++){
			for(int j=0; j<4; j++){
				if(i!=j){
					if(current_board[board.getMyColor()][i]==current_board[board.getMyColor()][j]){
						prev=true;
						break;
					}
					else{
						prev=false;
					}
				}
			}
		}
		if(prev==false){
			for(int i=0; i<4; i++){
				for(int j=0; j<4; j++){
					if(i!=j){
						if(new_board[board.getMyColor()][i]==new_board[board.getMyColor()][j]){
							return true;
						}
					}
				}
			}
		}
		return false;
	}
	
	private boolean blocking(int[][] current_board, int i){
		for(int j=0; j<4; j++){
			if(i!=j){
				if(current_board[board.getMyColor()][i]==current_board[board.getMyColor()][j]){
					return true;
				}
			}
		}
		return false;
	}
}


