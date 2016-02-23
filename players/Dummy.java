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

import java.util.Random;

import game.Game;

/**
 * @author vincent
 *
 */
public class Dummy implements Player {
	protected String myUsername;
	protected String opponentUsername;
	protected int timeLimit;
	protected byte myColor;
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
	
	/**
	 * myPieces[0] is the square in the top left-hand corner,
	 * myPieces[99] is the square in the bottom right-hand corner. 
	 */
	protected int[] myPieces;
	/**
	 * How many moves pieces on a square can do
	 */
	protected int[] possibleMoves;

	/* (non-Javadoc)
	 * @see players.Player#setAIUsername(java.lang.String)
	 */
	@Override
	public void setAIUsername(String username) {
		myUsername = username;
	}

	/* (non-Javadoc)
	 * @see players.Player#setOpponentUsername(java.lang.String)
	 */
	@Override
	public void setOpponentUsername(String username) {
		opponentUsername = username;

	}

	/* (non-Javadoc)
	 * @see players.Player#setMoveTimeLimit(int)
	 */
	@Override
	public boolean setMoveTimeLimit(int timeLimit) {
		this.timeLimit = timeLimit;
		return true;
	}

	/* (non-Javadoc)
	 * @see players.Player#initalizeGame()
	 */
	@Override
	public void initalizeGame() {
		// Initially, we know of no pieces
		myPieces = new int[100];
		possibleMoves = new int[100];
		threeTurnRuleTurns = 0;
		threeTurnRuleLow = -1;
		threeTurnRuleHigh = -1;
		for(int i = 0; i < 100; i++){
			myPieces[i] = Game.PIECE_EMPTY;
		}
		myPieces[42] = myPieces[43] = myPieces[46] = myPieces[47]
		             = myPieces[52] = myPieces[53] = myPieces[56] = myPieces[57]
		             = Game.PIECE_BLOCKED;
	}
	
	/* (non-Javadoc)
	 * @see players.Player#createAISetup(int)
	 */
	@Override
	public void createAISetup(int color) {
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
		return 0;
	}

