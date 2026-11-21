# BÁO CÁO TỔNG HỢP KẾT QUẢ CHẠY TEST THỰC NGHIỆM TN1 (RQ1a)
## Phân Hệ Máy: Quang (`quang-qwen` — Shard 1/4)

> **Ngày hoàn thành thực nghiệm mới**: 22/11/2026 (Chạy lại hoàn chỉnh có đầy đủ thời gian từng bước build)  
> **Thành viên thực hiện**: Quang  
> **Mô hình AI**: `qwen/qwen3-coder-30b-a3b-instruct` (qua OpenRouter trên Amazon Bedrock)  
> **Phạm vi phân công**: Shard 1/4 gồm 63 Target Classes Java × 3 Kỹ thuật Prompt (`zero_shot`, `few_shot`, `zero_shot_project_aware`) = 189 lượt chạy thực tế

---

## 📊 1. Tổng Quan Kết QuẢ Thực Nghiệm Mới (189 Bộ Test)

| Trạng thái thực thi trên JVM | Số lượng | Tỷ lệ (%) | Ý nghĩa thực nghiệm |
| :--- | :---: | :---: | :--- |
| **VALID** (Chạy và PASS 100%) | 37 | 19.6% | Test biên dịch thành công, thực thi trên Maven/Gradle và pass toàn bộ assertion (`tests_passed = tests_total`). |
| **ASSERTION** (Chạy và bắt lỗi logic) | 19 | 10.1% | Test biên dịch và chạy trên JVM, phát hiện lỗi sai khác giữa hành vi dự kiến và thực tế (`AssertionError`). |
| **RUNTIME** (Gặp Exception khi chạy) | 19 | 10.1% | Test biên dịch và chạy trên JVM, gặp ngoại lệ thời gian chạy (`NullPointerException`, `ClassNotFoundException`, v.v.). |
| **COMPILE** (Biên dịch thành công) | 111 | 58.7% | Mã test biên dịch được với JDK tương ứng (dùng làm pool $c_0$ cho TN2 sửa lỗi). |
| **INVALID_OUTPUT** (Cú pháp không hợp lệ) | 3 | 1.6% | Mô hình sinh ra văn bản/markdown không đúng cấu trúc lớp kiểm thử Java hoàn chỉnh. |
| **INFRA** (Lỗi repo đối chuẩn ngoại vi) | 0 | 0.0% | Lỗi hạ tầng tải thư viện ngoại vi (đã retry giải quyết thành công, còn lại 0). |
| **TỔNG CỘNG** | **189** | **100.0%** | **Đầy đủ 100% dữ liệu phân công Shard 1/4** |

👉 **Tổng số bộ test đã chạy thực tế trên máy JVM (`tests_total >= 1`)**: **72 bộ test** (37 VALID + 19 ASSERTION + 19 RUNTIME).  
👉 **Tổng số bộ test cần sửa lỗi đưa vào Pool cho TN2 ($c_0$)**: **149 bộ test** (149 file mã nguồn Java đã được lưu trữ trong `c0/` và `tn2_pool.jsonl`).  
👉 **Số dòng đã có đầy đủ thời gian từng bước kiểm thử (Bổ sung hoàn hảo)**:  
   - `step_compile_s` (Biên dịch mã test): **186 dòng**  
   - `step_run_target_s` (Chạy riêng test đích): **75 dòng**  
   - `step_module_tests_s` (Chạy toàn bộ test module kiểm regression): **37 dòng**

---

## 🧪 2. Chi Tiết Toàn Bộ Ca Chạy Test Thực Tế Trên JVM

### A. Nhóm VALID (37 Ca) — Test thực thi trên JVM và PASS toàn bộ (`tests_passed = tests_total`)

