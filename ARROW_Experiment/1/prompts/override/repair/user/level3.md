---
id: repair.user.level3
version: team-v1
role: user
used_by: adaptive level 3 (COMPILE, RUNTIME, ASSERTION with known failing methods)
placeholders: failure_summary, failing_methods, test_package, test_class_name, repository_configuration, error_block, extra_context, failed_attempts, test_source, cut_fqn, cut_path, cut_source
---
The test class below failed ARROW's verification. {{failure_summary}} The failure is in these test methods: {{failing_methods}}.

Rewrite only these test methods. Keep every other method, field, and import of the current test class unchanged. You may add imports, fields, or helper methods that the rewritten methods need. ARROW takes only the listed methods, plus any new imports, fields, and helper methods, from your answer and puts them into the current test class.

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

Current test class:
<test>
{{test_source}}
</test>

Class under test {{cut_fqn}} (file {{cut_path}}):
<source>
{{cut_source}}
</source>

Earlier repair attempts that failed (each entry shows the round, the failure signature, the first error, and the code that was changed). Do not repeat these changes:
<attempts>
{{failed_attempts}}
</attempts>

Return the complete Java source of {{test_class_name}} with the listed methods rewritten. Your answer must differ from the current test class and from every failed attempt above; returning duplicate code wastes a repair round. Return Java code only, with no ``` and no explanations.
