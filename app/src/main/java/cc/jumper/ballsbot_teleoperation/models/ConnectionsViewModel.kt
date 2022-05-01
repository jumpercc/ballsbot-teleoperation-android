package cc.jumper.ballsbot_teleoperation.models

import androidx.lifecycle.*
import cc.jumper.ballsbot_teleoperation.data.Connection
import cc.jumper.ballsbot_teleoperation.data.ConnectionDao
import cc.jumper.ballsbot_teleoperation.network.isCertificateValid
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ConnectionsViewModel(private val connectionsDao: ConnectionDao) : ViewModel() {

    val allConnections: LiveData<List<Connection>> = connectionsDao.getConnections().asLiveData()

    fun getConnection(connectionId: Int): LiveData<Connection> {
        return connectionsDao.getConnection(connectionId).asLiveData()
    }

    fun deleteConnection(connection: Connection) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionsDao.delete(connection)
        }
    }

    private fun validPort(port: String): Boolean {
        try {
            if (port.toInt() > 0) {
                return true
            }
        } catch (e: Throwable) {
            // return false below
        }
        return false
    }

    fun isValidEntry(
        description: String,
        hostName: String,
        port: String,
        key: String,
        certificate: String
    ): Boolean {
        return (
                description.isNotBlank()
                        && hostName.isNotBlank()
                        && port.isNotBlank()
                        && key.isNotBlank()
                        && validPort(port)
                        && certificate.isNotBlank()
                        && isCertificateValid(certificate)
                )
    }

    fun addConnection(
        description: String,
        hostName: String,
        port: Int,
        key: String,
        certificate: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionsDao.insert(
                Connection(0, description, hostName, port, key, certificate)
            )
        }
    }

    fun updateConnection(
        connection: Connection,
        description: String,
        hostName: String,
        port: Int,
        key: String,
        certificate: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            connectionsDao.update(
                connection.copy(
                    description = description,
                    hostName = hostName,
                    port = port,
                    key = key,
                    certificate = certificate,
                )
            )
        }
    }
}

class ConnectionsViewModelFactory(private val itemDao: ConnectionDao) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ConnectionsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ConnectionsViewModel(itemDao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}