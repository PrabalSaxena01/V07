#!/usr/bin/env python
import os
import subprocess
import time
import sys

def start_v07_server():
    try:
        print("Starting V07 Server...")
        
        # Use JAVA_HOME
        java_path = "java"
        if os.path.exists("C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"):
            java_path = "C:/Program Files/Microsoft/jdk-17.0.19.10-hotspot/bin/java.exe"
        
        cmd = [java_path, "-cp", "app/src/main/java", 
               "-Dfile.encoding=UTF-8", "-Xmx512m", 
               "com.v07.dashboard.V07ServerKt$startServer"]
        
        print(f"Running: {' '.join(cmd)}")
        
        # Start server process
        proc = subprocess.Popen(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
        print(f"Server started with PID: {proc.pid}")
        
        # Wait for server to start
        time.sleep(5)
        
        # Test server health
        import urllib.request
        import json
        
        try:
            response = urllib.request.urlopen("http://127.0.0.1:8000/health", timeout=10)
            health_data = json.loads(response.read().decode('utf-8'))
            print(f"Server health: {health_data}")
            print("SUCCESS: V07 Server is running!")
        except Exception as e:
            print(f"Server test failed: {e}")
            # Check if process is still running
            if proc.poll() is not None:
                stdout, stderr = proc.communicate()
                print(f"Server output: {stdout}")
                print(f"Server errors: {stderr}")
        
    except Exception as e:
        print(f"Error: {e}")
        sys.exit(1)

if __name__ == "__main__":
    start_v07_server()