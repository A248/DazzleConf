
## Temporarily Excluded

Right now, this implementation attempt at INI is broken. Not only that, it is broken by design and cannot be implemented in the current API.

That's because INI does not implement strong typing. It does not specify a format for lists, and a single element might be written without brackets.

The current `DataTree/Backend` API of DazzleConf, however, expects the backend to load the correct types. This INI implementation can never actually do that. Without being told what types to prefer, the input data is under-specified and indiscernible.

Until this is solved, INI cannot be implemented in DazzleConf v2.