---
id: repair.user.level2
version: team-v1
role: user
used_by: fixed_l2 (every round), adaptive level 2
placeholders: failure_summary, test_package, test_class_name, repository_configuration, error_block, extra_context, failed_attempts, test_source, cut_fqn, cut_path, cut_source
---
The test class below failed ARROW's verification. {{failure_summary}} This request also gives additional information about the failure and about earlier repair attempts (if any).

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

Additional information for this failure:
<extra>
{{extra_context}}
</extra>

Repair attempts that already failed (do not repeat them; each entry shows the round, the failure signature, the first error, and the code that was changed):
<attempts>
{{failed_attempts}}
</attempts>

Current test class:
<test>
{{test_source}}
</test>

Class under test {{cut_fqn}} (file {{cut_path}}):
<source>
{{cut_source}}
</source>

Return the complete corrected Java source of {{test_class_name}}. Return Java code only, with no ``` and no explanations.
