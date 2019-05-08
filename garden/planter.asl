// Agent planter in project garden.mas2j
/* Initial beliefs and rules */
/* Initial goals */
//!plant.
/* Plans */
/*
+!plant : needPlant 
	<- .print("planting");
		-needPlant[source(percept)];
		!plant.

+!plant : not needPlant <- !plant.
*/
+plant(X,Y) : true
	<- moveTo(X,Y);
		plant;
		-plant(X,Y)[source(_)].
