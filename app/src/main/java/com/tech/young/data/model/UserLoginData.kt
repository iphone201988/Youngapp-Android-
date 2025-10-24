package com.tech.young.data.model

import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class UserLoginData(
    val __v: Int? = 0,
    val _id: String?,
    val about: String?,
    val createdAt: String?,
    val deactivatedByAdmin: Boolean?,
    val deviceToken: String?,
    val deviceType: Int? = 0,
    val email: String?,
    val gender: Int? = 0,
    val isActive: Boolean?,
    val isDeleted: Boolean?,
    val location: String?,
    val phoneNumber: String?,
    val profileImage: String?,
    val role: Int,
    val socialId: String? = "",
    val subscription: Any?,
    val token: String?,
    val updatedAt: String?,
    val userName: String?,
)

@Parcelize
data class LoginApiData(
    val data: Data?, val message: String?, val success: Boolean?,
) : Parcelable

@Parcelize
data class Data(
    val _id: String?, val is2FAEnabled: Boolean?, val qrCodeUrl: String?, val secret: String?,
) : Parcelable


data class Signup(
    val data: SignupData?, val message: String?, val success: Boolean?,
)

data class SignupData(
    val _id: String?, val role: String?,
)


/*******************Complete Registration ********************/
data class RegistrationCompleted(
    val data: RegistrationCompletedData?, val message: String?, val success: Boolean?,
)

data class RegistrationCompletedData(
    val _id: String?, val qrCodeUrl: String?, val secret: String?,
)



//@Parcelize
//data class Login(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?,
//) : Parcelable {
//    @Parcelize
//    data class Data(
//        var _id: String?,
//        var is2FAEnabled: Boolean?,
//        var qrCodeUrl: String?,
//        var secret: String?,
//    ) : Parcelable
//}


@Parcelize
data class Login(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) :Parcelable
{
    @Parcelize
    data class Data(
        var _id: String?,
        var additionalPhotos: List<String?>?,
        var company: String?,
        var qrCodeUrl: String?,
        var secret: String?,
        var countryCode: String?,
        var createdAt: String?,
        var customers: Int?,
        var email: String?,
        var firstName: String?,
        var followers: Int?,
        var following: Int?,
        var formUpload: List<String?>?,
        var is2FAEnabled: Boolean?,
        var isDeactivatedByUser: Boolean?,
        var isRegistrationCompleted: Boolean?,
        var isVerified: Boolean?,
        var lastName: String?,
        var location: Location?,
        var phone: String?,
        var role: String?,
        var topicsOfInterest: List<String?>?,
        var username: String?
    ): Parcelable {
        @Parcelize
        data class Location(
            var coordinates: List<Double?>?,
            var type: String?
        ): Parcelable
    }
}

@Parcelize
data class StreamData(
    val title: String?,
    val topic: String?,
    val scheduleDate: String?,
    val image: Uri?,
    val description: String?,

    ) : Parcelable


@Parcelize
data class ShareData(
    val title: String?,
    val topic: String?,
    val description: String?,
    val symbol : String?,
    val symbolValue : String?,
    val image : Uri?

    ) : Parcelable




/******************* verification 2 factor api response ********************/
data class Verification2faApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var name: String?,
        var role: String?,
        var token: String?,
    )
}

/******************* trending topic api response ********************/
//data class TrendingTopicApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var topics: List<Topic?>?,
//    ) {
//        data class Topic(
//            var count: Int?,
//            var topic: String?,
//        )
//    }
//}

data class TrendingTopicApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var topics: List<Topic?>?
    ) {
        data class Topic(
            var _id: String?,
            var count: Int?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var title: String?,
            var topic: String?,
            var userId: String?
        )
    }
}

