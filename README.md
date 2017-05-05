# xman
A simple facade for working with XPath in Java. It is intended to make automated testing of XML transformations code easier.

Goals:
1. Simple. Avoid complexity. Avoid unnecessary dependencies. Avoid unnecessary state.
2. Transparent. Do not swallow exceptions. Again, avoid unnecessary state.
3. Resilient. Tolerate XPath with or without namespaces. Tolerate XML issues as much as the base Java 8 classes permit.
3. Fluid interface. Most methods return **this**. Methods that begin with **as** are terminal.

Limitations:
1. Performance. Simplicity and transparency are more important than performance because the library is only intended for use in test code. The library maintains very little state and objects, as well as their factories are created on the fly. Queries are not compiled. The DOM constructed from an XML character sequence is not cached; only the textual representations is held in a variable.  The only concession to performance is caching the namespaces discovered in an XML document. 
2. Thread safety. Although the library has very little state, it is not thread safe. Create a new instance of Xman for every thread.

TODO: Remove evalute-to-string. Instead, create a pretty print method the prints the results as a string, if the user desires.