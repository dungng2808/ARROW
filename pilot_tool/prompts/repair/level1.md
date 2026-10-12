---
id: repair.level1
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

Return the complete corrected test class {{test_class_name}} (package {{test_package}}) as one Java file.
