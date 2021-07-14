# Changelog

### 7/14/2021 12:05

#### Implemented copy-paste-delete operation

* This operation is the safe, non-atomic move operation implemented beforehand, but can now be invoked explicitly.
* A sizeable amount of refactoring has been done to accommodate the changes, including name changes that are more
  descriptive.

#### Known bugs

* Due to priority being in the `TV` class, `Movie` is not receiving any development support at this time and therefore *
  may* be broken.
* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```
* There is a hard-to-reproduce bug that occurs when starting a `java.nio.Files` operation such as `move()` or `copy()`
  where the method may throw a `NoSuchFileException` regardless of whether the file is present. Furthermore, it may
  occur on only some files within a directory but not others.
	* This may be related with files being in different partitions or drives.

#### Priorities

1. Semi-automated testing with JUnit.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> **Future:*** Consider using other algorithms to speed up execution.
> > Using `String.contains()` and other string methods is not very optimal, but it is done repeatedly in the program. A possible improvement would be the [Boyer–Moore string-search algorithm](https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string-search_algorithm#Implementations).
>
> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 7/6/2021 9:54

#### Adjusted `organizedMove()`

* The method withing the class are now non-static, and it has a constructor.
	* This class will change in the future with the potential of separating operations into subclasses if the class gets
	  large enough.
	* `Main` has been adjusted accordingly.
* The `java.nio.Files` methods throw `NoSuchFileException`s when the parent directory of its target path are not
  created. By adding a `Files.createDirectories()` invocation on the path's parent, this issue has been resolved.

### 7/5/2021 22:20

#### Reimplemented `java.nio.Files.move()`

* After reading some documentation, the `NIO` package does provide operation safety with the `ATOMIC_MOVE` option.
* It has been reinstated, with a fallback to the `safeNonAtomicMove()` method upon receiving an I/O exception.

#### General

* Moved `String SPECIAL` of `TV` into `MetadataOps`
	* Namely, the unknown episode/special string.

* Minor refactoring.

#### Known bugs

* Due to priority being in the `TV` class, `Movie` is not receiving any development support at this time and therefore *
  may* be broken.
* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```
* There is a hard-to-reproduce bug that occurs when starting a `java.nio.Files` operation such as `move()` or `copy()`
  where the method may throw a `NoSuchFileException` regardless of whether the file is present. Furthermore, it may
  occur on only some files within a directory but not others.
	* This may be related with files being in different partitions or drives.

#### Priorities

