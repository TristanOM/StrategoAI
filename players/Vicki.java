/*
 * Implementation of the board game Stratego.
 * Copyright (C) 2011-2012 Vincent Tunru <Vincent.Tunru@phil.uu.nl>,
 *                         Roseline de Boer <R.M.deboer@students.uu.nl>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2
 * as published by the Free Software Foundation.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

/**
 * 
 */
package players;

import game.Game;

/**
 * @author vincent
 *
 */
public class Vicki implements Player {
	protected byte myColor;
	protected int[] myPieces;
	protected int[] hisPieces;
	/**
	 * For each piece, possibleMoves[i][0] is up, [1] is right, [2] is down and [3] is left
	 */
	protected boolean[][] possibleMoves;
	protected boolean[] hasMoved;
	
	protected int lastMoveFrom;
	protected int lastMoveTo;
	/**
	 * How often we've been moving back and forth between lastMoveFrom and lastMoveTo
	 */
	protected int threeTurnRuleTurns;
	/**
	 * The lowest square we've been moving back and forth on
	 */
	protected int threeTurnRuleLow;
	/**
	 * The highest square we've been moving back and forth on
	 */
	protected int threeTurnRuleHigh;
	
	protected int PIECE_PARAM_FIRST_MOVE = 0;
	protected int PIECE_PARAM_STILL_OPPONENT = 1;
	// This value will be multiplied by a larger number when the potential target is stronger
	protected int PIECE_PARAM_BEAT_OPPONENT = 2;
	// How much we want to move to each side
	protected int PIECE_PARAM_BIAS_FORWARD = 3;
	protected int PIECE_PARAM_BIAS_RIGHT = 4;
	protected int PIECE_PARAM_BIAS_BACKWARD = 5;
	protected int PIECE_PARAM_BIAS_LEFT = 6;
	protected int PIECE_PARAM_EXPLORATION_RATE = 7;
	protected int PIECE_PARAM_WATER_CORNER = 8;
	protected int PIECE_PARAM_VALLEY_EDGE = 9;
	// A bit dirty, but this should be the last param as the amount of params will be deduced from it
	protected int PIECE_PARAM_RANDOM_INFLUENCE = 10;
	
	protected double[][] pieceParams;
	
	protected int OPTION_PARAM_MULTIPLIER = 4;
	
	public Vicki(){
		initializeParams();
	}
	
