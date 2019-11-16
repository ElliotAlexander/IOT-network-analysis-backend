# GDP-Group-31-Backend

[![CircleCI](https://circleci.com/gh/ElliotAlexander/GDP-Group-31-Backend.svg?style=svg&circle-token=b58db770983c57865e5c66b2b18e083b59f28593)](https://circleci.com/gh/ElliotAlexander/GDP-Group-31-Backend)

This is the Java based backend for GDP Group 31. 

## IntelliJ

Note that the project comes bundled with a partially ignored .idea folder, as well as a .iml file. This is to allow a user to import the project into IntelliJ in one easy stroke (*in theory*). The project is built, tested and packaged using **maven**, the tooling for which should automatically be enabled inside IntelliJ (you can find this menu down the right hand side pane of the UI). This is probably the easiest way to get the project up and running, and allows you to run and debug the project in-IDE. That said, instructions to get the project running on the command line are below. 

## Running the project manually

### Installation

The project **must** run bare-metal on a system, due to the need for direct access to the hosts network adaptors. This won't work in docker. Because of this, we rely on Docker only for the build and testing part of the project, and bash scripts for everything else. If you're running on Windows, you might need to use Bash for windows, or write a basic Bat script to get this running. 

### Dependencies

The projects only dependency is **docker**. All other build tools are bundled inside the dockerfiles included. 


#### Build

To build the project, simply run `./bin/build.sh` from the project directory. This will build the project inside a docker container and leave the compiled files inside a /build/ folder in the project directory. 

#### Testing

To run project tests, use the `./bin/test.sh` script, again running the project inside a docker container. This script **won't** leave any leftover files, so you'll need to re-run build if you then want to run the project.

#### Running

To run the project, make sure you've run `./bin/build` first, then run `./bin/run.sh`. The project may be quite CPU consumptive, so ensure you don't leave it running in the background when you're not using it. 

