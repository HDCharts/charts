
import org.gradle.api.publish.maven.MavenPom

object ChartsPublishing {
    fun configurePom(
        pom: MavenPom,
        moduleName: String,
        moduleDescription: String,
    ) {
        pom.name.set(moduleName)
        pom.description.set(moduleDescription)
        pom.inceptionYear.set(Config.INCEPTION_YEAR)
        pom.url.set(Config.PROJECT_URL)

        pom.licenses {
            license {
                name.set(Config.LICENSE_NAME)
                url.set(Config.LICENSE_URL)
            }
        }
        pom.developers {
            developer {
                id.set(Config.DEVELOPER_ID)
                name.set(Config.DEVELOPER_NAME)
            }
        }
        pom.issueManagement {
            system.set(Config.ISSUE_SYSTEM)
            url.set(Config.ISSUE_URL)
        }
        pom.scm {
            connection.set(Config.SCM_CONNECTION)
            url.set(Config.PROJECT_URL)
        }
    }
}
