---
id: repair.user.level1
version: team-v1
role: user
used_by: fixed_plus, fixed_minus (every round), adaptive level 1
placeholders: failure_summary, test_package, test_class_name, repository_configuration, error_block, test_source, cut_fqn, cut_path, cut_source
---
The test class below failed ARROW's verification. {{failure_summary}}

Required test class
- Package: {{test_package}}
- Class name: {{test_class_name}}

Project information:
<project>
{{repository_configuration}}
</project>

Error output:
<error>
{{error_block}}
</error>

Current test class:
<test>
{{test_source}}
</test>

Class under test {{cut_fqn}} (file {{cut_path}}):
<source>
{{cut_source}}
</source>

Return the complete corrected Java source of {{test_class_name}}. Return Java code only, with no ``` and no explanations.
