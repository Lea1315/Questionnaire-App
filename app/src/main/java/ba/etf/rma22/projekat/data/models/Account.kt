package ba.etf.rma22.projekat.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity
data class Account(@PrimaryKey val id: Int, val student: String, val hash: String)