---
id: generation.zero_shot_project_aware
version: v1
role: user
---
Write a unit test class for the Java class below. It must build and run inside the repository described here.

Repository configuration:
{{repo_context}}

Test class name: {{test_class_name}}
Write it to: {{test_file_path}} (package {{test_package}})

Class under test ({{cut_path}}):
```java
{{cut_source}}
```
