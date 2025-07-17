package com.tech.young.data.model

import com.tech.young.data.DropDownData


object DummyLists {
    val ageRanges = listOf("18-21", "22-25", "26-35", "35-50", "50-65", ">65")
    val genders = listOf("Male", "Female", "Nonbinary", "Other")
    val martialStatus = listOf("Single", "In a relationship", "Married", "Divorced", "Widowed")
    val children = listOf("0", "planning", "1", "2...10", ">10")
    val homeOwnershipStatus = listOf("Rent", "Own", "Other")
    val generalMemberObjective = listOf("Find a professional", "Financial literacy", "Networking")
    val generalMemberFinancialExperience = listOf("Limited", "Moderate", "Extensive")
    val generalMemberInvestments = listOf(
        "Stocks",
        "Retirement",
        "Cryptocurrency",
        "Real Estate",
        "Startups",
        "Savings"
    )

    val generalMemberServicesInterested = listOf(
        "Financial planning",
        "Investment management",
        "Child education",
        "Estate planning",
        "Student loan management",
        "Debt management",
        "Startups",
        "Small businesses",
        "Life Insurance"
    )

    val financialAdvisorProductsServicesOffered = generalMemberServicesInterested
    val financialAdvisorAreasOfExpertise = generalMemberServicesInterested
    val startupAndSmallBusinessIndustry = listOf(
        "Technology", "Healthcare", "E-commerce", "Retail", "Biotech",
        "Fintech", "Education", "Other"
    )

    val startupAndSmallBusinessInterestedIn = listOf(
        "Investors", "Loans", "Donations", "Customers", "Hiring"
    )

    val investorVCIndustryInterestedIn = startupAndSmallBusinessIndustry

    val investorVCAreasOfExpertise = listOf(
        "Investment analysis", "Financial modeling", "Mentorship",
        "Distribution chain", "Industry knowledge", "Industry connections"
    )

    val insuranceProductsServicesOffered = listOf("Insurance", "Annuities", "Other")

    val insuranceAreasOfExpertise = insuranceProductsServicesOffered

    val race = listOf(
        "Black", "White", "Asian", "Hispanic/ Latino",
        "American Indian/ Alaska Native", "Native Hawaiian/ Pacific Islander"
    )

    val educationLevel = listOf("High school", "College", "Graduate school")

    val yearsEmployed = listOf("0-5", "6-10", "10-20", ">20")

    val yearsInFinancialIndustry = listOf("0-3", "4-10", "10+")

    val salaryRange = listOf("Unemployed", "$0-10K", "$11-50K", "$51-100K", "$101-200K", "$201-250K", "over $251K")

    val financialExperience = listOf("Limited", "Moderate", "Extensive")

    val riskTolerance = listOf(
        "Low - Risks scare me",
        "Moderate - I’m on the fence",
        "High - Bring it on"
    )

    val feedTopics = listOf(
        "Stocks", "Insurance", "Retirement", "Savings", "Investment Management",
        "Child Education", "Student Loan Management", "Debt Management", "Tax Planning",
        "Financial Planning", "Wealth Education", "Estate Planning", "Investor",
        "Venture Capitalist", "Small Business", "Grants", "Loans", "Annuities"
    )

    val topicsOfInterest = listOf(
        "Wealth Education", "Household budgeting", "Financial planning", "Wealth Management",
        "Investing", "Child Education Planning", "Retirement Planning", "Estate Planning",
        "Debt Management", "Student loan Management", "Tax Planning", "Annuities",
        "Life Insurance", "Stocks", "Index funds", "ETFs", "Bonds", "Mutual Funds",
        "Crypto", "REITs", "Tech", "Conservative funds", "Moderate funds",
        "Aggressive funds", "Startups (Tech, Health, Lifestyle, Education, Ecommerce, Other)"
    )

    val licensesOrCertification = listOf("Securities", "CFA", "Other")

    val servicesOffered = listOf(
        "Wealth Education", "Household budgeting", "Financial planning", "Wealth Management",
        "Investing", "Child Education Planning", "Retirement Planning", "Estate Planning",
        "Debt Management", "Student loan Management", "Tax Planning"
    )

    val stageOfBusiness = listOf("Pre-Seed", "Seed", "Series A", "Series B")

    val fundsRaised = listOf("$10K-$100K", "$100K-$1M", "$1M-$3M", "$3M-$15M", ">$15M")

    val fundsRaising = listOf("$50K-$250K", "$500K-$2M", "$2M-$5M", "$5M-$20M", ">$20M")

    val businessRevenue = listOf("$0-$50K", "$50K-$500K", "$500K-$5M", "$5M-$25M", ">$25M")

    val startUpSeeking = listOf(
        "Investors (VC, angel, private)", "Loans", "Donations", "Customers", "Hiring"
    )

    val productsOrServicesOffered = listOf(
        "Annuities", "Whole life Insurance", "Universal Life Insurance", "Index Life Insurance",
        "Term Life Insurance", "Variable Life Insurance", "Group Life Insurance", "Other"
    )

    val reportReasons = listOf(
        "Spam", "Harassment", "Hate Speech", "Violence or Threats", "Sexually Explicit Content",
        "Child Exploitation", "Self-Harm or Suicide", "False Information", "Scam or Fraud",
        "Impersonation", "Inappropriate Username or Profile", "Intellectual Property Violation",
        "Privacy Violation", "Terrorism or Extremism", "Illegal Activity",
        "Disruptive Behavior", "Wrong Category"
    )


    // dummy lists
    fun getRaceList():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("Black"))
        list.add(DropDownData("White"))
        list.add(DropDownData("Asian"))
        list.add(DropDownData("Hispanic/ Latino"))
        list.add(DropDownData("American Indian/ Alaska Native"))
        list.add(DropDownData("Native Hawaiian/ Pacific Islander"))
        return list
    }

    fun getGenderList():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("Male"))
        list.add(DropDownData("Female"))
        list.add(DropDownData("Nonbinary"))
        list.add(DropDownData("Other"))
        return list
    }

    fun getAgeList():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("18-21"))
        list.add(DropDownData("22-25"))
        list.add(DropDownData("26-35"))
        list.add(DropDownData("35-50"))
        list.add(DropDownData("50-65"))
        list.add(DropDownData(">65"))
        return list
    }

    fun getMartialStatus():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("Single"))
        list.add(DropDownData("In a relationship"))
        list.add(DropDownData("Married"))
        list.add(DropDownData("Divorced"))
        list.add(DropDownData("Widowed"))
        return list
    }

    fun getEduLevel():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("High school"))
        list.add(DropDownData("College"))
        list.add(DropDownData("Graduate school"))
        return list
    }


    fun getIndustries():ArrayList<DropDownData>{
        val list=ArrayList<DropDownData>()
        list.add(DropDownData("Technology"))
        list.add(DropDownData("Healthcare"))
        list.add(DropDownData("E-commerce"))
        list.add(DropDownData("Retail"))
        list.add(DropDownData("Biotech"))
        list.add(DropDownData("Fintech"))
        list.add(DropDownData("Education"))
        list.add(DropDownData("Other"))
        return list
    }
}
