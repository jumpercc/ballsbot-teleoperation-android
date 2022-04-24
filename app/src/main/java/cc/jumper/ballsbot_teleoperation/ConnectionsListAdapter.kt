package cc.jumper.ballsbot_teleoperation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.databinding.ConnectionsListConnectionBinding

class ConnectionsListAdapter(private val onItemClicked: (Connection) -> Unit) :
    ListAdapter<Connection, ConnectionsListAdapter.ItemViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
         val adapterLayout = LayoutInflater.from(parent.context)
             .inflate(R.layout.connections_list_connection, parent, false)
         return ItemViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val current = getItem(position)
        holder.itemView.setOnClickListener {
            onItemClicked(current)
        }
        holder.bind(current)
    }

    class ItemViewHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        private val itemDescription: TextView = view.findViewById(R.id.item_description)

        fun bind(connection: Connection) {
            itemDescription.text = connection.description
        }
    }

    companion object {
        private val DiffCallback = object : DiffUtil.ItemCallback<Connection>() {
            override fun areItemsTheSame(oldItem: Connection, newItem: Connection): Boolean {
                return oldItem === newItem
            }

            override fun areContentsTheSame(oldConnection: Connection, newConnection: Connection): Boolean {
                return oldConnection.hostName == newConnection.hostName &&
                    oldConnection.port == newConnection.port
            }
        }
    }
}