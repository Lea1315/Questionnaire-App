package ba.etf.rma21.projekat.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import ba.etf.rma22.projekat.data.models.*
import ba.etf.rma22.projekat.data.repositories.*

@Database(
    entities = arrayOf(
        Account::class,
        Anketa::class,
        AnketaTaken::class,
        Istrazivanje::class,
        Grupa::class,
        Pitanje::class,
        Odgovor::class
    ), version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun accountDao(): AccountDao
    abstract fun anketaDao(): AnketaDao
    abstract fun anketaTakenDao(): AnketaTakenDao
    abstract fun istrazivanjeDao(): IstrazivanjeDao
    abstract fun grupaDao(): GrupaDao
    abstract fun pitanjeDao(): PitanjeDao
    abstract fun odgovorDao(): OdgovorDao

    companion object {
        private var INSTANCE: AppDatabase? = null
        fun setInstance(appdb: AppDatabase): Unit {
            INSTANCE = appdb
        }

        fun getInstance(context: Context): AppDatabase {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = buildRoomDB(context)
                }
            }
            return INSTANCE!!
        }

        private fun buildRoomDB(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "RMA22DB"
            ).build()
    }
}