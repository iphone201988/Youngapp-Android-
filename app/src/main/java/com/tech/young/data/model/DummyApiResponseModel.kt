package com.tech.young.data.model

class DummyApiResponseModel : ArrayList<DummyApiItem>()

data class DummyApiItem(
    val id: Int, val imdbId: String, val posterURL: String, val title: String
)

data class ChooseAccountType(
    var isSelected: Boolean = false, val name: String
)

data class SideMenuBar(
    var isSelected: Boolean = false, val name: String,var heading:String?=null,var headingShow:Boolean=false
)

data class EditProfileListModel(
    var title:String,var subTitle:String,var image:Int,var listType:Int,var isTop:Boolean=false,var lastlogin:String?=null
)

data class CategoryModel(
    var title: String,var isSelected:Boolean=false
)
