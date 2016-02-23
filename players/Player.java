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

/**
 * @author vincent
 *
 */
public interface Player {
	/**
	 * @param username This player's name
	 */
	public void setAIUsername(String username);
	
	/**
	 * @param username Opponent's name
	 */
	public void setOpponentUsername(String username);
	
	/**
	 * @param timeLimit The number of seconds a turn may take 
	 * @return Whether this limit is acceptable
	 */
	public boolean setMoveTimeLimit(int timeLimit);
	
	/**
	 * Called when the game will be initialized
	 */
	public void initalizeGame();
	
	/**
	 * This player should create his/her setup.
	 * 
	 * The red player places his pieces on rows 0-3,
	 * the blue player places his pieces on rows 6-9.
	 * The red player starts.
	 * 
	 * @param color 0 when this player is red, 1 when this player is blue
	 */
	public void createAISetup(int color);
	
	/**
	 * For each possible starting position, return the respective starting piece.
	 * 
	 * @param x X-coordinate of the piece we want to know the value of.
	 * @param y Y-coordinate of the piece we want to know the value of.
	 * @return Rank of the respective piece, 13 for empty tiles.
	 */
	public int getAISetupPiece(int x, int y);
	
	public void setOpponentSetupPiece(int x, int y);
	
	/**
	 * Called when both setups are valid; the game will now begin.
	 */
	public void startGame();
	
	/**
	 * Called when the opponenent makes a move.
	 * 
	 * @param startX Starting X-coordinate of the piece to move
	 * @param startY Starting Y-coordinate of the piece to move
	 * @param endX Ending X-coordinate of the piece to move
	 * @param endY Ending Y-coordinate of the piece to move
	 * @param rank Rank of the opponent's piece, if revealed, 12 otherwise
	 * @param result What the move results in.
	 * @return If the opponent requested a draw, returning 1 accepts the draw, 0 rejects it
	 */
	public byte submitOpponentMove(int startX, int startY,
	                               int endX, int endY,
	                               int rank, int result);
	
	/**
	 * Get the next move
	 * 
	 * This method is similar to Metaforge's getAIMove, the difference being that
	 * this doesn't manipulate a parameter passed by reference, but just returns
	 * the move that is to be made.
	 * 
	 * @return An array containing the move like this:
	 *         result[0] Starting X-coordinate of the piece to move
	 *         result[1] Starting Y-coordinate of the piece to move
	 *         result[2] Ending X-coordinate of the piece to move
	 *         result[3] Ending Y-coordinate of the piece to move
	 *         result[4] Special value for requesting a draw (1) or resigning (3)
	 */
	public int[] getMove();
	
	/**
	 * Called when a move was valid, detailing the results of that move
	 * 
	 * @param rank Rank of the attacked piece, if applicable. Otherwise 13 (empty)
	 * @param result Result of the attack, if applicable, otherwise 0
	 * @return 1 if you would like to resign, otherwise ignored
	 */
	public byte submitAIMoveResult(int rank, int result);
	
	/**
	 * Called when the game has ended
	 * 
	 * @param result Result of the game: loss (0), win(1) or draw (2)
	 * @param reason
	 */
	public void endGame(int result, int reason);
}