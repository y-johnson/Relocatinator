# Changelog

### 6/27/2021 10:52

#### General
* Reorganized `TV.java`
	* Added regex functionality into `episodes()` to allow for more refined episode detection.
* Added additional queueing functionality to `MediaQueue.java`
	* It will now prompt for additional files to queue.

#### Known bugs

* Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.

> #### Priorities
>
> 1. Allow for more granular user input.
>
> 2. Refine user experience.
>
> 3. Abstract command line operations from regular program operations.
>
> 4. Implement proper logging support.
>
> 5. Reimplement "history" output that summarizes all operations into a text file.
>
> *Future:* Implement online API media verification for additional metadata or corrections.
>
> *Future:* Make the program semi-automated with the usage of command line arguments to allow for scheduled or programmatic
> organization.

### 6/8/2021 17:44

#### Implemented `Operations.organizedMove`

* Name is a work-in-progress.
* Essentially, it handles the basic filesystem operation of moving a file but with the added functionality afforded by
  the `Media` subclasses.
	* Move operations are handled by Java's `Files.move()` method.
	* The options `ATOMIC_MOVE` and `REPLACE_EXISTING` are enabled.

#### Updated `MetadataOps.java` with regular expressions

* By using regex, the process of matching resolution can be expanded on to allow for a wider pool of comparisons.
	* As a result, it is now possible to match the following formats for the three most common resolutions (1080p, 720p,
	  480p):
		- 1920x1080
		- 1920 x 1080
		- 1920x 1080p [The spacing is correct]
		- 1080p
		- 1080

#### `Media` no longer extends `File` and now includes type information

* This was changed to allow for more versatility with `Media` objects.
	* Due to `File` objects being immutable, there would be overhead in reassigning a current `Media` object to another
	  path due to it needing a new instantiation.
	* To compensate, `Media` now own a File object which can be changed and retrieved with `setFile()` and `getFile()`.
	* Additionally, implementation for `MediaTypes type` storage in `Media` objects has been finally added.
* All classes that utilized this functionality have been updated to use the getter.

#### General

* `MediaList` now has proper support for `Movie` objects when deducing a list name.
* `Main.java` has added the aforementioned move operation support to its pipeline.
* `MediaQueue.stringOfContents()` is now more visually pleasing and has added information regarding the source file.

#### Known bugs

* Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.

* Some file names may include trigger phrases for `TV.episodes()` in the format of `E[digit][digit]` and can create
  erroneous custom names that do not follow the user's expectations.
	* Migrating `TV.seasons()` and `TV.episodes` to regex would most certainly fix this issue.

> #### Priorities
>
> ~~Ensure program can do basic operations such as batch rename and move.~~
>
> 1. Allow for more granular user input.
>
> 2. Refine user experience.
>
> 3. Abstract command line operations from regular program operations.
>
> 4. Implement proper logging support.
>
> 5. Reimplement "history" output that summarizes all operations into a text file.
>
> *Future:* Implement online API media verification for additional metadata or corrections.
>
> *Future:* Make the program semi-automated with the usage of command line arguments to allow for scheduled or programmatic
> organization.

### 6/4/2021 10:05

#### Removal of ```GenericVideo.java```

* The initial goal for ```GenericVideo``` does not align with the direction of the program. Upon the implementation of
  user modification, the use case for generic video can be superseded by proper use of the ```Movie``` object.
	* Other minor changes have been made throughout the code to accommodate for this.

* This also means that the only two ```Media``` subclasses are ```TV``` and ```Movie```.
	* Development will proceed with these two in mind, but others, such as ```Music``` can be implemented at a later
	  time.

#### Known bugs

* Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.

#### Priorities

~~Fix all ```Media``` subtypes.~~

~~Fix metadata logic for ```Media``` subtypes.~~

1. Ensure program can do basic operations such as batch rename and move.

2. Allow for more granular user input.

3. Refine user experience.

4. Abstract command line operations from regular program operations.

5. Implement proper logging support.

6. Reimplement "history" output that summarizes all operations into a text file.

*Future:* Implement online API media verification for additional metadata or corrections.

### 6/3/2021 22:37

#### Reimplementation of ```Movie.java```

* The ```Movie``` class has been remade to match its related classes.
	* As with ```TV```, a large portion of how it gets information from its name has been streamlined and simplified.
		* Many parts of the code have been changed to allow for a more polymorphic implementation.

#### General

* Minor changes to ```TV.java``` and ```MediaList.java``` to accommodate ```Movie.java```.
* There is an argument to be made about processing each individual movie as a ```MediaList``` inside the queue;
  presumably, it will allow for a simpler implementation of multi-part movies or movies that have similar metadata in
  their name.

#### Known bugs

* Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.

#### Priorities

1. Fix all ```Media``` subtypes.
	* Their implementation should be completely detached from the rest of the program.

	* ```Media``` should not output anything to the command line nor should it have to worry about global variables
	  in ```Main```.
		* Using ```MediaList``` as the holster for metadata which ```Media``` would want in the future is an option.

2. Fix metadata logic for ```Media``` subtypes.

3. Ensure program can do basic operations such as batch rename and move.

4. Allow for more granular user input.

### 6/2/2021 17:51

#### Reimplementation of ```TV.java```

* The ```Media``` and ```TV``` classes have been remade to support the new queue system.
	* They only depend on their parent class, ```File```.

* ```TV``` object creation has been heavily streamlined.
	* ```TV``` objects are still capable of extracting information from their filename.
		* This has been improved to support a wider variety of indicators.
	* ```String[] metadata``` has been removed as it does not provide any innate advantage over individual variables and
	  reduces clarity.

#### General

* ```Subtitles.java``` and ```MediaHistory.java``` have been removed.
	* They provide functionality which could be of great use later on, but their implementation must be refined
	  alongside the program. Because they are not essential, they have been removed to simplify the core aspects of the
	  program.

* ```MediaQueue``` and ```MediaList``` now implement ```Iterable```.
	* They only call upon their underlying data structure's iterator, however. This functionality is sufficient right
	  now.

#### Priorities

1. Fix all ```Media``` subtypes.
	* Their implementation should be completely detached from the rest of the program.

	* ```Media``` should not output anything to the command line nor should it have to worry about global variables
	  in ```Main```.
		* Using ```MediaList``` as the holster for metadata which ```Media``` would want in the future is an option.

2. Fix metadata logic for ```Media``` subtypes.

3. Ensure program can do basic operations such as batch rename and move.

4. Allow for more granular user input.

### 6/1/2021 20:02

#### Even more major changes to ```MediaQueue.java```

* Finished the refactor for the queue system.
	* It can, theoretically, queue up various lists of ```Media``` subtypes, and said lists possess the source directory
	  of the files, their extension, and their (collective) destination.
		* This means that it would be possible to queue up different lists of files that go to different destinations
		  and have their own extensions.
	* It has been ported to a non-static implementation to allow for multiple instantiations.
	* It still has room for improvement as development progresses, but the core of it does not need to be changed for
	  now.

#### General

* Multiple files have had minor additions and changes to improve functionality.

#### Priorities

1. Fix all ```Media``` subtypes.
	* Their implementation should be completely detached from the rest of the program.

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