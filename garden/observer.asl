// Agent observer in project garden.mas2j
/* Initial beliefs and rules*/

/* Initial goal */

!start.
/* Plans */

+!start : needPlant 
	<- -needPlant[source(_)];
		!startPlanter;
		!start.
		
+!start : true <- !start.

+!startPlanter : not free(X,Y) & not discovered(Z,W) 
	<- ?pos(planter,M,N);
		discover(M,N).

+!startPlanter : not free(Z,W) & discovered(X,Y) 
	<- discover(X,Y).
		
+!startPlanter : free(X,Y) 
	<- .send(planter, tell, plant(X,Y));
		-free(X,Y)[source(_)];
		!start.
