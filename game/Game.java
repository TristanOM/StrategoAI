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
package game;

import players.Player;

/**
 * @author vincent
 *
 */
public class Game {
	public static int PIECE_MARSHAL = 0;
	public static int PIECE_GENERAL = 1;
	public static int PIECE_COLONEL = 2;
	public static int PIECE_MAJOR = 3;
	public static int PIECE_CAPTAIN = 4;
	public static int PIECE_LIEUTENANT = 5;
	public static int PIECE_SERGEANT = 6;
	public static int PIECE_MINER = 7;
	public static int PIECE_SCOUT = 8;
	public static int PIECE_SPY = 9;
	public static int PIECE_BOMB = 10;
	public static int PIECE_FLAG = 11;
	public static int PIECE_UNKNOWN = 12;
	/**
	 * Square without a piece
	 */
	public static int PIECE_EMPTY = 13;
	/**
	 * Squares that cannot be entered, such as the lake
	 */
	public static int PIECE_BLOCKED = 14;
	
	public static byte PLAYER_RED = 0;
	public static byte PLAYER_BLUE = 1;
	
	public static int TURN_REJECTDRAW = -5;
	public static int TURN_REQUESTDRAW = -1;
	
	public static int RESULT_NONE = 0;
	/**
	 * The attacker managed to kill the defender
	 */
	public static int RESULT_KILL = 1;
	/**
	 * The attacker was killed
	 */
	public static int RESULT_KILLED = 2;
	/**
	 * Both attacker and defender were killed
	 */
	public static int RESULT_DRAW = 3;
	
	public static int END_RESULT_LOSS = 0;
	public static int END_RESULT_WIN = 1;
	public static int END_RESULT_DRAW = 2;
	
	/**
	 * The game ended because a flag was captured
	 */
	public static int END_REASON_CAPTURED = 0;
	/**
	 * The game ended because both players couldn't move
	 */
	public static int END_REASON_STUCK = 3;
	
	protected Player redPlayer;
	protected Player bluePlayer;
	
	protected boolean initialized = false;
	
	protected int[] squareContents;
	/**
	 * 0 for red player, 1 for blue. If the square has no owner, that should be represented in squareContents
	 */
	protected byte[] squareOwner;
	/**
	 * Amount of moves the piece on this square has - if all are 0, it's a draw
	 */
	protected int[] openDirections;
	
	protected boolean gameEnded;
	
	protected History history;
	protected int winner;
	
	// Options
	protected boolean verbose = true;
	
	protected boolean OPTION_TWO_SQUARE_RULE = true;
	
	/**
	 * Convenience method to get a list of the pieces each player gets
	 * 
	 * @return A list of available pieces
	 */
	public static int[] getAvailablePieces() {
		int[] availablePieces = new int[40];
		availablePieces[0] = Game.PIECE_MARSHAL;
		availablePieces[1] = Game.PIECE_GENERAL;
		availablePieces[2] = Game.PIECE_COLONEL;
		availablePieces[3] = Game.PIECE_COLONEL;
		availablePieces[4] = Game.PIECE_MAJOR;
		availablePieces[5] = Game.PIECE_MAJOR;
		availablePieces[6] = Game.PIECE_MAJOR;
		availablePieces[7] = Game.PIECE_CAPTAIN;
		availablePieces[8] = Game.PIECE_CAPTAIN;
		availablePieces[9] = Game.PIECE_CAPTAIN;
		availablePieces[10] = Game.PIECE_CAPTAIN;
		availablePieces[11] = Game.PIECE_LIEUTENANT;
		availablePieces[12] = Game.PIECE_LIEUTENANT;
		availablePieces[13] = Game.PIECE_LIEUTENANT;
		availablePieces[14] = Game.PIECE_LIEUTENANT;
		availablePieces[15] = Game.PIECE_SERGEANT;
		availablePieces[16] = Game.PIECE_SERGEANT;
		availablePieces[17] = Game.PIECE_SERGEANT;
		availablePieces[18] = Game.PIECE_SERGEANT;
		availablePieces[19] = Game.PIECE_MINER;
		availablePieces[20] = Game.PIECE_MINER;
		availablePieces[21] = Game.PIECE_MINER;
		availablePieces[22] = Game.PIECE_MINER;
		availablePieces[23] = Game.PIECE_MINER;
		availablePieces[24] = Game.PIECE_SCOUT;
		availablePieces[25] = Game.PIECE_SCOUT;
		availablePieces[26] = Game.PIECE_SCOUT;
		availablePieces[27] = Game.PIECE_SCOUT;
		availablePieces[28] = Game.PIECE_SCOUT;
		availablePieces[29] = Game.PIECE_SCOUT;
		availablePieces[30] = Game.PIECE_SCOUT;
		availablePieces[31] = Game.PIECE_SCOUT;
		availablePieces[32] = Game.PIECE_SPY;
		availablePieces[33] = Game.PIECE_BOMB;
		availablePieces[34] = Game.PIECE_BOMB;
		availablePieces[35] = Game.PIECE_BOMB;
		availablePieces[36] = Game.PIECE_BOMB;
		availablePieces[37] = Game.PIECE_BOMB;
		availablePieces[38] = Game.PIECE_BOMB;
		availablePieces[39] = Game.PIECE_FLAG;
		return availablePieces;
	}
	
