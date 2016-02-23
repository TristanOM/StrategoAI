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

package ga;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.File;

import game.Game;
import players.Vicki;
import players.Dummy;
import players.Player;

public class Evolve {

	protected static int PARAM_NUMBER_OF_PARENTS = 2;
	protected static int PARAM_POPULATION_SIZE = 200;

	/**
	 * The amount of matches each solution plays against the reference opponent each generation
	 */
	protected static int PARAM_NUMBER_OF_OPPONENTS = 5;
	protected static int PARAM_GENERATIONS = 20;
	
	/**
	 * With a weight of 1, the influence the fitness of an individual has is linear,
	 * 2 is exponential, etc. A higher value means more exploitation, less exploration.
	 */
	protected static int PARAM_FITNESS_WEIGHT = 2;
	
	/**
	 * Reward for winning. Later this could also incorporate the type of win.
	 */
	protected static int PARAM_REWARD_WIN = 1000;
	/**
	 * Punishment/reward for the amount of moves needed in a game.
	 * The amount of move can range from about 300 to about 5500
	 * in a simple AI against a random player.
	 */
	protected static double PARAM_MOVE_MULTIPLIER = -0.25;
	/**
	 * Punishment/reward for the amount of pieces you have left at the end of a game.
	 * The more you have left, the more convincing your victory will mostly have been.
	 * This could perhaps later take into account the kind of pieces still left.
	 */
	protected static double PARAM_MY_PIECES_MULTIPLIER = 100;
	/**
	 * Punishment/reward for the amount of pieces the opponent has left at the end of a game.
	 * Fewer pieces could point to a harsher victory and be psychologically beneficial,
	 * but could also be the result of an ineffective searching strategy.
	 */
	protected static double PARAM_HIS_PIECES_MULTIPLIER = -20;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Vicki[] solutions = new Vicki[PARAM_POPULATION_SIZE];
		int[] fitness = new int[PARAM_POPULATION_SIZE];;
		
		int challengerFitness;
		byte challengerColour;
		byte opponentColour;
		int average;
		double[][] averageParams = null;
		double[][] ps;
		int wins;
		int bestSolution = 0;
		
		// File to write statistics to
		String outputFile = "output.csv";
		String output = "";
		String outputHeadings = "";
		
		// Directory to save the population is
		String saveDir = "population";
		boolean loadPrevious = false;
		String population;
		
		Player referenceOpponent = new Dummy();
		
		int generations = 0;
		
		String tmpDir = System.getProperty("java.io.tmpdir");
		if(!tmpDir.endsWith(System.getProperty("file.separator"))){
			tmpDir += System.getProperty("file.separator");
		}
		
		// Generate initial population
		BufferedReader latestReader = null;
		if((new File(tmpDir + saveDir + java.io.File.separator + "latestGeneration.csv")).exists()){
			try {
				latestReader = new BufferedReader(new FileReader(tmpDir + saveDir + java.io.File.separator + "latestGeneration.csv"));
			} catch (FileNotFoundException e) {
				// Too bad!
			}
		}
		int amountOfParameters = 1;
		for(int i = 0; i < solutions.length; i++){
			solutions[i] = new Vicki();
			
			// Try to load previous parameters
			if(loadPrevious && latestReader != null){
				String paramLine;
				try {
					paramLine = latestReader.readLine();

					// The first value of each line is the fitness
					fitness[i] = Integer.valueOf(paramLine.substring(0, paramLine.indexOf(',')));
					paramLine = paramLine.substring(paramLine.indexOf(',') + 1);
					
					// The rest can be used to initialize this player
					solutions[i].initializeParams(paramLine);
					
				} catch (IOException e) {
					// Too bad, random values then
					solutions[i].initializeParams();
				}
			} else {
				solutions[i].initializeParams();
			}
		}
		
		if(loadPrevious && latestReader != null){
			solutions = generateNewPopulation(solutions, fitness);
		}
		
