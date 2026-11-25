#!/usr/bin/env python3
"""Gộp nhiều file test thành MỘT lớp, vì `arrow gen --c0` chỉ nhận một lớp (chốt 26/11/2026, ngoại lệ ghi vào RP2).

1. EvoSuite — merge(test_src, scaffolding_src)
   EvoSuite 1.2.0 sinh hai file cho mỗi lớp X:
       X_ESTest.java              các test, khai báo  class X_ESTest extends X_ESTest_scaffolding
       X_ESTest_scaffolding.java  field, @BeforeClass/@Before/@After/@AfterClass, initializeClasses(), resetClasses()
   Cách gộp (chỉ chuyển chỗ mã, không đổi nội dung test):
       a. bỏ "extends X_ESTest_scaffolding" ở lớp test;
       b. chép toàn bộ thân lớp scaffolding vào đầu thân lớp test;
       c. đổi mọi tên X_ESTest_scaffolding còn lại (ví dụ X_ESTest_scaffolding.class.getClassLoader()) thành X_ESTest;
       d. thêm các import của scaffolding mà lớp test chưa có.
   Tên lớp sau đó do arrow tự đổi thành X_ArrowTest (rename_class của java-helper đổi cả X_ESTest.class).

2. ChatUniTest — merge_flat(sources, outer_name)   (cách A "gộp phẳng", chốt 26/11/2026)
   merge=true của ChatUniTest chỉ sinh một lớp @Suite trỏ tới các lớp test, không gộp mã. Ở đây mọi thành viên của các
   lớp test được chép vào MỘT lớp thường; tên trùng thì đổi ở lớp sau (hậu tố _c<số thứ tự>).

Dùng như thư viện:  merge(test_src, scaffolding_src) -> mã đã gộp
Dùng từ dòng lệnh:  python3 gop_scaffolding.py X_ESTest.java X_ESTest_scaffolding.java -o X_merged.java
"""
import argparse   # đọc tham số dòng lệnh (khi chạy file trực tiếp)
import re         # biểu thức chính quy: tìm "class X extends Y", import, package
import sys        # sys.exit, sys.stdout

# Dòng chú thích chèn vào chỗ đặt thân scaffolding, để người đọc file gộp biết đoạn nào là chép sang.
# (Không được chứa chữ "X_ESTest_scaffolding", vì bước kiểm cuối sẽ tưởng là còn tham chiếu sót.)
MARK = "// TN3: thân lớp scaffolding của EvoSuite được gộp vào đây (gop_scaffolding.py)"


class MergeError(Exception):
    """Không gộp được (cấu trúc file khác dự kiến). Script ghi MERGE_FAILED, không đưa vào xác minh."""


def _code_mask(src):
    """Đánh dấu từng ký tự của mã nguồn: True nếu là mã thật, False nếu nằm trong comment, chuỗi hoặc ký tự.
    Cần để không nhầm dấu { } hay chữ "class" nằm trong comment/chuỗi là cú pháp thật."""
    mask = [True] * len(src)                 # mặc định mọi ký tự là mã
    i, n = 0, len(src)
    while i < n:
        c = src[i]                           # ký tự hiện tại
        two = src[i:i + 2]                   # hai ký tự bắt đầu từ i
        if two == "//":                      # comment một dòng: tới hết dòng
            j = src.find("\n", i)
            j = n if j < 0 else j            # không có xuống dòng → tới cuối file
            for k in range(i, j):
                mask[k] = False
            i = j
        elif two == "/*":                    # comment khối: tới "*/"
            j = src.find("*/", i + 2)
            j = n if j < 0 else j + 2
            for k in range(i, j):
                mask[k] = False
            i = j
        elif c in "\"'":                     # chuỗi "..." hoặc ký tự '.'
            j = i + 1
            while j < n and src[j] != c:     # tìm dấu đóng cùng loại
                j += 2 if src[j] == "\\" else 1   # gặp \ thì bỏ qua cả ký tự sau (ví dụ \" không đóng chuỗi)
            j = min(j + 1, n)
            for k in range(i, j):
                mask[k] = False
            i = j
        else:
            i += 1                           # ký tự mã thường
    return mask


