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