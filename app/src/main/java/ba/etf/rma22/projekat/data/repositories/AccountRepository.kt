package ba.etf.rma22.projekat.data.repositories

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.Account
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class AccountRepository {
    companion object {
        lateinit var db: AppDatabase
        var acHash = "b6c3bb54-0628-4eb7-aafd-6c0d37e2b147"

        suspend fun postaviHash(acHash: String): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val trenutni = getUser()
                    if (this@Companion.acHash != acHash || trenutni == null || trenutni.student.isBlank()) {
                        db.clearAllTables()
                        db.accountDao().ubaciKorisnika(Account(1, "", acHash))
                        this@Companion.acHash = acHash
                    }
                    val url = ApiConfig.baseURL + "/student/$acHash"
                    val URL = URL(url)
                    (URL.openConnection() as HttpURLConnection).run {
                        val result = this.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(result)
                        if (!json.has("message")) {
                            db.accountDao().izmijeniEmail(json.getString("student"))
                            true
                        } else
                            false
                    }
                } catch (e: Exception) {
                    false
                }
            }
        }

        suspend fun getHash() = acHash

        suspend fun getUser() = withContext(Dispatchers.IO) {
            try {
                db.accountDao().dajKorisnika()
            } catch (e: Exception) {
                null
            }
        }
    }
}

@Dao
interface AccountDao {
    @Query("DELETE from account")
    suspend fun obrisiKorisnika()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciKorisnika(korisnik: Account)

    @Query("SELECT * FROM account WHERE id=1")
    suspend fun dajKorisnika(): Account

    @Query("UPDATE account SET student=:email WHERE id=1")
    suspend fun izmijeniEmail(email: String)
}