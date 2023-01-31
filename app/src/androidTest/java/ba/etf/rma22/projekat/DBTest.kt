package ba.etf.rma22.projekat

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import ba.etf.rma22.projekat.data.models.*
import ba.etf.rma22.projekat.data.repositories.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.*
import org.junit.Assert.assertThat
import org.junit.BeforeClass
import org.junit.FixMethodOrder
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.MethodSorters
import java.net.URL


/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(AndroidJUnit4::class)
class DBTest {

    private val countOdgovor = "SELECT COUNT(*) AS broj_odgovora FROM Odgovor"
    private val countIstrazivanje = "SELECT COUNT(*) AS broj_istrazivanja FROM Istrazivanje"
    private val countGrupa = "SELECT COUNT(*) AS broj_grupa FROM Grupa"
    private val countAnketa = "SELECT COUNT(*) AS broj_anketa FROM Anketa"
    private val countPitanje = "SELECT COUNT(*) AS broj_pitanja FROM Pitanje"

    private val describeOdgovor = "pragma table_info('Odgovor')"
    private val describeIstrazivanje = "pragma table_info('Istrazivanje')"
    private val describeGrupa = "pragma table_info('Grupa')"
    private val describeAnketa = "pragma table_info('Anketa')"
    private val describeAccount = "pragma table_info('Account')"
    private val describePitanje = "pragma table_info('Pitanje')"
    private val describeTable = mapOf(
        "Odgovor" to describeOdgovor,
        "Istrazivanje" to describeIstrazivanje,
        "Grupa" to describeGrupa,
        "Pitanje" to describePitanje,
        "Anketa" to describeAnketa,
        "Account" to describeAccount
    )
    private val kolone = mapOf(
        "Odgovor" to arrayListOf("id", "odgovoreno"),
        "Grupa" to arrayListOf("id", "naziv"),
        "Account" to arrayListOf("acHash"),
        "Istrazivanje" to arrayListOf("id", "naziv", "godina"),
        "Anketa" to arrayListOf("id", "naziv", "datumPocetak", "datumKraj", "trajanje"),
        "Pitanje" to arrayListOf("id", "naziv", "tekstPitanja", "opcije")
    )
    @get:Rule
    val intentsTestRule = ActivityScenarioRule(MainActivity::class.java)


    companion object {
        var pocentiHash: String = ""
        private lateinit var context: Context
        private var ktid: Int = 0
        lateinit var db:SQLiteDatabase
        @BeforeClass @JvmStatic
        fun createDb() = runBlocking {
            val scenario = ActivityScenario.launch(
                MainActivity::class.java
            )
            context = ApplicationProvider.getApplicationContext<Context>()

            var client: OkHttpClient = OkHttpClient()
            var builder: Request.Builder = Request.Builder()
                .url(URL(ApiConfig.baseURL + "/student/" + AccountRepository.getHash() + "/upisugrupeipokusaji"))
                .delete()
            var request: Request = builder.build()
            withContext(Dispatchers.IO) {
                var response: Response = client.newCall(request).execute()
                var odgovor: String = response.body.toString()
            }

            db = SQLiteDatabase.openDatabase(context.getDatabasePath("RMA22DB").absolutePath,null,SQLiteDatabase.OPEN_READONLY)


        }
    }

    private fun executeCountAndCheck(query: String, column: String, value: Long) {
        var rezultat = db.rawQuery(query,null)
        rezultat.moveToFirst()
        var brojOdgovora = rezultat.getLong(0)
        MatcherAssert.assertThat(brojOdgovora, `is`(equalTo(value)))
    }

    private fun checkColumns(query: String, naziv: String) {
        var rezultat = db.rawQuery(query,null)
        val list = (1..rezultat.count).map {
            rezultat.moveToNext()
            rezultat.getString(1)
        }
        assertThat(list, hasItems(*kolone[naziv]!!.toArray()))
    }


    @Test
    fun a0_pripremiPocetak() = runBlocking {
        AccountRepository.postaviHash(AccountRepository.acHash)
        assert(true)
    }

    @Test
    fun a01_postojeTabele() = runBlocking {
        for(tabela in arrayOf("Odgovor","Pitanje","Anketa","Istrazivanje")){
            for (kolona in kolone[tabela]!!){
                checkColumns(describeTable[tabela]!!,tabela)
            }
        }
    }
    @Test
    fun a1_getIstrazivanja() = runBlocking {
        var istrazivanja = IstrazivanjeIGrupaRepository.getIstrazivanja(1)
        assertThat(istrazivanja, CoreMatchers.notNullValue())
        assertThat(istrazivanja?.size,CoreMatchers.equalTo(5))
        executeCountAndCheck(countIstrazivanje,"broj_istrazivanja",5)


    }

    @Test
    fun a2_getGrupe() = runBlocking {
        var grupe = IstrazivanjeIGrupaRepository.getGrupe()
        assertThat(grupe,CoreMatchers.notNullValue())
        assertThat(grupe?.size,CoreMatchers.equalTo(8))
        executeCountAndCheck(countGrupa,"broj_grupa",8)
    }
    @Test
    fun a3_getUpisaneGrupe() = runBlocking {
        var upisane = IstrazivanjeIGrupaRepository.getUpisaneGrupe()
        assertThat(upisane?.size,CoreMatchers.equalTo(0))
    }

