package com.darktornado.felicahistoryreader

import com.darktornado.library.FeliCa

class History(fc: FeliCa) {
    val date: String
    val index: String
    val device: String?
    val action: String
    val balance: Int
    val type: Int

    override fun toString(): String {
        return """
            index: $index
            data: $date
            type: $type
            device: $device
            action: $action
            balance: ${balance}円
            """.trimIndent()
    }

    init {
        date = fc.year.toString() + ". " + fc.month + ". " + fc.date + "."
        index = fc.index.toString()
        type = fc.type
        device = fc.device
        action = fc.action
        balance = fc.balance
    }
}