def _class_body(src, name):
    """Tìm thân lớp `name`: trả về (vị trí '{' mở, vị trí '}' đóng tương ứng), bỏ qua comment và chuỗi."""
    mask = _code_mask(src)
    for m in re.finditer(r"\bclass\s+" + re.escape(name) + r"\b", src):    # mọi chỗ "class <name>"
        if not mask[m.start()]:
            continue                                                         # nằm trong comment/chuỗi → bỏ
        # dấu { đầu tiên (là mã thật) sau tên lớp = mở thân lớp
        open_at = next((k for k in range(m.end(), len(src)) if src[k] == "{" and mask[k]), None)
        if open_at is None:
            break
        depth = 0                                                            # đếm độ sâu ngoặc nhọn
        for k in range(open_at, len(src)):
            if not mask[k]:
                continue
            if src[k] == "{":
                depth += 1
            elif src[k] == "}":
                depth -= 1
                if depth == 0:                                               # về 0 = gặp } đóng thân lớp
                    return open_at, k
        break
    raise MergeError(f"không tìm thấy thân lớp {name}")


# Một dòng import: "import a.b.C;", "import static a.b.C.m;", "import a.b.*;"
IMPORT_RE = re.compile(r"^[ \t]*import[ \t]+(?:static[ \t]+)?[\w.]+(?:\.\*)?[ \t]*;[ \t]*$", re.M)


def _imports(src):
    """Danh sách các dòng import trong mã (đã bỏ khoảng trắng hai đầu)."""
    return [m.group(0).strip() for m in IMPORT_RE.finditer(src)]


def merge(test_src, scaffolding_src):
    """Gộp scaffolding vào lớp test của EvoSuite; trả về mã có đúng một lớp."""
    test_src = test_src.replace("\r\n", "\n")                  # đổi xuống dòng Windows thành \n cho dễ xử lý
    scaffolding_src = scaffolding_src.replace("\r\n", "\n")
    # tìm "class X_ESTest extends X_ESTest_scaffolding"
    m = re.search(r"\bclass\s+(\w+)\s+extends\s+(\w+_ESTest_scaffolding)\b", test_src)
    if not m:
        raise MergeError("lớp test không kế thừa *_ESTest_scaffolding")
    test_name, scaf_name = m.group(1), m.group(2)              # X_ESTest, X_ESTest_scaffolding
    if not re.search(r"\bclass\s+" + re.escape(scaf_name) + r"\b", scaffolding_src):
        raise MergeError(f"file scaffolding không có lớp {scaf_name}")

    s_open, s_close = _class_body(scaffolding_src, scaf_name)  # vị trí { } của thân lớp scaffolding
    body = scaffolding_src[s_open + 1:s_close]                 # phần bên trong { }
    body = re.sub(r"\b" + re.escape(scaf_name) + r"\b", test_name, body)   # đổi tên tự tham chiếu trong thân

    # a. bỏ "extends X_ESTest_scaffolding": thay đoạn khớp bằng "class X_ESTest"
    out = test_src[:m.start()] + f"class {test_name}" + test_src[m.end():]
    # b. chèn thân scaffolding ngay sau dấu { của lớp test
    t_open, _ = _class_body(out, test_name)
    out = out[:t_open + 1] + "\n\n  " + MARK + body.rstrip() + "\n\n  // TN3: hết phần scaffolding\n" + out[t_open + 1:]
    # c. đổi tên còn sót (nếu lớp test tự nhắc tới lớp scaffolding)
    out = re.sub(r"\b" + re.escape(scaf_name) + r"\b", test_name, out)
    # d. thêm import của scaffolding mà lớp test chưa có, chèn sau import cuối (hoặc sau package)
    have = set(_imports(out))
    extra = [imp for imp in _imports(scaffolding_src) if imp not in have]
    if extra:
        last = None
        mask = _code_mask(out)
        for im in IMPORT_RE.finditer(out):
            # chỉ tính import là mã thật (vị trí chữ "import" sau khoảng trắng đầu dòng không nằm trong comment)
            if mask[im.start() + len(im.group(0)) - len(im.group(0).lstrip())]:
                last = im
        if last is not None:
            at = last.end()                                    # chèn ngay sau import cuối
        else:
            pm = re.search(r"^[ \t]*package[ \t]+[\w.]+[ \t]*;[ \t]*$", out, re.M)
            at = pm.end() if pm else 0                         # không có import → sau dòng package (hoặc đầu file)
        out = out[:at] + "\n" + "\n".join(extra) + out[at:]
    if re.search(r"\b\w+_ESTest_scaffolding\b", out):          # kiểm cuối: không còn tham chiếu nào tới lớp scaffolding
        raise MergeError("vẫn còn tham chiếu tới lớp scaffolding sau khi gộp")
    return out