/******************* get profile api response ********************/
@Parcelize
data class GetProfileApiResponse(
    var `data`: GetProfileApiResponseData?,
    var message: String?,
    var success: Boolean?,
) : Parcelable {
    @Parcelize
    data class GetProfileApiResponseData(
        var user: User?,
    ) : Parcelable {
        @Parcelize
        data class User(
            val _id: String?,
            val additionalPhotos: List<String?>?,
            val children: String?,
            val countryCode: String?,
            val createdAt: String?,
            val customers: Int?,
            val email: String?,
            val firstName: String?,
            val averageRating: Double?,
            val followers: Int?,
            val following: Int?,
            val formUpload: List<String?>?,
            val is2FAEnabled: Boolean?,
            val isConnectedWithProfile: Boolean?,
            val isDeactivatedByUser: Boolean?,
            val isFollowed: Boolean?,
            val isRegistrationCompleted: Boolean?,
            val isReported: Boolean?,
            val isVerified: Boolean?,
            val lastLogin: String?,
            val lastName: String?,
            val location: Location?,
            val phone: String?,
            val ratings: List<Rating?>?,
            val residenceStatus: String?,
            val role: String?,
            val sharesCount: Int?,
            val topicsOfInterest: List<String?>?,
            val username: String?,
            var about: String?,
            var age: String?,
            var areaOfExpertise: String?,
            var businessRevenue: String?,
            var city: String?,
            var company: String?,
            var crdNumber: String?,
            var fairnessForward: Boolean?,
            var followedBy: List<String?>?,
            var gender: String?,
            var investors: Boolean?,
            var packageName: String?,
            var productsOffered: String?,
            var servicesInterested: String?,
            var profileImage: String?,
            var race: String?,
            var state: String?,
            var website: String?,
            var yearFounded: String?,
            val financialExperience: String?,
            val goals: String?,
            val investmentAccounts: Boolean?,
            val investmentRealEstate: Boolean?,
            val retirement: Boolean?,
            val riskTolerance: String?,
            val salaryRange: String?,
            val yearsEmployed: String?,
            val cryptoInvestments: String?,
            val otherSecurityInvestments: String?,
            val realEstate: String?,
            val retirementAccount: String?,
            var isDocumentVerified: String?,
            val savings: String?,
            val specificCryptoSymbols: String?,
            val specificStockSymbols: String?,
            val startups: String?,
            val stockInvestments: String?,
            val certificates:String?,
            val occupation:String?,
            val servicesProvided:String?,
            val yearsInFinancialIndustry:String?,
            val fundsRaising:String?,
            val fundsRaised:String?,
            val stageOfBusiness:String?,
            var isRated: Double?,
            var maritalStatus : String?
        ) : Parcelable {
            @Parcelize
            data class Location(
                val coordinates: List<Double?>?,
                val type: String?,
            ) : Parcelable

            @Parcelize
            data class Rating(
                var _id: String?,
                var ratings: Int?,
            ) : Parcelable
        }
    }
}

//data class dsaas(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?
//) {
//    data class Data(
//        var user: User?
//    ) {
//        data class User(
//            var _id: String?,
//            var additionalPhotos: List<Any?>?,
//            var age: String?,
//            var areaOfExpertise: String?,
//            var city: String?,
//            var company: String?,
//            var countryCode: String?,
//            var createdAt: String?,
//            var customers: Int?,
//            var email: String?,
//            var firstName: String?,
//            var followers: Int?,
//            var following: Int?,
//            var formUpload: List<Any?>?,
//            var gender: String?,
//            var is2FAEnabled: Boolean?,
//            var isConnectedWithProfile: Boolean?,
//            var isDeactivatedByUser: Boolean?,
//            var isDocumentVerified: String?,
//            var isFollowed: Boolean?,
//            var isRegistrationCompleted: Boolean?,
//            var isReported: Boolean?,
//            var isVerified: Boolean?,
//            var lastLogin: String?,
//            var lastName: String?,
//            var location: Location?,
//            var packageName: String?,
//            var phone: String?,
//            var profileImage: String?,
//            var race: String?,
//            var ratings: List<Any?>?,
//            var role: String?,
//            var sharesCount: Int?,
//            var state: String?,
//            var topicsOfInterest: List<Any?>?,
//            var username: String?,
//            var yearFounded: String?
//        ) {
//            data class Location(
//                var coordinates: List<Double?>?,
//                var type: String?
//            )
//        }
//    }
//}

/******************* get events  api response ********************/
data class GetEventsApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var events: List<Event?>?,
        var pagination: Pagination?,
    ) {
        data class Event(
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var `file`: String?,
            var scheduledDate: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var userId: String?,
            var public : Boolean?
        )

        data class Pagination(
            var limit: Int?,
            var page: Int?,
            var total: Int?,
        )
    }
}


