---
id: repair.level3
version: v1
role: user
---
The unit test class below fails verification.

Verification result: {{state_description}}
{{error_block}}

Current test class ({{test_class_name}}):
```java
{{test_source}}
```

Class under test ({{cut_path}}):
```java
{{cut_source}}
```

The failing test methods are: {{failing_methods}}.
Rewrite only these methods so that they compile and pass. Keep every other method unchanged.
You may add imports, fields, or helper methods if needed. If a method cannot be fixed, leave it out.
Return the complete test class {{test_class_name}} (package {{test_package}}) as one Java file.
