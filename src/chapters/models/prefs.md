# Prefs

The `Prefs` class is responsible for the default preferences between sessions.

## Snippet

```kt
class Prefs(context: Context) {
  private val PREFS = "parkingrssi"

  private val CAPACITY_KEY = "capacity"
  private val SESSION_KEY = "session"

  private val CAPACITY_DEFAULT = 10
  private val SESSION_NULL = 0L

  private var sp: SharedPreferences

  init {
    sp = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
  }

  fun setCapacity(value: Int) {
    sp.edit().putInt(CAPACITY_KEY, value).apply()
  }

  fun setSession(value: Long) {
    sp.edit().putLong(SESSION_KEY, value).apply()
  }

  fun getCapacity(): Int {
    return sp.getInt(CAPACITY_KEY, CAPACITY_DEFAULT)
  }

  fun getSession(): Long {
    return sp.getLong(SESSION_KEY, SESSION_NULL)
  }

  companion object {
    // Singleton
    private var sInstance: Prefs? = null
    @Synchronized
    fun getInstance(ctx: Context): Prefs? {
      if (sInstance == null) {
        sInstance = Prefs(ctx)
      }
      return sInstance
    }
  }
}
```

## Fields

| Fields           | Declaration                             | Description                                                                                                                                   |
| ---------------- | --------------------------------------- | --------------------------------------------------------------------------------------------------------------------------------------------- |
| PREFS            | `private val PREFS = "parkingrssi"`     | Used to identify the preferences saved on the device                                                                                          |
| CAPACITY_KEY     | `private val CAPACITY_KEY = "capacity"` | Used to identify the saved capacity on the device                                                                                             |
| SESSION_KEY      | `private val SESSION_KEY = "session"`   | Used to identify the saved session on the device                                                                                              |
| CAPACITY_DEFAULT | `private val CAPACITY_DEFAULT = 10`     | The default capacity saved to the device if no capacity has been set                                                                          |
| SESSION_NULL     | `private val SESSION_NULL = 0L`         | The default session saved to the device if no session has been set                                                                            |
| sp               | `private var sp: SharedPreferences`     | [SharedPreferences](https://developer.android.com/reference/android/content/SharedPreferences) which are used to access data from the device. |
