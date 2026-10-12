---
id: generation.few_shot
version: v1
role: user
---
Here are examples of a Java class and a unit test class written for it.

{{#each examples}}
Example {{index}} – class under test:
```java
{{cut_source}}
```
Example {{index}} – unit test:
```java
{{test_source}}
```

{{/each}}
Now write a unit test class for the Java class below.

Test class name: {{test_class_name}} (package {{test_package}})

Class under test ({{cut_path}}):
```java
{{cut_source}}
```
