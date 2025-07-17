package com.tech.buckChats.ui.media_soup_webtrc

data class consumerTransport(
    val params: Params5
)

data class Params5(
    val id: String,
    val kind: String,
    val producerId: String,
    val rtpParameters: RtpParameters7,
    val serverConsumerId: String
)

data class RtpParameters7(
    val codecs: List<Codec35>,
    val encodings: List<Encoding>,
    val headerExtensions: List<HeaderExtension99>,
    val mid: String,
    val rtcp: Rtcp85
)

data class Codec35(
    val clockRate: Int,
    val mimeType: String,
    val parameters: Parameters66,
    val payloadType: Int,
    val rtcpFeedback: List<RtcpFeedback555>
)

data class Encoding(
    val rtx: Rtx,
    val scalabilityMode: String,
    val ssrc: Int
)

data class HeaderExtension99(
    val encrypt: Boolean,
    val id: Int,
    val parameters: ParametersX,
    val uri: String
)

data class Rtcp85(
    val cname: String,
    val reducedSize: Boolean
)

data class Parameters66(
    val apt: Int
)

data class RtcpFeedback555(
    val parameter: String,
    val type: String
)

data class Rtx(
    val ssrc: Int
)

class ParametersX