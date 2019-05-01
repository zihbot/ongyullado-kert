// Agent observer in project garden.mas2j
/* Initial beliefs and rules*/

/* Initial goal */

!start.
/* Plans */
+!start : needPlant 
	<- !!startPlanter;
		-needPlant[source(_)];
		!start.
		
+!start : true <- !start.

+!startPlanter : not free(X,Y) & not discovered(Z,W) 
	<- ?pos(planter,M,N);
		discover(M,N).
	
-!startPlanter : free(X,Y) 
	<- .send(planter, tell, plant(X,Y));
		-free(X,Y)[source(_)];
		!start.
