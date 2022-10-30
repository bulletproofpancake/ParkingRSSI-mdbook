# Singletons

Singletons are classes which only has a single instance during the runtime of the application.
When a class is used by the program, it is often instantiated with a constructor like so:

```kt
val session = Session(0, "Session 1", 2, 3);
```

This creates an instance of a session with the following data:

| variable | id  | name      | rows | cols |
| -------- | --- | --------- | ---- | ---- |
| session  | 0   | Session 1 | 2    | 3    |

This means that every time we use the variable `session`,
we would always get the following data
(unless it has been changed, but for this example let us assume that it doesn't).

This means that if we wish to use create another session, we would create another instance like so:

```kt
val session2 = Session(1, "Session 2", 4, 4)
```

Which would create an instance of a session with the following data:

| variable | id  | name      | rows | cols |
| -------- | --- | --------- | ---- | ---- |
| session2 | 1   | Session 2 | 4    | 4    |

So if we are to print out `session.id` and `session2.id` we would have `0` and `1` as our results respectively.

However, there are times where we only need a single instance of a class
and having multiple instances of these classes would be disastrous as it can lead to inaccurate data.
In this application, the [`DBHelper`](dbhelper.md) and [`Prefs`](prefs.md) classes are singletons because
we do not want our records and settings be saved to different instances which can cause sync issues.

It is important to know that instances are not limited to separate variables.
Since variables are only containers,its contents can be easily replaced.
For example, we created an instance of `DBHelper` and stored it in the `dbHelper` variable.
Somewhere along the line, `DBHelper` has been instantiated and kept in the `dbHelper` variable.
This would mean that `dbHelper` would no longer be able to access all of the data recorded in it before.

Singletons help prevent this issue because they ensure that only a single instance of the class lives.

An implementation of a Singleton can be seen like so:

```kt
// A container for the singleton instance
private var sInstance: DBHelper? = null
@Synchronized
fun getInstance(ctx: Context): DBHelper? {
    // Checks if there is an existing instance
    if (sInstance == null) {
        // if there is none, an instance is created
        sInstance = DBHelper(ctx.applicationContext)
    }
    // Returns the instance to whatever needs it
    return sInstance
}
```