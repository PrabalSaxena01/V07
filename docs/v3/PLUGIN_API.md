# V3 Plugin Platform API Specification

## 1. Global Bridge Interface: `window.V07Platform`

Plugins communicate with the host runtime exclusively through the asynchronous, typed `V07Platform` interface injected into the sandboxed plugin context. Direct access to `window.parent` or native Android bridges is prohibited and structurally blocked.

```typescript
interface V07PlatformAPI {
  readonly version: string;
  readonly pluginId: string;
  
  // Storage API (Requires: storage:isolated)
  storage: {
    get<T = any>(key: string): Promise<T | null>;
    set<T = any>(key: string, value: T): Promise<void>;
    delete(key: string): Promise<void>;
    keys(): Promise<string[]>;
    clear(): Promise<void>;
    // Structured Domain Records (Partitioned Firestore & SQLite)
    insertEvent(event: PluginDomainEvent): Promise<{ success: boolean; id: string }>;
    queryEvents(options?: { category?: string; date?: string; limit?: number }): Promise<PluginDomainEvent[]>;
    deleteEvent(id: string): Promise<{ success: boolean }>;
  };

  // Event Bus API (Requires: events:listen / events:emit)
  events: {
    on(event: string, handler: (payload: any) => void): () => void; // Returns unsubscribe fn
    emit(event: string, payload: any): Promise<void>;
  };

  // UI & Feedback API
  ui: {
    toast(message: string, type?: 'info' | 'success' | 'warn' | 'error'): void;
    openModal(options: ModalOptions): Promise<ModalResult>;
    setWidgetBadge(count: number): void;
    requestResize(height: number): void;
  };

  // Host Alarms & Notifications (Requires: notifications:schedule)
  notifications: {
    schedule(options: NotificationScheduleOptions): Promise<string>; // Returns alarmId
    cancel(alarmId: string): Promise<void>;
  };

  // Telemetry Queries (Requires: telemetry:read)
  telemetry: {
    getScreenTimeToday(): Promise<ScreenTimeSummary>;
    getActiveFocusSession(): Promise<FocusSession | null>;
  };
}
```

---

## 2. API Method Contracts & Examples

### 2.1 Storage API
- **`V07Platform.storage.get(key)`**:
  - *Purpose*: Retrieve a stored JSON value from the plugin's isolated partition.
  - *Permissions Required*: `storage:isolated`
  - *Errors*: `E_PERMISSION_DENIED` if permission was omitted in manifest.
- **`V07Platform.storage.set(key, value)`**:
  - *Purpose*: Persist an object/primitive. Serialized directly into SQLite.
  - *Example*:
    ```javascript
    await V07Platform.storage.set('active_streak', 14);
    ```

### 2.2 Event Bus API
- **`V07Platform.events.on(event, handler)`**:
  - *Purpose*: Listen to core or inter-plugin domain events.
  - *Supported Core Events*:
    - `day:start` (Morning checkin completed)
    - `day:end` (Evening review completed)
    - `session:start` (Focus stopwatch started)
    - `session:stop` (Focus stopwatch stopped)
    - `task:completed` (Task checked off)
  - *Example*:
    ```javascript
    const unsub = V07Platform.events.on('day:start', (dayData) => {
      console.log('New day initialized:', dayData.date);
    });
    ```

### 2.3 UI Extension Contracts
- **Widget Sizing Handshake**:
  Widgets loaded in dashboard slots report content dimensions via `V07Platform.ui.requestResize(height)` to ensure seamless responsive scaling without layout shift.
- **Unified Toast Feedback**:
  ```javascript
  V07Platform.ui.toast('Habit streak updated (+50 XP)', 'success');
  ```

### 2.4 Structured Domain Records API
Plugins requiring historical series, session logs, or timeline entries persist records in an isolated, indexed table (`plugin_events` in SQLite) and synchronized collection (`users/{uid}/plugins/{pluginId}/events` in Firestore):
- **`nativeInvoke('events:insert', { id, category, payloadJson, timestamp, date })`**
- **`nativeInvoke('events:query', { category, date, limit })`**
- **`nativeInvoke('events:delete', { id })`**

### 2.5 Host HTTP Loopback Endpoints
The platform exposes REST endpoints for plugins or companion tools:
- **`GET /api/v3/plugins/{pluginId}/events?category=...&date=...`**: Query plugin domain records.
- **`POST /api/v3/plugins/{pluginId}/events`**: Insert domain record.
- **`DELETE /api/v3/plugins/{pluginId}/events?id={id}`**: Delete record with tombstone generation.
- **`GET /api/v3/plugins/{pluginId}/storage?key={key}`**: Read isolated KV storage.
- **`POST /api/v3/plugins/{pluginId}/storage`**: Write isolated KV storage.
- **`DELETE /api/v3/plugins/{pluginId}/storage?key={key}`**: Delete key.