    @Test
    fun a4_upisiIProvjeri() = runBlocking {
        var grupe = IstrazivanjeIGrupaRepository.getGrupe()
        IstrazivanjeIGrupaRepository.upisiUGrupu(grupe!![0]?.id)
        var upisane = IstrazivanjeIGrupaRepository.getUpisaneGrupe()
        assertThat(upisane?.size,CoreMatchers.equalTo(1))
        assertThat(upisane?.intersect(grupe)?.size,CoreMatchers.equalTo(1))
    }
    @Test
    fun a4a_upisiIProvjeri() = runBlocking{
        var grupe = IstrazivanjeIGrupaRepository.getGrupe()
        IstrazivanjeIGrupaRepository.upisiUGrupu(6)
        var upisane = IstrazivanjeIGrupaRepository.getUpisaneGrupe()
        assertThat(upisane?.size,CoreMatchers.equalTo(2))
        assertThat(upisane!!.intersect(grupe!!).size,CoreMatchers.equalTo(2))
    }
    @Test
    fun a5_zapocniUpisaneAnkete() = runBlocking {
        var upisaneAnkete = AnketaRepository.getUpisane()
        var prije = TakeAnketaRepository.getPoceteAnkete()
        TakeAnketaRepository.zapocniAnketu(upisaneAnkete!![0]?.id)
        var poslije = TakeAnketaRepository.getPoceteAnkete()
        assertThat(prije,CoreMatchers.`is`(CoreMatchers.nullValue()))
        assertThat(poslije!!.size,CoreMatchers.equalTo(1))
    }

    @Test
    fun a6_zapocniNemogucuAnketu() = runBlocking {
        TakeAnketaRepository.zapocniAnketu(999)
        assertThat(TakeAnketaRepository.getPoceteAnkete()!!.size,CoreMatchers.equalTo(1))
    }

    @Test
    fun a7_provjeriBezOdgovora() = runBlocking {
        var poceti = TakeAnketaRepository.getPoceteAnkete()
        assertThat(OdgovorRepository.getOdgovoriAnketa(poceti!![poceti.size-1]?.AnketumId)!!.size,CoreMatchers.equalTo(0))
    }
    @Test
    fun a8_provjeriOdgovor() = runBlocking {
        var poceti = TakeAnketaRepository.getPoceteAnkete()
        var pitanja = PitanjeAnketaRepository.getPitanja(poceti!![poceti.size-1]?.AnketumId)
        var result = OdgovorRepository.postaviOdgovorAnketa(poceti!![poceti.size-1]?.id,pitanja!![0]?.id,1)
        executeCountAndCheck(countOdgovor,"broj_odgovora",1)
        assertThat(result,CoreMatchers.notNullValue())
        assertThat(result,CoreMatchers.equalTo(60))
        assertThat(OdgovorRepository.getOdgovoriAnketa(poceti!![poceti.size-1]?.AnketumId)!!.size,CoreMatchers.equalTo(1))
    }
    @Test
    fun a8a_provjeriNepoceteAnkete() = runBlocking {
        var ankete = AnketaRepository.getAll()
        var poceti = TakeAnketaRepository.getPoceteAnkete()!!.map { ktid -> ankete!!.first{id -> id!!.id == ktid!!.AnketumId}}
        var nepoceti = AnketaRepository.getUpisane()!!.minus(poceti)
        assertThat(nepoceti.size, `is`(2))
        assertThat(nepoceti, hasItem(nepoceti.first{it->it.naziv.contains("Anketa 5")}))
    }
    @Test
    fun a9_provjeriAnkete() = runBlocking {
        assertThat(AnketaRepository.getAll()!!.size,CoreMatchers.equalTo(6))
        executeCountAndCheck(countAnketa,"broj_anketa",6)
    }

    @Test
    fun a9a_provjeriPitanja() = runBlocking {
        var ankete = AnketaRepository.getAll()
        assertThat(ankete,CoreMatchers.notNullValue())
        var pitanja = PitanjeAnketaRepository.getPitanja(ankete!![0]?.id)
        assertThat(pitanja,CoreMatchers.notNullValue())
        assertThat(pitanja!!.size,CoreMatchers.equalTo(2))
    }

    fun checkProperties(propA:Collection<String>,propB:Collection<String>){
        for(trazeniProperty in propA){
            assertThat(propB,hasItem(trazeniProperty))
        }
    }
    @Test
    fun sveKlaseIspravne() {
        var pitanjeProperties = Pitanje::class.java.kotlin.members.map { it.name }
        var pitanjeTProperties = listOf("id","naziv","tekstPitanja","opcije")
        checkProperties(pitanjeTProperties,pitanjeProperties)

        var anketaProperties = Anketa::class.java.kotlin.members.map {it.name}
        var anketaTProperties = listOf("id","naziv","datumPocetak","datumKraj","trajanje")
        checkProperties(anketaTProperties,anketaProperties)

        var anketaTakenProperties = AnketaTaken::class.java.kotlin.members.map { it.name }
        var anketaTakenTProperties = listOf("id","student","datumRada","progres")
        checkProperties(anketaTakenTProperties,anketaTakenProperties)

        var grupaProperties = Grupa::class.java.kotlin.members.map { it.name }
        var grupaTProperties = listOf("id","naziv")
        checkProperties(grupaTProperties,grupaProperties)

        var istrazivanjeProperties = Istrazivanje::class.java.kotlin.members.map { it.name }
        var istrazivanjeTProperties = listOf("id","naziv","godina")
        checkProperties(istrazivanjeTProperties,istrazivanjeProperties)

        var odgovorProperties = Odgovor::class.java.kotlin.members.map { it.name }
        var odgovorTProperties = listOf("id","odgovoreno")
        checkProperties(odgovorTProperties,odgovorProperties)
    }

}