// Agent observer in project garden.mas2j
/* Initial beliefs and rules*/

/* Initial goal */

!start.
/* Plans */

+!start : needPlant 
	<- -needPlant[source(_)];
		!startPlanter;		
		!start.
+!start: needWeedSearch
	<- -needWeedSearch[source(_)];
		!startWeeders;
		!start.
+!start : needWatering 
	<- -needWatering[source(_)];
		!startSprinkler;		
		!start.	
+!start : needExtinguish 
	<- -needExtinguish[source(_)];	
	.send(sprinkler, tell, extinguish);
	!start.	
+!start : cleanAfterFire 
	<- -cleanAfterFire[source(_)];	
	.send(weeders, tell, clean);
	!start.	
		
+!start : true <-  !start.

+extinguishFinished : true
	<- checkForFire;
	-extinguishFinished[source(_)].	

+!startPlanter : not free(X,Y) & not discovered(Z,W) 
	<- ?pos(planter,M,N);
		discover(M,N).		

+!startPlanter : not free(Z,W) & discovered(X,Y) & not fullyDiscovered(X,Y)
	<- discover(X,Y).
		
+!startPlanter : free(X,Y) 
	<- .send(planter, tell, plant(X,Y));
		!start.	
+!startWeeders: not weedDiscovered(X,Y)
	<- searchWeed.
+!startWeeders : weedDiscovered(X, Y) 
	<- .send(weeders, tell, remove(X,Y));
		!start.
		
+!startSprinkler : not hasPlant(X,Y)
	<- searchPlants.
+!startSprinkler : hasPlant(X,Y)
	<- .send(sprinkler, tell, water);		
		!start.		
		