/******************* get saved post  api response ********************/



//data class GetSavedPostApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var pagination: Pagination?,
//    var success: Boolean?
//) {
//    data class Data(
//        var posts: List<Post?>?
//    ) {
//        data class Post(
//            var _id: String?,
//            var commentsCount: Int?,
//            var createdAt: String?,
//            var description: String?,
//            var image: String?,
//            var isLiked: Boolean?,
//            var isReported: Boolean?,
//            var likesCount: Int?,
//            var scheduleDate: String?,
//            var status: String?,
//            var ratings: Double?,
//            var streamUrl: String?,
//            var symbol: String?,
//            var title: String?,
//            var topic: String?,
//            var type: String?,
//            var userId: UserId?,
//            var symbolValue: String?
//        ) {
//            data class UserId(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?,
//                var profileImage: String?
//            )
//        }
//    }
//
//    data class Pagination(
//        var limit: Int?,
//        var page: Int?,
//        var total: Int?
//    )


    data class GetSavedPostApiResponse(
        var `data`: Data?,
        var message: String?,
        var pagination: Pagination?,
        var success: Boolean?
    ) {
        data class Data(
            var posts: List<Post?>?
        ) {
            data class Post(
                var _id: String?,
                var commentsCount: Int?,
                var createdAt: String?,
                var description: String?,
                var image: String?,
                var isAlreadyAddedToCalendar: Boolean?,
                var isLiked: Boolean?,
                var isReported: Boolean?,
                var likesCount: Int?,
                var reShareCount: Int?,
                var ratings: Double?,
                var scheduleDate: String?,
                var reSharedBy: ReSharedBy?,
                var reSharedPostId: String?,
                var symbol: String?,
                var symbolValue: String?,
                var title: String?,
                var topic: String?,
                var totalSavedCount: Int?,
                var type: String?,
                var userId: UserId?,
                var status: String?,
                var streamUrl: String?
            ) {
                data class ReSharedBy(
                    var _id: String?,
                    var firstName: String?,
                    var lastName: String?,
                    var profileImage: String?,
                    var role: String?,
                    var username: String?
                )

                data class UserId(
                    var _id: String?,
                    var firstName: String?,
                    var lastName: String?,
                    var profileImage: String?,
                    var username: String?
                )
            }
        }

        data class Pagination(
            var limit: Int?,
            var page: Int?,
            var total: Int?
        )
    }


    /******************* get chat api response ********************/
//data class GetChatApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var chats: List<Chat?>?,
//    ) {
//        data class Chat(
//            var _id: String?,
//            var chatUsers: List<ChatUser?>?,
//            var createdAt: String?,
//            var hasUnreadMessages: Boolean?,
//            var lastMessage: LastMessage?,
//        ) {
//            data class ChatUser(
//                var _id: String?,
//                var profileImage: String?,
//                var role: String?,
//                var username: String?,
//            )
//
//            data class LastMessage(
//                var _id: String?,
//                var message: String?,
//            )
//        }
//    }
//}


data class GetChatApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var chats: List<Chat?>?
    ) {
        data class Chat(
            var _id: String?,
            var chatUsers: List<ChatUser?>?,
            var createdAt: String?,
            var hasUnreadMessages: Boolean?,
            var isVaultChat: Boolean?,
            var lastMessage: LastMessage?,
            var vaultId: String?
        ) {
            data class ChatUser(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var role: String?,
                var username: String?
            )

            data class LastMessage(
                var _id: String?,
                var message: String?
            )
        }
    }
}

/******************* get user  api response ********************/
//data class GetUserApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var pagination: Pagination?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var users: List<User?>?,
//    ) {
//        data class User(
//            var _id: String?,
//            var firstName: String?,
//            var lastName: String?,
//            var profileImage: String?,
//            var role: String?,
//            var username: String?,
//            var isRated: Double?,
//            var isSelected: Boolean = false,
//        )
//    }
//
//    data class Pagination(
//        var limit: Int?,
//        var page: Int?,
//        var totalPages: Int?,
//    )
//}

