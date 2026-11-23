package com.pminteractive.pmtours.support

import android.content.SharedPreferences

class InMemorySharedPreferences : SharedPreferences {
    private val datos = mutableMapOf<String, Any?>()

    override fun getAll(): MutableMap<String, *> = datos.toMutableMap()

    override fun getString(key: String?, defValue: String?): String? =
        datos[key] as? String ?: defValue

    override fun getStringSet(key: String?, defValues: MutableSet<String>?): MutableSet<String>? {
        @Suppress("UNCHECKED_CAST")
        return datos[key] as? MutableSet<String> ?: defValues
    }

    override fun getInt(key: String?, defValue: Int): Int = datos[key] as? Int ?: defValue
    override fun getLong(key: String?, defValue: Long): Long = datos[key] as? Long ?: defValue
    override fun getFloat(key: String?, defValue: Float): Float = datos[key] as? Float ?: defValue
    override fun getBoolean(key: String?, defValue: Boolean): Boolean = datos[key] as? Boolean ?: defValue

    override fun contains(key: String?): Boolean = datos.containsKey(key)

    override fun edit(): SharedPreferences.Editor = Editor()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {}

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {}

    private inner class Editor : SharedPreferences.Editor {
        private val pendientes = mutableMapOf<String, Any?>()
        private var clearAll = false

        override fun putString(key: String?, value: String?): SharedPreferences.Editor {
            pendientes[key!!] = value; return this
        }

        override fun putStringSet(key: String?, values: MutableSet<String>?): SharedPreferences.Editor {
            pendientes[key!!] = values; return this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor {
            pendientes[key!!] = value; return this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor {
            pendientes[key!!] = value; return this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor {
            pendientes[key!!] = value; return this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor {
            pendientes[key!!] = value; return this
        }

        override fun remove(key: String?): SharedPreferences.Editor {
            pendientes[key!!] = null; return this
        }

        override fun clear(): SharedPreferences.Editor {
            clearAll = true; return this
        }

        override fun commit(): Boolean {
            if (clearAll) datos.clear()
            pendientes.forEach { (k, v) -> if (v == null) datos.remove(k) else datos[k] = v }
            return true
        }

        override fun apply() { commit() }
    }
}