# ---------------------------------------------------------------------------
# ChatUniTest (chốt 26/11/2026, cách A "gộp phẳng"): chép mọi thành viên (trường, phương thức, lớp con, khối khởi tạo)
# của các lớp test vào MỘT lớp thường. Không dùng @Nested: arrow chạy test bằng -Dtest=<lớp>, Surefire cũ (vd. 2.22)
# với provider JUnit Platform lọc theo đúng tên lớp nên bỏ mất lớp lồng (Lớp$Con) → 0 test (DISCOVERY).
# Tên trùng giữa các lớp (trường, phương thức như setUp, lớp con) thì đổi tên ở lớp sau, kèm hậu tố _c<số thứ tự>.
# Hệ quả phải ghi là ngoại lệ: @BeforeEach/@AfterEach của lớp này cũng chạy trước/sau test của lớp khác; chú thích
# cấp lớp (vd. @ExtendWith(MockitoExtension.class)) áp cho mọi test.
# ---------------------------------------------------------------------------

_LEAD_ANN = re.compile(r"\s*@[\w.]+(?:\s*\((?:[^()]|\([^()]*\))*\))?")      # một chú thích ở đầu (cho phép ngoặc lồng 1 mức)


def _strip_leading_annotations(head):
    """Bỏ các chú thích @... ở đầu một khai báo, trả về phần còn lại."""
    while True:
        m = _LEAD_ANN.match(head)
        if not m:
            return head
        head = head[m.end():]


def _members(body):
    """Tách thân lớp (phần trong { }) thành các thành viên ở mức 1. Trả về danh sách (đoạn mã, loại, tên):
    loại = "type" (lớp con), "method", "field", "init" (khối khởi tạo, không tên)."""
    mask = _code_mask(body)
    out, start, depth = [], 0, 0
    for k, ch in enumerate(body):
        if not mask[k]:
            continue                                          # bỏ comment/chuỗi
        if ch == "{":
            depth += 1
        elif ch == "}":
            depth -= 1
            if depth == 0:                                    # hết một khối ở mức 1 (phương thức, lớp con, khối khởi tạo)
                out.append(body[start:k + 1])
                start = k + 1
        elif ch == ";" and depth == 0:                        # hết một khai báo trường
            out.append(body[start:k + 1])
            start = k + 1
    tail = body[start:].strip()
    if tail:                                                  # phần thừa (thường chỉ là comment cuối)
        out.append(body[start:])
    res = []
    for chunk in out:
        if not chunk.strip():
            continue
        cm = _code_mask(chunk)
        stop = next((i for i, ch in enumerate(chunk) if cm[i] and ch in "{;="), len(chunk))   # phần đầu, trước { ; =
        head = _strip_leading_annotations("".join(ch if cm[i] else " " for i, ch in enumerate(chunk[:stop])))
        tm = re.search(r"\b(?:class|interface|enum|record)\s+(\w+)", head)
        if tm:
            res.append((chunk, "type", tm.group(1)))
            continue
        mm = re.search(r"(\w+)\s*\(", head)                   # có "(" trước { ; = → phương thức (hoặc constructor)
        if mm:
            res.append((chunk, "method", mm.group(1)))
            continue
        fm = re.search(r"(\w+)\s*$", head.strip())             # trường: tên là từ cuối trước = hoặc ;
        if fm and head.strip():
            res.append((chunk, "field", fm.group(1)))
        else:
            res.append((chunk, "init", None))                 # khối khởi tạo { } hoặc static { }, hoặc comment
    return res


def _rename_ident(code, old, new):
    """Đổi tên định danh `old` thành `new` trong mã (nguyên từ), bỏ qua comment và chuỗi."""
    mask = _code_mask(code)
    out, last = [], 0
    for m in re.finditer(r"(?<![\w$])" + re.escape(old) + r"(?![\w$])", code):
        if mask[m.start()]:
            out.append(code[last:m.start()])
            out.append(new)
            last = m.end()
    out.append(code[last:])
    return "".join(out)


