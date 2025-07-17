package com.tech.young.data.model

data class CreateTransportModel(
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