		BufferedWriter latestWriter = null;
		File latestFile;
		if(saveDir != null && saveDir != ""){
			(new File(tmpDir + saveDir)).mkdir();
			try{
				latestWriter = new BufferedWriter(new FileWriter(tmpDir + saveDir + java.io.File.separator + "latestGeneration.csv"));
			} catch(IOException e) {
				// Too bad...
				System.out.println(e.getMessage());
			}
		}
		
		Game game = new Game(solutions[0], solutions[1]);
		game.setVerbose(false);
		
		// Evolve PARAM_GENERATIONS generations
		do{
			wins = 0;
			fitness = new int[PARAM_POPULATION_SIZE];
			// Let each solution fight the reference solution PARAM_NUMBER_OF_OPPONENTS times
			for(int challenger = 0; challenger < solutions.length; challenger++){
				for(int opponentNr = 0; opponentNr < PARAM_NUMBER_OF_OPPONENTS; opponentNr++){
					
					if(Math.random() < 0.5){
						challengerColour = Game.PLAYER_RED;
						opponentColour = Game.PLAYER_BLUE;
						game.setRedPlayer(solutions[challenger]);
						game.setBluePlayer(referenceOpponent);
					} else {
						challengerColour = Game.PLAYER_BLUE;
						opponentColour = Game.PLAYER_RED;
						game.setRedPlayer(referenceOpponent);
						game.setBluePlayer(solutions[challenger]);
					}
					
					game.initialize();
					game.play();
					
					// Calculate the fitness of the challenger
					challengerFitness = (int) (PARAM_MOVE_MULTIPLIER * game.getNumberOfPlys() +
					             PARAM_MY_PIECES_MULTIPLIER * game.getPiecesLeft(challengerColour) +
					             PARAM_HIS_PIECES_MULTIPLIER * game.getPiecesLeft(opponentColour));
					if(game.hasWon(challengerColour)){
						challengerFitness += PARAM_REWARD_WIN;
						wins++;
					}
					
					// Update this player's fitness
					fitness[challenger] = (fitness[challenger] * opponentNr + challengerFitness) / (opponentNr + 1);
				}
				
				if(fitness[challenger] > fitness[bestSolution]){
					bestSolution = challenger;
				}
				
				if(averageParams == null){
					averageParams = solutions[challenger].getParams();
				} else {
					ps = solutions[challenger].getParams();
					for(int i = 0; i < ps.length; i++){
						for(int j = 0; j < ps[i].length; j++){
							if(averageParams[i] == null){
								averageParams[i] = new double[ps[i].length];
							}
							averageParams[i][j] += ps[i][j];
						}
					}
				}

			}
			
			// Save this generation to disk
			if(latestWriter != null){
				population = "";
				for(int i = 0; i < solutions.length; i++){
					population += fitness[i] + ',';
					for(double[] rank : solutions[i].getParams()){
						for(double p : rank){
							population += "," + Double.toString(p);
						}
					}
					population += "\n";
				}
				latestFile = new File(tmpDir + saveDir + java.io.File.separator + "latestGeneration.csv");
				if(latestFile.exists() && generations > 0){
					latestFile.renameTo(new File(tmpDir + saveDir + java.io.File.separator + "generation" + (generations - 1) + ".csv"));
				}
				try{
					latestWriter = new BufferedWriter(new FileWriter(tmpDir + saveDir + java.io.File.separator + "latestGeneration.csv"));
					latestWriter.write(population);
					latestWriter.flush();
				} catch (Exception e) {
					// Too bad, not writing to file!
				}
			}
			
			// Print some statistics on this generation
			
			// TODO: print the variety of this population
			System.out.println(" == Generation " + (generations + 1) + " ==");
			
			average = getAverage(fitness);
			
			System.out.println("Winners: " + wins);
			
			System.out.println("Average fitness: " + average / fitness.length);
			output += generations + "," + (average / fitness.length);
			
			for(int i = 0; i < averageParams.length; i++){
				for(int j = 0; j < averageParams[i].length; j++){
					averageParams[i][j] = averageParams[i][j] / PARAM_POPULATION_SIZE;
				}
			}
				
			System.out.print("Average parameters: " + averageParams[0][0]);
			output += "," + Double.toString(averageParams[0][0]);
			if(generations == 0){
				outputHeadings = "Generation,Average fitness," + Game.getPieceName(0) + " (0)";
			}
			for(int j = 0; j < averageParams[0].length; j++){
				for(int i = 0; i < averageParams.length; i++){
					if(i == 0 && j == 0){
						continue;
					}
					
					if(generations == 0){
						outputHeadings += "," + Game.getPieceName(i) + " (" + i + ")";
					}

					System.out.print(", " + averageParams[i][j]);
					output += ',' + Double.toString(averageParams[i][j]);
				}
			}
			output += "\n";
			System.out.print("\n");
			averageParams = new double[averageParams.length][];
			
			System.out.println("Best performer: " + bestSolution + "\n\tfitness: " + fitness[bestSolution] + "");
						
			// All solutions have fought a number of other solutions and their fitness calculated,
			// now let's mate the best of them
			solutions = generateNewPopulation(solutions, fitness);
			generations++;
			
			// Reset all solutions' fitness
			fitness = new int[fitness.length];
		} while(generations < PARAM_GENERATIONS);
		
