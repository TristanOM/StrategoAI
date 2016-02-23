/**
 * 
 */
package Test;

import players.Vicki;
import players.Dummy;
import players.Bob;
import players.Bob3;
import game.Game;

/**
 * @author vincent
 *
 */
public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		//Dummy player2 = new Dummy();
      //Dummy player2 = new Dummy();

      Bob3 player1 = new Bob3();
     Bob player2 = new Bob();
      //Vicki player1 = new Vicki();

	//Vicki player2 = new Vicki();
		Game game = new Game(player1, player2);
		game.setVerbose(false);
		
		int red = 0;
		int blue = 0;
		int games = 1;
		
		for(int i = 0; i < games; i++){
			game.initialize();
		
			game.play();
			if(game.hasWon(Game.PLAYER_RED)){
				red++;
			} else {
				blue++;
			}
		}
		
		System.out.println("Played " + games + " games.\nResults:\n\tRed: " + red + " wins\n\tBlue: " + blue + " wins");
	}

}