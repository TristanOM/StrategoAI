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
 * Bob3 is a player made by Tristan Oliver-Mallory for Special Topics with Dr Mckinstry
 */
package players;

import game.Game;

/**
 * @author vincent
 *
 */
public class Bob3 implements Player {
	protected byte myColor;
	protected int[] myPieces;
	protected int[] hisPieces;
   
   protected int[] unknownPieces;//tracks possible unknown pieces
   
   //protected boolean[] hasMovedOpponent;//check opponent hasMoved places

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
		
	public Bob3(){
		//initializeParams();
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
   
   unknownPieces = new int[12];//this set up the count of unknown enemy pieces
   setUnKnown();   
   
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
      
      if(result == Game.RESULT_DRAW){
         makeKnown(rank);//should set the dead enemy rank
      }
		
		hisPieces[startX + 10*startY] = Game.PIECE_EMPTY;
		hisPieces[endX + 10*endY] = rank;
		hasMoved[endX + 10*endY] = true;
      
     // makeKnown(rank);//my method to track known opponent pieces

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
      //if(result == Game.RESULT_KILL){
         //makeKnown(rank);//my method to track known opponent pieces
      //}
      else if(result == Game.RESULT_DRAW){
         makeKnown(rank);//
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
		} 
      else if(xPos < 9 && myPieces[square + 1] != Game.PIECE_BLOCKED) {
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
		
		// Should probably not be used for placing flags and bombs,
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
   
   //need way to check ajacent squares as well even if empty
   //Problem when trying to find out the rank of an enemy after an attack
   
   
  
   //rank of your piece compared with rank of unknown opponent
   
   
  	//Function made by Tristan Oliver-Mallory
   // This is used to estimate all unknown enemy pieces.
   //It tracks which pieces have not yet been revealed.
   protected void makeKnown(int Rank){
	   unknownPieces[Rank] = unknownPieces[Rank] -1; 
   }
   
   protected void setUnKnown(){
   
	   unknownPieces[0] = 1;
      unknownPieces[1] = 1;
      unknownPieces[2] = 2;
      unknownPieces[3] = 3;
      unknownPieces[4] = 4;
      unknownPieces[5] = 4;
      unknownPieces[6] = 4;
      unknownPieces[7] = 5;
      unknownPieces[8] = 8;
      unknownPieces[9] = 1;
      unknownPieces[10] = 6;
      unknownPieces[11] = 1; 
 
   }

//Function made by Tristan Oliver-Mallory
//This function atAttack() returns a value -1 to 1 that indicates a pieces chances of surviving an attack. 
//If the piece dies its a -1 if it live its a 1. 
//If the enemy piece is unknown it will estimate the chances of winning based on all unknown enemy pieces

   protected double atAttack(int yourRank, int attackPos){
   	double count = 0;
      int divider = 0;
      
      if (hisPieces[attackPos]== Game.PIECE_EMPTY){
         count = 0;
      }
      else if(hisPieces[attackPos] != Game.PIECE_UNKNOWN){
         if(yourRank == Game.PIECE_MINER && hisPieces[attackPos] == Game.PIECE_BOMB){
            count = 1;
         }
         else if(yourRank == Game.PIECE_SPY && hisPieces[attackPos] != Game.PIECE_MARSHAL ){
            count = 1;
         }
         else if(yourRank < hisPieces[attackPos]){
      		count = 1;
      	}
      	else if(yourRank > hisPieces[attackPos]){
      		count = -1;
      	}
      
      }
      
     else{ 
         //if(hasMoved[attackPos]);
      	
      	for(int i =0; i < 10; i++){
      			if(yourRank < i){
      				count = count + (unknownPieces[i]);
                  divider = divider + (unknownPieces[i]);
      			}
      			//if tie add nothing
      			else if(yourRank > i){
                  count = count - unknownPieces[i];
                  divider = divider + (unknownPieces[i]);
               }
               else
                  divider = divider + (unknownPieces[i]);
               			
      	}
      	
      	if(yourRank == Game.PIECE_MINER && hasMoved[attackPos] == false ){//don't count bomb if piece is a miner
      		count = count + (unknownPieces[Game.PIECE_BOMB]);
            divider = divider + (unknownPieces[Game.PIECE_BOMB]);
      	}
         else{
            count = count - (unknownPieces[Game.PIECE_BOMB]);
             divider = divider + (unknownPieces[Game.PIECE_BOMB]);
         }
         
      	if(yourRank == Game.PIECE_SPY){
      		count = count + (unknownPieces[0]);
            count = count + (unknownPieces[0]);//spies can defeat marshals 
             //divider = divider + (unknownPieces[0]);
      	}
         if(!hasMoved[attackPos]){
         count = count +unknownPieces[Game.PIECE_FLAG];//take flag into account as well
         divider = divider + (unknownPieces[Game.PIECE_FLAG]);
         }
         if(count != 0){
         count = count/divider ;
         }

     }
      
      //System.out.println("Attack" + count);      	 

   	return count;
   	
   }
   
         
      //Function made by Tristan Oliver-Mallory
   //This function, evaluateMove() returns a value -1 to 1 that indicates a pieces chances of surviving at attacking a position. 
//If the piece dies its a -1 if it lives its a 1. 
//This evaluation is much simpler than Bob because it only take the attack move into account
      protected double evaluateMove(int from, int to){
        double attack = 0;
                
       attack = atAttack( myPieces[from], to);//first the attack val                

      return attack;
      
      }                   
       

}