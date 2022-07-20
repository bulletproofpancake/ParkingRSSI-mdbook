package com.silentrald.parkingrssi

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class RecyclerViewAdapter(
    val datasets: ArrayList<ArrayList<Float>> = arrayListOf(),
    val labels: ArrayList<Int> = arrayListOf()
) : RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvLabel: TextView = view.findViewById(R.id.tv_label)
        val tvData: TextView = view.findViewById(R.id.tv_data)
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        // Create a new view, which defines the UI of the list item
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.layout_datapoint, viewGroup, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.tvLabel.text = labels[position].toString()
        holder.tvData.text = datasets[position].toString()
    }

    override fun getItemCount(): Int {
        return datasets.size
    }

    fun removeItem(pos: Int) {
        labels.removeAt(pos)
        datasets.removeAt(pos)

        notifyDataSetChanged()
    }
}