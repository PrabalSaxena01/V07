package com.v07.v3.core.plugin.handlers

import com.v07.v3.core.data.EventRepository
import org.json.JSONArray
import org.json.JSONObject

class StorageBridgeHandler(
    private val repository: EventRepository
) : BridgeHandler {

    override fun canHandle(method: String): Boolean {
        return method.startsWith("storage:")
    }

    override fun handle(pluginId: String, method: String, params: JSONObject): String? {
        return when (method) {
            "storage:get" -> {
                val key = params.optString("key")
                val value = repository.getPluginValue(pluginId, key)
                val res = JSONObject().apply {
                    put("found", value != null)
                    put("value", value ?: JSONObject.NULL)
                }
                bridgeSuccess(res)
            }
            "storage:set" -> {
                val key = params.optString("key")
                val value = params.optString("value")
                val ok = repository.setPluginValue(pluginId, key, value)
                val res = JSONObject().put("success", ok)
                bridgeSuccess(res)
            }
            "storage:delete" -> {
                val key = params.optString("key")
                val ok = repository.deletePluginValue(pluginId, key)
                val res = JSONObject().put("success", ok)
                bridgeSuccess(res)
            }
            "storage:keys" -> {
                val keys = repository.getAllPluginKeys(pluginId)
                val arr = JSONArray()
                keys.forEach { arr.put(it) }
                val res = JSONObject().put("keys", arr)
                bridgeSuccess(res)
            }
            else -> null
        }
    }
}
