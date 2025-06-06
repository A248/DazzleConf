
## The Bug Hunt

The bug hunt is a collaborative effort to find bugs...

### Introduction

Starting with the release of `2.0.0-M1`, anyone who finds a qualifying bug will be featured and mentioned in the README.

### What counts as a bug?

* User input which generates an unexpected error. End-user input should not be able to cause exceptions, so if it does, that's a bug.
* Client code that is contractually compliant which, when interfacing with DazzleConf, generates errors or unexpected behavior.
* Any other behavioral problems in DazzleConf itself, or in how the library is expected to function.
* Bugs that occur between components of the library itself are valid.  If the library is not upholding its own internal contracts but just happens to work on the outside, that is still a bug.

You can even find bugs in the documentation, assuming it is something meaningful (and not just a single-word typo).

### Restrictions

The following cases will not be included in the bug hunt.

* Code which violates clearly documented contracts.
* Silly things like passing `null` to @NonNull-marked parameters, or using deliberately wrong generic types, will be rejected.
* Bugs caused by code you submitted in a PR. This requirement exists to prevent people from intentionally PR'ing bugs into the codebase then "finding" them afterward.
