package com.cagatay.gameclients.data

case class ClientEvent(client_id: String, event_id: String, event_timestamp: Long,
                       event_type: String, event_key: String, event_value: String)