data class GetUserApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?
) {
    data class Data(
        var users: List<User?>?
    ) {
        data class User(
            var _id: String?,
            var firstName: String?,
            var isRated: Any?,
            var lastName: String?,
            var profileImage: String?,
            var role: String?,
            var servicesInterested: String?,
            var topicsOfInterest: List<String?>?,
            var username: String?,
            var isSelected: Boolean = false,
        )
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var totalPages: Int?
    )
}
/******************* get chat message   api response ********************/
data class GetChatMessageApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var messages: List<Messages?>?,
    ) {
        data class Messages(
            var _id: String?,
            var chatId: String?,
            var createdAt: String?,
            var isRead: Boolean?,
            var message: String?,
            var senderId: SenderId?,
        ) {
            data class SenderId(
                var _id: String?,
                var profileImage: String?,
                var role: String?,
                var username: String?,
            )
        }
    }
}

/******************* get chat message   api response ********************/
data class GetVaultApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?,
) {
    data class Data(
        var vaults: List<Vault?>?,
    ) {
        data class Vault(
            var _id: String?,
            var admin: Admin?,
            var commentsCount: Int?,
            var likesCount: Int?,
            var createdAt: String?,
            var description: String?,
            var ratings: Double?,
            var image: String?,
            var isReported: Boolean?,
            var isSaved: Boolean?,
            var members: List<Member?>?,
            var title: String?,
            var topic: String?,
        ) {
            data class Admin(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username: String?,
            )

            data class Member(
                var _id: String?,
                var profileImage: String?,
            )
        }
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var total: Int?,
    )
}

/******************* get vault detail api  response ********************/
//data class VaultDetailApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?
//) {
//    data class Data(
//        var vault: Vault?
//    ) {
//        data class Vault(
//            var _id: String?,
//            var access: String?,
//            var admin: Admin?,
//            var category: List<String?>?,
//            var createdAt: String?,
//            var description: String?,
//            var image: String?,
//            var isMember: Boolean?,
//            var members: List<Member?>?,
//            var title: String?,
//            var topic: String?
//        ) {
//            data class Admin(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?,
//                var profileImage: String?,
//                var username: String?
//            )
//
//            data class Member(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?,
//                var profileImage: String?,
//                var username: String?
//            )
//        }
//    }
//}

data class VaultDetailApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var vault: Vault?,
    ) {
        data class Vault(
            var _id: String?,
            var access: String?,
            var admin: Admin?,
            var category: List<String?>?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isMember: Boolean?,
            var members: List<Member?>?,
            var chatId : String?,
            var title: String?,
            var topic: String?,
        ) {
            data class Admin(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username: String?,
            )

            data class Member(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username: String?,
            )
        }
    }
}

/******************* get vault detail api  response ********************/
data class JoinVaultRoomApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var vaultId: String?,
    )
}

/******************* get all post api response  response ********************/
//data class ExchangeShareApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var pagination: Pagination?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var posts: List<Post?>?,
//    ) {
//        data class Post(
//            var isReportVisible: Boolean = false,
//            var _id: String?,
//            var commentsCount: Int?,
//            var createdAt: String?,
//            var description: String?,
//            var image: String?,
//            var isLiked: Boolean?,
//            var isReported: Boolean?,
//            var isSaved: Boolean?,
//            var likesCount: Int?,
//            var ratings: Double?,
//            var reSharedBy: ReSharedBy?,
//            var reSharedPostId: String?,
//            var reShareCount: Int?,
//            var totalSavedCount : Int?,
//            var scheduleDate: String?,
//            var symbol: String?,
//            var title: String?,
//            var topic: String?,
//            var type: String?,
//            var userId: UserId?,
//            var symbolValue : String?
//        ) {
//            data class UserId(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?,
//                var profileImage: String?,
//            )
//        }
//    }
//
//    data class Pagination(
//        var limit: Int?,
//        var page: Int?,
//        var total: Int?,
//    )
//}





