package com.tech.young.data.model

import com.google.gson.annotations.SerializedName

//data class ConsumerModel(
//    val params: Params3
//)
//
//data class Params3(
//    val dtlsParameters: DtlsParameters2,
//    val iceCandidates: List<IceCandidate3>,
//    val iceParameters: IceParameters2,
//    val id: String
//)
//
//data class DtlsParameters2(
//    val fingerprints: List<Fingerprint3>,
//    val role: String
//)
//
//data class IceCandidate3(
//    val address: String,
//    val foundation: String,
//    val ip: String,
//    val port: Int,
//    val priority: Int,
//    val protocol: String,
//    val tcpType: String,
//    val type: String
//)
//
//data class IceParameters2(
//    val iceLite: Boolean,
//    val password: String,
//    val usernameFragment: String
//)
//
//data class Fingerprint3(
//    val algorithm: String,
//    val value: String
//)

//data class ConsumerModel(
//    var params: Params?
//) {
//    data class Params(
//        var dtlsParameters: DtlsParameters?,
//        var iceCandidates: List<IceCandidate?>?,
//        var iceParameters: IceParameters?,
//        var id: String?
//    ) {
//        data class DtlsParameters(
//            var fingerprints: List<Fingerprint?>?,
//            var role: String?
//        ) {
//            data class Fingerprint(
//                var algorithm: String?,
//                var value: String?
//            )
//        }
//
//        data class IceCandidate(
//            var address: String?,
//            var foundation: String?,
//            var ip: String?,
//            var port: Int?,
//            var priority: Int?,
//            var protocol: String?,
//            var tcpType: String?,
//            var type: String?
//        )
//
//        data class IceParameters(
//            var iceLite: Boolean?,
//            var password: String?,
//            var usernameFragment: String?
//        )
//    }
//}

data class ConsumerModel(
    var params: Params?
) {
    data class Params(
        var dtlsParameters: DtlsParameters?,
        var iceCandidates: List<IceCandidate?>?,
        var iceParameters: IceParameters?,
        var id: String?
    ) {
        data class DtlsParameters(
            var fingerprints: List<Fingerprint?>?,
            var role: String?
        ) {
            data class Fingerprint(
                var algorithm: String?,
                var value: String?
            )
        }

        data class IceCandidate(
            var address: String?,
            var foundation: String?,
            var ip: String?,
            var port: Int?,
            var priority: Int?,
            var protocol: String?,
            var tcpType: String?,
            var type: String?
        )

        data class IceParameters(
            var iceLite: Boolean?,
            var password: String?,
            var usernameFragment: String?
        )
    }
}