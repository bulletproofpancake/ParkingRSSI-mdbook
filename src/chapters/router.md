# Router

The `Router` class contains the information for each router the app connects to.
It is located at `app > java > com.silentrald.parkingrssi > Router.kt`.

## Fields

| Field    | Declaration                    | Description                                                                                                             |
| -------- | ------------------------------ | ----------------------------------------------------------------------------------------------------------------------- |
| bssid    | `private var bssid: Long = 0L` | The network address received from the [wifi manager](https://developer.android.com/guide/topics/connectivity/wifi-scan) |
| bssidStr | `private var bssidStr: String` | The network address received from the [wifi manager](https://developer.android.com/guide/topics/connectivity/wifi-scan) |
| name     | `private var name: String`     | The name of the network.                                                                                                |
| row      | `var row: Int = 0`             | Which row in the router matrix the router is set                                                                        |
| col      | `var col: Int = 0`             | Which column in the router matrix the router is set                                                                     |

## Constructors

### Router(Long, String)

Creates a router using a `Long` and `String` parameters.

#### Snippet

```kt
constructor(bssid: Long, name: String) {
    this.bssid = bssid
    this.name = name

    this.bssidStr = convertBSSIDToString(bssid)
}
```

### Router(String, String)

Creates a router using a `String` and `String` parameters.

#### Snippet

```kt
constructor(bssidStr: String, name: String) {
    this.bssidStr = bssidStr
    this.name = name

    this.bssid = convertBSSIDToLong(bssidStr)
}
```

## Methods

### getBSSID

Returns the `bssid` of the router.

#### Snippet

```kt
fun getBSSID(): Long{
    return this.bssid
}
```

### getBSSIDStr

Returns the `bssidStr` of the router.

#### Snippet

```kt
fun getBSSIDStr(): String{
    return this.bssidStr
}
```

### getName

Returns the `name` of the router.

#### Snippet

```kt
fun getName(): String{
    return this.name
}
```

### convertBSSIDToLong

Formats the `bssidStr` to a `Long` type.

#### Snippet

```kt
private fun convertBSSIDToLong(bssidStr: String): Long {
    val str = bssidStr.replace(":", "")

    return str.toLong(16)
}
```

### convertBSSIDToString

Formats the `bssid` to a `String` type.

#### Snippet

```kt
private fun convertBSSIDToString(bssid: Long): String {
    var bssidStr = bssid.toString(16)

    bssidStr = "${
      bssidStr.substring(0, 2)
    }:${
      bssidStr.substring(2, 4)
    }:${
      bssidStr.substring(4, 6)
    }:${
      bssidStr.substring(6, 8)
    }:${
      bssidStr.substring(8, 10)
    }:${
      bssidStr.substring(10)
    }"

    return bssidStr
}
```

## Overrides

### toString

Overrides the default toString method to the string format.

#### Snippet
```kt
override fun toString(): String{
    return "$bssid: $name<$bssidStr> ($row, $col)"
}
```