data class ExchangeShareApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?
) {
    data class Data(
        var posts: List<Post?>?
    ) {
        data class Post(
            var _id: String?,
            var isReportVisible: Boolean = false,
            var commentsCount: Int?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isAlreadyAddedToCalendar: Boolean?,
            var isLiked: Boolean?,
            var isReported: Boolean?,
            var isSaved: Boolean?,
            var likesCount: Int?,
            var ratings: Double?,
            var reShareCount: Int?,
            var reSharedBy: ReSharedBy?,
            var reSharedPostId: String?,
            var symbol: String?,
            var symbolValue: String?,
            var title: String?,
            var topic: String?,
            var totalSavedCount: Int?,
            var type: String?,
            var userId: UserId?
        ) {
            data class ReSharedBy(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username: String?,
                var role: String?
            )

            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var location: Location?,
                var username: String?,
                var profileImage: String?
            ) {
                data class Location(
                    var coordinates: List<Double?>?,
                    var type: String?
                )
            }
        }
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var total: Int?
    )
}

/******************* get vaults api response  response ********************/
data class VaultExchangeApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?,
) {
    data class Data(
        var vaults: List<Vault?>?,
    ) {
        data class Vault(
            var isReportVisible: Boolean = false,
            var _id: String?,
            var admin: Admin?,
            var commentsCount: Int?,
            var createdAt: String?,
            var description: String?,
            var totalSavedCount: Int?,
            var image: String?,
            var isReported: Boolean?,
            var members: List<Member?>?,
            var title: String?,
            var topic: String?,
            var ratings: Double?,
            var isSaved: Boolean?,
        ) {
            data class Admin(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username: String?,
            )

            data class Member(
                var _id: String?,
                var profileImage: String?,
            )
        }
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var total: Int?,
    )

}

/*******************saved post api response  response ********************/
data class SavedPostApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var postId: String?,
    )
}

/******************* send otp api response  response ********************/
data class SendOtpApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var _id: String?,
    )
}

/******************* verify otp api response  response ********************/
data class VerifyOtpApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var _id: String?,
        var role: String?,
    )
}

/*************** update user ***********************/
data class UpdateUserProfileResponse(
    val data: UpdateUserProfileData?,
    val message: String?,
    val success: Boolean?,
)

data class UpdateUserProfileData(
    val name: String?,
)

/*************** get other user profile *********************/
data class GetOtherUserProfileModel(
    val data: GetOtherUserProfileModelData?,
    val message: String?,
    val success: Boolean?,
)

data class GetOtherUserProfileModelData(
    val user: GetOtherUserProfileData?,
)

data class GetOtherUserProfileData(
    val _id: String?,
    val additionalPhotos: List<String?>?,
    var goals: String?,
    var website: String?,
    val age: String?,
    val chatId: String?,
    var city: String?,
    var productsOffered: String?,
    var state: String?,
    var about: String?,
    val company: String?,
    val countryCode: String?,
    val createdAt: String?,
    val customers: Int?,
    val email: String?,
    val fairnessForward: Boolean?,
    val financialExperience: String?,
    val firstName: String?,
    val followedBy: List<String?>?,
    val followers: Int?,
    val following: Int?,
    val formUpload: List<Any?>?,
    val gender: String?,
    val homeOwnerShip: String?,
    val investmentAccounts: Boolean?,
    val investmentRealEstate: Boolean?,
    val investments: String?,
    val investors: Boolean?,
    val is2FAEnabled: Boolean?,
    val isConnectedWithProfile: Boolean?,
    val isFollowed: Boolean?,
    val isRated: Double?,
    var averageRating: Double?,
    val isRegistrationCompleted: Boolean?,
    val isReported: Boolean?,
    val isVerified: Boolean?,
    val lastLogin: String?,
    val lastName: String?,
    val licenseImage: String?,
    val maritalStatus: String?,
    val objective: String?,
    val packageName: String?,
    val phone: String?,
    val profileImage: String?,
    val ratings: List<Rating?>?,
    val retirement: Boolean?,
    val role: String?,
    val servicesInterested: String?,
    val stripeCustomerId: String?,
    val subscriptionId: String?,
    val topicsOfInterest: List<Any?>?,
    val username: String?,
    val sharesCount: Int,
)
//{
//    data class IsConnectedWithProfile(
//        var __v: Int?,
//        var _id: String?,
//        var createdAt: String?,
//        var customer: String?,
//        var updatedAt: String?,
//        var userId: String?
//    )
//}

data class Rating(
    val _id: String?,
    val ratings: Double?,
)


