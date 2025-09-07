package com.github.alanderua.jic.impl

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.protobuf.ProtoBuf

internal val json = Json { prettyPrint = true }

@OptIn(ExperimentalSerializationApi::class)
internal val protobuf = ProtoBuf {  }