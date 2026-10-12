---
id: repair.level2
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

Additional information:
{{level2_block}}

{{#if attempts}}
Previous attempts that did not fix the problem. Do not repeat them:
{{#each attempts}}
- Attempt {{round}}: {{outcome}}
{{/each}}

{{/if}}
Return the complete corrected test class {{test_class_name}} (package {{test_package}}) as one Java file.
