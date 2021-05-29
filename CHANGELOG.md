# Changelog

###5/29/2021 13:42
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