| STT | Tên Lớp Java (FQN) | Prompt Strategy | Số Test Chạy | Số Test PASS | Step Compile | Step Run Target | Step Module Tests | Tổng Thời Gian | Run ID |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `com.javatechie.s3.FileStorageServiceApplication` | `zero_shot_project_aware` | 1 | 1 | 5.6s | 17.7s | 21.7s | 62s | `20261122-005718-36hd` |
| 2 | `com.javatechie.spring.mockito.api.SpringBootMockitoApplication` | `zero_shot_project_aware` | 1 | 1 | 5.1s | 5.6s | 10.6s | 36.4s | `20261122-005504-pddl` |
| 3 | `com.whitepaek.demosigninwithapple.Application` | `zero_shot_project_aware` | 1 | 1 | 3.6s | 4.8s | 7.4s | 26.9s | `20261122-005848-ojn1` |
| 4 | `me.aboullaite.SpringBootJwtApplication` | `few_shot` | 1 | 1 | 3.1s | 3.8s | 25.2s | 40.8s | `20261122-010107-sb7n` |
| 5 | `me.aboullaite.SpringBootJwtApplication` | `zero_shot_project_aware` | 2 | 2 | 3.1s | 27.7s | 43s | 85.8s | `20261122-010150-e08j` |
| 6 | `com.toptal.BirthdayFilterProcessor` | `few_shot` | 2 | 2 | 3.9s | 4.2s | 7.1s | 25.6s | `20261122-005506-6v5n` |
| 7 | `com.toptal.BirthdayFilterProcessor` | `zero_shot` | 3 | 3 | 3.9s | 4.3s | 7.1s | 64.9s | `20261122-005358-75a9` |
| 8 | `com.example.ConditionalValidationApplication` | `few_shot` | 1 | 1 | 3.5s | 7.6s | 5.6s | 29s | `20261122-010150-yc6h` |
| 9 | `com.example.ConditionalValidationApplication` | `zero_shot` | 2 | 2 | 3.3s | 4.9s | 5.7s | 70.5s | `20261122-010036-m5yo` |
| 10 | `com.example.ConditionalValidationApplication` | `zero_shot_project_aware` | 1 | 1 | 3.7s | 5.3s | 5.7s | 23.7s | `20261122-010221-l8mb` |
| 11 | `com.github.ssedano.hash.JumpConsistentHash` | `few_shot` | 10 | 10 | 2.4s | 3.2s | 3.4s | 17.6s | `20261122-005631-rxzj` |
| 12 | `com.openkappa.splitmap.Slice` | `zero_shot_project_aware` | 8 | 8 | 5.2s | 5.6s | 6.8s | 29.1s | `20261122-001902-vg9e` |
| 13 | `Fibonacci` | `few_shot` | 4 | 4 | 2.2s | 2.7s | 3s | 16.7s | `20261122-003809-ipz3` |
| 14 | `Fibonacci` | `zero_shot_project_aware` | 5 | 5 | 2.7s | 2.6s | 3.6s | 17.4s | `20261122-003828-0hbc` |
| 15 | `com.somethingsimilar.opposite_of_a_bloom_filter.ByteArrayFilter` | `few_shot` | 10 | 10 | 3.9s | 4.4s | 4.5s | 28.1s | `20261122-000552-brd9` |
| 16 | `com.igormaznitsa.mvngolang.utils.SysUtils` | `few_shot` | 14 | 14 | 6.5s | 9.4s | 10.5s | 41.6s | `20261122-002517-jazi` |
| 17 | `com.igormaznitsa.mvngolang.utils.SysUtils` | `zero_shot` | 13 | 13 | 7.8s | 8.8s | 11.9s | 121.4s | `20261122-002256-3jqv` |
| 18 | `org.microbule.config.core.CompositeConfig` | `few_shot` | 6 | 6 | 9.9s | 5.9s | 12.1s | 50.6s | `20261122-000958-xw63` |
| 19 | `org.microbule.config.core.CompositeConfig` | `zero_shot` | 7 | 7 | 8.8s | 6.4s | 11.9s | 129.1s | `20261122-000740-vf6w` |
| 20 | `org.microbule.config.core.CompositeConfig` | `zero_shot_project_aware` | 7 | 7 | 7.1s | 6.1s | 17.1s | 68.4s | `20261122-001057-dhyb` |
| 21 | `com.tbp.av.controller.HelloController` | `few_shot` | 1 | 1 | 3.3s | 3.4s | 9.7s | 24.8s | `20261122-004724-be4b` |
| 22 | `com.tbp.av.controller.HelloController` | `zero_shot` | 1 | 1 | 3.4s | 3.6s | 9.9s | 110s | `20261122-004529-mcs3` |
| 23 | `com.tbp.av.controller.HelloController` | `zero_shot_project_aware` | 1 | 1 | 3.5s | 3.4s | 10.4s | 28.1s | `20261122-004750-0p4t` |
| 24 | `com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor.HeaderExtractor` | `zero_shot_project_aware` | 4 | 4 | 3.6s | 3.8s | 3.8s | 24.4s | `20261122-002504-ucc4` |
| 25 | `br.com.caelum.vraptor.console.myproducts.Order` | `zero_shot_project_aware` | 3 | 3 | 3.3s | 3.8s | 4.6s | 25.3s | `20261122-005043-wclh` |
| 26 | `com.redis.utils.MythTime` | `few_shot` | 3 | 3 | 8.4s | 5.8s | 59.6s | 203.8s | `20261122-002840-aky0` |
| 27 | `com.redis.utils.MythTime` | `zero_shot` | 3 | 3 | 7.5s | 4.8s | 56.4s | 212.5s | `20261122-002503-jjo2` |
| 28 | `com.redis.utils.MythTime` | `zero_shot_project_aware` | 3 | 3 | 6.8s | 5.7s | 55.9s | 195.7s | `20261122-003206-ts3l` |
| 29 | `hello.domain.Hello` | `few_shot` | 3 | 3 | 4.4s | 5.4s | 10.5s | 30.8s | `20261122-000401-pxtx` |
| 30 | `hello.domain.Hello` | `zero_shot` | 3 | 3 | 4.3s | 4.8s | 7s | 234s | `20261121-235945-0f8a` |
| 31 | `hello.domain.Hello` | `zero_shot_project_aware` | 3 | 3 | 4.6s | 5.9s | 8.1s | 27.5s | `20261122-000435-h90w` |
| 32 | `com.qulice.plugin.alpha.Main` | `few_shot` | 3 | 3 | 2.4s | 2.3s | 2.6s | 15.9s | `20261122-003844-h1gn` |
| 33 | `com.qulice.plugin.alpha.Main` | `zero_shot` | 3 | 3 | 2.4s | 2.6s | 3.2s | 26.1s | `20261122-003813-evul` |
| 34 | `com.qulice.plugin.alpha.Main` | `zero_shot_project_aware` | 3 | 3 | 2.3s | 2.3s | 2.7s | 15.1s | `20261122-003903-c6v3` |
| 35 | `com.github.phenomics.ontolib.ontology.scoredist.ScoreSamplingOptions` | `few_shot` | 11 | 11 | 5.2s | 6.8s | 17.4s | 42.3s | `20261122-000137-w91i` |
| 36 | `com.github.phenomics.ontolib.ontology.scoredist.ScoreSamplingOptions` | `zero_shot` | 11 | 11 | 6s | 6.2s | 8s | 93.8s | `20261121-235959-kibn` |
| 37 | `com.github.phenomics.ontolib.ontology.scoredist.ScoreSamplingOptions` | `zero_shot_project_aware` | 11 | 11 | 6s | 6.7s | 10.5s | 59.6s | `20261122-000222-ptul` |

