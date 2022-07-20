package com.silentrald.parkingrssi

class Router {
    companion object {
        fun convertBSSIDToLong(bssidStr: String): Long {
            val str = bssidStr.replace(":", "")

            return str.toLong(16)
        }

        fun convertBSSIDToString(bssid: Long): String {
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
    }
}