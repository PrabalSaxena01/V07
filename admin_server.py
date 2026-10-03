#!/usr/bin/env python3
"""
V07 Standalone Admin Server & Universal Database Management Engine
Serves the V07 Admin Panel web application and full REST API over HTTP.
Provides complete CRUD, SQL execution, profile management, and database operations.
"""

import os
import sys
import json
import time
import socket
import sqlite3
import shutil
import mimetypes
import subprocess
import traceback
from http.server import HTTPServer, BaseHTTPRequestHandler
from urllib.parse import urlparse, parse_qs

BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ASSETS_DIR = os.path.join(BASE_DIR, "app", "src", "main", "assets")
V3_ASSETS_DIR = os.path.join(ASSETS_DIR, "v3")
DATA_DIR = os.path.join(BASE_DIR, "data")
PROFILES_DIR = os.path.join(DATA_DIR, "profiles")
BACKUPS_DIR = os.path.join(DATA_DIR, "backups")
SCRATCH_DIR = os.path.join(BASE_DIR, "scratch")

os.makedirs(PROFILES_DIR, exist_ok=True)
os.makedirs(BACKUPS_DIR, exist_ok=True)

# Ensure default profile exists
default_db_path = os.path.join(PROFILES_DIR, "default.db")
if not os.path.exists(default_db_path):
    scratch_phone_db = os.path.join(SCRATCH_DIR, "v07_phone.db")
    if os.path.exists(scratch_phone_db):
        shutil.copy2(scratch_phone_db, default_db_path)
    else:
        # Create empty SQLite DB
        con = sqlite3.connect(default_db_path)
        con.close()

SESSION_TOKEN = "v07-admin-local-master-token"

# State
active_profile_id = "default"

TABLE_SCHEMAS = {
    "sessions": """CREATE TABLE IF NOT EXISTS sessions (
        id TEXT PRIMARY KEY,
        activity TEXT NOT NULL,
        start_time INTEGER NOT NULL,
        end_time INTEGER,
        duration_sec INTEGER DEFAULT 0,
        notes TEXT
    );""",
    "tasks": """CREATE TABLE IF NOT EXISTS tasks (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        priority TEXT DEFAULT 'medium',
        status TEXT DEFAULT 'pending',
        created_at INTEGER NOT NULL,
        completed_at INTEGER,
        deadline TEXT
    );""",
    "thoughts": """CREATE TABLE IF NOT EXISTS thoughts (
        id TEXT PRIMARY KEY,
        timestamp INTEGER NOT NULL,
        date TEXT,
        topic TEXT,
        content TEXT NOT NULL,
        pinned INTEGER DEFAULT 0,
        tags TEXT
    );""",
    "app_usage": """CREATE TABLE IF NOT EXISTS app_usage (
        id TEXT PRIMARY KEY,
        package_name TEXT NOT NULL,
        app_name TEXT,
        start_time INTEGER NOT NULL,
        end_time INTEGER NOT NULL,
        duration_ms INTEGER NOT NULL,
        category TEXT DEFAULT 'other'
    );""",
    "day_tracker": """CREATE TABLE IF NOT EXISTS day_tracker (
        id TEXT PRIMARY KEY,
        title TEXT NOT NULL,
        target_date TEXT NOT NULL,
        hour INTEGER,
        pinned INTEGER DEFAULT 0,
        created_at INTEGER NOT NULL
    );""",
    "recycle_bin": """CREATE TABLE IF NOT EXISTS recycle_bin (
        recycle_id TEXT PRIMARY KEY,
        original_id TEXT,
        category TEXT,
        data_json TEXT,
        deleted_at INTEGER NOT NULL
    );""",
    "plugin_storage": """CREATE TABLE IF NOT EXISTS plugin_storage (
        plugin_id TEXT NOT NULL,
        key TEXT NOT NULL,
        value_json TEXT,
        updated_at INTEGER NOT NULL,
        PRIMARY KEY (plugin_id, key)
    );""",
    "events": """CREATE TABLE IF NOT EXISTS events (
        id TEXT PRIMARY KEY,
        category TEXT NOT NULL,
        date TEXT NOT NULL,
        ts INTEGER NOT NULL,
        timestamp INTEGER NOT NULL,
        title TEXT,
        content TEXT,
        score REAL,
        metadata_json TEXT
    );""",
    "deleted_tombstones": """CREATE TABLE IF NOT EXISTS deleted_tombstones (
        id TEXT PRIMARY KEY,
        category TEXT,
        deleted_at INTEGER NOT NULL
    );"""
}

