AmberCraft
=
Real electricity simulation minecraft mod.

![alt text](/resources/AmberCraftWallPaper.png)

At the moment the mod is development to **minecraft 1.21.4**, but will be update to new versions.

#### Central ideas
* Simulate resistors, capacitors, indutores, diodes, and transistors in game.
* Simulate basics thermodynamics laws in game.
* To be survival-friendly.
* To be very modular, with many different options to do the same work.
* Don't have all in one machines[^1].
* No TIERS, all machines or material have positives and negatives, this isn't an unbreakable rule!

#### How to run?
* Clone the repository in your machine.
* Open it with IntelliJ IDEA and with the import finish.
* Run in the IDE terminal git checkout MNA.
* Run in the IDE terminal git submodule deinit libs/MNA
* Run in the IDE terminal git submodule add https://github.com/Welbre/CircuitSimulation.git libs/MNA
* Run the gradle neogradlew/runs/runClientData and the neogradlew/runServerData tasks.
* And last, run the gradle neogradle/runs/runClient to run the game.
obs: This process should be executed only one time.

[^1]: Machines that can do multiple functions in a single block without punishing the player in any way.
