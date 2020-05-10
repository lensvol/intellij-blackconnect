# intellij-blackconnect

![GitHub](https://img.shields.io/github/license/lensvol/intellij-blackconnect)

Simple plugin for IDEA intended to help you to avoid overhead from starting [black](https://github.com/psf/black) process each time you save a Python file.

Instead, on each press of `Alt + Shift + B` plugin will send contents of the current Python file to the [blackd](https://black.readthedocs.io/en/stable/blackd.html) and replace them with the formatted version (if any changes were made at all).

## Features

* Automatic handling of Python type stubs (`.pyi`)
* Configurable options:
    * Hostname and port
    * Preferred line length (default: 88)

## Installation

1. Open this project in IDEA.
2. Choose `Prepare Plugin Module 'intellij-blackconnect' for Deployment` in the **Build** menu.
3.  In **Preferences** window, choose *Plugins* pane and click on the gear icon.
4. Choose `Install Plugin from Disk` and point it to *intellij-blackconnect.jar* in the root directory.
5. Enjoy!

## TODOs

* Publish it in the [JetBrains Plugin Repository](https://plugins.jetbrains.com/)
* Use Gradle instead of Pludin DevKit.
* Make plugin properly [dynamic](https://www.jetbrains.org/intellij/sdk/docs/basics/plugin_structure/dynamic_plugins.html).
* More configuration options.
* Hook into file saving logic.
* Use background thread instead of UI thread for HTTP call.
* Automatic detection of applicable Python frameworks.
* Spawn **blackd** process if not started already.

## Special thanks

* Andrey Vlasovskikh - goading me into publishing this plugin.
* Łukasz Langa - hinting at a proper font for the icon.
* Nazmul Idris - writing [awesome article](https://developerlife.com/2019/08/25/idea-plugin-example-intro/) about IDEA plugins.
