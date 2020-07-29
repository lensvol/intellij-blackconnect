# ![logo](https://raw.githubusercontent.com/lensvol/intellij-blackconnect/master/_static/readme_logo.svg) intellij-blackconnect

[![GitHub](https://img.shields.io/github/license/lensvol/intellij-blackconnect)](https://github.com/lensvol/intellij-blackconnect/blob/master/LICENSE) [![JetBrains IntelliJ Plugins](https://img.shields.io/jetbrains/plugin/v/14321-blackconnect)](https://plugins.jetbrains.com/plugin/14321-blackconnect/versions) [![JetBrains IntelliJ plugins](https://img.shields.io/jetbrains/plugin/d/14321-blackconnect)](https://plugins.jetbrains.com/plugin/14321-blackconnect) [![build](https://github.com/lensvol/intellij-blackconnect/workflows/build/badge.svg)](https://github.com/lensvol/intellij-blackconnect/actions?query=workflow%3Abuild) [![codecov](https://codecov.io/gh/lensvol/intellij-blackconnect/branch/master/graph/badge.svg)](https://codecov.io/gh/lensvol/intellij-blackconnect)

Simple plugin for IDEA intended to help you to avoid overhead from starting [black](https://github.com/psf/black) process each time you save a Python file.

Instead, on each press of `Alt + Shift + B` plugin will send contents of the current Python file to the [blackd](https://black.readthedocs.io/en/stable/blackd.html) and replace them with the formatted version (if any changes were made at all).

## Features

* Automatic handling of Python type stubs (`.pyi`).
* Ability to trigger on each file save.
* Rough support for Jupyter Notebook files.
* Ability to reformat selected fragment only instead of whole file.
* Load formatting settings from **[tool.black]** section of `pyproject.toml`
* Configurable options:
    * Hostname and port.
    * Preferred line length (default: 88).
    * Skipping sanity checks ("fast mode").
    * Skipping string normalization.
    * Target specific Python versions.

## Installation

#### Stable version

Just go to the [plugin page](https://plugins.jetbrains.com/plugin/14321-blackconnect) in *JetBrains Plugins Repository* and press `Install to IDE`.

#### Early Access Preview

Be brave and try out "early access preview" version:
* Open *"Preferences"*
* Select *"Plugins"* tab
* Click on the gear icon and choose *"Manage plugin repositories"*
* Add [https://plugins.jetbrains.com/plugins/eap/14321](https://plugins.jetbrains.com/plugins/eap/14321) to the list of repositories
* Find **BlackConnect** in the Marketplace, version should have suffix *"EAP-[timestamp]"*

#### Development Snapshot

...or just compile it straight from the source code:

* Open this project in IDEA.
* Open _Gradle_ side tab and run `buildPlugin` task.
* After project is built, open your favorite IDEA-based IDE.
* In **Preferences** window, choose *Plugins* pane and click on the gear icon.
* Choose `Install Plugin from Disk` and point it to ZIP file in the `build/distributions` directory.

## TODOs

* Spawn **blackd** process if not started already.
* Make plugin properly [dynamic](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/dynamic_plugins.html).
* "Live reformatting".
* Automatic detection of applicable Python frameworks.
* Get line length from Python code style settings.

## Special thanks

* [Andrey Vlasovskikh](https://github.com/vlasovskikh) - goading me into publishing this plugin.
* [Łukasz Langa](https://github.com/ambv) - hinting at a proper font for the icon.
* [Nazmul Idris](https://github.com/nazmulidris) - writing [awesome article](https://developerlife.com/2019/08/25/idea-plugin-example-intro/) about IDEA plugins.
* [Joachim Ansorg](https://github.com/jansorg) - for the [most detailed guide](https://www.plugin-dev.com/intellij/) to writing IDEA plugins I could find.