//data class GetOtherUserProfileModel(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?
//) {
//    data class Data(
//        var user: User?
//    ) {
//        data class User(
//            var _id: String?,
//            var additionalPhotos: List<String?>?,
//            var age: String?,
//            var chatId: String?,
//            var company: String?,
//            var countryCode: String?,
//            var createdAt: String?,
//            var customers: Int?,
//            var email: String?,
//            var fairnessForward: Boolean?,
//            var financialExperience: String?,
//            var firstName: String?,
//            var followedBy: List<String?>?,
//            var followers: Int?,
//            var following: Int?,
//            var formUpload: List<Any?>?,
//            var gender: String?,
//            var homeOwnerShip: String?,
//            var investmentAccounts: Boolean?,
//            var investmentRealEstate: Boolean?,
//            var investments: String?,
//            var investors: Boolean?,
//            var is2FAEnabled: Boolean?,
//            var isConnectedWithProfile: Boolean?,
//            var isFollowed: Boolean?,
//            var isRated: Double?,
//            var isRegistrationCompleted: Boolean?,
//            var isReported: Boolean?,
//            var isVerified: Boolean?,
//            var lastLogin: String?,
//            var lastName: String?,
//            var licenseImage: String?,
//            var maritalStatus: String?,
//            var objective: String?,
//            var packageName: String?,
//            var phone: String?,
//            var profileImage: String?,
//            var ratings: List<Rating?>?,
//            var retirement: Boolean?,
//            var role: String?,
//            var servicesInterested: String?,
//            var stripeCustomerId: String?,
//            var subscriptionId: String?,
//            var topicsOfInterest: List<Any?>?,
//            var username: String?
//        ) {
//            data class Rating(
//                var _id: String?,
//                var ratings: Double?
//            )
//        }
//    }
//}


/*************** get comment api response *********************/
data class GetCommentApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var comments: List<Comment?>?,
        var pagination: Pagination?,
    ) {
        data class Comment(
            var _id: String?,
            var comment: String?,
            var createdAt: String?,
            var isLiked: Boolean?,
            var likesCount: Int?,
            var type: String?,
            var userId: UserId?,
            var vaultId: String?,
        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
                var username :  String?,
                var role: String?,
            )
        }

        data class Pagination(
            var limit: Int?,
            var page: Int?,
            var total: Int?,
        )
    }
}


data class ReceivedSocketComment(
    var _id: String?,
    var comment: String?,
    var createdAt: String?,
    var type: String?,
    var userId: UserId?,
    var vaultId: String?
) {
    data class UserId(
        var _id: String?,
        var firstName: String?,
        var lastName: String?,
        var profileImage: String?,
        var role: String?,
        var username: String?
    )
}
/*************** get latest user api response *********************/
//data class GetLatestUserApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var users: List<User?>?,
//    ) {
//        data class User(
//            var _id: String?,
//            var firstName: String?,
//            var lastName: String?,
//            var profileImage: String?,
//            var role: String?,
//            var username: String?,
//            var ratings: Double?,
//            var isRated: Double?
//
//            )
//    }
//}
//

data class GetLatestUserApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?
) {
    data class Data(
        var users: List<User?>?
    ) {
        data class User(
            var _id: String?,
            var firstName: String?,
            var isRated: Double?,
            var lastName: String?,
            var profileImage: String?,
            var role: String?,
            var servicesInterested: String?,
            var topicsOfInterest: List<String?>?,
            var username: String?
        )
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var totalPages: Int?
    )
}
/*************** get post by id  api response *********************/


//data class GetPostDetailsApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var success: Boolean?,
//) {
//    data class Data(
//        var post: Post?,
//    ) {
//        data class Post(
//            var _id: String?,
//            var createdAt: String?,
//            var description: String?,
//            var image: String?,
//            var isDeleted: Boolean?,
//            var isPublished: Boolean?,
//            var reSharedBy: String?,
//            var reSharedPostId: String?,
//            var symbol: String?,
//            var title: String?,
//            var topic: String?,
//            var type: String?,
//            var userId: UserId?,
//            var rating : Double?,
//        ) {
//            data class UserId(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?,
//                var profileImage: String?,
//            )
//        }
//    }
//}



