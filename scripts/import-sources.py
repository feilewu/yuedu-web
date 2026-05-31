#!/usr/bin/env python3
"""Import book sources from a URL or JSON file into yuedu-web's SQLite database."""

import json
import sys
import sqlite3
import urllib.request
import urllib.error
import os

DB_PATH = os.path.expanduser("~/.yuedu-web/legado.db")

def fetch_json(url):
    print(f"Downloading from {url}...")
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req, timeout=30) as resp:
        return json.loads(resp.read().decode("utf-8"))

def import_sources(conn, sources):
    cur = conn.cursor()
    count = 0
    for src in sources:
        if "bookSourceUrl" not in src:
            continue

        cols = []
        vals = []
        placeholders = []

        field_map = {
            "bookSourceUrl": "bookSourceUrl",
            "bookSourceName": "bookSourceName",
            "bookSourceGroup": "bookSourceGroup",
            "bookSourceType": "bookSourceType",
            "bookUrlPattern": "bookUrlPattern",
            "customOrder": "customOrder",
            "enabled": "enabled",
            "enabledExplore": "enabledExplore",
            "jsLib": "jsLib",
            "enabledCookieJar": "enabledCookieJar",
            "concurrentRate": "concurrentRate",
            "header": "header",
            "loginUrl": "loginUrl",
            "loginUi": "loginUi",
            "loginCheckJs": "loginCheckJs",
            "coverDecodeJs": "coverDecodeJs",
            "bookSourceComment": "bookSourceComment",
            "variableComment": "variableComment",
            "lastUpdateTime": "lastUpdateTime",
            "respondTime": "respondTime",
            "weight": "weight",
            "exploreUrl": "exploreUrl",
            "exploreScreen": "exploreScreen",
            "ruleExplore": "ruleExplore",
            "searchUrl": "searchUrl",
            "ruleSearch": "ruleSearch",
            "ruleBookInfo": "ruleBookInfo",
            "ruleToc": "ruleToc",
            "ruleContent": "ruleContent",
            "eventListener": "eventListener",
            "customButton": "customButton",
            "ruleReview": "ruleReview",
        }

        for json_key, col_name in field_map.items():
            val = src.get(json_key)
            if isinstance(val, (dict, list)):
                val = json.dumps(val, ensure_ascii=False)
            elif val is None:
                val = ""
            elif isinstance(val, bool):
                val = 1 if val else 0
            cols.append(col_name)
            vals.append(val)
            placeholders.append("?")

        cols_str = ", ".join(cols)
        placeholders_str = ", ".join(placeholders)
        updates = ", ".join(f"{c}=excluded.{c}" for c in cols)

        sql = f"INSERT INTO book_sources ({cols_str}) VALUES ({placeholders_str}) ON CONFLICT(bookSourceUrl) DO UPDATE SET {updates}"
        cur.execute(sql, vals)
        count += 1

    conn.commit()
    return count

def main():
    conn = sqlite3.connect(DB_PATH)

    if len(sys.argv) > 1:
        url_or_file = sys.argv[1]
        if url_or_file.startswith(("http://", "https://")):
            data = fetch_json(url_or_file)
        else:
            with open(url_or_file, "r", encoding="utf-8") as f:
                data = json.load(f)
    else:
        print("No URL provided.")
        print("Usage: python import-sources.py <url_or_file>")
        print("Example URLs:")
        print("  https://raw.githubusercontent.com/XIU2/Yuedu/master/sources/generated.json")
        sys.exit(1)

    if isinstance(data, dict) and "data" in data:
        data = data["data"]

    if not isinstance(data, list):
        print(f"Expected a JSON array, got {type(data).__name__}")
        sys.exit(1)

    count = import_sources(conn, data)
    print(f"Imported {count} book sources")
    conn.close()

if __name__ == "__main__":
    main()
