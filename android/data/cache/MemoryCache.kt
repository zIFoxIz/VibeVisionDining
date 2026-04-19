package com.example.vibevision.data.cache

class MemoryCache(private val ttlMs: Long = 120_000L) {
    private data class Entry(val value: Any, val timestamp: Long)

    private val storage: MutableMap<String, Entry> = mutableMapOf()

    @Suppress("UNCHECKED_CAST")
    fun <T> get(key: String): T? {
        val entry = storage[key] ?: return null
        val age = System.currentTimeMillis() - entry.timestamp
        if (age > ttlMs) {
            storage.remove(key)
            return null
        }
        return entry.value as? T
    }

    fun put(key: String, value: Any) {
        storage[key] = Entry(value = value, timestamp = System.currentTimeMillis())
    }

    fun invalidate(key: String) {
        storage.remove(key)
    }

    fun clear() {
        storage.clear()
    }
}
