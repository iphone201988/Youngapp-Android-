package com.tech.buckChats.ui.media_soup_webtrc

data class consumerTransportAudio(
    val params: Params23
)

data class Params23(
    val id: String,
    val kind: String,
    val producerId: String,
    val rtpParameters: RtpParameters25,
    val serverConsumerId: String
)

data class RtpParameters25(
    val codecs: List<Codec225>,
    val encodings: List<Encoding635>,
    val headerExtensions: List<HeaderExtension5454>,
    val mid: String,
    val rtcp: Rtcp656
)

data class Codec225(
    val channels: Int,
    val clockRate: Int,
    val mimeType: String,
    val parameters: Parameters54,
    val payloadType: Int,
    val rtcpFeedback: List<Any>
)

data class Encoding635(
    val ssrc: Int
)

data class HeaderExtension5454(
    val encrypt: Boolean,
    val id: Int,
    val parameters: ParametersX5,
    val uri: String
)

data class Rtcp656(
    val cname: String,
    val reducedSize: Boolean
)

data class Parameters54(
    val minptime: Int,
    val useinbandfec: Int
)

class ParametersX5