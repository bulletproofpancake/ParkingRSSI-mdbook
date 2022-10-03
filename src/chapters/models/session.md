# Session

The `Session` class contains information for saving and restoring sessions
which are configurations for the routers.

## Snippet

```kt
class Session(
	val id: Long,
	var name: String,
	var rows: Int,
	var cols: Int
) {
	override fun toString(): String {
		return "Id: $id; Name: $name; Rows: $rows; Cols: $cols\n"
	}
}
```

## Fields

| Field | Description                                      |
| ----- | ------------------------------------------------ |
| id    | Used to identify the record in the database      |
| name  | The name set by the user to identify the session |
| rows  | The number of rows for the router matrix         |
| cols  | The number of columns for the router matrix      |

## Overrides

### toString

Overrides the default toString method to the string format.

#### Snippet

```kt
override fun toString(): String{
	return "Id: $id; Name: $name; Rows: $rows; Cols: $cols\n"
}
```