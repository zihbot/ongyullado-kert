// Agent weeders in project garden.mas2j
/* Initial beliefs and rules */
/* Initial goals */
/* Plans */

+remove(X,Y) : true
	<- goTo(X,Y);
		remove;
		-remove(X,Y)[source(_)].

