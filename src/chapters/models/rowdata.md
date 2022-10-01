# RowData

The `RowData` class is used for saving and restoring the RSSI values recorded.

## Snippet

```kt
class RowData (
	val recordId: Long,
	val label: Int
) {
	val values = arrayListOf<Float>()

	fun addVal(value: Float) {
		values.add(value)
	}

	override fun toString(): String {
		return "$recordId: $label; $values"
	}
}
```