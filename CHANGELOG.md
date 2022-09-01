<!-- Keep a Changelog guide -> https://keepachangelog.com -->
# BlackConnect Changelog

## [Unreleased]

## [0.5.0]
### Summary
- Well, that one was long overdue. The groundwork for starting **blackd** from inside the plugin was
done more than a year ago, but alas - mental health is a fickle thing and burnout is no laughing matter.
Funnily enough, it took being depressed from the ongoing world crisis to finally push me into
releasing this as a futile effort to allay my anxiety.
-  
- Anyways, here it is. Go play with it, have fun, and come back with helpful suggestions.
-      
- Stay safe. Stay sane.

### Changes
- Support starting <b>blackd</b> when the plugin starts.
- Lower IDE compatibility bound is now 2021.1.3. 

## [0.4.6]
### Summary 
- A small release to tide you over till bigger features ship.

### Changes
- Support 3.10 as a target version. (kudos to [Alex Opie](https://github.com/lxop))
- Fix broken link to **blackd** documentation in plugin description.

## [0.4.5]
- Support new `--skip-magic-trailing-comma` option.
- Support Python 3.9 as target version.
- Added "Trigger on Code Reformat" option (kudos to [Andrey Vlasovskikh](https://github.com/vlasovskikh)).
- Fix rare crash when saving non-Python files with Jupyter support enabled (kudos to [Elliot Waite](https://github.com/elliotwaite)).
- Fix for rare crash when updating document during Undo/Redo operation.

## [0.4.4]
### Summary 
- This release is dedicated to the memory of our cat **Luna**, who passed away due to cancer last year.
She was kind, smart and loyal. Best cat in the world.
- **We miss you, girl.**

### Changes
- Add button to copy line length settings from the IDE ("right margin").
- Support for connecting to blackd over SSL (kudos to [studioj](https://github.com/studioj))
- Make server error notifications more descriptive.
- Miscellaneous fixes and improvements.