data class GetPostDetailsApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var post: Post?
    ) {
        data class Post(
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var isDeleted: Boolean?,
            var isPublished: Boolean?,
            var reSharedBy: ReSharedBy?,
            var reSharedPostId: String?,
            var symbol: String?,
            var symbolValue: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var userId: UserId?,
            var rating : Double?,
            var image: String?
        ) {
            data class ReSharedBy(
                var _id: String?,
                var firstName: String?,
                var lastName: String?
            )

            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
            )
        }
    }
}

/*************** received socket  api response *********************/


data class ReceivedMessageApiResponse(
    var _id: String?,
    var chatId: String?,
    var createdAt: String?,
    var isRead: Boolean?,
    var message: String?,
    var senderId: SenderId?,
) {
    data class SenderId(
        var _id: String?,
        var profileImage: String?,
        var role: String?,
        var username: String?,
    )
}

/*************** received socket  api response *********************/

data class AddCommentApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var comment: Comment?,
    ) {
        data class Comment(
            var _id: String?,
            var comment: String?,
            var createdAt: String?,
            var isLiked: Boolean?,
            var postId: String?,
            var type: String?,
            var userId: UserId?,
        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
            )
        }
    }
}

/*************** get post comeent  api response *********************/

data class GetCommentApiResponsePost(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var comments: List<Comment?>?,
        var pagination: Pagination?,
    ) {
        data class Comment(
            var _id: String?,
            var comment: String?,
            var createdAt: String?,
            var isLiked: Boolean?,
            var likesCount: Int?,
            var postId: String?,
            var type: String?,
            var userId: UserId?,

        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var username: String?,
                var profileImage: String?,
                var role: String?,
            )
        }

        data class Pagination(
            var limit: Int?,
            var page: Int?,
            var total: Int?,
        )
    }
}


/*************** add vault comment  api response *********************/

data class AddCommentApiResponseVault(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var comment: Comment?,
    ) {
        data class Comment(
            var _id: String?,
            var comment: String?,
            var createdAt: String?,
            var isLiked: Boolean?,
            var type: String?,
            var userId: UserId?,
            var vaultId: String?,
        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
            )
        }
    }
}


/*************** comment like dislike  api response *********************/

data class CommentLikeDislikeApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var commentId: String?,
    )
}

/*************** stream   api response *********************/


data class StreamApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var post: Post?,
    ) {
        data class Post(
            var __v: Int?,
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isDeleted: Boolean?,
            var isPublished: Boolean?,
            var likedBy: List<Any?>?,
            var scheduleDate: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var updatedAt: String?,
        )
    }
}

/***************  get stream api response *********************/

//data class GetStreamApiResponse(
//    var `data`: Data?,
//    var message: String?,
//    var pagination: Pagination?,
//    var success: Boolean?
//) {
//    data class Data(
//        var posts: List<Post?>?
//    ) {
//        data class Post(
//            var _id: String?,
//            var commentsCount: Int?,
//            var createdAt: String?,
//            var description: String?,
//            var image: String?,
//            var isLiked: Boolean?,
//            var isReported: Boolean?,
//            var isSaved: Boolean?,
//            var likesCount: Int?,
//            var scheduleDate: String?,
//            var title: String?,
//            var topic: String?,
//            var type: String?,
//            var userId: UserId?
//        ) {
//            data class UserId(
//                var _id: String?,
//                var firstName: String?,
//                var lastName: String?
//            )
//        }
//    }
//
//    data class Pagination(
//        var limit: Int?,
//        var page: Int?,
//        var total: Int?
//    )
//}


data class GetStreamApiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?,
) {
    data class Data(
        var posts: List<Post?>?,
    ) {
        data class Post(
            var isReportVisible: Boolean = false,
            var _id: String?,
            var commentsCount: Int?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isLiked: Boolean?,
            var isReported: Boolean?,
            var isSaved: Boolean?,
            var likesCount: Int?,
            var scheduleDate: String?,
            var totalSavedCount: Int?,
            var streamUrl: String?,
            var symbol: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var ratings: Double?,
            var userId: UserId?,

            var isAlreadyAddedToCalendar : Boolean?
        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var username: String?,
                var location: Location?,
                var profileImage: String?,
            ) {
                data class Location(
                    var coordinates: List<Double?>?,
                    var type: String?,
                )
            }
        }
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var total: Int?,
    )
}