### B. Nhóm ASSERTION (19 Ca) — Test thực thi trên JVM và phát hiện lỗi Assertion

| STT | Tên Lớp Java (FQN) | Prompt Strategy | Số Test Chạy | Số Test PASS | Step Compile | Step Run Target | Tổng Thời Gian | Run ID |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `com.javatechie.spring.mockito.api.SpringBootMockitoApplication` | `few_shot` | 1 | 0 | 4.9s | 8.6s | 23.7s | `20261122-005438-rorg` |
| 2 | `com.javatechie.spring.mockito.api.SpringBootMockitoApplication` | `zero_shot` | 1 | 0 | 5.4s | 9.1s | 61.2s | `20261122-005333-jpjp` |
| 3 | `me.aboullaite.SpringBootJwtApplication` | `zero_shot` | 2 | 1 | 4.2s | 25.9s | 157.3s | `20261122-005826-2ci0` |
| 4 | `com.monitor4all.cn.springcloud_gateway.SpringcloudGatewayApplication` | `few_shot` | 2 | 1 | 6.1s | 11.6s | 30.7s | `20261122-010036-rz73` |
| 5 | `com.monitor4all.cn.springcloud_gateway.SpringcloudGatewayApplication` | `zero_shot` | 2 | 1 | 6.5s | 12.8s | 128.3s | `20261122-005824-14pu` |
| 6 | `com.monitor4all.cn.springcloud_gateway.SpringcloudGatewayApplication` | `zero_shot_project_aware` | 2 | 1 | 6.7s | 11.8s | 29.3s | `20261122-010109-opak` |
| 7 | `es.softtek.jwtDemo.JwtDemoApplication` | `zero_shot_project_aware` | 2 | 1 | 3.1s | 8.2s | 20s | `20261122-005333-g3id` |
| 8 | `com.minio.MinioDemoApplication` | `zero_shot_project_aware` | 1 | 0 | 6.8s | 9.6s | 29.7s | `20261122-005752-ldeo` |
| 9 | `com.github.ssedano.hash.JumpConsistentHash` | `zero_shot_project_aware` | 12 | 11 | 3s | 4s | 25.2s | `20261122-005651-xyx4` |
| 10 | `com.google.uzaygezen.core.CompactHilbertCurve` | `few_shot` | 5 | 4 | 4.2s | 5.8s | 20.1s | `20261122-004422-dbyi` |
| 11 | `com.google.uzaygezen.core.CompactHilbertCurve` | `zero_shot` | 6 | 5 | 4.2s | 4.6s | 140.8s | `20261122-004157-9nq0` |
| 12 | `Fibonacci` | `zero_shot` | 3 | 2 | 2.4s | 2.4s | 37.1s | `20261122-003727-fn36` |
| 13 | `org.everit.json.schema.StringSchema` | `few_shot` | 7 | 5 | 8.9s | 4.9s | 29.1s | `20261121-235856-gxbi` |
| 14 | `com.somethingsimilar.opposite_of_a_bloom_filter.ByteArrayFilter` | `zero_shot` | 14 | 13 | 3.1s | 3.4s | 36.4s | `20261122-000506-33ie` |
| 15 | `com.github.hburgmeier.jerseyoauth2.protocol.impl.extractor.HeaderExtractor` | `few_shot` | 4 | 3 | 3.4s | 3.8s | 18s | `20261122-002443-fvg0` |
| 16 | `com.macasaet.fernet.Key` | `zero_shot_project_aware` | 24 | 21 | 3.8s | 5.2s | 25.4s | `20261122-003631-me40` |
| 17 | `de.slub.urn.RQF_RFC8141` | `few_shot` | 17 | 15 | 3.3s | 3.4s | 20.2s | `20261122-010445-3y6c` |
| 18 | `de.slub.urn.RQF_RFC8141` | `zero_shot` | 31 | 28 | 3.9s | 4.2s | 43.6s | `20261122-010358-6opd` |
| 19 | `de.slub.urn.RQF_RFC8141` | `zero_shot_project_aware` | 17 | 13 | 3.8s | 5.2s | 22.4s | `20261122-010508-lrp5` |

