# Changelog

### 6/1/2021 15:03

#### Major changes to ```MediaQueue.java```.

* ```MediaQueue``` now separates the different collection of files into ```MediaList```s.

* Each list will call  ```PathFinder.findFiles()``` upon instantiation to populate itself. This allows for the
  overarching queue to feature various media types from various directories and process them independently of each
  other.

* ```MediaQueue``` is being shifted into a non-static implementation.
* This will make it easier to implement multiple queues in the future by taking advantage of Java's object-oriented
  nature.
* This also necessitates the reorganization and further abstraction of the various systems that used
  the ```MediaQueue```.

#### Priorities

1. Fix ```MediaQueue``` and all ```Media``` subtypes.
	* Their implementation should not rely on each other.

	* ```MediaQueue``` should, at most, be reliant on ```PathFinder``` and ```ConsoleEvent```.

	* ```Media``` should not output anything to the command line nor should it have to worry about global variables
	  in ```Main```.
		* Using ```MediaList``` as the holster for metadata which ```Media``` would want in the future is an option.

2. Fix metadata logic for ```Media``` subtypes.

3. Ensure program can do basic operations such as batch rename and move.

4. Allow for more granular user input.

> #### To-Do
>* Work towards fully removing the dependency on legacy HandBrake compatibility code to extend program utility and
   > versatility.
>* Refactor most of the program logic to make it more structured and clean.
>* Add JavaDocs for all methods.

Note: HandBrake functionality has been commented out for debugging purposes.

### 5/29/2021 13:42

Further improvements being made to code abstraction and logic.

* ```PathFinder.java``` was moved from package ```filesystem``` to package ```yjohnson```.
	* Package ```general``` underwent minor changes that relate to this refactoring, plus some minor reorganization.

* ```MediaQueue.java``` was majorly reworked to improve the logic that precedes file searching.
	* Of note, a repeated recursive call of ```PathFinder.findFiles()``` was found, which essentially duplicated running
	  time of that operation.

* ```TV.java``` has had its reliance on ```HandBrake.java``` removed. It now relies on ```Main.java``` as a stop-gap.

To-Do:

* Work towards fully removing the dependency on legacy HandBrake compatibility code to extend program utility and
  versatility.
* Refactor most of the program logic to make it more structured and clean.
* Add JavaDocs for all methods.

### 5/28/2021 16:07

Polished different portions of the program to reflect better programming practices.

* Added a built-in `````trim()````` to the messages printed by ConsoleEvent.
* Started to reduce HandBrake reliance in an effort to decouple it from Relocatinator.

To-Do:

* Work towards fully removing the dependency on legacy HandBrake compatibility code to extend program utility and
  versatility.
* Refactor most of the program logic to make it more structured and clean.
* Add JavaDocs for all methods.