def merge_flat(sources, outer_name):
    """sources: mã các lớp test ChatUniTest (không gồm lớp @Suite). outer_name: tên lớp gộp (<Lớp>_ChatUniTest).
    Trả về (mã một lớp, thông tin gộp {classes, renamed, class_annotations})."""
    package, imports, annotations, parts, renamed, dropped = None, [], [], [], [], []
    taken = set()                                             # tên thành viên đã dùng ở các lớp trước
    for idx, src in enumerate(sources, 1):
        src = src.replace("\r\n", "\n")
        pm = re.search(r"^[ \t]*package[ \t]+([\w.]+)[ \t]*;", src, re.M)
        if pm:
            if package and pm.group(1) != package:            # mọi lớp phải cùng package
                raise MergeError("các lớp test khác package")
            package = pm.group(1)
        mask = _code_mask(src)
        imps = [m for m in IMPORT_RE.finditer(src) if mask[m.start() + len(m.group(0)) - len(m.group(0).lstrip())]]
        imports += [m.group(0).strip() for m in imps]
        start = imps[-1].end() if imps else (pm.end() if pm else 0)
        decl = src[start:]                                    # phần khai báo lớp (kèm chú thích cấp lớp)
        cm = re.search(r"\bclass\s+(\w+)", decl)
        if not cm:
            raise MergeError("không thấy khai báo lớp")
        name = cm.group(1)
        # chú thích cấp lớp: các @... đứng trước từ khóa class
        for am in re.finditer(r"@[\w.]+(?:\s*\((?:[^()]|\([^()]*\))*\))?", decl[:cm.start()]):
            a = re.sub(r"\s+", " ", am.group(0)).strip()
            kind = re.match(r"@([\w.]+)", a).group(1).split(".")[-1]       # loại chú thích, vd. DisplayName, ExtendWith
            if a in annotations:
                continue                                                  # trùng hệt → bỏ
            if any(re.match(r"@([\w.]+)", x).group(1).split(".")[-1] == kind for x in annotations):
                dropped.append(a)                                         # cùng loại, khác giá trị: chú thích thường không lặp được → giữ cái đầu
                continue
            annotations.append(a)
        o, c_ = _class_body(decl, name)
        body = decl[o + 1:c_]
        body = _rename_ident(body, name, outer_name)          # tự tham chiếu tên lớp (Lớp.class, new Lớp()) → lớp gộp
        members = _members(body)
        names = {n for _, kind, n in members if n and kind in ("type", "method", "field")}
        clash = sorted(n for n in names if n in taken and n != outer_name)
        for n in clash:                                       # đổi tên trùng ở lớp này (cả khai báo và chỗ dùng)
            new = f"{n}_c{idx}"
            body = _rename_ident(body, n, new)
            renamed.append({"class": name, "from": n, "to": new})
        members = _members(body)                              # tách lại sau khi đổi tên
        out_members = []
        for chunk, kind, n in members:
            if kind == "method" and n == outer_name:          # constructor của lớp test cũ → khối khởi tạo { ... }
                b = chunk.index("{")
                chunk = chunk[b:]
            out_members.append(chunk.strip("\n"))
            if n:
                taken.add(n)
        parts.append(f"    // ===== TN3: từ lớp {name} =====\n" + "\n".join(out_members))
    if not parts:
        raise MergeError("không có lớp test nào")
    seen, uniq = set(), []                                    # bỏ import trùng, giữ thứ tự
    for imp in imports:
        if imp not in seen:
            seen.add(imp)
            uniq.append(imp)
    ann = "".join(a + "\n" for a in annotations)
    out = (f"package {package};\n\n" if package else "") + "\n".join(uniq) + \
          f"\n\n// TN3: các lớp test của ChatUniTest gộp phẳng thành một lớp (gop_scaffolding.merge_flat)\n" \
          f"{ann}public class {outer_name} {{\n\n" + "\n\n".join(parts) + "\n}\n"
    return out, {"classes": len(sources), "renamed": renamed, "class_annotations": annotations,
                 "dropped_annotations": dropped}


def main():
    """Chạy tay: gộp hai file EvoSuite, in ra màn hình hoặc ghi ra file -o."""
    ap = argparse.ArgumentParser(description="Gộp X_ESTest_scaffolding.java vào X_ESTest.java")
    ap.add_argument("test")
    ap.add_argument("scaffolding")
    ap.add_argument("-o", "--out", help="file ra (mặc định in ra màn hình)")
    a = ap.parse_args()
    with open(a.test, encoding="utf-8") as f:
        t = f.read()
    with open(a.scaffolding, encoding="utf-8") as f:
        s = f.read()
    try:
        merged = merge(t, s)
    except MergeError as e:
        sys.exit(f"MERGE_FAILED: {e}")
    if a.out:
        with open(a.out, "w", encoding="utf-8", newline="\n") as f:
            f.write(merged)
    else:
        sys.stdout.write(merged)


if __name__ == "__main__":
    main()
