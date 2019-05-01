// Agent planter in project garden.mas2j

/* Initial beliefs and rules */

/* Initial goals */

!plant.

/* Plans */

+!plant : needPlant 
	<- .print("planting");	
		-needPlant[source(percept)];
		!plant.
+!plant : true <- !plant.

