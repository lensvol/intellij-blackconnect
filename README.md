# ![logo](https://raw.githubusercontent.com/lensvol/intellij-blackconnect/master/_static/readme_logo.svg) intellij-blackconnect
 
[![Plugin version](https://img.shields.io/jetbrains/plugin/v/14321-blackconnect)](https://plugins.jetbrains.com/plugin/14321-blackconnect/versions) 
[![Total downloads](https://img.shields.io/jetbrains/plugin/d/14321-blackconnect)](https://plugins.jetbrains.com/plugin/14321-blackconnect) 
[![Plugin rating](https://img.shields.io/jetbrains/plugin/r/rating/14321-blackconnect)](https://plugins.jetbrains.com/plugin/14321-blackconnect/reviews)
[![Build status](https://github.com/lensvol/intellij-blackconnect/workflows/build/badge.svg)](https://github.com/lensvol/intellij-blackconnect/actions?query=workflow%3Abuild) 
[![Code Coverage](https://codecov.io/gh/lensvol/intellij-blackconnect/branch/master/graph/badge.svg)](https://codecov.io/gh/lensvol/intellij-blackconnect)
[![License](https://img.shields.io/github/license/lensvol/intellij-blackconnect)](https://github.com/lensvol/intellij-blackconnect/blob/master/LICENSE)

**NB**: *If you want to see this plugin as an official way to use **black** with PyCharm - feel free to voice your support in [this Pull Request](https://github.com/psf/black/pull/3150)!*

Simple plugin for IDEA intended to help you to avoid overhead from starting [black](https://github.com/psf/black) process each time you save a Python file.

Instead, on each press of `Alt + Shift + B` plugin will send contents of the current Python file to the [blackd](https://black.readthedocs.io/en/stable/blackd.html) and replace them with the formatted version (if any changes were made at all).

## Features

* Ability to trigger on each file save.
* Integration with the "Reformat Code" function.
* Start <b>blackd</b> when IDE opens.
* Automatic handling of Python type stubs (`.pyi`).
* Rough support for Jupyter Notebook files.
* Ability to reformat selected fragment only instead of whole file.
* Load formatting settings from **[tool.black]** section of `pyproject.toml`
* Connect to **blackd** over SSL if needed (e.g. blackd behind nginx)
* Configurable options:
    * Hostname, port and https.
    * Preferred line length (default: 88).
    * Skipping sanity checks ("fast mode").
    * Skipping string normalization.
    * Processing of "magic trailing comma" in collections.
    * Target specific Python versions.

## Installation

**NB:** If you plan on triggering reformat on each save, please make sure to disable features in other plugins (e.g. [Save Actions](https://plugins.jetbrains.com/plugin/7642-save-actions)) which may trigger standard "Reformat code" action. 

Also, please make sure to disable **black** in "Settings / Tools / File Watchers" if you installed it previously using [the example from documentation](https://black.readthedocs.io/en/stable/editor_integration.html).

#### Stable version

Just go to the [plugin page](https://plugins.jetbrains.com/plugin/14321-blackconnect) in *JetBrains Plugins Repository* and press `Install to IDE`.

#### Development Snapshot

...or just compile it straight from the source code:

* Open this project in IDEA.
* Open _Gradle_ side tab and run `buildPlugin` task.
* After project is built, open your favorite IDEA-based IDE.
* In **Preferences** window, choose *Plugins* pane and click on the gear icon.
* Choose `Install Plugin from Disk` and point it to ZIP file in the `build/distributions` directory.

## TODOs

* Make plugin properly [dynamic](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/dynamic_plugins.html).
* "Live reformatting".
* Automatic detection of applicable Python frameworks.
* Detection of possible conflicts with other plugins / File Watchers.
* Better integration with Jupyter Notebooks.

## Contributing

Development requirements:

* Java SDK 11 or newer
* Python 3.8 or newer
* Poetry

Run tests:

```shell
./gradlew test
```

Launch a testing version of PyCharm with the plugin enabled:

```shell
./gradlew runIde
```

## Special thanks

* [Andrey Vlasovskikh](https://github.com/vlasovskikh) - goading me into publishing this plugin.
* [Łukasz Langa](https://github.com/ambv) - hinting at a proper font for the icon.
* [Nazmul Idris](https://github.com/nazmulidris) - writing [awesome article](https://developerlife.com/2020/11/21/idea-plugin-example-intro/) about IDEA plugins.
* [Joachim Ansorg](https://github.com/jansorg) - for the [most detailed guide](https://www.plugin-dev.com/intellij/) to writing IDEA plugins I could find.
