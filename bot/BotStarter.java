

package bot;

/**
 * @author ByteeCat
 * 
 * Baclava Georgiana-Liliana 322CB
 * Daraban Alexandra-Mihaela 322CB (capitan)
 * Ifrim Andreea-Carmen 322CB
 * Lupancescu Diana 322CB
 */

import java.util.ArrayList;
import java.util.LinkedList;

import map.Region;
import map.SuperRegion;
import move.AttackTransferMove;
import move.PlaceArmiesMove;


public class BotStarter implements Bot 
{
	@Override
	/**
	 * A method that returns which region the bot would like to start on, the pickable regions are stored in the BotState.
	 * The bots are asked in turn (ABBAABBAAB) where they would like to start and return a single region each time they are asked.
	 * This method returns one region from the most rewarding superRegion at first if possible or a region from the neighborhood if 
	 * the bot already picked some regions
	 */
	public Region getStartingRegion(BotState state, Long timeOut){
		
		Region startingRegion = null;
		if( state.getVisibleMap() == null ){
			double rand = Math.random();
			int r = (int)(rand*state.getPickableStartingRegions().size());
			int regionId = state.getPickableStartingRegions().get(r).getId();
			startingRegion = state.getFullMap().getRegion(regionId);
		}
		else {
			for(Region reg: state.getPickableStartingRegions()){
				if( getNumberOfEnemies(reg,state.getMyPlayerName())!=0 ){
					startingRegion = reg;
					break;
				}
			}
		}
		return startingRegion;
	}


	/**
	 * 
	 * A method that returns the number of enemies that are surrounding the region. It sums up all the armies.
	 * @param region to which is returned the number of enemies
	 * @param opponentName the name of the opponent
	 * @return the number of enemies
	 */
	public int getNumberOfEnemies(Region region, String opponentName){

		int count = 0;
		LinkedList<Region> neighbors = region.getNeighbors();

		for(Region reg:neighbors){
			if(reg.ownedByPlayer(opponentName) == true){
				count += reg.getArmies();
			}
		}

		return count;
	}

	/**
	 * A method that returns the number of free regions that are surrounding the region given as parameter.
	 * @param region to which is returned the number of free regions
	 * @param state of the bot
	 * @return the number of free regions
	 */
	public int getNumberOfFreeRegions(Region region, BotState state){
		int count = 0;
		LinkedList<Region> neighbors = region.getNeighbors();
		for(Region reg:neighbors){
			if(reg.ownedByPlayer(state.getOpponentPlayerName()) == false && reg.ownedByPlayer(state.getMyPlayerName()) == false){
				count++;
			}
		}
		return count;
	}

	/**
	 * A method that returns the number of the regions owned by the opponent from the neighborhood
	 * @param region to which the number of enemy regions is returned
	 * @param opponentName the name of the opponent
	 * @return the number of enemy regions
	 */
	public int getNumberOfEnemyRegions(Region region, String opponentName){
		
		int count = 0;
		LinkedList<Region> neighbors = region.getNeighbors();
		
		for(Region reg:neighbors){
			if(reg.ownedByPlayer(opponentName) == true){
				count++;
			}
		}
		return count;
	}

	

	@Override
	/**
	 * This method is called for at first part of each round. It places armies on all the regions which are in danger.
	 * After all bot's regions are safe, it places 1 army on each region which is on the external side of our map: if it
	 * has as neighbors enemy or free regions.
	 * @return The list of PlaceArmiesMoves for one round
	 */
	public ArrayList<PlaceArmiesMove> getPlaceArmiesMoves(BotState state, Long timeOut) 
	{

		ArrayList<PlaceArmiesMove> placeArmiesMoves = new ArrayList<PlaceArmiesMove>();
		String myName = state.getMyPlayerName();
		String opponentName = state.getOpponentPlayerName();
		// the number of armies which must be placed on the map
		int armiesLeft = state.getStartingArmies();
		ArrayList<Region> myRegions = new ArrayList<Region>();
		
		//the regions which are near a free or an opponent's region are placed in a list
		for(Region toRegion : state.getVisibleMap().getRegions()){
			if(toRegion.ownedByPlayer(myName) == true){
				
				if(getNumberOfEnemyRegions(toRegion, opponentName) != 0 || getNumberOfFreeRegions(toRegion, state) != 0){
					myRegions.add(toRegion);
				}
			}
		}
		
		//the list of regions is sorted with bubble sort in descending order from the most endangered region to the most safest region
		for(int i = 0;i<myRegions.size();i++){
			
			for(int j=i+1;j<myRegions.size();j++){
				
				if(getNumberOfEnemies(myRegions.get(i), opponentName) < getNumberOfEnemies(myRegions.get(j), opponentName)){
					
					Region aux = myRegions.get(i);
					myRegions.set(i, myRegions.get(j));
					myRegions.set(j, aux);
				}
			}
		}
		
		//we place the armies
		//if a region can't become safe after the armies are placed, it is skipped
		for(Region toRegion : myRegions){
			
			int enemies = getNumberOfEnemies(toRegion,opponentName);
	
			//the minimum number of armies is placed in order to region to become safe
			if(armiesLeft - (enemies/2 + 1 - toRegion.getArmies()) > 0 && enemies/2 + 1 - toRegion.getArmies() > 0){
				placeArmiesMoves.add(new PlaceArmiesMove(myName, toRegion, enemies/2 + 1 - toRegion.getArmies()));
				armiesLeft -= (enemies/2 + 1 - toRegion.getArmies());
			}
		}
		
		//if there are still armies left, they are placed one by one on the regions
		while(armiesLeft>0){
			
			for(Region toRegion:myRegions){
			
				if(armiesLeft==0)
					break;
				
				placeArmiesMoves.add(new PlaceArmiesMove(myName, toRegion,1));
				
				armiesLeft--;
			}
		}
		
		return placeArmiesMoves;
	}
	

