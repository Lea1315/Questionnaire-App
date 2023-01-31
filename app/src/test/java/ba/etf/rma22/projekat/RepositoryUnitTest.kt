package ba.etf.rma22.projekat


import ba.etf.rma22.projekat.data.models.*
import ba.etf.rma22.projekat.data.repositories.*

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.hasItem
import org.hamcrest.MatcherAssert.assertThat
import org.junit.FixMethodOrder
import org.junit.Test
import org.junit.runners.MethodSorters
import java.net.URL


@FixMethodOrder(MethodSorters.NAME_ASCENDING)
class RepositoryUnitTest {
    suspend fun obrisi(){
        var client: OkHttpClient = OkHttpClient()
        var builder: Request.Builder = Request.Builder()
            .url(URL(ApiConfig.baseURL + "/student/" + AccountRepository.acHash + "/upisugrupeipokusaji"))
            .delete()
        var request: Request = builder.build()
        withContext(Dispatchers.IO) {
            var response: Response = client.newCall(request).execute()
        }
    }
    @Test
    fun a0_pripremiPocetak() = runBlocking {
        obrisi()
    }

    @Test
    fun a1_getIstrazivanja() = runBlocking {
        var istrazivanja = IstrazivanjeIGrupaRepository.getIstrazivanja()
        assertThat(istrazivanja,CoreMatchers.notNullValue())
        assertThat(istrazivanja?.size,CoreMatchers.equalTo(6))

    }

    @Test
    fun a2_getGrupe() = runBlocking {
        var grupe = IstrazivanjeIGrupaRepository.getGrupe()
        assertThat(grupe,CoreMatchers.notNullValue())
        assertThat(grupe?.size,CoreMatchers.equalTo(8))
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
        var pitanjaIOdgovori = PitanjeAnketaRepository.getPitanja(poceti!![poceti.size-1]?.AnketumId)
        var result = OdgovorRepository.postaviOdgovorAnketa(poceti!![poceti.size-1]?.id,pitanjaIOdgovori!![0]?.id,1)
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