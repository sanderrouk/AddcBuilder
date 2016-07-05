# README #

This program is built using Java and uses SSH to connect to an Ubuntu server version 16.04 and then builds it into an Active Directory Domain Controller using Samba 4.

### Project Structure ###
The project uses the gradle wrapper to get the dependencies from the maven repository. 
The ADDC build logic can be found in the Logic directory,
The Interface controller is in the controller directory.


### How do I get set up? ###
On usage and set up consult the [wiki](../../wiki/Home), alternatively you can compile the package using the gradle wrapper by running 
```gradle
gradlew :distZip
```
Or just run the program by running 
```gradle
gradlew :run
```