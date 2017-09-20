package com.runesuite.client.api.live

import com.runesuite.client.api.GrandExchangeOffer
import com.runesuite.client.raw.Client.accessor

object GrandExchangeOffers {

    val SIZE = accessor.grandExchangeOffers.size

    val all: List<GrandExchangeOffer> get() {
        return accessor.grandExchangeOffers.mapNotNull { it?.takeIf { it.id != 0 }?.let { GrandExchangeOffer(it) } }
    }

    fun get(): List<GrandExchangeOffer?> {
        return accessor.grandExchangeOffers.map { it?.takeIf { it.id != 0 }?.let { GrandExchangeOffer(it) } }
    }

    operator fun get(index: Int): GrandExchangeOffer? {
        return accessor.grandExchangeOffers[index]?.takeIf { it.id != 0 }?.let { GrandExchangeOffer(it) }
    }
}