	public Game(Player player1, Player player2) {
		player1.setAIUsername("Player 1");
		player1.setOpponentUsername("Player 2");
		player2.setAIUsername("Player 2");
		player2.setOpponentUsername("Player 1");
		
		redPlayer = player1;
		bluePlayer = player2;
	}
	
	public void setRedPlayer(Player redPlayer){
		redPlayer.setAIUsername("Player 1");
		redPlayer.setOpponentUsername("Player 2");
		this.redPlayer = redPlayer;
	}
	
	public void setBluePlayer(Player bluePlayer){
		bluePlayer.setAIUsername("Player 2");
		bluePlayer.setOpponentUsername("Player 1");
		this.bluePlayer = bluePlayer;
	}

	public void initialize() {
		redPlayer.initalizeGame();
		bluePlayer.initalizeGame();
		
		redPlayer.createAISetup(PLAYER_RED);
		bluePlayer.createAISetup(PLAYER_BLUE);
		
		gameEnded = false;
		
		initializePieces();
		
		history = new History();
		history.outputLog(verbose);
		if(verbose){
			System.out.println("Game initialized. Board setup: ");
			System.out.println(boardToString());
		}
		
		winner = -1;
		
		initialized = true;
	}

	public void play() {
		if(!initialized){
			// TODO Throw exception
			System.out.println("Error: Trying to play a game with an uninitialized board");
			return;
		}
		
		redPlayer.startGame();
		bluePlayer.startGame();
		
		byte nextPlayer = PLAYER_RED;
		int[] move;
		boolean validMove;
		boolean canMove;
		
		int initialSquare = -1;
		int targetSquare = -1;
		Move oneTurnBack;
		Move twoTurnsBack;
		Move threeTurnsBack;
		int threeMoveRuleHigh = -1;
		int threeMoveRuleLow = -1;
		// Used to check whether a particular square (i) is the high or low part
		int notI;
		playGame: while(!hasEnded()){
			canMove = false;
			
			threeMoveRuleHigh = threeMoveRuleLow = -1;
			if(history.size() >= 7){
				oneTurnBack = history.peek(history.size() - 2);
				twoTurnsBack = history.peek(history.size() - 4);
				threeTurnsBack = history.peek(history.size() - 6);
				
				if(oneTurnBack.getFromX() == threeTurnsBack.getFromX() &&
				   oneTurnBack.getFromY() == threeTurnsBack.getFromY() &&
				   oneTurnBack.getToX() == threeTurnsBack.getToX() &&
				   oneTurnBack.getToY() == threeTurnsBack.getToY() &&
				   twoTurnsBack.getFromX() == threeTurnsBack.getToX() &&
				   twoTurnsBack.getFromY() == threeTurnsBack.getToY() &&
				   twoTurnsBack.getToX() == threeTurnsBack.getFromX() &&
				   twoTurnsBack.getToY() == threeTurnsBack.getFromY()){
					
					threeMoveRuleHigh = Math.max(oneTurnBack.getFromX() + 10 * oneTurnBack.getFromY(),
					                             oneTurnBack.getToX() + 10 * oneTurnBack.getToY());
					threeMoveRuleLow = Math.min(oneTurnBack.getFromX() + 10 * oneTurnBack.getFromY(),
					                            oneTurnBack.getToX() + 10 * oneTurnBack.getToY());
				}
			}
			
			for(int i = 0; i < openDirections.length; i++){
				if(openDirections[i] > 0 &&
				   squareOwner[i] == nextPlayer){
					// If there is only one way to move from here...
					if(openDirections[i] == 1){
						// ...and that move is back to the square we already went to thrice
						notI = -1;
						
						if(i == threeMoveRuleHigh){
							notI = threeMoveRuleLow;
						} else if(i == threeMoveRuleLow) {
							notI = threeMoveRuleHigh;
						}
						
						// ...(i.e. the square we're currently looking at is one of the two we've been moving
						//     back and forth on, and the other one is currently free, or in other words, the
						//     one direction we can move in)....
						if(notI != -1
						   && squareContents[notI] != PIECE_BLOCKED
						   && !(squareContents[notI] < PIECE_UNKNOWN && squareOwner[notI] == nextPlayer)){
							// Then this is not a valid move
							continue;
						}
					}
					
					// If we can move in more than one direction, or the one direction we can move in
					// has not been moved to and fro three times in a row yet, we can move!
					canMove = true;
					
					break;
				}
			}
			
			if(!canMove){
				byte winner;
				if(nextPlayer == PLAYER_RED){
					winner = PLAYER_BLUE;
				} else {
					winner = PLAYER_RED;
				}
				history.add(new Move(winner, END_REASON_STUCK));
				endGame(winner, END_REASON_STUCK);
				if(verbose){
					System.out.println(boardToString());
				}
				continue;
			}
			
			do{
				validMove = false;
				if(nextPlayer == PLAYER_RED){
					move = redPlayer.getMove();
				} else {
					move = bluePlayer.getMove();
				}
				
				if(move == null){
					// TODO handle gracefully
					System.out.println("Error: player did not return a move, you lose!");
					endGame(nextPlayer % 1, END_REASON_STUCK);
					
					break playGame;
				}
				
				// TODO Check whether the player wants to draw or resign
				
				initialSquare = move[0] + move[1] * 10;
				targetSquare = move[2] + move[3] * 10;
				
				// You can only move in one direction
				if(move[0] != move[2] && move[1] != move[3]){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move diagonally.");
					continue;
				}
				
				// Most pieces can only move one step
				if(Math.abs(move[0] - move[2] + move[1] - move[3]) != 1 &&
				   squareContents[initialSquare] != PIECE_SCOUT){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move by more than one square at a time");
					continue;
				}
				
				if(squareContents[initialSquare] == PIECE_SCOUT){
					// If the scout moves horizontally, check if the road is clear
					if(move[0] != move[2]){
						if(move[0] > move[2]){
							for(int sq = initialSquare + 1; sq < targetSquare; sq++){
								if(squareContents[sq] != PIECE_EMPTY){
									System.out.println("Error: trying to move the scout over other pieces or water");
									continue;
								}
							}
						} else {
							for(int sq = initialSquare - 1; sq > targetSquare; sq--){
								if(squareContents[sq] != PIECE_EMPTY){
									System.out.println("Error: trying to move the scout over other pieces or water");
									continue;
								}
							}
						}
					// If the scouts move vertically, check if the road is clear
					} else {
						if(move[1] > move[3]){
							for(int sq = initialSquare + 10; sq < targetSquare; sq += 10){
								if(squareContents[sq] != PIECE_EMPTY){
									System.out.println("Error: trying to move the scout over other pieces or water");
									continue;
								}
							}
						} else {
							for(int sq = initialSquare - 10; sq > targetSquare; sq -= 10){
								if(squareContents[sq] != PIECE_EMPTY){
									System.out.println("Error: trying to move the scout over other pieces or water");
									continue;
								}
							}
						}
					}
				}
				
				if(!isValidSquare(move[0], move[1])){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move from an invalid square (" + (move[0] + 1) + ", " + (move[1] + 1) + ")");
					continue;
				}
				
				if(!isValidSquare(move[2], move[3])){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move from (" + move[0] + ", " + move[1] + ") to an invalid square (" + (move[2] + 1) + ", " + (move[3] + 1) + ")");
					continue;
				}
				
				// If this player does not own the piece on the selected square, this move is invalid
				if(squareOwner[initialSquare] != nextPlayer){
					// TODO Pass the error on to the player
					String error = "Error: ";
					if(nextPlayer == PLAYER_BLUE){
						error += "BLUE";
					} else {
						error += "RED";
					}
					System.out.println(error + " is trying to move a piece not owned (" + (move[0] + 1) + ", " + (move[1] + 1) + ")");
					continue;
				}
				
				// If there is no or an immovable piece on the selected square, this move is invalid
				if(squareContents[initialSquare] == PIECE_EMPTY ||
				   squareContents[initialSquare] == PIECE_UNKNOWN ||
				   squareContents[initialSquare] == PIECE_BLOCKED ||
				   squareContents[initialSquare] == PIECE_BOMB ||
				   squareContents[initialSquare] == PIECE_FLAG){
					// TODO Pass the error on to the player
					String error = "Error: ";
					if(nextPlayer == PLAYER_BLUE){
						error += "BLUE";
					} else {
						error += "RED";
					}
					System.out.println(error + " is trying to move an immovable or non-existent piece (" + (move[0] + 1) + ", " + (move[1] + 1) + ")");
					System.exit(0);
					continue;
				}
				
				// Check whether the target square is available
				if(squareContents[targetSquare] == PIECE_UNKNOWN ||
				   squareContents[targetSquare] == PIECE_BLOCKED){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move to a blocked square (" + (move[2] + 1) + ", " + (move[3] + 1) + ")");
					continue;
				}
				
				// If the target square contains a piece, check that it is not owned by the player himself
				if(squareContents[targetSquare] != PIECE_EMPTY &&
				   squareOwner[targetSquare] == nextPlayer){
					// TODO Pass the error on to the player
					System.out.println("Error: trying to move on top of your own piece (" + (move[2] + 1) + ", " + (move[3] + 1) + ")");
					continue;
				}
				
				// Check whether this move violates the two-square rule (and the rule applies)
				if(OPTION_TWO_SQUARE_RULE &&
				   -1 != threeMoveRuleHigh &&
				   -1 != threeMoveRuleLow &&
				   threeMoveRuleHigh == Math.max(targetSquare, initialSquare) &&
				   threeMoveRuleLow == Math.min(targetSquare, initialSquare)){
					// TODO Pass the error on to the player
					System.out.println("Error: cannot move to and fro more than three times in a row");
					
					continue;
				}
				
				validMove = true;
			} while(!validMove);

			int piece = squareContents[initialSquare];
			
			// If the target square is empty, we can just move
			if(squareContents[targetSquare] == PIECE_EMPTY){
				// TODO Take into account whether the player wants to resign
				if(nextPlayer == PLAYER_RED){
					redPlayer.submitAIMoveResult(PIECE_EMPTY, RESULT_NONE);
					bluePlayer.submitOpponentMove(move[0], move[1], move[2], move[3], PIECE_UNKNOWN, RESULT_NONE);
				} else {
					bluePlayer.submitAIMoveResult(PIECE_EMPTY, RESULT_NONE);
					redPlayer.submitOpponentMove(move[0], move[1], move[2], move[3], PIECE_UNKNOWN, RESULT_NONE);
				}

				popSquare(initialSquare);
				putSquare(piece, nextPlayer, targetSquare);
				history.add(new Move(nextPlayer, piece, move[0], move[1], move[2], move[3], RESULT_NONE, PIECE_UNKNOWN));
				
			// If the target square contains the opponent's flag, the game is over!
			} else if(squareContents[targetSquare] == PIECE_FLAG) {
				history.add(new Move(nextPlayer, piece, move[0], move[1], move[2], move[3], RESULT_KILL, PIECE_FLAG));
				history.add(new Move(nextPlayer, END_REASON_CAPTURED));
				
				if(nextPlayer == PLAYER_RED){
					redPlayer.submitAIMoveResult(PIECE_FLAG, RESULT_KILL);
					bluePlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILL);
				} else {
					bluePlayer.submitAIMoveResult(PIECE_FLAG, RESULT_KILL);
					redPlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILL);
				}
				
				endGame(nextPlayer, END_REASON_CAPTURED);
				if(verbose){
					System.out.println(boardToString());
				}
				
			// If the target square contains a stronger piece
			} else if((squareContents[targetSquare] == PIECE_BOMB && squareContents[initialSquare] != PIECE_MINER) ||
			          squareContents[targetSquare] < squareContents[initialSquare])
			{

				// TODO Take into account whether the player wants to resign
				if(nextPlayer == PLAYER_RED){
					redPlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_KILLED);
					bluePlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILLED);
				} else {
					bluePlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_KILLED);
					redPlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILLED);
				}

				// Remove the attacking piece, the defending piece is left alone
				popSquare(initialSquare);
				history.add(new Move(nextPlayer, piece, move[0], move[1], move[2], move[3], RESULT_KILLED, squareContents[targetSquare]));
				
			// If the target contains a weaker piece
			} else if((squareContents[targetSquare] == PIECE_MARSHAL && squareContents[initialSquare] == PIECE_SPY) ||
			          squareContents[targetSquare] > squareContents[initialSquare])
			{
				// TODO Take into account whether the player wants to resign
				if(nextPlayer == PLAYER_RED){
					redPlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_KILL);
					bluePlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILL);
				} else {
					bluePlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_KILL);
					redPlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_KILL);
				}

				// Overwrite the defending piece with the attacking piece
				history.add(new Move(nextPlayer, piece, move[0], move[1], move[2], move[3], RESULT_KILL, squareContents[targetSquare]));
				popSquare(initialSquare);
				putSquare(piece, nextPlayer, targetSquare);
			// If the target contains a piece equally strong, it's a draw
			} else if(squareContents[targetSquare] == squareContents[initialSquare]){
				if(nextPlayer == PLAYER_RED){
					redPlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_DRAW);
					bluePlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_DRAW);
				} else {
					bluePlayer.submitAIMoveResult(squareContents[targetSquare], RESULT_DRAW);
					redPlayer.submitOpponentMove(move[0], move[1], move[2], move[3], squareContents[initialSquare], RESULT_DRAW);
				}
				
				popSquare(targetSquare);
				popSquare(initialSquare);
				history.add(new Move(nextPlayer, piece, move[0], move[1], move[2], move[3], RESULT_DRAW, piece));
			}
			
			if(nextPlayer == PLAYER_RED){
				nextPlayer = PLAYER_BLUE;
			} else {
				nextPlayer = PLAYER_RED;
			}
		}
		
		initialized = false;
	}
	
	/**
	 * @return Number of plys played in this game. -1 if the game hasn't finished yet.
	 */
	public int getNumberOfPlys(){
		if(!hasEnded()){
			return -1;
		}
		
		return history.size();
	}
	
	public int getPiecesLeft(byte player){
		if(!hasEnded()){
			return -1;
		}
		
		int pieces = 0;
		
		for(int i = 0; i < squareContents.length; i++){
			if(squareContents[i] < PIECE_UNKNOWN && squareOwner[i] == player){
				pieces++;
			}
		}
		
		return pieces;
	}
	
	public boolean hasWon(byte player){
		if(!hasEnded()){
			return false;
		}
		return (winner == player);
	}
	
	public void setVerbose(boolean verbose){
		this.verbose = verbose;
	}
	
	/**
	 * @param rank Rank of a piece
	 * @return Human readable name of the piece passed as argument.
	 */
	public static String getPieceName(int rank){
		switch(rank){
		case 0: return "Marshal";
		case 1: return "General";
		case 2: return "Colonel";
		case 3: return "Major";
		case 4: return "Captain";
		case 5: return "Lieutenant";
		case 6: return "Sergeant";
		case 7: return "Miner";
		case 8: return "Scout";
		case 9: return "Spy";
		case 10: return "Bomb";
		case 11: return "Flag";
		default: return "Unknown";
		}
	}
	
	/**
	 * Terminate the game
	 * 
	 * @param winner Who won the game. -1 if nobody did
	 * @param reason Why the game was ended
	 */
	protected void endGame(int winner, int reason){
		gameEnded = true;
		
		if(-1 == winner){
			redPlayer.endGame(END_RESULT_DRAW, reason);
			bluePlayer.endGame(END_RESULT_DRAW, reason);
		} else if(winner == PLAYER_RED){
			redPlayer.endGame(END_RESULT_WIN, reason);
			bluePlayer.endGame(END_RESULT_LOSS, reason);
		} else if(winner == PLAYER_BLUE){
			redPlayer.endGame(END_RESULT_LOSS, reason);
			bluePlayer.endGame(END_RESULT_WIN, reason);
		}
		
		this.winner = winner;
	}
	
	/**
	 * @return Whether the game has already ended
	 */
	protected boolean hasEnded(){
		return gameEnded;
	}
	
	/**
	 * Empty the contents of a square
	 * 
	 * @param square Number of the square that needs to be cleared
	 * @return The previous contents of the square
	 */
	protected int popSquare(int square){
		// TODO: throw exception when square < 0 or square > 99
		int oldValue = squareContents[square];
		if(square == 42 || square == 43 ||
		   square == 46 || square == 47 ||
		   square == 52 || square == 53 ||
		   square == 56 || square == 57){
			// Hmm, I suppose this is pretty stupid,
			// since nobody would ever remove a piece from water
			// (there never is one). Let's leave it in for laughs.
			squareContents[square] = PIECE_BLOCKED;
		} else {
			squareContents[square] = PIECE_EMPTY;
			openDirections[square] = 0;
		}
			
		int xPos = square % 10;
		int yPos = square / 10;
		
		// All my squares next to this one have one more direction to move in 
		if(xPos < 9 && squareOwner[square + 1] == squareOwner[square] &&
		   squareContents[square + 1] < PIECE_BOMB){
			openDirections[square + 1]++;
		}
		
		if(xPos > 0 && squareOwner[square - 1] == squareOwner[square] &&
		   squareContents[square - 1] < PIECE_BOMB){
			openDirections[square - 1]++;
		}
		
		if(yPos < 9 && squareOwner[square + 10] == squareOwner[square] &&
		   squareContents[square + 10] < PIECE_BOMB){
			openDirections[square + 10]++;
		}
		
		if(yPos > 0 && squareOwner[square - 10] == squareOwner[square] &&
		   squareContents[square - 10] < PIECE_BOMB){
			openDirections[square - 10]++;
		}
			
		return oldValue;
	}
	
	/**
	 * Put a piece onto a square
	 * 
	 * NOTE: this method does not check for valid moves;
	 * do so before calling it to avoid overwriting pieces
	 * you do not want to overwrite.
	 * 
	 * @param piece Piece to place
	 * @param owner Owner of the piece
	 * @param square Where to place the piece
	 */
	protected boolean putSquare(int piece, byte owner, int square){
		if(!isValidSquare(square)){
			return false;
		}
		
		squareContents[square] = piece;
		squareOwner[square] = owner;
		
		openDirections[square] = 0;
		int xPos = square % 10;
		int yPos = square / 10;
		
		// All my squares next to this one have one fewer direction to move in 
		if(xPos < 9 && squareOwner[square + 1] == squareOwner[square] &&
		   squareContents[square + 1] < PIECE_UNKNOWN){
			openDirections[square + 1]--;
			// It's not one of my pieces; now is it available?
		} else if(xPos < 9 && squareContents[square + 1] != PIECE_BLOCKED) {
			openDirections[square]++;
		}
		
		if(xPos > 0 && squareOwner[square - 1] == squareOwner[square] &&
		   squareContents[square - 1] < PIECE_UNKNOWN){
			openDirections[square - 1]--;
			// It's not one of my pieces; now is it available?
		} else if(xPos > 0 && squareContents[square - 1] != PIECE_BLOCKED) {
			openDirections[square]++;
		}
		
		if(yPos < 9 && squareOwner[square + 10] == squareOwner[square] &&
		   squareContents[square + 10] < PIECE_UNKNOWN){
			openDirections[square + 10]--;
			// It's not one of my pieces; now is it available?
		} else if(yPos < 9 && squareContents[square + 10] != PIECE_BLOCKED) {
			openDirections[square]++;
		}
		
		if(yPos > 0 && squareOwner[square - 10] == squareOwner[square] &&
		   squareContents[square - 10] < PIECE_UNKNOWN){
			openDirections[square - 10]--;
			// It's not one of my pieces; now is it available?
		} else if(yPos > 0 && squareContents[square - 10] != PIECE_BLOCKED) {
			openDirections[square]++;
		}
		
		// This method should not be used to placing flags and bombs,
		// but just in case.
		if(piece == PIECE_FLAG || piece == PIECE_BOMB){
			openDirections[square] = 0;
		}
		
		return true;
	}
	
	protected boolean isValidSquare(int x, int y){
		// TODO double-check how this works
		if(x < 0){
			return false;
		}
		if(x >= 10){
			return false;
		}
		if(y < 0){
			return false;
		}
		if(y >= 10){
			return false;
		}
		
		// Water!
		if((y == 4 || y == 5) &&
		   (x == 2 || x == 3 || x == 6 || x == 7))
		{
			return false;
		}
		
		return true;
	}
	
	protected boolean isValidSquare(int square){
		// TODO double-check correct workings
		return (square >= 0 && square <= 99 &&
		        square != 42 && square != 43 &&
		        square != 46 && square != 47 &&
		        square != 52 && square != 53 &&
		        square != 56 && square != 57);
	}
	
	/**
	 * Place the setups each player requests and check that it is valid
	 */
	protected void initializePieces(){
		if(initialized){
			// TODO Throw an exception when trying to initialize the board when we're playing
			return;
		}

		squareContents = new int[100];
		squareOwner = new byte[100];
		openDirections = new int[100];
		
		for(int i = 0; i < squareContents.length; i++){
			squareContents[i] = PIECE_EMPTY;
			openDirections[i] = 0;
		}
		squareContents[42] = squareContents[43] = squareContents[46] = squareContents[47]
		                   = squareContents[52] = squareContents[53] = squareContents[56] = squareContents[57]
		                   = PIECE_BLOCKED;
		
		Integer[] piecesAvailable = new Integer[12];
		// Check that the red player uses exactly all his pieces
		piecesAvailable[PIECE_MARSHAL] = 1;
		piecesAvailable[PIECE_GENERAL] = 1;
		piecesAvailable[PIECE_COLONEL] = 2;
		piecesAvailable[PIECE_MAJOR] = 3;
		piecesAvailable[PIECE_CAPTAIN] = 4;
		piecesAvailable[PIECE_LIEUTENANT] = 4;
		piecesAvailable[PIECE_SERGEANT] = 4;
		piecesAvailable[PIECE_MINER] = 5;
		piecesAvailable[PIECE_SCOUT] = 8;
		piecesAvailable[PIECE_SPY] = 1;
		piecesAvailable[PIECE_BOMB] = 6;
		piecesAvailable[PIECE_FLAG] = 1;
		
		int currentPiece;
		for(int y = 0; y < 4; y++){
			for(int x = 0; x < 10; x++){
				currentPiece = redPlayer.getAISetupPiece(x, y);
				if(piecesAvailable[currentPiece] <= 0){
					// TODO: throw an exception when this piece is unavailable
					System.out.println("Error: trying to place a piece that is unavailable (" + currentPiece + ")");
					return;
				}
				
				if(currentPiece == PIECE_UNKNOWN ||
				   currentPiece == PIECE_EMPTY ||
				   currentPiece == PIECE_BLOCKED){
					   // TODO Throw an exception when not all squares are occupied
					   System.out.println("Error: red player did not place all pieces");
					   return;
				}
				
				piecesAvailable[currentPiece]--;
				putSquare(currentPiece, PLAYER_RED, x+10*y);
				bluePlayer.setOpponentSetupPiece(x, y);
			}
		}

		// Check that the blue player uses exactly all his pieces
		piecesAvailable[PIECE_MARSHAL] = 1;
		piecesAvailable[PIECE_GENERAL] = 1;
		piecesAvailable[PIECE_COLONEL] = 2;
		piecesAvailable[PIECE_MAJOR] = 3;
		piecesAvailable[PIECE_CAPTAIN] = 4;
		piecesAvailable[PIECE_LIEUTENANT] = 4;
		piecesAvailable[PIECE_SERGEANT] = 4;
		piecesAvailable[PIECE_MINER] = 5;
		piecesAvailable[PIECE_SCOUT] = 8;
		piecesAvailable[PIECE_SPY] = 1;
		piecesAvailable[PIECE_BOMB] = 6;
		piecesAvailable[PIECE_FLAG] = 1;

		for(int y = 6; y < 10; y++){
			for(int x = 0; x < 10; x++){
				currentPiece = bluePlayer.getAISetupPiece(x, y);
				if(piecesAvailable[currentPiece] <= 0){
					// TODO: throw an exception when this piece is unavailable
					System.out.println("Error: trying to place a piece that is unavailable");
					return;
				}
				
				if(currentPiece == PIECE_UNKNOWN ||
				   currentPiece == PIECE_EMPTY ||
				   currentPiece == PIECE_BLOCKED){
					   // TODO Throw an exception when not all squares are occupied
					   System.out.println("Error: blue player did not place all pieces");
					   return;
				}
				
				piecesAvailable[currentPiece]--;
				putSquare(currentPiece, PLAYER_BLUE, x + 10*y);
				redPlayer.setOpponentSetupPiece(x, y);
			}
		}
	}
	
	/**
	 * Get the coordinates of a square in human readable form (x, y).
	 * 
	 * Mostly useful for debugging.
	 * 
	 * @param square
	 * @return
	 */
	protected String getCoordinates(int square){
		return "(" + (square % 10 + 1) + ", " + (square / 10 + 1) + ")";
	}
	
	protected String boardToString(){
		String setup = "+---|---|---|---|---|---|---|---|---|---+";
		for(int i = 0; i < squareContents.length; i++){
			if(i % 10 == 0){
				setup += "|\n";
			}
			
			setup += "|";
			
			if(squareContents[i] == PIECE_EMPTY){
				setup += "   ";
			} else if(squareContents[i] == PIECE_BLOCKED) {
				setup += "~~~";
			} else {
				if(squareOwner[i] == PLAYER_RED){
					setup += "R";
				} else {
					setup += "B";
				}
				if(squareContents[i] < 10){
					setup += " ";
				}
				setup += squareContents[i];
			}
		}
		setup += "|\n+---|---|---|---|---|---|---|---|---|---+";
		return setup;
	}
}