### C. Nhóm RUNTIME (19 Ca) — Test thực thi trên JVM và gặp Ngoại lệ (Runtime Exception)

| STT | Tên Lớp Java (FQN) | Prompt Strategy | Số Test Chạy | Số Test PASS | Step Compile | Step Run Target | Tổng Thời Gian | Run ID |
| :---: | :--- | :--- | :---: | :---: | :---: | :---: | :---: | :--- |
| 1 | `com.example.demo.DemoApplication` | `zero_shot_project_aware` | 1 | 0 | 3.2s | 6.2s | 21.2s | `20261122-004951-gt57` |
| 2 | `com.toptal.BirthdayFilterProcessor` | `zero_shot_project_aware` | 3 | 2 | 4.6s | 4.1s | 18.6s | `20261122-005534-vgqh` |
| 3 | `com.github.ssedano.hash.JumpConsistentHash` | `zero_shot` | 13 | 11 | 2.4s | 3.2s | 31.9s | `20261122-005555-z38w` |
| 4 | `com.stripe.interview.Main` | `few_shot` | 4 | 3 | 3.3s | 4.1s | 19.8s | `20261122-005954-3x6h` |
| 5 | `com.stripe.interview.Main` | `zero_shot` | 4 | 3 | 3.5s | 5.7s | 29.4s | `20261122-005918-vjs7` |
| 6 | `com.stripe.interview.Main` | `zero_shot_project_aware` | 4 | 3 | 3.3s | 3.8s | 15.8s | `20261122-010016-xe68` |
| 7 | `me.tuine.minio.MinioApplication` | `zero_shot_project_aware` | 1 | 0 | 4.5s | 6.4s | 28s | `20261122-005302-2ymf` |
| 8 | `com.gxk.jvm.Args` | `few_shot` | 15 | 4 | 4.9s | 2.8s | 20.6s | `20261122-004443-zmqf` |
| 9 | `com.google.uzaygezen.core.CompactHilbertCurve` | `zero_shot_project_aware` | 5 | 3 | 5.3s | 6.2s | 37.5s | `20261122-004444-d9co` |
| 10 | `de.monticore.generating.templateengine.ObjectFactory` | `few_shot` | 0 | 0 | 5.8s | 5.9s | 42.9s | `20261122-002848-mbfi` |
| 11 | `de.monticore.generating.templateengine.ObjectFactory` | `zero_shot` | 0 | 0 | 6.3s | 5.8s | 114.8s | `20261122-002644-b15o` |
| 12 | `de.monticore.generating.templateengine.ObjectFactory` | `zero_shot_project_aware` | 0 | 0 | 6.6s | 4.8s | 42.7s | `20261122-002936-azp5` |
| 13 | `org.everit.json.schema.StringSchema` | `zero_shot` | 8 | 4 | 9.7s | 5.5s | 140.4s | `20261121-235630-3uz3` |
| 14 | `com.somethingsimilar.opposite_of_a_bloom_filter.ByteArrayFilter` | `zero_shot_project_aware` | 11 | 10 | 4s | 7.2s | 26.1s | `20261122-000627-zqyf` |
| 15 | `com.oracle.bedrock.util.ProxyHelper` | `zero_shot` | 9 | 7 | 5.8s | 6s | 122.9s | `20261121-235630-uioc` |
| 16 | `org.abhijitsarkar.spring.beer.repository.BaseCouchbaseRepository` | `zero_shot` | 5 | 3 | 5.7s | 5.6s | 76.2s | `20261122-003025-ts37` |
| 17 | `com.amazonaws.secretsmanager.sql.AWSSecretsManagerPostgreSQLDriver` | `zero_shot_project_aware` | 14 | 0 | 9.5s | 20.8s | 50.8s | `20261122-010624-8rxv` |
| 18 | `org.fusesource.jansi.AnsiRenderer` | `zero_shot_project_aware` | 19 | 13 | 5.2s | 5.5s | 46.6s | `20261122-010746-8jj5` |
| 19 | `leetcode.medium.OnlineStockSpan` | `zero_shot_project_aware` | 8 | 7 | 6.3s | 5s | 32.3s | `20261122-010436-384a` |

---

## 📂 3. Vị Trí Lưu Trữ Dữ Liệu Gốc

Tất cả các tệp kết quả thực nghiệm mới của máy Quang đã được lưu trữ an toàn tại thư mục:
`D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/`

- **Bảng tổng hợp tất cả 189 ca chạy**: [tn1_candidates.csv](file:///D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/tn1_candidates.csv)
- **Nhật ký chạy chi tiết**: [tn1_runs.jsonl](file:///D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/tn1_runs.jsonl)
- **Danh sách pool chuẩn bị cho TN2**: [tn2_pool.jsonl](file:///D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/tn2_pool.jsonl)
- **Kho mã nguồn unit test ($c_0$)**: [results/quang-qwen/c0/](file:///D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/c0/)
- **Kho logs chi tiết (JSON/ERR)**: [results/quang-qwen/logs/](file:///D:/ki9/SET490/ARROW/ARROW_Experiment/1/results/quang-qwen/logs/)
- **Thư mục cache & truy vết đầy đủ**: `D:/arrow-homes/quang-qwen/`
