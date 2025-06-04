
# Streaming branch

Data streaming NEEDS WORK. There are a few reasons:

* Because of nested configuration sections. Looking at SubSectionLiaison, we find that there is no way for the liaison to return a DataStreamable.
* Error values. The API in this branch is designed like a `Stream<(String, DataEntry)>`. This fails to communicate error values per-entry.
* Push versus pull. It's not clear yet, but this design decision deserves more consideration.

Considering these challenges, there is very little purpose to the API in this branch. Large configurations (which benefit from streaming) will undoubtedly use nested configuration sections, for example. So, the `DataStreamable` interface was removed during version 2's development.