/***************  stream detail api response *********************/


data class StreamDetailApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var post: Post?,
    ) {
        data class Post(
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isDeleted: Boolean?,
            var isPublished: Boolean?,
            var scheduleDate: String?,
            var streamUrl: String?,
            var title: String?,
            var topic: String?,
            var type: String?,

            var isAlreadyAddedToCalendar : Boolean?,
            var userId: UserId?,
        ) {
            data class UserId(
                var _id: String?,
                var firstName: String?,
                var lastName: String?,
                var profileImage: String?,
            )
        }
    }
}


/***************  create post  detail api response *********************/
data class CreatePostApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var post: Post?,
    ) {
        data class Post(
            var __v: Int?,
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isDeleted: Boolean?,
            var isPublished: Boolean?,
            var symbol: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var updatedAt: String?,
        )
    }

}


/***************  create post  detail api response *********************/
data class DigitApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?,
) {
    data class Data(
        var _id: String?,
        var isDocumentVerified: String?,
    )
}

/***************  get ads api response *********************/

data class GetAdsAPiResponse(
    var `data`: Data?,
    var message: String?,
    var pagination: Pagination?,
    var success: Boolean?
) {
    data class Data(
        var ads: List<Ad?>?
    ) {
        data class Ad(
            var __v: Int?,
            var _id: String?,
            var company: String?,
            var createdAt: String?,
            var email: String?,
            var `file`: String?,
            var name: String?,
            var status: String?,
            var updatedAt: String?,
            var userId: String?,
            var website: String?
        )
    }

    data class Pagination(
        var limit: Int?,
        var page: Int?,
        var total: Int?
    )
}

/***************  add rating  api response *********************/
data class AddRatingApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var ratings: Int?
    )
}

/***************  download history   api response *********************/
data class DownloadHistoryApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var posts: List<Post?>?
    ) {
        data class Post(
            var __v: Int?,
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var image: String?,
            var isDeleted: Boolean?,
            var isPublished: Boolean?,
            var reSharedBy: String?,
            var reSharedPostId: String?,
            var scheduleDate: String?,
            var symbol: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var updatedAt: String?
        )
    }
}


/***************  get rating   api response *********************/

data class GetRatingApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var averageRating: Double?,
        var ratingsCount: RatingsCount?,
        var totalCount: Int?
    ) {
        data class RatingsCount(
            var `1`: Int?,
            var `2`: Int?,
            var `3`: Int?,
            var `4`: Int?,
            var `5`: Int?
        )
    }
}

/***************  get media  api response *********************/


data class MediaApiResponse(
    var `data`: List<Data?>?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var __v: Int?,
        var _id: String?,
        var createdAt: String?,
        var imageUrl: String?,
        var title: String?,
        var updatedAt: String?
    )
}


/***************  even update api response *********************/

data class EventUpdateApiResponse(
    var `data`: Data?,
    var message: String?,
    var success: Boolean?
) {
    data class Data(
        var event: Event?
    ) {
        data class Event(
            var __v: Int?,
            var _id: String?,
            var createdAt: String?,
            var description: String?,
            var `file`: String?,
            var isDeleted: Boolean?,
            var `public`: Boolean?,
            var scheduledDate: String?,
            var title: String?,
            var topic: String?,
            var type: String?,
            var updatedAt: String?,
            var userId: String?
        )
    }
}

/***************  push notification model  api response *********************/


//@Parcelize
//data class FcmPayload(
//    val type: String? = null,
//    val postId: String? = null,
//    val userId: String? = null,
//    val username: String? = null,
//    val profileImage: String? = null,
//    val role: String? = null,
//    val chatId: String? = null
//) : Parcelable


@Parcelize
data class FcmPayload(
    val type: String? = null,
    val postId: String? = null,
    val userId: String? = null,
    val username: String? = null,
    val profileImage: String? = null,
    val role: String? = null,
    val chatId: String? = null,

    val firstName: String?,
    val lastName: String?,
    // 🆕 Common FCM pending data fields
    val title: String? = null,
    val body: String? = null,
    val message: String? = null,
    val imageUrl: String? = null,
    val timestamp: String? = null

) : Parcelable