	/* (non-Javadoc)
	 * @see players.Player#getMove()
	 */
	@Override
	public int[] getMove() {
		int newPosition;
		int xPos;
		int yPos;
		// TODO incorporate Scout
		// Move a random movable piece
		int[] moveCandidatesFrom = new int[possibleMoves.length * 4];
		int[] moveCandidatesTo = new int[possibleMoves.length * 4];
		int amountOfCandidates = 0;
		for(int i = 0; i < moveCandidatesFrom.length; i++){
			moveCandidatesFrom[i] = moveCandidatesTo[i] = -1;
		}
		for(int i = 0; i < possibleMoves.length; i++){
			if(possibleMoves[i] <= 0){
				continue;
			}
			newPosition = -1;
			xPos = i % 10;
			yPos = i / 10;
			
			// Try moving lower
			if(yPos < 9 && myPieces[i+10] == Game.PIECE_EMPTY){
				newPosition = i+10;
				// Only consider this move if it does not break the three move-rule
				if(threeTurnRuleTurns < 3
				   || !(threeTurnRuleHigh == Math.max(i, newPosition)
			            && threeTurnRuleLow == Math.min(i, newPosition))){
					moveCandidatesFrom[amountOfCandidates] = i;
					moveCandidatesTo[amountOfCandidates] = newPosition;
					amountOfCandidates++;
				}
			}
			
			// Try moving higher
			if(yPos > 0 && myPieces[i-10] == Game.PIECE_EMPTY) {
				newPosition = i - 10;
				// Only consider this move if it does not break the three move-rule
				if(threeTurnRuleTurns < 3
				   || !(threeTurnRuleHigh == Math.max(i, newPosition)
			            && threeTurnRuleLow == Math.min(i, newPosition))){
					moveCandidatesFrom[amountOfCandidates] = i;
					moveCandidatesTo[amountOfCandidates] = newPosition;
					amountOfCandidates++;
				}
			}
			
			// Try moving to the left
			if(xPos > 0 && myPieces[i-1] == Game.PIECE_EMPTY) {
				newPosition = i - 1;
				// Only consider this move if it does not break the three move-rule
				if(threeTurnRuleTurns < 3
				   || !(threeTurnRuleHigh == Math.max(i, newPosition)
			            && threeTurnRuleLow == Math.min(i, newPosition))){
					moveCandidatesFrom[amountOfCandidates] = i;
					moveCandidatesTo[amountOfCandidates] = newPosition;
					amountOfCandidates++;
				}
			} 

			// Try moving to the right
			if(xPos < 9 && myPieces[i+1] == Game.PIECE_EMPTY)  {
				newPosition = i + 1;
				// Only consider this move if it does not break the three move-rule
				if(threeTurnRuleTurns < 3
				   || !(threeTurnRuleHigh == Math.max(i, newPosition)
				        && threeTurnRuleLow == Math.min(i, newPosition))){
					moveCandidatesFrom[amountOfCandidates] = i;
					moveCandidatesTo[amountOfCandidates] = newPosition;
					amountOfCandidates++;
				}
			}
		}

		if(amountOfCandidates <= 0){
			// TODO: throw exception
			if(myColor == Game.PLAYER_BLUE){
				System.out.print("BLUE: ");
			} else {
				System.out.print("RED: ");
			}
			System.out.println("I could not find a move!");
			return null;
		}
		
		Random r = new Random();
		int move = r.nextInt(amountOfCandidates);

		int[] result = new int[4];
		result[0] = moveCandidatesFrom[move] % 10;
		result[1] = moveCandidatesFrom[move] / 10;
		result[2] = moveCandidatesTo[move] % 10;
		result[3] = moveCandidatesTo[move] / 10;
		lastMoveFrom = moveCandidatesFrom[move];
		lastMoveTo = moveCandidatesTo[move];
		
		if(threeTurnRuleHigh == Math.max(lastMoveFrom, lastMoveTo) &&
		   threeTurnRuleLow == Math.min(lastMoveFrom, lastMoveTo)){
			threeTurnRuleTurns++;
		} else {
			threeTurnRuleHigh = Math.max(lastMoveFrom, lastMoveTo);
			threeTurnRuleLow = Math.min(lastMoveFrom, lastMoveTo);
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

	protected void setPiece(int square, int piece){
		myPieces[square] = piece;
		
		possibleMoves[square] = 0;
		int xPos = square % 10;
		int yPos = square / 10;
		
		// All my squares next to this one have one fewer direction to move in 
		if(xPos < 9 && myPieces[square + 1] < Game.PIECE_UNKNOWN){
			possibleMoves[square + 1]--;
			// It's not one of my pieces; now is it available?
		} else if(xPos < 9 && myPieces[square + 1] != Game.PIECE_BLOCKED) {
			possibleMoves[square]++;
		}
		
		if(xPos > 0 && myPieces[square - 1] < Game.PIECE_UNKNOWN){
			possibleMoves[square - 1]--;
			// It's not one of my pieces; now is it available?
		} else if(xPos > 0 && myPieces[square - 1] != Game.PIECE_BLOCKED) {
			possibleMoves[square]++;
		}
		
		if(yPos < 9 && myPieces[square + 10] < Game.PIECE_UNKNOWN){
			possibleMoves[square + 10]--;
			// It's not one of my pieces; now is it available?
		} else if(yPos < 9 && myPieces[square + 10] != Game.PIECE_BLOCKED) {
			possibleMoves[square]++;
		}
		
		if(yPos > 0 && myPieces[square - 10] < Game.PIECE_UNKNOWN){
			possibleMoves[square - 10]--;
			// It's not one of my pieces; now is it available?
		} else if(yPos > 0 && myPieces[square - 10] != Game.PIECE_BLOCKED) {
			possibleMoves[square]++;
		}
		
		// Should probably not be used for placing flags and boms,
		// but just in case...
		if(piece == Game.PIECE_FLAG || piece == Game.PIECE_BOMB){
			possibleMoves[square] = 0;
		}
	}
	
	protected void removePiece(int square){
		myPieces[square] = Game.PIECE_EMPTY;
		int xPos = square % 10;
		int yPos = square / 10;

		possibleMoves[square] = 0;
		// All my squares next to this one have one more direction to move in 
		if(xPos < 9 && myPieces[square + 1] < Game.PIECE_BOMB){
			possibleMoves[square + 1]++;
		}
		
		if(xPos > 0 && myPieces[square - 1] < Game.PIECE_BOMB){
			possibleMoves[square - 1]++;
		}
		
		if(yPos < 9 && myPieces[square + 10] < Game.PIECE_BOMB){
			possibleMoves[square + 10]++;
		}
		
		if(yPos > 0 && myPieces[square - 10] < Game.PIECE_BOMB){
			possibleMoves[square - 10]++;
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