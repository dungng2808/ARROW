# Output thực nghiệm được chia sẻ

Mỗi thành viên xuất output bằng `tools/team-experiment/team.py export` sau khi
run hoàn tất. `team-r1/<member>/<dataset>/` chứa kết quả JSONL chia phần, manifests,
summary, provenance và checksum `export.json`; `machine.json` ở thư mục thành viên
ghi môi trường thực tế. Không đặt dataset, cache, repo clone hoặc checkpoint ở đây.

Run đầy đủ và log vẫn ở `Dataset/_team-r1/` trên từng máy. Chỉ stage thư mục output
của mình, tạo PR riêng. Sau khi đủ 5 thành viên, `team.py aggregate` tạo
`team-r1/_aggregate/full-summary.json` và `TONG_HOP.md`.

Hướng dẫn: [kế hoạch 5 máy](../docs/team-experiment/README.md).
