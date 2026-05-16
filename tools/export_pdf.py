"""Exportiert eine Markdown-Datei als PDF mit xhtml2pdf."""

import sys
import pathlib
import markdown
from xhtml2pdf import pisa

CSS = """
body {
    font-family: "Segoe UI", Arial, sans-serif;
    font-size: 10.5pt;
    line-height: 1.6;
    color: #1a1a1a;
}

h1 {
    font-size: 22pt;
    color: #27221E;
    border-bottom: 3px solid #7C2D25;
    padding-bottom: 6px;
    margin-top: 0;
}

h2 {
    font-size: 15pt;
    color: #27221E;
    border-bottom: 1px solid #C6BAA4;
    padding-bottom: 4px;
    margin-top: 28px;
}

h3 {
    font-size: 12pt;
    color: #3E3731;
    margin-top: 20px;
}

h4 {
    font-size: 10.5pt;
    color: #3E3731;
    margin-top: 14px;
}

code {
    font-family: "Consolas", "Courier New", monospace;
    font-size: 9pt;
    background: #F5F0E8;
    padding: 1px 4px;
    border-radius: 3px;
    color: #27221E;
}

pre {
    background: #F5F0E8;
    border-left: 4px solid #7C2D25;
    padding: 10px 14px;
    font-size: 8.5pt;
    line-height: 1.45;
    overflow: hidden;
    page-break-inside: avoid;
}

pre code {
    background: none;
    padding: 0;
}

table {
    width: 100%;
    border-collapse: collapse;
    margin: 12px 0;
    font-size: 9.5pt;
    page-break-inside: avoid;
}

th {
    background: #3E3731;
    color: #F2EBDD;
    padding: 6px 10px;
    text-align: left;
}

td {
    border-bottom: 1px solid #DDD5C4;
    padding: 5px 10px;
}

tr:nth-child(even) td {
    background: #FAF7F2;
}

blockquote {
    border-left: 4px solid #C6BAA4;
    margin-left: 0;
    padding-left: 14px;
    color: #5E5248;
    font-style: italic;
}

hr {
    border: none;
    border-top: 1px solid #C6BAA4;
    margin: 20px 0;
}

a { color: #7C2D25; }

p { margin: 6px 0 10px 0; }

ul, ol { padding-left: 22px; margin: 6px 0; }
li { margin: 2px 0; }
"""

def export(md_path: str, pdf_path: str):
    src = pathlib.Path(md_path).read_text(encoding="utf-8")
    html_body = markdown.markdown(src, extensions=["tables", "fenced_code", "toc"])
    html = f"""<!DOCTYPE html>
<html lang="de">
<head><meta charset="utf-8"><style>{CSS}</style></head>
<body>{html_body}</body>
</html>"""
    with open(pdf_path, "wb") as f:
        result = pisa.CreatePDF(html.encode("utf-8"), dest=f, encoding="utf-8")
    if result.err:
        print(f"Fehler beim PDF-Export: {result.err}")
        sys.exit(1)
    print(f"PDF erstellt: {pdf_path}")

if __name__ == "__main__":
    if len(sys.argv) < 3:
        print("Verwendung: python export_pdf.py <input.md> <output.pdf>")
        sys.exit(1)
    export(sys.argv[1], sys.argv[2])
