package com.silentrald.parkingrssi

import android.content.Context
import android.util.Log
import kotlin.math.pow

class KNNClassifier(var k: Int = 3) { //k is the literal property for KNN
    var inputSize: Int = 6 //depende kung ilang yung number of routers
    val matrix = arrayListOf<ArrayList<Float>>() //values ng RSSI, 2D matrix siya
    val labels = arrayListOf<Int>()

    fun addPoint(point: ArrayList<Float>, label: Int) { //training part
        if (point.size != inputSize) {
            return
        }

        matrix.add(point)
        labels.add(label)
    }

    // Euclidean Distance
    //E(v1-v2)^2
    private fun calculateDistance(ps1: ArrayList<Float>, ps2: ArrayList<Float>): Float { //evaluation part
        if (ps1.size != ps2.size) return -1f //kuhanin lahat ng distances dito ng bagong point.

        var distance = 0f
        for (i in 0 until ps1.size) {
            distance += (ps1[i] - ps2[i]).pow(2)
        }
        return distance
    }

    fun predict(point: ArrayList<Float>): Int {
        val distances = arrayListOf<Pair<Float, Int>>()
        var distance = 0f
        for (i in 0 until matrix.size) {
            distance = calculateDistance(point, matrix[i])
            distances.add(Pair(distance, labels[i]))
        }
        Log.i("KNN", distances.toString())

        distances.sortWith(compareBy { it.first })
        val hashmap = hashMapOf<Int, Int>()
        val min = k.coerceAtMost(matrix.size)
        for (i in 0 until min) {
            hashmap[i] = hashmap.getOrDefault(i, 0) + 1
        }
        Log.i("KNN", hashmap.toString())

        var max = -1 // tie breaking, randomized
        val outputs = arrayListOf<Int>()
        for ((key, value) in hashmap) {
            if (value > max) {
                max = value
                outputs.clear()
                outputs.add(key)
            } else if (value == max) {
                outputs.add(key)
            }
        }
        Log.i("KNN", outputs.toString())

        val output = if (outputs.size > 1) {
            distances[0].second
        } else {
            outputs[0]
        }

        return output
    }

    fun loadMatrix(context: Context) { //q
        val db = DBHelper(context, null)
        val data = db.getData()
        db.close()

        inputSize = data.input

        matrix.clear()
        matrix.addAll(data.matrix)

        labels.clear()
        labels.addAll(data.labels)

        Log.i("KNN", matrix.toString())
        Log.i("KNN", labels.toString())
    } //querry sa database

    fun saveMatrix(context: Context) {
        val db = DBHelper(context, null)
        db.setData(matrix, labels, inputSize)
        db.close()
    } //querry sa database
}