package com.example.data.model

data class CertificateType(
    val id: String,
    val title: String,
    val englishName: String,
    val description: String,
    val category: String,
    val specificFields: List<CertificateField> = emptyList(),
    val bodyTemplate: String,
    val isSuccession: Boolean = false
)

object CertificateRegistry {
    val ALL_TYPES = listOf(
        CertificateType(
            id = "citizenship",
            title = "নাগরিকত্ব সনদ",
            englishName = "Citizenship Certificate",
            description = "জন্মসূত্রে ও স্থায়ীভাবে বাংলাদেশের নাগরিকত্বের প্রত্যয়ন",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "nid_birth_no",
                    label = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নং",
                    hint = "যেমন: ১৯৮৯৭৫১২৩৪৫৬৭৮৯০",
                    helperText = "১০, ১৩ বা ১৭ ডিজিটের এনআইডি অথবা জন্ম নিবন্ধন নং লিখুন",
                    type = FieldType.NUMBER,
                    required = true
                ),
                CertificateField(
                    key = "occupation",
                    label = "পেশা",
                    hint = "যেমন: শিক্ষকতা / কৃষি / ব্যবসা",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা এবং জন্মসূত্রে ও আইনত বাংলাদেশের প্রকৃত নাগরিক। আমার জানামতে তিনি কোনো রাষ্ট্রবিরোধী বা আইন পরিপন্থী কর্মকাণ্ডে লিপ্ত নহেন। তাঁহার নৈতিক চরিত্র উত্তম।"
        ),
        CertificateType(
            id = "character",
            title = "চারিত্রিক সনদ",
            englishName = "Character Certificate",
            description = "উত্তম নৈতিক চরিত্র ও কোনো অপরাধমূলক কাজে লিপ্ত না থাকার প্রত্যয়ন",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "nid_birth_no",
                    label = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নং",
                    hint = "এনআইডি বা জন্ম নিবন্ধন নম্বর",
                    type = FieldType.NUMBER,
                    required = true
                ),
                CertificateField(
                    key = "purpose",
                    label = "সনদ ব্যবহারের উদ্দেশ্য",
                    hint = "যেমন: চাকুরির আবেদন / বিশ্ববিদ্যালয়ে ভর্তি",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি আমার ব্যক্তিগতভাবে পরিচিত। তিনি সৎ, চরিত্রবান ও শান্ত স্বভাবের নাগরিক। আমার জানামতে তিনি রাষ্ট্র ও সমাজবিরোধী কোনো কার্যকলাপে জড়িত নহেন। আমি তাঁহার জীবনের সর্বাঙ্গীন উন্নতি ও সাফল্য কামনা করি।"
        ),
        CertificateType(
            id = "succession",
            title = "ওয়ারিশান সনদ",
            englishName = "Succession Certificate",
            description = "মৃত ব্যক্তির বৈধ উত্তরাধিকারী/ওয়ারিশগণের পূর্ণাঙ্গ তালিকা সম্বলিত সনদ",
            category = "উত্তরাধিকার",
            isSuccession = true,
            specificFields = listOf(
                CertificateField(
                    key = "deceased_name",
                    label = "মৃত ব্যক্তির নাম",
                    hint = "মরহুম/মরহুমা ব্যক্তির পূর্ণ নাম",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "death_date",
                    label = "মৃত্যুর তারিখ",
                    hint = "দিন/মাস/বছর",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "deceased_nid",
                    label = "মৃত ব্যক্তির এনআইডি/মৃত্যু নিবন্ধন নং",
                    hint = "এনআইডি বা মৃত্যু নিবন্ধন নং",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, মরহুম/মরহুমা {deceased_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district} অত্র ইউনিয়নের স্থায়ী বাসিন্দা ছিলেন। তিনি বিগত {death_date} খ্রিঃ তারিখে মৃত্যুবরণ করেন। মৃত্যুকালে তিনি নিম্নবর্ণিত ওয়ারিশগণকে রেখে যান। আমার জানামতে নিম্নোক্ত ব্যক্তিগণ ব্যতীত তাঁহার আর কোনো বৈধ ওয়ারিশ নাই।"
        ),
        CertificateType(
            id = "annual_income",
            title = "বার্ষিক আয়ের প্রত্যয়ন",
            englishName = "Annual Income Certificate",
            description = "পারিবারিক বা ব্যক্তিগত বাৎসরিক আয়ের প্রাতিষ্ঠানিক প্রত্যয়ন",
            category = "আর্থিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "income_amount",
                    label = "বার্ষিক আয়ের পরিমাণ (অংকে)",
                    hint = "যেমন: ১,২০,০০০",
                    helperText = "টাকার পরিমাণ বাংলা বা ইংরেজি অংকে লিখুন",
                    type = FieldType.NUMBER,
                    required = true
                ),
                CertificateField(
                    key = "income_in_words",
                    label = "বার্ষিক আয়ের পরিমাণ (কথায়)",
                    hint = "যেমন: এক লক্ষ বিশ হাজার টাকা মাত্র",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "income_source",
                    label = "আয়ের মূল উৎস",
                    hint = "যেমন: কৃষি / ক্ষুদ্র ব্যবসা / বেতন",
                    type = FieldType.TEXT,
                    required = true
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা। স্থানীয় তদন্ত ও তথ্যানুযায়ী তাঁহার {income_source} উৎস হইতে সর্বমোট বাৎসরিক আয় {income_amount} (কথায়: {income_in_words}) টাকা। তাঁহার আর্থিক অবস্থা সাধারণ ও স্বচ্ছ।"
        ),
        CertificateType(
            id = "landless",
            title = "ভূমিহীন সনদ",
            englishName = "Landless Certificate",
            description = "আবেদনকারীর কোনো আবাদি বা বসতবাড়ি জমি না থাকার প্রত্যয়ন",
            category = "সামাজিক সুরক্ষা",
            specificFields = listOf(
                CertificateField(
                    key = "nid_birth_no",
                    label = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নং",
                    hint = "এনআইডি বা জন্ম নিবন্ধন",
                    type = FieldType.NUMBER,
                    required = true
                ),
                CertificateField(
                    key = "living_condition",
                    label = "বর্তমান বসবাসের ধরন",
                    hint = "যেমন: খাস জমিতে / ভাড়া বাসায় / অন্যের আশ্রয়ে",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। স্থানীয় তদন্তে নিশ্চিত হওয়া গিয়াছে যে, তাঁহার ও তাঁহার পরিবারের নামে অত্র এলাকায় কোনো আবাদি বা অনাবাদি জমিজমা কিংবা বসতভিটা নাই। তিনি একজন প্রকৃত ভূমিহীন ব্যক্তি।"
        ),
        CertificateType(
            id = "unmarried",
            title = "অবিবাহিত সনদ",
            englishName = "Unmarried Certificate",
            description = "অদ্যাবধি কোনো বিবাহ বন্ধনে আবদ্ধ না হওয়ার প্রত্যয়ন",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "nid_birth_no",
                    label = "জাতীয় পরিচয়পত্র / জন্ম নিবন্ধন নং",
                    hint = "এনআইডি বা জন্ম নিবন্ধন নং",
                    type = FieldType.NUMBER,
                    required = true
                ),
                CertificateField(
                    key = "dob",
                    label = "জন্ম তারিখ",
                    hint = "দিন/মাস/বছর",
                    type = FieldType.DATE,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। অদ্যবধি তিনি কোনো বিবাহ বন্ধনে আবদ্ধ হন নাই। তিনি সম্পূর্ণ অবিবাহিত।"
        ),
        CertificateType(
            id = "married",
            title = "বিবাহিত সনদ",
            englishName = "Married Certificate",
            description = "বৈবাহিক সম্পর্ক ও দাম্পত্য জীবনের সত্যায়ন পত্র",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "spouse_name",
                    label = "স্বামী/স্ত্রীর নাম",
                    hint = "স্বামী অথবা স্ত্রীর পূর্ণ নাম",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "marriage_date",
                    label = "বিবাহের তারিখ",
                    hint = "যেমন: ১৫/০৩/২০১৮",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি {spouse_name}-এর সহিত ধর্মীয় ও আইনগত বিধান মোতাবেক বিবাহ বন্ধনে আবদ্ধ হইয়া বর্তমানে সুখে-শান্তিতে দাম্পত্য জীবন যাপন করিতেছেন।"
        ),
        CertificateType(
            id = "non_remarriage",
            title = "পুনর্বিবাহ না হওয়া সনদ",
            englishName = "Non-remarriage Certificate",
            description = "স্বামী/স্ত্রীর মৃত্যুর পর পুনরায় বিবাহ না করার প্রত্যয়ন",
            category = "সামাজিক সুরক্ষা",
            specificFields = listOf(
                CertificateField(
                    key = "late_spouse_name",
                    label = "মৃত স্বামী/স্ত্রীর নাম",
                    hint = "মরহুম/মরহুমা স্বামী বা স্ত্রীর নাম",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "nid_birth_no",
                    label = "জাতীয় পরিচয়পত্র নং",
                    hint = "আবেদনকারীর এনআইডি নং",
                    type = FieldType.NUMBER,
                    required = true
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, মৃত স্বামী: {late_spouse_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তাঁহার স্বামীর মৃত্যুর পর হইতে তিনি অদ্যবধি কোনো দ্বিতীয় বা পুনর্বিবাহে আবদ্ধ হন নাই। তিনি বিধবা জীবন যাপন করিতেছেন।"
        ),
        CertificateType(
            id = "disability",
            title = "প্রতিবন্ধী প্রত্যয়ন",
            englishName = "Disability Certificate",
            description = "শারীরিক/দৃষ্টি/বুদ্ধি প্রতিবন্ধিতা সংক্রান্ত প্রাথমিক প্রত্যয়ন",
            category = "সামাজিক সুরক্ষা",
            specificFields = listOf(
                CertificateField(
                    key = "disability_type",
                    label = "প্রতিবন্ধিতার ধরন",
                    hint = "যেমন: শারীরিক / দৃষ্টি / শ্রবণ / বুদ্ধি প্রতিবন্ধী",
                    type = FieldType.DROPDOWN,
                    options = listOf("শারীরিক প্রতিবন্ধী", "দৃষ্টি প্রতিবন্ধী", "শ্রবণ ও বাক প্রতিবন্ধী", "বুদ্ধি প্রতিবন্ধী", "বহুমাত্রিক প্রতিবন্ধী"),
                    required = true
                ),
                CertificateField(
                    key = "disability_card_no",
                    label = "সুবর্ণ নাগরিক কার্ড নং (যদি থাকে)",
                    hint = "সমাজসেবা কার্ড নম্বর",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র দেওয়া যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের একজন স্থায়ী বাসিন্দা এবং তিনি জন্মগত/দুর্ঘটনাজনিত কারণে একজন প্রকৃত {disability_type}। তাঁহার প্রতি সহানুভূতি ও সরকারি সহায়তা একান্ত কাম্য।"
        ),
        CertificateType(
            id = "freedom_fighter_child",
            title = "মুক্তিযোদ্ধা সন্তান সনদ",
            englishName = "Freedom Fighter Child Certificate",
            description = "বীর মুক্তিযোদ্ধার সন্তান/সন্ততি হিসেবে প্রত্যয়ন পত্র",
            category = "বিশেষ প্রত্যয়ন",
            specificFields = listOf(
                CertificateField(
                    key = "ff_name",
                    label = "বীর মুক্তিযোদ্ধার নাম",
                    hint = "বীর মুক্তিযোদ্ধার পূর্ণ নাম",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "ff_gazette_no",
                    label = "গেজেট / লাল মুক্তিবার্তা নং",
                    hint = "গেজেট নম্বর ও তারিখ",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "relation_with_ff",
                    label = "মুক্তিযোদ্ধার সাথে সম্পর্ক",
                    hint = "যেমন: পুত্র / কন্যা",
                    type = FieldType.DROPDOWN,
                    options = listOf("পুত্র", "কন্যা", "নাতি", "নাতনি"),
                    required = true
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। তিনি মহান মুক্তিযুদ্ধে অংশগ্রহণকারী বীর মুক্তিযোদ্ধা {ff_name} (গেজেট/মুক্তিবার্তা নং: {ff_gazette_no})-এর ঔরসজাত {relation_with_ff}। তিনি মুক্তিযুদ্ধের গৌরবোজ্জ্বল পরিবারের সদস্য।"
        ),
        CertificateType(
            id = "permanent_resident",
            title = "স্থায়ী বাসিন্দা সনদ",
            englishName = "Permanent Resident Certificate",
            description = "ইউনিয়নের নির্দিষ্ট গ্রাম ও ঠিকানায় স্থায়ী বসবাসের প্রত্যয়ন",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "holding_no",
                    label = "হোল্ডিং নং",
                    hint = "যেমন: ১২/ক",
                    type = FieldType.TEXT,
                    required = false
                ),
                CertificateField(
                    key = "years_living",
                    label = "কত বছর যাবৎ বসবাস করছেন",
                    hint = "যেমন: জন্মগতভাবে / ২০ বছর",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি পরিবার-পরিজনসহ দীর্ঘদিন যাবৎ অত্র ইউনিয়নের উল্লেখিত ঠিকানায় স্থায়ীভাবে বসবাস করিয়া আসিতেছেন। তিনি অত্র এলাকার একজন সুপরিচিত ও সম্মানিত স্থায়ী অধিবাসী।"
        ),
        CertificateType(
            id = "poverty",
            title = "দরিদ্র সনদ",
            englishName = "Poverty Certificate",
            description = "অসহায় ও দরিদ্র পরিবারের সদস্যদের জন্য আর্থিক সহায়তার প্রত্যয়ন",
            category = "সামাজিক সুরক্ষা",
            specificFields = listOf(
                CertificateField(
                    key = "family_members",
                    label = "পরিবারের সদস্য সংখ্যা",
                    hint = "যেমন: ৫ জন",
                    type = FieldType.NUMBER,
                    required = false
                ),
                CertificateField(
                    key = "aid_purpose",
                    label = "সাহায্যের উদ্দেশ্য",
                    hint = "যেমন: শিক্ষা উপবৃত্তি / চিকিৎসা অনুদান",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী বাসিন্দা। তিনি অত্যন্ত দরিদ্র, নিঃস্ব ও অনগ্রসর পরিবারের সদস্য। তাঁহার পক্ষে প্রয়োজনীয় ব্যয় নির্বাহ করা কষ্টসাধ্য। তিনি যেকোনো সরকারি ও বেসরকারি আর্থিক সহযোগিতা ও অনুদান পাওয়ার যোগ্য।"
        ),
        CertificateType(
            id = "business_status",
            title = "ব্যবসা প্রত্যয়ন",
            englishName = "Business Certificate",
            description = "ইউনিয়ন এলাকায় ব্যবসা প্রতিষ্ঠান পরিচালনা সংক্রান্ত প্রত্যয়ন",
            category = "বাণিজ্যিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "business_name",
                    label = "ব্যবসা প্রতিষ্ঠানের নাম",
                    hint = "যেমন: মেসার্স রহিম ট্রেডার্স",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "trade_license_no",
                    label = "ট্রেড লাইসেন্স নং",
                    hint = "যেমন: ইউপি/২০২৪/৭৮",
                    type = FieldType.TEXT,
                    required = false
                ),
                CertificateField(
                    key = "business_type",
                    label = "ব্যবসার প্রকৃতি",
                    hint = "যেমন: মুদি দোকান / হার্ডওয়্যার / ফার্মেসি",
                    type = FieldType.TEXT,
                    required = true
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন পত্র প্রদান করা যাইতেছে যে, {applicant_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের বাজারে '{business_name}' নামে একটি {business_type} ব্যবসা সফলতার সহিত আইনানুগভাবে পরিচালনা করিয়া আসিতেছেন। তাঁহার ব্যবসা প্রতিষ্ঠানটি বৈধ ও সুনামের সহিত পরিচালিত।"
        ),
        CertificateType(
            id = "death",
            title = "মৃত্যু প্রত্যয়ন",
            englishName = "Death Certificate",
            description = "ইউনিয়ন বাসিন্দার স্বাভাবিক মৃত্যুর ঘটনা সংক্রান্ত প্রত্যয়ন",
            category = "নাগরিক সেবা",
            specificFields = listOf(
                CertificateField(
                    key = "deceased_name",
                    label = "মৃত ব্যক্তির নাম",
                    hint = "মরহুম/মরহুমা ব্যক্তির পূর্ণ নাম",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "date_of_death",
                    label = "মৃত্যুর তারিখ",
                    hint = "দিন/মাস/বছর",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "place_of_death",
                    label = "মৃত্যুর স্থান",
                    hint = "যেমন: নিজ বাসভবনে / হাসপাতালে",
                    type = FieldType.TEXT,
                    required = true
                ),
                CertificateField(
                    key = "age_at_death",
                    label = "মৃত্যুকালে বয়স",
                    hint = "যেমন: ৬৫ বছর",
                    type = FieldType.TEXT,
                    required = false
                )
            ),
            bodyTemplate = "এই মর্মে প্রত্যয়ন করা যাইতেছে যে, {deceased_name}, পিতা/স্বামী: {father_husband_name}, মাতা: {mother_name}, গ্রাম: {village}, ওয়ার্ড নং: {ward_no}, ডাকঘর: {post_office}, উপজেলা: {upazila}, জেলা: {district}। তিনি অত্র ইউনিয়নের স্থায়ী অধিবাসী ছিলেন। তিনি বিগত {date_of_death} খ্রিঃ তারিখে {place_of_death}-এ স্বাভাবিক মৃত্যুবরণ করিয়াছেন। আমি তাঁহার বিদেহী আত্মার শান্তি ও মাগফেরাত কামনা করি।"
        )
    )

    fun findById(id: String): CertificateType? = ALL_TYPES.find { it.id == id }
}
