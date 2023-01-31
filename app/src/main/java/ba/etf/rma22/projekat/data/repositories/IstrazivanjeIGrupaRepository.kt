package ba.etf.rma22.projekat.data.repositories

import androidx.room.*
import ba.etf.rma21.projekat.data.AppDatabase
import ba.etf.rma22.projekat.data.models.Grupa
import ba.etf.rma22.projekat.data.models.Istrazivanje
import ba.etf.rma22.projekat.data.models.parseGrupu
import ba.etf.rma22.projekat.data.models.parseIstrazivanje
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class IstrazivanjeIGrupaRepository {
    companion object {
        lateinit var db: AppDatabase
        suspend fun getIstrazivanja(offset: Int): List<Istrazivanje> {
            val istrazivanja = arrayListOf<Istrazivanje>()
            val rutaIstrazivanja = "${ApiConfig.baseURL}/istrazivanje?offset=$offset"
            return withContext(Dispatchers.IO) {
                try {
                    (URL(rutaIstrazivanja).openConnection() as HttpURLConnection).run {
                        val istrazivanjaJSONString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val istrazivanjaJSON = JSONArray(istrazivanjaJSONString)
                        for (i in 0 until istrazivanjaJSON.length()) {
                            istrazivanja.add(
                                istrazivanjaJSON.getJSONObject(i).parseIstrazivanje()
                            )
                        }
                        db.istrazivanjeDao().novaIstrazivanja(istrazivanja)
                        istrazivanja
                    }
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        suspend fun getIstrazivanja(): List<Istrazivanje> {
            var offset = 1
            val istrazivanja = arrayListOf<Istrazivanje>()
            return withContext(Dispatchers.IO) {
                try {
                    while (true) {
                        val istrazivanjaZaOffset = getIstrazivanja(offset)
                        if (istrazivanjaZaOffset.isEmpty()) break
                        istrazivanja.addAll(istrazivanjaZaOffset)
                        offset += 1
                    }
                    istrazivanja
                } catch (e: Exception) {
                    db.istrazivanjeDao().dajIstrazivanja()
                }
            }
        }

        suspend fun getGrupe(): List<Grupa> {
            val grupe = arrayListOf<Grupa>()
            val rutaGrupe = "${ApiConfig.baseURL}/grupa"
            return withContext(Dispatchers.IO) {
                try {
                    val istrazivanja = getIstrazivanja()
                    (URL(rutaGrupe).openConnection() as HttpURLConnection).run {
                        val grupeJSONString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val grupeJSON = JSONArray(grupeJSONString)
                        for (i in 0 until grupeJSON.length()) {
                            val grupaJSON = grupeJSON.getJSONObject(i)
                            val istrazivanjeZaGrupu =
                                istrazivanja.first { it.id == grupaJSON.getInt("IstrazivanjeId") }
                            grupe.add(grupaJSON.parseGrupu(istrazivanjeZaGrupu.naziv))
                        }
                    }
                    db.grupaDao().ubaciGrupe(grupe)
                    grupe
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        suspend fun getGrupeZaIstrazivanje(idIstrazivanja: Int): List<Grupa> {
            val grupe = arrayListOf<Grupa>()
            val rutaIstrazivanje = "${ApiConfig.baseURL}/istrazivanje/$idIstrazivanja"
            val rutaGrupe = "${ApiConfig.baseURL}/grupa"
            return withContext(Dispatchers.IO) {
                try {
                    (URL(rutaIstrazivanje).openConnection() as HttpURLConnection).run {
                        val istrazivanjeJSONString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val istrazivanjeJSON = JSONObject(istrazivanjeJSONString)
                        (URL(rutaGrupe).openConnection() as HttpURLConnection).run {
                            val grupeJSONString =
                                this.inputStream.bufferedReader().use { it.readText() }
                            val grupeJSON = JSONArray(grupeJSONString)
                            for (i in 0 until grupeJSON.length()) {
                                val grupaJSON = grupeJSON.getJSONObject(i)
                                if (idIstrazivanja == grupaJSON.getInt("IstrazivanjeId")) {
                                    grupe.add(grupaJSON.parseGrupu(istrazivanjeJSON.getString("naziv")))
                                }
                            }
                        }
                    }
                    grupe
                } catch (e: Exception) {
                    emptyList()
                }
            }
        }

        suspend fun getGrupeZaAnketu(idAnkete: Int): List<Grupa> {
            val grupe = arrayListOf<Grupa>()
            val istrazivanja = getIstrazivanja()
            val rutaGrupe = "${ApiConfig.baseURL}/anketa/$idAnkete/grupa"
            return try {
                (URL(rutaGrupe).openConnection() as HttpURLConnection).run {
                    val grupeJSONString =
                        this.inputStream.bufferedReader().use { it.readText() }
                    val grupeJSON = JSONArray(grupeJSONString)
                    for (i in 0 until grupeJSON.length()) {
                        val grupaJSON = grupeJSON.getJSONObject(i)
                        grupe.add(grupaJSON.parseGrupu(istrazivanja.first {
                            it.id == grupaJSON.getInt(
                                "IstrazivanjeId"
                            )
                        }.naziv))
                    }
                }
                grupe
            } catch (e: Exception) {
                emptyList()
            }
        }

        suspend fun upisiUGrupu(idGrupa: Int): Boolean {
            return withContext(Dispatchers.IO) {
                try {
                    val rutaZaUpis =
                        "${ApiConfig.baseURL}/grupa/$idGrupa/student/${AccountRepository.acHash}"
                    (URL(rutaZaUpis).openConnection() as HttpURLConnection).run {
                        requestMethod = "POST"
                        val result = inputStream.bufferedReader().use { it.readText() }
                        val msg = JSONObject(result).getString("message")
                        println(msg)
                        val user = AccountRepository.getUser() ?: return@withContext false
                        val match = "Student ${user.student} je dodan u grupu Grupa"
                        println(match)
                        if (msg.contains(match)) return@withContext true
                        false
                    }
                } catch (e: Exception) {
                    println(e)
                    false
                }
            }
        }

        suspend fun getUpisaneGrupe(): List<Grupa> {
            val istrazivanja = getIstrazivanja()
            return withContext(Dispatchers.IO) {
                try {
                    val upisaneGrupe = arrayListOf<Grupa>()
                    val rutaUpisaneGrupe =
                        "${ApiConfig.baseURL}/student/${AccountRepository.acHash}/grupa"
                    (URL(rutaUpisaneGrupe).openConnection() as HttpURLConnection).run {
                        val upisaneGrupeString =
                            this.inputStream.bufferedReader().use { it.readText() }
                        val upisaneGrupeJSON = JSONArray(upisaneGrupeString)
                        for (i in 0 until upisaneGrupeJSON.length()) {
                            val grupaJSON = upisaneGrupeJSON.getJSONObject(i)
                            val istrazivanjeZaGrupu =
                                istrazivanja.first { it.id == grupaJSON.getInt("IstrazivanjeId") }
                            upisaneGrupe.add(grupaJSON.parseGrupu(istrazivanjeZaGrupu.naziv))
                        }
                    }
                    db.grupaDao().ubaciGrupe(upisaneGrupe)
                    upisaneGrupe
                } catch (e: Exception) {
                    db.grupaDao().dajGrupe()
                }
            }
        }
    }
}

@Dao
interface IstrazivanjeDao {
    @Query("SELECT * FROM istrazivanje")
    suspend fun dajIstrazivanja(): List<Istrazivanje>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciIstrazivanja(istrazivanja: List<Istrazivanje>)

    @Query("DELETE FROM istrazivanje")
    suspend fun obrisiIstrazivanja()

    @Transaction
    suspend fun novaIstrazivanja(istrazivanja: List<Istrazivanje>) {
        obrisiIstrazivanja()
        ubaciIstrazivanja(istrazivanja)
    }
}

@Dao
interface GrupaDao {
    @Query("SELECT * FROM grupa")
    suspend fun dajGrupe(): List<Grupa>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun ubaciGrupe(grupe: List<Grupa>)
}
