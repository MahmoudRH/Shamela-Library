package com.folioreader.model.dictionary

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import java.io.IOException
import java.util.Arrays

/**
 * @author gautam chibde on 4/7/17.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
class Senses {
    @JsonProperty
    @JsonDeserialize(using = DefinitionDeserializer::class)
    private lateinit var definition: Array<String>

    @JsonProperty
    var examples: List<Example>? = null
    override fun toString(): String {
        return "Senses{" +
                "definition=" + Arrays.toString(definition) +
                ", examples=" + examples +
                '}'
    }

    fun getDefinition(): Array<String> {
        return definition
    }

    fun setDefinition(definition: Array<String>) {
        this.definition = definition
    }

    class DefinitionDeserializer : StdDeserializer<Array<String?>?> {
        constructor() : super(Array<String>::class.java) {}
        protected constructor(vc: Class<*>?) : super(vc) {}

        @Throws(IOException::class, JsonProcessingException::class)
        override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Array<String?> {
            val node = p.codec.readTree<JsonNode>(p)
            val strings: MutableList<String> = ArrayList()
            val oc = p.codec
            if (node.isArray) {
                for (n in node) {
                    strings.add(oc.treeToValue(n, String::class.java))
                }
            } else {
                strings.add(oc.treeToValue(node, String::class.java))
            }
            return strings.toTypedArray<String?>()
        }
    }
}