def get_active_db_path():
    path = os.path.join(PROFILES_DIR, f"{active_profile_id}.db")
    if not os.path.exists(path):
        init_db_file(path)
    return path

def init_db_file(path):
    con = sqlite3.connect(path)
    cur = con.cursor()
    cur.execute("PRAGMA journal_mode = WAL;")
    for sql in TABLE_SCHEMAS.values():
        cur.execute(sql)
    con.commit()
    con.close()

def get_db():
    con = sqlite3.connect(get_active_db_path(), timeout=10.0)
    con.row_factory = sqlite3.Row
    return con

def get_local_ip():
    try:
        s = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        s.connect(("8.8.8.8", 80))
        ip = s.getsockname()[0]
        s.close()
        return ip
    except Exception:
        return "127.0.0.1"

class AdminRequestHandler(BaseHTTPRequestHandler):
    def send_cors_headers(self):
        self.send_header("Access-Control-Allow-Origin", "*")
        self.send_header("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS")
        self.send_header("Access-Control-Allow-Headers", "Content-Type, Authorization, X-Requested-With, X-Session-Token")

    def do_OPTIONS(self):
        self.send_response(204)
        self.send_cors_headers()
        self.end_headers()

    def send_json(self, data, status=200):
        body = json.dumps(data, default=str).encode("utf-8")
        self.send_response(status)
        self.send_header("Content-Type", "application/json; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(body)

    def read_json_body(self):
        content_length = int(self.headers.get("Content-Length", 0))
        if content_length > 0:
            raw = self.rfile.read(content_length).decode("utf-8")
            return json.loads(raw)
        return {}

    def serve_html_with_bridge(self, file_path):
        if not os.path.exists(file_path):
            self.send_error(404, "File not found")
            return
        with open(file_path, "r", encoding="utf-8") as f:
            content = f.read()

        # Injected V07Platform bridge
        host = self.headers.get("Host", f"127.0.0.1:{self.server.server_port}")
        base_url = f"http://{host}"
        injection = f"""<script>
window.V07Platform = {{
    BRIDGE_VERSION: '3.0.0-v3',
    BASE_URL: '{base_url}',
    TOKEN: '{SESSION_TOKEN}',
    activeProfile: '{active_profile_id}',
    isDesktopStandAlone: true
}};
</script>"""
        if "</head>" in content:
            content = content.replace("</head>", f"{injection}\n</head>", 1)
        elif "<body" in content:
            content = content.replace("<body", f"{injection}\n<body", 1)
        else:
            content = injection + content

        body = content.encode("utf-8")
        self.send_response(200)
        self.send_header("Content-Type", "text/html; charset=utf-8")
        self.send_header("Content-Length", str(len(body)))
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(body)

    def serve_static(self, rel_path):
        # Look in v3 assets first, then root assets
        target = os.path.join(V3_ASSETS_DIR, rel_path.lstrip("/"))
        if not os.path.exists(target):
            target = os.path.join(ASSETS_DIR, rel_path.lstrip("/"))
        if not os.path.exists(target) or os.path.isdir(target):
            self.send_error(404, "Not Found")
            return

        mime_type, _ = mimetypes.guess_type(target)
        if not mime_type:
            if target.endswith(".woff2"):
                mime_type = "font/woff2"
            elif target.endswith(".js"):
                mime_type = "application/javascript"
            elif target.endswith(".css"):
                mime_type = "text/css"
            else:
                mime_type = "application/octet-stream"

        with open(target, "rb") as f:
            data = f.read()
        self.send_response(200)
        self.send_header("Content-Type", mime_type)
        self.send_header("Content-Length", str(len(data)))
        self.send_header("Cache-Control", "public, max-age=3600")
        self.send_cors_headers()
        self.end_headers()
        self.wfile.write(data)

    def do_GET(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)

        # Static / Page routes
        if path in ["", "/", "/admin", "/admin.html"]:
            admin_file = os.path.join(V3_ASSETS_DIR, "admin.html")
            if not os.path.exists(admin_file):
                admin_file = os.path.join(ASSETS_DIR, "admin.html")
            self.serve_html_with_bridge(admin_file)
            return

        if path in ["/companion", "/companion.html"]:
            comp_file = os.path.join(V3_ASSETS_DIR, "companion.html")
            if not os.path.exists(comp_file):
                comp_file = os.path.join(ASSETS_DIR, "companion.html")
            self.serve_html_with_bridge(comp_file)
            return

        if path in ["/index", "/index.html"]:
            idx_file = os.path.join(V3_ASSETS_DIR, "index.html")
            if not os.path.exists(idx_file):
                idx_file = os.path.join(ASSETS_DIR, "index.html")
            self.serve_html_with_bridge(idx_file)
            return

        # API Routes
        if path.startswith("/api/v3/"):
            self.handle_api_get(path, query)
            return

        # General static asset fallback
        self.serve_static(path)

    def do_POST(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        if path.startswith("/api/v3/"):
            self.handle_api_post(path, query)
        else:
            self.send_error(404, "Endpoint not found")

    def do_PUT(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        if path.startswith("/api/v3/"):
            self.handle_api_put(path, query)
        else:
            self.send_error(404, "Endpoint not found")

    def do_DELETE(self):
        parsed = urlparse(self.path)
        path = parsed.path
        query = parse_qs(parsed.query)
        if path.startswith("/api/v3/"):
            self.handle_api_delete(path, query)
        else:
            self.send_error(404, "Endpoint not found")

    # =========================================================================
    # API HANDLERS
    # =========================================================================

    def handle_api_get(self, path, query):
        global active_profile_id
        try:
            if path == "/api/v3/status":
                con = get_db()
                table_counts = {}
                for t in ["sessions", "tasks", "thoughts", "app_usage", "day_tracker", "events", "plugin_storage", "recycle_bin"]:
                    try:
                        table_counts[t] = con.execute(f"SELECT count(*) FROM {t}").fetchone()[0]
                    except Exception:
                        table_counts[t] = 0
                con.close()

                adb_connected = False
                try:
                    res = subprocess.run(["adb", "devices"], capture_output=True, text=True, timeout=2)
                    lines = [line.strip() for line in res.stdout.strip().split("\n")[1:] if line.strip() and not line.startswith("*")]
                    adb_connected = len(lines) > 0
                except Exception:
                    pass

                self.send_json({
                    "status": "online",
                    "version": "3.0.0-v3",
                    "engine": "SQLite WAL Engine (Admin Live Server)",
                    "session_token": SESSION_TOKEN,
                    "active_profile": active_profile_id,
                    "database_path": get_active_db_path(),
                    "table_counts": table_counts,
                    "adb_connected": adb_connected
                })
                return

            if path == "/api/v3/database/stats":
                db_path = get_active_db_path()
                file_size = os.path.getsize(db_path) if os.path.exists(db_path) else 0
                con = get_db()
                tables = {}
                for row in con.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall():
                    tname = row["name"]
                    if tname.startswith("sqlite_") or tname == "android_metadata":
                        continue
                    try:
                        tables[tname] = con.execute(f"SELECT count(*) FROM {tname}").fetchone()[0]
                    except Exception:
                        tables[tname] = 0
                con.close()
                self.send_json({
                    "active_profile": active_profile_id,
                    "file_size_bytes": file_size,
                    "tables": tables,
                    "wal_enabled": True
                })
                return

            if path == "/api/v3/admin/profiles":
                profiles = []
                for fname in os.listdir(PROFILES_DIR):
                    if fname.endswith(".db"):
                        pid = fname[:-3]
                        fpath = os.path.join(PROFILES_DIR, fname)
                        fsize = os.path.getsize(fpath)
                        mtime = int(os.path.getmtime(fpath) * 1000)
                        try:
                            c = sqlite3.connect(fpath)
                            tcount = len([r[0] for r in c.execute("SELECT name FROM sqlite_master WHERE type='table'").fetchall() if not r[0].startswith("sqlite_")])
                            c.close()
                        except Exception:
                            tcount = 0
                        profiles.append({
                            "id": pid,
                            "is_active": pid == active_profile_id,
                            "file_size": fsize,
                            "table_count": tcount,
                            "last_modified": mtime
                        })
                profiles.sort(key=lambda x: (not x["is_active"], x["id"]))
                self.send_json(profiles)
                return

            if path == "/api/v3/admin/tables":
                con = get_db()
                tables = []
                for row in con.execute("SELECT name FROM sqlite_master WHERE type='table' ORDER BY name ASC").fetchall():
                    tname = row["name"]
                    if tname.startswith("sqlite_") or tname == "android_metadata":
                        continue
                    cols = []
                    for c in con.execute(f"PRAGMA table_info({tname})").fetchall():
                        cols.append({
                            "name": c[1],
                            "type": c[2] or "TEXT",
                            "notnull": bool(c[3]),
                            "dflt_value": c[4],
                            "pk": c[5]
                        })
                    tables.append({"name": tname, "columns": cols})
                con.close()
                self.send_json(tables)
                return

            if path == "/api/v3/admin/table/data":
                tname = query.get("table", [""])[0]
                if not tname:
                    self.send_json({"error": "Missing table param"}, 400)
                    return
                limit = int(query.get("limit", ["50"])[0])
                offset = int(query.get("offset", ["0"])[0])
                sort_by = query.get("sort_by", [""])[0]
                sort_dir = query.get("sort_dir", ["ASC"])[0].upper()
                if sort_dir not in ["ASC", "DESC"]:
                    sort_dir = "ASC"
                search = query.get("search", [""])[0].strip()

                con = get_db()
                cols = [c[1] for c in con.execute(f"PRAGMA table_info({tname})").fetchall()]
                where_clauses = []
                params = []
                if search and cols:
                    search_likes = [f'CAST("{c}" AS TEXT) LIKE ?' for c in cols]
                    where_clauses.append(f"({' OR '.join(search_likes)})")
                    params.extend([f"%{search}%"] * len(cols))

                where_sql = f" WHERE {' AND '.join(where_clauses)}" if where_clauses else ""
                total = con.execute(f'SELECT count(*) FROM "{tname}"{where_sql}', params).fetchone()[0]

                order_sql = f' ORDER BY "{sort_by}" {sort_dir}' if sort_by in cols else ""
                sql = f'SELECT * FROM "{tname}"{where_sql}{order_sql} LIMIT {limit} OFFSET {offset}'
                rows = [dict(r) for r in con.execute(sql, params).fetchall()]
                con.close()

                self.send_json({
                    "table": tname,
                    "total": total,
                    "limit": limit,
                    "offset": offset,
                    "columns": cols,
                    "rows": rows
                })
                return

            if path == "/api/v3/admin/backups":
                backups = []
                for fname in os.listdir(BACKUPS_DIR):
                    if fname.endswith(".db") or fname.endswith(".bak"):
                        fpath = os.path.join(BACKUPS_DIR, fname)
                        backups.append({
                            "name": fname,
                            "path": fpath,
                            "size": os.path.getsize(fpath),
                            "timestamp": int(os.path.getmtime(fpath) * 1000)
                        })
                backups.sort(key=lambda x: x["timestamp"], reverse=True)
                self.send_json(backups)
                return

            if path == "/api/v3/database/integrity":
                con = get_db()
                check_result = con.execute("PRAGMA integrity_check;").fetchall()
                con.close()
                self.send_json({"result": [r[0] for r in check_result]})
                return

            # Entity endpoints: sessions, tasks, thoughts, entries, daytracker, appusage, recyclebin, plugins
            if path == "/api/v3/sessions":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM sessions ORDER BY start_time DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/tasks":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM tasks ORDER BY created_at DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/thoughts":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM thoughts ORDER BY timestamp DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/entries":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM events ORDER BY ts DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/daytracker":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM day_tracker ORDER BY created_at DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/admin/appusage":
                limit = int(query.get("limit", ["100"])[0])
                offset = int(query.get("offset", ["0"])[0])
                con = get_db()
                total = con.execute("SELECT count(*) FROM app_usage").fetchone()[0]
                rows = [dict(r) for r in con.execute(f"SELECT * FROM app_usage ORDER BY start_time DESC LIMIT {limit} OFFSET {offset}").fetchall()]
                con.close()
                self.send_json({"total": total, "rows": rows})
                return

            if path == "/api/v3/plugins":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM plugin_storage ORDER BY updated_at DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            if path == "/api/v3/recyclebin":
                con = get_db()
                rows = [dict(r) for r in con.execute("SELECT * FROM recycle_bin ORDER BY deleted_at DESC LIMIT 100").fetchall()]
                con.close()
                self.send_json(rows)
                return

            self.send_json({"error": f"Unknown GET endpoint: {path}"}, 404)
        except Exception as e:
            traceback.print_exc()
            self.send_json({"error": str(e)}, 500)

    def handle_api_post(self, path, query):
        global active_profile_id
        try:
            body = self.read_json_body()

            # Profile management
            if path == "/api/v3/admin/profiles/switch":
                pid = body.get("profile_id", "").strip()
                if not pid:
                    self.send_json({"ok": False, "error": "Missing profile_id"}, 400)
                    return
                p_path = os.path.join(PROFILES_DIR, f"{pid}.db")
                if not os.path.exists(p_path):
                    init_db_file(p_path)
                active_profile_id = pid
                self.send_json({"ok": True, "active_profile": active_profile_id})
                return

            if path == "/api/v3/admin/profiles/create":
                pid = body.get("profile_id", "").strip()
                if not pid:
                    self.send_json({"ok": False, "error": "Missing profile_id"}, 400)
                    return
                p_path = os.path.join(PROFILES_DIR, f"{pid}.db")
                if os.path.exists(p_path):
                    self.send_json({"ok": False, "error": "Profile already exists"}, 400)
                    return
                init_db_file(p_path)
                self.send_json({"ok": True, "created": pid})
                return

            if path == "/api/v3/admin/profiles/clone":
                src = body.get("source_id", "").strip()
                tgt = body.get("target_id", "").strip()
                if not src or not tgt:
                    self.send_json({"ok": False, "error": "Missing source_id or target_id"}, 400)
                    return
                src_path = os.path.join(PROFILES_DIR, f"{src}.db")
                tgt_path = os.path.join(PROFILES_DIR, f"{tgt}.db")
                if not os.path.exists(src_path):
                    self.send_json({"ok": False, "error": "Source profile does not exist"}, 404)
                    return
                if os.path.exists(tgt_path):
                    self.send_json({"ok": False, "error": "Target profile already exists"}, 400)
                    return
                # Checkpoint source WAL
                try:
                    c = sqlite3.connect(src_path)
                    c.execute("PRAGMA wal_checkpoint(TRUNCATE);")
                    c.close()
                except Exception:
                    pass
                shutil.copy2(src_path, tgt_path)
                self.send_json({"ok": True, "cloned": tgt})
                return

            # Table row manipulation
            if path == "/api/v3/admin/table/row":
                tname = body.get("table", "").strip()
                row = body.get("row", {})
                if not tname or not row:
                    self.send_json({"ok": False, "error": "Missing table or row data"}, 400)
                    return
                cols = list(row.keys())
                placeholders = ["?"] * len(cols)
                vals = [row[c] for c in cols]
                sql = f'INSERT INTO "{tname}" ({", ".join([f"{c}" for c in cols])}) VALUES ({", ".join(placeholders)})'
                con = get_db()
                con.execute(sql, vals)
                con.commit()
                con.close()
                self.send_json({"ok": True, "inserted": row})
                return

            # SQL Workbench
            if path == "/api/v3/admin/sql":
                sql = body.get("sql", "").strip()
                if not sql:
                    self.send_json({"error": "Missing SQL statement"}, 400)
                    return
                start_time = time.perf_counter()
                con = get_db()
                is_query = sql.strip().upper().startswith(("SELECT", "PRAGMA", "EXPLAIN"))
                try:
                    if is_query:
                        cur = con.cursor()
                        cur.execute(sql)
                        cols = [d[0] for d in cur.description] if cur.description else []
                        rows = [dict(zip(cols, r)) for r in cur.fetchall()]
                        elapsed = (time.perf_counter() - start_time) * 1000.0
                        con.close()
                        self.send_json({
                            "query": sql,
                            "execution_time_ms": round(elapsed, 2),
                            "is_query": True,
                            "columns": cols,
                            "rows": rows,
                            "row_count": len(rows)
                        })
                    else:
                        cur = con.cursor()
                        cur.executescript(sql) if ";" in sql and sql.count(";") > 1 else cur.execute(sql)
                        rows_affected = cur.rowcount
                        con.commit()
                        elapsed = (time.perf_counter() - start_time) * 1000.0
                        con.close()
                        self.send_json({
                            "query": sql,
                            "execution_time_ms": round(elapsed, 2),
                            "is_query": False,
                            "rows_affected": rows_affected
                        })
                except Exception as sqle:
                    con.close()
                    elapsed = (time.perf_counter() - start_time) * 1000.0
                    self.send_json({
                        "error": str(sqle),
                        "execution_time_ms": round(elapsed, 2)
                    }, 400)
                return

            # Maintenance
            if path == "/api/v3/database/vacuum":
                con = get_db()
                con.execute("VACUUM;")
                con.close()
                self.send_json({"ok": True, "message": "VACUUM completed successfully"})
                return

            if path == "/api/v3/database/reindex":
                con = get_db()
                con.execute("REINDEX;")
                con.close()
                self.send_json({"ok": True, "message": "REINDEX completed successfully"})
                return

            if path == "/api/v3/database/backup":
                timestamp = int(time.time())
                backup_name = f"{active_profile_id}_backup_{timestamp}.db"
                backup_path = os.path.join(BACKUPS_DIR, backup_name)
                con = get_db()
                con.execute("PRAGMA wal_checkpoint(TRUNCATE);")
                con.close()
                shutil.copy2(get_active_db_path(), backup_path)
                self.send_json({"ok": True, "backup": backup_name, "path": backup_path})
                return

            if path == "/api/v3/admin/backup/restore":
                bpath = body.get("path", "").strip()
                if not bpath or not os.path.exists(bpath):
                    self.send_json({"ok": False, "error": "Invalid backup file path"}, 400)
                    return
                # Restore over active database
                dest_path = get_active_db_path()
                shutil.copy2(bpath, dest_path)
                # Remove stale WAL/SHM
                for ext in ["-wal", "-shm"]:
                    stale = dest_path + ext
                    if os.path.exists(stale):
                        try: os.remove(stale)
                        except Exception: pass
                self.send_json({"ok": True, "message": f"Successfully restored {os.path.basename(bpath)} to {active_profile_id}"})
                return

            if path == "/api/v3/admin/cache/clear":
                self.send_json({"ok": True, "message": "Cache evicted successfully"})
                return

            # Entity CRUD: sessions, tasks, thoughts, daytracker, entries
            if path in ["/api/v3/sessions", "/api/v3/sessions/record"]:
                con = get_db()
                sid = body.get("id", f"sess_{int(time.time()*1000)}")
                con.execute(
                    "INSERT OR REPLACE INTO sessions (id, activity, start_time, end_time, duration_sec, notes) VALUES (?, ?, ?, ?, ?, ?)",
                    (sid, body.get("activity", "Focus"), int(body.get("start_time", time.time()*1000)), int(body.get("end_time", time.time()*1000)), int(body.get("duration_sec", 0)), body.get("notes", ""))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True, "id": sid})
                return

            if path == "/api/v3/tasks":
                con = get_db()
                tid = body.get("id", f"task_{int(time.time()*1000)}")
                con.execute(
                    "INSERT OR REPLACE INTO tasks (id, title, priority, status, created_at, completed_at, deadline) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    (tid, body.get("title", ""), body.get("priority", "medium"), body.get("status", "pending"), int(body.get("created_at", time.time()*1000)), body.get("completed_at"), body.get("deadline"))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True, "id": tid})
                return

            if path == "/api/v3/thoughts":
                con = get_db()
                th_id = body.get("id", f"th_{int(time.time()*1000)}")
                con.execute(
                    "INSERT OR REPLACE INTO thoughts (id, timestamp, date, topic, content, pinned, tags) VALUES (?, ?, ?, ?, ?, ?, ?)",
                    (th_id, int(body.get("timestamp", time.time()*1000)), body.get("date"), body.get("topic", "Reflection"), body.get("content", ""), int(body.get("pinned", 0)), body.get("tags", ""))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True, "id": th_id})
                return

            if path == "/api/v3/daytracker":
                con = get_db()
                dt_id = body.get("id", f"dt_{int(time.time()*1000)}")
                con.execute(
                    "INSERT OR REPLACE INTO day_tracker (id, title, target_date, hour, pinned, created_at) VALUES (?, ?, ?, ?, ?, ?)",
                    (dt_id, body.get("title", ""), body.get("target_date", ""), body.get("hour"), int(body.get("pinned", 0)), int(body.get("created_at", time.time()*1000)))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True, "id": dt_id})
                return

            if path == "/api/v3/entries":
                con = get_db()
                eid = body.get("id", f"evt_{int(time.time()*1000)}")
                con.execute(
                    "INSERT OR REPLACE INTO events (id, category, date, ts, timestamp, title, content, score, metadata_json) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    (eid, body.get("category", "log"), body.get("date", ""), int(body.get("ts", time.time()*1000)), int(body.get("timestamp", time.time()*1000)), body.get("title"), body.get("content"), body.get("score"), body.get("metadata_json"))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True, "id": eid})
                return

            if path == "/api/v3/plugins/storage":
                con = get_db()
                con.execute(
                    "INSERT OR REPLACE INTO plugin_storage (plugin_id, key, value_json, updated_at) VALUES (?, ?, ?, ?)",
                    (body.get("plugin_id", ""), body.get("key", ""), body.get("value_json", "{}"), int(time.time()*1000))
                )
                con.commit()
                con.close()
                self.send_json({"ok": True})
                return

            if path == "/api/v3/recyclebin/restore":
                rid = body.get("recycle_id", "")
                con = get_db()
                item = con.execute("SELECT * FROM recycle_bin WHERE recycle_id = ?", (rid,)).fetchone()
                if item and item["category"] and item["data_json"]:
                    data = json.loads(item["data_json"])
                    table = item["category"]
                    cols = list(data.keys())
                    sql = f'INSERT OR REPLACE INTO "{table}" ({", ".join(cols)}) VALUES ({", ".join(["?"]*len(cols))})'
                    con.execute(sql, list(data.values()))
                    con.execute("DELETE FROM recycle_bin WHERE recycle_id = ?", (rid,))
                    con.commit()
                con.close()
                self.send_json({"ok": True, "restored": rid})
                return

            self.send_json({"error": f"Unknown POST endpoint: {path}"}, 404)
        except Exception as e:
            traceback.print_exc()
            self.send_json({"error": str(e)}, 500)

    def handle_api_put(self, path, query):
        try:
            body = self.read_json_body()
            if path == "/api/v3/admin/table/row":
                tname = body.get("table", "").strip()
                pk_col = body.get("pk_column", "id").strip()
                pk_val = body.get("pk_value")
                row = body.get("row", {})
                if not tname or not pk_col or pk_val is None or not row:
                    self.send_json({"ok": False, "error": "Missing table, pk_column, pk_value, or row data"}, 400)
                    return
                set_clauses = [f'"{k}" = ?' for k in row.keys()]
                vals = list(row.values()) + [pk_val]
                sql = f'UPDATE "{tname}" SET {", ".join(set_clauses)} WHERE "{pk_col}" = ?'
                con = get_db()
                cur = con.cursor()
                cur.execute(sql, vals)
                con.commit()
                con.close()
                self.send_json({"ok": True, "updated": pk_val, "rows_affected": cur.rowcount})
                return

            # Direct entity update
            if path == "/api/v3/sessions" or path == "/api/v3/sessions/record":
                self.handle_api_post("/api/v3/sessions", query)
                return
            if path == "/api/v3/tasks":
                self.handle_api_post("/api/v3/tasks", query)
                return
            if path == "/api/v3/thoughts":
                self.handle_api_post("/api/v3/thoughts", query)
                return
            if path == "/api/v3/daytracker":
                self.handle_api_post("/api/v3/daytracker", query)
                return
            if path == "/api/v3/entries":
                self.handle_api_post("/api/v3/entries", query)
                return

            self.send_json({"error": f"Unknown PUT endpoint: {path}"}, 404)
        except Exception as e:
            traceback.print_exc()
            self.send_json({"error": str(e)}, 500)

    def handle_api_delete(self, path, query):
        global active_profile_id
        try:
            body = self.read_json_body() if self.headers.get("Content-Length") else {}
            if path == "/api/v3/admin/profiles":
                pid = query.get("profile_id", [""])[0] or body.get("profile_id", "")
                if not pid:
                    self.send_json({"ok": False, "error": "Missing profile_id"}, 400)
                    return
                if pid == active_profile_id:
                    self.send_json({"ok": False, "error": "Cannot delete currently active profile"}, 400)
                    return
                p_path = os.path.join(PROFILES_DIR, f"{pid}.db")
                if os.path.exists(p_path):
                    os.remove(p_path)
                for ext in ["-wal", "-shm"]:
                    stale = p_path + ext
                    if os.path.exists(stale):
                        try: os.remove(stale)
                        except Exception: pass
                self.send_json({"ok": True, "deleted": pid})
                return

            if path == "/api/v3/admin/table/row":
                tname = query.get("table", [""])[0] or body.get("table", "")
                pk_col = query.get("pk_column", ["id"])[0] or body.get("pk_column", "id")
                pk_val = query.get("pk_value", [""])[0] or body.get("pk_value")
                if not tname or not pk_col or pk_val is None:
                    self.send_json({"ok": False, "error": "Missing table, pk_column, or pk_value"}, 400)
                    return
                sql = f'DELETE FROM "{tname}" WHERE "{pk_col}" = ?'
                con = get_db()
                cur = con.cursor()
                cur.execute(sql, (pk_val,))
                con.commit()
                con.close()
                self.send_json({"ok": True, "deleted": pk_val, "rows_affected": cur.rowcount})
                return

            if path == "/api/v3/admin/appusage":
                aid = query.get("id", [""])[0] or body.get("id", "")
                con = get_db()
                if aid:
                    con.execute("DELETE FROM app_usage WHERE id = ?", (aid,))
                else:
                    con.execute("DELETE FROM app_usage;")
                con.commit()
                con.close()
                self.send_json({"ok": True})
                return

            if path == "/api/v3/recyclebin":
                rid = query.get("recycle_id", [""])[0] or body.get("recycle_id", "")
                con = get_db()
                if rid:
                    con.execute("DELETE FROM recycle_bin WHERE recycle_id = ?", (rid,))
                else:
                    con.execute("DELETE FROM recycle_bin;")
                con.commit()
                con.close()
                self.send_json({"ok": True})
                return

            # Direct entity delete
            table_map = {
                "/api/v3/sessions": "sessions",
                "/api/v3/tasks": "tasks",
                "/api/v3/thoughts": "thoughts",
                "/api/v3/daytracker": "day_tracker",
                "/api/v3/entries": "events"
            }
            if path in table_map:
                tname = table_map[path]
                eid = query.get("id", [""])[0] or body.get("id", "")
                if not eid:
                    self.send_json({"ok": False, "error": "Missing id"}, 400)
                    return
                con = get_db()
                con.execute(f'DELETE FROM "{tname}" WHERE id = ?', (eid,))
                con.commit()
                con.close()
                self.send_json({"ok": True, "deleted": eid})
                return

            self.send_json({"error": f"Unknown DELETE endpoint: {path}"}, 404)
        except Exception as e:
            traceback.print_exc()
            self.send_json({"error": str(e)}, 500)

def main():
    port = 8765
    candidate_ports = [8765, 8000, 8080, 8888, 9000]
    server = None
    for p in candidate_ports:
        try:
            server = HTTPServer(("0.0.0.0", p), AdminRequestHandler)
            port = p
            break
        except Exception as e:
            print(f"Port {p} in use ({e}), trying next...")

    if not server:
        print("Error: Could not bind to any candidate ports.")
        sys.exit(1)

    local_ip = get_local_ip()
    print("=" * 70)
    print("  V07 STANDALONE ADMIN CONSOLE & DATABASE ENGINE DEPLOYED")
    print("=" * 70)
    print(f"  Active Profile:    {active_profile_id}")
    print(f"  Database File:     {get_active_db_path()}")
    print("-" * 70)
    print(f"  Local Machine URL:  http://localhost:{port}/admin")
    print(f"  Loopback URL:       http://127.0.0.1:{port}/admin")
    print(f"  Local Network URL:  http://{local_ip}:{port}/admin")
    print("=" * 70)
    print("  Press Ctrl+C to terminate.")
    print("=" * 70)
    sys.stdout.flush()

    try:
        server.serve_forever()
    except KeyboardInterrupt:
        print("\nAdmin server shutting down...")
        server.server_close()

if __name__ == "__main__":
    main()