	/**
	 * A method that returns the estimated number of armies lost by the attacked region and by the attacking region:
	 * For the amount of defending armies destroyed: The first value is exactly 60% of the total amount of attackers. 
	 * The second value is given  namely each attacker has a 60% chance of destroying one defending army. 
	 * This second value is thus dependent on luck, as it should average 60% of the attacking armies. 
	 * After these two values are calculated they are averaged with use of the luck factor which currently is 16%.
	 *  So 84% of the first value is combined with 16% of the second value. For the amount of attacking armies destroyed, 
	 *  the same calculations are used, except the 60% is changed to 70%. 
	 */
	public float [] average( int attack, int defense){
		
		float [] avg = new float[2];
		float deadDefense = (60 * attack)/100;
		float chanceAttack = (60 * defense)/100;
		
		//the amount of defending armies destroyed
		avg[0] = (84 * deadDefense + 16 * chanceAttack)/100;
		
		float deadAttack = (70 * defense)/100;
		float chanceDefense = (70 * attack)/100;
		
		//the amount of attacking armies destroyed
		avg[1] = (84 * deadAttack + 16 * chanceDefense)/100;
		
		return avg;
	}
	
	/**
	 * A method that returns true if the region can be attacked with success by the player with the name given as parameter.
	 * It uses the average method to find out if the region can be attacked with all the available armies surrounding it.
	 * @param region the region which is tested if it can be attacked
	 * @param name of the enemy
	 * @return true if it can be attacked with success and false otherwise
	 */
	public boolean successfulAttack(Region region, String name){
		int defense = region.getArmies();
		
		// it calculates the maximum number of armies which can attack the region
		int attack = getNumberOfEnemies(region, name) - getNumberOfEnemyRegions(region, name);
		float[] avg = average(attack, defense);
		
		//if it can be attacked and conquered it returns true
		if(avg[0] >= defense && avg[1]+1 <= attack)
			return true;
		
		//otherwise, it returns false
		return false;
	}
	
