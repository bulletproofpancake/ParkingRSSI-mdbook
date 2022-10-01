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