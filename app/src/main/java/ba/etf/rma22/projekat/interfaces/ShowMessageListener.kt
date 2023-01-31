package ba.etf.rma22.projekat.interfaces

import ba.etf.rma22.projekat.data.models.Grupa

interface ShowMessageListener {
    fun ubaciPoruku(grupa: Grupa?)
}