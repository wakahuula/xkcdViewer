package de.stoupas.xkcd.settings

import android.util.Base64
import java.io.ByteArrayInputStream
import java.io.DataInputStream

/** Reads only the ArrayList<String> wire format produced by the historical plugin.
 * No Java objects/classes are instantiated from persisted input.
 */
object LegacyStringListDecoder {
    private const val PREFIX = "VGhpcyBpcyB0aGUgcHJlZml4IGZvciBhIGxpc3Qu"

    fun decode(encoded: String): List<String>? {
        if (encoded.length > 1024 * 1024 || !encoded.startsWith(PREFIX)) return null
        return try {
            val bytes = Base64.decode(encoded.substring(PREFIX.length), Base64.DEFAULT)
            DataInputStream(ByteArrayInputStream(bytes)).use { input ->
                require(input.readInt() == 0xACED0005.toInt())
                require(input.readUnsignedByte() == 0x73) // object
                require(input.readUnsignedByte() == 0x72) // class descriptor
                require(input.readUTF() == "java.util.ArrayList")
                require(input.readLong() == 8683452581122892189L)
                require(input.readUnsignedByte() == 3) // serializable + custom write
                require(input.readUnsignedShort() == 1)
                require(input.readUnsignedByte() == 'I'.code && input.readUTF() == "size")
                require(input.readUnsignedByte() == 0x78 && input.readUnsignedByte() == 0x70)
                val size = input.readInt()
                require(size in 0..100_000 && size <= bytes.size)
                require(input.readUnsignedByte() == 0x77 && input.readUnsignedByte() == 4)
                require(input.readInt() == size)
                val strings = mutableListOf<String>()
                val references = mutableListOf<String>()
                repeat(size) {
                    when (input.readUnsignedByte()) {
                        0x74 -> input.readUTF().also { strings.add(it); references.add(it) }
                        0x71 -> {
                            val index = input.readInt() - 0x7E0002
                            require(index in references.indices)
                            strings.add(references[index])
                        }
                        else -> error("Unsupported legacy list element")
                    }
                }
                require(input.readUnsignedByte() == 0x78 && input.available() == 0)
                strings
            }
        } catch (_: Exception) {
            null
        }
    }
}
