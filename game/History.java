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

import java.util.Stack;

public class History {
	protected Stack<Move> history;
	protected boolean debug = false;
	
	public History(){
		history = new Stack<Move>();
	}
	
	public void outputLog(boolean debug){
		this.debug = debug;
	}
	
	public void add(Move move){
		history.push(move);
		if(debug){
			System.out.println(move);
		}
	}
	
	public Move peek(){
		return peek(history.size() - 1);
	}
	
	public Move peek(int number){
		return history.get(number);
	}
	
	public int size(){
		return history.size();
	}
}