	@Override
	/**
	 * This method is called for at the second part of each round. It creates a list of regions which might be attacked and a list
	 * of bot's regions.
	 * @return The list of AttackTransferMoves for one round
	 */
	public ArrayList<AttackTransferMove> getAttackTransferMoves(BotState state, Long timeOut) {
		
		ArrayList<AttackTransferMove> attackTransferMoves = new ArrayList<AttackTransferMove>();
		ArrayList<Region> possibleAttacks = new ArrayList<Region>();
		ArrayList<Region> myRegions = new ArrayList<Region>();

		String myName = state.getMyPlayerName();
		String opponentName = state.getOpponentPlayerName();
		
		LinkedList<Region> regions = (state.getVisibleMap()).getRegions();
		
		int [] armiesLeft = new int [regions.size()];
		
		for(Region toRegion : state.getVisibleMap().getRegions()){
			
			if(toRegion.ownedByPlayer(myName)==false){
			
				//if the region can be attacked by the player's bot, it is added in the list of possible attacks
				if(successfulAttack(toRegion,myName) == true || toRegion.ownedByPlayer(opponentName) == false){
					possibleAttacks.add(toRegion);
				}
			}
			else{
				
				myRegions.add(toRegion);
				
				//the number of armies left in the region is saved
				armiesLeft[myRegions.indexOf(toRegion)] = toRegion.getArmies();
			}
		}

		//the list of possible targets is sorted by its importance
		possibleAttacks=sort(possibleAttacks,myName, opponentName);
		
		for(Region toRegion:possibleAttacks){
			
			int defense = toRegion.getArmies();
			
			//creates the list of neighbors
			LinkedList<Region> neighbors = toRegion.getNeighbors();
			
			for(Region fromRegion:neighbors){
				if(fromRegion.ownedByPlayer(myName) == true && defense > 0){
					
					int attack = 0;
					int myArmies = armiesLeft[myRegions.indexOf(fromRegion)];
					int enemies = getNumberOfEnemies(fromRegion,opponentName);
					boolean enemyOwnership = toRegion.ownedByPlayer(opponentName);
					
					if( enemies != 0 && enemyOwnership == true)
						attack = myArmies - (int)(1.5*defense);
					else
						if(enemyOwnership == false && enemies!=0)
							attack = myArmies - (enemies/2 + 1);
						else
							if(enemyOwnership == false && enemies == 0 ){
								
								//assume it's not a wasteland
								attack = 4;
								
								//verify if it is a wasteland
								for(Region wasteland : state.getWasteLands()){
									
									if(wasteland.getId() == toRegion.getId()){
										attack = 10;
										break;
									}
								}
							}
					
					if(attack > 1 && myArmies - attack >= 1){
						
						attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, toRegion, attack));
						
						//it calculates the number of armies remained in the region
						armiesLeft[myRegions.indexOf(fromRegion)] -= attack;
					
						float[] avg = average(attack, defense);
						defense = defense - (int)avg[0];
					}
				}
			}
		}
		
		/* The transfer part starts. We choose to move the armies from the regions which are surrounded only by allies to its 
		 * neighbors which are endangered or surrounded by free regions. This way, we use most of our armies.
		 */
		for(Region fromRegion : state.getVisibleMap().getRegions()){

			//if the region is surrounded by allies, we can move armies from it
			if(fromRegion.ownedByPlayer(myName) == true && getNumberOfEnemyRegions(fromRegion,opponentName) == 0 && getNumberOfFreeRegions(fromRegion,state) == 0){
				
				Region region=null;
				int max=0;
				LinkedList<Region> neighbors = fromRegion.getNeighbors();
				
				//the most endangered neighbor is picked
				for(Region reg:neighbors){
				
					int enemies = getNumberOfEnemies(reg,opponentName);
					if(reg.ownedByPlayer(myName) && enemies>=max){
						
						max = enemies;
						region = reg;
					}
				}
				
				//if there isn't any neighbor near an enemy region, we pick the first region which is near a free region
				if(region == null){
					
					for(Region reg : neighbors){
						
						if(getNumberOfFreeRegions(reg, state)!=0){
						
							region=reg;
							break;
						}
					}
				}
				
				if(region != null && fromRegion.getArmies() != 1)
					attackTransferMoves.add(new AttackTransferMove(myName, fromRegion, region, fromRegion.getArmies()-1));
			}
		}

		return attackTransferMoves;
	}
	
	/**
	 * This method calculates the importance of the region.Its importance is given by the number of armies divided at the
	 * number of regions which are not owned by our player from the superRegion. If the region is owned by the opponent, 
	 * its importance is multiplied by 3.
	 * @param region the region to which we return the importance
	 * @param myName the name of the bot
	 * @param opponentName the name of the opponent
	 * @return the importance of the region
	 */
	public int getImportance(Region region, String myName, String opponentName){
		
		SuperRegion superRegion=region.getSuperRegion();
		LinkedList<Region> reglist = superRegion.getSubRegions();
		
		int count=0;
		int armies = region.getArmies();
		
		for(Region r:reglist)
			if(r.ownedByPlayer(myName)==false)
				count++;
		
		if (region.ownedByPlayer(opponentName)==true)
			armies = armies*3; 
		
		return armies/count;
	}

	
	/**
	 * This method returns a sorted list of the list of region given as parameter. The list is sorted in descending
	 * order from the most important region to the least important region using the method getImportance(). The regions
	 * owned by the opponent have priority in the list.
	 * @param regions the list which must be sorted
	 * @param myName the name of the bot
	 * @param opponentName the name of the opponent
	 * @return the sorted list
	 */
	public ArrayList<Region> sort(ArrayList<Region> regions, String myName, String opponentName){
		int n = regions.size();
		int [] importance = new int[n];
		
		for(int i = 0; i < n; i++)
			importance[i] = getImportance(regions.get(i), myName, opponentName);
		
		for(int i=0;i < n; i++)
			for(int j=i+1; j < n; j++)
		
				if( importance[i] < importance[j]){
				
					Region aux = regions.get(i);
					regions.set(i, regions.get(j));
					regions.set(j, aux);
					
					int auximp = importance[i];
					importance[i] = importance[j];
					importance[j] = auximp;
				}
		
		return regions;
	}
	
	/**
	 * The main method
	 * @param args
	 */
	public static void main(String[] args)
	{
		BotParser parser = new BotParser(new BotStarter());
		parser.run();
	}
}
