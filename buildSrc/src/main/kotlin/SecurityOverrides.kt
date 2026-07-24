object SecurityOverrides {
    const val PROTOBUF_GROUP = "com.google.protobuf"
    val PROTOBUF_ARTIFACTS =
        setOf(
            "protobuf-java",
            "protobuf-java-util",
            "protobuf-javalite",
            "protobuf-kotlin",
            "protobuf-kotlin-lite",
        )
    const val PROTOBUF_REASON = "Mitigate CVE-2024-7254 / GHSA-735f-pc8j-v9w8"

    const val JDOM_GROUP = "org.jdom"
    const val JDOM_ARTIFACT = "jdom2"
    const val JDOM_REASON = "Mitigate XXE in org.jdom:jdom2 < 2.0.6.1"

    const val NETTY_GROUP = "io.netty"
    const val NETTY_HTTP2_ARTIFACT = "netty-codec-http2"
    const val NETTY_CODEC_ARTIFACT = "netty-codec"
    const val NETTY_HTTP_ARTIFACT = "netty-codec-http"
    const val NETTY_HTTP2_REASON = "Mitigate MadeYouReset HTTP/2 DDoS in io.netty:netty-codec-http2 <= 4.1.123.Final"
    const val NETTY_CODEC_REASON = "Mitigate zip bomb DoS in io.netty:netty-codec < 4.1.125.Final"
    const val NETTY_HTTP_REASON = "Mitigate CRLF injection in io.netty:netty-codec-http < 4.1.129.Final"

    const val COMMONS_LANG_GROUP = "org.apache.commons"
    const val COMMONS_LANG3_ARTIFACT = "commons-lang3"
    const val COMMONS_LANG3_REASON = "Mitigate uncontrolled recursion in org.apache.commons:commons-lang3 < 3.18.0"

    const val LOGBACK_GROUP = "ch.qos.logback"
    val LOGBACK_ARTIFACTS = setOf("logback-core", "logback-classic")
    const val LOGBACK_REASON = "Mitigate ACE in ch.qos.logback:logback-core < 1.3.16"

    const val HTTP_COMPONENTS_GROUP = "org.apache.httpcomponents"
    const val HTTP_CLIENT_ARTIFACT = "httpclient"
    const val HTTP_CLIENT_REASON = "Mitigate host confusion in org.apache.httpcomponents:httpclient < 4.5.13"

    const val JOSE4J_GROUP = "org.bitbucket.b_c"
    const val JOSE4J_ARTIFACT = "jose4j"
    const val JOSE4J_REASON = "Mitigate DoS via compressed JWE content in org.bitbucket.b_c:jose4j < 0.9.6"

    const val JACKSON_CORE_GROUP = "com.fasterxml.jackson.core"
    const val JACKSON_CORE_ARTIFACT = "jackson-core"
    const val JACKSON_CORE_REASON =
        "Mitigate Number Length Constraint Bypass in com.fasterxml.jackson.core:jackson-core <= 2.18.5"
}
