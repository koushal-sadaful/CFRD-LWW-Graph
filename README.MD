Get Started:
1. Download JDK if not already installed
2. Please import the gradle project into intelliJ or any IDE and run the tests

Assumptions/ Thought Explanation:
1. Graph is undirected and unweighted, minor change is required to make graph directed.
2. Any operation takes a vertex or edge object (because the timestamp is crucial to the design)
3. The graph should work for any primitive type, for non-primitive type, the type of value object needs to adjust the hashCode and equals function
4. The path between any two vertices is not necessarily the shortest path
5. The idea behind is that this data structure needs to be wrapped around another level that will call the operations with concurrent changes
6. It is expected that other replicas will propagate a vertex node they created, so the epoch time is preserved to maintain data integrity
7. Given two operations taken place at exactly same epoch time, we will favor the order of operations. First come wins. Maybe they should be both discarded instead of having a bias?
8. Design could be further made robust by using immutable objects for the sets to project against multi-threading issues