	public void initializeParams(){
		// Possible params:
		//  - amount of moves one can make afterwards
		//  - whether there is an opponent to be hit
		//  - move away from dangerous pieces
		//  - parameter per piece
		//  - incorporate possible negative results of an attack
		//  - parameter to hit every moving piece when all threats are far away (>10 squares)
		
		// TODO investigate the effect of limiting the range of certain parameters
		double[][] pieceParams = new double[Game.PIECE_SPY + 1][];
		for(int i = 0; i < pieceParams.length; i++){
			pieceParams[i] = new double[PIECE_PARAM_RANDOM_INFLUENCE + 1];
			pieceParams[i][PIECE_PARAM_FIRST_MOVE] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_STILL_OPPONENT] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_BEAT_OPPONENT] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_BIAS_FORWARD] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_BIAS_RIGHT] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_BIAS_BACKWARD] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_BIAS_LEFT] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_EXPLORATION_RATE] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_WATER_CORNER] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_VALLEY_EDGE] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			pieceParams[i][PIECE_PARAM_RANDOM_INFLUENCE] = (Math.random() - 0.5) * OPTION_PARAM_MULTIPLIER;
			
		}
		initializeParams(pieceParams);
	}
	
	public void initializeParams(String paramLine){
		double[][] params = new double[Game.PIECE_SPY + 1][];
		int currentParam = 0;
		
		while(paramLine.length() > 0){
			for(int j = 0; j <= Game.PIECE_SPY; j++){
				if(params[j] == null){
					params[j] = new double[PIECE_PARAM_RANDOM_INFLUENCE + 1];
				}
				if(paramLine.indexOf(',') == -1){
					params[j][currentParam] = Double.valueOf(paramLine);
					paramLine = "";
					break;
				}
				params[j][currentParam] = Double.valueOf(paramLine.substring(0, paramLine.indexOf(',')));
				paramLine = paramLine.substring(paramLine.indexOf(',') + 1);
			}
			currentParam++;
		}
		
		initializeParams(params);
	}
	
	public void initializeParams(double[][] pieceParams){
		this.pieceParams = pieceParams;
	}
	
	public double[][] getParams(){
		return pieceParams;
	}
	
	public double evaluateMove(int from, int to){
		
		// TODO Protect last miner
		
		double valuation = 0;
		
		// Add a penalty if your piece has moved (gives away that it is not a bomb)
		if(!hasMoved[from]){
			// TODO Increase this value when there are fewer pieces left that have not moved yet
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_FIRST_MOVE];
		}

		if(hisPieces[to] == Game.PIECE_UNKNOWN && !hasMoved[to]){
			// TODO Incorporate the chance that you can beat this opponent
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_STILL_OPPONENT];
		}
		
		// Add a penalty/bonus if there's a piece at our target we can beat
		if(hisPieces[to] < Game.PIECE_UNKNOWN && myPieces[from] < hisPieces[to]){
			// The parameter weighs in more when the opponent's piece is relatively strong and ours relatively weak
			valuation += (Game.PIECE_FLAG - hisPieces[to] + myPieces[from]) * pieceParams[myPieces[from]][PIECE_PARAM_BEAT_OPPONENT];
		}
		
		// If we can attack an unknown piece that has moved,
		// see if our piece is brave (read: weak) enough to do that
		// TODO Incorporate whether we're a miner
		if(hasMoved[to] && hisPieces[to] == Game.PIECE_UNKNOWN){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_EXPLORATION_RATE];
		}
		
		// If we can move to the opponent's side
		if((myColor == Game.PLAYER_RED && to == (from + 10)) ||
		   (myColor == Game.PLAYER_BLUE && to == (from - 10))){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_BIAS_FORWARD];
		}
		
		// If we can move to the right
		if((myColor == Game.PLAYER_RED && to == (from - 1)) ||
		   (myColor == Game.PLAYER_BLUE && to == (from + 1))){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_BIAS_RIGHT];
		}
		
		// If we can move to our own side
		if((myColor == Game.PLAYER_RED && to == (from - 10)) ||
		   (myColor == Game.PLAYER_BLUE && to == (from + 10))){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_BIAS_BACKWARD];
		}
		
		// If we can move to the left
		if((myColor == Game.PLAYER_RED && to == (from + 1)) ||
		   (myColor == Game.PLAYER_BLUE && to == (from - 1))){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_BIAS_LEFT];
		}
		
		// If we can occupy a square at the corner of a lake at the opponent's side
		if((myColor == Game.PLAYER_RED && (to == 61 || to == 64 || to == 65 || to == 69)) ||
		   (myColor == Game.PLAYER_BLUE && (to == 31 || to == 34 || to == 35 || to == 38))){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_WATER_CORNER];
		}

		// If we are contemplating leaving a square at the corner of a lake at the opponent's side
		if((myColor == Game.PLAYER_RED && (from == 61 || from == 64 || from == 65 || from == 69)) ||
		   (myColor == Game.PLAYER_BLUE && (from == 31 || from == 34 || from == 35 || from == 38))){
			valuation -= pieceParams[myPieces[from]][PIECE_PARAM_WATER_CORNER];
		}
		
		// If we can occupy a square at the outer columns of the rows that contain the lakes
		if(to == 40 || to == 49 || to == 50 || to == 59){
			valuation += pieceParams[myPieces[from]][PIECE_PARAM_VALLEY_EDGE];
		}
		
		// If are contemplating leaving a square at the outer columns of the rows that contain the lakes
		if(from == 40 || from == 49 || from == 50 || from == 59){
			valuation -= pieceParams[myPieces[from]][PIECE_PARAM_VALLEY_EDGE];
		}
		
		// Add the random influence
		valuation += Math.random() * PIECE_PARAM_RANDOM_INFLUENCE;
		
		return valuation;
	}

	/* (non-Javadoc)
	 * @see players.Player#setAIUsername(java.lang.String)
	 */
	@Override
	public void setAIUsername(String username) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see players.Player#setOpponentUsername(java.lang.String)
	 */
	@Override
	public void setOpponentUsername(String username) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see players.Player#setMoveTimeLimit(int)
	 */
	@Override
	public boolean setMoveTimeLimit(int timeLimit) {
		// TODO Auto-generated method stub
		return true;
	}

	/* (non-Javadoc)
	 * @see players.Player#initalizeGame()
	 */
	@Override
	public void initalizeGame() {
		// Initially, we know of no pieces
		myPieces = new int[100];
		hisPieces = new int[100];
		possibleMoves = new boolean[100][4];
		threeTurnRuleTurns = 0;
		threeTurnRuleLow = -1;
		threeTurnRuleHigh = -1;
		for(int i = 0; i < 100; i++){
			myPieces[i] = Game.PIECE_EMPTY;
			
			if(i < 40){
				hisPieces[i] = Game.PIECE_UNKNOWN;
			} else {
				hisPieces[i] = Game.PIECE_EMPTY;
			}
		}
		myPieces[42] = myPieces[43] = myPieces[46] = myPieces[47]
		             = myPieces[52] = myPieces[53] = myPieces[56] = myPieces[57]
		             = hisPieces[42] = hisPieces[43] = hisPieces[46] = hisPieces[47]
		             = hisPieces[52] = hisPieces[53] = hisPieces[56] = hisPieces[57]
		             = Game.PIECE_BLOCKED;
	}

	/* (non-Javadoc)
	 * @see players.Player#createAISetup(int)
	 */
	@Override
	public void createAISetup(int color) {
		// Initialize to avoid null pointer exceptions - will all be reset at the end of this method
		hasMoved = new boolean[100];
		myColor = (byte) color;
		int[] availablePieces = getSetup();
		if(myColor == Game.PLAYER_RED){
			// My pieces are upside-down
			int pieceToSwap;
			for(int i = 0; i < (int) ((availablePieces.length - 1) / 2); i++){
				pieceToSwap = availablePieces[availablePieces.length-1 - i];
				availablePieces[availablePieces.length-1 - i] = availablePieces[i];
				availablePieces[i] = pieceToSwap;
			}
		}
		for(int i = color * 60; i < color * 60 + 40; i++){
			setPiece(i, availablePieces[i - color * 60]);
		}
		
		// No piece has moved yet
		hasMoved = new boolean[100];
	}

	/* (non-Javadoc)
	 * @see players.Player#getAISetupPiece(int, int)
	 */
	@Override
	public int getAISetupPiece(int x, int y) {
		return myPieces[x + y * 10];
	}

	/* (non-Javadoc)
	 * @see players.Player#setOpponentSetupPiece(int, int)
	 */	
	public void setOpponentSetupPiece(int x, int y) {
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see players.Player#startGame()
	 */
	@Override
	public void startGame() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see players.Player#submitOpponentMove(int, int, int, int, int, int)
	 */
	@Override
	public byte submitOpponentMove(int startX, int startY, int endX, int endY,
			int rank, int result) {
		// TODO Auto-generated method stub
		if(result == Game.RESULT_KILL || result == Game.RESULT_DRAW){
			// My piece is killed!
			removePiece(10 * endY + endX);
		}
		
		hisPieces[startX + 10*startY] = Game.PIECE_EMPTY;
		hisPieces[endX + 10*endY] = rank;
		hasMoved[endX + 10*endY] = true;

		return 0;
	}

	/* (non-Javadoc)
	 * @see players.Player#getMove()
	 */
	public int[] getMove() {
		int bestFrom = -1;
		int bestTo = -1;
		double bestValuation = Double.NEGATIVE_INFINITY;
		double valuation;
		// Move the best move
		for(int i = 0; i < possibleMoves.length; i++){
			// Moving up?
			if(possibleMoves[i][0]){
				valuation = evaluateMove(i, i-10);
				if(valuation > bestValuation &&
				   !(threeTurnRuleTurns >= 3 &&
				     threeTurnRuleHigh == i &&
				     threeTurnRuleLow == i-10)){
					bestFrom = i;
					bestTo = i-10;
					bestValuation = valuation;
				}
			}
			
			// Move to the right?
			if(possibleMoves[i][1]){
				valuation = evaluateMove(i, i+1);
				if(valuation > bestValuation &&
				   !(threeTurnRuleTurns >= 3 &&
				     threeTurnRuleHigh == i+1 &&
				     threeTurnRuleLow == i)){
					bestFrom = i;
					bestTo = i+1;
					bestValuation = valuation;
				}
			}
			
			// Moving down?
			if(possibleMoves[i][2]){
				valuation = evaluateMove(i, i+10);
				if(valuation > bestValuation &&
				   !(threeTurnRuleTurns >= 3 &&
				     threeTurnRuleHigh == i+10 &&
				     threeTurnRuleLow == i)){
					bestFrom = i;
					bestTo = i+10;
					bestValuation = valuation;
				}
			}
			
			// Move to the left?
			if(possibleMoves[i][3]){
				valuation = evaluateMove(i, i-1);
				if(valuation > bestValuation &&
				   !(threeTurnRuleTurns >= 3 &&
				     threeTurnRuleHigh == i &&
				     threeTurnRuleLow == i-1)){
					bestFrom = i;
					bestTo = i-1;
					bestValuation = valuation;
				}
			}
		}
		
		if(bestFrom == -1 || bestTo == -1 || bestValuation == Double.NEGATIVE_INFINITY){
			System.out.println(bestFrom);
			System.out.println(bestTo);
			System.out.println(bestValuation);
			return null;
		}
			
		int[] result = new int[4];
		result[0] = bestFrom % 10;
		result[1] = bestFrom / 10;
		result[2] = bestTo % 10;
		result[3] = bestTo / 10;
		lastMoveFrom = bestFrom;
		lastMoveTo = bestTo;
		if(threeTurnRuleHigh == Math.max(bestFrom, bestTo) &&
		   threeTurnRuleLow == Math.min(bestFrom, bestTo)){
			threeTurnRuleTurns++;
		} else {
			threeTurnRuleHigh = Math.max(bestFrom, bestTo);
			threeTurnRuleLow = Math.min(bestFrom, bestTo);
			threeTurnRuleTurns = 1;
		}
		return result;
	}

	/* (non-Javadoc)
	 * @see players.Player#submitAIMoveResult(int, int)
	 */
	@Override
	public byte submitAIMoveResult(int rank, int result) {
		if(result == Game.RESULT_KILL || result == Game.RESULT_NONE){
			// The piece gets to live!
			setPiece(lastMoveTo, myPieces[lastMoveFrom]);
		}
		removePiece(lastMoveFrom);
		return 0;
	}

	/* (non-Javadoc)
	 * @see players.Player#endGame(int, int)
	 */
	@Override
	public void endGame(int result, int reason) {
		// TODO Auto-generated method stub

	}

	/**
	 * This should only be called for placing your own pieces.
	 * 
	 * @param square Square to place the piece on.
	 * @param piece Rank of the piece.
	 */
	protected void setPiece(int square, int piece){
		myPieces[square] = piece;
		hasMoved[square] = true;
		
		possibleMoves[square] = new boolean[4];
		int xPos = square % 10;
		int yPos = square / 10;
		
		// All my squares next to this one have one fewer direction to move in 
		
		// To the right
		if(xPos < 9 && myPieces[square + 1] < Game.PIECE_UNKNOWN){
			possibleMoves[square + 1][3] = false;
			// It's not one of my pieces; now is it available?
		} else if(xPos < 9 && myPieces[square + 1] != Game.PIECE_BLOCKED) {
			possibleMoves[square][1] = true;
		}
		
		// To the left
		if(xPos > 0 && myPieces[square - 1] < Game.PIECE_UNKNOWN){
			possibleMoves[square - 1][1] = false;
			// It's not one of my pieces; now is it available?
		} else if(xPos > 0 && myPieces[square - 1] != Game.PIECE_BLOCKED) {
			possibleMoves[square][3] = true;
		}
		
		// Below
		if(yPos < 9 && myPieces[square + 10] < Game.PIECE_UNKNOWN){
			possibleMoves[square + 10][0] = false;
			// It's not one of my pieces; now is it available?
		} else if(yPos < 9 && myPieces[square + 10] != Game.PIECE_BLOCKED) {
			possibleMoves[square][2] = true;
		}
		
		// Above
		if(yPos > 0 && myPieces[square - 10] < Game.PIECE_UNKNOWN){
			possibleMoves[square - 10][2] = false;
			// It's not one of my pieces; now is it available?
		} else if(yPos > 0 && myPieces[square - 10] != Game.PIECE_BLOCKED) {
			possibleMoves[square][0] = true;
		}
		
		// Should probably not be used for placing flags and boms,
		// but just in case...
		if(piece == Game.PIECE_FLAG || piece == Game.PIECE_BOMB){
			possibleMoves[square] = new boolean[4];
		}
	}
	
	protected void removePiece(int square){
		myPieces[square] = Game.PIECE_EMPTY;
		int xPos = square % 10;
		int yPos = square / 10;

		possibleMoves[square] = new boolean[4];
		// All my squares next to this one have one more direction to move in
		
		// To the right
		if(xPos < 9 && myPieces[square + 1] < Game.PIECE_BOMB){
			possibleMoves[square + 1][3] = true;
		}
		
		// To the left
		if(xPos > 0 && myPieces[square - 1] < Game.PIECE_BOMB){
			possibleMoves[square - 1][1] = true;
		}
		
		// Below
		if(yPos < 9 && myPieces[square + 10] < Game.PIECE_BOMB){
			possibleMoves[square + 10][0] = true;
		}
		
		// Above
		if(yPos > 0 && myPieces[square - 10] < Game.PIECE_BOMB){
			possibleMoves[square - 10][2] = true;
		}
	}
	
	protected int[] getSetup(){
		int[] pieces = { Game.PIECE_BOMB,
		                 Game.PIECE_GENERAL,
		                 Game.PIECE_MARSHAL,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_SERGEANT,
		                 Game.PIECE_LIEUTENANT,
		                 Game.PIECE_SERGEANT,
		                 Game.PIECE_LIEUTENANT,
		                 Game.PIECE_LIEUTENANT,
		                 Game.PIECE_BOMB,
		                 Game.PIECE_COLONEL,
		                 Game.PIECE_SPY,
		                 Game.PIECE_BOMB,
		                 Game.PIECE_CAPTAIN,
		                 Game.PIECE_SERGEANT,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_BOMB,
		                 Game.PIECE_LIEUTENANT,
		                 Game.PIECE_CAPTAIN,
		                 Game.PIECE_BOMB,
		                 Game.PIECE_MAJOR,
		                 Game.PIECE_MINER,
		                 Game.PIECE_MINER,
		                 Game.PIECE_COLONEL,
		                 Game.PIECE_MAJOR,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_MAJOR,
		                 Game.PIECE_SERGEANT,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_FLAG,
		                 Game.PIECE_BOMB,
		                 Game.PIECE_MINER,
		                 Game.PIECE_MINER,
		                 Game.PIECE_MINER,
		                 Game.PIECE_CAPTAIN,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_SCOUT,
		                 Game.PIECE_CAPTAIN,
		                 Game.PIECE_SCOUT};
		
		return pieces;
	}
}