// Agent sprinkler in project garden.mas2j
/* Initial beliefs and rules */
/* Initial goals */
/* Plans */
+water : true
	<- watering;
	-water[source(_)].
	
+extinguish : true
	<- extinguish;	
	-extinguish[source(_)];
	.send(observer, tell, extinguishFinished).
