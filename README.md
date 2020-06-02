# intellij-blackconnect

![GitHub](https://img.shields.io/github/license/lensvol/intellij-blackconnect) ![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/14321-blackconnect) ![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/14321-blackconnect) 

Simple plugin for IDEA intended to help you to avoid overhead from starting [black](https://github.com/psf/black) process each time you save a Python file.

Instead, on each press of `Alt + Shift + B` plugin will send contents of the current Python file to the [blackd](https://black.readthedocs.io/en/stable/blackd.html) and replace them with the formatted version (if any changes were made at all).

## Features

* Automatic handling of Python type stubs (`.pyi`).
* Ability to trigger on each file save.
* Rough support for Jupyter Notebook files.
* Configurable options:
    * Hostname and port.
    * Preferred line length (default: 88).
    * Skipping sanity checks ("fast mode").
    * Skipping string normalization.
    * Target specific Python versions. 

## Installation

There are several ways to do this:

* Install stable version from [JetBrains Plugins Repository](https://plugins.jetbrains.com/plugin/14321-blackconnect)
* Be brave and try out "early access preview" version:
  * Open *"Preferences"*
  * Select *"Plugins"* tab
  * Click on the gear icon and choose *"Manage plugin repositories"*
  * Add [https://plugins.jetbrains.com/plugins/eap/14321](https://plugins.jetbrains.com/plugins/eap/14321) to the list of repositories
  * Find **BlackConnect** in the Marketplace, version should have suffix *"EAP-[timestamp]"*
* ...or just compile it straight from the source code:
  * Open this project in IDEA.
  * Open _Gradle_ side tab and run `buildPlugin` task.
  * After project is built, open your favorite IDEA-based IDE.
  * In **Preferences** window, choose *Plugins* pane and click on the gear icon.
  * Choose `Install Plugin from Disk` and point it to ZIP file in the `build/distributions` directory.

## TODOs

* Spawn **blackd** process if not started already.
* Load applicable **black** settings from `pyproject.toml`
* Make plugin properly [dynamic](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/dynamic_plugins.html).
* "Live reformatting".
* Automatic detection of applicable Python frameworks.
* Get line length from Python code style settings.
* Re-format only selected code fragment.

## Special thanks

* Andrey Vlasovskikh - goading me into publishing this plugin.
* Łukasz Langa - hinting at a proper font for the icon.
* Nazmul Idris - writing [awesome article](https://developerlife.com/2019/08/25/idea-plugin-example-intro/) about IDEA plugins.
* Joachim Ansorg - for the most detailed guide to writing IDEA plugins.
