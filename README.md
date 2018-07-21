# LockBlock

LockBlock provides universal, distributed, blockchain-based authentication.

## Overview

![Overview](https://raw.githubusercontent.com/nickbild/nickbild79_firebase/master/public/img/lockblock3.png)

![Overview](https://raw.githubusercontent.com/nickbild/nickbild79_firebase/master/public/img/block.png)

## Compilation

Place *.java files in a new directory, then run the command:

javac *.java

NOTE: org.sqlite.JDBC is required:

https://bitbucket.org/xerial/sqlite-jdbc

## Usage

To initiate a new blockchain (i.e. create the genesis block):

java LockBlock init <BLOCKCHAIN-FILE-NAME>

To add a new user to the block:

java LockBlock add <EXISTING-BLOCKCHAIN-FILE-NAME> <USERNAME> <USER-PUBLIC-KEY>

The public key must be RSA encrypted and in PEM format.  They can be generated with openssl, e.g.:

openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -pubout -in private_key.pem -out public_key.pem

To validate a blockchain:

java LockBlock validate <EXISTING-BLOCKCHAIN-FILE-NAME>

To challenge a user's authentication request:

java LockBlockAuth validate1 <EXISTING-BLOCKCHAIN-FILE-NAME> <USERNAME>

To validate a user's challenge response:

java LockBlockAuth validate2 <USERNAME> <ONE-TIME-PASSWORD>

After passing this test, the user has successfully authenticated.

For a user to generate a one time password from the server challenge:

java LockBlockLogin password <USER-PRIVATE-KEY> <CHALLENGE-TEXT>

## Further Information

https://nickbild79.firebaseapp.com/#!/lockblock


