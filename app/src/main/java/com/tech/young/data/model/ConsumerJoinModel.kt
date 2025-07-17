package com.tech.young.data.model

import com.google.gson.annotations.SerializedName

data class ConsumerJoinModel(
    var roomProducers: RoomProducers?,
    var rtpCapabilities: RtpCapabilities?
) {
    data class RoomProducers(
        var audioProducer: String?,
        var videoProducer: String?
    )

    data class RtpCapabilities(
        var codecs: List<Codec?>?,
        var headerExtensions: List<HeaderExtension?>?
    ) {
        data class Codec(
            var channels: Int?,
            var clockRate: Int?,
            var kind: String?,
            var mimeType: String?,
            var parameters: Parameters?,
            var preferredPayloadType: Int?,
            var rtcpFeedback: List<RtcpFeedback?>?
        ) {
            data class Parameters(
                var apt: Int?,
                @SerializedName("x-google-start-bitrate")
                var xGoogleStartBitrate: Int?
            )

            data class RtcpFeedback(
                var parameter: String?,
                var type: String?
            )
        }

        data class HeaderExtension(
            var direction: String?,
            var kind: String?,
            var preferredEncrypt: Boolean?,
            var preferredId: Int?,
            var uri: String?
        )
    }
}