1. Semi-automated testing with JUnit.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> **Future:*** Consider using other algorithms to speed up execution.
> > Using `String.contains()` and other string methods is not very optimal, but it is done repeatedly in the program. A possible improvement would be the [Boyer–Moore string-search algorithm](https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string-search_algorithm#Implementations).
>
> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 7/5/2021 18:53

#### Implemented file checksum validation

* This resides in `yjohnson/Checksum` and is hardcoded to use `MD5`.
* This class has been designed to "provide support" to file operations by adding checksum validation.
* Credit for large parts of this code go
  to [Réal Gagnon @ Real's How-to](https://www.rgagnon.com/javadetails/java-0416.html).

#### Expanded functionality in `Operations`

* New subclass: `MediaIOWrapper`.
	* It is meant to be a wrapper for `Media` objects that must undertake a move operation, providing a simpler way to
	  pass along, track, and store `Path` information.
* The method `moveMedia()` has been changed to `copyMedia()`.
	* The method has been explicitly designed to do copy operations only.
		* This allows an intermediate step to occur between moving files: checksum validation.
	* The highlight of this method is the ability to use the `Checksum` class to verify that files are equivalent.
	  > Because operations may take a long time, it is of utmost importance that the user's files are preserved throughout runtime. If the target directory is suddenly not reachable, full, or otherwise impaired, then the operation "fails" without compromising the user's files.

* Functionality has been abstracted from `organizedMove()`.

	* It now does a copy operation first and *then* deletes the source file, assuming the former succeeded.
		* This quite literally cripples per-file performance and doubles space complexity for the sake of safety, but I
		  may look into developing an "
		  unsafe" alternative in the future.
	* Source file deletion post-move is handled by `removeSourceFilePostMove()`.
	* `Media` path generation has been moved to the `Media` subclasses with a method
	  named `generateCustomPathStructure()`.

#### `Media` & `MediaQueue`

* Various method names have been changed to be more clear about their usage.
* `getMediaTitle()` retrieves the media's proper title or name.
	* For the currently implemented classes, they imply:
		- `TV` - The series' name (*not* the episode's name)
		- `Movie` - The movie's name
	* This was added, in part, to improve `MediaQueue.tallyNames()`.

* `generateCustomPathStructure()` takes in a `Path` parameter and returns what the specific `Media` subclass would "
  want" their file structure to be under the given parent path.

#### Known bugs

* Due to priority resting in the `TV` class, `Movie` is not receiving any development support at this time and
  therefore *may* be broken.
* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

#### Priorities

1. Semi-automated testing with JUnit.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> **Future:*** Consider using other algorithms to speed up execution.
> > Using `String.contains()` and other string methods is not very optimal, but it is done repeatedly in the program. A possible improvement would be the [Boyer–Moore string-search algorithm](https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string-search_algorithm#Implementations).
>
> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 7/5/2021 14:20

#### Slight changes

* Some minor refactoring.

### 7/2/2021 15:31

#### Further refinements to `TV`

* Abstracted and improved bracket removal functionality into its own method: `removeBrackets()`.
	* Added the ability to remove multiple brackets of the same type.
	* Now has logging.

* Improved `parseTVInfo()`.
	* It now features better redundancy for season and episode parsing.
		* Added a regular expression array that exclusively matches episode and season information from a file name that
		  does not match the ones in `regexSeasonAndEp`.
		  ```java
			  private static final Pattern[] regexSoloEp = {
				  Pattern.compile(" *- ? *(?<epInd>E?(?<episode>0*([1-9][0-9]*|0)))"),
				  Pattern.compile("(?<epInd>E?(?<episode>0*([1-9][0-9]*|0)))$")
			  };
			  private static final Pattern[] regexSoloSeason = {
				  Pattern.compile("(?i)(?<seasonInd>Season ?\\b(?<season>0*([1-9][0-9]*|0)))"),
				  Pattern.compile("(?i)(?<seasonInd>S(?<season>0*([1-9][0-9]*|0))) +"),
			  };
			```
		* Similarly, season information also gets parsed from the file's name, bu tin the event that none is found, it
		  will also search in the parent directory.
	* If the method fails to parse properly, it will default to legacy functionality (the super slow one).
	* Two helper methods have been added: `matchEpisodeOnly()` and `matchSeasonOnly()`.

#### Known bugs

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

#### Priorities

1. Semi-automated testing with JUnit.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> **Future:*** Consider using other algorithms to speed up execution.
> > Using `String.contains()` and other string methods is not very optimal, but it is done repeatedly in the program. A possible improvement would be the [Boyer–Moore string-search algorithm](https://en.wikipedia.org/wiki/Boyer%E2%80%93Moore_string-search_algorithm#Implementations).
>
> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.
>

### 7/1/2021 14:35

#### `TV` now parses with regular expressions

* When extracting information from file names such as season and episode number, the new method `parseTVInfo()` will
  first use regular expressions before defaulting to previous methods.
	* There are currently three regular expressions in use, dedicated to different naming formats. They are able to
	  parse most naming conventions though not all.
	  ```java
			private static final Pattern[] regexSeasonAndEp = {
				Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>S(?<season>0*[1-9][0-9]*|0)) *(?<epInd>E(?<episode>0*([1-9][0-9]*|0)))"),
				Pattern.compile(
						"(?i)(?:(?: *- ?)? ?(?<seasonInd>Season ?\\b(?<season>0*([1-9][0-9]*|0))))? *(?: *- ?)? *(?<epInd>Episode ?\\b(?<episode>0*([1-9][0-9]*|0)))"),
				Pattern.compile("(?i)(?: *- ?)? ?(?<seasonInd>(?<season>0*([1-9][0-9]*|0))) *x *(?: *- ?)? *(?<epInd>(?<episode>0*([1-9][0-9]*|0)))")
		 };
		```

#### Created `CLI` class

* It is where all command-line interactions will be moved to and handled. The goal is to have the rest of the code not
  depend on either it, or `ConsoleEvent`.

* Multiple code segments have been moved from `Main` into this class.

#### The `moveMedia()` operation will no longer overwrite

* In case of any unprecedented operations that may lead to data-loss, the operation no longer has the `REPLACE_EXISTING`
  flag.
* This will likely be reimplemented in a more robust manner.

#### Known bugs

* ~~The `seasons()` method of the `TV` class does not accurately parse season information from the file's name if the
  season is larger than 9. It is possible that it reads the first number and believes it is done, thereby ignoring the
  second digit and misrepresenting the output season. This can lead to the program overwriting it's own output and even
  data loss.~~
  ![image](https://user-images.githubusercontent.com/62960501/124062581-8ca8ff80-d9f6-11eb-837b-08bb0020fd21.png)
	* This issue has been addressed. Unless the naming format is largely unconventional, the information will be parsed
	  correctly. Additionally, the program is now unable to overwrite files.

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

* ~~Series name is not being properly parsed.~~
	* ~~When both `- E00` and `S01E00` notation are used, the resulting string may keep the last unnecessary dash as a
	  leftover. This generates incorrect series names and worsens the resulting custom file names.~~
	  ![image](https://user-images.githubusercontent.com/62960501/124062820-09d47480-d9f7-11eb-8bd2-9f7c120fe684.png)
		* This issue has been addressed with the regex implementations.

#### Priorities

1. ~~Add logging to all classes and methods that would benefit from it.~~
2. Allow for more granular user input and refine user experience.
	- Modifying created media files post-extraction.
	- Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.
>

### 6/30/2021 23:15

#### General

* `MediaQueue`'s `File destinationDir` private variable and the `Path target` parameter have been removed.
	* The queue does not need to know where the files are going for any of its operations.
* `ConsoleEvent.askUserForMediaType()`'s parameter `String dir` has been removed.
* `Main` has been reorganized; the prompt for a destination directory only occurs after the user confirms that the queue
  looks OK and before it starts doing an organized move.
* `Operations` now has a logger and does not utilize `ConsoleEvent` anymore.

#### Known bugs

* The `seasons()` method of the `TV` class does not accurately parse season information from the file's name if the
  season is larger than 9. It is possible that it reads the first number and believes it is done, thereby ignoring the
  second digit and misrepresenting the output season. This can lead to the program overwriting its own output and even
  data loss.
  ![image](https://user-images.githubusercontent.com/62960501/124062581-8ca8ff80-d9f6-11eb-837b-08bb0020fd21.png)

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

* Series name is not being properly parsed.
	* When both `- E00` and `S01E00` notation are used, the resulting string may keep the last unnecessary dash as a
	  leftover. This generates incorrect series names and worsens the resulting custom file names.
	  ![image](https://user-images.githubusercontent.com/62960501/124062820-09d47480-d9f7-11eb-8bd2-9f7c120fe684.png)

#### Priorities

1. Add logging to all classes and methods that would benefit from it.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Explore possibilities of different interfaces, such as:
	- semi-automated, headless operation
	- GUI
	- command line arguments
	- website

4. Reimplement "history" output that summarizes all operations into a text file.

> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 6/30/2021 15:41

#### New branch: *cli-abstraction*

#### `ConsoleEvent` has been removed from all major classes

* `ConsoleEvent` will be used with the project, primarily within CLI applications.
* This abstraction had reverberating effects throughout the project, all of which are notable.

#### `MediaQueue`

* Now located in the `media` package.
* The constructor now has four parameters:
	- The source directory of the files to search for.
	- The extension to filter with.
	- The destination directory.
		- This may be removed in favor of a postponed declaration.
	- The type to assign them as.

* The arguments are checked before code execution for "validity" and will throw an `IllegalArgumentException()` if they
  are not valid.
* Previous operations that prompted a program close through `ConsoleEvent` now log at the `ERROR` level.

#### `MediaType`

* Renamed from `MediaTypes`.
* The enum now features the following method.
  ```java 
  public abstract Media instantiate (Path path);
  ```
	* This method must be overridden by every future `MediaType`.
	* The purpose of this change is to guarantee that every future `MediaType` possesses a way to instantiate its
	  corresponding class, rather than having to maintain multiple areas of code whenever new ones are added.
		* It now has a logger to be primarily used whenever a given path does not correspond to a file.

#### `Main`

* `ConsoleEvent` functionality from `MediaQueue` has been moved into a new method:
  ``` java
  private static void userMediaQueueCLI (); 
  ```

#### Known bugs

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

#### Priorities

1. Add logging to all classes and methods that would benefit from it.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Abstract command line operations from regular program operations.
	1. ~~Remove all traces of `ConsoleEvent` from the classes that do not need it.~~
	2. ~~Move all of it to a dedicated CLI-to-program translation method/class.~~
	3. Explore possibilities of different interfaces, such as:
		- semi-automated, headless operation
		- GUI
		- command line arguments
		- website

4. Reimplement "history" output that summarizes all operations into a text file.

> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 6/29/2021 22:15

#### General

* `Media` has a new abstract method.
	* `isValid()` is a method whose main purpose is to determine whether every critical part of the object is "
	  well-formed".
		* Subsequent operations that need the object should invoke the method to prevent a critical exception.
* `TV` has improved functionality
	* Improved episode parsing and recognition by adding a regex group named `seq`.
		* The matched string from this group is removed from the working filename.
			* This implementation also improves overall code legibility.
* `MediaList` (the Class) has been moved into `MediaQueue.java` (the file)
	* This is because `MediaList` is only ever used in the context of an overarching `MediaQueue`.
* More methods have had logging added.

#### Known bugs

* ~~Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.~~
	* It occurred due to MediaList objects being added to the MediaQueue regardless of whether they had anything in
	  them.
	* So, if a directory were to not contain any matching files, the list would be empty. MediaList deduces its name
	  variable from the files it contains. Because no files are present, the name is left null, and the program resumes
	  normal operation.

	* By adding an `isEmpty()` check on the internal data structure, `MediaQueue` will only add a `MediaList` when the
	  aforementioned returns false.

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

#### Priorities

1. Add logging to all classes and methods that would benefit from it.
2. Allow for more granular user input and refine user experience.
	1. Modifying created media files post-extraction.
	2. Allow for users to dictate what goes together in the same folders.

3. Abstract command line operations from regular program operations.
	1. Remove all traces of `ConsoleEvent` from the classes that do not need it.
	2. Move all of it to a dedicated CLI-to-program translation method/class.
	3. Explore possibilities of different interfaces, such as:
		- semi-automated, headless operation
		- GUI
		- command line arguments
		- website

4. Reimplement "history" output that summarizes all operations into a text file.

> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from
> files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or
> programmatic organization.

### 6/27/2021 10:52

#### General

* Improved logging implementation with `SLF4J`.
	* Using `SLF4J` allows for dynamic logging implementations that do not tie up the entire codebase to a singular
	  framework. This makes future-proofing the code easier and enhances interoperability.
		* When compiling, `SLF4J` must be added to the `-classpath` argument alongside the logging library of choice.
			* `log4j` was the one chosen for development.
			* More information can be found on [SLF4J's site](http://www.slf4j.org/manual.html).

	* Logging is output to a `log.out` file in the working directory.
	* Added some logging functionality to various classes.
		* `TV`, `Main`, `MetadataOps`, and `ConsoleEvent` have had logging implemented or improved.
			* `ConsoleEvent` has been thoroughly reworked to accommodate these changes, and `Logging` (the Class) has
			  been removed.
		* All classes will have logging implemented in the future.

* Logs are no longer output on the console.
* `HandBrake` (the Class) has been removed.
	* The implementation of this class was built on the idea of a single-minded and monolithic program. As a result, it
	  clashes with the direction of the overall program.
	* The implementation of similar functionality is being considered but is not a priority; the stability and
	  reliability of what's currently being worked on takes priority.

#### Known bugs

* Processing a directory with files that are not of the same extension as the user input will result in a ```null```
  being output as a processed file. The program does not crash, however.

* After answering the prompt to add more files to the queue, the program registers an "Invalid directory" error before
  allowing user input.
  ``` java
  Input the destination directory: 	// First request, unable to respond
  Input the destination directory:	// Second request, able to respond
  Invalid directory.			// This one appears as a response to the first request for a directory.
  /* USER ENTRY HERE */			// This would respond to the second request
  ```

> #### Priorities
>
> 1. Allow for more granular user input.
>
> 2. Refine user experience.
>
> 3. Abstract command line operations from regular program operations.
>
> 4. ~~Implement proper logging support.~~ Add logging to all classes and methods that would benefit from it it.
>
> 5. Reimplement "history" output that summarizes all operations into a text file.
>
> ***Future:*** Implement *ffmpeg*'s *ffprobe* functionality to qet more reliable and abundant metadata information from files.
> > This library may be of use here: [FFmpeg Java](https://github.com/bramp/ffmpeg-cli-wrapper)
>
> ***Future:*** Implement online API media verification for additional metadata or corrections.
>
> ***Future:*** Make the program semi-automated with the usage of command line arguments to allow for scheduled or programmatic
> organization.

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