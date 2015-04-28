# Proxeur
Proxeur is a very simple Java proxy server

Its behaviour is not much different from other simple Java proxy server that you find on the Internet.
Except that it is written a bit more object-oriented instead of a monolithic class.

Shared functionality is in an abstract class instead of method calls.
Both the client and server classes inherits from this abstract class.
The principle of single-responsibility is used that is, each class does one thing only.