		System.out.println("Done!");
		
		if(outputFile == null || outputFile == ""){
			return;
		}
		
		try{
			FileWriter f = new FileWriter(tmpDir + outputFile);
			BufferedWriter w = new BufferedWriter(f);
			w.write(outputHeadings + "\n" + output);
			w.close();
		} catch (Exception e) {
			// Too bad, not writing to file!
		}
		
		// TODO Runnen tot de verbetering afvlakt
	}
	
	protected static int getAverage(int[] fitness) {
		int average = 0;
		for(int i = 0; i < fitness.length; i++){
			average += fitness[i];
		}
		
		return average;
	}

	protected static Vicki[] generateNewPopulation(Vicki[] solutions, int[] fitness){
		// Generate a new population
		Vicki[] newSolutions = new Vicki[PARAM_POPULATION_SIZE];

		int[] parents;
		int[] parentValues;
		int currentFitnessSum;
		double[][] params;
		
		// Calculate the total fitness of the entire population
		int sum = 0;
		for(int f : fitness){
			sum += f^PARAM_FITNESS_WEIGHT;
		}
		
		for(int s = 0; s < newSolutions.length; s++){
			parents = new int[PARAM_NUMBER_OF_PARENTS];
			// For each parent, calculate a number between 0 and the total fitness.
			parentValues = new int[PARAM_NUMBER_OF_PARENTS];
			for(int i = 0; i < parentValues.length; i++){
				parentValues[i] = (int) (Math.random() * sum);
			}
			
			// We can now select the solution that is exactly that amount of fitness away
			// from the total fitness.
			// (So solutions with greater fitness have a greater chance of being selected.)
			currentFitnessSum = sum;
			for(int i = fitness.length - 1; i >= 0; i--){
				currentFitnessSum -= fitness[i]^PARAM_FITNESS_WEIGHT;
				for(int j = 0; j < parentValues.length; j++){
					// By default the selected parent is the first (because 0 is an int's default value).
					// If it is zero, we either have not found the chosen parent yet, or it actually
					// is the first. In either case, we say that the current solution is the chosen one.
					if(currentFitnessSum < parentValues[j] && parents[j] == 0){
						parents[j] = i;
					}
				}
			}
			
			// We've selected a number of parents, now let's combine them
			params = solutions[parents[0]].getParams();
			for(int i = 0; i < params.length; i++){
				for(int j = 0; j < params[i].length; j++){
					// Get this parameter from one of my parents
					params[i][j] = solutions[parents[Math.round(Math.round(Math.random() * (parents.length - 1)))]].getParams()[i][j];
				}
			}
			newSolutions[s] = new Vicki();
			newSolutions[s].initializeParams(params);
		}

		return newSolutions;
	}

}