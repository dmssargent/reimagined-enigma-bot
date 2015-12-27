# Reimagined Enigma Bot
A Reimagined Way to do Android bots

This system is divided into two parts: Reimagined and Enigma.

## Reimagined
This portion is the desktop and developer toolkit for Enigma clients, the Robot Controller (or just "Enigma") and the Driver Station ("Reimagined" or "Enigma Driver"). 

Reimagined allows for
- on-the-fly protocol inspection
- Robot Scripting
- Hardware functionality testing
- Custom testing
- Robot emulation (which platform is TBD)

## Engima
The Android system for robot control. Engima is the core for the robot controller. It allows for Android P2P communication, beside communciation with a Reimagined setup. This system relies heavily on Google Guava for the basic building blocks on the system.

### Robot Service
The Robot Service is the master controller for Enigma. It bootstraps and teardowns every other component, except the GUI.

## Simple Dag
This is the core dependency injection system for instation of any elements for when what constructor isn't known until runtime. This relies heavily on reflection, and is the only component that should be using reflection. The API is similar to Dagger, except for a few things.

### How to create instance of a class
```java
SimpleDag.create(klazz.class, this)
```
```this``` is used as a reference to the calling object in case that object knows that some components needed by the class are unknown by the predefined Providers. The requirement for any method in the reference is the same as in a provider. 

### What happens if the instance can't be created?
A ```SimpleDagException``` is thrown as a unchecked exception. A JSON array containing information is included if the resolution of how to create the instance couldn't be determined.

# Contributing
I want to know of any improvements, usablity, or any other failures or ideas that you come across feel free to open an issue.
If there is anything you want to help with, open a pull request.

## Requests for opening a Pull Request
- Only add functionality or modify functionality of a certain component (unless you open an issue to describe the reasoning for doing both in your pull request, and enough people like the idea to continue)
- Follow Google Java Style Guidelines in your contributions
- If you add a method, class, interface, or anything of that nature, document it
- If you just refactor a method, document the change you made in the method (you don't have to document any pre-existing methods or classes, it is my fault for the lack of documentation)
- Keep the code you right readable (be descriptive in the names of anything you write; if you throw an exception, don't say it failed, tell why it failed, being as descriptive as possible, and never saying "a thing", "a module", "a instance", and so forth, tell what "a thing" is)
- Write unit tests (either test new functionality or prevent regression of old, bad functionality; Rule 1 doesn't apply to this)
- Write secure code, and fix any security issues found

# License
Apache 2, see LICENSE.md for more info
