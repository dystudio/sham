grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "warn" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
		mavenLocal()
		mavenCentral()
        grailsPlugins()
        grailsHome()
        grailsCentral()
    }
    dependencies {
		def webdriverVersion = '2.29.0'
		test "org.seleniumhq.selenium:selenium-firefox-driver:$webdriverVersion"
		test "org.codehaus.geb:geb-spock:0.6.0"

		test('org.codehaus.groovy.modules.http-builder:http-builder:0.5.1') {
			excludes "xml-apis", 'groovy'
		}
    }
	
	plugins {
		compile ':hibernate:1.3.7'
		compile ':tomcat:1.3.7'

		compile ':fixtures:1.1'
		compile ':build-test-data:1.1.2'
		compile ':sham:0.3'

		compile ':cache-headers:1.1.5'

		compile ":commentable:0.7.7"

		runtime ":jquery:1.7.1"
		compile ":cached-resources:1.0"
		compile ":zipped-resources:1.0"
		compile ":resources:1.1.6"
		runtime ":lesscss-resources:0.6.1"

		test ":spock:0.5-groovy-1.7"
		test ":geb:0.6.0"
	}
}
