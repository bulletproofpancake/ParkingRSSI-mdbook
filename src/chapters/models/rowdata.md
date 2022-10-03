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

## Fields

| Field    | Declaration                       | Description                                                      |
| -------- | --------------------------------- | ---------------------------------------------------------------- |
| recordID | `val recordID: Long`              | Used to identify the record in the database.                     |
| label    | `val label: Int`                  | The number of cars occupied for the set of RSSI values recorded. |
| values   | `val values = arrayListOf<Float>` | The RSSI values for this row.                                    |

## Methods

### addVal

The `addVal` method adds the RSSI value from a router to `values`.

#### Snippet

```kt
fun addVal(value: Float){
	values.add(value)
}
```

#### Parameters

| Parameter | Declaration    | Description                            |
| --------- | -------------- | -------------------------------------- |
| value     | `value: Float` | The RSSI value received from a router. |

## Overrides

### toString

Overrides the default toString method to the string format.

#### Snippet

```kt
override fun toString(): String{
	return "$recordID: $label; $values"
}
```