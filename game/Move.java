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

package game;

public class Move {
	protected int piece;
	protected int fromX;
	protected int fromY;
	protected int toX;
	protected int toY;
	protected byte toPlay;
	protected int result;
	protected int opponent;
	protected boolean endGame;
	
	/**
	 * Call with these arguments when the game ends and thus no move is made.
	 * 
	 * @param winner
	 * @param reason
	 */
	public Move(byte winner, int reason){
		endGame = true;
		toPlay = winner;
		result = reason;
	}
	
	public Move(byte toPlay, int piece, int fromX, int fromY,
	                 int toX, int toY, int result, int opponent){
		endGame = false;
		this.toPlay = toPlay;
		this.piece = piece;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
		this.result = result;
		this.opponent = opponent;
	}
	
	public int getFromX(){
		return fromX;
	}
	
	public int getFromY(){
		return fromY;
	}
	
	public int getToX(){
		return toX;
	}
	
	public int getToY(){
		return toY;
	}
	
	public String toString(){
		String output;
		if(toPlay == Game.PLAYER_BLUE){
			output = "BLUE player ";
		} else {
			output = "RED player ";
		}
		
		if(endGame){
			if(result == Game.END_REASON_CAPTURED){
				return output + "captures the flag and WINS THE GAME!";
			} else {
				return output + "blocked all paths and WINS THE GAME!";
			}
		}
		
		if(fromX == -1){
			return output + "could not make a move and passes a turn.";
		}
		
		output += "moves " + piece + " from (" + (fromX + 1) + ", " + (fromY + 1) + ") to (" + (toX + 1) + ", " + (toY + 1) + ").";
		if(result == Game.RESULT_NONE){
			return output;
		}
		
		if(result == Game.RESULT_DRAW){
			return output + "\n\tAnother " + piece + " was encounted - both pieces die!";
		}
		
		if(result == Game.RESULT_KILL){
			return output + "\n\tA " + opponent + " was found and slaughtered!";
		}
		
		if(result == Game.RESULT_KILLED){
			return output + "\n\tA " + opponent + " was found for which it was no match...";
		}
		
		return output;
	}
	
	public int getPlayer(){
		return toPlay;
	}
	
	public int getPiece(){
		return piece;
	}
	
	public int getOriginX(){
		return fromX;
	}
	
	public int getOriginY(){
		return fromY;
	}
	
	public int getDestinationX(){
		return toX;
	}
	
	public int getDestinationY(){
		return toY;
	}
	
	public int getResult(){
		return result;
	}
	
	public int getVictim(){
		return opponent;
	}
}