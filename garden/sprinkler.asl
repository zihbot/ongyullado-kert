// Agent sprinkler in project garden.mas2j

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

+water(X,Y) : true
	<- watering;
	-water(X,Y)[source(_)].

