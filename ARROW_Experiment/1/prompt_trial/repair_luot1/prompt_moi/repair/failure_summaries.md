---
id: repair.failure_summaries
version: team-v1
role: data
used_by: placeholder failure_summary of repair/user/level1, level2, level3
---
COMPILE: The test class does not compile. Fix the exact lines the compiler reports: use only constructors, methods, and types that appear in the source code or in the additional information (if any), and check the imports, parameter types, and static or non-static form.
UNKNOWN: The build failed before the tests could run, and the compiler reported no error in the test file. A common cause is that the project treats compiler warnings as errors: remove unchecked casts, raw types, and deprecated methods from the test class.
DISCOVERY: The test class compiles, but the test runner did not discover or run any of its test methods. Check the @Test annotations and the imports of the test framework that the project uses, and make the class and its test methods public.
RUNTIME: Some test methods throw an unexpected exception while running. Look at the stack trace: if the exception is thrown from the class under test, change the input or setup to fit that code path, or check that the exception is thrown if the source code throws it on purpose for this input; if the exception is thrown from the test class itself, fix the test's data setup.
ASSERTION: The test methods run, but some assertions fail or an AssertionError is thrown. Use the source code to decide whether the expected value or the input is wrong, and change an expected value only when the source code shows that it is wrong. If the AssertionError is thrown inside the class under test (in the stack trace, the first line that does not belong to JUnit, TestNG, or the Java standard library is a line of the class under test, not of the test class), it comes from an assert statement in the source code: the input breaks a precondition of the method, so change the input, not the expected value.
REGRESSION: The test methods pass when run alone, but running the test class together with the module's existing tests makes existing tests fail or stop running, or makes this test class fail. Find where the test class changes shared state (static fields, System properties, the default locale or time zone, files) and remove that change, or restore the old value when the test ends.
